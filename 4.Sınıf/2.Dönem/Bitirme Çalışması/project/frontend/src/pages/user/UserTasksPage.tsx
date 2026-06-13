import React, { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '@/auth/AuthContext';
import { openDocument } from '@/api/client';
import { fetchPublicPostings } from '@/api/postings';
import { getMyTaskAssignment, listMyTaskAssignments, submitMyTaskAssignment } from '@/api/userTasks';
import { TaskCommitGraph } from '@/components/TaskCommitGraph';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useTaskGraph } from '@/hooks/useTaskGraph';
import { useToast } from '@/hooks/use-toast';
import { getTaskGraphStatusBadgeClass, getTaskGraphStatusLabel } from '@/lib/taskGraphStatus';
import { resolveUserColor } from '@/lib/userColor';
import { cn } from '@/lib/utils';

const formatDateTime = (value?: string | null) => (value ? new Date(value).toLocaleString('tr-TR') : '-');

const UserTasksPage: React.FC = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();
    const { user } = useAuth();
    const [selectedProjectId, setSelectedProjectId] = useState<string>('');
    const [selectedNodeId, setSelectedNodeId] = useState<string>('');
    const [textAnswer, setTextAnswer] = useState('');
    const [submissionFiles, setSubmissionFiles] = useState<FileList | null>(null);

    const { data: assignments } = useQuery({
        queryKey: ['my-task-assignments'],
        queryFn: listMyTaskAssignments,
    });

    const { data: postings } = useQuery({
        queryKey: ['public-postings-for-task-graph'],
        queryFn: () => fetchPublicPostings(),
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

    const { graph, isLoading } = useTaskGraph({ mode: 'user', projectId: selectedProjectId || undefined });

    const myUserId = useMemo(() => {
        const parsed = Number(user?.id);
        return Number.isFinite(parsed) ? parsed : null;
    }, [user?.id]);
    const myColor = resolveUserColor(myUserId, null);

    const visibleGraph = useMemo(() => {
        if (!graph) {
            return { branches: [], nodes: [], edges: [] };
        }

        const allowedBranches = new Set<string>(['MAIN']);
        (graph.branches ?? []).forEach((branch) => {
            const key = String(branch.branchKey ?? '').toUpperCase();
            const owner = branch.ownerUserId ?? null;
            const mineByOwner = myUserId != null && owner === myUserId;
            const mineByKey = myUserId != null && key === `USER-${myUserId}`;
            if (mineByOwner || mineByKey || key === 'MAIN') {
                allowedBranches.add(key);
            }
        });

        const nodes = (graph.nodes ?? []).filter((node) => {
            const key = String(node.branchKey ?? 'MAIN').toUpperCase();
            if (allowedBranches.has(key)) return true;
            if (key === 'MAIN') return true;
            return myUserId != null && node.assignedToUserId === myUserId;
        });

        const visibleTaskIds = new Set(nodes.map((node) => String(node.taskId)));
        const edges = (graph.edges ?? []).filter((edge) => (
            visibleTaskIds.has(String(edge.fromTaskId)) && visibleTaskIds.has(String(edge.toTaskId))
        ));

        const branchKeys = new Set(nodes.map((node) => String(node.branchKey ?? 'MAIN').toUpperCase()));
        const branches = (graph.branches ?? []).filter((branch) => branchKeys.has(String(branch.branchKey ?? 'MAIN').toUpperCase()));

        return { branches, nodes, edges };
    }, [graph, myUserId]);

    const nodes = visibleGraph.nodes;
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

    const assignmentIdByTaskId = useMemo(() => {
        const map = new Map<string, number>();
        (assignments ?? []).forEach((item) => {
            map.set(String(item.taskId), item.assignmentId);
        });
        return map;
    }, [assignments]);

    const selectedAssignmentId = selectedNode ? assignmentIdByTaskId.get(selectedNode.taskId) : undefined;

    const { data: selectedAssignment } = useQuery({
        queryKey: ['my-task-assignment-detail', selectedAssignmentId],
        queryFn: () => getMyTaskAssignment(String(selectedAssignmentId)),
        enabled: Boolean(selectedAssignmentId),
    });

    useEffect(() => {
        setTextAnswer(selectedAssignment?.textAnswer ?? '');
        setSubmissionFiles(null);
    }, [selectedAssignmentId, selectedAssignment?.textAnswer]);

    const submitMutation = useMutation({
        mutationFn: async () => {
            if (!selectedAssignmentId) return;
            await submitMyTaskAssignment(selectedAssignmentId, {
                textAnswer,
                files: submissionFiles ? Array.from(submissionFiles) : [],
            });
        },
        onSuccess: async () => {
            await Promise.all([
                queryClient.invalidateQueries({ queryKey: ['my-task-assignment-detail', selectedAssignmentId] }),
                queryClient.invalidateQueries({ queryKey: ['my-task-assignments'] }),
                queryClient.invalidateQueries({ queryKey: ['task-graph', 'user'] }),
            ]);
            setSubmissionFiles(null);
            toast({ title: 'Gorev teslim edildi' });
        },
        onError: (error: unknown) => {
            const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Teslim islemi basarisiz.';
            toast({ variant: 'destructive', title: 'Islem basarisiz', description: message });
        },
    });

    const projectName = useMemo(() => {
        const found = (postings ?? []).find((item) => String(item.id) === selectedProjectId);
        return found?.title ?? (selectedProjectId ? `Project #${selectedProjectId}` : '');
    }, [postings, selectedProjectId]);

    return (
        <div className="space-y-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-bold">Gorev Gecmisim</h1>
                    <p className="text-sm text-muted-foreground">MAIN ve kisisel branch gorevlerini zaman cizelgesinde takip edin.</p>
                </div>
                <div className="w-[280px]">
                    <Select value={selectedProjectId} onValueChange={setSelectedProjectId}>
                        <SelectTrigger>
                            <SelectValue placeholder="Select project" />
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
            </div>

            {selectedProjectId && <p className="text-sm text-muted-foreground">{projectName}</p>}

            {!selectedProjectId ? (
                <Card>
                    <CardContent className="py-10 text-center text-muted-foreground">Henuz gorev kaydi yok.</CardContent>
                </Card>
            ) : isLoading ? (
                <div className="text-sm text-muted-foreground">Gorev grafigi yukleniyor...</div>
            ) : (
                <div className="grid gap-4 lg:grid-cols-[1.45fr_1fr]">
                    <Card className="min-h-[620px]">
                        <CardHeader>
                            <CardTitle>Gorev Grafigi</CardTitle>
                        </CardHeader>
                        <CardContent className="max-h-[72vh] overflow-auto pr-3">
                            <TaskCommitGraph
                                nodes={nodes}
                                edges={visibleGraph.edges}
                                branches={visibleGraph.branches}
                                selectedNodeId={selectedNodeId}
                                onSelectNode={setSelectedNodeId}
                                resolveAssigneeLabel={(assignedToUserId) => (
                                    assignedToUserId != null ? `User #${String(assignedToUserId)}` : null
                                )}
                                resolveAssigneeColor={(assignedToUserId, node) => {
                                    if (assignedToUserId != null && myUserId != null && assignedToUserId === myUserId) {
                                        return myColor;
                                    }
                                    return node?.assignedToUserColor ?? null;
                                }}
                                highlightedUserId={myUserId}
                                highlightedTaskIds={(assignments ?? []).map((item) => item.taskId)}
                            />
                        </CardContent>
                    </Card>

                    <Card className="min-h-[620px]">
                        <CardHeader>
                            <CardTitle>Gorev Detayi</CardTitle>
                        </CardHeader>
                        <CardContent>
                            {!selectedNode ? (
                                <p className="text-sm text-muted-foreground">Bir gorev dugumu secin.</p>
                            ) : (
                                <div className="space-y-4 text-sm">
                                    <div>
                                        <h3 className="text-base font-semibold text-foreground">{selectedNode.title}</h3>
                                        <p className="text-muted-foreground">{formatDateTime(selectedNode.createdAt)}</p>
                                    </div>

                                    <Badge variant="outline" className={cn('border', getTaskGraphStatusBadgeClass(selectedNode.status))}>
                                        {getTaskGraphStatusLabel(selectedNode.status)}
                                    </Badge>

                                    <div className="space-y-2">
                                        <p className="font-medium text-foreground/90">Description</p>
                                        <p className="whitespace-pre-wrap text-foreground/85">{selectedAssignment?.taskDescription || 'No description available.'}</p>
                                    </div>

                                    <div className="space-y-2">
                                        <p className="font-medium text-foreground/90">Admin Attachments</p>
                                        {(selectedAssignment?.taskAttachments ?? []).length === 0 ? (
                                            <p className="text-muted-foreground">No attachments.</p>
                                        ) : (
                                            selectedAssignment?.taskAttachments.map((attachment) => (
                                                <div key={attachment.id} className="flex items-center justify-between rounded border p-2">
                                                    <span className="truncate">{attachment.fileName}</span>
                                                    <div className="flex gap-2">
                                                        <Button size="sm" variant="outline" onClick={() => openDocument(attachment.downloadUrl, { mode: 'view', contentType: 'application/pdf' })}>View</Button>
                                                        <Button size="sm" variant="outline" onClick={() => openDocument(attachment.downloadUrl, { mode: 'download', defaultFileName: attachment.fileName })}>Download</Button>
                                                    </div>
                                                </div>
                                            ))
                                        )}
                                    </div>

                                    <div className="space-y-2 border-t border-border/70 pt-4">
                                        <p className="font-medium text-foreground/90">Senin Cevabin</p>
                                        <textarea
                                            className="w-full min-h-[110px] rounded-md border border-input bg-background px-3 py-2 text-sm"
                                            value={textAnswer}
                                            onChange={(event) => setTextAnswer(event.target.value)}
                                            placeholder="Cevabinizi yazin..."
                                        />
                                        <Input
                                            type="file"
                                            accept="application/pdf,.pdf"
                                            multiple
                                            onChange={(event) => setSubmissionFiles(event.target.files)}
                                        />
                                        {(selectedAssignment?.submissionFiles ?? []).length > 0 && (
                                            <div className="space-y-2">
                                                <p className="text-xs text-muted-foreground">Daha once yuklenen dosyalar</p>
                                                {selectedAssignment?.submissionFiles.map((file) => (
                                                    <div key={file.id} className="flex items-center justify-between rounded border p-2">
                                                        <span className="truncate">{file.fileName}</span>
                                                        <div className="flex gap-2">
                                                            <Button size="sm" variant="outline" onClick={() => openDocument(file.downloadUrl, { mode: 'view', contentType: 'application/pdf' })}>View</Button>
                                                            <Button size="sm" variant="outline" onClick={() => openDocument(file.downloadUrl, { mode: 'download', defaultFileName: file.fileName })}>Download</Button>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                        <Button
                                            onClick={() => submitMutation.mutate()}
                                            disabled={!selectedAssignmentId || submitMutation.isPending}
                                        >
                                            Gorevi Teslim Et
                                        </Button>
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

export default UserTasksPage;
