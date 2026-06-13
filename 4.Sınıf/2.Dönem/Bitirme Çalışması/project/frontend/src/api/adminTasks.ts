import { apiClient } from './client';

export type TaskAssignmentStatus =
    | 'ASSIGNED'
    | 'SUBMITTED'
    | 'APPROVED'
    | 'REJECTED'
    | 'REVISION_REQUESTED'
    | 'DONE'
    | 'FAILED';

export type TaskAssignMode = 'ALL' | 'USER';
export type TaskReviewDecision = 'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED';
export type TaskScope = 'MAIN' | 'USER';
export type TaskGraphReviewStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REVISION_REQUESTED';

export interface TaskUserSummary {
    userId: number;
    email: string;
    firstName?: string | null;
    lastName?: string | null;
}

export interface TaskFileResponse {
    id: number;
    fileName: string;
    contentType: string;
    fileSizeBytes: number;
    uploadedAt: string;
    downloadUrl: string;
}

export interface TaskAssignmentListItemResponse {
    assignmentId: number;
    taskId: number;
    projectId: number;
    taskTitle: string;
    dueDate?: string | null;
    status: TaskAssignmentStatus;
    assignedAt?: string | null;
    submittedAt?: string | null;
    reviewedAt?: string | null;
    assignee: TaskUserSummary;
}

export interface TaskAssignmentDetailResponse {
    assignmentId: number;
    taskId: number;
    projectId: number;
    taskTitle: string;
    taskDescription: string;
    dueDate?: string | null;
    status: TaskAssignmentStatus;
    assignedAt?: string | null;
    submittedAt?: string | null;
    reviewedAt?: string | null;
    reviewNote?: string | null;
    textAnswer?: string | null;
    assignee: TaskUserSummary;
    createdBy: TaskUserSummary;
    taskAttachments: TaskFileResponse[];
    submissionFiles: TaskFileResponse[];
}

export interface TaskCreateRequest {
    title: string;
    description: string;
    dueDate?: string | null;
    assignMode?: TaskAssignMode;
    assigneeUserId?: number;
    scope?: TaskScope;
    assignedToUserId?: number;
}

export interface TaskCreateResponse {
    id: number;
    projectId: number;
    title: string;
    description: string;
    dueDate?: string | null;
    createdAt: string;
    createdBy: TaskUserSummary;
    assignments: TaskAssignmentListItemResponse[];
}

export interface TaskReviewRequest {
    decision: TaskReviewDecision;
    note?: string;
}

export interface AdminTaskPatchRequest {
    title?: string;
    description?: string;
    dueDate?: string | null;
    scope?: TaskScope;
    assignedToUserId?: number;
    status?: TaskGraphReviewStatus;
}

export interface AdminTaskReviewRequest {
    status: TaskGraphReviewStatus;
    reviewNote?: string;
    assignedToUserId?: number;
}

export interface AdminUserSummary {
    id: string;
    email: string;
    role: string;
    enabled: boolean;
    preferredColor?: string | null;
    firstName?: string | null;
    lastName?: string | null;
}

export interface SubmissionOverviewLite {
    userId?: string | number | null;
    userEmail?: string | null;
    userFirstName?: string | null;
    userLastName?: string | null;
    status?: string | null;
}

const normalizeList = <T>(data: unknown): T[] => {
    if (Array.isArray(data)) return data as T[];
    if (data && typeof data === 'object' && Array.isArray((data as { content?: unknown[] }).content)) {
        return (data as { content: T[] }).content;
    }
    if (data && typeof data === 'object' && Array.isArray((data as { items?: unknown[] }).items)) {
        return (data as { items: T[] }).items;
    }
    if (data && typeof data === 'object' && Array.isArray((data as { users?: unknown[] }).users)) {
        return (data as { users: T[] }).users;
    }
    return [];
};

export const listProjectAssignments = async (projectId: string) => {
    const { data } = await apiClient.get<TaskAssignmentListItemResponse[]>(
        `/admin/projects/${projectId}/task-assignments`
    );
    return normalizeList<TaskAssignmentListItemResponse>(data);
};

export const listUserAssignmentsForAdmin = async (userId: string) => {
    const { data } = await apiClient.get<TaskAssignmentListItemResponse[]>(
        `/admin/users/${userId}/task-assignments`
    );
    return normalizeList<TaskAssignmentListItemResponse>(data);
};

export const getTaskAssignmentForAdmin = async (assignmentId: string) => {
    const { data } = await apiClient.get<TaskAssignmentDetailResponse>(
        `/admin/task-assignments/${assignmentId}`
    );
    return data;
};

export const createProjectTask = async (projectId: string, payload: TaskCreateRequest) => {
    const { data } = await apiClient.post<TaskCreateResponse>(
        `/admin/projects/${projectId}/tasks`,
        payload
    );
    return data;
};

export const uploadTaskAttachments = async (taskId: string | number, files: File[]) => {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    const { data } = await apiClient.post<TaskFileResponse[]>(
        `/admin/tasks/${taskId}/attachments`,
        formData
    );
    return normalizeList<TaskFileResponse>(data);
};

export const reviewTaskAssignment = async (assignmentId: string | number, payload: TaskReviewRequest) => {
    const { data } = await apiClient.post<TaskAssignmentDetailResponse>(
        `/admin/task-assignments/${assignmentId}/review`,
        payload
    );
    return data;
};

export const patchAdminTask = async (taskId: string | number, payload: AdminTaskPatchRequest) => {
    const { data } = await apiClient.patch(`/admin/tasks/${taskId}`, payload);
    return data;
};

export const deleteAdminTask = async (taskId: string | number) => {
    await apiClient.delete(`/admin/tasks/${taskId}`);
};

export const reviewAdminTask = async (taskId: string | number, payload: AdminTaskReviewRequest) => {
    const { data } = await apiClient.post(`/admin/tasks/${taskId}/review`, payload);
    return data;
};

export const listAdminUsers = async (size = 200): Promise<AdminUserSummary[]> => {
    const safeSize = Math.min(Math.max(Number(size) || 100, 1), 100);

    let data: { content?: unknown[] } | unknown[] | { items?: unknown[] } | { users?: unknown[] };
    try {
        const response = await apiClient.get<{ content?: unknown[] } | unknown[]>(
            '/admin/users',
            { params: { page: 0, size: safeSize } }
        );
        data = response.data;
    } catch (firstError) {
        try {
            const response = await apiClient.get<{ content?: unknown[] } | unknown[]>(
                '/admin/users',
                { params: { page: 1, size: safeSize } }
            );
            data = response.data;
        } catch {
            const response = await apiClient.get<{ content?: unknown[] } | unknown[]>(
                '/admin/users'
            );
            data = response.data;
        }
    }

    const rows = normalizeList<Record<string, unknown>>(data);
    const normalized = rows
        .map((row) => {
            const nestedUser = (row.user && typeof row.user === 'object') ? (row.user as Record<string, unknown>) : null;
            const idRaw = row.id ?? row.userId ?? nestedUser?.id ?? nestedUser?.userId;
            const id = String(idRaw ?? '').trim();
            if (!id) return null;

            const email = String(
                row.email
                ?? row.mail
                ?? row.userEmail
                ?? row.username
                ?? nestedUser?.email
                ?? nestedUser?.mail
                ?? `user-${id}`
            ).trim();

            const roleCandidates: string[] = [];
            const pushRole = (value: unknown) => {
                if (typeof value === 'string' && value.trim()) roleCandidates.push(value);
            };

            pushRole(row.role);
            pushRole(row.userRole);
            pushRole(row.authority);

            if (Array.isArray(row.roles)) {
                row.roles.forEach((entry) => {
                    if (typeof entry === 'string') {
                        roleCandidates.push(entry);
                    } else if (entry && typeof entry === 'object') {
                        const roleObj = entry as Record<string, unknown>;
                        pushRole(roleObj.name);
                        pushRole(roleObj.role);
                        pushRole(roleObj.authority);
                    }
                });
            }

            const normalizedRole = roleCandidates
                .map((role) => role.toUpperCase().replace('ROLE_', ''))
                .find(Boolean) ?? '';

            const enabledRaw = row.enabled ?? row.isEnabled ?? row.active;
            const enabled = typeof enabledRaw === 'boolean' ? enabledRaw : true;

            return {
                id,
                email,
                role: normalizedRole || 'USER',
                enabled,
                preferredColor: typeof row.preferredColor === 'string'
                    ? row.preferredColor
                    : (typeof row.color === 'string' ? row.color : null),
                firstName: typeof (row.firstName ?? nestedUser?.firstName) === 'string'
                    ? String(row.firstName ?? nestedUser?.firstName)
                    : null,
                lastName: typeof (row.lastName ?? nestedUser?.lastName) === 'string'
                    ? String(row.lastName ?? nestedUser?.lastName)
                    : null,
            } as AdminUserSummary;
        })
        .filter((item): item is AdminUserSummary => item !== null);

    return normalized.filter((user) => user.role !== 'ADMIN');
};

export const setAdminUserEnabled = async (userId: string | number, enabled: boolean) => {
    const id = String(userId);
    const attempts: Array<() => Promise<unknown>> = [
        () => apiClient.patch(`/admin/users/${id}`, { enabled }),
        () => apiClient.patch(`/admin/users/${id}`, { isEnabled: enabled }),
        () => apiClient.patch(`/admin/users/${id}`, { active: enabled }),
        () => apiClient.patch(`/admin/users/${id}/status`, { enabled }),
        () => apiClient.patch(`/admin/users/${id}/status`, { isEnabled: enabled }),
        () => apiClient.post(`/admin/users/${id}/${enabled ? 'activate' : 'deactivate'}`),
        () => apiClient.post(`/admin/users/${id}/${enabled ? 'enable' : 'disable'}`),
        () => apiClient.put(`/admin/users/${id}/enabled`, { enabled }),
    ];

    let firstError: unknown;
    for (const attempt of attempts) {
        try {
            await attempt();
            return;
        } catch (error) {
            if (!firstError) firstError = error;
        }
    }

    throw firstError;
};

export const setAdminUserColor = async (userId: string | number, color: string | null) => {
    const id = String(userId);
    const payload = { color };
    const { data } = await apiClient.patch(`/admin/users/${id}/color`, payload);
    return data;
};

export const deleteAdminUser = async (userId: string | number) => {
    const id = String(userId);
    try {
        await apiClient.delete(`/admin/users/${id}`);
        return;
    } catch (firstError) {
        try {
            await apiClient.delete(`/admin/users/${id}/delete`);
            return;
        } catch {
            throw firstError;
        }
    }
};

export const listApprovedMembersByProject = async (projectId: string) => {
    const { data } = await apiClient.get<{ content?: SubmissionOverviewLite[] } | SubmissionOverviewLite[]>(
        '/admin/submissions',
        { params: { postingId: projectId, status: 'APPROVED', page: 0, size: 200 } }
    );
    const rows = normalizeList<SubmissionOverviewLite>(data);
    const map = new Map<string, TaskUserSummary>();
    rows.forEach((row) => {
        const userId = row.userId;
        if (userId === null || userId === undefined) return;
        const normalizedUserId = Number(String(userId));
        if (!Number.isFinite(normalizedUserId)) return;
        const idKey = String(normalizedUserId);
        if (!map.has(idKey)) {
            map.set(idKey, {
                userId: normalizedUserId,
                email: row.userEmail ?? '',
                firstName: row.userFirstName ?? null,
                lastName: row.userLastName ?? null,
            });
        }
    });
    return Array.from(map.values());
};
