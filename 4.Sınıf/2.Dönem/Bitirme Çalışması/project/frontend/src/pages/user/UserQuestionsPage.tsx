import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchMyQuestions } from '@/api/user';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { normalizeEntityId } from '@/lib/submissionStatus';

const UserQuestionsPage: React.FC = () => {
    const { data: questions, isLoading } = useQuery({
        queryKey: ['my-questions'],
        queryFn: fetchMyQuestions,
    });

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold">Sorularim</h1>
                <p className="text-sm text-slate-500">Sordugun sorularin durumunu buradan takip edebilirsin.</p>
            </div>

            {isLoading ? (
                <Card><CardContent className="p-6 text-sm text-slate-500">Yukleniyor...</CardContent></Card>
            ) : questions?.length ? (
                <div className="space-y-4">
                    {questions.map((question) => {
                        const statusLabel = question.answerText
                            ? (question.isPublished ? 'Yayinlandi' : 'Cevaplandi')
                            : 'Cevap Bekliyor';
                        const statusVariant = question.answerText ? 'default' : 'secondary';

                        return (
                            <Card key={normalizeEntityId(question.id)}>
                                <CardHeader className="pb-3">
                                    <div className="flex items-start justify-between gap-3">
                                        <CardTitle className="text-base leading-snug">{question.questionText}</CardTitle>
                                        <Badge variant={statusVariant}>{statusLabel}</Badge>
                                    </div>
                                    <p className="text-xs text-slate-500">
                                        {question.createdAt ? new Date(question.createdAt).toLocaleString() : '-'}
                                    </p>
                                </CardHeader>
                                {question.answerText && (
                                    <CardContent className="pt-0">
                                        <details>
                                            <summary className="cursor-pointer text-sm font-medium text-slate-700">Cevabi Goster</summary>
                                            <p className="mt-2 rounded-md border bg-slate-50 p-3 text-sm text-slate-700">{question.answerText}</p>
                                        </details>
                                    </CardContent>
                                )}
                            </Card>
                        );
                    })}
                </div>
            ) : (
                <Card><CardContent className="p-6 text-sm text-slate-500">Henuz soru sormadin.</CardContent></Card>
            )}
        </div>
    );
};

export default UserQuestionsPage;
