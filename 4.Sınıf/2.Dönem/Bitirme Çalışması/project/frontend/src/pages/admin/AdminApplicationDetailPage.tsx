import React from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchSubmissionDetail } from '@/api/admin';
import { downloadFile, viewFile } from '@/api/client';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ArrowLeft, Download, AlertCircle, Loader2, User } from 'lucide-react';
import { cn } from '@/lib/utils';
import { getSubmissionStatusLabel, normalizeEntityId, normalizeSubmissionStatus } from '@/lib/submissionStatus';

const AdminApplicationDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const { data: app, isLoading, isError } = useQuery({
        queryKey: ['admin-submission-detail', id],
        queryFn: () => fetchSubmissionDetail(id!),
        enabled: !!id,
    });

    const getStatusBadge = (status: string | null | undefined) => {
        const normalizedStatus = normalizeSubmissionStatus(status);
        return (
            <Badge
                className={cn(
                    'font-medium',
                    normalizedStatus === 'PENDING' && 'bg-yellow-100 text-yellow-800',
                    normalizedStatus === 'APPROVED' && 'bg-green-100 text-green-700',
                    normalizedStatus === 'REJECTED' && 'bg-red-100 text-red-700',
                    normalizedStatus === 'UNKNOWN' && 'bg-slate-100 text-slate-700'
                )}
            >
                {getSubmissionStatusLabel(status)}
            </Badge>
        );
    };

    if (isLoading) return (
        <div className="flex flex-col items-center justify-center p-20 space-y-4">
            <Loader2 className="w-10 h-10 animate-spin text-blue-500" />
            <p className="text-slate-500 font-medium">Loading Submission Details...</p>
        </div>
    );

    if (isError || !app) return (
        <div className="flex flex-col items-center justify-center p-20 space-y-6 text-center">
            <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center">
                <AlertCircle className="w-10 h-10 text-slate-400" />
            </div>
            <div>
                <h1 className="text-2xl font-bold text-slate-900">Submission Not Found</h1>
                <p className="text-slate-500 mt-2">The submission you are looking for does not exist.</p>
            </div>
            <Button asChild variant="outline">
                <Link to="/admin/submissions">
                    <ArrowLeft className="w-4 h-4 mr-2" />
                    Back to Submissions
                </Link>
            </Button>
        </div>
    );

    const applicantFirstName = app.user?.firstName ?? app.userFirstName ?? '';
    const applicantLastName = app.user?.lastName ?? app.userLastName ?? '';
    const applicantName = [applicantFirstName, applicantLastName].filter(Boolean).join(' ').trim() || 'Bilinmeyen Kullanici';
    const applicantEmail = app.user?.email ?? app.userEmail ?? '-';

    return (
        <div className="space-y-6 animate-in fade-in duration-500">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="sm" onClick={() => navigate(-1)} className="text-slate-500">
                        <ArrowLeft className="w-4 h-4 mr-2" />
                        Back
                    </Button>
                    <div>
                        <h1 className="text-2xl font-bold tracking-tight text-slate-900 leading-none">Submission Detail</h1>
                        <p className="text-sm text-slate-500 mt-1">ID: <span className="font-mono text-xs">{app.id}</span></p>
                    </div>
                </div>
                <div className="flex items-center gap-4 bg-white p-2 px-4 rounded-full border border-slate-100 shadow-sm">
                    <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">Current Status</span>
                    {getStatusBadge(app.status)}
                </div>
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
                <Card className="border-slate-200">
                    <CardHeader className="bg-slate-50/50 border-b border-slate-100">
                        <CardTitle className="text-sm font-bold uppercase tracking-wider text-slate-500 flex items-center gap-2">
                            <User className="w-4 h-4" /> Applicant Information
                        </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-6 space-y-4">
                        <div className="flex items-center gap-4">
                            <div className="w-12 h-12 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-lg font-bold">
                                {(applicantFirstName?.[0] || '?')}
                            </div>
                            <div>
                                <h3 className="text-lg font-bold text-slate-900">{applicantName}</h3>
                                <p className="text-sm text-slate-500">{applicantEmail}</p>
                            </div>
                        </div>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4">
                            <div>
                                <label className="text-xs font-bold text-slate-400 uppercase">School Email</label>
                                <p className="text-sm font-medium">{applicantEmail}</p>
                            </div>
                            <div>
                                <label className="text-xs font-bold text-slate-400 uppercase">User ID</label>
                                <p className="text-sm font-medium">{normalizeEntityId(app.user?.id ?? app.userId) || '-'}</p>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                <Card className="border-slate-200">
                    <CardHeader className="bg-slate-50/50 border-b border-slate-100">
                        <CardTitle className="text-sm font-bold uppercase tracking-wider text-slate-500">Posting Details</CardTitle>
                    </CardHeader>
                    <CardContent className="pt-6 space-y-4">
                        <div>
                            <label className="text-xs font-bold text-slate-400 uppercase">Title</label>
                            <p className="text-lg font-bold text-slate-900">{app.postingTitle}</p>
                        </div>
                        <div className="flex gap-4">
                            <div>
                                <label className="text-xs font-bold text-slate-400 uppercase">Category</label>
                                <p><Badge variant="outline">{app.postingCategory}</Badge></p>
                            </div>
                            <div>
                                <label className="text-xs font-bold text-slate-400 uppercase">Applied On</label>
                                <p className="text-sm font-medium">{new Date(app.createdAt).toLocaleDateString()}</p>
                            </div>
                        </div>
                    </CardContent>
                </Card>

                <Card className="border-slate-200 lg:col-span-2">
                    <CardHeader className="bg-slate-50/50 border-b border-slate-100">
                        <CardTitle className="text-sm font-bold uppercase tracking-wider text-slate-500">Documents</CardTitle>
                    </CardHeader>
                    <CardContent className="pt-6">
                        {app.cvDownloadUrl ? (
                            <div
                                className="flex items-center justify-between p-4 bg-white border border-slate-200 rounded-xl shadow-sm hover:border-blue-200 transition-colors cursor-pointer"
                                onClick={() => viewFile(app.cvDownloadUrl!, 'application/pdf')}
                            >
                                <div className="flex items-center gap-3">
                                    <div className="p-2 bg-blue-50 rounded-lg">
                                        <Download className="w-5 h-5 text-blue-600" />
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-slate-900">Curriculum Vitae (PDF)</p>
                                        <p className="text-xs text-slate-500">Uploaded at registration</p>
                                    </div>
                                </div>
                                <Button
                                    variant="ghost"
                                    size="sm"
                                    className="text-blue-600"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        downloadFile(app.cvDownloadUrl!, 'cv.pdf');
                                    }}
                                >
                                    Download
                                </Button>
                            </div>
                        ) : (
                            <div className="text-center py-8 text-slate-400 italic">No CV available.</div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
};

export default AdminApplicationDetailPage;
