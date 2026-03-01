package com.fastreport.model.section;

/**
 * An empty vertical spacer that inserts blank space between sections.
 *
 * @param lines number of blank lines (each ~11pt in PDF, 1 row in XLSX)
 */
public record SpacerSection(int lines) implements ReportSection {

    public SpacerSection {
        if (lines < 1) throw new IllegalArgumentException("lines must be >= 1");
    }
}
