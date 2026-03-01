package com.fastreport.renderer.xlsx;

import com.fastreport.model.ReportDefinition;
import com.fastreport.model.section.*;
import com.fastreport.model.style.FontStyle;
import com.fastreport.renderer.ReportRenderer;
import org.dhatim.fastexcel.Workbook;

import java.io.IOException;
import java.io.OutputStream;

/** Main XLSX orchestrator: renders a ReportDefinition to XLSX via FastExcel. */
public class XlsxRenderer implements ReportRenderer {

    private final XlsxFullWidthRowRenderer fullWidthRenderer = new XlsxFullWidthRowRenderer();
    private final XlsxDetailRenderer detailRenderer = new XlsxDetailRenderer();
    private final XlsxListRenderer listRenderer = new XlsxListRenderer();

    @Override
    public void render(ReportDefinition report, OutputStream out) throws IOException {
        try (var wb = new Workbook(out, "FastReportEngine", "1.0")) {
            var ws = wb.newWorksheet("Report");
            var ctx = new XlsxRowContext(ws, report.getTheme());

            // Title
            int titleRow = ctx.nextRow();
            ws.value(titleRow, 0, report.getTitle());
            FontStyle titleFs = report.getTheme().titleStyle();
            ws.style(titleRow, 0).bold().fontSize((int) titleFs.fontSize())
                    .fontColor(colorHex(titleFs.color())).set();

            // Metadata
            if (report.getMetadata() != null) {
                FontStyle labelFs = report.getTheme().metadataLabelStyle();
                for (var entry : report.getMetadata().entrySet()) {
                    int row = ctx.nextRow();
                    ws.value(row, 0, entry.getKey() + ":");
                    ws.style(row, 0).bold().fontSize((int) labelFs.fontSize())
                            .fontColor(colorHex(labelFs.color())).set();
                    ws.value(row, 1, entry.getValue());
                    ws.style(row, 1).fontSize((int) labelFs.fontSize())
                            .fontColor(colorHex(report.getTheme().metadataValueStyle().color())).set();
                }
            }

            ctx.nextRow(); // blank separator

            // Sections
            if (report.getSections() != null) {
                for (ReportSection section : report.getSections()) {
                    switch (section) {
                        case FullWidthRow fwr -> fullWidthRenderer.render(fwr, ctx);
                        case DetailSection ds -> detailRenderer.render(ds, ctx);
                        case ListSection ls -> listRenderer.render(ls, ctx);
                    }
                }
            }
        }
    }

    private String colorHex(java.awt.Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
