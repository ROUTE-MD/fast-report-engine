package com.fastreport.renderer.pdf;

import com.fastreport.layout.ResponsiveTableLayout;
import com.fastreport.model.column.ColumnDef;
import com.fastreport.model.section.ListSection;
import com.fastreport.model.style.Alignment;
import com.fastreport.model.style.FontStyle;
import com.fastreport.model.style.TableStyle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/** Renders a ListSection (table with responsive overflow) in PDF. */
public class PdfListRenderer implements PdfSectionRenderer<ListSection> {

    private static final float HEADER_HEIGHT = 18f;
    private static final float MAIN_ROW_HEIGHT = 14f;
    private static final float DETAIL_ROW_HEIGHT = 12f;
    private static final float DETAIL_GAP = 2f;
    private static final float SUBTITLE_HEIGHT = 10f;
    private static final float ACCENT_BAR_WIDTH = 2.5f;
    private static final Color RED = new Color(0xCC, 0x00, 0x00);

    @Override
    public void render(ListSection section, PdfPageContext ctx) throws IOException {
        ctx.moveY(-section.marginTop());

        TableStyle ts = section.tableStyle() != null ? section.tableStyle() : ctx.theme().tableStyle();

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

        // Layout
        ResponsiveTableLayout layout = new ResponsiveTableLayout(
                section.columns(), ctx.usableWidth(),
                section.baseColumnKeys() != null ? section.baseColumnKeys() : List.of());

        List<ColumnDef> mainCols = layout.mainColumns();
        List<ColumnDef> detailCols = layout.detailColumns();
        float[] colWidths = layout.mainColWidths();
        boolean hasDetail = layout.hasDetailRows();
        int detailPerRow = layout.detailColumnsPerRow();

        String curr = ctx.theme().currencySymbol();
        String datePat = ctx.theme().datePattern();

        // Draw table header
        drawHeader(ctx, mainCols, colWidths, ts, layout.tableWidth());

        // Detail subtitle
        if (hasDetail) {
            drawDetailSubtitle(ctx, detailCols, layout.tableWidth());
        }

        // Data rows
        int rowCount = 0;
        for (Map<String, Object> row : section.rows()) {
            rowCount++;
            boolean isAlt = rowCount % 2 == 0;

            float recordHeight = MAIN_ROW_HEIGHT;
            if (hasDetail) {
                int detailLines = (int) Math.ceil((double) detailCols.size() / detailPerRow);
                recordHeight += 2 * DETAIL_GAP + detailLines * DETAIL_ROW_HEIGHT;
            }

            if (ctx.needsPageBreak(recordHeight)) {
                ctx.startNewPage();
                drawHeader(ctx, mainCols, colWidths, ts, layout.tableWidth());
                if (hasDetail) drawDetailSubtitle(ctx, detailCols, layout.tableWidth());
            }

            // Main row
            var rowStyle = isAlt ? ts.altRowStyle() : ts.rowStyle();
            float y = ctx.y();

            if (rowStyle.backgroundColor() != null) {
                ctx.stream().setNonStrokingColor(rowStyle.backgroundColor());
                ctx.stream().addRect(ctx.margin(), y - MAIN_ROW_HEIGHT, layout.tableWidth(), MAIN_ROW_HEIGHT);
                ctx.stream().fill();
            }

            float cellX = ctx.margin();
            float textY = y - MAIN_ROW_HEIGHT + 4f;
            PDType1Font dataFont = PdfPageContext.resolveFont(rowStyle.font());

            for (int c = 0; c < mainCols.size(); c++) {
                ColumnDef col = mainCols.get(c);
                Object val = row.get(col.key());
                String text = FormatUtil.format(val, col, curr, datePat);
                Color color = (FormatUtil.shouldRedIfNegative(col.type()) && FormatUtil.isNegative(val))
                        ? RED : rowStyle.font().color();
                PdfTextHelper.drawText(ctx.stream(), text, dataFont, rowStyle.font().fontSize(),
                        cellX, textY, colWidths[c], rowStyle.paddingH(), col.effectiveAlignment(), color);
                cellX += colWidths[c];
            }

            // Grid line
            ctx.stream().setStrokingColor(ts.gridColor());
            ctx.stream().setLineWidth(0.3f);
            ctx.stream().moveTo(ctx.margin(), y - MAIN_ROW_HEIGHT);
            ctx.stream().lineTo(ctx.margin() + layout.tableWidth(), y - MAIN_ROW_HEIGHT);
            ctx.stream().stroke();

            ctx.setY(y - MAIN_ROW_HEIGHT);

            // Detail rows
            if (hasDetail) {
                ctx.moveY(-DETAIL_GAP);
                int detailLines = (int) Math.ceil((double) detailCols.size() / detailPerRow);
                float detailHeight = detailLines * DETAIL_ROW_HEIGHT;

                // Background
                ctx.stream().setNonStrokingColor(ts.detailRowValueStyle().backgroundColor() != null
                        ? ts.detailRowValueStyle().backgroundColor()
                        : new Color(250, 250, 245));
                ctx.stream().addRect(ctx.margin(), ctx.y() - detailHeight, layout.tableWidth(), detailHeight);
                ctx.stream().fill();

                // Accent bar
                ctx.stream().setNonStrokingColor(ts.accentColor());
                ctx.stream().addRect(ctx.margin(), ctx.y() - detailHeight, ACCENT_BAR_WIDTH, detailHeight);
                ctx.stream().fill();

                // Key-value pairs
                float pairWidth = layout.tableWidth() / detailPerRow;
                PDType1Font labelFont = PdfPageContext.resolveFont(ts.detailRowLabelStyle().font());
                PDType1Font valueFont = PdfPageContext.resolveFont(ts.detailRowValueStyle().font());
                float labelFs = ts.detailRowLabelStyle().font().fontSize();
                float valueFs = ts.detailRowValueStyle().font().fontSize();

                for (int d = 0; d < detailCols.size(); d++) {
                    int line = d / detailPerRow;
                    int pair = d % detailPerRow;
                    ColumnDef dc = detailCols.get(d);
                    Object val = row.get(dc.key());
                    String label = dc.label() + ": ";
                    String value = FormatUtil.format(val, dc, curr, datePat);
                    Color valColor = (FormatUtil.shouldRedIfNegative(dc.type()) && FormatUtil.isNegative(val))
                            ? RED : ts.detailRowValueStyle().font().color();

                    float labelX = ctx.margin() + ACCENT_BAR_WIDTH + 2f + pair * pairWidth;
                    float lineY = ctx.y() - (line + 1) * DETAIL_ROW_HEIGHT + 3f;
                    float labelW = labelFont.getStringWidth(label) / 1000f * labelFs;

                    ctx.stream().beginText();
                    ctx.stream().setFont(labelFont, labelFs);
                    ctx.stream().setNonStrokingColor(ts.detailRowLabelStyle().font().color());
                    ctx.stream().newLineAtOffset(labelX, lineY);
                    ctx.stream().showText(label);
                    ctx.stream().endText();

                    float availW = pairWidth - labelW - ACCENT_BAR_WIDTH - 6f;
                    PdfTextHelper.drawText(ctx.stream(), value, valueFont, valueFs,
                            labelX + labelW, lineY, availW, 0f, Alignment.LEFT, valColor);
                }

                ctx.setY(ctx.y() - detailHeight - DETAIL_GAP);
            }
        }

        // Summary row
        if (section.showSummaryRow() && section.summaryValues() != null) {
            ctx.moveY(-4f);
            ctx.ensureSpace(MAIN_ROW_HEIGHT + 6f);

            ctx.stream().setStrokingColor(ts.headerStyle().backgroundColor() != null
                    ? ts.headerStyle().backgroundColor() : Color.DARK_GRAY);
            ctx.stream().setLineWidth(1f);
            ctx.stream().moveTo(ctx.margin(), ctx.y());
            ctx.stream().lineTo(ctx.margin() + layout.tableWidth(), ctx.y());
            ctx.stream().stroke();
            ctx.moveY(-2f);

            PDType1Font boldFont = PdfPageContext.resolveFont(true, false);
            float sumCellX = ctx.margin();
            float sumTextY = ctx.y() - MAIN_ROW_HEIGHT + 4f;

            for (int c = 0; c < mainCols.size(); c++) {
                ColumnDef col = mainCols.get(c);
                Object val = section.summaryValues().get(col.key());
                String text;
                if (c == 0 && val == null) {
                    text = "TOTALS";
                } else {
                    text = FormatUtil.format(val, col, curr, datePat);
                }
                Color color = (FormatUtil.shouldRedIfNegative(col.type()) && FormatUtil.isNegative(val))
                        ? RED : Color.BLACK;
                Alignment align = c == 0 && val == null ? Alignment.LEFT : col.effectiveAlignment();
                PdfTextHelper.drawText(ctx.stream(), text, boldFont, 8f,
                        sumCellX, sumTextY, colWidths[c], 3f, align, color);
                sumCellX += colWidths[c];
            }
            ctx.setY(ctx.y() - MAIN_ROW_HEIGHT);
        }

        ctx.moveY(-section.marginBottom());
    }

    private void drawHeader(PdfPageContext ctx, List<ColumnDef> mainCols,
                            float[] colWidths, TableStyle ts, float tableWidth) throws IOException {
        var hs = ts.headerStyle();
        ctx.stream().setNonStrokingColor(hs.backgroundColor() != null ? hs.backgroundColor() : Color.DARK_GRAY);
        ctx.stream().addRect(ctx.margin(), ctx.y() - HEADER_HEIGHT, tableWidth, HEADER_HEIGHT);
        ctx.stream().fill();

        PDType1Font hFont = PdfPageContext.resolveFont(hs.font());
        float cellX = ctx.margin();
        float textY = ctx.y() - HEADER_HEIGHT + 6f;
        for (int c = 0; c < mainCols.size(); c++) {
            ColumnDef col = mainCols.get(c);
            PdfTextHelper.drawText(ctx.stream(), col.label(), hFont, hs.font().fontSize(),
                    cellX, textY, colWidths[c], hs.paddingH(), col.effectiveAlignment(),
                    hs.font().color());
            cellX += colWidths[c];
        }
        ctx.setY(ctx.y() - HEADER_HEIGHT);
    }

    private void drawDetailSubtitle(PdfPageContext ctx, List<ColumnDef> detailCols,
                                    float tableWidth) throws IOException {
        var sb = new StringBuilder("> Details: ");
        for (int i = 0; i < detailCols.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(detailCols.get(i).label());
        }
        PDType1Font font = PdfPageContext.resolveFont(false, true);
        float fs = 6f;
        ctx.stream().beginText();
        ctx.stream().setFont(font, fs);
        ctx.stream().setNonStrokingColor(new Color(160, 160, 160));
        ctx.stream().newLineAtOffset(ctx.margin() + 3f, ctx.y() - SUBTITLE_HEIGHT + 3f);
        ctx.stream().showText(PdfTextHelper.truncate(sb.toString(), tableWidth - 6f, font, fs));
        ctx.stream().endText();
        ctx.setY(ctx.y() - SUBTITLE_HEIGHT);
    }
}
