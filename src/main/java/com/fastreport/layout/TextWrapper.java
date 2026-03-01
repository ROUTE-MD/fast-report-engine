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

            // Word itself exceeds max width — force-break it into multiple lines
            if (wordWidth > maxWidth) {
                if (!current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder();
                }
                forceBreakAll(word, maxWidth, font, fontSize, lines);
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

    /** Breaks a long word into as many lines as needed, adding all to the list. */
    private static void forceBreakAll(String word, float maxWidth, PDFont font,
                                      float fontSize, List<String> lines) throws IOException {
        String remaining = word;
        while (!remaining.isEmpty()) {
            float rw = font.getStringWidth(remaining) / 1000f * fontSize;
            if (rw <= maxWidth) {
                lines.add(remaining);
                break;
            }
            int cutAt = findCut(remaining, maxWidth, font, fontSize);
            lines.add(remaining.substring(0, cutAt));
            remaining = remaining.substring(cutAt);
        }
    }

    /** Binary-searches for the longest prefix that fits within maxWidth. */
    private static int findCut(String text, float maxWidth, PDFont font, float fontSize) throws IOException {
        int lo = 1, hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (font.getStringWidth(text.substring(0, mid)) / 1000f * fontSize <= maxWidth) {
                lo = mid;
            } else {
                hi = mid - 1;
            }
        }
        return lo;
    }
}
