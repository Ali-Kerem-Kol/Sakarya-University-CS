import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
    createProjectTask,
    deleteAdminTask,
    getTaskAssignmentForAdmin,
    listApprovedMembersByProject,
    listProjectAssignments,
    patchAdminTask,
    reviewTaskAssignment,
    reviewAdminTask,
    TaskAssignmentDetailResponse,
    TaskAssignmentListItemResponse,
    TaskAssignmentStatus,
    TaskReviewDecision,
    TaskGraphReviewStatus,
    TaskScope,
    TaskUserSummary,
    uploadTaskAttachments,
} from '@/api/adminTasks';
import { TaskGraphNode } from '@/api/adminTimeline';
import { TaskCommitGraph } from '@/components/TaskCommitGraph';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useTaskGraph } from '@/hooks/useTaskGraph';
import { useToast } from '@/hooks/use-toast';
import { getTaskGraphStatusBadgeClass, getTaskGraphStatusLabel } from '@/lib/taskGraphStatus';
import { resolveUserColor } from '@/lib/userColor';
import { cn } from '@/lib/utils';

interface ProjectTaskGraphManagerProps {
    projectId: string;
    title?: string;
    initialFocusedUserId?: string;
}

interface TaskFormState {
    title: string;
    description: string;
    scope: TaskScope;
    assignedToUserId: string;
    status: TaskGraphReviewStatus;
}

const emptyCreateForm: TaskFormState = {
    title: '',
    description: '',
    scope: 'MAIN',
    assignedToUserId: '',
    status: 'PENDING',
};

const toDisplayName = (user?: TaskUserSummary) => {
    if (!user) return '-';
    const fullName = `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim();
    return fullName || user.email || `User #${String(user.userId)}`;
};

const formatDateTime = (value?: string | null) => (value ? new Date(value).toLocaleString('tr-TR') : '-');
const toTaskScope = (node: TaskGraphNode): TaskScope => (node.branchKey === 'MAIN' ? 'MAIN' : 'USER');

const toTaskIdParam = (node: TaskGraphNode): string | number => {
    const maybeNumber = Number(node.taskId);
    return Number.isFinite(maybeNumber) ? maybeNumber : node.taskId;
};

const toOptionalNumber = (value: string): number | undefined => {
    const trimmed = value.trim();
    if (!trimmed) return undefined;
    const parsed = Number(trimmed);
    return Number.isFinite(parsed) ? parsed : undefined;
};

const toEditableStatus = (status: TaskGraphNode['status']): TaskGraphReviewStatus =>
    status === 'SUBMITTED' ? 'PENDING' : status;

const toAssignmentStatusLabel = (status?: TaskAssignmentListItemResponse['status']) => {
    switch (status) {
        case 'APPROVED':
            return 'Onaylandi';
        case 'DONE':
            return 'Tamamlandi';
        case 'SUBMITTED':
            return 'Admin onayi bekliyor';
        case 'REVISION_REQUESTED':
            return 'Revizyon istendi';
        case 'REJECTED':
        case 'FAILED':
            return 'Basarisiz';
        default:
            return 'Atandi';
    }
};

const mapAssignmentStatusToGraphStatus = (status?: TaskAssignmentStatus): TaskGraphNode['status'] => {
    switch (status) {
        case 'APPROVED':
        case 'DONE':
            return 'SUCCESS';
        case 'REJECTED':
        case 'FAILED':
            return 'FAILED';
        case 'REVISION_REQUESTED':
            return 'REVISION_REQUESTED';
        case 'SUBMITTED':
            return 'SUBMITTED';
        default:
            return 'PENDING';
    }
};

const statusActions: Array<{ label: string; status: TaskGraphReviewStatus; variant?: 'default' | 'destructive' | 'outline' | 'secondary' }> = [
    { label: 'SUCCESS', status: 'SUCCESS' },
    { label: 'FAILED', status: 'FAILED', variant: 'destructive' },
    { label: 'REVISION_REQUESTED', status: 'REVISION_REQUESTED', variant: 'outline' },
    { label: 'PENDING', status: 'PENDING', variant: 'secondary' },
];

const ProjectTaskGraphManager: React.FC<ProjectTaskGraphManagerProps> = ({ projectId, title = 'Gorev Gecmisi Yonetimi', initialFocusedUserId }) => {
    const queryClient = useQueryClient();
    const { toast } = useToast();

    const [selectedNodeId, setSelectedNodeId] = useState('');
    const [createOpen, setCreateOpen] = useState(false);
    const [editOpen, setEditOpen] = useState(false);
    const [deleteOpen, setDeleteOpen] = useState(false);

    const [createForm, setCreateForm] = useState<TaskFormState>(emptyCreateForm);
    const [editForm, setEditForm] = useState<TaskFormState>(emptyCreateForm);
    const [createAttachments, setCreateAttachments] = useState<FileList | null>(null);
    const [focusedUserId, setFocusedUserId] = useState<string>('');

    const [selectedMainAssignmentId, setSelectedMainAssignmentId] = useState<number | null>(null);

    const { graph, isLoading, isError } = useTaskGraph({ mode: 'admin', projectId });

    const { data: members } = useQuery({
        queryKey: ['admin-task-approved-members', projectId],
        queryFn: () => listApprovedMembersByProject(projectId),
        enabled: Boolean(projectId),
    });

    const { data: projectAssignments } = useQuery({
        queryKey: ['admin-project-assignments', projectId],
        queryFn: () => listProjectAssignments(projectId),
        enabled: Boolean(projectId),
    });

    const memberMap = useMemo(() => {
        const map = new Map<number, TaskUserSummary>();
        (members ?? []).forEach((member) => map.set(member.userId, member));
        return map;
    }, [members]);

    const activeMembers = useMemo(() => {
        const map = new Map<number, TaskUserSummary>();
        (projectAssignments ?? []).forEach((assignment) => {
            const assignee = assignment.assignee;
            if (!assignee?.userId) return;
            map.set(assignee.userId, assignee);
        });
        (members ?? []).forEach((member) => {
            if (!map.has(member.userId)) {
                map.set(member.userId, member);
            }
        });
        return Array.from(map.values()).sort((a, b) => toDisplayName(a).localeCompare(toDisplayName(b), 'tr'));
    }, [members, projectAssignments]);
    const memberStatsById = useMemo(() => {
        const stats = new Map<string, {
            total: number;
            pending: number;
            submitted: number;
            completed: number;
            failed: number;
            latestStatus?: TaskAssignmentListItemResponse['status'];
            latestAt?: string | null;
        }>();

        (projectAssignments ?? []).forEach((assignment) => {
            const userId = assignment.assignee?.userId;
            if (!userId) return;
            const key = String(userId);
            const current = stats.get(key) ?? {
                total: 0,
                pending: 0,
                submitted: 0,
                completed: 0,
                failed: 0,
            };

            current.total += 1;
            if (assignment.status === 'SUBMITTED') current.submitted += 1;
            else if (assignment.status === 'APPROVED' || assignment.status === 'DONE') current.completed += 1;
            else if (assignment.status === 'REJECTED' || assignment.status === 'FAILED') current.failed += 1;
            else current.pending += 1;

            const nextTime = new Date(assignment.assignedAt ?? 0).getTime();
            const currentTime = new Date(current.latestAt ?? 0).getTime();
            if (!current.latestAt || nextTime > currentTime) {
                current.latestAt = assignment.assignedAt;
                current.latestStatus = assignment.status;
            }

            stats.set(key, current);
        });

        return stats;
    }, [projectAssignments]);

    const branchNameMap = useMemo(() => {
        const map = new Map<string, string>();
        (graph?.branches ?? []).forEach((branch) => map.set(branch.branchKey, branch.branchName));
        return map;
    }, [graph?.branches]);

    const nodes = graph?.nodes ?? [];

    useEffect(() => {
        setFocusedUserId(initialFocusedUserId ?? '');
    }, [initialFocusedUserId, projectId]);

    useEffect(() => {
        if (nodes.length === 0) {
            setSelectedNodeId('');
            return;
        }
        if (!nodes.some((node) => node.nodeId === selectedNodeId)) {
            setSelectedNodeId(nodes[0].nodeId);
        }
    }, [nodes, selectedNodeId]);

    const selectedNode = nodes.find((node) => node.nodeId === selectedNodeId) ?? null;
    const isSelectedMainNode = selectedNode?.branchKey === 'MAIN';
    const highlightedTaskIds = useMemo(() => {
        if (!focusedUserId) return [];
        return (projectAssignments ?? [])
            .filter((assignment) => String(assignment.assignee?.userId ?? '') === focusedUserId)
            .map((assignment) => assignment.taskId);
    }, [focusedUserId, projectAssignments]);
    const focusedUserColor = useMemo(() => {
        if (!focusedUserId) return null;
        const focusedNode = nodes.find((node) => String(node.assignedToUserId ?? '') === focusedUserId);
        return resolveUserColor(focusedUserId, focusedNode?.assignedToUserColor ?? null);
    }, [focusedUserId, nodes]);
    const focusedUserTaskStatusMap = useMemo(() => {
        const map = new Map<string, TaskGraphNode['status']>();
        if (!focusedUserId) return map;
        (projectAssignments ?? [])
            .filter((assignment) => String(assignment.assignee?.userId ?? '') === focusedUserId)
            .forEach((assignment) => map.set(String(assignment.taskId), mapAssignmentStatusToGraphStatus(assignment.status)));
        return map;
    }, [focusedUserId, projectAssignments]);
    const resolveGraphNodeStatus = (node: TaskGraphNode): TaskGraphNode['status'] => {
        if (!focusedUserId) return node.status;
        const branchKey = String(node.branchKey ?? '').toUpperCase();
        if (branchKey !== 'MAIN') return node.status;
        return focusedUserTaskStatusMap.get(String(node.taskId)) ?? node.status;
    };
    const mainTaskAssignments = useMemo(() => {
        if (!selectedNode || !isSelectedMainNode) return [];
        const selectedTaskId = Number(selectedNode.taskId);
        if (!Number.isFinite(selectedTaskId)) return [];
        return (projectAssignments ?? []).filter((assignment) => assignment.taskId === selectedTaskId);
    }, [isSelectedMainNode, projectAssignments, selectedNode]);

    useEffect(() => {
        if (!isSelectedMainNode || mainTaskAssignments.length === 0) {
            setSelectedMainAssignmentId(null);
            return;
        }
        if (focusedUserId) {
            const focusedAssignment = mainTaskAssignments.find(
                (assignment) => String(assignment.assignee?.userId ?? '') === focusedUserId,
            );
            if (focusedAssignment) {
                if (selectedMainAssignmentId !== focusedAssignment.assignmentId) {
                    setSelectedMainAssignmentId(focusedAssignment.assignmentId);
                }
                return;
            }
        }
        if (selectedMainAssignmentId && mainTaskAssignments.some((assignment) => assignment.assignmentId === selectedMainAssignmentId)) {
            return;
        }
        setSelectedMainAssignmentId(mainTaskAssignments[0].assignmentId);
    }, [focusedUserId, isSelectedMainNode, mainTaskAssignments, selectedMainAssignmentId]);

    const { data: selectedMainAssignmentDetail } = useQuery({
        queryKey: ['admin-task-assignment-detail', selectedMainAssignmentId],
        queryFn: () => getTaskAssignmentForAdmin(String(selectedMainAssignmentId)),
        enabled: selectedMainAssignmentId != null,
    });

    useEffect(() => {
        if (!selectedNode) return;
        const scope = toTaskScope(selectedNode);
        const assignee = selectedNode.assignedToUserId != null ? String(selectedNode.assignedToUserId) : '';
        setEditForm({
            title: selectedNode.title,
            description: selectedNode.description ?? '',
            scope,
            assignedToUserId: assignee,
            status: toEditableStatus(selectedNode.status),
        });

    }, [selectedNode]);

    const invalidateGraph = async () => {
        await queryClient.invalidateQueries({ queryKey: ['task-graph', 'admin', String(projectId)] });
    };

    const createMutation = useMutation({
        mutationFn: async () => {
            const created = await createProjectTask(projectId, {
                title: createForm.title.trim(),
                description: createForm.description.trim(),
                scope: createForm.scope,
                assignedToUserId: createForm.scope === 'USER' ? toOptionalNumber(createForm.assignedToUserId) : undefined,
                assignMode: createForm.scope === 'USER' ? 'USER' : 'ALL',
            });
            if (createAttachments && createAttachments.length > 0) {
                await uploadTaskAttachments(created.id, Array.from(createAttachments));
            }
            return created;
        },
        onSuccess: async () => {
            await invalidateGraph();
            setCreateOpen(false);
            setCreateForm(emptyCreateForm);
            setCreateAttachments(null);
            toast({ title: 'Gorev olusturuldu' });
        },
        onError: (error: unknown) => {
            const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Olusturma basarisiz.';
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: message });
        },
    });

    const editMutation = useMutation({
        mutationFn: async () => {
            if (!selectedNode) return;
            const payload: {
                title?: string;
                description?: string;
                scope?: TaskScope;
                assignedToUserId?: number;
                status?: TaskGraphReviewStatus;
            } = {
                title: editForm.title.trim(),
                status: editForm.status,
                scope: editForm.scope,
            };

            payload.description = editForm.description.trim();

            if (editForm.scope === 'USER') {
                payload.assignedToUserId = toOptionalNumber(editForm.assignedToUserId);
            }

            await patchAdminTask(toTaskIdParam(selectedNode), payload);
        },
        onSuccess: async () => {
            await invalidateGraph();
            setEditOpen(false);
            toast({ title: 'Gorev guncellendi' });
        },
        onError: (error: unknown) => {
            const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Guncelleme basarisiz.';
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: message });
        },
    });

    const deleteMutation = useMutation({
        mutationFn: async () => {
            if (!selectedNode) return;
            await deleteAdminTask(toTaskIdParam(selectedNode));
        },
        onSuccess: async () => {
            await invalidateGraph();
            setDeleteOpen(false);
            toast({ title: 'Gorev silindi' });
        },
        onError: (error: unknown) => {
            const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Silme basarisiz.';
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: message });
        },
    });

    const reviewMutation = useMutation({
        mutationFn: async (status: TaskGraphReviewStatus) => {
            if (!selectedNode) return;
            const assignedToUserId = selectedNode.assignedToUserId ?? undefined;

            await reviewAdminTask(toTaskIdParam(selectedNode), {
                status,
                assignedToUserId,
            });
        },
        onSuccess: async () => {
            await invalidateGraph();
            toast({ title: 'Durum guncellendi' });
        },
        onError: (error: unknown) => {
            const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Durum guncelleme basarisiz.';
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: message });
        },
    });

    const assignmentReviewMutation = useMutation({
        mutationFn: async ({ assignmentId, decision }: { assignmentId: number; decision: TaskReviewDecision }) =>
            reviewTaskAssignment(assignmentId, { decision }),
        onSuccess: async (updated: TaskAssignmentDetailResponse) => {
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ['admin-project-assignments', projectId] }),
                queryClient.invalidateQueries({ queryKey: ['admin-task-assignment-detail', updated.assignmentId] }),
            ]);
            await invalidateGraph();
            toast({ title: 'Teslim durumu guncellendi' });
        },
        onError: (error: unknown) => {
            const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Onay islemi basarisiz.';
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: message });
        },
    });

    const resolveUserLabel = (assignedToUserId?: string | number | null) => {
        if (assignedToUserId == null) return null;
        const key = typeof assignedToUserId === 'string' ? Number(assignedToUserId) : assignedToUserId;
        const user = Number.isFinite(key) ? memberMap.get(key) : undefined;
        return user ? toDisplayName(user) : `User #${String(assignedToUserId)}`;
    };

    const canSubmitCreate = createForm.title.trim() && (createForm.scope === 'MAIN' || createForm.assignedToUserId);
    const canSubmitEdit = editForm.title.trim() && (editForm.scope === 'MAIN' || editForm.assignedToUserId);

    return (
        <div className="space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-2">
                <h2 className="text-xl font-semibold">{title}</h2>
                <Dialog open={createOpen} onOpenChange={setCreateOpen}>
                    <DialogTrigger asChild>
                        <Button>Gorev Olustur</Button>
                    </DialogTrigger>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>Gorev Olustur</DialogTitle>
                            <DialogDescription>MAIN veya USER kapsaminda yeni gorev olusturun.</DialogDescription>
                        </DialogHeader>

                        <div className="space-y-3">
                            <div className="space-y-2">
                                <Label>Title *</Label>
                                <Input
                                    value={createForm.title}
                                    onChange={(event) => setCreateForm((current) => ({ ...current, title: event.target.value }))}
                                />
                            </div>

                            <div className="space-y-2">
                                <Label>Description</Label>
                                <textarea
                                    className="w-full min-h-[90px] rounded-md border border-input bg-background px-3 py-2 text-sm"
                                    value={createForm.description}
                                    onChange={(event) => setCreateForm((current) => ({ ...current, description: event.target.value }))}
                                />
                            </div>

                            <div className="grid gap-3 md:grid-cols-2">
                                <div className="space-y-2">
                                    <Label>Scope *</Label>
                                    <Select
                                        value={createForm.scope}
                                        onValueChange={(value) => setCreateForm((current) => ({ ...current, scope: value as TaskScope, assignedToUserId: '' }))}
                                    >
                                        <SelectTrigger>
                                            <SelectValue />
                                        </SelectTrigger>
                                        <SelectContent>
                                            <SelectItem value="MAIN">MAIN</SelectItem>
                                            <SelectItem value="USER">USER</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </div>

                                <div className="space-y-2">
                                <Label>Kullanici (USER icin zorunlu)</Label>
                                    <Select
                                        value={createForm.assignedToUserId}
                                        onValueChange={(value) => setCreateForm((current) => ({ ...current, assignedToUserId: value }))}
                                        disabled={createForm.scope !== 'USER'}
                                    >
                                        <SelectTrigger>
                                            <SelectValue placeholder="Kullanici secin" />
                                        </SelectTrigger>
                                        <SelectContent>
                                            {(members ?? []).map((member) => (
                                                <SelectItem key={String(member.userId)} value={String(member.userId)}>
                                                    {toDisplayName(member)}
                                                </SelectItem>
                                            ))}
                                        </SelectContent>
                                    </Select>
                                </div>
                            </div>

                            <div className="space-y-2">
                                <Label>Ekler (opsiyonel)</Label>
                                <Input
                                    type="file"
                                    accept="application/pdf,.pdf"
                                    multiple
                                    onChange={(event) => setCreateAttachments(event.target.files)}
                                />
                            </div>
                        </div>

                        <DialogFooter>
                            <Button onClick={() => createMutation.mutate()} disabled={!canSubmitCreate || createMutation.isPending}>
                                Olustur
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </div>

            {isLoading ? (
                <div className="text-sm text-muted-foreground">Gorev grafigi yukleniyor...</div>
            ) : isError ? (
                <div className="rounded border border-red-200 bg-red-50 p-3 text-sm text-red-700 dark:border-red-500/40 dark:bg-red-500/10 dark:text-red-300">Gorev grafigi yuklenemedi.</div>
            ) : (
                <div className="grid gap-4 lg:grid-cols-[1.45fr_1fr]">
                    <Card className="min-h-[620px]">
                        <CardHeader>
                            <CardTitle>Gorev Grafigi</CardTitle>
                        </CardHeader>
                        <CardContent className="max-h-[72vh] overflow-auto pr-3">
                            <TaskCommitGraph
                                nodes={nodes}
                                edges={graph?.edges ?? []}
                                branches={graph?.branches ?? []}
                                selectedNodeId={selectedNodeId}
                                onSelectNode={setSelectedNodeId}
                                resolveAssigneeLabel={resolveUserLabel}
                                monochromeMode
                                highlightedUserId={focusedUserId || undefined}
                                highlightedTaskIds={highlightedTaskIds}
                                nodeColorMode={focusedUserId ? 'highlight-only' : 'monochrome'}
                                resolveAssigneeColor={(assignedToUserId, node) => {
                                    if (assignedToUserId == null) return node?.assignedToUserColor ?? null;
                                    if (focusedUserId && String(assignedToUserId) === focusedUserId) {
                                        return focusedUserColor;
                                    }
                                    return node?.assignedToUserColor ?? null;
                                }}
                                resolveDisplayStatus={resolveGraphNodeStatus}
                            />
                        </CardContent>
                    </Card>

                    <Card className="min-h-[620px]">
                        <CardHeader>
                            <CardTitle>Gorev Islemleri</CardTitle>
                        </CardHeader>
                        <CardContent className="max-h-[72vh] overflow-auto pr-2">
                            <div className="mb-4 space-y-2 rounded-md border border-border/70 bg-muted/40 p-3">
                                <div className="flex items-center justify-between gap-2">
                                    <p className="text-xs font-semibold text-foreground">Projede Aktif Ogrenciler</p>
                                </div>
                                {activeMembers.length === 0 ? (
                                    <p className="text-xs text-muted-foreground">Aktif ogrenci bulunamadi.</p>
                                ) : (
                                    <Select
                                        value={focusedUserId || 'all'}
                                        onValueChange={(value) => setFocusedUserId(value === 'all' ? '' : value)}
                                    >
                                        <SelectTrigger className="h-10 border-border/70 bg-card/70 text-sm">
                                            <SelectValue placeholder="Tum ogrenciler" />
                                        </SelectTrigger>
                                        <SelectContent className="border-border/70 bg-popover/95">
                                            <SelectItem value="all">Tum ogrenciler</SelectItem>
                                            {activeMembers.map((member) => {
                                                const memberId = String(member.userId);
                                                const stats = memberStatsById.get(memberId);
                                                const label = stats ? `${toDisplayName(member)} (${stats.total})` : toDisplayName(member);
                                                return (
                                                    <SelectItem key={memberId} value={memberId}>
                                                        {label}
                                                    </SelectItem>
                                                );
                                            })}
                                        </SelectContent>
                                    </Select>
                                )}
                                {focusedUserId && (
                                    <div className="rounded border border-border/70 bg-card/80 p-2 text-[11px] text-muted-foreground">
                                        {(() => {
                                            const selectedMember = activeMembers.find((member) => String(member.userId) === focusedUserId);
                                            const stats = memberStatsById.get(focusedUserId);
                                            return (
                                                <div className="space-y-1">
                                                    <p className="font-semibold text-foreground">{selectedMember ? toDisplayName(selectedMember) : `User #${focusedUserId}`}</p>
                                                    {selectedMember?.email && <p>E-posta: {selectedMember.email}</p>}
                                                    {stats && <p>Toplam: {stats.total} - Bekleyen: {stats.pending} - Onayli: {stats.completed} - Incelemede: {stats.submitted}</p>}
                                                    {stats?.latestStatus && <p>Son durum: {toAssignmentStatusLabel(stats.latestStatus)} ({formatDateTime(stats.latestAt)})</p>}
                                                </div>
                                            );
                                        })()}
                                    </div>
                                )}
                            </div>

                            {!selectedNode ? (
                                <p className="text-sm text-muted-foreground">Bir gorev dugumu secin.</p>
                            ) : (
                                <div className="space-y-4 text-sm">
                                    <div>
                                        <h3 className="text-base font-semibold text-foreground">{selectedNode.title}</h3>
                                        <p className="text-muted-foreground">{formatDateTime(selectedNode.createdAt)}</p>
                                    </div>

                                    <div className="space-y-1 text-xs text-muted-foreground">
                                        <p>Branch: {branchNameMap.get(selectedNode.branchKey) || selectedNode.branchKey}</p>
                                        <p>Gorev ID: {selectedNode.taskId}</p>
                                        {selectedNode.assignedToUserId != null && <p>Atanan: {resolveUserLabel(selectedNode.assignedToUserId)}</p>}
                                    </div>

                                    <Badge variant="outline" className={cn('border', getTaskGraphStatusBadgeClass(resolveGraphNodeStatus(selectedNode)))}>
                                        {getTaskGraphStatusLabel(resolveGraphNodeStatus(selectedNode))}
                                    </Badge>

                                    {isSelectedMainNode && (
                                        <div className="space-y-3 rounded-lg border border-border/70 bg-muted/35 p-3">
                                            <p className="text-sm font-semibold text-foreground">Ana Gorev Teslim Paneli</p>
                                            {mainTaskAssignments.length === 0 ? (
                                                <p className="text-xs text-muted-foreground">Bu gorev icin assignment bulunamadi.</p>
                                            ) : (
                                                <>
                                                    <Select
                                                        value={selectedMainAssignmentId != null ? String(selectedMainAssignmentId) : undefined}
                                                        onValueChange={(value) => setSelectedMainAssignmentId(Number(value))}
                                                    >
                                                        <SelectTrigger className="h-10 border-border/70 bg-card/70 text-sm">
                                                            <SelectValue placeholder="Teslim yapan ogrenciyi secin" />
                                                        </SelectTrigger>
                                                        <SelectContent className="max-h-72 border-border/70 bg-popover/95">
                                                            {mainTaskAssignments.map((assignment) => (
                                                                <SelectItem key={assignment.assignmentId} value={String(assignment.assignmentId)}>
                                                                    {toDisplayName(assignment.assignee)} - {toAssignmentStatusLabel(assignment.status)}
                                                                </SelectItem>
                                                            ))}
                                                        </SelectContent>
                                                    </Select>

                                                    {selectedMainAssignmentDetail && (
                                                        <div className="space-y-2 rounded-md border border-border/70 bg-card p-3">
                                                            <p className="text-xs font-semibold text-foreground">
                                                                Secili Kullanici: {toDisplayName(selectedMainAssignmentDetail.assignee)}
                                                            </p>
                                                            <p className="text-xs text-muted-foreground">
                                                                Teslim: {formatDateTime(selectedMainAssignmentDetail.submittedAt)} - Inceleme: {formatDateTime(selectedMainAssignmentDetail.reviewedAt)}
                                                            </p>

                                                            <div className="space-y-1">
                                                                <p className="text-xs font-medium text-foreground/90">Yanit Metni</p>
                                                                <div className="max-h-28 overflow-auto rounded border border-border/70 bg-muted/35 p-2 text-xs text-foreground/90">
                                                                    {selectedMainAssignmentDetail.textAnswer?.trim() || 'Metin yaniti yok.'}
                                                                </div>
                                                            </div>

                                                            <div className="space-y-1">
                                                                <p className="text-xs font-medium text-foreground/90">Teslim Dosyalari</p>
                                                                {selectedMainAssignmentDetail.submissionFiles.length === 0 ? (
                                                                    <p className="text-xs text-muted-foreground">Dosya yuklenmemis.</p>
                                                                ) : (
                                                                    <div className="space-y-1">
                                                                        {selectedMainAssignmentDetail.submissionFiles.map((file) => (
                                                                            <a
                                                                                key={file.id}
                                                                                href={file.downloadUrl}
                                                                                target="_blank"
                                                                                rel="noreferrer"
                                                                                className="block text-xs text-sky-700 underline"
                                                                            >
                                                                                {file.fileName}
                                                                            </a>
                                                                        ))}
                                                                    </div>
                                                                )}
                                                            </div>

                                                            <div className="flex flex-wrap gap-2 pt-1">
                                                                <Button
                                                                    size="sm"
                                                                    onClick={() => assignmentReviewMutation.mutate({ assignmentId: selectedMainAssignmentDetail.assignmentId, decision: 'APPROVED' })}
                                                                    disabled={assignmentReviewMutation.isPending}
                                                                >
                                                                    Onayla
                                                                </Button>
                                                                <Button
                                                                    size="sm"
                                                                    variant="outline"
                                                                    onClick={() => assignmentReviewMutation.mutate({ assignmentId: selectedMainAssignmentDetail.assignmentId, decision: 'REVISION_REQUESTED' })}
                                                                    disabled={assignmentReviewMutation.isPending}
                                                                >
                                                                    Revizyon Istegi
                                                                </Button>
                                                                <Button
                                                                    size="sm"
                                                                    variant="destructive"
                                                                    onClick={() => assignmentReviewMutation.mutate({ assignmentId: selectedMainAssignmentDetail.assignmentId, decision: 'REJECTED' })}
                                                                    disabled={assignmentReviewMutation.isPending}
                                                                >
                                                                    Reddet
                                                                </Button>
                                                            </div>
                                                        </div>
                                                    )}
                                                </>
                                            )}
                                        </div>
                                    )}

                                    {!isSelectedMainNode && (
                                        <div className="flex flex-wrap gap-2">
                                            {statusActions.map((item) => (
                                                <Button
                                                    key={item.status}
                                                    variant={item.variant ?? 'default'}
                                                    size="sm"
                                                    onClick={() => reviewMutation.mutate(item.status)}
                                                    disabled={reviewMutation.isPending}
                                                >
                                                    {item.label}
                                                </Button>
                                            ))}
                                        </div>
                                    )}

                                    <div className="flex gap-2 border-t pt-4">
                                        <Dialog open={editOpen} onOpenChange={setEditOpen}>
                                            <DialogTrigger asChild>
                                                <Button variant="outline" size="sm">Duzenle</Button>
                                            </DialogTrigger>
                                            <DialogContent>
                                                <DialogHeader>
                                                    <DialogTitle>Gorev Duzenle</DialogTitle>
                                                    <DialogDescription>Baslik, aciklama, kapsam ve durum bilgisini guncelleyin.</DialogDescription>
                                                </DialogHeader>

                                                <div className="space-y-3">
                                                    <div className="space-y-2">
                                                        <Label>Title *</Label>
                                                        <Input
                                                            value={editForm.title}
                                                            onChange={(event) => setEditForm((current) => ({ ...current, title: event.target.value }))}
                                                        />
                                                    </div>

                                                    <div className="space-y-2">
                                                        <Label>Description</Label>
                                                        <textarea
                                                            className="w-full min-h-[90px] rounded-md border border-input bg-background px-3 py-2 text-sm"
                                                            value={editForm.description}
                                                            onChange={(event) => setEditForm((current) => ({ ...current, description: event.target.value }))}
                                                        />
                                                    </div>

                                                    <div className="grid gap-3 md:grid-cols-2">
                                                        <div className="space-y-2">
                                                            <Label>Scope</Label>
                                                            <Select
                                                                value={editForm.scope}
                                                                onValueChange={(value) => setEditForm((current) => ({ ...current, scope: value as TaskScope, assignedToUserId: '' }))}
                                                            >
                                                                <SelectTrigger>
                                                                    <SelectValue />
                                                                </SelectTrigger>
                                                                <SelectContent>
                                                                    <SelectItem value="MAIN">MAIN</SelectItem>
                                                                    <SelectItem value="USER">USER</SelectItem>
                                                                </SelectContent>
                                                            </Select>
                                                        </div>

                                                        <div className="space-y-2">
                                                            <Label>Kullanici</Label>
                                                            <Select
                                                                value={editForm.assignedToUserId}
                                                                onValueChange={(value) => setEditForm((current) => ({ ...current, assignedToUserId: value }))}
                                                                disabled={editForm.scope !== 'USER'}
                                                            >
                                                                <SelectTrigger>
                                                                    <SelectValue placeholder="Kullanici secin" />
                                                                </SelectTrigger>
                                                                <SelectContent>
                                                                    {(members ?? []).map((member) => (
                                                                        <SelectItem key={String(member.userId)} value={String(member.userId)}>
                                                                            {toDisplayName(member)}
                                                                        </SelectItem>
                                                                    ))}
                                                                </SelectContent>
                                                            </Select>
                                                        </div>
                                                    </div>

                                                    <div className="space-y-2">
                                                        <Label>Status</Label>
                                                        <Select
                                                            value={editForm.status}
                                                            onValueChange={(value) => setEditForm((current) => ({ ...current, status: value as TaskGraphReviewStatus }))}
                                                        >
                                                            <SelectTrigger>
                                                                <SelectValue />
                                                            </SelectTrigger>
                                                            <SelectContent>
                                                                <SelectItem value="PENDING">PENDING</SelectItem>
                                                                <SelectItem value="SUCCESS">SUCCESS</SelectItem>
                                                                <SelectItem value="FAILED">FAILED</SelectItem>
                                                                <SelectItem value="REVISION_REQUESTED">REVISION_REQUESTED</SelectItem>
                                                            </SelectContent>
                                                        </Select>
                                                    </div>
                                                </div>

                                                <DialogFooter>
                                                    <Button onClick={() => editMutation.mutate()} disabled={!canSubmitEdit || editMutation.isPending}>
                                                        Kaydet
                                                    </Button>
                                                </DialogFooter>
                                            </DialogContent>
                                        </Dialog>

                                        <Dialog open={deleteOpen} onOpenChange={setDeleteOpen}>
                                            <DialogTrigger asChild>
                                                <Button variant="destructive" size="sm">Sil</Button>
                                            </DialogTrigger>
                                            <DialogContent>
                                                <DialogHeader>
                                                    <DialogTitle>Bu gorev silinsin mi?</DialogTitle>
                                                    <DialogDescription>Bu islem gorevi kalici olarak siler.</DialogDescription>
                                                </DialogHeader>
                                                <DialogFooter>
                                                    <Button variant="outline" onClick={() => setDeleteOpen(false)}>Iptal</Button>
                                                    <Button variant="destructive" onClick={() => deleteMutation.mutate()} disabled={deleteMutation.isPending}>
                                                        Sil
                                                    </Button>
                                                </DialogFooter>
                                            </DialogContent>
                                        </Dialog>
                                    </div>
                                </div>
                            )}
                        </CardContent>
                    </Card>
                </div>
            )}
        </div>
    );
};

export default ProjectTaskGraphManager;
