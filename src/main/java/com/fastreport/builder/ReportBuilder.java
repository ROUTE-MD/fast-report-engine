package com.fastreport.builder;

import com.fastreport.model.ReportDefinition;
import com.fastreport.model.ReportOrientation;
import com.fastreport.model.ReportTheme;
import com.fastreport.model.section.MetadataBlock;
import com.fastreport.model.section.ReportSection;
import com.fastreport.model.section.SeparatorLine;
import com.fastreport.model.section.SpacerSection;
import com.fastreport.model.style.Alignment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Fluent builder for constructing a ReportDefinition with ordered sections.
 * Metadata entries are buffered and flushed as a MetadataBlock when a
 * non-meta call (separator, section, build) is made, preserving insertion order.
 */
public class ReportBuilder {

    private String title;
    private Alignment titleAlignment = Alignment.LEFT;
    private ReportOrientation orientation = ReportOrientation.LANDSCAPE;
    private ReportTheme theme = ReportTheme.defaults();
    private byte[] logo;
    private float logoWidth = 80f;
    private float logoHeight = 40f;
    private float titleMarginTop = 0f;
    private final List<ReportSection> sections = new ArrayList<>();
    private LinkedHashMap<String, String> metaBuffer = new LinkedHashMap<>();

    public ReportBuilder title(String title) {
        this.title = title;
        return this;
    }

    public ReportBuilder titleAlignment(Alignment alignment) {
        this.titleAlignment = alignment;
        return this;
    }

    public ReportBuilder titleCenter() {
        this.titleAlignment = Alignment.CENTER;
        return this;
    }

    /** Adds extra vertical space before the title (e.g. gap between logo and title). */
    public ReportBuilder titleMarginTop(float points) {
        this.titleMarginTop = points;
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

    /** Buffers a metadata entry. Flushed as a MetadataBlock when a non-meta call follows. */
    public ReportBuilder meta(String key, String value) {
        metaBuffer.put(key, value);
        return this;
    }

    public ReportBuilder section(ReportSection section) {
        flushMeta();
        this.sections.add(section);
        return this;
    }

    public FullWidthRowBuilder fullWidthRow() {
        flushMeta();
        return new FullWidthRowBuilder(this);
    }

    public DetailSectionBuilder detailSection() {
        flushMeta();
        return new DetailSectionBuilder(this);
    }

    public ListSectionBuilder listSection() {
        flushMeta();
        return new ListSectionBuilder(this);
    }

    /** Adds a separator line with default style. */
    public ReportBuilder separator() {
        flushMeta();
        this.sections.add(SeparatorLine.builder().build());
        return this;
    }

    /** Adds a separator line with custom color. */
    public ReportBuilder separator(java.awt.Color color) {
        flushMeta();
        this.sections.add(SeparatorLine.builder().color(color).build());
        return this;
    }

    /** Adds vertical blank space (number of lines). */
    public ReportBuilder space(int lines) {
        flushMeta();
        this.sections.add(new SpacerSection(lines));
        return this;
    }

    /** Adds a separator line with custom color and thickness. */
    public ReportBuilder separator(java.awt.Color color, float thickness) {
        flushMeta();
        this.sections.add(SeparatorLine.builder().color(color).thickness(thickness).build());
        return this;
    }

    ReportBuilder addSection(ReportSection section) {
        flushMeta();
        this.sections.add(section);
        return this;
    }

    public ReportDefinition build() {
        flushMeta();
        return ReportDefinition.builder()
                .title(title)
                .titleAlignment(titleAlignment)
                .orientation(orientation)
                .theme(theme)
                .logo(logo)
                .logoWidth(logoWidth)
                .logoHeight(logoHeight)
                .titleMarginTop(titleMarginTop)
                .metadata(new LinkedHashMap<>())
                .sections(List.copyOf(sections))
                .build();
    }

    /** Flushes buffered meta entries as a MetadataBlock section. */
    private void flushMeta() {
        if (!metaBuffer.isEmpty()) {
            sections.add(new MetadataBlock(metaBuffer));
            metaBuffer = new LinkedHashMap<>();
        }
    }
}
