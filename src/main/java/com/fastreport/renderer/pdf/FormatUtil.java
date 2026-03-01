package com.fastreport.renderer.pdf;

import com.fastreport.model.column.ColumnDef;
import com.fastreport.model.column.ColumnType;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Shared formatting utility for PDF rendering. */
final class FormatUtil {

    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ITALY);

    private FormatUtil() {}

    /** Formats a value based on column type. */
    static String format(Object value, ColumnType type, String currencySymbol, String datePattern) {
        if (value == null) return "";
        return switch (type) {
            case STRING -> value.toString();
            case INTEGER -> {
                if (value instanceof Number n) yield new DecimalFormat("#,##0", SYMBOLS).format(n.longValue());
                yield value.toString();
            }
            case DECIMAL -> {
                if (value instanceof Number n) yield new DecimalFormat("#,##0.00", SYMBOLS).format(n.doubleValue());
                yield value.toString();
            }
            case CURRENCY -> {
                if (value instanceof BigDecimal bd) yield currencySymbol + new DecimalFormat("#,##0.00", SYMBOLS).format(bd);
                if (value instanceof Number n) yield currencySymbol + new DecimalFormat("#,##0.00", SYMBOLS).format(n.doubleValue());
                yield value.toString();
            }
            case DATE -> {
                if (value instanceof LocalDate ld) yield ld.format(DateTimeFormatter.ofPattern(datePattern));
                yield value.toString();
            }
            case PERCENTAGE -> {
                if (value instanceof Number n) yield new DecimalFormat("0.0%", SYMBOLS).format(n.doubleValue());
                yield value.toString();
            }
            case BOOLEAN -> {
                if (value instanceof Boolean b) yield b ? "Yes" : "No";
                yield value.toString();
            }
        };
    }

    /** Formats a value using a ColumnDef (convenience). */
    static String format(Object value, ColumnDef col, String currencySymbol, String datePattern) {
        return format(value, col.type(), currencySymbol, datePattern);
    }

    /** Returns true if the value is a negative number. */
    static boolean isNegative(Object value) {
        if (value instanceof BigDecimal bd) return bd.signum() < 0;
        if (value instanceof Number n) return n.doubleValue() < 0;
        return false;
    }

    /** Returns true if the value is a positive number (strictly > 0). */
    static boolean isPositive(Object value) {
        if (value instanceof BigDecimal bd) return bd.signum() > 0;
        if (value instanceof Number n) return n.doubleValue() > 0;
        return false;
    }

    /** Returns true if the column type should color values by sign (red/green). */
    static boolean shouldColorBySign(ColumnType type) {
        return type == ColumnType.CURRENCY || type == ColumnType.DECIMAL;
    }
}
