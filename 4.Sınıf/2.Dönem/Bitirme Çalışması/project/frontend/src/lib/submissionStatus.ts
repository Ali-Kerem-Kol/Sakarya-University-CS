export type SubmissionStatusKey = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELED' | 'REMOVED' | 'UNKNOWN';

export const normalizeSubmissionStatus = (status: string | null | undefined): SubmissionStatusKey => {
    const normalized = String(status ?? '').trim().toUpperCase();

    if (normalized === 'PENDING' || normalized === 'PENDING_APPROVAL') {
        return 'PENDING';
    }

    if (normalized === 'APPROVED') {
        return 'APPROVED';
    }

    if (normalized === 'REJECTED') {
        return 'REJECTED';
    }

    if (normalized === 'CANCELED' || normalized === 'CANCELLED') {
        return 'CANCELED';
    }

    if (normalized === 'REMOVED' || normalized === 'DELETED') {
        return 'REMOVED';
    }

    return 'UNKNOWN';
};

export const getSubmissionStatusLabel = (status: string | null | undefined): string => {
    const normalizedStatus = normalizeSubmissionStatus(status);

    if (normalizedStatus === 'PENDING') return 'Onay Bekliyor';
    if (normalizedStatus === 'APPROVED') return 'Onaylandi';
    if (normalizedStatus === 'REJECTED') return 'Reddedildi';
    if (normalizedStatus === 'CANCELED') return 'Canceled';
    if (normalizedStatus === 'REMOVED') return 'Removed';

    return 'Bilinmiyor';
};

export const normalizeEntityId = (value: unknown): string => {
    if (value === null || value === undefined) {
        return '';
    }
    return String(value);
};
