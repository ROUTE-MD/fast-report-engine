package com.fastreport.renderer;

import com.fastreport.model.ReportDefinition;

import java.io.IOException;
import java.io.OutputStream;

/** Renders a report definition to a specific output format. */
public interface ReportRenderer {
    void render(ReportDefinition report, OutputStream out) throws IOException;
}
