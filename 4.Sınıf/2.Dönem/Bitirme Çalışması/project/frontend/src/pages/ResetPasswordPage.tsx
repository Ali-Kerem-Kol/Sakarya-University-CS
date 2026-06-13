import React from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { useForm, SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuthMutations } from '@/auth/useAuthMutations';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Loader2, Lock, XCircle } from 'lucide-react';
import { useToast } from "@/hooks/use-toast";

const resetPasswordSchema = z.object({
    newPassword: z.string().min(8, 'Password must be at least 8 characters'),
    confirmPassword: z.string().min(8, 'Password must be at least 8 characters'),
}).refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"],
});

type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;

const ResetPasswordPage: React.FC = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const navigate = useNavigate();
    const { toast } = useToast();
    const { resetPasswordMutation } = useAuthMutations();

    const { register, handleSubmit, formState: { errors } } = useForm<ResetPasswordFormValues>({
        resolver: zodResolver(resetPasswordSchema),
    });

    const onSubmit: SubmitHandler<ResetPasswordFormValues> = (data) => {
        if (!token) {
            toast({ variant: "destructive", title: "Invalid Link", description: "Missing reset token." });
            return;
        }

        resetPasswordMutation.mutate({ token, newPassword: data.newPassword }, {
            onSuccess: () => {
                toast({ title: "Success", description: "Password has been reset. Please login." });
                navigate('/login');
            },
            onError: (err: any) => {
                toast({
                    variant: "destructive",
                    title: "Error",
                    description: err.response?.data?.message || "Failed to reset password."
                });
            }
        });
    };

    if (!token) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-background p-4">
                <Card className="max-w-md w-full border-border/70 bg-card/95 shadow-xl shadow-black/15">
                    <CardHeader>
                        <CardTitle className="text-destructive flex items-center gap-2">
                            <XCircle className="w-6 h-6" /> Invalid Link
                        </CardTitle>
                        <CardDescription>Missing or invalid password reset token.</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="mb-4 text-sm text-muted-foreground">
                            The link you followed may be broken or expired. Please request a new password reset link.
                        </p>
                        <Link to="/forgot-password">
                            <Button className="w-full">Request New Link</Button>
                        </Link>
                    </CardContent>
                </Card>
            </div>
        );
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-background p-4">
            <Card className="max-w-md w-full border-border/70 bg-card/95 shadow-xl shadow-black/15">
                <CardHeader className="space-y-1">
                    <CardTitle className="text-2xl font-bold text-center">Reset Password</CardTitle>
                    <CardDescription className="text-center">
                        Enter your new password below.
                    </CardDescription>
                </CardHeader>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <CardContent className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="newPassword">New Password</Label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                                <Input
                                    id="newPassword"
                                    type="password"
                                    placeholder="••••••••"
                                    className="pl-10"
                                    {...register('newPassword')}
                                />
                            </div>
                            {errors.newPassword && (
                                <p className="text-sm text-destructive">{errors.newPassword.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="confirmPassword">Confirm Password</Label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                                <Input
                                    id="confirmPassword"
                                    type="password"
                                    placeholder="••••••••"
                                    className="pl-10"
                                    {...register('confirmPassword')}
                                />
                            </div>
                            {errors.confirmPassword && (
                                <p className="text-sm text-destructive">{errors.confirmPassword.message}</p>
                            )}
                        </div>
                    </CardContent>
                    <CardFooter className="flex flex-col gap-4">
                        <Button
                            type="submit"
                            className="w-full"
                            disabled={resetPasswordMutation.isPending}
                        >
                            {resetPasswordMutation.isPending ? (
                                <><Loader2 className="mr-2 h-4 w-4 animate-spin" /> Resetting...</>
                            ) : (
                                "Reset Password"
                            )}
                        </Button>
                        <Link to="/login" className="text-sm text-primary hover:underline">
                            Back to Login
                        </Link>
                    </CardFooter>
                </form>
            </Card>
        </div>
    );
};

export default ResetPasswordPage;
