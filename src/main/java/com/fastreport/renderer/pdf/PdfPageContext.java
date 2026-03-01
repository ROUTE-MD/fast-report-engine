package com.fastreport.renderer.pdf;

import com.fastreport.model.ReportDefinition;
import com.fastreport.model.ReportOrientation;
import com.fastreport.model.ReportTheme;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mutable state tracker for PDF rendering: current page, Y position, content stream.
 */
public class PdfPageContext {

    private static final float MARGIN = 30f;
    private static final float FOOTER_OFFSET = 15f;

    private final PDDocument document;
    private final ReportDefinition report;
    private final ReportTheme theme;
    private final float pageWidth;
    private final float pageHeight;
    private final PDImageXObject logoImage;
    private final String generatedAt;

    private PDPageContentStream stream;
    private float y;
    private int pageNumber;

    public PdfPageContext(PDDocument document, ReportDefinition report) throws IOException {
        this.document = document;
        this.report = report;
        this.theme = report.getTheme();
        this.generatedAt = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(theme.dateTimePattern()));

        if (report.getOrientation() == ReportOrientation.LANDSCAPE) {
            this.pageWidth = PDRectangle.A4.getHeight();
            this.pageHeight = PDRectangle.A4.getWidth();
        } else {
            this.pageWidth = PDRectangle.A4.getWidth();
            this.pageHeight = PDRectangle.A4.getHeight();
        }

        if (report.getLogo() != null && report.getLogo().length > 0) {
            this.logoImage = PDImageXObject.createFromByteArray(document, report.getLogo(), "logo");
        } else {
            this.logoImage = null;
        }
    }

    /** Starts a new page, drawing logo if present. Returns the Y position for content. */
    public float startNewPage() throws IOException {
        if (stream != null) {
            drawFooter();
            stream.close();
        }
        PDPage page = new PDPage(new PDRectangle(pageWidth, pageHeight));
        document.addPage(page);
        pageNumber++;
        stream = new PDPageContentStream(document, page);
        y = pageHeight - MARGIN;

        if (logoImage != null) {
            stream.drawImage(logoImage, MARGIN, y - report.getLogoHeight(),
                    report.getLogoWidth(), report.getLogoHeight());
            y -= report.getLogoHeight() + 4f;
        }
        return y;
    }

    /** Returns true if remaining space is insufficient. */
    public boolean needsPageBreak(float requiredHeight) {
        return y - requiredHeight < MARGIN + 20f;
    }

    /** Starts a new page if needed. */
    public void ensureSpace(float requiredHeight) throws IOException {
        if (needsPageBreak(requiredHeight)) {
            startNewPage();
        }
    }

    /** Draws the page footer (page number and generation info). */
    public void drawFooter() throws IOException {
        PDFont font = resolveFont(false, false);
        float fs = theme.pageFooterFontSize();
        Color color = theme.pageFooterColor();
        float footerY = MARGIN - FOOTER_OFFSET;

        stream.beginText();
        stream.setFont(font, fs);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(MARGIN, footerY);
        stream.showText("Page " + pageNumber);
        stream.endText();

        String right = "Generated: " + generatedAt;
        float rw = font.getStringWidth(right) / 1000f * fs;
        stream.beginText();
        stream.setFont(font, fs);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(MARGIN + usableWidth() - rw, footerY);
        stream.showText(right);
        stream.endText();
    }

    /** Closes the last page (draws footer and closes stream). */
    public void closeFinal() throws IOException {
        if (stream != null) {
            drawFooter();
            stream.close();
            stream = null;
        }
    }

    public PDPageContentStream stream() { return stream; }
    public float y() { return y; }
    public void setY(float y) { this.y = y; }
    public void moveY(float delta) { this.y += delta; }
    public float margin() { return MARGIN; }
    public float usableWidth() { return pageWidth - 2 * MARGIN; }
    public float pageWidth() { return pageWidth; }
    public float pageHeight() { return pageHeight; }
    public int pageNumber() { return pageNumber; }
    public ReportTheme theme() { return theme; }
    public PDDocument document() { return document; }

    /** Resolves a Standard14 font from bold/italic flags. */
    public static PDType1Font resolveFont(boolean bold, boolean italic) {
        if (bold && italic) return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);
        if (bold) return new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        if (italic) return new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    /** Resolves a font from a FontStyle. */
    public static PDType1Font resolveFont(com.fastreport.model.style.FontStyle style) {
        return resolveFont(style.bold(), style.italic());
    }
}
