import { apiClient } from './client';
import { appendJsonData } from './multipart';
import { TaskAssignmentDetailResponse, TaskAssignmentListItemResponse } from './adminTasks';

const normalizeList = <T>(data: unknown): T[] => {
    if (Array.isArray(data)) return data as T[];
    if (data && typeof data === 'object' && Array.isArray((data as { content?: unknown[] }).content)) {
        return (data as { content: T[] }).content;
    }
    return [];
};

export const listMyTaskAssignments = async () => {
    const { data } = await apiClient.get<TaskAssignmentListItemResponse[]>('/me/task-assignments');
    return normalizeList<TaskAssignmentListItemResponse>(data);
};

export const getMyTaskAssignment = async (assignmentId: string | number) => {
    const { data } = await apiClient.get<TaskAssignmentDetailResponse>(
        `/me/task-assignments/${assignmentId}`
    );
    return data;
};

export const submitMyTaskAssignment = async (
    assignmentId: string | number,
    payload: { textAnswer?: string; files?: File[] }
) => {
    const formData = new FormData();
    appendJsonData(formData, { textAnswer: payload.textAnswer ?? '' });
    (payload.files ?? []).forEach((file) => formData.append('files', file));
    const { data } = await apiClient.post<TaskAssignmentDetailResponse>(
        `/me/task-assignments/${assignmentId}/submit`,
        formData
    );
    return data;
};
