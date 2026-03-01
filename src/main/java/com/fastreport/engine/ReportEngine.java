package com.fastreport.engine;

import com.fastreport.model.ReportDefinition;
import com.fastreport.renderer.pdf.PdfRenderer;
import com.fastreport.renderer.xlsx.XlsxRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Facade for the report engine: convenience methods for rendering to PDF and XLSX.
 */
public class ReportEngine {

    private final PdfRenderer pdfRenderer = new PdfRenderer();
    private final XlsxRenderer xlsxRenderer = new XlsxRenderer();

    /** Renders a report to PDF and writes to the given output stream. */
    public void renderPdf(ReportDefinition report, OutputStream out) throws IOException {
        pdfRenderer.render(report, out);
    }

    /** Renders a report to XLSX and writes to the given output stream. */
    public void renderXlsx(ReportDefinition report, OutputStream out) throws IOException {
        xlsxRenderer.render(report, out);
    }

    /** Renders a report to a PDF file. */
    public void renderPdf(ReportDefinition report, Path path) throws IOException {
        try (var out = Files.newOutputStream(path)) {
            renderPdf(report, out);
        }
    }

    /** Renders a report to an XLSX file. */
    public void renderXlsx(ReportDefinition report, Path path) throws IOException {
        try (var out = Files.newOutputStream(path)) {
            renderXlsx(report, out);
        }
    }
}
