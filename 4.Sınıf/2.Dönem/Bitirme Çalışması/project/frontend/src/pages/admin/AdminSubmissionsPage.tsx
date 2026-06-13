import React, { useEffect, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import { addSubmissionByAdmin, fetchAdminSubmissions, acceptSubmission, rejectSubmission, removeSubmission } from '@/api/admin';
import { listAdminUsers } from '@/api/adminTasks';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Eye, Filter, Download } from 'lucide-react';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { cn } from '@/lib/utils';
import { useToast } from '@/hooks/use-toast';
import { openDocument } from '@/api/client';
import { getSubmissionStatusLabel, normalizeEntityId, normalizeSubmissionStatus } from '@/lib/submissionStatus';

const AdminSubmissionsPage: React.FC = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();
    const [searchParams] = useSearchParams();
    const postingId = searchParams.get('postingId') || undefined;
    const [statusFilter, setStatusFilter] = useState<string>('ALL');
    const [searchQuery, setSearchQuery] = useState('');
    const [showArchivedSubmissions, setShowArchivedSubmissions] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState<{ id: string; applicantName: string; statusLabel: string } | null>(null);
    const [deleteConfirmText, setDeleteConfirmText] = useState('');
    const [manualAddOpen, setManualAddOpen] = useState(false);
    const [manualAddUserId, setManualAddUserId] = useState('');

    useEffect(() => {
        if (statusFilter === 'CANCELED' || statusFilter === 'REMOVED') {
            setShowArchivedSubmissions(true);
        }
    }, [statusFilter]);

    useEffect(() => {
        if (showArchivedSubmissions) return;
        if (statusFilter === 'CANCELED' || statusFilter === 'REMOVED') {
            setStatusFilter('ALL');
        }
    }, [showArchivedSubmissions, statusFilter]);

    const { data: submissions, isLoading, refetch } = useQuery({
        queryKey: ['admin-submissions', postingId],
        queryFn: () => fetchAdminSubmissions(postingId ? { postingId } : {}),
    });

    const { data: users } = useQuery({
        queryKey: ['admin-users-list-short-for-posting-add'],
        queryFn: () => listAdminUsers(300),
        enabled: Boolean(postingId),
    });

    const approvedMemberUserIdSet = new Set(
        (submissions ?? [])
            .filter((submission) => normalizeSubmissionStatus(submission.status) === 'APPROVED')
            .map((submission) => normalizeEntityId(submission.user?.id ?? submission.userId))
            .filter(Boolean),
    );

    const addableUsers = (users ?? []).filter((user) => !approvedMemberUserIdSet.has(String(user.id)));

    const filteredSubmissions = submissions?.filter((sub) => {
        const userId = normalizeEntityId(sub.user?.id ?? sub.userId);
        const firstName = (sub.user?.firstName ?? sub.userFirstName ?? '').trim();
        const lastName = (sub.user?.lastName ?? sub.userLastName ?? '').trim();
        const email = (sub.user?.email ?? sub.userEmail ?? '').trim();
        const fullName = [firstName, lastName].filter(Boolean).join(' ').trim();
        const statusKey = normalizeSubmissionStatus(sub.status);
        const isArchived = statusKey === 'CANCELED' || statusKey === 'REMOVED';

        if (showArchivedSubmissions) {
            if (!isArchived) return false;
            if (statusFilter === 'ALL') return true;
            return statusKey === statusFilter;
        }
        if (isArchived) return false;
        if (statusFilter !== 'ALL' && statusKey !== statusFilter) return false;

        if (!searchQuery) return true;
        const q = searchQuery.toLowerCase();

        return (
            email.toLowerCase().includes(q) ||
            fullName.toLowerCase().includes(q) ||
            (sub.postingTitle ?? '').toLowerCase().includes(q) ||
            userId.toLowerCase().includes(q)
        );
    });

    const acceptMutation = useMutation({
        mutationFn: acceptSubmission,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-submissions'] });
            await refetch();
            toast({ title: 'Accepted', description: 'Candidate accepted into the project.' });
        },
        onError: (err: unknown) => {
            const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
            toast({ variant: 'destructive', title: 'Action Failed', description: message || 'Could not accept submission.' });
        },
    });

    const rejectMutation = useMutation({
        mutationFn: rejectSubmission,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-submissions'] });
            await refetch();
            toast({ title: 'Rejected', description: 'Candidate application rejected.' });
        },
        onError: (err: unknown) => {
            const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
            toast({ variant: 'destructive', title: 'Action Failed', description: message || 'Could not reject submission.' });
        },
    });

    const removeMutation = useMutation({
        mutationFn: removeSubmission,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-submissions'] });
            await refetch();
            toast({ title: 'Removed', description: 'Submission removed.' });
        },
        onError: (err: unknown) => {
            const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
            toast({ variant: 'destructive', title: 'Action Failed', description: message || 'Could not remove submission.' });
        },
    });

    const manualAddMutation = useMutation({
        mutationFn: async () => {
            if (!postingId || !manualAddUserId) return;
            await addSubmissionByAdmin(postingId, { userId: manualAddUserId, status: 'APPROVED' });
        },
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['admin-submissions'] });
            await queryClient.invalidateQueries({ queryKey: ['admin-submissions-for-users-panel'] });
            await refetch();
            setManualAddOpen(false);
            setManualAddUserId('');
            toast({ title: 'Kullanici projeye dahil edildi', description: 'Kullanici onayli olarak projeye eklendi.' });
        },
        onError: (err: unknown) => {
            const message = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: message || 'Kullanici projeye eklenemedi.' });
        },
    });

    const handleViewCV = async (url: string) => {
        if (!url) return;
        try {
            await openDocument(url, { mode: 'view', contentType: 'application/pdf' });
        } catch {
            toast({ variant: 'destructive', title: 'Error', description: 'Could not open CV.' });
        }
    };

    const handleDownloadCV = async (url: string, fileName: string) => {
        if (!url) return;
        try {
            await openDocument(url, { mode: 'download', defaultFileName: fileName, contentType: 'application/pdf' });
        } catch {
            toast({ variant: 'destructive', title: 'Error', description: 'Could not download CV.' });
        }
    };

    return (
        <div className="space-y-6 animate-in fade-in duration-500">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold">{postingId ? 'Proje Basvuranlari' : 'Basvurular'}</h1>
                    <p className="text-sm text-muted-foreground">
                        {postingId
                            ? `Sadece secili projeye ait basvurular listeleniyor (Proje ID: ${postingId}).`
                            : 'Tum proje basvurularini yonetin.'}
                    </p>
                </div>
                <div className="flex items-center gap-2 w-full md:w-auto">
                    {postingId && (
                        <Button variant="outline" onClick={() => setManualAddOpen(true)}>
                            Kullanici Ekle
                        </Button>
                    )}
                    <Input
                        placeholder="Search applicant or position..."
                        className="w-full md:w-[250px]"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                    <Select value={statusFilter} onValueChange={setStatusFilter}>
                        <SelectTrigger className="w-[140px]">
                            <Filter className="w-4 h-4 mr-2 text-muted-foreground" />
                            <SelectValue placeholder="Status" />
                        </SelectTrigger>
                        <SelectContent>
                            {showArchivedSubmissions ? (
                                <>
                                    <SelectItem value="ALL">All Status</SelectItem>
                                    <SelectItem value="CANCELED">Canceled</SelectItem>
                                    <SelectItem value="REMOVED">Removed</SelectItem>
                                </>
                            ) : (
                                <>
                                    <SelectItem value="ALL">All Status</SelectItem>
                                    <SelectItem value="PENDING">Pending</SelectItem>
                                    <SelectItem value="APPROVED">Approved</SelectItem>
                                    <SelectItem value="REJECTED">Rejected</SelectItem>
                                </>
                            )}
                        </SelectContent>
                    </Select>
                    <Button
                        variant="outline"
                        onClick={() => {
                            setShowArchivedSubmissions((current) => {
                                const next = !current;
                                setStatusFilter('ALL');
                                return next;
                            });
                        }}
                    >
                        {showArchivedSubmissions ? 'Arsivi Gizle' : 'Arsivi Goster'}
                    </Button>
                </div>
            </div>

            <div className="overflow-hidden rounded-2xl border border-border/70 bg-card/92 shadow-md shadow-black/10">
                <Table className="[&_thead]:bg-muted/45">
                    <TableHeader>
                        <TableRow>
                            <TableHead>Applicant</TableHead>
                            <TableHead>Position & Project</TableHead>
                            <TableHead>CV</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Applied At</TableHead>
                            <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow><TableCell colSpan={6} className="text-center p-8">Loading...</TableCell></TableRow>
                        ) : filteredSubmissions?.length === 0 ? (
                            <TableRow><TableCell colSpan={6} className="text-center p-8 text-muted-foreground">No applications found.</TableCell></TableRow>
                        ) : (
                            filteredSubmissions?.map((sub) => {
                                const userId = normalizeEntityId(sub.user?.id ?? sub.userId);
                                const shortUserId = userId.length > 0 ? userId.substring(0, 8) : '-';
                                const firstName = (sub.user?.firstName ?? sub.userFirstName ?? '').trim();
                                const lastName = (sub.user?.lastName ?? sub.userLastName ?? '').trim();
                                const email = (sub.user?.email ?? sub.userEmail ?? '').trim();
                                const fullName = [firstName, lastName].filter(Boolean).join(' ').trim();
                                const applicantName = fullName || (userId ? `User #${userId}` : 'Bilinmeyen Kullanici');
                                const statusKey = normalizeSubmissionStatus(sub.status);
                                const submissionId = normalizeEntityId(sub.id);
                                const canApprove = statusKey !== 'APPROVED';
                                const canReject = statusKey === 'PENDING' || statusKey === 'APPROVED';
                                const canDelete = statusKey !== 'REMOVED' && statusKey !== 'CANCELED';

                                return (
                                    <TableRow key={sub.id}>
                                        <TableCell>
                                            <div className="font-medium text-foreground">{applicantName}</div>
                                            <div className="text-xs text-muted-foreground">{email || '-'}</div>
                                            <div className="text-[10px] text-muted-foreground mt-1 uppercase tracking-wide">ID: {shortUserId !== '-' ? `${shortUserId}...` : '-'}</div>
                                        </TableCell>
                                        <TableCell>
                                            <div className="font-medium">{sub.postingTitle || 'Pozisyon Bilinmiyor'}</div>
                                            <Badge variant="outline" className="text-[10px] mt-1">{sub.postingCategory || '-'}</Badge>
                                        </TableCell>
                                        <TableCell>
                                            {sub.cvDownloadUrl ? (
                                                <div className="flex items-center gap-2">
                                                    <Button variant="ghost" size="sm" className="h-8 gap-2 text-blue-600 dark:text-blue-300 dark:hover:bg-blue-500/10" onClick={() => handleViewCV(sub.cvDownloadUrl!)}>
                                                        <Eye className="w-4 h-4" /> View
                                                    </Button>
                                                    <Button variant="ghost" size="sm" className="h-8 gap-2 text-blue-600 dark:text-blue-300 dark:hover:bg-blue-500/10" onClick={() => handleDownloadCV(sub.cvDownloadUrl!, 'cv.pdf')}>
                                                        <Download className="w-4 h-4" /> Download
                                                    </Button>
                                                </div>
                                            ) : (
                                                <span className="text-xs italic text-muted-foreground">No CV</span>
                                            )}
                                        </TableCell>
                                        <TableCell>
                                            <Badge
                                                variant="outline"
                                                className={cn(
                                                    statusKey === 'APPROVED' && 'border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-50 dark:border-emerald-500/40 dark:bg-emerald-500/10 dark:text-emerald-300',
                                                    statusKey === 'REJECTED' && 'border-red-200 bg-red-50 text-red-700 hover:bg-red-50 dark:border-red-500/40 dark:bg-red-500/10 dark:text-red-300',
                                                    statusKey === 'PENDING' && 'border-yellow-200 bg-yellow-100 text-yellow-800 hover:bg-yellow-100 dark:border-yellow-500/40 dark:bg-yellow-500/10 dark:text-yellow-300',
                                                    statusKey === 'CANCELED' && 'border-orange-200 bg-orange-50 text-orange-700 hover:bg-orange-50 dark:border-orange-500/40 dark:bg-orange-500/10 dark:text-orange-300',
                                                    statusKey === 'REMOVED' && 'border-fuchsia-200 bg-fuchsia-50 text-fuchsia-700 hover:bg-fuchsia-50 dark:border-fuchsia-500/40 dark:bg-fuchsia-500/10 dark:text-fuchsia-300',
                                                )}
                                            >
                                                {getSubmissionStatusLabel(sub.status)}
                                            </Badge>
                                        </TableCell>
                                        <TableCell className="text-sm text-muted-foreground">
                                            {sub.createdAt ? new Date(sub.createdAt).toLocaleDateString() : '-'}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <div className="flex justify-end gap-2">
                                                {submissionId && canApprove && (
                                                    <>
                                                        <Button
                                                            variant="outline"
                                                            size="sm"
                                                            onClick={() => acceptMutation.mutate(submissionId)}
                                                            className="border-green-200 text-green-700 hover:bg-green-50 dark:border-green-500/40 dark:text-green-300 dark:hover:bg-green-500/10"
                                                        >
                                                            {statusKey === 'REJECTED' || statusKey === 'CANCELED' || statusKey === 'REMOVED' ? 'Re-Approve' : 'Approve'}
                                                        </Button>
                                                    </>
                                                )}
                                                {submissionId && canReject && (
                                                    <>
                                                        <Button
                                                            variant="outline"
                                                            size="sm"
                                                            onClick={() => rejectMutation.mutate(submissionId)}
                                                            className="border-red-200 text-red-700 hover:bg-red-50 dark:border-red-500/40 dark:text-red-300 dark:hover:bg-red-500/10"
                                                        >
                                                            Reject
                                                        </Button>
                                                    </>
                                                )}
                                                {submissionId && canDelete && (
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={() => {
                                                            setDeleteTarget({
                                                                id: submissionId,
                                                                applicantName,
                                                                statusLabel: getSubmissionStatusLabel(sub.status),
                                                            });
                                                            setDeleteConfirmText('');
                                                        }}
                                                        className="border-border/80 text-muted-foreground hover:bg-muted/40 dark:text-foreground/80"
                                                    >
                                                        Delete
                                                    </Button>
                                                )}
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                );
                            })
                        )}
                    </TableBody>
                </Table>
            </div>

            <Dialog
                open={Boolean(deleteTarget)}
                onOpenChange={(open) => {
                    if (!open) {
                        setDeleteTarget(null);
                        setDeleteConfirmText('');
                    }
                }}
            >
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Basvuru silinsin mi?</DialogTitle>
                        <DialogDescription>
                            Bu islem geri alinamaz. Onaylamak icin kutuya <span className="font-semibold text-foreground">SIL</span> yazin.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-2 text-xs text-muted-foreground">
                        <p>Aday: {deleteTarget?.applicantName ?? '-'}</p>
                        <p>Durum: {deleteTarget?.statusLabel ?? '-'}</p>
                    </div>
                    <Input
                        value={deleteConfirmText}
                        onChange={(event) => setDeleteConfirmText(event.target.value.toUpperCase())}
                        placeholder="SIL"
                    />
                    <DialogFooter>
                        <Button
                            variant="outline"
                            onClick={() => {
                                setDeleteTarget(null);
                                setDeleteConfirmText('');
                            }}
                        >
                            Iptal
                        </Button>
                        <Button
                            variant="destructive"
                            disabled={!deleteTarget || deleteConfirmText !== 'SIL' || removeMutation.isPending}
                            onClick={() => {
                                if (!deleteTarget) return;
                                removeMutation.mutate(deleteTarget.id, {
                                    onSuccess: () => {
                                        setDeleteTarget(null);
                                        setDeleteConfirmText('');
                                    },
                                });
                            }}
                        >
                            Sil
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={manualAddOpen} onOpenChange={setManualAddOpen}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Kullanici Ekle</DialogTitle>
                        <DialogDescription>
                            Basvuru suresini kacirmis bir kullaniciyi bu projeye onayli uye olarak ekleyin.
                        </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-3">
                        <div className="space-y-2">
                            <span className="text-xs font-medium text-muted-foreground">Kullanici</span>
                            <Select value={manualAddUserId} onValueChange={setManualAddUserId}>
                                <SelectTrigger>
                                    <SelectValue placeholder="Kullanici secin" />
                                </SelectTrigger>
                                <SelectContent>
                                    {addableUsers.length === 0 ? (
                                        <SelectItem value="__none__" disabled>
                                            Eklenebilir kullanici kalmadi
                                        </SelectItem>
                                    ) : (
                                        addableUsers.map((user) => (
                                            <SelectItem key={String(user.id)} value={String(user.id)}>
                                                {user.firstName || user.lastName
                                                    ? `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim()
                                                    : user.email}{' '}
                                                ({user.email})
                                            </SelectItem>
                                        ))
                                    )}
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setManualAddOpen(false)}>Iptal</Button>
                        <Button
                            onClick={() => manualAddMutation.mutate()}
                            disabled={!manualAddUserId || manualAddUserId === '__none__' || manualAddMutation.isPending}
                        >
                            Kullanici Ekle
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
};

export default AdminSubmissionsPage;
