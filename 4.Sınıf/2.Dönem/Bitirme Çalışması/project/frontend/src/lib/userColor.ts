const HEX_COLOR_PATTERN = /^#[0-9A-Fa-f]{6}$/;

const DEFAULT_PALETTE = [
    '#2563EB',
    '#0891B2',
    '#0D9488',
    '#16A34A',
    '#CA8A04',
    '#EA580C',
    '#DC2626',
    '#DB2777',
    '#9333EA',
    '#4F46E5',
    '#0F766E',
    '#65A30D',
];

export const normalizeHexColor = (value?: string | null): string | null => {
    if (!value) return null;
    const trimmed = String(value).trim();
    if (!trimmed || !HEX_COLOR_PATTERN.test(trimmed)) return null;
    return trimmed.toUpperCase();
};

export const getDeterministicUserColor = (userId?: string | number | null): string => {
    const parsed = Number(String(userId ?? '0'));
    const safe = Number.isFinite(parsed) ? Math.trunc(parsed) : 0;
    const index = ((safe % DEFAULT_PALETTE.length) + DEFAULT_PALETTE.length) % DEFAULT_PALETTE.length;
    return DEFAULT_PALETTE[index];
};

export const resolveUserColor = (userId?: string | number | null, preferredColor?: string | null): string =>
    normalizeHexColor(preferredColor) ?? getDeterministicUserColor(userId);
