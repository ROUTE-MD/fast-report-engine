package com.fastreport.renderer.pdf;

import com.fastreport.model.ReportDefinition;
import com.fastreport.model.section.*;
import com.fastreport.model.style.Alignment;
import com.fastreport.model.style.FontStyle;
import com.fastreport.renderer.ReportRenderer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.io.OutputStream;

/** Main PDF orchestrator: renders a ReportDefinition to PDF via PDFBox. */
public class PdfRenderer implements ReportRenderer {

    private static final java.awt.Color SEPARATOR_COLOR = new java.awt.Color(52, 152, 219);

    private final PdfFullWidthRowRenderer fullWidthRenderer = new PdfFullWidthRowRenderer();
    private final PdfDetailRenderer detailRenderer = new PdfDetailRenderer();
    private final PdfListRenderer listRenderer = new PdfListRenderer();

    @Override
    public void render(ReportDefinition report, OutputStream out) throws IOException {
        try (var doc = new PDDocument()) {
            var ctx = new PdfPageContext(doc, report);

            // First page
            ctx.startNewPage();

            // Title
            FontStyle titleFs = report.getTheme().titleStyle();
            PDType1Font titleFont = PdfPageContext.resolveFont(titleFs);
            ctx.moveY(-titleFs.fontSize());
            Alignment titleAlign = report.getTitleAlignment() != null ? report.getTitleAlignment() : Alignment.LEFT;
            PdfTextHelper.drawText(ctx.stream(), report.getTitle(), titleFont, titleFs.fontSize(),
                    ctx.margin(), ctx.y(), ctx.usableWidth(), 0f, titleAlign, titleFs.color());
            ctx.moveY(-4f);

            // Metadata
            if (report.getMetadata() != null && !report.getMetadata().isEmpty()) {
                FontStyle labelFs = report.getTheme().metadataLabelStyle();
                FontStyle valueFs = report.getTheme().metadataValueStyle();
                PDType1Font labelFont = PdfPageContext.resolveFont(labelFs);
                PDType1Font valueFont = PdfPageContext.resolveFont(valueFs);

                for (var entry : report.getMetadata().entrySet()) {
                    ctx.moveY(-11f);
                    String labelText = entry.getKey() + ": ";
                    float labelW = labelFont.getStringWidth(labelText) / 1000f * labelFs.fontSize();

                    ctx.stream().beginText();
                    ctx.stream().setFont(labelFont, labelFs.fontSize());
                    ctx.stream().setNonStrokingColor(labelFs.color());
                    ctx.stream().newLineAtOffset(ctx.margin(), ctx.y());
                    ctx.stream().showText(labelText);
                    ctx.stream().endText();

                    ctx.stream().beginText();
                    ctx.stream().setFont(valueFont, valueFs.fontSize());
                    ctx.stream().setNonStrokingColor(valueFs.color());
                    ctx.stream().newLineAtOffset(ctx.margin() + labelW, ctx.y());
                    ctx.stream().showText(entry.getValue());
                    ctx.stream().endText();
                }
                ctx.moveY(-6f);
            }

            // Sections
            if (report.getSections() != null) {
                for (ReportSection section : report.getSections()) {
                    switch (section) {
                        case FullWidthRow fwr -> fullWidthRenderer.render(fwr, ctx);
                        case DetailSection ds -> detailRenderer.render(ds, ctx);
                        case ListSection ls -> listRenderer.render(ls, ctx);
                        case SeparatorLine sl -> renderSeparator(sl, ctx);
                    }
                }
            }

            ctx.closeFinal();
            doc.save(out);
        }
    }

    private void renderSeparator(SeparatorLine sl, PdfPageContext ctx) throws IOException {
        ctx.moveY(-sl.marginTop());
        ctx.ensureSpace(sl.thickness() + sl.marginBottom());
        java.awt.Color color = sl.color() != null ? sl.color() : SEPARATOR_COLOR;
        ctx.stream().setStrokingColor(color);
        ctx.stream().setLineWidth(sl.thickness());
        ctx.stream().moveTo(ctx.margin(), ctx.y());
        ctx.stream().lineTo(ctx.margin() + ctx.usableWidth(), ctx.y());
        ctx.stream().stroke();
        ctx.moveY(-sl.marginBottom());
    }
}
