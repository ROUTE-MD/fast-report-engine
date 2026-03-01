package com.fastreport.model.column;

import com.fastreport.model.style.Alignment;
import com.fastreport.model.style.CellStyle;
import lombok.Builder;

/**
 * Defines a single column in a ListSection.
 *
 * @param key          field key in the row Map
 * @param label        display header text
 * @param type         data type for formatting and alignment
 * @param widthWeight  relative width weight for PDF layout distribution
 * @param baseColumn   true = always visible in table header
 * @param alignment    explicit alignment (null = auto from type)
 * @param wrapText     true = wrap text in both PDF and XLSX (default true)
 * @param style        per-column style override (null = use table style)
 * @param excelWidth   explicit Excel column width in characters (0 = auto)
 */
@Builder(toBuilder = true)
public record ColumnDef(
        String key,
        String label,
        ColumnType type,
        float widthWeight,
        boolean baseColumn,
        Alignment alignment,
        boolean wrapText,
        CellStyle style,
        int excelWidth
) {
    /** Returns the effective alignment, auto-determined from type if not explicit. */
    public Alignment effectiveAlignment() {
        if (alignment != null) return alignment;
        return switch (type) {
            case STRING, BOOLEAN, DATE -> Alignment.LEFT;
            case INTEGER, DECIMAL, CURRENCY, PERCENTAGE -> Alignment.RIGHT;
        };
    }

    public static class ColumnDefBuilder {
        private float widthWeight = 1.0f;
        private boolean wrapText = true;
    }
}
