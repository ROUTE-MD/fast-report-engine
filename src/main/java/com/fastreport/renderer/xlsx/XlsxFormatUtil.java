package com.fastreport.renderer.xlsx;

import com.fastreport.model.column.ColumnDef;
import com.fastreport.model.column.ColumnType;
import com.fastreport.model.style.Alignment;
import org.dhatim.fastexcel.Worksheet;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Shared Excel formatting utility. */
final class XlsxFormatUtil {

    private XlsxFormatUtil() {}

    /** Writes a cell value with correct Excel data type and formatting. */
    static void writeTypedValue(Worksheet ws, int row, int col, Object value,
                                ColumnType type, String currencySymbol,
                                String datePattern) throws IOException {
        if (value == null) return;

        switch (type) {
            case STRING -> ws.value(row, col, value.toString());
            case INTEGER -> {
                if (value instanceof Number n) {
                    ws.value(row, col, n.longValue());
                    ws.style(row, col).format("#,##0").set();
                } else {
                    ws.value(row, col, value.toString());
                }
            }
            case DECIMAL -> {
                if (value instanceof Number n) {
                    ws.value(row, col, n.doubleValue());
                    ws.style(row, col).format("#,##0.00").set();
                } else {
                    ws.value(row, col, value.toString());
                }
            }
            case CURRENCY -> {
                double dv = value instanceof BigDecimal bd ? bd.doubleValue()
                        : value instanceof Number n ? n.doubleValue() : 0;
                ws.value(row, col, dv);
                ws.style(row, col).format(currencySymbol + "#,##0.00").set();
            }
            case DATE -> {
                if (value instanceof LocalDate ld) {
                    ws.value(row, col, ld.format(DateTimeFormatter.ofPattern(datePattern)));
                } else {
                    ws.value(row, col, value.toString());
                }
            }
            case PERCENTAGE -> {
                if (value instanceof Number n) {
                    ws.value(row, col, n.doubleValue());
                    ws.style(row, col).format("0.0%").set();
                } else {
                    ws.value(row, col, value.toString());
                }
            }
            case BOOLEAN -> {
                if (value instanceof Boolean b) ws.value(row, col, b ? "Yes" : "No");
                else ws.value(row, col, value.toString());
            }
        }
    }

    /** Returns the Excel horizontal alignment string for a column, respecting per-column override. */
    static String excelAlignment(ColumnDef col) {
        Alignment a = col.effectiveAlignment();
        return switch (a) {
            case LEFT -> "left";
            case CENTER -> "center";
            case RIGHT -> "right";
        };
    }

    /** Returns the Excel horizontal alignment string for a column type (fallback). */
    static String excelAlignment(ColumnType type) {
        return switch (type) {
            case STRING, BOOLEAN, DATE -> "left";
            case INTEGER, DECIMAL, CURRENCY, PERCENTAGE -> "right";
        };
    }

    /** Estimates a sensible Excel column width for a column type. */
    static int estimateWidth(ColumnType type, String label) {
        int labelW = label.length() + 4;
        int typeW = switch (type) {
            case STRING -> 20;
            case DATE -> 14;
            case CURRENCY -> 16;
            case DECIMAL -> 14;
            case INTEGER -> 12;
            case PERCENTAGE -> 12;
            case BOOLEAN -> 8;
        };
        return Math.max(labelW, typeW);
    }
}
