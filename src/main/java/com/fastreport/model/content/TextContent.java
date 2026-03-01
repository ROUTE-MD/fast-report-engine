package com.fastreport.model.content;

import com.fastreport.model.style.FontStyle;

/**
 * A piece of text with an optional style override.
 *
 * @param text  the text to display
 * @param style optional font style (null = use theme defaults)
 */
public record TextContent(String text, FontStyle style) {

    /** Creates text content with default styling. */
    public TextContent(String text) {
        this(text, null);
    }
}
