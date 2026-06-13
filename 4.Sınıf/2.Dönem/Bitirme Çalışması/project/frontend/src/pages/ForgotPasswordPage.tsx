import React from 'react';
import { useForm } from 'react-hook-form';
import { useAuthMutations } from '@/auth/useAuthMutations';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Link } from 'react-router-dom';

const ForgotPasswordPage: React.FC = () => {
    const { forgotPasswordMutation } = useAuthMutations();
    const { register, handleSubmit } = useForm<{ email: string }>();

    const onSubmit = (data: { email: string }) => {
        forgotPasswordMutation.mutate(data.email);
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-background px-4">
            <Card className="w-full max-w-md border-border/70 bg-card/95 shadow-xl shadow-black/15">
                <CardHeader>
                    <CardTitle>Reset Password</CardTitle>
                    <CardDescription>Enter your email to receive a reset link.</CardDescription>
                </CardHeader>
                <CardContent>
                    {forgotPasswordMutation.isSuccess ? (
                        <div className="rounded-md border border-emerald-300 bg-emerald-50 p-4 text-emerald-700 dark:border-emerald-500/30 dark:bg-emerald-500/10 dark:text-emerald-300">
                            Reset link sent! Please check your email.
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                            <div className="space-y-2">
                                <Label>Email</Label>
                                <Input {...register('email', { required: true })} type="email" placeholder="student@ogr.sakarya.edu.tr" />
                            </div>
                            <Button type="submit" className="w-full" disabled={forgotPasswordMutation.isPending}>
                                {forgotPasswordMutation.isPending ? 'Sending...' : 'Send Reset Link'}
                            </Button>
                        </form>
                    )}
                </CardContent>
                <CardFooter>
                    <Link to="/login" className="text-sm text-primary hover:underline mx-auto">Back to Login</Link>
                </CardFooter>
            </Card>
        </div>
    );
};

export default ForgotPasswordPage;
