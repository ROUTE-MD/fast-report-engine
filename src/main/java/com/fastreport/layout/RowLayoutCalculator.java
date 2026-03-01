package com.fastreport.layout;

import com.fastreport.model.column.ColumnDef;
import com.fastreport.model.style.Alignment;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Measures row heights considering word-wrapped cell content.
 */
public final class RowLayoutCalculator {

    /** Layout of a single cell: wrapped lines and rendering info. */
    public record CellLayout(List<String> lines, float cellWidth, Alignment align, Color color,
                             PDFont font, float fontSize) {
        /** Content height given a line height multiplier. */
        public float contentHeight(float lineHeight) { return lines.size() * lineHeight; }
    }

    /** Layout of a complete row: all cell layouts and resolved height. */
    public record RowLayout(List<CellLayout> cells, float rowHeight) {}

    private static final float LINE_HEIGHT_FACTOR = 1.3f;

    private RowLayoutCalculator() {}

    /**
     * Calculates the layout for a data row, word-wrapping cells as needed.
     *
     * @param row       the data row
     * @param columns   main columns
     * @param colWidths column widths in points
     * @param font      the font
     * @param fontSize  the font size
     * @param paddingH  horizontal padding
     * @param paddingV  vertical padding
     * @param formatter value formatter
     * @return the row layout with resolved height
     */
    public static RowLayout calculate(Map<String, Object> row, List<ColumnDef> columns,
                                      float[] colWidths, PDFont font, float fontSize,
                                      float paddingH, float paddingV,
                                      java.util.function.BiFunction<Object, ColumnDef, String> formatter) throws IOException {
        float lineHeight = fontSize * LINE_HEIGHT_FACTOR;
        var cells = new ArrayList<CellLayout>(columns.size());
        float maxContentHeight = lineHeight; // minimum one line

        for (int i = 0; i < columns.size(); i++) {
            ColumnDef col = columns.get(i);
            Object value = row.get(col.key());
            String text = formatter.apply(value, col);
            float usable = colWidths[i] - 2 * paddingH;
            List<String> lines = col.wrapText()
                    ? TextWrapper.wrap(text, usable, font, fontSize)
                    : List.of(text != null && !text.isEmpty() ? text : "");
            Color color = Color.BLACK;
            cells.add(new CellLayout(lines, colWidths[i], col.effectiveAlignment(), color, font, fontSize));
            float h = lines.size() * lineHeight;
            if (h > maxContentHeight) maxContentHeight = h;
        }

        float rowHeight = maxContentHeight + 2 * paddingV;
        return new RowLayout(cells, rowHeight);
    }
}
