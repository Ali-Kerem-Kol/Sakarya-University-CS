import { apiClient } from './client';
import { Posting, PostingCategory, PaginatedArray, Announcement } from '@/types';

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

export const fetchPublicPostings = async (category?: PostingCategory) => {
    const params = category ? { category } : {};
    const { data } = await apiClient.get<any>('/public/postings', { params });
    return normalize<Posting>(data);
};

export const fetchPostingDetail = async (id: string) => {
    const response = await apiClient.get<Posting>(`/public/postings/${id}`);
    return response.data;
};

export const fetchPublicAnnouncements = async () => {
    const { data } = await apiClient.get<any>('/public/announcements');
    return normalize<Announcement>(data);
};
