package com.project.project.util;

import java.util.regex.Pattern;

/**
 * Resolves stable per-user colors and validates custom color values.
 */
public final class UserColorResolver {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final String[] DEFAULT_PALETTE = {
            "#2563EB",
            "#0891B2",
            "#0D9488",
            "#16A34A",
            "#CA8A04",
            "#EA580C",
            "#DC2626",
            "#DB2777",
            "#9333EA",
            "#4F46E5",
            "#0F766E",
            "#65A30D"
    };

    private UserColorResolver() {
    }

    public static String resolveDisplayColor(Long userId, String preferredColor) {
        String normalized = normalizeHexOrNull(preferredColor);
        if (normalized != null) {
            return normalized;
        }
        return deterministicColorForUserId(userId);
    }

    public static String normalizeHexOrNull(String color) {
        if (color == null) {
            return null;
        }
        String trimmed = color.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!HEX_COLOR_PATTERN.matcher(trimmed).matches()) {
            return null;
        }
        return trimmed.toUpperCase();
    }

    public static String deterministicColorForUserId(Long userId) {
        long safeId = userId != null ? userId : 0L;
        int index = (int) Math.floorMod(safeId, DEFAULT_PALETTE.length);
        return DEFAULT_PALETTE[index];
    }
}
