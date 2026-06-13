import { apiClient } from './client';
import {
    getTaskAssignmentForAdmin,
    TaskAssignmentDetailResponse,
    TaskAssignmentListItemResponse,
    TaskAssignmentStatus,
} from './adminTasks';

export interface TimelineStats {
    assigned: number;
    submitted: number;
    approved: number;
    rejected: number;
    pending: number;
}

export interface ProjectTimelineEvent {
    id: string;
    taskId: number;
    taskTitle: string;
    actor: string;
    eventAt: string | null;
    assignType: 'ALL' | 'USER';
    stats: TimelineStats;
    assignmentIds: number[];
    completedUsers: string[];
    waitingUsers: string[];
    latestAssignmentId: number;
}

export interface UserTimelineEvent {
    id: string;
    assignmentId: number;
    taskTitle: string;
    status: TaskAssignmentStatus;
    assignedAt?: string | null;
    submittedAt?: string | null;
    reviewedAt?: string | null;
}

const normalizeList = <T>(data: unknown): T[] => {
    if (Array.isArray(data)) return data as T[];
    if (data && typeof data === 'object' && Array.isArray((data as { content?: unknown[] }).content)) {
        return (data as { content: T[] }).content;
    }
    return [];
};

const formatUserName = (item: TaskAssignmentListItemResponse) => {
    const firstName = item.assignee?.firstName ?? '';
    const lastName = item.assignee?.lastName ?? '';
    const fullName = `${firstName} ${lastName}`.trim();
    return fullName || item.assignee?.email || `User #${String(item.assignee?.userId ?? '-')}`;
};

const getLatestDate = (rows: TaskAssignmentListItemResponse[]) => {
    const sorted = [...rows].sort((a, b) => {
        const first = new Date(b.reviewedAt || b.submittedAt || b.assignedAt || 0).getTime();
        const second = new Date(a.reviewedAt || a.submittedAt || a.assignedAt || 0).getTime();
        return first - second;
    });
    return sorted[0] || rows[0];
};

export const listProjectTimeline = async (projectId: string): Promise<ProjectTimelineEvent[]> => {
    const { data } = await apiClient.get<TaskAssignmentListItemResponse[]>(`/admin/projects/${projectId}/task-assignments`);
    const rows = normalizeList<TaskAssignmentListItemResponse>(data);

    const grouped = new Map<number, TaskAssignmentListItemResponse[]>();
    rows.forEach((item) => {
        const current = grouped.get(item.taskId) ?? [];
        current.push(item);
        grouped.set(item.taskId, current);
    });

    const events = Array.from(grouped.entries()).map(([taskId, items]) => {
        const latest = getLatestDate(items);
        const approvedUsers = items.filter((item) => item.status === 'APPROVED' || item.status === 'DONE').map(formatUserName);
        const waitingUsers = items
            .filter((item) => item.status === 'ASSIGNED' || item.status === 'SUBMITTED' || item.status === 'REVISION_REQUESTED')
            .map(formatUserName);

        const stats: TimelineStats = {
            assigned: items.filter((item) => item.status === 'ASSIGNED').length,
            submitted: items.filter((item) => item.status === 'SUBMITTED').length,
            approved: items.filter((item) => item.status === 'APPROVED' || item.status === 'DONE').length,
            rejected: items.filter((item) => item.status === 'REJECTED' || item.status === 'FAILED').length,
            pending: waitingUsers.length,
        };

        return {
            id: `task-${String(taskId)}`,
            taskId,
            taskTitle: latest.taskTitle,
            actor: 'Admin',
            eventAt: latest.reviewedAt || latest.submittedAt || latest.assignedAt || null,
            assignType: items.length > 1 ? ('ALL' as const) : ('USER' as const),
            stats,
            assignmentIds: items.map((item) => item.assignmentId),
            completedUsers: approvedUsers,
            waitingUsers,
            latestAssignmentId: latest.assignmentId,
        };
    });

    return events.sort((a, b) => {
        const first = a.eventAt ? new Date(a.eventAt).getTime() : 0;
        const second = b.eventAt ? new Date(b.eventAt).getTime() : 0;
        return second - first;
    });
};

export const listUserTimeline = async (userId: string): Promise<UserTimelineEvent[]> => {
    const { data } = await apiClient.get<TaskAssignmentListItemResponse[]>(`/admin/users/${userId}/task-assignments`);
    const rows = normalizeList<TaskAssignmentListItemResponse>(data);
    return rows
        .map((item) => ({
            id: `assignment-${String(item.assignmentId)}`,
            assignmentId: item.assignmentId,
            taskTitle: item.taskTitle,
            status: item.status,
            assignedAt: item.assignedAt,
            submittedAt: item.submittedAt,
            reviewedAt: item.reviewedAt,
        }))
        .sort((a, b) => {
            const first = new Date(a.reviewedAt || a.submittedAt || a.assignedAt || 0).getTime();
            const second = new Date(b.reviewedAt || b.submittedAt || b.assignedAt || 0).getTime();
            return second - first;
        });
};

export const getTimelineAssignmentDetail = async (assignmentId: string | number): Promise<TaskAssignmentDetailResponse> => {
    return getTaskAssignmentForAdmin(String(assignmentId));
};

export type TaskGraphNodeStatus = 'PENDING' | 'SUBMITTED' | 'SUCCESS' | 'FAILED' | 'REVISION_REQUESTED';

export interface TaskGraphNode {
    nodeId: string;
    taskId: string;
    branchKey: string;
    createdAt?: string | null;
    title: string;
    description?: string | null;
    status: TaskGraphNodeStatus;
    createdByUserId?: number | null;
    assignedToUserId?: number | null;
    assignedToUserColor?: string | null;
}

export interface TaskGraphEdge {
    edgeId: string;
    fromTaskId: string;
    toTaskId: string;
}

export interface TaskGraphBranch {
    branchKey: string;
    branchName: string;
    ownerUserId?: number | null;
    ownerUserColor?: string | null;
}

export interface TaskGraphResponse {
    projectId: number;
    nodes: TaskGraphNode[];
    edges: TaskGraphEdge[];
    branches: TaskGraphBranch[];
}

const normalizeStringId = (value: unknown): string | null => {
    if (value === null || value === undefined) return null;
    if (typeof value === 'string' && value.trim()) return value;
    if (typeof value === 'number' && Number.isFinite(value)) return String(value);
    return null;
};

const normalizeNumber = (value: unknown): number | null => {
    if (value === null || value === undefined) return null;
    if (typeof value === 'number' && Number.isFinite(value)) return value;
    if (typeof value === 'string' && value.trim()) {
        const parsed = Number(value);
        return Number.isFinite(parsed) ? parsed : null;
    }
    return null;
};

const normalizeStatus = (value: unknown): TaskGraphNodeStatus => {
    const normalized = String(value ?? '').toUpperCase();
    if (normalized === 'SUCCESS' || normalized === 'FAILED' || normalized === 'REVISION_REQUESTED' || normalized === 'SUBMITTED') {
        return normalized;
    }
    return 'PENDING';
};

const normalizeGraph = (raw: unknown): TaskGraphResponse => {
    const data = (raw ?? {}) as Record<string, unknown>;

    const branches: TaskGraphBranch[] = (Array.isArray(data.branches) ? data.branches : [])
        .flatMap((item) => {
            const row = item as Record<string, unknown>;
            const branchKey = String(row.branchKey ?? row.branchId ?? '').trim();
            if (!branchKey) return [];
            return [{
                branchKey,
                branchName: String(row.branchName ?? row.label ?? branchKey),
                ownerUserId: normalizeNumber(row.ownerUserId ?? row.userId),
                ownerUserColor: typeof row.ownerUserColor === 'string' ? row.ownerUserColor : null,
            } satisfies TaskGraphBranch];
        })
        ;

    const nodes: TaskGraphNode[] = (Array.isArray(data.nodes) ? data.nodes : [])
        .flatMap((item) => {
            const row = item as Record<string, unknown>;
            const taskId = normalizeStringId(row.taskId ?? row.nodeId);
            if (!taskId) return [];
            const branchKey = String(row.branchKey ?? row.branchId ?? 'MAIN').trim() || 'MAIN';
            const assignedToUserId = normalizeNumber(row.assignedToUserId);
            const rawNodeId = normalizeStringId(row.nodeId);
            const nodeId = rawNodeId ?? `${taskId}:${branchKey}:${assignedToUserId ?? 'NA'}`;
            return [{
                nodeId,
                taskId,
                branchKey,
                createdAt: typeof row.createdAt === 'string' ? row.createdAt : null,
                title: String(row.title ?? '').trim() || `Task #${taskId}`,
                description: typeof row.description === 'string' ? row.description : null,
                status: normalizeStatus(row.status),
                createdByUserId: normalizeNumber(row.createdByUserId),
                assignedToUserId,
                assignedToUserColor: typeof row.assignedToUserColor === 'string' ? row.assignedToUserColor : null,
            } satisfies TaskGraphNode];
        })
        .sort((first, second) => {
            const firstTime = new Date(first.createdAt ?? 0).getTime();
            const secondTime = new Date(second.createdAt ?? 0).getTime();
            if (secondTime !== firstTime) return secondTime - firstTime;
            return first.taskId.localeCompare(second.taskId, 'tr');
        });

    const edges: TaskGraphEdge[] = (Array.isArray(data.edges) ? data.edges : [])
        .flatMap((item) => {
            const row = item as Record<string, unknown>;
            const fromTaskId = normalizeStringId(row.fromTaskId ?? row.from);
            const toTaskId = normalizeStringId(row.toTaskId ?? row.to);
            if (!fromTaskId || !toTaskId) return [];
            return [{
                edgeId: `${fromTaskId}->${toTaskId}`,
                fromTaskId,
                toTaskId,
            } satisfies TaskGraphEdge];
        })
        ;

    return {
        projectId: normalizeNumber(data.projectId) ?? 0,
        branches,
        nodes,
        edges,
    };
};

export const getAdminProjectTaskGraph = async (projectId: string | number): Promise<TaskGraphResponse> => {
    const { data } = await apiClient.get(`/admin/projects/${projectId}/task-graph`);
    return normalizeGraph(data);
};

export const getMyTaskGraph = async (projectId?: string | number): Promise<TaskGraphResponse> => {
    const { data } = await apiClient.get('/users/me/task-graph', {
        params: projectId ? { projectId } : undefined,
    });
    return normalizeGraph(data);
};
