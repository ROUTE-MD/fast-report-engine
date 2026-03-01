package com.fastreport.builder;

import com.fastreport.model.ReportDefinition;
import com.fastreport.model.ReportOrientation;
import com.fastreport.model.ReportTheme;
import com.fastreport.model.section.ReportSection;
import com.fastreport.model.section.SeparatorLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Fluent builder for constructing a ReportDefinition with ordered sections.
 */
public class ReportBuilder {

    private String title;
    private ReportOrientation orientation = ReportOrientation.LANDSCAPE;
    private ReportTheme theme = ReportTheme.defaults();
    private byte[] logo;
    private float logoWidth = 80f;
    private float logoHeight = 40f;
    private final LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
    private final List<ReportSection> sections = new ArrayList<>();

    public ReportBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ReportBuilder orientation(ReportOrientation orientation) {
        this.orientation = orientation;
        return this;
    }

    public ReportBuilder portrait() {
        this.orientation = ReportOrientation.PORTRAIT;
        return this;
    }

    public ReportBuilder landscape() {
        this.orientation = ReportOrientation.LANDSCAPE;
        return this;
    }

    public ReportBuilder theme(ReportTheme theme) {
        this.theme = theme;
        return this;
    }

    public ReportBuilder logo(byte[] logoBytes, float width, float height) {
        this.logo = logoBytes;
        this.logoWidth = width;
        this.logoHeight = height;
        return this;
    }

    public ReportBuilder logo(Path logoFile, float width, float height) throws IOException {
        return logo(Files.readAllBytes(logoFile), width, height);
    }

    public ReportBuilder meta(String key, String value) {
        this.metadata.put(key, value);
        return this;
    }

    public ReportBuilder section(ReportSection section) {
        this.sections.add(section);
        return this;
    }

    public FullWidthRowBuilder fullWidthRow() {
        return new FullWidthRowBuilder(this);
    }

    public DetailSectionBuilder detailSection() {
        return new DetailSectionBuilder(this);
    }

    public ListSectionBuilder listSection() {
        return new ListSectionBuilder(this);
    }

    /** Adds a separator line with default style. */
    public ReportBuilder separator() {
        this.sections.add(SeparatorLine.builder().build());
        return this;
    }

    /** Adds a separator line with custom color. */
    public ReportBuilder separator(java.awt.Color color) {
        this.sections.add(SeparatorLine.builder().color(color).build());
        return this;
    }

    /** Adds a separator line with custom color and thickness. */
    public ReportBuilder separator(java.awt.Color color, float thickness) {
        this.sections.add(SeparatorLine.builder().color(color).thickness(thickness).build());
        return this;
    }

    ReportBuilder addSection(ReportSection section) {
        this.sections.add(section);
        return this;
    }

    public ReportDefinition build() {
        return ReportDefinition.builder()
                .title(title)
                .orientation(orientation)
                .theme(theme)
                .logo(logo)
                .logoWidth(logoWidth)
                .logoHeight(logoHeight)
                .metadata(metadata)
                .sections(List.copyOf(sections))
                .build();
    }
}
