package com.fastreport.renderer.xlsx;

import com.fastreport.model.content.TextContent;
import com.fastreport.model.section.FullWidthRow;
import com.fastreport.model.style.FontStyle;

import java.io.IOException;

/** Renders a FullWidthRow section in XLSX. */
public class XlsxFullWidthRowRenderer implements XlsxSectionRenderer<FullWidthRow> {

    @Override
    public void render(FullWidthRow section, XlsxRowContext ctx) throws IOException {
        var ws = ctx.worksheet();
        int row = ctx.nextRow();

        if (section.left() != null) {
            writeContent(ws, row, 0, section.left(), ctx);
        }
        if (section.center() != null) {
            writeContent(ws, row, 3, section.center(), ctx);
        }
        if (section.right() != null) {
            writeContent(ws, row, 6, section.right(), ctx);
        }
    }

    private void writeContent(org.dhatim.fastexcel.Worksheet ws, int row, int col,
                              TextContent content, XlsxRowContext ctx) throws IOException {
        FontStyle fs = content.style() != null ? content.style() : ctx.theme().subtitleStyle();
        ws.value(row, col, content.text());
        var style = ws.style(row, col).fontSize((int) fs.fontSize());
        if (fs.bold()) style.bold();
        if (fs.italic()) style.italic();
        style.fontColor(colorHex(fs.color())).set();
    }

    private String colorHex(java.awt.Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
