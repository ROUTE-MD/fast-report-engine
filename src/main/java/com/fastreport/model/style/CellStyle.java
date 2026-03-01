package com.fastreport.model.style;

import lombok.Builder;

import java.awt.Color;

/**
 * Style for a table cell: font, background, alignment, padding, border.
 *
 * @param font                font style
 * @param backgroundColor     cell background (null = transparent)
 * @param horizontalAlignment text alignment within the cell
 * @param paddingH            horizontal padding in points (PDF)
 * @param paddingV            vertical padding in points (PDF)
 * @param borderColor         cell border color (null = no border)
 */
@Builder(toBuilder = true)
public record CellStyle(
        FontStyle font,
        Color backgroundColor,
        Alignment horizontalAlignment,
        float paddingH,
        float paddingV,
        Color borderColor
) {
    /** Sensible defaults: default font, left-aligned, 3pt/2pt padding, no border. */
    public static CellStyle defaults() {
        return new CellStyle(FontStyle.defaults(), null, Alignment.LEFT, 3f, 2f, null);
    }
}
