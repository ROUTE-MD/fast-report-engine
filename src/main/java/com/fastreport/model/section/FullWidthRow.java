package com.fastreport.model.section;

import com.fastreport.model.content.TextContent;
import lombok.Builder;

import java.awt.Color;

/**
 * A horizontal band spanning full page width with up to three text slots.
 * Used for subtitles, section headers, separator lines, etc.
 */
@Builder(toBuilder = true)
public record FullWidthRow(
        TextContent left,
        TextContent center,
        TextContent right,
        float height,
        Color backgroundColor,
        float marginTop,
        float marginBottom
) implements ReportSection {

    /** Default height for full-width rows. */
    public static final float DEFAULT_HEIGHT = 20f;
    /** Default bottom margin. */
    public static final float DEFAULT_MARGIN_BOTTOM = 4f;

    // Lombok @Builder needs a custom builder to set defaults on records
    public static class FullWidthRowBuilder {
        private float height = DEFAULT_HEIGHT;
        private float marginBottom = DEFAULT_MARGIN_BOTTOM;
    }
}
