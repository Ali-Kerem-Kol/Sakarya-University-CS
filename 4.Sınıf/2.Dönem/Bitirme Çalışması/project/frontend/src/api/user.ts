import { apiClient } from './client';
import { isPdfFile } from './multipart';
import { Application, UserProfile, ProjectDocument, PaginatedArray, ProjectTask, PublicQuestion, StudentQuestion } from '@/types';

// Helper to ensure we always have an array
const normalize = <T>(data: any): PaginatedArray<T> => {
    const array = (Array.isArray(data) ? data : (data?.content ?? [])) as PaginatedArray<T>;
    if (data && !Array.isArray(data) && typeof data === 'object') {
        array.totalElements = data.totalElements;
        array.totalPages = data.totalPages;
        array.size = data.size;
        array.number = data.number;
    }
    return array;
};

export const fetchMySubmissions = async () => {
    const { data } = await apiClient.get<any>('/me/submissions');
    return normalize<Application>(data);
};

export const createSubmission = async (postingId: string) => {
    const { data } = await apiClient.post<Application>('/me/submissions', { postingId });
    return data;
};

export const fetchMyProfile = async () => {
    const { data } = await apiClient.get<UserProfile>('/me/profile');
    return data;
};

export const updateMyProfile = async (data: Partial<UserProfile>) => {
    const { data: response } = await apiClient.put('/me/profile', data);
    return response;
};

export const fetchMyDocuments = async () => {
    const { data } = await apiClient.get<any>('/users/me/documents');
    return normalize<ProjectDocument>(data);
};


export const uploadCv = async (file: File) => {
    if (!isPdfFile(file)) {
        throw new Error('INVALID_FILE_TYPE');
    }
    const formData = new FormData();
    formData.append('file', file);
    const { data } = await apiClient.post('/users/me/documents/cv', formData);
    return data;
};


export const updateMyPassword = async (data: { oldPassword: string, newPassword: string }) => {
    const { data: response } = await apiClient.put('/me/password', data);
    return response;
};

export const changeEmail = async (email: string) => {
    const { data: response } = await apiClient.put('/me/email', { email });
    return response;
};

// --- TASKS (Student) ---
export const fetchMyTasks = async () => {
    const { data } = await apiClient.get<any>('/me/tasks');
    return normalize<ProjectTask>(data);
};

export const updateTaskStatus = async (taskId: string, status: string) => {
    const { data } = await apiClient.put(`/me/tasks/${taskId}/status`, { status });
    return data;
};

// --- Q&A (Student) ---
export const fetchPostingPublicQa = async (postingId: string) => {
    const { data } = await apiClient.get<PublicQuestion[]>(`/postings/${postingId}/qa`);
    return Array.isArray(data) ? data : [];
};

export const askQuestion = async (postingId: string, questionText: string) => {
    const { data } = await apiClient.post<StudentQuestion>('/questions', { postingId, questionText });
    return data;
};

export const fetchMyQuestions = async () => {
    const { data } = await apiClient.get<StudentQuestion[]>('/questions/my');
    return Array.isArray(data) ? data : [];
};
