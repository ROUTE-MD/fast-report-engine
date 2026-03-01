package com.fastreport.renderer.xlsx;

import com.fastreport.model.ReportTheme;
import org.dhatim.fastexcel.Worksheet;

/** Mutable state tracker for XLSX rendering: current worksheet and row index. */
public class XlsxRowContext {

    private Worksheet worksheet;
    private int currentRow;
    private final ReportTheme theme;

    public XlsxRowContext(Worksheet worksheet, ReportTheme theme) {
        this.worksheet = worksheet;
        this.currentRow = 0;
        this.theme = theme;
    }

    /** Returns the current row index and advances to the next. */
    public int nextRow() { return currentRow++; }

    /** Skips n rows and returns the new position. */
    public int skipRows(int n) { currentRow += n; return currentRow; }

    public int currentRow() { return currentRow; }
    public Worksheet worksheet() { return worksheet; }
    public ReportTheme theme() { return theme; }
    public void setWorksheet(Worksheet ws) { this.worksheet = ws; }
}
