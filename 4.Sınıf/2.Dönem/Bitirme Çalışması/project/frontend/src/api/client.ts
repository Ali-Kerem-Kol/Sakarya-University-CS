import axios, { AxiosError } from 'axios';

// Use environment variable or default to /api/v1
const API_BASE_URL = import.meta.env.VITE_API_BASE || '/api/v1';

const API_BASE_PATH = (() => {
    try {
        return new URL(API_BASE_URL, window.location.origin).pathname.replace(/\/$/, '');
    } catch {
        return API_BASE_URL.startsWith('/') ? API_BASE_URL.replace(/\/$/, '') : '';
    }
})();

export const apiClient = axios.create({
    baseURL: API_BASE_URL,
});

// Request interceptor to add token
apiClient.interceptors.request.use(
    (config) => {
        if (config.data instanceof FormData) {
            // Let browser set multipart boundary automatically.
            if (config.headers) {
                delete (config.headers as Record<string, unknown>)['Content-Type'];
                delete (config.headers as Record<string, unknown>)['content-type'];
            }
        } else if (config.headers && !('Content-Type' in (config.headers as Record<string, unknown>)) && !('content-type' in (config.headers as Record<string, unknown>))) {
            (config.headers as Record<string, unknown>)['Content-Type'] = 'application/json';
        }

        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor to handle errors
apiClient.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        const status = error.response?.status;

        // Handle 401 Unauthorized - Clear storage and redirect to login
        if (status === 401) {
            if (!window.location.pathname.startsWith('/login')) {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('user');
                window.location.href = '/login';
                // Return a rejected promise to stop propagation, though page will reload
                return Promise.reject(error);
            }
        }

        // Handle 403 Forbidden
        if (status === 403) {
            // "Yetkin yok" message
            // Ideally use a toast, but alert is acceptable for global handler outside react context
            alert("Erişim yetkiniz yok (403).");
        }

        // Handle 413 Payload Too Large
        if (status === 413) {
            alert("Dosya boyutu çok büyük. Lütfen daha küçük bir dosya yükleyin (Max 50MB).");
        }

        return Promise.reject(error);
    }
);

export const downloadFile = async (url: string, defaultFileName: string, params?: any) => {
    await openDocument(url, {
        mode: 'download',
        defaultFileName,
        params,
    });
};

type OpenDocumentOptions = {
    mode: 'view' | 'download';
    defaultFileName?: string;
    params?: any;
    contentType?: string;
};

export const openDocument = async (url: string, options: OpenDocumentOptions) => {
    try {
        const response = await apiClient.get(normalizeApiUrl(url), {
            responseType: 'blob',
            params: options.params
        });

        // Try to get filename from content-disposition
        let fileName = options.defaultFileName || 'document';
        const suggestion = response.headers['content-disposition'];
        if (suggestion) {
            const match = suggestion.match(/filename="?([^"]+)"?/);
            if (match?.[1]) fileName = match[1];
        }

        const blob = new Blob([response.data], { type: options.contentType || response.headers['content-type'] || 'application/pdf' });
        const objectUrl = window.URL.createObjectURL(blob);

        if (options.mode === 'view') {
            window.open(objectUrl, '_blank', 'noopener,noreferrer');
            return;
        }

        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(objectUrl);
    } catch (error) {
        console.error('Document action failed:', error);
        throw error;
    }
};

export const viewFile = async (url: string, contentType?: string) => {
    await openDocument(url, {
        mode: 'view',
        contentType,
    });
};

const normalizeApiUrl = (url: string) => {
    const value = String(url || '').trim();
    if (!value) return value;

    if (/^https?:\/\//i.test(value)) {
        return value;
    }

    let normalized = value;
    if (API_BASE_PATH && normalized.startsWith(`${API_BASE_PATH}/`)) {
        normalized = normalized.slice(API_BASE_PATH.length);
    }

    if (!normalized.startsWith('/')) {
        normalized = `/${normalized}`;
    }

    return normalized;
};

export const mapBackendErrorToMessage = (error: unknown, fallback: string) => {
    const responseData = (error as { response?: { data?: { errorCode?: string; message?: string } } })?.response?.data;
    const errorCode = String(responseData?.errorCode ?? '').toUpperCase();
    const message = String(responseData?.message ?? '');
    const normalizedMessage = message.toUpperCase();

    if (errorCode.includes('INVALID_FILE_TYPE') || normalizedMessage.includes('INVALID_FILE_TYPE')) {
        return 'Sadece PDF yukleyebilirsiniz.';
    }

    if (
        errorCode.includes('INVALID_MULTIPART_JSON') ||
        normalizedMessage.includes('JSON PARSE') ||
        normalizedMessage.includes('CANNOT DESERIALIZE') ||
        normalizedMessage.includes('HTTPMESSAGE_NOT_READABLE') ||
        normalizedMessage.includes('FORM_DATA_JSON_ERROR')
    ) {
        return 'Form verisi hatali, tekrar deneyin.';
    }

    return message || fallback;
};
