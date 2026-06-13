import React, { useMemo, useState } from 'react';
import { TaskGraphBranch, TaskGraphEdge, TaskGraphNode } from '@/api/adminTimeline';
import { CommitEdge } from '@/components/task-graph/CommitEdge';
import { CommitNode } from '@/components/task-graph/CommitNode';
import { getBranchLineColor } from '@/lib/branchColor';
import { buildGraphLayout } from '@/lib/graphLayout';
import { getTaskGraphStatusLabel } from '@/lib/taskGraphStatus';
import { normalizeHexColor, resolveUserColor } from '@/lib/userColor';
import { cn } from '@/lib/utils';

interface TaskCommitGraphProps {
    nodes: TaskGraphNode[];
    edges: TaskGraphEdge[];
    branches: TaskGraphBranch[];
    selectedNodeId?: string;
    onSelectNode: (nodeId: string) => void;
    resolveAssigneeLabel?: (assignedToUserId?: string | number | null) => string | null | undefined;
    resolveAssigneeColor?: (assignedToUserId?: string | number | null, node?: TaskGraphNode) => string | null | undefined;
    resolveDisplayStatus?: (node: TaskGraphNode) => TaskGraphNode['status'];
    nodeColorMode?: 'all-users' | 'highlight-only' | 'monochrome';
    monochromeMode?: boolean;
    highlightedUserId?: string | number | null;
    highlightedTaskIds?: Array<string | number>;
    scrollable?: boolean;
}

interface HoverState {
    node: TaskGraphNode;
    branchName: string;
    mouseX: number;
    mouseY: number;
}

const TOOLTIP_WIDTH = 330;
const normalizeBranchKey = (value: unknown) => String(value ?? 'MAIN').trim().toUpperCase() || 'MAIN';
const normalizeUserId = (value: unknown) => {
    if (value === null || value === undefined) return null;
    const normalized = String(value).trim();
    return normalized ? normalized : null;
};

const formatDateTime = (value?: string | null) => {
    if (!value) return '-';
    return new Date(value).toLocaleString('tr-TR');
};

const getStatusMarkerColor = (status: TaskGraphNode['status']) => {
    switch (status) {
        case 'SUCCESS':
            return '#16A34A';
        case 'FAILED':
            return '#DC2626';
        case 'REVISION_REQUESTED':
            return '#D97706';
        case 'SUBMITTED':
            return '#0284C7';
        default:
            return '#64748B';
    }
};

export const TaskCommitGraph: React.FC<TaskCommitGraphProps> = ({
    nodes,
    edges,
    branches,
    selectedNodeId,
    onSelectNode,
    resolveAssigneeLabel,
    resolveAssigneeColor,
    resolveDisplayStatus,
    nodeColorMode = 'all-users',
    monochromeMode = false,
    highlightedUserId,
    highlightedTaskIds = [],
    scrollable = false,
}) => {
    const [hover, setHover] = useState<HoverState | null>(null);
    const highlightedUser = normalizeUserId(highlightedUserId);
    const highlightedTaskSet = useMemo(() => new Set(highlightedTaskIds.map((value) => String(value))), [highlightedTaskIds]);
    const showStatusMarkers = highlightedUser !== null || highlightedTaskSet.size > 0;
    const monoLineColor = '#8FA0B6';

    const branchOwnerColorByKey = useMemo(() => {
        const map = new Map<string, string>();
        branches.forEach((branch) => {
            const key = normalizeBranchKey(branch.branchKey);
            const normalized = normalizeHexColor(branch.ownerUserColor);
            if (normalized) {
                map.set(key, normalized);
            }
        });
        return map;
    }, [branches]);

    const resolveNodeUserColor = (_node: TaskGraphNode) => (
        nodeColorMode === 'monochrome' ? '#CBD5E1' : '#CBD5E1'
    );

    const focusLineColor = useMemo(() => {
        if (!highlightedUser) return '#0EA5E9';
        const color = resolveAssigneeColor?.(highlightedUser) ?? resolveUserColor(highlightedUser, null);
        return normalizeHexColor(color) ?? '#0EA5E9';
    }, [highlightedUser, resolveAssigneeColor]);

    const isHighlightedNode = (node: TaskGraphNode) => {
        const byAssignee = highlightedUser !== null && normalizeUserId(node.assignedToUserId) === highlightedUser;
        const byTask = highlightedTaskSet.has(String(node.taskId));
        return byAssignee || byTask;
    };

    const resolveLineColor = (branchKey: string) => {
        if (monochromeMode) return monoLineColor;
        if (normalizeBranchKey(branchKey) === 'MAIN') return '#64748B';
        return branchOwnerColorByKey.get(normalizeBranchKey(branchKey)) ?? getBranchLineColor(branchKey);
    };

    const layout = useMemo(() => buildGraphLayout(nodes, edges, branches, { newestOnTop: true }), [nodes, edges, branches]);
    const mainX = useMemo(
        () => layout.lanes.find((lane) => normalizeBranchKey(lane.branchKey) === 'MAIN')?.x ?? layout.centerX,
        [layout.centerX, layout.lanes],
    );

    const branchNameByKey = useMemo(() => {
        const map = new Map<string, string>();
        layout.lanes.forEach((lane) => map.set(normalizeBranchKey(lane.branchKey), lane.branchName));
        return map;
    }, [layout.lanes]);

    const commitsByBranch = useMemo(() => {
        const map = new Map<string, Array<{ x: number; y: number; node: TaskGraphNode }>>();
        layout.commits.forEach((commit) => {
            const key = normalizeBranchKey(commit.node.branchKey);
            const list = map.get(key) ?? [];
            list.push({ x: commit.x, y: commit.y, node: commit.node });
            map.set(key, list);
        });
        map.forEach((list) => list.sort((a, b) => a.y - b.y));
        return map;
    }, [layout.commits]);

    const renderEdges = useMemo(() => {
        const next: Array<{
            edgeId: string;
            kind: 'vertical' | 'fork';
            fromX: number;
            fromY: number;
            toX: number;
            toY: number;
            color: string;
        }> = [];
        const userLaneSegments: Array<{ branchKey: string; x: number; y1: number; y2: number }> = [];
        const mainLaneSegments: Array<{ x: number; y1: number; y2: number }> = [];

        const mainCommits = [...(commitsByBranch.get('MAIN') ?? [])].sort((a, b) => b.y - a.y);
        for (let index = 0; index < mainCommits.length - 1; index += 1) {
            const from = mainCommits[index];
            const to = mainCommits[index + 1];
            mainLaneSegments.push({
                x: from.x,
                y1: Math.min(from.y, to.y),
                y2: Math.max(from.y, to.y),
            });
        }

        const resolveAnchorMain = (commitY: number) => {
            if (mainCommits.length === 0) return undefined;
            const olderOrEqual = mainCommits
                .filter((main) => main.y >= commitY)
                .sort((a, b) => a.y - b.y)[0];
            if (olderOrEqual) return olderOrEqual;
            return undefined;
        };

        commitsByBranch.forEach((list, branchKey) => {
            if (branchKey === 'MAIN' || list.length === 0) return;
            const branchCommits = [...list].sort((a, b) => b.y - a.y);
            const episodes: Array<{
                anchorMainY: number | null;
                commits: Array<{ x: number; y: number; node: TaskGraphNode }>;
            }> = [];

            branchCommits.forEach((commit) => {
                const anchorMainY = resolveAnchorMain(commit.y)?.y ?? null;
                const current = episodes[episodes.length - 1];
                if (!current || current.anchorMainY !== anchorMainY) {
                    episodes.push({ anchorMainY, commits: [commit] });
                    return;
                }
                current.commits.push(commit);
            });

            episodes.forEach((episode, episodeIndex) => {
                const oldestCommit = episode.commits[0];
                const newestCommit = episode.commits[episode.commits.length - 1];

                for (let index = 0; index < episode.commits.length - 1; index += 1) {
                    const from = episode.commits[index];
                    const to = episode.commits[index + 1];
                    next.push({
                        edgeId: `vertical-${branchKey}-${episodeIndex}-${index}`,
                        kind: 'vertical',
                        fromX: from.x,
                        fromY: from.y,
                        toX: to.x,
                        toY: to.y,
                        color: resolveLineColor(branchKey),
                    });
                }

                userLaneSegments.push({
                    branchKey,
                    x: oldestCommit.x,
                    y1: Math.min(oldestCommit.y, newestCommit.y),
                    y2: Math.max(oldestCommit.y, newestCommit.y),
                });

                const baseMain = episode.anchorMainY == null
                    ? undefined
                    : mainCommits.find((main) => main.y === episode.anchorMainY);
                if (baseMain) {
                    next.push({
                        edgeId: `fork-MAIN-${branchKey}-${episodeIndex}`,
                        kind: 'fork',
                        fromX: mainX,
                        fromY: baseMain.y,
                        toX: oldestCommit.x,
                        toY: oldestCommit.y,
                        color: resolveLineColor(branchKey),
                    });
                }

                const mergeMain = mainCommits
                    .filter((main) => main.y < newestCommit.y)
                    .sort((a, b) => b.y - a.y)[0];
                if (mergeMain) {
                    next.push({
                        edgeId: `merge-${branchKey}-MAIN-${episodeIndex}`,
                        kind: 'fork',
                        fromX: newestCommit.x,
                        fromY: newestCommit.y,
                        toX: mainX,
                        toY: mergeMain.y,
                        color: resolveLineColor(branchKey),
                    });
                }
            });
        });

        return { edges: next, userLaneSegments, mainLaneSegments };
    }, [branchOwnerColorByKey, commitsByBranch, mainX, monochromeMode]);

    const focusTrailSegments = useMemo(() => {
        if (!highlightedUser && highlightedTaskSet.size === 0) return [];
        const highlightedCommits = layout.commits
            .filter((commit) => isHighlightedNode(commit.node))
            .sort((a, b) => {
                if (b.y !== a.y) return b.y - a.y;
                return a.x - b.x;
            });
        const segments: Array<{ id: string; fromX: number; fromY: number; toX: number; toY: number }> = [];
        for (let index = 0; index < highlightedCommits.length - 1; index += 1) {
            const from = highlightedCommits[index];
            const to = highlightedCommits[index + 1];
            segments.push({
                id: `focus-${from.node.nodeId}-${to.node.nodeId}`,
                fromX: from.x,
                fromY: from.y,
                toX: to.x,
                toY: to.y,
            });
        }
        return segments;
    }, [highlightedTaskSet, highlightedUser, layout.commits]);

    const hoveredAssignee = hover?.node
        ? resolveAssigneeLabel?.(hover.node.assignedToUserId)
            || (hover.node.assignedToUserId != null ? `User #${String(hover.node.assignedToUserId)}` : null)
        : null;

    const tooltipStyle = hover
        ? {
            left: Math.max(12, Math.min(hover.mouseX + 14, (window.innerWidth || 1280) - TOOLTIP_WIDTH - 12)),
            top: Math.max(12, hover.mouseY + 14),
        }
        : undefined;

    return (
        <div className="relative rounded-xl border border-border/70 bg-gradient-to-b from-card to-muted/35 p-4 dark:from-slate-900/90 dark:to-slate-800/80">
            <div className={cn('relative overflow-x-auto', scrollable && 'max-h-[68vh] overflow-auto')}>
                <div className="relative" style={{ width: Math.max(layout.width, 520), height: layout.height }}>
                    <svg className="absolute left-0 top-0" width={Math.max(layout.width, 520)} height={layout.height}>
                        {layout.lanes.map((lane) => (
                            (() => {
                                const branchKey = normalizeBranchKey(lane.branchKey);
                                if (branchKey === 'MAIN') {
                                    return renderEdges.mainLaneSegments.map((segment, index) => (
                                        <line
                                            key={`lane-${branchKey}-${index}`}
                                            x1={segment.x}
                                            y1={segment.y1}
                                            x2={segment.x}
                                            y2={segment.y2}
                                            stroke={resolveLineColor(branchKey)}
                                            strokeWidth={2.2}
                                            opacity={0.95}
                                        />
                                    ));
                                }

                                return renderEdges.userLaneSegments
                                    .filter((segment) => segment.branchKey === branchKey)
                                    .map((segment, index) => (
                                        <line
                                            key={`lane-${branchKey}-${index}`}
                                            x1={segment.x}
                                            y1={segment.y1}
                                            x2={segment.x}
                                            y2={segment.y2}
                                            stroke={resolveLineColor(branchKey)}
                                            strokeWidth={1.9}
                                            opacity={0.9}
                                        />
                                    ));
                            })()
                        ))}

                        {renderEdges.edges.map((edge) => (
                            <CommitEdge
                                key={edge.edgeId}
                                edgeId={edge.edgeId}
                                kind={edge.kind}
                                fromX={edge.fromX}
                                fromY={edge.fromY}
                                toX={edge.toX}
                                toY={edge.toY}
                                color={edge.color}
                            />
                        ))}

                        {focusTrailSegments.map((segment) => (
                            <line
                                key={segment.id}
                                x1={segment.fromX}
                                y1={segment.fromY}
                                x2={segment.toX}
                                y2={segment.toY}
                                stroke={focusLineColor}
                                strokeWidth={3.1}
                                opacity={0.98}
                            />
                        ))}
                    </svg>

                    {layout.commits.map((commit) => (
                        <CommitNode
                            key={commit.node.nodeId}
                            node={commit.node}
                            x={commit.x}
                            y={commit.y}
                            selected={selectedNodeId === commit.node.nodeId}
                            colorOverride={{
                                fill: resolveNodeUserColor(commit.node),
                                stroke: '#334155',
                                strokeWidth: 1.6,
                            }}
                            statusMarkerColor={showStatusMarkers && isHighlightedNode(commit.node)
                                ? getStatusMarkerColor(resolveDisplayStatus?.(commit.node) ?? commit.node.status)
                                : undefined}
                            onClick={() => onSelectNode(commit.node.nodeId)}
                            onHover={(event, node) => {
                                setHover({
                                    node,
                                    branchName: branchNameByKey.get(normalizeBranchKey(node.branchKey)) || node.branchKey,
                                    mouseX: event.clientX,
                                    mouseY: event.clientY,
                                });
                            }}
                            onLeave={() => {
                                setHover((current) => (current?.node.nodeId === commit.node.nodeId ? null : current));
                            }}
                        />
                    ))}
                </div>
            </div>

            {hover && tooltipStyle && (
                <div
                    className="pointer-events-none fixed z-[200] w-[330px] rounded-lg border border-border/80 bg-card/95 p-3 shadow-xl backdrop-blur"
                    style={tooltipStyle}
                >
                    <p className="text-sm font-semibold text-foreground">{hover.node.title}</p>
                    <div className="mt-2 space-y-1 text-xs text-foreground/90">
                        <p><span className="text-muted-foreground">Branch:</span> {hover.branchName}</p>
                        <p><span className="text-muted-foreground">Durum:</span> {getTaskGraphStatusLabel(resolveDisplayStatus?.(hover.node) ?? hover.node.status)}</p>
                        <p><span className="text-muted-foreground">Tarih:</span> {formatDateTime(hover.node.createdAt)}</p>
                        {hoveredAssignee && <p><span className="text-muted-foreground">Atanan:</span> {hoveredAssignee}</p>}
                    </div>
                </div>
            )}
        </div>
    );
};
