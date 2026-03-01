package com.fastreport.renderer.xlsx;

import com.fastreport.model.content.DetailField;
import com.fastreport.model.section.DetailSection;
import com.fastreport.model.style.FontStyle;

import java.io.IOException;

/** Renders a DetailSection (key-value grid) in XLSX. */
public class XlsxDetailRenderer implements XlsxSectionRenderer<DetailSection> {

    private static final String DETAIL_BG = "FFFDE7";
    private static final String LABEL_COLOR = "666666";

    @Override
    public void render(DetailSection section, XlsxRowContext ctx) throws IOException {
        var ws = ctx.worksheet();

        // Section title — merged across all columns
        if (section.sectionTitle() != null) {
            int row = ctx.nextRow();
            ws.value(row, 0, section.sectionTitle());
            ctx.mergeRow(row);
            FontStyle fs = ctx.theme().subtitleStyle();
            ws.style(row, 0).bold().fontSize((int) fs.fontSize())
                    .fontColor(colorHex(fs.color())).set();
        }

        int cols = section.columns();
        var fields = section.fields();
        String curr = ctx.theme().currencySymbol();
        String datePat = ctx.theme().datePattern();

        for (int i = 0; i < fields.size(); i += cols) {
            int row = ctx.nextRow();
            for (int c = 0; c < cols && i + c < fields.size(); c++) {
                DetailField field = fields.get(i + c);
                int labelCol = c * 2;
                int valueCol = labelCol + 1;

                // Label
                ws.value(row, labelCol, field.label() + ":");
                ws.style(row, labelCol).bold().italic()
                        .fontSize(9).fontColor(LABEL_COLOR)
                        .fillColor(DETAIL_BG).set();

                // Value
                XlsxFormatUtil.writeTypedValue(ws, row, valueCol, field.value(),
                        field.type(), curr, datePat);
                ws.style(row, valueCol).fillColor(DETAIL_BG).set();
            }
        }
        ctx.nextRow(); // blank separator
    }

    private String colorHex(java.awt.Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
