import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { updateMyProfile, updateMyPassword, changeEmail, fetchMyProfile } from '@/api/user';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import { Loader2, UserCircle, Lock, Mail } from 'lucide-react';

const profileSchema = z.object({
    firstName: z.string().min(2, 'Required'),
    lastName: z.string().min(2, 'Required'),
    phoneNumber: z.string().optional(),
    githubUrl: z.union([z.string().url(), z.string().length(0)]).optional(),
    linkedinUrl: z.union([z.string().url(), z.string().length(0)]).optional(),
});

const passwordSchema = z.object({
    currentPassword: z.string().min(1, 'Required'),
    newPassword: z.string().min(8, 'Min 8 characters'),
    confirmPassword: z.string(),
}).refine(data => data.newPassword === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
});

const emailSchema = z.object({
    newEmail: z.string().email('Invalid email address'),
});

const AdminProfilePage: React.FC = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();

    const { data: profile, isLoading } = useQuery({
        queryKey: ['admin-profile'],
        queryFn: fetchMyProfile,
    });

    // --- Profile ---
    const updateProfileMutation = useMutation({
        mutationFn: updateMyProfile,
        onSuccess: () => {
            toast({ title: 'Profile updated' });
            queryClient.invalidateQueries({ queryKey: ['admin-profile'] });
        },
        onError: () => toast({ variant: 'destructive', title: 'Update failed' }),
    });

    const profileForm = useForm<z.infer<typeof profileSchema>>({
        resolver: zodResolver(profileSchema),
        values: profile ? {
            firstName: profile.firstName,
            lastName: profile.lastName,
            phoneNumber: profile.phoneNumber || '',
            githubUrl: profile.githubUrl || '',
            linkedinUrl: profile.linkedinUrl || '',
        } : { firstName: '', lastName: '', phoneNumber: '', githubUrl: '', linkedinUrl: '' },
    });

    // --- Password ---
    const passwordMutation = useMutation({
        mutationFn: async (values: z.infer<typeof passwordSchema>) => {
            await updateMyPassword({ oldPassword: values.currentPassword, newPassword: values.newPassword });
        },
        onSuccess: () => {
            toast({ title: 'Password updated' });
            passwordForm.reset();
        },
        onError: (err: any) => toast({ variant: 'destructive', title: 'Failed', description: err.response?.data?.message || 'Could not update password.' }),
    });

    const passwordForm = useForm<z.infer<typeof passwordSchema>>({ resolver: zodResolver(passwordSchema) });

    // --- Email ---
    const emailMutation = useMutation({
        mutationFn: (values: z.infer<typeof emailSchema>) => changeEmail(values.newEmail),
        onSuccess: () => {
            toast({ title: 'Email updated', description: 'You may need to re-login.' });
            emailForm.reset();
            queryClient.invalidateQueries({ queryKey: ['admin-profile'] });
        },
        onError: (err: any) => toast({ variant: 'destructive', title: 'Failed', description: err.response?.data?.message || 'Could not update email.' }),
    });

    const emailForm = useForm<z.infer<typeof emailSchema>>({ resolver: zodResolver(emailSchema) });

    if (isLoading) return <div className="flex justify-center p-10"><Loader2 className="h-8 w-8 animate-spin text-muted-foreground" /></div>;

    return (
        <div className="space-y-6 max-w-3xl">
            <div>
                <h1 className="text-2xl font-bold text-foreground">Account Settings</h1>
                <p className="mt-1 text-sm text-muted-foreground">Manage your administrator profile and security settings.</p>
            </div>

            <Tabs defaultValue="profile">
                <TabsList className="mb-4">
                    <TabsTrigger value="profile" className="gap-2"><UserCircle className="w-4 h-4" />Profile</TabsTrigger>
                    <TabsTrigger value="security" className="gap-2"><Lock className="w-4 h-4" />Security</TabsTrigger>
                    <TabsTrigger value="email" className="gap-2"><Mail className="w-4 h-4" />Email</TabsTrigger>
                </TabsList>

                {/* Profile Tab */}
                <TabsContent value="profile">
                    <Card>
                        <CardHeader>
                            <CardTitle>Personal Information</CardTitle>
                            <CardDescription>Update your display name and contact details.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={profileForm.handleSubmit(d => updateProfileMutation.mutate(d))} className="space-y-4 max-w-md">
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="space-y-2">
                                        <Label>First Name</Label>
                                        <Input {...profileForm.register('firstName')} />
                                        {profileForm.formState.errors.firstName && <p className="text-red-500 text-xs">{profileForm.formState.errors.firstName.message}</p>}
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Last Name</Label>
                                        <Input {...profileForm.register('lastName')} />
                                        {profileForm.formState.errors.lastName && <p className="text-red-500 text-xs">{profileForm.formState.errors.lastName.message}</p>}
                                    </div>
                                </div>
                                <div className="space-y-2">
                                    <Label>Current Email</Label>
                                    <Input disabled value={profile?.email || ''} className="bg-muted/40" />
                                    <p className="text-xs text-muted-foreground">To change your email, use the Email tab.</p>
                                </div>
                                <div className="space-y-2">
                                    <Label>Phone</Label>
                                    <Input {...profileForm.register('phoneNumber')} placeholder="+90 555 000 0000" />
                                </div>
                                <div className="space-y-2">
                                    <Label>GitHub URL</Label>
                                    <Input {...profileForm.register('githubUrl')} placeholder="https://github.com/..." />
                                    {profileForm.formState.errors.githubUrl && <p className="text-red-500 text-xs">Invalid URL</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label>LinkedIn URL</Label>
                                    <Input {...profileForm.register('linkedinUrl')} placeholder="https://linkedin.com/in/..." />
                                    {profileForm.formState.errors.linkedinUrl && <p className="text-red-500 text-xs">Invalid URL</p>}
                                </div>
                                <Button type="submit" disabled={updateProfileMutation.isPending}>
                                    {updateProfileMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                                    Save Profile
                                </Button>
                            </form>
                        </CardContent>
                    </Card>
                </TabsContent>

                {/* Security Tab */}
                <TabsContent value="security">
                    <Card>
                        <CardHeader>
                            <CardTitle>Change Password</CardTitle>
                            <CardDescription>Use a strong password with at least 8 characters.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={passwordForm.handleSubmit(d => passwordMutation.mutate(d))} className="space-y-4 max-w-md">
                                <div className="space-y-2">
                                    <Label>Current Password</Label>
                                    <Input type="password" {...passwordForm.register('currentPassword')} />
                                    {passwordForm.formState.errors.currentPassword && <p className="text-red-500 text-xs">{passwordForm.formState.errors.currentPassword.message}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label>New Password</Label>
                                    <Input type="password" {...passwordForm.register('newPassword')} />
                                    {passwordForm.formState.errors.newPassword && <p className="text-red-500 text-xs">{passwordForm.formState.errors.newPassword.message}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label>Confirm New Password</Label>
                                    <Input type="password" {...passwordForm.register('confirmPassword')} />
                                    {passwordForm.formState.errors.confirmPassword && <p className="text-red-500 text-xs">{passwordForm.formState.errors.confirmPassword.message}</p>}
                                </div>
                                <Button type="submit" disabled={passwordMutation.isPending}>
                                    {passwordMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                                    Update Password
                                </Button>
                            </form>
                        </CardContent>
                    </Card>
                </TabsContent>

                {/* Email Tab — Admin Only */}
                <TabsContent value="email">
                    <Card>
                        <CardHeader>
                            <CardTitle>Change Email Address</CardTitle>
                            <CardDescription>
                                Your current email is <span className="font-semibold text-foreground">{profile?.email}</span>.
                                After changing, you may need to re-login.
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={emailForm.handleSubmit(d => emailMutation.mutate(d))} className="space-y-4 max-w-md">
                                <div className="space-y-2">
                                    <Label>New Email Address</Label>
                                    <Input type="email" {...emailForm.register('newEmail')} placeholder="admin@company.com" />
                                    {emailForm.formState.errors.newEmail && <p className="text-red-500 text-xs">{emailForm.formState.errors.newEmail.message}</p>}
                                </div>
                                <Button type="submit" variant="destructive" disabled={emailMutation.isPending}>
                                    {emailMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                                    Update Email
                                </Button>
                            </form>
                        </CardContent>
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    );
};

export default AdminProfilePage;
