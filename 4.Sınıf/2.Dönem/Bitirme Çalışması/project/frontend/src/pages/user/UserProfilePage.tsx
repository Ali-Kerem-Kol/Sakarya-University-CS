import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { updateMyProfile, updateMyPassword, fetchMyProfile } from '../../api/user';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Label } from '@/components/ui/label';
import { useToast } from "@/hooks/use-toast";
import { Loader2 } from 'lucide-react';

// Define schemas
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
    message: "Passwords do not match",
    path: ["confirmPassword"]
});

const UserProfilePage: React.FC = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();

    // -- Profile --
    const { data: profile, isLoading } = useQuery({
        queryKey: ['profile'],
        queryFn: fetchMyProfile,
    });

    const updateProfileMutation = useMutation({
        mutationFn: updateMyProfile,
        onSuccess: () => {
            toast({ title: "Profile updated" });
            queryClient.invalidateQueries({ queryKey: ['profile'] });
        },
        onError: () => toast({ variant: "destructive", title: "Update failed" })
    });

    const profileForm = useForm<z.infer<typeof profileSchema>>({
        resolver: zodResolver(profileSchema),
        values: profile ? {
            firstName: profile.firstName,
            lastName: profile.lastName,
            phoneNumber: profile.phoneNumber || '',
            githubUrl: profile.githubUrl || '',
            linkedinUrl: profile.linkedinUrl || ''
        } : {
            firstName: '',
            lastName: '',
            phoneNumber: '',
            githubUrl: '',
            linkedinUrl: ''
        },
    });

    const onProfileSubmit = (data: z.infer<typeof profileSchema>) => updateProfileMutation.mutate(data);

    // -- Password --
    const passwordMutation = useMutation({
        mutationFn: async (values: z.infer<typeof passwordSchema>) => {
            await updateMyPassword({
                oldPassword: values.currentPassword,
                newPassword: values.newPassword
            });
        },
        onSuccess: () => {
            toast({ title: "Password updated successfully" });
            passwordForm.reset();
        },
        onError: (err: any) => toast({
            variant: "destructive",
            title: "Security Update Failed",
            description: err.response?.data?.message || "Could not update password. Check your current password."
        })
    });

    const passwordForm = useForm<z.infer<typeof passwordSchema>>({ resolver: zodResolver(passwordSchema) });
    const onPasswordSubmit = (data: z.infer<typeof passwordSchema>) => passwordMutation.mutate(data);

    if (isLoading) return <div className="flex justify-center p-20"><Loader2 className="animate-spin w-10 h-10 text-primary" /></div>;

    return (
        <div className="space-y-6 max-w-4xl mx-auto animate-in fade-in duration-500">
            <div>
                <h1 className="text-3xl font-bold text-foreground">Account Settings</h1>
                <p className="mt-1 text-muted-foreground">Manage your personal information and account security.</p>
            </div>

            <Tabs defaultValue="profile" className="w-full">
                <TabsList className="mb-6 rounded-xl bg-muted/55 p-1">
                    <TabsTrigger value="profile" className="rounded-lg px-8 data-[state=active]:bg-card data-[state=active]:shadow-sm">Profile</TabsTrigger>
                    <TabsTrigger value="security" className="rounded-lg px-8 data-[state=active]:bg-card data-[state=active]:shadow-sm">Security</TabsTrigger>
                </TabsList>

                <TabsContent value="profile">
                    <Card className="border-border/70">
                        <CardHeader>
                            <CardTitle>Personal Information</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={profileForm.handleSubmit(onProfileSubmit)} className="space-y-6">
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div className="space-y-2">
                                        <Label>First Name</Label>
                                        <Input {...profileForm.register('firstName')} placeholder="John" />
                                        {profileForm.formState.errors.firstName && <p className="text-red-500 text-xs mt-1">{profileForm.formState.errors.firstName.message}</p>}
                                    </div>
                                    <div className="space-y-2">
                                        <Label>Last Name</Label>
                                        <Input {...profileForm.register('lastName')} placeholder="Doe" />
                                        {profileForm.formState.errors.lastName && <p className="text-red-500 text-xs mt-1">{profileForm.formState.errors.lastName.message}</p>}
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <Label>Email Address</Label>
                                    <Input disabled value={profile?.email || ''} className="border-border/60 bg-muted/40" />
                                    <p className="text-[10px] text-muted-foreground">Student emails cannot be changed once registered.</p>
                                </div>

                                <div className="space-y-2">
                                    <Label>Phone Number</Label>
                                    <Input {...profileForm.register('phoneNumber')} placeholder="+90 5XX XXX XX XX" />
                                </div>

                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
                                    <div className="space-y-2">
                                        <Label>GitHub URL</Label>
                                        <Input {...profileForm.register('githubUrl')} placeholder="https://github.com/username" />
                                        {profileForm.formState.errors.githubUrl && <p className="text-red-500 text-xs">Please enter a valid URL</p>}
                                    </div>
                                    <div className="space-y-2">
                                        <Label>LinkedIn URL</Label>
                                        <Input {...profileForm.register('linkedinUrl')} placeholder="https://linkedin.com/in/username" />
                                        {profileForm.formState.errors.linkedinUrl && <p className="text-red-500 text-xs">Please enter a valid URL</p>}
                                    </div>
                                </div>

                                <div className="border-t border-border/70 pt-4">
                                    <Button className="w-full md:w-auto px-10" disabled={updateProfileMutation.isPending}>
                                        {updateProfileMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                                        Update Profile
                                    </Button>
                                </div>
                            </form>
                        </CardContent>
                    </Card>
                </TabsContent>

                <TabsContent value="security" className="space-y-6">
                    <Card className="border-border/70">
                        <CardHeader>
                            <CardTitle>Change Password</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={passwordForm.handleSubmit(onPasswordSubmit)} className="space-y-5 max-w-md">
                                <div className="space-y-2">
                                    <Label>Current Password</Label>
                                    <Input type="password" {...passwordForm.register('currentPassword')} />
                                    {passwordForm.formState.errors.currentPassword && <p className="text-red-500 text-xs">{passwordForm.formState.errors.currentPassword.message}</p>}
                                </div>
                                <div className="space-y-2">
                                    <Label>New Password</Label>
                                    <Input type="password" {...passwordForm.register('newPassword')} />
                                    {passwordForm.formState.errors.newPassword && <p className="text-red-500 text-xs">{passwordForm.formState.errors.newPassword.message}</p>}
                                    <p className="text-[10px] text-muted-foreground">At least 8 characters required.</p>
                                </div>
                                <div className="space-y-2">
                                    <Label>Confirm New Password</Label>
                                    <Input type="password" {...passwordForm.register('confirmPassword')} />
                                    {passwordForm.formState.errors.confirmPassword && <p className="text-red-500 text-xs">{passwordForm.formState.errors.confirmPassword.message}</p>}
                                </div>
                                <div className="pt-4">
                                    <Button className="w-full" disabled={passwordMutation.isPending}>
                                        {passwordMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                                        Update Password
                                    </Button>
                                </div>
                            </form>
                        </CardContent>
                    </Card>
                </TabsContent>
            </Tabs>
        </div>
    );
};

export default UserProfilePage;
