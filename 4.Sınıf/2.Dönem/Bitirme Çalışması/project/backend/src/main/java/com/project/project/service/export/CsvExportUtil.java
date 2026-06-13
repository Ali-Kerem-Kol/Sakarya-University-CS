package com.project.project.service.export;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Writes CSV rows with basic escaping and injection protection.
 */
public final class CsvExportUtil {

    private CsvExportUtil() {
    }

    public static void writeRow(Writer writer, List<String> values) throws IOException {
        boolean first = true;
        for (String value : values) {
            if (!first) {
                writer.write(',');
            }
            writer.write(escapeCsv(sanitize(value)));
            first = false;
        }
        writer.write("\n");
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "";
        }
        if (!value.isEmpty()) {
            char first = value.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@') {
                return "'" + value;
            }
        }
        return value;
    }

    private static String escapeCsv(String value) {
        boolean needsQuoting = value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r");
        if (!needsQuoting) {
            return value;
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
