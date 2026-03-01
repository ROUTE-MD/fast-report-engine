package com.fastreport.model.section;

/** Sealed interface for all report section types. */
public sealed interface ReportSection permits FullWidthRow, DetailSection, ListSection, SeparatorLine {}
