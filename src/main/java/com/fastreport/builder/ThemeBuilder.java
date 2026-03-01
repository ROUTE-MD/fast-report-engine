package com.fastreport.builder;

import com.fastreport.model.ReportTheme;
import com.fastreport.model.style.CellStyle;
import com.fastreport.model.style.FontStyle;
import com.fastreport.model.style.TableStyle;

import java.awt.Color;

/**
 * Convenience builder for creating custom ReportTheme instances,
 * starting from the defaults and allowing selective overrides.
 */
public class ThemeBuilder {

    private FontStyle titleStyle;
    private FontStyle subtitleStyle;
    private FontStyle metadataLabelStyle;
    private FontStyle metadataValueStyle;
    private TableStyle tableStyle;
    private CellStyle detailLabelStyle;
    private CellStyle detailValueStyle;
    private Color pageFooterColor;
    private float pageFooterFontSize;
    private String currencySymbol;
    private String datePattern;
    private String dateTimePattern;

    private ThemeBuilder(ReportTheme base) {
        this.titleStyle = base.titleStyle();
        this.subtitleStyle = base.subtitleStyle();
        this.metadataLabelStyle = base.metadataLabelStyle();
        this.metadataValueStyle = base.metadataValueStyle();
        this.tableStyle = base.tableStyle();
        this.detailLabelStyle = base.detailLabelStyle();
        this.detailValueStyle = base.detailValueStyle();
        this.pageFooterColor = base.pageFooterColor();
        this.pageFooterFontSize = base.pageFooterFontSize();
        this.currencySymbol = base.currencySymbol();
        this.datePattern = base.datePattern();
        this.dateTimePattern = base.dateTimePattern();
    }

    /** Creates a new ThemeBuilder starting from default theme. */
    public static ThemeBuilder fromDefaults() {
        return new ThemeBuilder(ReportTheme.defaults());
    }

    /** Creates a new ThemeBuilder starting from an existing theme. */
    public static ThemeBuilder from(ReportTheme theme) {
        return new ThemeBuilder(theme);
    }

    public ThemeBuilder titleStyle(FontStyle style) {
        this.titleStyle = style;
        return this;
    }

    public ThemeBuilder subtitleStyle(FontStyle style) {
        this.subtitleStyle = style;
        return this;
    }

    public ThemeBuilder tableStyle(TableStyle style) {
        this.tableStyle = style;
        return this;
    }

    public ThemeBuilder currencySymbol(String symbol) {
        this.currencySymbol = symbol;
        return this;
    }

    public ThemeBuilder datePattern(String pattern) {
        this.datePattern = pattern;
        return this;
    }

    public ThemeBuilder dateTimePattern(String pattern) {
        this.dateTimePattern = pattern;
        return this;
    }

    public ThemeBuilder pageFooterColor(Color color) {
        this.pageFooterColor = color;
        return this;
    }

    public ThemeBuilder pageFooterFontSize(float size) {
        this.pageFooterFontSize = size;
        return this;
    }

    public ReportTheme build() {
        return new ReportTheme(
                titleStyle, subtitleStyle,
                metadataLabelStyle, metadataValueStyle,
                tableStyle,
                detailLabelStyle, detailValueStyle,
                pageFooterColor, pageFooterFontSize,
                currencySymbol, datePattern, dateTimePattern
        );
    }
}
