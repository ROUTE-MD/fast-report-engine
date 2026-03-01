package com.fastreport.renderer.xlsx;

import com.fastreport.model.section.ReportSection;

import java.io.IOException;

/** Interface for XLSX section renderers. */
public interface XlsxSectionRenderer<T extends ReportSection> {
    void render(T section, XlsxRowContext ctx) throws IOException;
}
