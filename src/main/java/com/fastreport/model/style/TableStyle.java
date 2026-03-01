package com.fastreport.model.style;

import lombok.Builder;

import java.awt.Color;

/**
 * Style configuration for table rendering in ListSection.
 *
 * @param headerStyle         header row cell style
 * @param rowStyle            normal data row cell style
 * @param altRowStyle         alternating data row cell style
 * @param detailRowLabelStyle label style for overflow detail rows
 * @param detailRowValueStyle value style for overflow detail rows
 * @param gridColor           horizontal row separator color
 * @param accentColor         left accent bar color for detail rows
 */
@Builder(toBuilder = true)
public record TableStyle(
        CellStyle headerStyle,
        CellStyle rowStyle,
        CellStyle altRowStyle,
        CellStyle detailRowLabelStyle,
        CellStyle detailRowValueStyle,
        Color gridColor,
        Color accentColor
) {
    private static final Color DARK_BG = new Color(0x2C, 0x3E, 0x50);
    private static final Color ALT_BG = new Color(0xF5, 0xF5, 0xF5);
    private static final Color DETAIL_BG = new Color(0xFF, 0xFD, 0xE7);
    private static final Color DETAIL_LABEL_COLOR = new Color(0x88, 0x88, 0x88);
    private static final Color GRID = new Color(0xDD, 0xDD, 0xDD);
    private static final Color ACCENT = new Color(0x34, 0x98, 0xDB);

    /** Default table style with dark header, alternating rows, and detail styling. */
    public static TableStyle defaults() {
        return new TableStyle(
                new CellStyle(
                        FontStyle.builder().fontSize(8).bold(true).color(Color.WHITE).build(),
                        DARK_BG, Alignment.LEFT, 3f, 3f, null),
                new CellStyle(
                        FontStyle.builder().fontSize(7).color(Color.BLACK).build(),
                        Color.WHITE, Alignment.LEFT, 3f, 2f, null),
                new CellStyle(
                        FontStyle.builder().fontSize(7).color(Color.BLACK).build(),
                        ALT_BG, Alignment.LEFT, 3f, 2f, null),
                new CellStyle(
                        FontStyle.builder().fontSize(6.5f).bold(true).italic(true).color(DETAIL_LABEL_COLOR).build(),
                        DETAIL_BG, Alignment.LEFT, 3f, 1f, null),
                new CellStyle(
                        FontStyle.builder().fontSize(6.5f).color(Color.BLACK).build(),
                        DETAIL_BG, Alignment.LEFT, 3f, 1f, null),
                GRID,
                ACCENT
        );
    }
}
