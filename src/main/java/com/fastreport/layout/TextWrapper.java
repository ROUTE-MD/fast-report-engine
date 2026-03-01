package com.fastreport.layout;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Word-wraps text to fit within a given width using PDFBox font metrics.
 */
public final class TextWrapper {

    private TextWrapper() {}

    /**
     * Wraps text into lines that fit within maxWidth.
     *
     * @param text     the text to wrap (may be null)
     * @param maxWidth maximum line width in points
     * @param font     the PDFont for measurement
     * @param fontSize the font size in points
     * @return list of wrapped lines (minimum 1 element)
     * @throws IOException if font metrics cannot be read
     */
    public static List<String> wrap(String text, float maxWidth, PDFont font, float fontSize) throws IOException {
        if (text == null || text.isEmpty()) {
            return List.of("");
        }

        var lines = new ArrayList<String>();
        String[] words = text.split(" ");
        var current = new StringBuilder();

        for (String word : words) {
            float wordWidth = font.getStringWidth(word) / 1000f * fontSize;

            // Force-break a single word that exceeds max width
            if (wordWidth > maxWidth && current.isEmpty()) {
                lines.add(forceBreak(word, maxWidth, font, fontSize));
                continue;
            }

            String candidate = current.isEmpty() ? word : current + " " + word;
            float candidateWidth = font.getStringWidth(candidate) / 1000f * fontSize;

            if (candidateWidth <= maxWidth) {
                current = new StringBuilder(candidate);
            } else {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                }
                current = new StringBuilder(word);
            }
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }

        return lines.isEmpty() ? List.of("") : lines;
    }

    private static String forceBreak(String word, float maxWidth, PDFont font, float fontSize) throws IOException {
        // Return as much as fits; caller will get the remainder on next iteration
        for (int i = word.length() - 1; i > 0; i--) {
            String sub = word.substring(0, i);
            if (font.getStringWidth(sub) / 1000f * fontSize <= maxWidth) {
                return sub;
            }
        }
        return word.substring(0, 1);
    }
}
