import React, { useRef } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchMyDocuments, uploadCv } from '@/api/user';
import { mapBackendErrorToMessage, openDocument } from '@/api/client';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { useToast } from "@/hooks/use-toast";
import { FileText, Upload, Loader2, Download, Eye, RefreshCw } from 'lucide-react';
import { isPdfFile } from '@/api/multipart';

const UserDocumentsPage: React.FC = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();
    const fileInputRef = useRef<HTMLInputElement>(null);

    const { data: documents, isLoading } = useQuery({
        queryKey: ['documents'],
        queryFn: fetchMyDocuments,
    });

    const uploadCvMutation = useMutation({
        mutationFn: uploadCv,
        onSuccess: () => {
            toast({ title: "CV Updated", description: "Your curriculum vitae has been successfully updated." });
            queryClient.invalidateQueries({ queryKey: ['documents'] });
            if (fileInputRef.current) fileInputRef.current.value = '';
        },
        onError: (err: unknown) => {
            const msg = mapBackendErrorToMessage(err, "Error uploading file.");
            toast({ variant: "destructive", title: "Upload failed", description: msg });
        }
    });

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            if (!isPdfFile(file)) {
                toast({ variant: "destructive", title: "Invalid format", description: "Sadece PDF yukleyebilirsiniz." });
                if (fileInputRef.current) fileInputRef.current.value = '';
                return;
            }
            uploadCvMutation.mutate(file);
        }
    };

    const handleView = async (url: string) => {
        try {
            await openDocument(url, { mode: 'view', contentType: 'application/pdf' });
        } catch {
            toast({ variant: "destructive", title: "View Failed", description: "Could not open file." });
        }
    };

    if (isLoading) {
        return <div className="flex justify-center p-10"><Loader2 className="animate-spin" /></div>;
    }

    const cv = documents?.find((d: any) => d.documentType === 'CV');

    return (
        <div className="space-y-6 max-w-3xl mx-auto animate-in fade-in slide-in-from-bottom-4">
            <h1 className="text-3xl font-bold text-foreground">My Documents</h1>

            <Card className="overflow-hidden border-border/70 shadow-sm">
                <CardHeader className="border-b border-border/70 bg-muted/35">
                    <CardTitle className="flex items-center gap-2">
                        <FileText className="w-5 h-5 text-primary" />
                        Curriculum Vitae
                    </CardTitle>
                    <CardDescription>Your CV is your primary document for job applications.</CardDescription>
                </CardHeader>
                <CardContent className="p-6">
                    {cv ? (
                        <div className="space-y-6">
                            <div className="flex flex-col justify-between gap-4 rounded-xl border border-border/70 bg-card/80 p-4 shadow-sm md:flex-row md:items-center">
                                <div className="flex items-center gap-4">
                                    <div className="rounded-lg bg-primary/10 p-3 text-primary">
                                        <FileText className="w-8 h-8" />
                                    </div>
                                    <div className="overflow-hidden">
                                        <p className="truncate font-semibold text-foreground" title={cv.fileName}>Uploaded CV: {cv.fileName}</p>
                                        <p className="mt-1 text-xs font-medium text-muted-foreground">
                                            Last Updated: {new Date(cv.createdAt).toLocaleDateString()}
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <Button size="sm" variant="outline" onClick={() => handleView(`/users/me/documents/${cv.id}/download`)}>
                                        <Eye className="w-4 h-4 mr-2" /> View
                                    </Button>
                                    <Button size="sm" variant="outline" onClick={() => openDocument(`/users/me/documents/${cv.id}/download`, { mode: 'download', defaultFileName: cv.fileName })}>
                                        <Download className="w-4 h-4 mr-2" /> Download
                                    </Button>
                                </div>
                            </div>

                            <div className="flex flex-col items-center gap-4 border-t border-border/70 pt-4 sm:flex-row">
                                <input
                                    type="file"
                                    ref={fileInputRef}
                                    className="hidden"
                                    accept="application/pdf,.pdf"
                                    onChange={handleFileChange}
                                />
                                <Button
                                    variant="secondary"
                                    className="w-full sm:w-auto"
                                    onClick={() => fileInputRef.current?.click()}
                                    disabled={uploadCvMutation.isPending}
                                >
                                    {uploadCvMutation.isPending ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <RefreshCw className="w-4 h-4 mr-2" />}
                                    Replace CV (Upload New)
                                </Button>
                                <p className="text-[10px] italic text-muted-foreground">Changing your CV will update it for all active applications.</p>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center py-16 px-4">
                            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-muted/45">
                                <Upload className="w-8 h-8 text-muted-foreground" />
                            </div>
                            <h3 className="mb-2 text-lg font-medium text-foreground">No CV Found</h3>
                            <p className="mx-auto mb-8 max-w-sm text-muted-foreground">
                                You haven't uploaded a CV yet. A PDF CV is mandatory to apply for projects.
                            </p>

                            <input
                                type="file"
                                ref={fileInputRef}
                                className="hidden"
                                accept="application/pdf,.pdf"
                                onChange={handleFileChange}
                            />
                            <Button
                                size="lg"
                                onClick={() => fileInputRef.current?.click()}
                                disabled={uploadCvMutation.isPending}
                                className="shadow-lg shadow-primary/20"
                            >
                                {uploadCvMutation.isPending ? <Loader2 className="w-5 h-5 mr-2 animate-spin" /> : <Upload className="w-5 h-5 mr-2" />}
                                Select PDF File
                            </Button>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
};

export default UserDocumentsPage;
