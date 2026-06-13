import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchMySubmissions } from '@/api/user';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { getSubmissionStatusLabel, normalizeSubmissionStatus } from '@/lib/submissionStatus';

const UserHomePage: React.FC = () => {
    const { data: submissions } = useQuery({
        queryKey: ['mySubmissions'],
        queryFn: fetchMySubmissions,
    });

    const approvedCount = submissions?.filter((item) => normalizeSubmissionStatus(item.status) === 'APPROVED').length ?? 0;
    const pendingCount = submissions?.filter((item) => normalizeSubmissionStatus(item.status) === 'PENDING').length ?? 0;
    const latestSubmission = submissions && submissions.length > 0 ? submissions[0] : null;

    return (
        <div className="space-y-6 animate-in fade-in duration-500">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h2 className="text-3xl font-bold tracking-tight">Hos geldin</h2>
                    <p className="text-muted-foreground">Projelere dogrudan basvurabilir, durumlarini hesabindan takip edebilirsin.</p>
                </div>
                <Link to="/">
                    <Button>Projeleri Gor</Button>
                </Link>
            </div>

            <div className="grid gap-4 md:grid-cols-3">
                <Card>
                    <CardHeader className="pb-3">
                        <CardTitle className="text-base">Toplam Basvuru</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold">{submissions?.length ?? 0}</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="pb-3">
                        <CardTitle className="text-base">Onay Bekleyen</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold">{pendingCount}</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="pb-3">
                        <CardTitle className="text-base">Onaylanan</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-3xl font-bold">{approvedCount}</p>
                    </CardContent>
                </Card>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Son Basvuru</CardTitle>
                </CardHeader>
                <CardContent>
                    {latestSubmission ? (
                        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                            <div>
                                <p className="font-semibold">{latestSubmission.postingTitle || latestSubmission.positionKey || 'Pozisyon Bilinmiyor'}</p>
                                <p className="text-sm text-muted-foreground">
                                    Basvuru Tarihi: {latestSubmission.createdAt ? new Date(latestSubmission.createdAt).toLocaleDateString() : '-'}
                                </p>
                            </div>
                            <Badge variant={normalizeSubmissionStatus(latestSubmission.status) === 'REJECTED' ? 'destructive' : 'secondary'}>
                                {getSubmissionStatusLabel(latestSubmission.status)}
                            </Badge>
                        </div>
                    ) : (
                        <p className="text-sm text-muted-foreground">Henuz basvuru bulunmuyor.</p>
                    )}
                </CardContent>
            </Card>
        </div>
    );
};

export default UserHomePage;
