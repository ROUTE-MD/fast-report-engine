package com.fastreport.model;

import com.fastreport.model.style.CellStyle;
import com.fastreport.model.style.FontStyle;
import com.fastreport.model.style.TableStyle;
import lombok.Builder;

import java.awt.Color;

/**
 * Top-level theme configuration for the report, controlling all visual aspects.
 */
@Builder(toBuilder = true)
public record ReportTheme(
        FontStyle titleStyle,
        FontStyle subtitleStyle,
        FontStyle metadataLabelStyle,
        FontStyle metadataValueStyle,
        TableStyle tableStyle,
        CellStyle detailLabelStyle,
        CellStyle detailValueStyle,
        Color pageFooterColor,
        float pageFooterFontSize,
        String currencySymbol,
        String datePattern,
        String dateTimePattern
) {
    private static final Color TITLE_COLOR = new Color(0x1B, 0x3A, 0x5C);
    private static final Color SUBTITLE_COLOR = new Color(0x66, 0x66, 0x66);
    private static final Color META_LABEL_COLOR = new Color(0x88, 0x88, 0x88);
    private static final Color META_VALUE_COLOR = new Color(0x33, 0x33, 0x33);
    private static final Color DETAIL_LABEL_COLOR = new Color(0x66, 0x66, 0x66);
    private static final Color FOOTER_COLOR = new Color(0xAA, 0xAA, 0xAA);

    /** Default theme with sensible banking-style colors and formatting. */
    public static ReportTheme defaults() {
        return ReportTheme.builder()
                .titleStyle(FontStyle.builder().fontSize(16).bold(true).color(TITLE_COLOR).build())
                .subtitleStyle(FontStyle.builder().fontSize(10).color(SUBTITLE_COLOR).build())
                .metadataLabelStyle(FontStyle.builder().fontSize(8).bold(true).color(META_LABEL_COLOR).build())
                .metadataValueStyle(FontStyle.builder().fontSize(8).color(META_VALUE_COLOR).build())
                .tableStyle(TableStyle.defaults())
                .detailLabelStyle(CellStyle.builder()
                        .font(FontStyle.builder().fontSize(8).bold(true).color(DETAIL_LABEL_COLOR).build())
                        .paddingH(3f).paddingV(2f).build())
                .detailValueStyle(CellStyle.builder()
                        .font(FontStyle.builder().fontSize(9).color(Color.BLACK).build())
                        .paddingH(3f).paddingV(2f).build())
                .pageFooterColor(FOOTER_COLOR)
                .pageFooterFontSize(7f)
                .currencySymbol("\u20AC")
                .datePattern("dd/MM/yyyy")
                .dateTimePattern("dd/MM/yyyy HH:mm")
                .build();
    }
}
