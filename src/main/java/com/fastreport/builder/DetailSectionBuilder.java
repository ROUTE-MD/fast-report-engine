package com.fastreport.builder;

import com.fastreport.model.column.ColumnType;
import com.fastreport.model.content.DetailField;
import com.fastreport.model.section.DetailSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for DetailSection (key-value grid).
 */
public class DetailSectionBuilder {

    private final ReportBuilder parent;
    private String sectionTitle;
    private int columns = 2;
    private float marginTop = 8f;
    private float marginBottom = 8f;
    private final List<DetailField> fields = new ArrayList<>();

    DetailSectionBuilder(ReportBuilder parent) {
        this.parent = parent;
    }

    public DetailSectionBuilder title(String title) {
        this.sectionTitle = title;
        return this;
    }

    public DetailSectionBuilder columns(int columns) {
        this.columns = columns;
        return this;
    }

    public DetailSectionBuilder field(String label, Object value) {
        this.fields.add(new DetailField(label, value));
        return this;
    }

    public DetailSectionBuilder field(String label, Object value, ColumnType type) {
        this.fields.add(new DetailField(label, value, type));
        return this;
    }

    public DetailSectionBuilder marginTop(float mt) {
        this.marginTop = mt;
        return this;
    }

    public DetailSectionBuilder marginBottom(float mb) {
        this.marginBottom = mb;
        return this;
    }

    public ReportBuilder end() {
        return parent.addSection(DetailSection.builder()
                .sectionTitle(sectionTitle)
                .fields(List.copyOf(fields))
                .columns(columns)
                .marginTop(marginTop)
                .marginBottom(marginBottom)
                .build());
    }
}
