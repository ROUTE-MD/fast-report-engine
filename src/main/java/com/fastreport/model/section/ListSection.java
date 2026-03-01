package com.fastreport.model.section;

import com.fastreport.model.column.ColumnDef;
import com.fastreport.model.style.TableStyle;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * A table section with columns, streaming rows, optional summary,
 * and responsive overflow of non-base columns into detail rows.
 */
@Builder(toBuilder = true)
public record ListSection(
        String sectionTitle,
        List<ColumnDef> columns,
        List<String> baseColumnKeys,
        Iterable<Map<String, Object>> rows,
        boolean showSummaryRow,
        Map<String, Object> summaryValues,
        TableStyle tableStyle,
        float marginTop,
        float marginBottom,
        float maxWeight
) implements ReportSection {

    public static class ListSectionBuilder {
        private float marginTop = 8f;
        private float marginBottom = 8f;
        private float maxWeight = 0f;
    }
}
