package com.fastreport.model.section;

import java.util.LinkedHashMap;

/**
 * A block of key-value metadata pairs rendered in order within the sections pipeline.
 * This allows metadata to be interleaved with separators and other sections.
 *
 * @param entries ordered key-value pairs
 */
public record MetadataBlock(LinkedHashMap<String, String> entries) implements ReportSection {

    public MetadataBlock {
        entries = new LinkedHashMap<>(entries);
    }
}
