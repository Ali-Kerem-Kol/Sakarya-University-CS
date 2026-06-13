import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { acceptSubmission, fetchAdminPostings, fetchAdminSubmissions, fetchUserDocuments, rejectSubmission } from '@/api/admin';
import { AdminUserSummary, listAdminUsers, setAdminUserColor } from '@/api/adminTasks';
import { downloadFile, mapBackendErrorToMessage } from '@/api/client';
import ProjectTaskGraphManager from '@/components/ProjectTaskGraphManager';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { useToast } from '@/hooks/use-toast';
import { getSubmissionStatusLabel, normalizeEntityId, normalizeSubmissionStatus } from '@/lib/submissionStatus';
import { resolveUserColor } from '@/lib/userColor';

type SubmissionItem = {
    id?: string | number | null;
    postingId?: string | number | null;
    postingTitle?: string | null;
    status?: string | null;
    createdAt?: string | null;
    submittedAt?: string | null;
    userId?: string | number | null;
    user?: { id?: string | number | null } | null;
};

type UserProject = {
    projectId: string;
    projectTitle: string;
};

type GraphModalState = {
    user: AdminUserSummary;
    projectId: string;
    projectTitle: string;
} | null;

const getFullName = (user: { firstName?: string | null; lastName?: string | null; email: string }) => {
    const fullName = `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim();
    return fullName || user.email;
};

const formatDateTime = (value?: string | null) => {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '-';
    return date.toLocaleString('tr-TR');
};

const sortByLatest = (items: SubmissionItem[]) =>
    [...items].sort((a, b) => {
        const aTime = new Date((a.createdAt ?? a.submittedAt) || 0).getTime();
        const bTime = new Date((b.createdAt ?? b.submittedAt) || 0).getTime();
        return bTime - aTime;
    });

const getSubmissionBadgeClass = (statusKey: ReturnType<typeof normalizeSubmissionStatus>) => {
    if (statusKey === 'APPROVED') {
        return 'border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-500/40 dark:bg-emerald-500/10 dark:text-emerald-300';
    }
    if (statusKey === 'REJECTED') {
        return 'border-red-200 bg-red-50 text-red-700 dark:border-red-500/40 dark:bg-red-500/10 dark:text-red-300';
    }
    if (statusKey === 'PENDING') {
        return 'border-yellow-200 bg-yellow-50 text-yellow-800 dark:border-yellow-500/40 dark:bg-yellow-500/10 dark:text-yellow-300';
    }
    return 'border-border bg-muted/50 text-muted-foreground';
};

const AdminUsersPage: React.FC = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();

    const [search, setSearch] = useState('');
    const [selectedUserIdForDocs, setSelectedUserIdForDocs] = useState<string>('');
    const [selectedUserIdForSubmissions, setSelectedUserIdForSubmissions] = useState<string>('');
    const [graphModal, setGraphModal] = useState<GraphModalState>(null);
    const [colorDraftByUserId, setColorDraftByUserId] = useState<Record<string, string>>({});
    const [selectedProjectByUserId, setSelectedProjectByUserId] = useState<Record<string, string>>({});
    const [showArchivedSubmissions, setShowArchivedSubmissions] = useState(false);

    const { data: users, isLoading, isError, error } = useQuery({
        queryKey: ['admin-users-list'],
        queryFn: () => listAdminUsers(),
    });

    const { data: submissions } = useQuery({
        queryKey: ['admin-submissions-for-users-panel'],
        queryFn: () => fetchAdminSubmissions({ page: 0, size: 1000 }),
    });

    const { data: postings } = useQuery({
        queryKey: ['admin-postings-for-users-panel'],
        queryFn: () => fetchAdminPostings(),
    });

    const postingNameById = useMemo(() => {
        const map = new Map<string, string>();
        (postings ?? []).forEach((posting) => {
            map.set(String(posting.id), posting.title ?? `Proje #${String(posting.id)}`);
        });
        return map;
    }, [postings]);

    const usersById = useMemo(() => {
        const map = new Map<string, AdminUserSummary>();
        (users ?? []).forEach((user) => map.set(String(user.id), user));
        return map;
    }, [users]);

    useEffect(() => {
        if (!users?.length) return;
        const next: Record<string, string> = {};
        users.forEach((user) => {
            const id = String(user.id);
            next[id] = resolveUserColor(user.id, user.preferredColor);
        });
        setColorDraftByUserId(next);
    }, [users]);

    const selectedUserForDocs = useMemo(
        () => usersById.get(selectedUserIdForDocs) ?? null,
        [selectedUserIdForDocs, usersById],
    );

    const selectedUserForSubmissions = useMemo(
        () => usersById.get(selectedUserIdForSubmissions) ?? null,
        [selectedUserIdForSubmissions, usersById],
    );

    useEffect(() => {
        if (!selectedUserIdForSubmissions) {
            setShowArchivedSubmissions(false);
        }
    }, [selectedUserIdForSubmissions]);

    const { data: userDocs, isLoading: isDocsLoading } = useQuery({
        queryKey: ['admin-user-docs', selectedUserIdForDocs],
        queryFn: () => fetchUserDocuments(selectedUserIdForDocs),
        enabled: Boolean(selectedUserIdForDocs),
    });

    const submissionsByUserId = useMemo(() => {
        const map = new Map<string, SubmissionItem[]>();
        (submissions ?? []).forEach((submission) => {
            const userId = normalizeEntityId(submission.user?.id ?? submission.userId);
            if (!userId) return;
            const list = map.get(userId) ?? [];
            list.push(submission as SubmissionItem);
            map.set(userId, list);
        });
        map.forEach((items, key) => map.set(key, sortByLatest(items)));
        return map;
    }, [submissions]);

    const activeProjectsByUser = useMemo(() => {
        const map = new Map<string, UserProject[]>();
        submissionsByUserId.forEach((items, userId) => {
            const user = usersById.get(userId);
            if (!user || !user.enabled) return;
            const seen = new Set<string>();
            const projects: UserProject[] = [];
            items.forEach((submission) => {
                if (normalizeSubmissionStatus(submission.status) !== 'APPROVED') return;
                const projectId = normalizeEntityId(submission.postingId);
                if (!projectId || seen.has(projectId)) return;
                seen.add(projectId);
                projects.push({
                    projectId,
                    projectTitle: postingNameById.get(projectId) ?? submission.postingTitle ?? `Proje #${projectId}`,
                });
            });
            if (projects.length > 0) map.set(userId, projects);
        });
        return map;
    }, [postingNameById, submissionsByUserId, usersById]);

    const visibleUsers = useMemo(() => {
        const q = search.trim().toLowerCase();
        return (users ?? [])
            .filter((user) => activeProjectsByUser.has(String(user.id)))
            .filter((user) => {
                if (!q) return true;
                const haystack = `${user.id} ${user.email} ${getFullName(user)}`.toLowerCase();
                return haystack.includes(q);
            });
    }, [activeProjectsByUser, search, users]);

    const approveMutation = useMutation({
        mutationFn: async (submissionId: string) => acceptSubmission(submissionId),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-submissions-for-users-panel'] });
            toast({ title: 'Basvuru onaylandi' });
        },
        onError: (err: unknown) => {
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: mapBackendErrorToMessage(err, 'Basvuru onaylanamadi.') });
        },
    });

    const rejectMutation = useMutation({
        mutationFn: async (submissionId: string) => rejectSubmission(submissionId),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-submissions-for-users-panel'] });
            toast({ title: 'Basvuru reddedildi' });
        },
        onError: (err: unknown) => {
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: mapBackendErrorToMessage(err, 'Basvuru reddedilemedi.') });
        },
    });

    const colorMutation = useMutation({
        mutationFn: async ({ userId, color }: { userId: string; color: string }) => setAdminUserColor(userId, color),
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ['admin-users-list'] }),
                queryClient.invalidateQueries({ queryKey: ['admin-users-list-short'] }),
            ]);
            toast({ title: 'Kullanici rengi guncellendi' });
        },
        onError: (err: unknown) => {
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: mapBackendErrorToMessage(err, 'Kullanici rengi guncellenemedi.') });
        },
    });

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold">Kullanicilar</h1>
                <p className="text-sm text-slate-500">Kullanicilarin aktif oldugu projeleri gor ve proje bazli tum gorev agacini ac.</p>
            </div>

            <Card>
                <CardContent className="pt-6">
                    <Input placeholder="Kullanici ara..." value={search} onChange={(event) => setSearch(event.target.value)} />
                </CardContent>
            </Card>

            <Card>
                <CardContent className="p-0">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Kullanici</TableHead>
                                <TableHead>E-posta</TableHead>
                                <TableHead>Renk</TableHead>
                                <TableHead>Aktif Projeler</TableHead>
                                <TableHead>Dokuman</TableHead>
                                <TableHead>Basvuru</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {isLoading ? (
                                <TableRow><TableCell colSpan={6} className="text-center p-6">Yukleniyor...</TableCell></TableRow>
                            ) : isError ? (
                                <TableRow><TableCell colSpan={6} className="text-center p-6 text-red-600">Veriler yuklenemedi: {error instanceof Error ? error.message : 'Bilinmeyen hata'}</TableCell></TableRow>
                            ) : visibleUsers.length === 0 ? (
                                <TableRow><TableCell colSpan={6} className="text-center p-6 text-slate-500">Aktif proje kaydi olan kullanici bulunamadi.</TableCell></TableRow>
                            ) : visibleUsers.map((user) => {
                                const userId = String(user.id);
                                const projects = activeProjectsByUser.get(userId) ?? [];
                                const userSubmissions = submissionsByUserId.get(userId) ?? [];
                                const activeSubmissionCount = userSubmissions.filter((submission) => {
                                    const status = normalizeSubmissionStatus(submission.status);
                                    return status !== 'CANCELED' && status !== 'REMOVED';
                                }).length;
                                const draftColor = colorDraftByUserId[userId] ?? resolveUserColor(user.id, user.preferredColor);
                                return (
                                    <TableRow key={userId}>
                                        <TableCell><div className="font-medium">{getFullName(user)}</div><div className="text-xs text-slate-500">ID: {user.id}</div></TableCell>
                                        <TableCell>{user.email}</TableCell>
                                        <TableCell>
                                            <div className="flex items-center gap-2">
                                                <input
                                                    type="color"
                                                    value={draftColor}
                                                    className="h-8 w-10 cursor-pointer rounded border border-slate-300 bg-white p-1"
                                                    onChange={(event) => {
                                                        const value = event.target.value;
                                                        setColorDraftByUserId((current) => ({ ...current, [userId]: value }));
                                                    }}
                                                />
                                                <Button
                                                    size="sm"
                                                    variant="outline"
                                                    onClick={() => colorMutation.mutate({ userId, color: draftColor })}
                                                    disabled={colorMutation.isPending}
                                                >
                                                    Kaydet
                                                </Button>
                                            </div>
                                        </TableCell>
                                        <TableCell>
                                            {projects.length === 0 ? (
                                                <span className="text-xs text-muted-foreground">Aktif proje yok</span>
                                            ) : (
                                                <Select
                                                    value={selectedProjectByUserId[userId] ?? ''}
                                                    onValueChange={(projectId) => {
                                                        setSelectedProjectByUserId((current) => ({ ...current, [userId]: projectId }));
                                                        const selectedProject = projects.find((project) => project.projectId === projectId);
                                                        if (selectedProject) {
                                                            setGraphModal({
                                                                user,
                                                                projectId: selectedProject.projectId,
                                                                projectTitle: selectedProject.projectTitle,
                                                            });
                                                        }
                                                    }}
                                                >
                                                    <SelectTrigger className="h-9 w-[240px]">
                                                        <SelectValue placeholder="Proje secin" />
                                                    </SelectTrigger>
                                                    <SelectContent>
                                                        {projects.map((project) => (
                                                            <SelectItem key={`${userId}-${project.projectId}`} value={project.projectId}>
                                                                {project.projectTitle}
                                                            </SelectItem>
                                                        ))}
                                                    </SelectContent>
                                                </Select>
                                            )}
                                        </TableCell>
                                        <TableCell><Button size="sm" variant="outline" onClick={() => setSelectedUserIdForDocs(userId)}>CV</Button></TableCell>
                                        <TableCell><Button size="sm" variant="outline" onClick={() => setSelectedUserIdForSubmissions(userId)}>Basvurular ({activeSubmissionCount})</Button></TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>

            <Dialog open={Boolean(graphModal)} onOpenChange={(open) => { if (!open) setGraphModal(null); }}>
                <DialogContent className="max-w-[96vw] w-[1400px] max-h-[95vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle>Proje Gorev Yonetimi</DialogTitle>
                        <DialogDescription>
                            {graphModal ? `${graphModal.projectTitle} - Kullanici odagi: ${getFullName(graphModal.user)}` : ''}
                        </DialogDescription>
                    </DialogHeader>
                    {graphModal && (
                        <ProjectTaskGraphManager
                            projectId={graphModal.projectId}
                            title={`${graphModal.projectTitle} - Gorev Gecmisi Yonetimi`}
                            initialFocusedUserId={String(graphModal.user.id)}
                        />
                    )}
                </DialogContent>
            </Dialog>

            <Dialog open={Boolean(selectedUserIdForDocs)} onOpenChange={(open) => { if (!open) setSelectedUserIdForDocs(''); }}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Kullanici Belgeleri</DialogTitle>
                        <DialogDescription>{selectedUserForDocs ? `${getFullName(selectedUserForDocs)} - ${selectedUserForDocs.email}` : 'Kullanici secilmedi'}</DialogDescription>
                    </DialogHeader>
                    <div className="space-y-2 max-h-[320px] overflow-auto">
                        {isDocsLoading ? <p className="text-sm text-slate-500">Belgeler yukleniyor...</p> : (userDocs?.length ?? 0) === 0 ? <p className="text-sm text-slate-500">Belge bulunamadi.</p> : userDocs?.map((doc) => (
                            <div key={doc.id} className="flex items-center justify-between rounded border p-2 text-sm">
                                <div><p className="font-medium">{doc.fileName}</p><p className="text-xs text-slate-500">{doc.documentType}</p></div>
                                <Button size="sm" variant="outline" onClick={() => downloadFile(`/admin/documents/${doc.id}/download`, doc.fileName)}>Indir</Button>
                            </div>
                        ))}
                    </div>
                </DialogContent>
            </Dialog>

            <Dialog open={Boolean(selectedUserIdForSubmissions)} onOpenChange={(open) => { if (!open) setSelectedUserIdForSubmissions(''); }}>
                <DialogContent className="max-w-3xl">
                    <DialogHeader>
                        <DialogTitle>Kullanici Basvurulari</DialogTitle>
                        <DialogDescription>{selectedUserForSubmissions ? `${getFullName(selectedUserForSubmissions)} - ${selectedUserForSubmissions.email}` : 'Kullanici secilmedi'}</DialogDescription>
                    </DialogHeader>
                    <div className="flex justify-end">
                        <Button
                            size="sm"
                            variant="outline"
                            onClick={() => setShowArchivedSubmissions((current) => !current)}
                        >
                            {showArchivedSubmissions ? 'Arsivi Gizle' : 'Arsivi Goster'}
                        </Button>
                    </div>
                    <div className="max-h-[420px] overflow-auto rounded border">
                        <Table>
                            <TableHeader>
                                <TableRow><TableHead>Basvuru ID</TableHead><TableHead>Ilan</TableHead><TableHead>Ilan ID</TableHead><TableHead>Tarih</TableHead><TableHead>Durum</TableHead><TableHead>Aksiyon</TableHead></TableRow>
                            </TableHeader>
                            <TableBody>
                                {(submissionsByUserId.get(selectedUserIdForSubmissions) ?? [])
                                    .filter((submission) => {
                                        if (showArchivedSubmissions) return true;
                                        const status = normalizeSubmissionStatus(submission.status);
                                        return status !== 'CANCELED' && status !== 'REMOVED';
                                    })
                                    .map((submission) => {
                                    const submissionId = normalizeEntityId(submission.id);
                                    const submissionStatus = normalizeSubmissionStatus(submission.status);
                                    const canApprove = submissionStatus !== 'APPROVED';
                                    const canReject = submissionStatus === 'PENDING' || submissionStatus === 'APPROVED';
                                    return (
                                        <TableRow key={`${submission.id}-${submission.createdAt ?? submission.submittedAt ?? ''}`}>
                                            <TableCell>{submissionId ?? '-'}</TableCell>
                                            <TableCell>{submission.postingTitle ?? '-'}</TableCell>
                                            <TableCell>{normalizeEntityId(submission.postingId) ?? '-'}</TableCell>
                                            <TableCell>{formatDateTime(submission.createdAt ?? submission.submittedAt)}</TableCell>
                                            <TableCell>
                                                <Badge variant="outline" className={getSubmissionBadgeClass(submissionStatus)}>
                                                    {getSubmissionStatusLabel(submission.status)}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>
                                                {submissionId && (canApprove || canReject) ? (
                                                    <div className="flex gap-1">
                                                        {canApprove && (
                                                            <Button size="sm" variant="outline" onClick={() => approveMutation.mutate(submissionId)} disabled={approveMutation.isPending}>
                                                                {submissionStatus === 'PENDING' ? 'Onayla' : 'Tekrar Onayla'}
                                                            </Button>
                                                        )}
                                                        {canReject && (
                                                            <Button size="sm" variant="outline" onClick={() => rejectMutation.mutate(submissionId)} disabled={rejectMutation.isPending}>
                                                                Reddet
                                                            </Button>
                                                        )}
                                                    </div>
                                                ) : (
                                                    <span className="text-xs text-slate-500">-</span>
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    </div>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default AdminUsersPage;
