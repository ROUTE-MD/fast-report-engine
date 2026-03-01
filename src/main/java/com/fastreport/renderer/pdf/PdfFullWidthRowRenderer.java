package com.fastreport.renderer.pdf;

import com.fastreport.model.content.TextContent;
import com.fastreport.model.section.FullWidthRow;
import com.fastreport.model.style.Alignment;
import com.fastreport.model.style.FontStyle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;

/** Renders a FullWidthRow section in PDF. */
public class PdfFullWidthRowRenderer implements PdfSectionRenderer<FullWidthRow> {

    @Override
    public void render(FullWidthRow section, PdfPageContext ctx) throws IOException {
        ctx.moveY(-section.marginTop());
        ctx.ensureSpace(section.height());

        var stream = ctx.stream();
        float y = ctx.y();
        float margin = ctx.margin();
        float width = ctx.usableWidth();

        // Background
        if (section.backgroundColor() != null) {
            stream.setNonStrokingColor(section.backgroundColor());
            stream.addRect(margin, y - section.height(), width, section.height());
            stream.fill();
        }

        float textY = y - section.height() + 5f;

        // Left
        drawSlot(section.left(), ctx, margin, textY, width, Alignment.LEFT);
        // Center
        drawSlot(section.center(), ctx, margin, textY, width, Alignment.CENTER);
        // Right
        drawSlot(section.right(), ctx, margin, textY, width, Alignment.RIGHT);

        ctx.setY(y - section.height() - section.marginBottom());
    }

    private void drawSlot(TextContent content, PdfPageContext ctx,
                          float cellX, float cellY, float cellWidth,
                          Alignment align) throws IOException {
        if (content == null || content.text() == null) return;
        FontStyle fs = content.style() != null ? content.style() : ctx.theme().subtitleStyle();
        PDType1Font font = PdfPageContext.resolveFont(fs);
        PdfTextHelper.drawText(ctx.stream(), content.text(), font, fs.fontSize(),
                cellX, cellY, cellWidth, 3f, align, fs.color());
    }
}
