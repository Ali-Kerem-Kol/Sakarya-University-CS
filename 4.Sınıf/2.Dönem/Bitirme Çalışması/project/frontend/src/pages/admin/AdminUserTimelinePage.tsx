import React, { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchAdminPostings } from '@/api/admin';
import { TaskGraphNode } from '@/api/adminTimeline';
import { getTimelineAssignmentDetail } from '@/api/adminTimeline';
import { TaskAssignmentStatus, listAdminUsers, listUserAssignmentsForAdmin } from '@/api/adminTasks';
import { openDocument } from '@/api/client';
import { TaskCommitGraph } from '@/components/TaskCommitGraph';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useTaskGraph } from '@/hooks/useTaskGraph';
import { getTaskGraphStatusBadgeClass, getTaskGraphStatusLabel } from '@/lib/taskGraphStatus';
import { resolveUserColor } from '@/lib/userColor';
import { cn } from '@/lib/utils';

const formatDate = (value?: string | null) => (value ? new Date(value).toLocaleString('tr-TR') : '-');
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

const AdminUserTimelinePage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const [selectedProjectId, setSelectedProjectId] = useState<string>('');
    const [selectedNodeId, setSelectedNodeId] = useState<string>('');

    const { data: users } = useQuery({
        queryKey: ['admin-users-list-short'],
        queryFn: () => listAdminUsers(),
    });

    const { data: postings } = useQuery({
        queryKey: ['admin-postings-list-for-user-timeline'],
        queryFn: () => fetchAdminPostings(),
    });

    const { data: assignments, isLoading: isAssignmentsLoading } = useQuery({
        queryKey: ['admin-user-assignments', id],
        queryFn: () => listUserAssignmentsForAdmin(String(id)),
        enabled: Boolean(id),
    });

    const projectIds = useMemo(() => {
        const unique = new Set<string>();
        (assignments ?? []).forEach((item) => unique.add(String(item.projectId)));
        return Array.from(unique);
    }, [assignments]);

    useEffect(() => {
        if (projectIds.length > 0 && !selectedProjectId) {
            setSelectedProjectId(projectIds[0]);
        }
    }, [projectIds, selectedProjectId]);

    const { graph, isLoading: isGraphLoading } = useTaskGraph({
        mode: 'admin',
        projectId: selectedProjectId || '0',
    });

    const normalizedUserId = useMemo(() => String(id ?? '').trim(), [id]);

    const visibleGraph = useMemo(() => {
        if (!graph || !normalizedUserId) {
            return { branches: [], nodes: [], edges: [] };
        }

        const targetBranchKey = `USER-${normalizedUserId}`.toUpperCase();
        const allowedBranches = new Set<string>(['MAIN', targetBranchKey]);

        const nodes = (graph.nodes ?? []).filter((node) => {
            const key = String(node.branchKey ?? 'MAIN').toUpperCase();
            if (allowedBranches.has(key)) return true;
            return String(node.assignedToUserId ?? '') === normalizedUserId;
        });

        const visibleTaskIds = new Set(nodes.map((node) => String(node.taskId)));
        const edges = (graph.edges ?? []).filter((edge) => (
            visibleTaskIds.has(String(edge.fromTaskId)) && visibleTaskIds.has(String(edge.toTaskId))
        ));

        const branchKeys = new Set(nodes.map((node) => String(node.branchKey ?? 'MAIN').toUpperCase()));
        const branches = (graph.branches ?? []).filter((branch) => branchKeys.has(String(branch.branchKey ?? 'MAIN').toUpperCase()));

        return { branches, nodes, edges };
    }, [graph, normalizedUserId]);

    useEffect(() => {
        const nodes = visibleGraph.nodes;
        if (nodes.length === 0) {
            setSelectedNodeId('');
            return;
        }
        if (!nodes.some((node) => node.nodeId === selectedNodeId)) {
            setSelectedNodeId(nodes[0].nodeId);
        }
    }, [selectedNodeId, visibleGraph.nodes]);

    const selectedNode = useMemo(
        () => visibleGraph.nodes.find((node) => node.nodeId === selectedNodeId) ?? null,
        [selectedNodeId, visibleGraph.nodes],
    );

    const assignmentIdByTask = useMemo(() => {
        const map = new Map<string, number>();
        (assignments ?? [])
            .filter((item) => String(item.projectId) === selectedProjectId)
            .forEach((item) => map.set(String(item.taskId), item.assignmentId));
        return map;
    }, [assignments, selectedProjectId]);
    const assignmentStatusByTask = useMemo(() => {
        const map = new Map<string, TaskGraphNode['status']>();
        (assignments ?? [])
            .filter((item) => String(item.projectId) === selectedProjectId)
            .forEach((item) => map.set(String(item.taskId), mapAssignmentStatusToGraphStatus(item.status)));
        return map;
    }, [assignments, selectedProjectId]);

    const selectedAssignmentId = selectedNode ? assignmentIdByTask.get(String(selectedNode.taskId)) : undefined;

    const highlightedTaskIds = useMemo(
        () => (assignments ?? [])
            .filter((item) => String(item.projectId) === selectedProjectId)
            .map((item) => item.taskId),
        [assignments, selectedProjectId],
    );

    const { data: selectedAssignment } = useQuery({
        queryKey: ['admin-user-assignment-detail', selectedAssignmentId],
        queryFn: () => getTimelineAssignmentDetail(String(selectedAssignmentId)),
        enabled: Boolean(selectedAssignmentId),
    });

    const user = (users ?? []).find((item) => String(item.id) === normalizedUserId);
    const userLabel = user
        ? `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || user.email
        : `User #${normalizedUserId || '-'}`;
    const focusedUserColor = resolveUserColor(normalizedUserId || null, user?.preferredColor);

    const projectName = useMemo(() => {
        const found = (postings ?? []).find((item) => String(item.id) === selectedProjectId);
        return found?.title ?? (selectedProjectId ? `Project #${selectedProjectId}` : '');
    }, [postings, selectedProjectId]);
    const resolveNodeStatus = (node: TaskGraphNode): TaskGraphNode['status'] => {
        if (String(node.branchKey ?? '').toUpperCase() !== 'MAIN') {
            return node.status;
        }
        return assignmentStatusByTask.get(String(node.taskId)) ?? node.status;
    };

    return (
        <div className="space-y-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-bold">Kullanici Gorev Gecmisi</h1>
                    <p className="text-sm text-slate-500">{userLabel}</p>
                </div>
                <div className="flex items-center gap-2">
                    <div className="w-[280px]">
                        <Select value={selectedProjectId} onValueChange={setSelectedProjectId}>
                            <SelectTrigger>
                                <SelectValue placeholder="Proje secin" />
                            </SelectTrigger>
                            <SelectContent>
                                {projectIds.map((projectId) => (
                                    <SelectItem key={projectId} value={projectId}>
                                        {(postings ?? []).find((item) => String(item.id) === projectId)?.title || `Project #${projectId}`}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                    </div>
                    <Link to="/admin/admin-users">
                        <Button variant="outline">Kullanicilara Don</Button>
                    </Link>
                </div>
            </div>

            {selectedProjectId && <p className="text-sm text-slate-600">{projectName}</p>}

            {isAssignmentsLoading ? (
                <div className="text-sm text-slate-500">Kullanici gorevleri yukleniyor...</div>
            ) : !selectedProjectId ? (
                <Card>
                    <CardContent className="py-10 text-center text-slate-500">Bu kullaniciya ait gorev kaydi yok.</CardContent>
                </Card>
            ) : isGraphLoading ? (
                <div className="text-sm text-slate-500">Gorev grafigi yukleniyor...</div>
            ) : (
                <div className="grid gap-4 lg:grid-cols-[1.45fr_1fr]">
                    <Card className="min-h-[620px]">
                        <CardHeader>
                            <CardTitle>Gorev Grafigi</CardTitle>
                        </CardHeader>
                        <CardContent className="max-h-[72vh] overflow-auto pr-3">
                            <TaskCommitGraph
                                nodes={visibleGraph.nodes}
                                edges={visibleGraph.edges}
                                branches={visibleGraph.branches}
                                selectedNodeId={selectedNodeId}
                                onSelectNode={setSelectedNodeId}
                                resolveAssigneeLabel={() => userLabel}
                                highlightedUserId={normalizedUserId}
                                highlightedTaskIds={highlightedTaskIds}
                                resolveAssigneeColor={(id, node) => {
                                    if (id == null) return node?.assignedToUserColor ?? null;
                                    if (String(id) === normalizedUserId) return focusedUserColor;
                                    const matched = (users ?? []).find((item) => String(item.id) === String(id));
                                    return resolveUserColor(id, matched?.preferredColor);
                                }}
                                resolveDisplayStatus={resolveNodeStatus}
                            />
                        </CardContent>
                    </Card>

                    <Card className="min-h-[620px]">
                        <CardHeader>
                            <CardTitle>Secili Gorev</CardTitle>
                        </CardHeader>
                        <CardContent>
                            {!selectedNode ? (
                                <p className="text-sm text-slate-500">Bir gorev dugumu secin.</p>
                            ) : (
                                <div className="space-y-4 text-sm">
                                    <div>
                                        <h3 className="text-base font-semibold text-slate-900">{selectedNode.title}</h3>
                                        <p className="text-slate-500">{formatDate(selectedNode.createdAt)}</p>
                                    </div>

                                    <Badge variant="outline" className={cn('border', getTaskGraphStatusBadgeClass(resolveNodeStatus(selectedNode)))}>
                                        {getTaskGraphStatusLabel(resolveNodeStatus(selectedNode))}
                                    </Badge>

                                    <div className="space-y-2">
                                        <p className="font-medium text-slate-700">Kullanici Cevabi</p>
                                        <p className="whitespace-pre-wrap text-slate-700">{selectedAssignment?.textAnswer || '-'}</p>
                                    </div>

                                    <div className="space-y-2">
                                        <p className="font-medium text-slate-700">Yuklenen Dosyalar</p>
                                        {(selectedAssignment?.submissionFiles ?? []).length === 0 ? (
                                            <p className="text-slate-500">Dosya yok.</p>
                                        ) : (
                                            selectedAssignment?.submissionFiles.map((file) => (
                                                <div key={file.id} className="flex items-center justify-between rounded border p-2">
                                                    <span className="truncate">{file.fileName}</span>
                                                    <div className="flex gap-2">
                                                        <Button size="sm" variant="outline" onClick={() => openDocument(file.downloadUrl, { mode: 'view', contentType: file.contentType || 'application/pdf' })}>Goruntule</Button>
                                                        <Button size="sm" variant="outline" onClick={() => openDocument(file.downloadUrl, { mode: 'download', defaultFileName: file.fileName })}>Indir</Button>
                                                    </div>
                                                </div>
                                            ))
                                        )}
                                    </div>

                                    <div className="space-y-1 border-t pt-3 text-xs text-slate-600">
                                        <p>Assigned: {formatDate(selectedAssignment?.assignedAt)}</p>
                                        <p>Submitted: {formatDate(selectedAssignment?.submittedAt)}</p>
                                        <p>Reviewed: {formatDate(selectedAssignment?.reviewedAt)}</p>
                                        <p>Review Note: {selectedAssignment?.reviewNote || '-'}</p>
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

export default AdminUserTimelinePage;
