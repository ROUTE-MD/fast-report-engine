package com.fastreport.renderer.xlsx;

import com.fastreport.model.ReportTheme;
import org.dhatim.fastexcel.Worksheet;

/** Mutable state tracker for XLSX rendering: current worksheet and row index. */
public class XlsxRowContext {

    private Worksheet worksheet;
    private int currentRow;
    private final ReportTheme theme;
    private final int columnCount;

    public XlsxRowContext(Worksheet worksheet, ReportTheme theme, int columnCount) {
        this.worksheet = worksheet;
        this.currentRow = 0;
        this.theme = theme;
        this.columnCount = columnCount;
    }

    /** Returns the current row index and advances to the next. */
    public int nextRow() { return currentRow++; }

    /** Skips n rows and returns the new position. */
    public int skipRows(int n) { currentRow += n; return currentRow; }

    /** Merges cells across all columns in the given row. */
    public void mergeRow(int row) {
        if (columnCount > 1) {
            worksheet.range(row, 0, row, columnCount - 1).merge();
        }
    }

    public int currentRow() { return currentRow; }
    public int columnCount() { return columnCount; }
    public Worksheet worksheet() { return worksheet; }
    public ReportTheme theme() { return theme; }
    public void setWorksheet(Worksheet ws) { this.worksheet = ws; }
}
