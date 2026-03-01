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
     * @param maxWeight        max total weight for table header columns (0 = use pixel-based logic)
     */
    public ResponsiveTableLayout(List<ColumnDef> requestedColumns, float usableWidth,
                                 List<String> baseColumnKeys, float maxWeight) {
        // Separate inline columns first — they always go to detail rows
        var candidateColumns = new ArrayList<ColumnDef>();
        var forceDetail = new ArrayList<ColumnDef>();
        for (ColumnDef col : requestedColumns) {
            if (col.inline()) {
                forceDetail.add(col);
            } else {
                candidateColumns.add(col);
            }
        }

        if (maxWeight > 0) {
            // Weight-based overflow logic
            var main = new ArrayList<>(candidateColumns);
            var detail = new ArrayList<>(forceDetail);
            float totalWeight = sumWeights(main);

            if (totalWeight > maxWeight) {
                // Remove non-base columns from the end first
                for (int i = main.size() - 1; i >= 0 && totalWeight > maxWeight; i--) {
                    if (!baseColumnKeys.contains(main.get(i).key())) {
                        totalWeight -= main.get(i).widthWeight();
                        detail.add(main.remove(i));
                    }
                }
                // If still over budget, remove base columns from the end (keep minimum 2)
                while (main.size() > MIN_MAIN_COLUMNS && totalWeight > maxWeight) {
                    ColumnDef removed = main.removeLast();
                    totalWeight -= removed.widthWeight();
                    detail.add(removed);
                }
            }

            this.mainColumns = List.copyOf(main);
            this.detailColumns = List.copyOf(detail);
        } else if (!candidateColumns.isEmpty() && usableWidth / candidateColumns.size() >= MIN_USEFUL_WIDTH) {
            this.mainColumns = new ArrayList<>(candidateColumns);
            this.detailColumns = new ArrayList<>(forceDetail);
        } else {
            var main = new ArrayList<ColumnDef>();
            var detail = new ArrayList<ColumnDef>();
            for (ColumnDef col : requestedColumns) {
                if (col.inline() || !baseColumnKeys.contains(col.key())) {
                    detail.add(col);
                } else {
                    main.add(col);
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

    /** Backwards-compatible constructor — uses pixel-based logic (maxWeight=0). */
    public ResponsiveTableLayout(List<ColumnDef> requestedColumns, float usableWidth,
                                 List<String> baseColumnKeys) {
        this(requestedColumns, usableWidth, baseColumnKeys, 0f);
    }

    private static float sumWeights(List<ColumnDef> columns) {
        float total = 0f;
        for (ColumnDef col : columns) total += col.widthWeight();
        return total;
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
