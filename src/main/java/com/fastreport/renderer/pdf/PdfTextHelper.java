package com.fastreport.renderer.pdf;

import com.fastreport.model.style.Alignment;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.Color;
import java.io.IOException;

/** Utility for drawing aligned and truncated text in PDF cells. */
public final class PdfTextHelper {

    private PdfTextHelper() {}

    /**
     * Draws text inside a cell with alignment and color. Truncates with ellipsis if needed.
     */
    public static void drawText(PDPageContentStream stream, String text,
                                PDFont font, float fontSize,
                                float cellX, float cellY, float cellWidth,
                                float padding, Alignment align, Color color) throws IOException {
        if (text == null || text.isEmpty()) return;

        float usableWidth = cellWidth - 2 * padding;
        String truncated = truncate(text, usableWidth, font, fontSize);
        float textWidth = font.getStringWidth(truncated) / 1000f * fontSize;

        float x = switch (align) {
            case LEFT -> cellX + padding;
            case CENTER -> cellX + (cellWidth - textWidth) / 2f;
            case RIGHT -> cellX + cellWidth - padding - textWidth;
        };

        stream.setNonStrokingColor(color);
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, cellY);
        stream.showText(truncated);
        stream.endText();
    }

    /**
     * Truncates text with ellipsis if it exceeds maxWidth.
     */
    public static String truncate(String text, float maxWidth, PDFont font, float fontSize) throws IOException {
        if (text == null || text.isEmpty()) return text;
        if (font.getStringWidth(text) / 1000f * fontSize <= maxWidth) return text;

        String ellipsis = "...";
        float ellipsisW = font.getStringWidth(ellipsis) / 1000f * fontSize;
        float available = maxWidth - ellipsisW;
        if (available <= 0) return ellipsis;

        int lo = 0, hi = text.length();
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (font.getStringWidth(text.substring(0, mid)) / 1000f * fontSize <= available) lo = mid;
            else hi = mid - 1;
        }
        return text.substring(0, lo) + ellipsis;
    }
}
