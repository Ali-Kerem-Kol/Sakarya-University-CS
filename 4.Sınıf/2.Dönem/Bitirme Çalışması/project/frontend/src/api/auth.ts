import { apiClient } from './client';
import { appendJsonData } from './multipart';
import { AuthResponse } from '@/types';

export const registerUser = async (data: any, cv: File) => {
    const formData = new FormData();
    appendJsonData(formData, data);
    // Part "cv" as File
    formData.append('cv', cv);

    if (import.meta.env.DEV) {
        const parts = Array.from(formData.entries()).map(([key, value]) => {
            if (value instanceof Blob) {
                return { key, kind: 'blob', type: value.type, size: value.size };
            }
            return { key, kind: typeof value, value };
        });
        console.debug('[register] multipart parts', parts);
    }

    const response = await apiClient.post('/auth/register', formData);
    return response.data;
};

export const loginUser = async (credentials: any) => {
    const { data } = await apiClient.post<AuthResponse>('/auth/login', credentials);
    return data;
};

export const verifyUser = async (token: string) => {
    const { data } = await apiClient.get(`/auth/verify?token=${token}`);
    return data;
};

export const forgotPassword = async (email: string) => {
    const { data } = await apiClient.post('/auth/forgot-password', { email });
    return data;
};

export const resetPassword = async (payload: { token: string, newPassword: string }) => {
    const { data } = await apiClient.post('/auth/reset-password', payload);
    return data;
};
