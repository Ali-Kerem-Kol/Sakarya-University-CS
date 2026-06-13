import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { answerQuestion, deleteQuestion, fetchAdminQuestions, setQuestionPublished } from '@/api/admin';
import { mapBackendErrorToMessage } from '@/api/client';
import { AdminQuestionStatusFilter } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { useToast } from '@/hooks/use-toast';
import { normalizeEntityId } from '@/lib/submissionStatus';
import { useSearchParams } from 'react-router-dom';

interface AdminQuestionsPageProps {
    postingId?: string;
    embedded?: boolean;
}

const AdminQuestionsPage: React.FC<AdminQuestionsPageProps> = ({ postingId, embedded = false }) => {
    const queryClient = useQueryClient();
    const { toast } = useToast();
    const [searchParams] = useSearchParams();

    const [statusFilter, setStatusFilter] = useState<string>('ALL');
    const [dateSort, setDateSort] = useState<'NEWEST' | 'OLDEST'>('NEWEST');
    const [activeQuestionId, setActiveQuestionId] = useState<string>('');
    const [answerText, setAnswerText] = useState('');
    const [specificDate, setSpecificDate] = useState('');

    const resolvedPostingId = postingId ?? searchParams.get('postingId') ?? undefined;

    const adminQuestionParams = useMemo(() => {
        const params: { postingId?: string; status?: AdminQuestionStatusFilter } = {};
        if (resolvedPostingId) params.postingId = resolvedPostingId;
        if (statusFilter !== 'ALL') params.status = statusFilter as AdminQuestionStatusFilter;
        return params;
    }, [resolvedPostingId, statusFilter]);

    const { data: questions, isLoading } = useQuery({
        queryKey: ['admin-questions', adminQuestionParams],
        queryFn: () => fetchAdminQuestions(adminQuestionParams),
    });

    const toDateKey = (value?: string | null) => {
        if (!value) return '';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return '';
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${date.getFullYear()}-${month}-${day}`;
    };

    const sortedQuestions = useMemo(() => {
        const items = [...(questions ?? [])].filter((question) => !specificDate || toDateKey(question.createdAt) === specificDate);
        items.sort((a, b) => {
            const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
            const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
            return dateSort === 'NEWEST' ? bTime - aTime : aTime - bTime;
        });
        return items;
    }, [questions, dateSort, specificDate]);

    const answerMutation = useMutation({
        mutationFn: ({ questionId, text }: { questionId: string; text: string }) => answerQuestion(questionId, text),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-questions'] });
            toast({ title: 'Cevap kaydedildi' });
            setActiveQuestionId('');
            setAnswerText('');
        },
        onError: (err: unknown) => {
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: mapBackendErrorToMessage(err, 'Cevap kaydedilemedi.') });
        },
    });

    const publishMutation = useMutation({
        mutationFn: ({ questionId, published }: { questionId: string; published: boolean }) => setQuestionPublished(questionId, published),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-questions'] });
            toast({ title: 'Yayin durumu guncellendi' });
        },
        onError: (err: unknown) => {
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: mapBackendErrorToMessage(err, 'Yayin durumu guncellenemedi.') });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: deleteQuestion,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-questions'] });
            toast({ title: 'Soru silindi' });
        },
        onError: (err: unknown) => {
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: mapBackendErrorToMessage(err, 'Soru silinemedi.') });
        },
    });

    return (
        <div className="space-y-6">
            {!embedded && (
                <div>
                    <h1 className="text-2xl font-bold">Sorular</h1>
                    <p className="text-sm text-slate-500">Sorulari cevapla ve yayin durumunu yonet.</p>
                </div>
            )}

            <Card>
                <CardHeader>
                    <CardTitle className="text-base">Filtreler</CardTitle>
                </CardHeader>
                <CardContent className="grid gap-4 md:grid-cols-3">
                    <div className="space-y-2">
                        <Label>Status</Label>
                        <Select value={statusFilter} onValueChange={setStatusFilter}>
                            <SelectTrigger><SelectValue /></SelectTrigger>
                            <SelectContent>
                                <SelectItem value="ALL">Hepsi</SelectItem>
                                <SelectItem value="UNANSWERED">Unanswered</SelectItem>
                                <SelectItem value="ANSWERED">Answered</SelectItem>
                                <SelectItem value="PUBLISHED">Published</SelectItem>
                                <SelectItem value="UNPUBLISHED">Unpublished</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="space-y-2">
                        <Label>Tarih</Label>
                        <Select value={dateSort} onValueChange={(value) => setDateSort(value as 'NEWEST' | 'OLDEST')}>
                            <SelectTrigger><SelectValue /></SelectTrigger>
                            <SelectContent>
                                <SelectItem value="NEWEST">En Yeni</SelectItem>
                                <SelectItem value="OLDEST">En Eski</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="space-y-2">
                        <Label>Belirli Tarih</Label>
                        <Input type="date" value={specificDate} onChange={(event) => setSpecificDate(event.target.value)} />
                    </div>
                </CardContent>
            </Card>

            <Card>
                <CardContent className="p-0">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Ogrenci</TableHead>
                                <TableHead>Soru</TableHead>
                                <TableHead>Created At</TableHead>
                                <TableHead>Cevap</TableHead>
                                <TableHead>Publish</TableHead>
                                <TableHead className="text-right">Aksiyon</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {isLoading ? (
                                <TableRow><TableCell colSpan={6} className="p-6 text-center">Yukleniyor...</TableCell></TableRow>
                            ) : sortedQuestions.length === 0 ? (
                                <TableRow><TableCell colSpan={6} className="p-6 text-center text-slate-500">Kayit bulunamadi.</TableCell></TableRow>
                            ) : (
                                sortedQuestions.map((question) => {
                                    const questionId = normalizeEntityId(question.id);
                                    const hasAnswer = Boolean(question.answerText);

                                    return (
                                        <TableRow key={questionId}>
                                            <TableCell>
                                                <div className="font-medium">{question.askedByName || 'N/A'}</div>
                                                <div className="text-xs text-slate-500">{question.askedByEmail || '-'}</div>
                                            </TableCell>
                                            <TableCell className="max-w-[380px]">
                                                <p className="truncate">{question.questionText}</p>
                                                <p className="text-xs text-slate-500 mt-1">{question.postingTitle}</p>
                                            </TableCell>
                                            <TableCell>{question.createdAt ? new Date(question.createdAt).toLocaleString() : '-'}</TableCell>
                                            <TableCell>
                                                {hasAnswer ? (
                                                    <div className="text-sm">
                                                        <p className="line-clamp-2">{question.answerText}</p>
                                                        <span className="text-xs text-green-700">Cevaplandi</span>
                                                    </div>
                                                ) : (
                                                    <span className="text-xs text-amber-700">Cevap yok</span>
                                                )}
                                            </TableCell>
                                            <TableCell>
                                                <Button
                                                    size="sm"
                                                    variant="outline"
                                                    disabled={!hasAnswer || publishMutation.isPending}
                                                    onClick={() => publishMutation.mutate({ questionId, published: !question.isPublished })}
                                                >
                                                    {question.isPublished ? 'Unpublish' : 'Publish'}
                                                </Button>
                                            </TableCell>
                                            <TableCell className="text-right">
                                                <div className="flex justify-end gap-2">
                                                    <Button size="sm" variant="outline" onClick={() => {
                                                        setActiveQuestionId(questionId);
                                                        setAnswerText(question.answerText ?? '');
                                                    }}>
                                                        Answer
                                                    </Button>
                                                    <Button size="sm" variant="outline" onClick={() => deleteMutation.mutate(questionId)}>
                                                        Delete
                                                    </Button>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    );
                                })
                            )}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>

            {activeQuestionId && (
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base">Cevap Yaz</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                        <Input
                            value={answerText}
                            onChange={(event) => setAnswerText(event.target.value)}
                            placeholder="Cevabinizi yazin..."
                        />
                        <div className="flex gap-2">
                            <Button
                                onClick={() => answerMutation.mutate({ questionId: activeQuestionId, text: answerText })}
                                disabled={!answerText.trim() || answerMutation.isPending}
                            >
                                Kaydet
                            </Button>
                            <Button variant="outline" onClick={() => {
                                setActiveQuestionId('');
                                setAnswerText('');
                            }}>
                                Vazgec
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            )}
        </div>
    );
};

export default AdminQuestionsPage;
