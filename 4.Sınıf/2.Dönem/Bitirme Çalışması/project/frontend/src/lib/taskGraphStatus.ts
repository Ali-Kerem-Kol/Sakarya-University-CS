import { TaskGraphNodeStatus } from '@/api/adminTimeline';

export const getTaskGraphStatusLabel = (status: string | null | undefined) => {
    const normalized = String(status ?? '').toUpperCase() as TaskGraphNodeStatus;
    switch (normalized) {
        case 'SUBMITTED':
            return 'Admin Onayinda';
        case 'SUCCESS':
            return 'Basarili';
        case 'FAILED':
            return 'Basarisiz';
        case 'REVISION_REQUESTED':
            return 'Revizyon Istendi';
        case 'PENDING':
        default:
            return 'Bekliyor';
    }
};

export const getTaskGraphStatusBadgeClass = (status: string | null | undefined) => {
    const normalized = String(status ?? '').toUpperCase() as TaskGraphNodeStatus;
    switch (normalized) {
        case 'SUBMITTED':
            return 'bg-cyan-100 text-cyan-800 border-cyan-300';
        case 'SUCCESS':
            return 'bg-emerald-100 text-emerald-800 border-emerald-300';
        case 'FAILED':
            return 'bg-red-100 text-red-800 border-red-300';
        case 'REVISION_REQUESTED':
            return 'bg-amber-100 text-amber-900 border-amber-300';
        case 'PENDING':
        default:
            return 'bg-slate-100 text-slate-800 border-slate-300';
    }
};
