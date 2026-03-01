package com.fastreport.builder;

import com.fastreport.model.content.TextContent;
import com.fastreport.model.section.FullWidthRow;
import com.fastreport.model.style.FontStyle;

import java.awt.Color;

/**
 * Fluent builder for FullWidthRow sections.
 */
public class FullWidthRowBuilder {

    private final ReportBuilder parent;
    private TextContent left;
    private TextContent center;
    private TextContent right;
    private float height = FullWidthRow.DEFAULT_HEIGHT;
    private Color backgroundColor;
    private float marginTop;
    private float marginBottom = FullWidthRow.DEFAULT_MARGIN_BOTTOM;

    FullWidthRowBuilder(ReportBuilder parent) {
        this.parent = parent;
    }

    public FullWidthRowBuilder left(String text) {
        this.left = new TextContent(text);
        return this;
    }

    public FullWidthRowBuilder left(String text, FontStyle style) {
        this.left = new TextContent(text, style);
        return this;
    }

    public FullWidthRowBuilder center(String text) {
        this.center = new TextContent(text);
        return this;
    }

    public FullWidthRowBuilder center(String text, FontStyle style) {
        this.center = new TextContent(text, style);
        return this;
    }

    public FullWidthRowBuilder right(String text) {
        this.right = new TextContent(text);
        return this;
    }

    public FullWidthRowBuilder right(String text, FontStyle style) {
        this.right = new TextContent(text, style);
        return this;
    }

    public FullWidthRowBuilder height(float height) {
        this.height = height;
        return this;
    }

    public FullWidthRowBuilder backgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }

    public FullWidthRowBuilder marginTop(float mt) {
        this.marginTop = mt;
        return this;
    }

    public FullWidthRowBuilder marginBottom(float mb) {
        this.marginBottom = mb;
        return this;
    }

    public ReportBuilder end() {
        return parent.addSection(FullWidthRow.builder()
                .left(left).center(center).right(right)
                .height(height).backgroundColor(backgroundColor)
                .marginTop(marginTop).marginBottom(marginBottom)
                .build());
    }
}
