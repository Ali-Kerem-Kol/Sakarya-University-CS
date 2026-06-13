import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchMySubmissions } from '@/api/user';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Loader2, AlertCircle } from 'lucide-react';
import { getSubmissionStatusLabel, normalizeEntityId, normalizeSubmissionStatus } from '@/lib/submissionStatus';

const UserSubmissionsPage: React.FC = () => {
    const { data: applications, isLoading, error } = useQuery({
        queryKey: ['mySubmissions'],
        queryFn: fetchMySubmissions,
    });

    const getStatusContent = (status: string) => {
        const normalizedStatus = normalizeSubmissionStatus(status);
        const variant = normalizedStatus === 'APPROVED' ? 'default' : normalizedStatus === 'REJECTED' ? 'destructive' : 'secondary';
        return <Badge variant={variant}>{getSubmissionStatusLabel(status)}</Badge>;
    };

    if (isLoading) return <div className="flex justify-center p-10"><Loader2 className="animate-spin" /></div>;
    if (error) return <div className="p-4 text-red-500 flex items-center gap-2"><AlertCircle /> Failed to load applications.</div>;

    return (
        <div className="space-y-6 container mx-auto p-4">
            <h1 className="text-3xl font-bold mb-4">My Applications</h1>

            {applications?.length === 0 ? (
                <Card>
                    <CardContent className="py-10 text-center text-muted-foreground">
                        You haven't applied to any positions yet.
                        <br />
                        <Link to="/" className="text-primary hover:underline mt-2 inline-block">Browse Openings</Link>
                    </CardContent>
                </Card>
            ) : (
                <div className="rounded-md border">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Position</TableHead>
                                <TableHead>Applied On</TableHead>
                                <TableHead>Status</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {applications?.map((app) => (
                                <TableRow key={app.id}>
                                    <TableCell className="font-medium">
                                        {app.postingTitle || app.positionKey || 'Pozisyon Bilinmiyor'}
                                        {(app as any).postingCategory && <Badge variant="outline" className="ml-2 text-[10px] h-4">{(app as any).postingCategory}</Badge>}
                                        <div className="text-xs text-muted-foreground mt-1">Application ID: {normalizeEntityId(app.id).slice(0, 8)}...</div>
                                    </TableCell>
                                    <TableCell>{app.createdAt ? new Date(app.createdAt).toLocaleDateString() : '-'}</TableCell>
                                    <TableCell>{getStatusContent(app.status)}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            )}
        </div>
    );
};

export default UserSubmissionsPage;
