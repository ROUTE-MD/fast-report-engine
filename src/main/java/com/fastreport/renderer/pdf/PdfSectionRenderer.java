package com.fastreport.renderer.pdf;

import com.fastreport.model.section.ReportSection;

import java.io.IOException;

/** Interface for PDF section renderers. */
public interface PdfSectionRenderer<T extends ReportSection> {
    void render(T section, PdfPageContext ctx) throws IOException;
}
