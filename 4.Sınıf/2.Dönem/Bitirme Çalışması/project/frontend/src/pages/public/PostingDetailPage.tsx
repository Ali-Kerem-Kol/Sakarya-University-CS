import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchPostingDetail } from '@/api/postings';
import { askQuestion, createSubmission, fetchMySubmissions, fetchPostingPublicQa } from '@/api/user';
import { downloadFile, viewFile } from '@/api/client';
import { useAuth } from '@/auth/AuthContext';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useToast } from '@/hooks/use-toast';
import { Skeleton } from '@/components/ui/skeleton';
import { ArrowLeft, CheckCircle, Download, MessageCircle, Send } from 'lucide-react';
import { Card, CardContent } from '@/components/ui/card';
import { PublicQuestion } from '@/types';

const PostingDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { isAuthenticated, user } = useAuth();
    const { toast } = useToast();
    const queryClient = useQueryClient();
    const [questionText, setQuestionText] = useState('');
    const [questionError, setQuestionError] = useState('');

    const { data: posting, isLoading: loadingPosting } = useQuery({
        queryKey: ['posting', id],
        queryFn: () => fetchPostingDetail(id!),
        enabled: !!id,
    });

    const { data: mySubmissions } = useQuery({
        queryKey: ['mySubmissions'],
        queryFn: fetchMySubmissions,
        enabled: isAuthenticated && user?.role === 'USER',
    });

    const hasApplied = mySubmissions?.some((submission) => submission.postingId === id);

    const { data: publicQuestions } = useQuery({
        queryKey: ['questions-public', id],
        queryFn: () => fetchPostingPublicQa(id!),
        enabled: !!id,
    });

    const applyMutation = useMutation({
        mutationFn: createSubmission,
        onSuccess: () => {
            toast({ title: 'Application Submitted', description: 'Good luck!' });
            queryClient.invalidateQueries({ queryKey: ['mySubmissions'] });
        },
        onError: (error: any) => {
            if (error.response?.status === 409) {
                toast({ title: 'Already Applied', description: 'You have already applied for this position.', variant: 'destructive' });
            } else {
                toast({ title: 'Error', description: 'Failed to submit application.', variant: 'destructive' });
            }
        },
    });

    const askMutation = useMutation({
        mutationFn: (text: string) => askQuestion(id!, text),
        onSuccess: () => {
            toast({ title: 'Soru gonderildi', description: 'Soru basariyla kaydedildi.' });
            setQuestionText('');
            setQuestionError('');
            queryClient.invalidateQueries({ queryKey: ['questions-public', id] });
            queryClient.invalidateQueries({ queryKey: ['my-questions'] });
        },
        onError: (error: unknown) => {
            const status = (error as { response?: { status?: number; data?: { message?: string } } })?.response?.status;
            const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message;

            if (status === 401) {
                navigate('/login', { state: { returnUrl: `/postings/${id}` } });
                return;
            }

            if (status === 400 || status === 422) {
                setQuestionError(message || 'Form verisi hatali, tekrar deneyin.');
                return;
            }

            toast({ title: 'Islem basarisiz', description: message || 'Soru gonderilemedi.', variant: 'destructive' });
        },
    });

    const handleApply = () => {
        if (!isAuthenticated) {
            navigate('/login', { state: { returnUrl: `/postings/${id}` } });
            return;
        }
        if (id) {
            applyMutation.mutate(id);
        }
    };

    const handleDownload = async (downloadUrl: string, filename: string) => {
        try {
            await downloadFile(downloadUrl, filename);
        } catch {
            toast({ title: 'Download Failed', description: 'Could not download file.', variant: 'destructive' });
        }
    };

    const handleView = async (url: string) => {
        try {
            await viewFile(url, 'application/pdf');
        } catch {
            toast({ title: 'View Failed', description: 'Could not open file.', variant: 'destructive' });
        }
    };

    if (loadingPosting) {
        return (
            <div className="max-w-4xl mx-auto space-y-6">
                <Skeleton className="h-12 w-3/4" />
                <Skeleton className="h-6 w-1/2" />
                <Skeleton className="h-64 w-full" />
            </div>
        );
    }

    if (!posting) {
        return <div className="text-center py-10">Posting not found.</div>;
    }

    return (
        <div className="max-w-4xl mx-auto animate-in fade-in zoom-in-95 duration-500 space-y-8">
            <Button variant="ghost" onClick={() => navigate('/')} className="hover:bg-slate-100 -ml-4">
                <ArrowLeft className="w-4 h-4 mr-2" /> Back to Listings
            </Button>

            <div className="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
                <div className="p-8 border-b border-slate-100 bg-slate-50/50">
                    <div className="flex justify-between items-start mb-4">
                        <Badge variant="outline" className="bg-white text-sm px-3 py-1">
                            {posting.category}
                        </Badge>
                        <Badge variant={posting.status === 'CLOSED' ? 'destructive' : 'default'}>
                            {posting.status}
                        </Badge>
                    </div>
                    <h1 className="text-3xl font-bold text-slate-900 mb-2">{posting.title}</h1>
                    <p className="text-lg text-slate-500 font-medium">{posting.projectName}</p>
                </div>

                <div className="p-8 space-y-8">
                    <section>
                        <h3 className="text-lg font-semibold mb-3 text-slate-900">Job Description</h3>
                        <div className="prose prose-slate max-w-none text-slate-600 leading-relaxed whitespace-pre-wrap">
                            {posting.description}
                        </div>
                    </section>

                    {posting.projectDetails && (
                        <section>
                            <h3 className="text-lg font-semibold mb-3 text-slate-900">Project Details</h3>
                            <div className="prose prose-slate max-w-none text-slate-600 leading-relaxed whitespace-pre-wrap">
                                {posting.projectDetails}
                            </div>
                        </section>
                    )}

                    {posting.attachments && posting.attachments.length > 0 && (
                        <section>
                            <h3 className="text-lg font-semibold mb-3 text-slate-900">Attachments</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {posting.attachments.map((file) => (
                                    <div key={file.id} className="flex items-center justify-between p-4 bg-slate-50 rounded-lg border border-slate-200">
                                        <div className="flex items-center gap-3 overflow-hidden">
                                            <div className="p-2 bg-white rounded border border-slate-200">
                                                <Download className="w-4 h-4 text-slate-400" />
                                            </div>
                                            <span className="text-sm font-medium text-slate-700 truncate">{file.originalFileName}</span>
                                        </div>
                                        <div className="flex gap-2">
                                            <Button size="sm" variant="ghost" onClick={() => handleView(file.downloadUrl)}>
                                                View
                                            </Button>
                                            <Button size="sm" variant="ghost" onClick={() => handleDownload(file.downloadUrl, file.originalFileName)}>
                                                Download
                                            </Button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </section>
                    )}
                </div>

                <div className="p-8 bg-slate-50 border-t border-slate-100 flex justify-end">
                    {posting.status === 'CLOSED' ? (
                        <Button disabled variant="secondary" className="w-full md:w-auto">
                            Applications Closed
                        </Button>
                    ) : hasApplied ? (
                        <div className="flex items-center text-green-600 font-medium bg-green-50 px-4 py-2 rounded-lg border border-green-100">
                            <CheckCircle className="w-5 h-5 mr-2" /> Applied on {new Date(mySubmissions!.find((submission) => submission.postingId === id)!.createdAt).toLocaleDateString()}
                        </div>
                    ) : (
                        <Button
                            className="w-full md:w-auto text-lg px-8 py-6 shadow-lg shadow-primary/20 hover:shadow-primary/30 transition-all"
                            onClick={handleApply}
                            disabled={applyMutation.isPending}
                        >
                            {applyMutation.isPending ? 'Submitting...' : 'Apply Now'} <Send className="w-4 h-4 ml-2" />
                        </Button>
                    )}
                </div>
            </div>

            <Card>
                <CardContent className="p-6 space-y-4">
                    <div className="flex items-center gap-2">
                        <MessageCircle className="w-5 h-5 text-primary" />
                        <h3 className="text-lg font-semibold">Sorular & Cevaplar</h3>
                    </div>
                    <div className="space-y-4 max-h-[420px] overflow-y-auto pr-2">
                        {publicQuestions?.length === 0 ? (
                            <p className="text-sm text-muted-foreground italic">Bu ilan icin yayinlanmis soru yok.</p>
                        ) : (
                            publicQuestions?.map((question: PublicQuestion) => (
                                <div key={String(question.id)} className="bg-slate-50 p-4 rounded-lg text-sm space-y-2">
                                    <p className="font-medium text-slate-800">Q: {question.questionText}</p>
                                    {question.answerText && (
                                        <p className="text-slate-600 pl-4 border-l-2 border-primary/20">
                                            <span className="font-semibold text-primary">A:</span> {question.answerText}
                                        </p>
                                    )}
                                </div>
                            ))
                        )}
                    </div>

                    {isAuthenticated && user?.role === 'USER' && (
                        <div className="border-t pt-4">
                            <textarea
                                className="w-full rounded-md border border-slate-300 p-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 min-h-[80px]"
                                placeholder="Soru sor..."
                                value={questionText}
                                onChange={(event) => setQuestionText(event.target.value)}
                            />
                            <Button
                                size="sm"
                                className="mt-2 w-full"
                                onClick={() => askMutation.mutate(questionText)}
                                disabled={!questionText.trim() || askMutation.isPending}
                            >
                                Gonder
                            </Button>
                            {questionError && <p className="mt-2 text-xs text-red-600">{questionError}</p>}
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
};

export default PostingDetailPage;
