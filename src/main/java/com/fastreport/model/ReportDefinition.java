package com.fastreport.model;

import com.fastreport.model.section.ReportSection;
import com.fastreport.model.style.Alignment;
import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Complete report definition: title, orientation, theme, logo, metadata, and ordered sections.
 */
@Data
@Builder(toBuilder = true)
public class ReportDefinition {
    private String title;

    @Builder.Default
    private Alignment titleAlignment = Alignment.LEFT;

    @Builder.Default
    private ReportOrientation orientation = ReportOrientation.LANDSCAPE;

    @Builder.Default
    private ReportTheme theme = ReportTheme.defaults();

    /** PNG/JPG bytes for the logo, nullable. */
    private byte[] logo;

    @Builder.Default
    private float logoWidth = 80f;

    @Builder.Default
    private float logoHeight = 40f;

    @Builder.Default
    private LinkedHashMap<String, String> metadata = new LinkedHashMap<>();

    private List<ReportSection> sections;
}
