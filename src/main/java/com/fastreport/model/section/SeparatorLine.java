package com.fastreport.model.section;

import lombok.Builder;

import java.awt.Color;

/**
 * A horizontal separator line spanning the full page/table width.
 *
 * @param color     line color (null = use theme accent color)
 * @param thickness line thickness in points
 * @param marginTop space above the line
 * @param marginBottom space below the line
 */
@Builder(toBuilder = true)
public record SeparatorLine(
        Color color,
        float thickness,
        float marginTop,
        float marginBottom
) implements ReportSection {

    public static class SeparatorLineBuilder {
        private float thickness = 1.5f;
        private float marginTop = 4f;
        private float marginBottom = 4f;
    }
}
