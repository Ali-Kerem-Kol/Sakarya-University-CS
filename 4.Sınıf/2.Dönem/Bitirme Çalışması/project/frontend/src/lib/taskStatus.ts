import { TaskAssignmentStatus } from '@/api/adminTasks';

export const getTaskStatusLabel = (status: string | null | undefined) => {
    const normalized = String(status ?? '').toUpperCase() as TaskAssignmentStatus;
    switch (normalized) {
        case 'ASSIGNED':
            return 'Atandi';
        case 'SUBMITTED':
            return 'Teslim Edildi';
        case 'APPROVED':
            return 'Onaylandi';
        case 'REJECTED':
            return 'Reddedildi';
        case 'REVISION_REQUESTED':
            return 'Revizyon Istendi';
        case 'DONE':
            return 'Tamamlandi';
        case 'FAILED':
            return 'Basarisiz';
        default:
            return 'Bilinmiyor';
    }
};

export const getTaskStatusBadgeClass = (status: string | null | undefined) => {
    const normalized = String(status ?? '').toUpperCase() as TaskAssignmentStatus;
    switch (normalized) {
        case 'ASSIGNED':
            return 'bg-slate-100 text-slate-800 border-slate-300';
        case 'SUBMITTED':
            return 'bg-blue-100 text-blue-800 border-blue-300';
        case 'APPROVED':
            return 'bg-green-100 text-green-800 border-green-300';
        case 'REJECTED':
            return 'bg-red-100 text-red-800 border-red-300';
        case 'REVISION_REQUESTED':
            return 'bg-amber-100 text-amber-900 border-amber-300';
        case 'DONE':
            return 'bg-emerald-100 text-emerald-800 border-emerald-300';
        case 'FAILED':
            return 'bg-rose-100 text-rose-800 border-rose-300';
        default:
            return 'bg-slate-100 text-slate-700 border-slate-300';
    }
};
