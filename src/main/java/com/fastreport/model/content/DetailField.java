package com.fastreport.model.content;

import com.fastreport.model.column.ColumnType;
import com.fastreport.model.style.FontStyle;

/**
 * A single key-value field in a DetailSection.
 *
 * @param label      the label text
 * @param value      the value (String, BigDecimal, LocalDate, Number, Boolean)
 * @param type       column type for formatting (default STRING)
 * @param labelStyle optional label style override
 * @param valueStyle optional value style override
 */
public record DetailField(
        String label,
        Object value,
        ColumnType type,
        FontStyle labelStyle,
        FontStyle valueStyle
) {
    /** Creates a field with STRING type and no style overrides. */
    public DetailField(String label, Object value) {
        this(label, value, ColumnType.STRING, null, null);
    }

    /** Creates a field with a specific type and no style overrides. */
    public DetailField(String label, Object value, ColumnType type) {
        this(label, value, type, null, null);
    }
}
