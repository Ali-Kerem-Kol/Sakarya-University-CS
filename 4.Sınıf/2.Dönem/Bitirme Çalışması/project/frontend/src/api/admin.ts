import { apiClient } from './client';
import { appendJsonData, isPdfFile } from './multipart';
import { Posting, PostingCategory, PostingStatus, PaginatedArray, ApplicantOverviewResponse, ProjectDocument, ProjectTask, Announcement, AdminQuestion, AdminQuestionStatusFilter } from '@/types';

// Helper to ensure we always have an array but keep pagination metadata
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

// ------------- POSTINGS -------------
export const fetchAdminPostings = async (params?: { category?: PostingCategory, status?: PostingStatus }) => {
    const { data } = await apiClient.get<any>('/admin/postings', { params });
    return normalize<Posting>(data);
};

export const createPosting = async (data: Partial<Posting>) => {
    const { data: response } = await apiClient.post<Posting>('/admin/postings', data);
    return response;
};

export const updatePosting = async (id: string, data: Partial<Posting>) => {
    const { data: response } = await apiClient.put<Posting>(`/admin/postings/${id}`, data);
    return response;
};

export const deletePosting = async (id: string) => {
    try {
        await apiClient.delete(`/admin/postings/${id}`);
        return;
    } catch (firstError) {
        try {
            await apiClient.delete(`/admin/postings/${id}/delete`);
            return;
        } catch {
            throw firstError;
        }
    }
};

export const publishPosting = async (id: string) => {
    await apiClient.post(`/admin/postings/${id}/publish`);
};

export const closePosting = async (id: string) => {
    await apiClient.post(`/admin/postings/${id}/close`);
};

export const reopenPosting = async (id: string) => {
    await apiClient.post(`/admin/postings/${id}/reopen`);
};

export const uploadPostingAttachments = async (id: string, files: File[]) => {
    const formData = new FormData();
    files.forEach((file) => {
        if (!isPdfFile(file)) {
            throw new Error('INVALID_FILE_TYPE');
        }
        formData.append('files', file);
    });
    await apiClient.post(`/admin/postings/${id}/attachments`, formData);
};

export const deletePostingAttachment = async (postingId: string, attachmentId: string) => {
    await apiClient.delete(`/admin/postings/${postingId}/attachments/${attachmentId}`);
};

// ------------- SUBMISSIONS -------------
export interface SubmissionOverview {
    id: string; // submission id
    userId?: string | number | null;
    userEmail?: string | null;
    userFirstName?: string | null;
    userLastName?: string | null;
    user?: {
        id?: string | number | null;
        firstName?: string | null;
        lastName?: string | null;
        email?: string | null;
    } | null;
    postingId?: string | null;
    postingTitle?: string | null;
    postingCategory?: PostingCategory | string | null;
    status?: string | null; // ApplicationStatus
    createdAt: string;
    cvDownloadUrl?: string;
    // Add other fields as needed based on "snapshot" requirement
}

export const fetchAdminSubmissions = async (params: { category?: string, status?: string, postingStatus?: string, page?: number, size?: number, sort?: string, postingId?: string }): Promise<PaginatedArray<SubmissionOverview>> => {
    const { data } = await apiClient.get<any>('/admin/submissions', { params });
    return normalize<SubmissionOverview>(data);
};

export const fetchSubmissionDetail = async (id: string) => {
    const { data } = await apiClient.get<SubmissionOverview>(`/admin/submissions/${id}`);
    return data;
};

export const acceptSubmission = async (id: string) => {
    await apiClient.post(`/admin/submissions/${id}/accept`);
};

export const rejectSubmission = async (id: string) => {
    await apiClient.post(`/admin/submissions/${id}/reject`);
};

export const removeSubmission = async (id: string) => {
    await apiClient.delete(`/admin/submissions/${id}`);
};

export const addSubmissionByAdmin = async (
    postingId: string,
    payload: { userId: string | number; status?: 'PENDING' | 'APPROVED' | 'REJECTED' }
) => {
    const { data } = await apiClient.post(`/admin/postings/${postingId}/submissions/manual`, payload);
    return data;
};

// ------------- TASKS (Admin) -------------
export const fetchPostingTasks = async (postingId: string) => {
    const { data } = await apiClient.get<any>(`/admin/postings/${postingId}/tasks`);
    return normalize<ProjectTask>(data);
};

export const createTask = async (postingId: string, task: Partial<ProjectTask>) => {
    const { data } = await apiClient.post<ProjectTask>(`/admin/postings/${postingId}/tasks`, task);
    return data;
};

export const updateTask = async (postingId: string, taskId: string, task: Partial<ProjectTask>) => {
    const { data } = await apiClient.put<ProjectTask>(`/admin/postings/${postingId}/tasks/${taskId}`, task);
    return data;
};

export const deleteTask = async (postingId: string, taskId: string) => {
    await apiClient.delete(`/admin/postings/${postingId}/tasks/${taskId}`);
};

export const assignTask = async (postingId: string, taskId: string, userId: string) => {
    await apiClient.post(`/admin/postings/${postingId}/tasks/${taskId}/assign/${userId}`);
};

// ------------- Q&A (Admin) -------------
export const fetchAdminQuestions = async (params?: { postingId?: string; status?: AdminQuestionStatusFilter }) => {
    const { data } = await apiClient.get<AdminQuestion[]>('/admin/questions', { params });
    return Array.isArray(data) ? data : [];
};

export const answerQuestion = async (questionId: string, answerText: string) => {
    const { data } = await apiClient.post(`/admin/questions/${questionId}/answer`, { answerText });
    return data;
};

export const setQuestionPublished = async (questionId: string, published: boolean) => {
    const { data } = await apiClient.post(`/admin/questions/${questionId}/publish`, { published });
    return data;
};

export const deleteQuestion = async (questionId: string) => {
    await apiClient.delete(`/admin/questions/${questionId}`);
};

// ------------- ANNOUNCEMENTS (Admin) -------------
export const fetchAdminAnnouncements = async () => {
    const { data } = await apiClient.get<any>('/admin/announcements');
    return normalize<Announcement>(data);
};

export const createAnnouncement = async (announcement: Partial<Announcement>) => {
    const { data } = await apiClient.post<Announcement>('/admin/announcements', announcement);
    return data;
};

export const updateAnnouncement = async (id: string, announcement: Partial<Announcement>) => {
    const { data } = await apiClient.put<Announcement>(`/admin/announcements/${id}`, announcement);
    return data;
};

export const deleteAnnouncement = async (id: string) => {
    await apiClient.delete(`/admin/announcements/${id}`);
};


// ------------- MAIL -------------
export interface MailPayload {
    subject: string;
    body: string;
    files?: File[];
}

export const createMailJobByCategory = async (category: string, payload: MailPayload) => {
    const formData = new FormData();
    appendJsonData(formData, { subject: payload.subject, body: payload.body });
    if (payload.files) Array.from(payload.files).forEach(f => formData.append('files', f));

    const { data } = await apiClient.post(`/admin/mail/jobs/category/${category}`, formData);
    return data;
};

export const createMailJobByPosting = async (postingId: string, payload: MailPayload) => {
    const formData = new FormData();
    appendJsonData(formData, { subject: payload.subject, body: payload.body });
    if (payload.files) Array.from(payload.files).forEach(f => formData.append('files', f));

    const { data } = await apiClient.post(`/admin/mail/jobs/posting/${postingId}`, formData);
    return data;
};

export const createMailJobAllStudents = async (payload: MailPayload) => {
    const formData = new FormData();
    appendJsonData(formData, { subject: payload.subject, body: payload.body });
    if (payload.files) Array.from(payload.files).forEach(f => formData.append('files', f));

    const { data } = await apiClient.post(`/admin/mail/jobs/all-students`, formData);
    return data;
};

export const fetchMailJob = async (jobId: string) => {
    const { data } = await apiClient.get(`/admin/mail/jobs/${jobId}`);
    return data;
};

export const fetchApplicantOverview = async (params: any): Promise<PaginatedArray<ApplicantOverviewResponse>> => {
    const { data } = await apiClient.get<any>('/admin/overview/applicants', { params });
    return normalize<ApplicantOverviewResponse>(data);
};

// ------------- USERS -------------
export const createAdminUser = async (user: { email: string, password?: string, firstName?: string, lastName?: string }) => {
    const { data } = await apiClient.post('/admin/admin-users', user);
    return data;
};

export const fetchUserDocuments = async (userId: string) => {
    const { data } = await apiClient.get<any>(`/admin/users/${userId}/documents`);
    return normalize<ProjectDocument>(data);
};

export const fetchUserAvailability = async (userId: string) => {
    const { data } = await apiClient.get<any>(`/admin/users/${userId}/availability`);
    return normalize<any>(data);
};
