import React, { useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { useAuthMutations } from '@/auth/useAuthMutations';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { CheckCircle, XCircle, Loader2 } from 'lucide-react';

const VerifyPage: React.FC = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const { verifyMutation } = useAuthMutations();

    useEffect(() => {
        if (token) {
            verifyMutation.mutate(token);
        }
    }, [token]);

    if (!token) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-background px-4">
                <Card className="max-w-md w-full border-border/70 bg-card/95 shadow-xl shadow-black/15">
                    <CardHeader>
                        <CardTitle className="text-destructive flex items-center gap-2">
                            <XCircle /> Invalid Link
                        </CardTitle>
                        <CardDescription>Missing verification token.</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <Link to="/login"><Button className="w-full">Go to Login</Button></Link>
                    </CardContent>
                </Card>
            </div>
        );
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-background px-4">
            <Card className="max-w-md w-full border-border/70 bg-card/95 text-center shadow-xl shadow-black/15">
                <CardHeader>
                    {verifyMutation.isPending && (
                        <div className="flex flex-col items-center">
                            <Loader2 className="animate-spin h-10 w-10 text-primary mb-4" />
                            <CardTitle>Verifying...</CardTitle>
                        </div>
                    )}
                    {verifyMutation.isSuccess && (
                        <div className="flex flex-col items-center">
                            <CheckCircle className="h-12 w-12 text-green-500 mb-4" />
                            <CardTitle>Account Verified!</CardTitle>
                            <CardDescription>Your email has been successfully verified.</CardDescription>
                        </div>
                    )}
                    {verifyMutation.isError && (
                        <div className="flex flex-col items-center">
                            <XCircle className="h-12 w-12 text-destructive mb-4" />
                            <CardTitle>Verification Failed</CardTitle>
                            <CardDescription>
                                {(verifyMutation.error as any)?.response?.data?.message || 'The token may be invalid or expired.'}
                            </CardDescription>
                        </div>
                    )}
                </CardHeader>
                <CardContent>
                    {(verifyMutation.isSuccess || verifyMutation.isError) && (
                        <Link to="/login">
                            <Button className="w-full mt-4">Proceed to Login</Button>
                        </Link>
                    )}
                </CardContent>
            </Card>
        </div>
    );
};

export default VerifyPage;
