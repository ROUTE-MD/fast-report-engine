package com.fastreport.renderer.xlsx;

import com.fastreport.model.column.ColumnDef;
import com.fastreport.model.column.ColumnType;
import com.fastreport.model.section.ListSection;
import org.dhatim.fastexcel.BorderSide;
import org.dhatim.fastexcel.BorderStyle;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Renders a ListSection (table) in XLSX. All columns shown as headers (Excel has infinite scroll). */
public class XlsxListRenderer implements XlsxSectionRenderer<ListSection> {

    private static final String HEADER_BG = "2C3E50";
    private static final String HEADER_FG = "FFFFFF";
    private static final String ALT_ROW_BG = "F5F5F5";
    private static final String RED = "CC0000";

    @Override
    public void render(ListSection section, XlsxRowContext ctx) throws IOException {
        var ws = ctx.worksheet();
        List<ColumnDef> allCols = section.columns();
        String curr = ctx.theme().currencySymbol();
        String datePat = ctx.theme().datePattern();

        // Section title — merged across all columns
        if (section.sectionTitle() != null) {
            int row = ctx.nextRow();
            ws.value(row, 0, section.sectionTitle());
            ctx.mergeRow(row);
            ws.style(row, 0).bold().fontSize(11)
                    .fontColor(colorHex(ctx.theme().subtitleStyle().color())).set();
        }

        // Column widths — factor in widthWeight for proportional sizing
        float totalWeight = 0f;
        for (ColumnDef col : allCols) totalWeight += col.widthWeight();
        int baseWidth = 14;
        for (int c = 0; c < allCols.size(); c++) {
            ColumnDef col = allCols.get(c);
            int w;
            if (col.excelWidth() > 0) {
                w = col.excelWidth();
            } else {
                int estimated = XlsxFormatUtil.estimateWidth(col.type(), col.label());
                int weighted = (int) (baseWidth * (col.widthWeight() / totalWeight) * allCols.size());
                w = Math.max(estimated, weighted);
            }
            ws.width(c, w);
        }

        // Header row
        int headerRow = ctx.nextRow();
        for (int c = 0; c < allCols.size(); c++) {
            ws.value(headerRow, c, allCols.get(c).label());
            ws.style(headerRow, c).bold().fontSize(10)
                    .fontColor(HEADER_FG).fillColor(HEADER_BG)
                    .horizontalAlignment("center")
                    .borderStyle(BorderSide.BOTTOM, BorderStyle.THIN).set();
        }

        // Data rows
        int rowCount = 0;
        for (Map<String, Object> data : section.rows()) {
            rowCount++;
            int row = ctx.nextRow();
            boolean isAlt = rowCount % 2 == 0;

            for (int c = 0; c < allCols.size(); c++) {
                ColumnDef col = allCols.get(c);
                Object val = data.get(col.key());

                XlsxFormatUtil.writeTypedValue(ws, row, c, val, col.type(), curr, datePat);

                var style = ws.style(row, c).fontSize(10)
                        .horizontalAlignment(XlsxFormatUtil.excelAlignment(col))
                        .verticalAlignment(XlsxFormatUtil.excelVerticalAlignment(col));

                if (isAlt) style.fillColor(ALT_ROW_BG);
                if (col.wrapText()) style.wrapText(true);

                if ((col.type() == ColumnType.CURRENCY || col.type() == ColumnType.DECIMAL)
                        && isNegative(val)) {
                    style.fontColor(RED);
                }
                style.set();
            }
        }

        // Summary row
        if (section.showSummaryRow() && section.summaryValues() != null) {
            int row = ctx.nextRow();
            ws.value(row, 0, "TOTALS");
            ws.style(row, 0).bold().fontSize(10)
                    .borderStyle(BorderSide.TOP, BorderStyle.MEDIUM).set();

            for (int c = 1; c < allCols.size(); c++) {
                ws.style(row, c).borderStyle(BorderSide.TOP, BorderStyle.MEDIUM).set();
            }

            for (int c = 0; c < allCols.size(); c++) {
                ColumnDef col = allCols.get(c);
                Object val = section.summaryValues().get(col.key());
                if (val != null) {
                    XlsxFormatUtil.writeTypedValue(ws, row, c, val, col.type(), curr, datePat);
                    var style = ws.style(row, c).bold().fontSize(10)
                            .horizontalAlignment(XlsxFormatUtil.excelAlignment(col))
                            .borderStyle(BorderSide.TOP, BorderStyle.MEDIUM);
                    if ((col.type() == ColumnType.CURRENCY || col.type() == ColumnType.DECIMAL)
                            && isNegative(val)) {
                        style.fontColor(RED);
                    }
                    style.set();
                }
            }
        }

        ctx.nextRow(); // blank separator
    }

    private boolean isNegative(Object val) {
        if (val instanceof BigDecimal bd) return bd.signum() < 0;
        if (val instanceof Number n) return n.doubleValue() < 0;
        return false;
    }

    private String colorHex(java.awt.Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
