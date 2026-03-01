package com.fastreport.layout;

import com.fastreport.model.column.ColumnDef;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides which columns render as table headers (main) and which overflow
 * into detail rows, based on available page width.
 */
public class ResponsiveTableLayout {

    private static final float MIN_USEFUL_WIDTH = 60f;
    private static final float WIDE_TABLE_THRESHOLD = 450f;
    private static final int MIN_MAIN_COLUMNS = 2;

    private final List<ColumnDef> mainColumns;
    private final List<ColumnDef> detailColumns;
    private final float[] mainColWidths;
    private final float tableWidth;

    /**
     * Computes the responsive layout.
     *
     * @param requestedColumns all columns in user-specified order
     * @param usableWidth      available width in PDF points
     * @param baseColumnKeys   keys of columns preferred for the table header
     */
    public ResponsiveTableLayout(List<ColumnDef> requestedColumns, float usableWidth,
                                 List<String> baseColumnKeys) {
        if (usableWidth / requestedColumns.size() >= MIN_USEFUL_WIDTH) {
            this.mainColumns = new ArrayList<>(requestedColumns);
            this.detailColumns = List.of();
        } else {
            var main = new ArrayList<ColumnDef>();
            var detail = new ArrayList<ColumnDef>();
            for (ColumnDef col : requestedColumns) {
                if (baseColumnKeys.contains(col.key())) {
                    main.add(col);
                } else {
                    detail.add(col);
                }
            }
            while (main.size() > MIN_MAIN_COLUMNS && usableWidth / main.size() < MIN_USEFUL_WIDTH) {
                detail.add(main.removeLast());
            }
            this.mainColumns = List.copyOf(main);
            this.detailColumns = List.copyOf(detail);
        }
        this.tableWidth = usableWidth;
        this.mainColWidths = distributeWidths(mainColumns, usableWidth);
    }

    public boolean hasDetailRows() { return !detailColumns.isEmpty(); }
    public List<ColumnDef> mainColumns() { return mainColumns; }
    public List<ColumnDef> detailColumns() { return detailColumns; }
    public float[] mainColWidths() { return mainColWidths; }
    public float tableWidth() { return tableWidth; }
    public int detailColumnsPerRow() { return tableWidth > WIDE_TABLE_THRESHOLD ? 2 : 1; }

    private static float[] distributeWidths(List<ColumnDef> columns, float totalWidth) {
        int n = columns.size();
        float[] widths = new float[n];
        float totalHint = 0f;
        for (ColumnDef col : columns) totalHint += col.widthWeight();

        float allocated = 0f;
        for (int i = 0; i < n; i++) {
            widths[i] = (columns.get(i).widthWeight() / totalHint) * totalWidth;
            if (widths[i] < MIN_USEFUL_WIDTH) widths[i] = MIN_USEFUL_WIDTH;
            allocated += widths[i];
        }
        if (allocated > 0 && Math.abs(allocated - totalWidth) > 0.01f) {
            float scale = totalWidth / allocated;
            for (int i = 0; i < n; i++) widths[i] *= scale;
        }
        return widths;
    }
}
