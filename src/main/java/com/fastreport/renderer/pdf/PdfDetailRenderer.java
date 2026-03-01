package com.fastreport.renderer.pdf;

import com.fastreport.model.content.DetailField;
import com.fastreport.model.section.DetailSection;
import com.fastreport.model.style.Alignment;
import com.fastreport.model.style.CellStyle;
import com.fastreport.model.style.FontStyle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;

/** Renders a DetailSection (key-value grid) in PDF. */
public class PdfDetailRenderer implements PdfSectionRenderer<DetailSection> {

    private static final float ROW_HEIGHT = 14f;
    private static final float PADDING = 4f;

    @Override
    public void render(DetailSection section, PdfPageContext ctx) throws IOException {
        ctx.moveY(-section.marginTop());

        // Section title
        if (section.sectionTitle() != null) {
            ctx.ensureSpace(18f);
            FontStyle titleFs = ctx.theme().subtitleStyle().toBuilder().bold(true).build();
            PDType1Font titleFont = PdfPageContext.resolveFont(titleFs);
            ctx.moveY(-titleFs.fontSize() - 2f);
            PdfTextHelper.drawText(ctx.stream(), section.sectionTitle(), titleFont, titleFs.fontSize(),
                    ctx.margin(), ctx.y(), ctx.usableWidth(), 0f, Alignment.LEFT, titleFs.color());
            ctx.moveY(-6f);
        }

        int cols = section.columns();
        float colWidth = ctx.usableWidth() / cols;
        float labelPortion = 0.4f;
        CellStyle labelStyle = ctx.theme().detailLabelStyle();
        CellStyle valueStyle = ctx.theme().detailValueStyle();

        var fields = section.fields();
        for (int i = 0; i < fields.size(); i += cols) {
            ctx.ensureSpace(ROW_HEIGHT);
            float rowY = ctx.y();

            for (int c = 0; c < cols && i + c < fields.size(); c++) {
                DetailField field = fields.get(i + c);
                float cellX = ctx.margin() + c * colWidth;
                float labelW = colWidth * labelPortion;
                float valueW = colWidth * (1f - labelPortion);

                FontStyle lfs = field.labelStyle() != null ? field.labelStyle() : labelStyle.font();
                FontStyle vfs = field.valueStyle() != null ? field.valueStyle() : valueStyle.font();
                PDType1Font lFont = PdfPageContext.resolveFont(lfs);
                PDType1Font vFont = PdfPageContext.resolveFont(vfs);

                float textY = rowY - ROW_HEIGHT + 4f;

                // Label
                PdfTextHelper.drawText(ctx.stream(), field.label() + ":", lFont, lfs.fontSize(),
                        cellX, textY, labelW, PADDING, Alignment.LEFT, lfs.color());

                // Value
                String formatted = FormatUtil.format(field.value(), field.type(),
                        ctx.theme().currencySymbol(), ctx.theme().datePattern());
                PdfTextHelper.drawText(ctx.stream(), formatted, vFont, vfs.fontSize(),
                        cellX + labelW, textY, valueW, PADDING, Alignment.LEFT, vfs.color());
            }

            ctx.setY(rowY - ROW_HEIGHT);
        }

        ctx.moveY(-section.marginBottom());
    }
}
