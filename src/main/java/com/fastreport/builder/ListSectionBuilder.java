package com.fastreport.builder;

import com.fastreport.model.column.ColumnDef;
import com.fastreport.model.column.ColumnType;
import com.fastreport.model.section.ListSection;
import com.fastreport.model.style.Alignment;
import com.fastreport.model.style.TableStyle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder for ListSection (table with responsive overflow).
 */
public class ListSectionBuilder {

    private final ReportBuilder parent;
    private String sectionTitle;
    private final List<ColumnDef> columns = new ArrayList<>();
    private final List<String> baseColumnKeys = new ArrayList<>();
    private Iterable<Map<String, Object>> rows;
    private boolean showSummaryRow;
    private Map<String, Object> summaryValues;
    private TableStyle tableStyle;
    private float marginTop = 8f;
    private float marginBottom = 8f;

    ListSectionBuilder(ReportBuilder parent) {
        this.parent = parent;
    }

    public ListSectionBuilder title(String title) {
        this.sectionTitle = title;
        return this;
    }

    /** Adds a column with default widthWeight=1.0 and baseColumn=false. */
    public ListSectionBuilder column(String key, String label, ColumnType type) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type).build());
        return this;
    }

    /** Adds a column with custom width weight. */
    public ListSectionBuilder column(String key, String label, ColumnType type, float widthWeight) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type).widthWeight(widthWeight).build());
        return this;
    }

    /** Adds a column with custom width weight and explicit alignment. */
    public ListSectionBuilder column(String key, String label, ColumnType type, float widthWeight, Alignment alignment) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type)
                .widthWeight(widthWeight).alignment(alignment).build());
        return this;
    }

    /** Adds a column with wrap disabled (text truncated instead of wrapped). */
    public ListSectionBuilder columnNoWrap(String key, String label, ColumnType type) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type).wrapText(false).build());
        return this;
    }

    /** Adds a column with wrap disabled and custom width weight. */
    public ListSectionBuilder columnNoWrap(String key, String label, ColumnType type, float widthWeight) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type)
                .widthWeight(widthWeight).wrapText(false).build());
        return this;
    }

    /** Adds a column and marks it as a base column (preferred for table header). */
    public ListSectionBuilder baseColumn(String key, String label, ColumnType type) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type).baseColumn(true).build());
        baseColumnKeys.add(key);
        return this;
    }

    /** Adds a base column with custom width weight. */
    public ListSectionBuilder baseColumn(String key, String label, ColumnType type, float widthWeight) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type)
                .widthWeight(widthWeight).baseColumn(true).build());
        baseColumnKeys.add(key);
        return this;
    }

    /** Adds a base column with custom width weight and explicit alignment. */
    public ListSectionBuilder baseColumn(String key, String label, ColumnType type, float widthWeight, Alignment alignment) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type)
                .widthWeight(widthWeight).alignment(alignment).baseColumn(true).build());
        baseColumnKeys.add(key);
        return this;
    }

    /** Adds a base column with wrap disabled. */
    public ListSectionBuilder baseColumnNoWrap(String key, String label, ColumnType type) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type)
                .baseColumn(true).wrapText(false).build());
        baseColumnKeys.add(key);
        return this;
    }

    /** Adds a base column with wrap disabled and custom width weight. */
    public ListSectionBuilder baseColumnNoWrap(String key, String label, ColumnType type, float widthWeight) {
        columns.add(ColumnDef.builder().key(key).label(label).type(type)
                .widthWeight(widthWeight).baseColumn(true).wrapText(false).build());
        baseColumnKeys.add(key);
        return this;
    }

    /** Adds a pre-built column definition. */
    public ListSectionBuilder column(ColumnDef columnDef) {
        columns.add(columnDef);
        if (columnDef.baseColumn()) baseColumnKeys.add(columnDef.key());
        return this;
    }

    public ListSectionBuilder rows(Iterable<Map<String, Object>> rows) {
        this.rows = rows;
        return this;
    }

    public ListSectionBuilder rows(List<Map<String, Object>> rows) {
        this.rows = rows;
        return this;
    }

    /** Accepts a single-pass Iterator (e.g. a DB cursor) — streamed without loading all into memory. */
    public ListSectionBuilder rows(Iterator<Map<String, Object>> iterator) {
        this.rows = () -> iterator;
        return this;
    }

    public ListSectionBuilder showSummaryRow(boolean show) {
        this.showSummaryRow = show;
        return this;
    }

    public ListSectionBuilder summaryValues(Map<String, Object> values) {
        this.summaryValues = values;
        this.showSummaryRow = true;
        return this;
    }

    public ListSectionBuilder tableStyle(TableStyle style) {
        this.tableStyle = style;
        return this;
    }

    public ListSectionBuilder marginTop(float mt) {
        this.marginTop = mt;
        return this;
    }

    public ListSectionBuilder marginBottom(float mb) {
        this.marginBottom = mb;
        return this;
    }

    public ReportBuilder end() {
        return parent.addSection(ListSection.builder()
                .sectionTitle(sectionTitle)
                .columns(List.copyOf(columns))
                .baseColumnKeys(baseColumnKeys.isEmpty() ? null : List.copyOf(baseColumnKeys))
                .rows(rows)
                .showSummaryRow(showSummaryRow)
                .summaryValues(summaryValues)
                .tableStyle(tableStyle)
                .marginTop(marginTop)
                .marginBottom(marginBottom)
                .build());
    }
}
