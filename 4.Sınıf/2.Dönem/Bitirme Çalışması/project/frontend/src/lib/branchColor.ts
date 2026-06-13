import { TaskGraphEdge, TaskGraphNode } from '@/api/adminTimeline';

const HASH_SEED = 2166136261;

const fnv1a = (value: string): number => {
    let hash = HASH_SEED;
    for (let index = 0; index < value.length; index += 1) {
        hash ^= value.charCodeAt(index);
        hash += (hash << 1) + (hash << 4) + (hash << 7) + (hash << 8) + (hash << 24);
    }
    return hash >>> 0;
};

const ALLOWED_HUE_BANDS: Array<[number, number]> = [
    [110, 165],
    [185, 255],
    [265, 320],
];

const pickHue = (hash: number): number => {
    const band = ALLOWED_HUE_BANDS[hash % ALLOWED_HUE_BANDS.length];
    const [min, max] = band;
    const span = max - min;
    const offset = Math.floor(hash / ALLOWED_HUE_BANDS.length) % (span + 1);
    return min + offset;
};

const normalizeBranchKey = (value: unknown) => String(value ?? 'MAIN').trim().toUpperCase() || 'MAIN';
const extractUserToken = (branchKey: string) => {
    const normalized = normalizeBranchKey(branchKey);
    const match = normalized.match(/^USER[-_](.+)$/);
    return match?.[1] ?? null;
};

const getColorFromSeed = (seed: string, satBase: number, satRange: number, lightBase: number, lightRange: number) => {
    const hash = fnv1a(seed);
    const hue = pickHue(hash);
    const saturation = satBase + (hash % satRange);
    const lightness = lightBase + ((hash >> 4) % lightRange);
    return `hsl(${hue} ${saturation}% ${lightness}%)`;
};

export const colorFromUserId = (userId: string | number) => {
    const token = String(userId ?? '').trim().toUpperCase();
    return getColorFromSeed(`USER-${token}`, 52, 14, 42, 11);
};

export const getBranchColor = (branchKey: unknown): string => {
    const normalized = normalizeBranchKey(branchKey);
    if (normalized === 'MAIN') {
        return 'hsl(214 20% 35%)';
    }

    const userToken = extractUserToken(normalized);
    if (userToken) {
        return colorFromUserId(userToken);
    }

    return getColorFromSeed(normalized, 52, 14, 42, 11);
};

export const getBranchLineColor = (branchKey: unknown): string => {
    const normalized = normalizeBranchKey(branchKey);
    if (normalized === 'MAIN') {
        return 'hsl(214 18% 46%)';
    }

    const userToken = extractUserToken(normalized);
    if (userToken) {
        return getColorFromSeed(`LINE-USER-${userToken}`, 58, 16, 45, 8);
    }

    return getColorFromSeed(`LINE-${normalized}`, 58, 16, 45, 8);
};

export const getStatusNodeStyle = (status: TaskGraphNode['status'], branchKey: string) => {
    const normalizedBranch = normalizeBranchKey(branchKey);
    if (normalizedBranch === 'MAIN') {
        return {
            fill: 'hsl(214 20% 35%)',
            stroke: 'hsl(214 20% 35%)',
            strokeWidth: 2.2,
        };
    }

    const normalized = String(status ?? 'PENDING').toUpperCase();
    if (normalized === 'SUCCESS') {
        return {
            fill: '#16a34a',
            stroke: '#166534',
            strokeWidth: 2,
        };
    }

    if (normalized === 'SUBMITTED') {
        return {
            fill: '#14b8a6',
            stroke: '#0f766e',
            strokeWidth: 2,
        };
    }

    if (normalized === 'FAILED') {
        return {
            fill: '#dc2626',
            stroke: '#991b1b',
            strokeWidth: 2,
        };
    }

    if (normalized === 'REVISION_REQUESTED') {
        return {
            fill: '#eab308',
            stroke: '#a16207',
            strokeWidth: 2,
        };
    }

    return {
        fill: '#ffffff',
        stroke: getBranchColor(normalizedBranch),
        strokeWidth: 2.6,
    };
};

export const getEdgeColor = (edge: TaskGraphEdge, nodeByTaskId: Map<string, TaskGraphNode>) => {
    const source = nodeByTaskId.get(String(edge.fromTaskId));
    return source ? getBranchLineColor(source.branchKey) : 'hsl(214 12% 60%)';
};

