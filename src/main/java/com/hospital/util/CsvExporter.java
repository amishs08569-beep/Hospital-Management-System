package com.hospital.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public final class CsvExporter {

    private CsvExporter() {}

    public static void exportToFile(File file, String[] headers, List<String[]> rows) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write headers
            if (headers != null && headers.length > 0) {
                writer.println(formatRow(headers));
            }
            // Write rows
            if (rows != null) {
                for (String[] row : rows) {
                    writer.println(formatRow(row));
                }
            }
        }
    }

    private static String formatRow(String[] items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append(",");
            String val = items[i] != null ? items[i] : "";
            // Escape quotes and commas
            if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
                sb.append("\"").append(val.replace("\"", "\"\"")).append("\"");
            } else {
                sb.append(val);
            }
        }
        return sb.toString();
    }
}
