package com.fastreport.model.style;

import lombok.Builder;

import java.awt.Color;

/**
 * Font style definition: family, size, weight, color.
 *
 * @param fontFamily font name (maps to Standard14 in PDFBox, ignored in Excel)
 * @param fontSize   size in points
 * @param bold       bold weight
 * @param italic     italic style
 * @param color      text color
 */
@Builder(toBuilder = true)
public record FontStyle(
        String fontFamily,
        float fontSize,
        boolean bold,
        boolean italic,
        Color color
) {
    /** Sensible defaults: Helvetica 10pt, regular, black. */
    public static FontStyle defaults() {
        return new FontStyle("Helvetica", 10f, false, false, Color.BLACK);
    }
}
