import React, { useMemo, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    fetchAdminPostings,
    createPosting,
    updatePosting,
    publishPosting,
    closePosting,
    reopenPosting,
    uploadPostingAttachments,
    deletePostingAttachment,
    deletePosting,
} from '@/api/admin';
import { mapBackendErrorToMessage } from '@/api/client';
import { isPdfFile } from '@/api/multipart';
import { Posting } from '@/types';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { Plus, Eye, EyeOff, Paperclip, Trash, RotateCcw, Settings, Search, MessageSquare } from 'lucide-react';
import { Link } from 'react-router-dom';
import ProjectTaskGraphManager from '@/components/ProjectTaskGraphManager';
import AdminQuestionsPage from '@/pages/admin/AdminQuestionsPage';

const getPostingStatusBadgeClass = (status?: string | null) => {
    const normalized = String(status ?? '').toUpperCase();
    if (normalized === 'PUBLISHED') {
        return 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-500/40 dark:bg-emerald-500/10 dark:text-emerald-300';
    }
    if (normalized === 'CLOSED') {
        return 'border-red-200 bg-red-50 text-red-700 dark:border-red-500/40 dark:bg-red-500/10 dark:text-red-300';
    }
    return 'border-border bg-muted/50 text-muted-foreground';
};

const AdminPostingsPage: React.FC = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();
    const [selectedPosting, setSelectedPosting] = useState<Posting | null>(null);
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [isAttachmentsOpen, setIsAttachmentsOpen] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState<Posting | null>(null);
    const [deleteConfirmText, setDeleteConfirmText] = useState('');
    const [taskManagerPosting, setTaskManagerPosting] = useState<Posting | null>(null);
    const [questionsPosting, setQuestionsPosting] = useState<Posting | null>(null);

    const [formCategory, setFormCategory] = useState<string>('BACKEND');
    const [formDescription, setFormDescription] = useState<string>('');
    const [formProjectDetails, setFormProjectDetails] = useState<string>('');

    const [filterCategory, setFilterCategory] = useState<string>('ALL');
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [sortBy, setSortBy] = useState<string>('CREATED_DESC');
    const [specificDate, setSpecificDate] = useState<string>('');

    const { data: postings, isLoading } = useQuery({
        queryKey: ['admin-postings'],
        queryFn: () => fetchAdminPostings(),
    });

    const createMutation = useMutation({
        mutationFn: createPosting,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-postings'] });
            setIsDialogOpen(false);
            toast({ title: 'Proje olusturuldu', description: 'Taslak proje basariyla olusturuldu.' });
        },
        onError: (err: any) => toast({ variant: 'destructive', title: 'Olusturma basarisiz', description: err.response?.data?.message || 'Proje olusturulamadi.' }),
    });

    const updateMutation = useMutation({
        mutationFn: (data: Partial<Posting>) => updatePosting(selectedPosting!.id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-postings'] });
            setIsDialogOpen(false);
            toast({ title: 'Proje guncellendi' });
        },
        onError: (err: any) => toast({ variant: 'destructive', title: 'Guncelleme basarisiz', description: err.response?.data?.message || 'Proje guncellenemedi.' }),
    });

    const publishMutation = useMutation({
        mutationFn: publishPosting,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-postings'] });
            toast({ title: 'Proje yayinlandi' });
        },
    });

    const closeMutation = useMutation({
        mutationFn: closePosting,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-postings'] });
            toast({ title: 'Proje gizlendi' });
        },
    });

    const reopenMutation = useMutation({
        mutationFn: reopenPosting,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-postings'] });
            toast({ title: 'Proje tekrar yayinlandi' });
        },
    });

    const deleteProjectMutation = useMutation({
        mutationFn: deletePosting,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-postings'] });
            toast({ title: 'Proje silindi' });
        },
        onError: (err: any) => {
            toast({ variant: 'destructive', title: 'Silme basarisiz', description: err?.response?.data?.message || 'Proje silinemedi.' });
        },
    });

    const categoryOptions = useMemo(() => {
        const set = new Set<string>();
        (postings ?? []).forEach((posting) => {
            const value = String(posting.category ?? '').trim();
            if (value) set.add(value);
        });
        return Array.from(set).sort((a, b) => a.localeCompare(b));
    }, [postings]);

    const visiblePostings = useMemo(() => {
        const toDateKey = (value?: string | null) => {
            if (!value) return '';
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) return '';
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${date.getFullYear()}-${month}-${day}`;
        };

        const source = [...(postings ?? [])];
        const normalizedSearch = searchTerm.trim().toLowerCase();

        const filtered = source.filter((posting) => {
            const categoryMatch = filterCategory === 'ALL' || posting.category === filterCategory;
            if (!categoryMatch) return false;
            if (specificDate && toDateKey(posting.createdAt) !== specificDate) return false;
            if (!normalizedSearch) return true;

            const haystack = [posting.title, posting.projectName, posting.category, posting.status]
                .filter(Boolean)
                .join(' ')
                .toLowerCase();

            return haystack.includes(normalizedSearch);
        });

        filtered.sort((a, b) => {
            if (sortBy === 'CATEGORY_ASC') return String(a.category).localeCompare(String(b.category));
            if (sortBy === 'CATEGORY_DESC') return String(b.category).localeCompare(String(a.category));
            if (sortBy === 'TITLE_ASC') return String(a.title).localeCompare(String(b.title));
            if (sortBy === 'TITLE_DESC') return String(b.title).localeCompare(String(a.title));

            const aTime = new Date(a.createdAt || 0).getTime();
            const bTime = new Date(b.createdAt || 0).getTime();
            if (sortBy === 'CREATED_ASC') return aTime - bTime;
            return bTime - aTime;
        });

        return filtered;
    }, [filterCategory, postings, searchTerm, sortBy, specificDate]);

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const formData = new FormData(e.currentTarget);
        const data: any = Object.fromEntries(formData.entries());
        data.category = formCategory;
        data.description = formDescription;
        data.projectDetails = formProjectDetails;

        if (selectedPosting) {
            updateMutation.mutate(data);
        } else {
            createMutation.mutate(data);
        }
    };

    const openCreate = () => {
        setSelectedPosting(null);
        setFormCategory('BACKEND');
        setFormDescription('');
        setFormProjectDetails('');
        setIsDialogOpen(true);
    };

    const openEdit = (posting: Posting) => {
        setSelectedPosting(posting);
        setFormCategory(posting.category || 'BACKEND');
        setFormDescription(posting.description || '');
        setFormProjectDetails(posting.projectDetails || '');
        setIsDialogOpen(true);
    };

    const openAttachments = (posting: Posting) => {
        setSelectedPosting(posting);
        setIsAttachmentsOpen(true);
    };

    const openDeleteConfirm = (posting: Posting) => {
        setDeleteTarget(posting);
        setDeleteConfirmText('');
    };

    const closeDeleteConfirm = () => {
        setDeleteTarget(null);
        setDeleteConfirmText('');
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold">Projeler</h1>
                    <p className="mt-1 text-sm text-muted-foreground">Tum projeleri filtreleyebilir ve gorevleri ekrandan aninda yonetebilirsiniz.</p>
                </div>
                <Button onClick={openCreate}><Plus className="w-4 h-4 mr-2" /> Yeni Proje</Button>
            </div>

            <div className="rounded-2xl border border-border/70 bg-card/92 shadow-md shadow-black/10">
                <div className="grid gap-3 border-b border-border/70 p-4 md:grid-cols-[1fr_220px_220px_220px]">
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                        <Input
                            className="pl-9"
                            placeholder="Proje ara..."
                            value={searchTerm}
                            onChange={(event) => setSearchTerm(event.target.value)}
                        />
                    </div>
                    <Select value={filterCategory} onValueChange={setFilterCategory}>
                        <SelectTrigger>
                            <SelectValue placeholder="Kategori filtrele" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="ALL">Tum Kategoriler</SelectItem>
                            {categoryOptions.map((category) => (
                                <SelectItem key={category} value={category}>{category}</SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                    <Select value={sortBy} onValueChange={setSortBy}>
                        <SelectTrigger>
                            <SelectValue placeholder="Siralama" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="CREATED_DESC">En Yeni</SelectItem>
                            <SelectItem value="CREATED_ASC">En Eski</SelectItem>
                            <SelectItem value="CATEGORY_ASC">Kategori (A-Z)</SelectItem>
                            <SelectItem value="CATEGORY_DESC">Kategori (Z-A)</SelectItem>
                            <SelectItem value="TITLE_ASC">Proje Adi (A-Z)</SelectItem>
                            <SelectItem value="TITLE_DESC">Proje Adi (Z-A)</SelectItem>
                        </SelectContent>
                    </Select>
                    <Input
                        type="date"
                        value={specificDate}
                        onChange={(event) => setSpecificDate(event.target.value)}
                        aria-label="Belirli tarih filtrele"
                    />
                </div>

                <Table className="[&_thead]:bg-muted/45">
                    <TableHeader>
                        <TableRow>
                            <TableHead>Proje</TableHead>
                            <TableHead>Kategori</TableHead>
                            <TableHead>Durum</TableHead>
                            <TableHead className="text-right">Basvurular</TableHead>
                            <TableHead className="text-center">Ekler</TableHead>
                            <TableHead className="text-center">Gorevler</TableHead>
                            <TableHead className="text-center">Sorular</TableHead>
                            <TableHead className="text-right">Islemler</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow><TableCell colSpan={8} className="text-center p-4">Yukleniyor...</TableCell></TableRow>
                        ) : visiblePostings.length === 0 ? (
                            <TableRow><TableCell colSpan={8} className="text-center p-8 text-muted-foreground">Proje bulunamadi.</TableCell></TableRow>
                        ) : (
                            visiblePostings.map((posting) => (
                                <TableRow key={posting.id}>
                                    <TableCell className="font-medium">
                                        <div>
                                            <p>{posting.title}</p>
                                            <p className="text-xs text-muted-foreground">{posting.projectName}</p>
                                        </div>
                                    </TableCell>
                                    <TableCell><Badge variant="outline">{posting.category}</Badge></TableCell>
                                    <TableCell>
                                        <Badge variant="outline" className={getPostingStatusBadgeClass(posting.status)}>
                                            {posting.status}
                                        </Badge>
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button variant="link" asChild>
                                            <Link to={`/admin/submissions?postingId=${posting.id}`}>Basvuranlar</Link>
                                        </Button>
                                    </TableCell>
                                    <TableCell className="text-center">
                                        <Button variant="ghost" size="sm" onClick={() => openAttachments(posting)} className="relative">
                                            <div className="flex items-center">
                                                <Paperclip className="w-4 h-4 mr-1" />
                                                {posting.attachments?.length || 0}
                                            </div>
                                        </Button>
                                    </TableCell>
                                    <TableCell className="text-center">
                                        <Button size="sm" variant="outline" title="Gorev Islemleri" onClick={() => setTaskManagerPosting(posting)}>
                                            Gorev Islemleri
                                        </Button>
                                    </TableCell>
                                    <TableCell className="text-center">
                                        <Button size="sm" variant="outline" title="Proje Sorulari" onClick={() => setQuestionsPosting(posting)}>
                                                <MessageSquare className="w-4 h-4 mr-1" />
                                                Sorular
                                        </Button>
                                    </TableCell>
                                    <TableCell className="text-right space-x-2">
                                        {posting.status === 'DRAFT' && (
                                            <Button size="sm" variant="ghost" className="text-green-600" onClick={() => publishMutation.mutate(posting.id)} title="Yayinla">
                                                <Eye className="w-4 h-4" />
                                            </Button>
                                        )}
                                        {posting.status === 'PUBLISHED' && (
                                            <Button size="sm" variant="ghost" className="text-red-500" onClick={() => closeMutation.mutate(posting.id)} title="Gizle">
                                                <EyeOff className="w-4 h-4" />
                                            </Button>
                                        )}
                                        {posting.status === 'CLOSED' && (
                                            <Button size="sm" variant="ghost" className="text-blue-600" onClick={() => reopenMutation.mutate(posting.id)} title="Tekrar Yayinla">
                                                <RotateCcw className="w-4 h-4" />
                                            </Button>
                                        )}
                                        <Button size="sm" variant="ghost" onClick={() => openEdit(posting)} title="Projeyi Duzenle">
                                            <Settings className="w-4 h-4" />
                                        </Button>
                                        <Button
                                            size="sm"
                                            variant="ghost"
                                            className="text-red-600"
                                            onClick={() => openDeleteConfirm(posting)}
                                            title="Projeyi Sil"
                                        >
                                            <Trash className="w-4 h-4" />
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>

            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto w-full">
                    <DialogHeader>
                        <DialogTitle>{selectedPosting ? 'Proje Duzenle' : 'Proje Olustur'}</DialogTitle>
                    </DialogHeader>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="space-y-2">
                            <Label>Baslik</Label>
                            <Input name="title" defaultValue={selectedPosting?.title} required />
                        </div>
                        <div className="space-y-2">
                            <Label>Kategori</Label>
                            <Select value={formCategory} onValueChange={setFormCategory}>
                                <SelectTrigger><SelectValue /></SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="BACKEND">Backend</SelectItem>
                                    <SelectItem value="FRONTEND">Frontend</SelectItem>
                                    <SelectItem value="MOBILE">Mobile</SelectItem>
                                    <SelectItem value="FULLSTACK">Fullstack</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                        <div className="space-y-2">
                            <Label>Aciklama</Label>
                            <textarea
                                className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                                value={formDescription}
                                onChange={(event) => setFormDescription(event.target.value)}
                                required
                                rows={5}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label>Proje Adi</Label>
                            <Input name="projectName" defaultValue={selectedPosting?.projectName} required />
                        </div>
                        <div className="space-y-2">
                            <Label>Proje Detayi</Label>
                            <textarea
                                className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                                value={formProjectDetails}
                                onChange={(event) => setFormProjectDetails(event.target.value)}
                                required
                                rows={5}
                            />
                        </div>

                        <Button type="submit" className="w-full" disabled={createMutation.isPending || updateMutation.isPending}>
                            {selectedPosting ? 'Guncelle' : 'Olustur'}
                        </Button>
                    </form>
                </DialogContent>
            </Dialog>

            <AttachmentsDialog
                posting={selectedPosting}
                open={isAttachmentsOpen}
                onOpenChange={setIsAttachmentsOpen}
                queryClient={queryClient}
            />

            <Dialog open={Boolean(taskManagerPosting)} onOpenChange={(open) => { if (!open) setTaskManagerPosting(null); }}>
                <DialogContent className="max-w-[96vw] w-[1400px] max-h-[95vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle>Gorev Yonetimi</DialogTitle>
                        <DialogDescription>{taskManagerPosting ? `${taskManagerPosting.title} icin gorev islemleri` : ''}</DialogDescription>
                    </DialogHeader>
                    {taskManagerPosting && (
                        <ProjectTaskGraphManager
                            projectId={String(taskManagerPosting.id)}
                            title={`${taskManagerPosting.title} - Gorev Gecmisi Yonetimi`}
                        />
                    )}
                </DialogContent>
            </Dialog>

            <Dialog open={Boolean(questionsPosting)} onOpenChange={(open) => { if (!open) setQuestionsPosting(null); }}>
                <DialogContent className="max-w-[96vw] w-[1300px] max-h-[95vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle>Proje Sorulari</DialogTitle>
                        <DialogDescription>{questionsPosting ? `${questionsPosting.title} icin sorulari yonetin` : ''}</DialogDescription>
                    </DialogHeader>
                    {questionsPosting && <AdminQuestionsPage postingId={String(questionsPosting.id)} embedded />}
                </DialogContent>
            </Dialog>

            <Dialog open={Boolean(deleteTarget)} onOpenChange={(open) => { if (!open) closeDeleteConfirm(); }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Projeyi silmek istediginize emin misiniz?</DialogTitle>
                        <DialogDescription>
                            Bu islem geri alinamaz. Silmek icin proje adini birebir yazin:
                            {' '}
                            <span className="font-semibold text-foreground">{deleteTarget?.title}</span>
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-2">
                        <Label>Proje adini yazin (buyuk/kucuk harf duyarlidir)</Label>
                        <Input
                            value={deleteConfirmText}
                            onChange={(event) => setDeleteConfirmText(event.target.value)}
                            placeholder={deleteTarget?.title || ''}
                        />
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={closeDeleteConfirm}>Iptal</Button>
                        <Button
                            variant="destructive"
                            onClick={() => {
                                if (!deleteTarget) return;
                                deleteProjectMutation.mutate(deleteTarget.id, {
                                    onSuccess: () => {
                                        closeDeleteConfirm();
                                    },
                                });
                            }}
                            disabled={!deleteTarget || deleteConfirmText !== deleteTarget.title || deleteProjectMutation.isPending}
                        >
                            Projeyi Sil
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
};

const AttachmentsDialog = ({
    posting,
    open,
    onOpenChange,
    queryClient,
}: {
    posting: Posting | null;
    open: boolean;
    onOpenChange: (open: boolean) => void;
    queryClient: any;
}) => {
    const { toast } = useToast();
    const [selectedFiles, setSelectedFiles] = useState<File[]>([]);

    const handleAttachmentSelection = (files: FileList | null) => {
        if (!files) {
            setSelectedFiles([]);
            return;
        }

        const list = Array.from(files);
        const invalidFile = list.find((file) => !isPdfFile(file));
        if (invalidFile) {
            setSelectedFiles([]);
            toast({ variant: 'destructive', title: 'Gecersiz format', description: 'Sadece PDF yukleyebilirsiniz.' });
            return;
        }

        setSelectedFiles(list);
    };

    const uploadMutation = useMutation({
        mutationFn: async (filesToUpload: File[]) => {
            if (!posting) {
                throw new Error('POSTING_NOT_SELECTED');
            }
            if (!filesToUpload.length) {
                throw new Error('FILES_REQUIRED');
            }
            await uploadPostingAttachments(posting.id, filesToUpload);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-postings'] });
            setSelectedFiles([]);
            toast({ title: 'Ekler yuklendi' });
        },
        onError: (err: unknown) => {
            toast({ variant: 'destructive', title: 'Yukleme basarisiz', description: mapBackendErrorToMessage(err, 'Yukleme basarisiz.') });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: async (attachmentId: string) => {
            if (!posting) return;
            await deletePostingAttachment(posting.id, attachmentId);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-postings'] });
            toast({ title: 'Ek silindi' });
        },
    });

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>{posting?.title} - Ek Yonetimi</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <div className="space-y-2">
                        <Label>Yeni Dosya Yukle</Label>
                        {posting?.status === 'CLOSED' ? (
                            <div className="rounded border border-red-200 bg-red-50 p-2 text-sm font-medium text-red-600 dark:border-red-500/30 dark:bg-red-500/10 dark:text-red-300">
                                Kapali projelerde yukleme devre disidir.
                            </div>
                        ) : (
                            <div className="flex gap-2">
                                <Input type="file" multiple accept="application/pdf,.pdf" onChange={(event) => handleAttachmentSelection(event.target.files)} />
                                <Button type="button" onClick={() => uploadMutation.mutate(selectedFiles)} disabled={!selectedFiles.length || uploadMutation.isPending}>
                                    <Plus className="w-4 h-4" />
                                </Button>
                            </div>
                        )}
                    </div>
                    <div className="space-y-2">
                        <Label>Mevcut Ekler</Label>
                        {posting?.attachments && posting.attachments.length > 0 ? (
                            <div className="space-y-2 max-h-[200px] overflow-y-auto">
                                {posting.attachments.map((attachment) => (
                                    <div key={attachment.id} className="flex items-center justify-between rounded border border-border/70 bg-muted/40 p-2 text-sm">
                                        <span className="truncate max-w-[300px]">{attachment.originalFileName}</span>
                                        <Button size="icon" variant="ghost" className="h-8 w-8 text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10" onClick={() => deleteMutation.mutate(attachment.id)}>
                                            <Trash className="w-4 h-4" />
                                        </Button>
                                    </div>
                                ))}
                            </div>
                        ) : <p className="text-sm text-muted-foreground italic">Ek bulunamadi.</p>}
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default AdminPostingsPage;
