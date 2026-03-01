package com.fastreport.renderer.pdf;

import com.fastreport.layout.ResponsiveTableLayout;
import com.fastreport.layout.RowLayoutCalculator;
import com.fastreport.layout.TextWrapper;
import com.fastreport.model.column.ColumnDef;
import com.fastreport.model.section.ListSection;
import com.fastreport.model.style.Alignment;
import com.fastreport.model.style.FontStyle;
import com.fastreport.model.style.TableStyle;
import com.fastreport.model.style.VerticalAlignment;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Renders a ListSection (table with responsive overflow) in PDF. */
public class PdfListRenderer implements PdfSectionRenderer<ListSection> {

    private static final float HEADER_HEIGHT = 18f;
    private static final float MIN_ROW_HEIGHT = 14f;
    private static final float DETAIL_ROW_HEIGHT = 12f;
    private static final float DETAIL_GAP = 2f;
    private static final float SUBTITLE_HEIGHT = 10f;
    private static final float ACCENT_BAR_WIDTH = 2.5f;
    private static final float LINE_HEIGHT_FACTOR = 1.3f;
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
                section.baseColumnKeys() != null ? section.baseColumnKeys() : List.of(),
                section.maxWeight());

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
        PDType1Font dataFont = PdfPageContext.resolveFont(ts.rowStyle().font());
        float fontSize = ts.rowStyle().font().fontSize();
        float paddingH = ts.rowStyle().paddingH();
        float paddingV = ts.rowStyle().paddingV();
        float lineHeight = fontSize * LINE_HEIGHT_FACTOR;

        // Detail row fonts (constant across rows, resolved once)
        PDType1Font detailLabelFont = null, detailValueFont = null;
        float detailLabelFs = 0f, detailValueFs = 0f, detailLineHeight = 0f;
        float pairWidth = 0f;
        if (hasDetail) {
            detailLabelFont = PdfPageContext.resolveFont(ts.detailRowLabelStyle().font());
            detailValueFont = PdfPageContext.resolveFont(ts.detailRowValueStyle().font());
            detailLabelFs = ts.detailRowLabelStyle().font().fontSize();
            detailValueFs = ts.detailRowValueStyle().font().fontSize();
            detailLineHeight = detailValueFs * LINE_HEIGHT_FACTOR;
            pairWidth = layout.tableWidth() / detailPerRow;
        }

        int rowCount = 0;
        for (Map<String, Object> row : section.rows()) {
            rowCount++;
            boolean isAlt = rowCount % 2 == 0;

            // Calculate wrapped layout for this row
            RowLayoutCalculator.RowLayout rowLayout = RowLayoutCalculator.calculate(
                    row, mainCols, colWidths, dataFont, fontSize, paddingH, paddingV,
                    (val, col) -> FormatUtil.format(val, col, curr, datePat));

            float mainRowHeight = Math.max(MIN_ROW_HEIGHT, rowLayout.rowHeight());

            // Pre-compute detail grid positions (fullWidth columns get their own row)
            float detailTotalHeight = 0f;
            List<List<String>> detailWrappedLines = null;
            List<Float> detailLabelWidths = null;
            List<String> detailLabels = null;
            List<Color> detailColors = null;
            float[] gridRowHeights = null;
            int[] detailGridRow = null;
            int[] detailPairPos = null;
            int gridRowCount = 0;

            if (hasDetail) {
                detailGridRow = new int[detailCols.size()];
                detailPairPos = new int[detailCols.size()];
                int curGridRow = 0, curPairPos = 0;

                for (int d = 0; d < detailCols.size(); d++) {
                    if (detailCols.get(d).fullWidth()) {
                        if (curPairPos > 0) { curGridRow++; curPairPos = 0; }
                        detailGridRow[d] = curGridRow;
                        detailPairPos[d] = 0;
                        curGridRow++;
                    } else {
                        detailGridRow[d] = curGridRow;
                        detailPairPos[d] = curPairPos;
                        curPairPos++;
                        if (curPairPos >= detailPerRow) { curGridRow++; curPairPos = 0; }
                    }
                }
                gridRowCount = curPairPos > 0 ? curGridRow + 1 : curGridRow;

                gridRowHeights = new float[gridRowCount];
                detailWrappedLines = new ArrayList<>(detailCols.size());
                detailLabelWidths = new ArrayList<>(detailCols.size());
                detailLabels = new ArrayList<>(detailCols.size());
                detailColors = new ArrayList<>(detailCols.size());

                float fullWidth = layout.tableWidth();
                for (int d = 0; d < detailCols.size(); d++) {
                    ColumnDef dc = detailCols.get(d);
                    Object val = row.get(dc.key());
                    String label = dc.label() + ": ";
                    String value = FormatUtil.format(val, dc, curr, datePat);
                    float labelW = detailLabelFont.getStringWidth(label) / 1000f * detailLabelFs;
                    float colWidth = dc.fullWidth() ? fullWidth : pairWidth;
                    float availW = colWidth - labelW - ACCENT_BAR_WIDTH - 6f;

                    List<String> lines;
                    if (dc.wrapText() && availW > 0) {
                        lines = TextWrapper.wrap(value, availW, detailValueFont, detailValueFs);
                    } else {
                        lines = List.of(value != null && !value.isEmpty() ? value : "");
                    }

                    detailWrappedLines.add(lines);
                    detailLabelWidths.add(labelW);
                    detailLabels.add(label);
                    Color valColor = (FormatUtil.shouldRedIfNegative(dc.type()) && FormatUtil.isNegative(val))
                            ? RED : ts.detailRowValueStyle().font().color();
                    detailColors.add(valColor);

                    int gr = detailGridRow[d];
                    float h = Math.max(DETAIL_ROW_HEIGHT, lines.size() * detailLineHeight + 2f);
                    if (h > gridRowHeights[gr]) gridRowHeights[gr] = h;
                }

                for (float h : gridRowHeights) detailTotalHeight += h;
            }

            float recordHeight = mainRowHeight;
            if (hasDetail) {
                recordHeight += 2 * DETAIL_GAP + detailTotalHeight;
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
                ctx.stream().addRect(ctx.margin(), y - mainRowHeight, layout.tableWidth(), mainRowHeight);
                ctx.stream().fill();
            }

            // Draw each cell (possibly multi-line) with vertical alignment
            float cellX = ctx.margin();
            for (int c = 0; c < mainCols.size(); c++) {
                ColumnDef col = mainCols.get(c);
                Object val = row.get(col.key());
                Color color = (FormatUtil.shouldRedIfNegative(col.type()) && FormatUtil.isNegative(val))
                        ? RED : rowStyle.font().color();

                RowLayoutCalculator.CellLayout cell = rowLayout.cells().get(c);
                List<String> lines = cell.lines();
                int lineCount = lines.size();
                float contentHeight = lineCount * lineHeight;

                // Compute vertical offset based on alignment
                VerticalAlignment vAlign = col.effectiveVerticalAlignment();
                float startY;
                if (lineCount == 1 && !col.wrapText()) {
                    // noWrap single line — truncate
                    String text = FormatUtil.format(val, col, curr, datePat);
                    float textY = vAlignOffset(y, mainRowHeight, fontSize, 1, lineHeight, paddingV, vAlign);
                    PdfTextHelper.drawText(ctx.stream(), text, dataFont, fontSize,
                            cellX, textY, colWidths[c], paddingH, col.effectiveAlignment(), color);
                } else {
                    startY = vAlignOffset(y, mainRowHeight, fontSize, lineCount, lineHeight, paddingV, vAlign);
                    for (int ln = 0; ln < lineCount; ln++) {
                        float lineY = startY - ln * lineHeight;
                        PdfTextHelper.drawText(ctx.stream(), lines.get(ln), dataFont, fontSize,
                                cellX, lineY, colWidths[c], paddingH, col.effectiveAlignment(), color);
                    }
                }
                cellX += colWidths[c];
            }

            // Grid line
            ctx.stream().setStrokingColor(ts.gridColor());
            ctx.stream().setLineWidth(0.3f);
            ctx.stream().moveTo(ctx.margin(), y - mainRowHeight);
            ctx.stream().lineTo(ctx.margin() + layout.tableWidth(), y - mainRowHeight);
            ctx.stream().stroke();

            ctx.setY(y - mainRowHeight);

            // Detail rows (with word-wrapping support)
            if (hasDetail) {
                ctx.moveY(-DETAIL_GAP);

                // Background
                ctx.stream().setNonStrokingColor(ts.detailRowValueStyle().backgroundColor() != null
                        ? ts.detailRowValueStyle().backgroundColor()
                        : new Color(250, 250, 245));
                ctx.stream().addRect(ctx.margin(), ctx.y() - detailTotalHeight, layout.tableWidth(), detailTotalHeight);
                ctx.stream().fill();

                // Accent bar
                ctx.stream().setNonStrokingColor(ts.accentColor());
                ctx.stream().addRect(ctx.margin(), ctx.y() - detailTotalHeight, ACCENT_BAR_WIDTH, detailTotalHeight);
                ctx.stream().fill();

                // Key-value pairs with wrapping (respects fullWidth flag)
                float gridRowYOffset = 0f;
                int prevGridRow = -1;
                for (int d = 0; d < detailCols.size(); d++) {
                    int gr = detailGridRow[d];
                    int pair = detailPairPos[d];
                    boolean isFull = detailCols.get(d).fullWidth();

                    // Accumulate Y offset when entering a new grid row
                    while (prevGridRow < gr - 1) {
                        prevGridRow++;
                        gridRowYOffset += gridRowHeights[prevGridRow];
                    }
                    if (gr != prevGridRow) {
                        if (prevGridRow >= 0) gridRowYOffset += gridRowHeights[prevGridRow];
                        prevGridRow = gr;
                    }

                    float colWidth = isFull ? layout.tableWidth() : pairWidth;
                    float labelX = ctx.margin() + ACCENT_BAR_WIDTH + 2f + pair * colWidth;
                    float firstLineY = ctx.y() - gridRowYOffset - DETAIL_ROW_HEIGHT + 3f;

                    // Draw label on first line
                    ctx.stream().beginText();
                    ctx.stream().setFont(detailLabelFont, detailLabelFs);
                    ctx.stream().setNonStrokingColor(ts.detailRowLabelStyle().font().color());
                    ctx.stream().newLineAtOffset(labelX, firstLineY);
                    ctx.stream().showText(detailLabels.get(d));
                    ctx.stream().endText();

                    // Draw value lines (may be wrapped across multiple lines)
                    List<String> lines = detailWrappedLines.get(d);
                    float labelW = detailLabelWidths.get(d);
                    float availW = colWidth - labelW - ACCENT_BAR_WIDTH - 6f;

                    for (int ln = 0; ln < lines.size(); ln++) {
                        float lineY = firstLineY - ln * detailLineHeight;
                        PdfTextHelper.drawText(ctx.stream(), lines.get(ln), detailValueFont, detailValueFs,
                                labelX + labelW, lineY, availW, 0f, Alignment.LEFT, detailColors.get(d));
                    }
                }

                ctx.setY(ctx.y() - detailTotalHeight - DETAIL_GAP);
            }
        }

        // Summary row
        if (section.showSummaryRow() && section.summaryValues() != null) {
            ctx.moveY(-4f);
            ctx.ensureSpace(MIN_ROW_HEIGHT + 6f);

            ctx.stream().setStrokingColor(ts.headerStyle().backgroundColor() != null
                    ? ts.headerStyle().backgroundColor() : Color.DARK_GRAY);
            ctx.stream().setLineWidth(1f);
            ctx.stream().moveTo(ctx.margin(), ctx.y());
            ctx.stream().lineTo(ctx.margin() + layout.tableWidth(), ctx.y());
            ctx.stream().stroke();
            ctx.moveY(-2f);

            PDType1Font boldFont = PdfPageContext.resolveFont(true, false);
            float sumCellX = ctx.margin();
            float sumTextY = ctx.y() - MIN_ROW_HEIGHT + 4f;

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
            ctx.setY(ctx.y() - MIN_ROW_HEIGHT);
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

    /** Computes the Y position of the first text line based on vertical alignment. */
    private static float vAlignOffset(float cellTop, float cellHeight, float fontSize,
                                      int lineCount, float lineHeight, float paddingV,
                                      VerticalAlignment vAlign) {
        float contentHeight = lineCount * lineHeight;
        return switch (vAlign) {
            case TOP -> cellTop - paddingV - fontSize;
            case BOTTOM -> cellTop - cellHeight + paddingV + contentHeight - fontSize;
            case MIDDLE -> cellTop - (cellHeight - contentHeight) / 2f - fontSize;
        };
    }
}
