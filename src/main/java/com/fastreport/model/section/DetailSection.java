package com.fastreport.model.section;

import com.fastreport.model.content.DetailField;
import lombok.Builder;

import java.util.List;

/**
 * A section displaying key-value pairs in a 1, 2, or 3-column grid layout.
 */
@Builder(toBuilder = true)
public record DetailSection(
        String sectionTitle,
        List<DetailField> fields,
        int columns,
        float marginTop,
        float marginBottom
) implements ReportSection {

    public static class DetailSectionBuilder {
        private int columns = 2;
        private float marginTop = 8f;
        private float marginBottom = 8f;
    }
}
