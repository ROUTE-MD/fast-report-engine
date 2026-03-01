# Fast Report Engine

Lightweight Java 21 report engine built on **Apache PDFBox 3** and **FastExcel**.
Declarative builder API, responsive table layout, theming, and dual PDF/XLSX output.

## Features

- **Sealed section model** — `FullWidthRow`, `DetailSection`, `ListSection`, `SeparatorLine`
- **Responsive table layout** — columns that don't fit the PDF page overflow into key-value detail rows beneath each record
- **Text wrapping** — enabled by default on all columns (PDF + XLSX), with dynamic row heights in PDF; disable per-column with `columnNoWrap`/`baseColumnNoWrap`
- **Column alignment** — per-column horizontal (`LEFT`, `CENTER`, `RIGHT`) and vertical (`TOP`, `MIDDLE`, `BOTTOM`) alignment
- **Streaming support** — pass an `Iterator` to render millions of rows without loading all data in memory
- **Dual output** — same `ReportDefinition` renders to both PDF and XLSX
- **Theming** — full control over fonts, colors, table styles, detail styles, and page footer
- **Logo & metadata** — optional logo on every page, ordered key-value metadata header
- **Separator lines** — insert horizontal rules anywhere in the report with `separator()`
- **Alternating row colors**, negative-value highlighting in red, summary/totals row
- **Automatic page breaks** with repeated table headers on new pages

## Requirements

- Java 21+
- Maven 3.9+

## Quick Start

```bash
mvn compile
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" \
  com.fastreport.App
```

This generates 4 files in the project root:

| File | Description |
|------|-------------|
| `banking_landscape.pdf` | Landscape A4, 10 columns with responsive overflow |
| `banking_portrait.pdf` | Portrait A4, more columns overflow to detail rows |
| `banking_report.xlsx` | Excel with all 10 columns as headers |
| `banking_4col.pdf` | Compact: 4 base columns in header, 6 in detail rows |

### Streaming Demo (100k rows)

```bash
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" \
  com.fastreport.StreamingApp
```

Generates `streaming_landscape.pdf` and `streaming_report.xlsx` using chunked iteration (1,000 rows per chunk, never all in memory).

## Usage

### 1. Build a report

```java
import com.fastreport.builder.ReportBuilder;
import com.fastreport.model.column.ColumnType;
import com.fastreport.model.style.Alignment;
import com.fastreport.model.style.VerticalAlignment;

var report = new ReportBuilder()
    .title("Monthly Report")
    .landscape()                          // or .portrait()
    .logo(logoPngBytes, 80f, 40f)         // optional
    .meta("Department", "Finance")
    .meta("Period", "January 2024")
    .separator()                          // horizontal line

    // Key-value detail section
    .detailSection()
        .title("Account Summary")
        .columns(2)                       // 1, 2, or 3 column grid
        .field("Account", "IT60 X054 ...")
        .field("Balance", new BigDecimal("42567.89"), ColumnType.CURRENCY)
        .end()

    .separator()                          // another line

    // Full-width separator row
    .fullWidthRow()
        .left("Transactions")
        .right("Generated: 2024-01-15")
        .end()

    // Data table
    .listSection()
        .title("Transaction List")
        .baseColumn("date", "Date", ColumnType.DATE)          // always in table header
        .baseColumn("desc", "Description", ColumnType.STRING, 2.0f,
                    Alignment.LEFT, VerticalAlignment.TOP)     // left + top aligned
        .baseColumn("amount", "Amount", ColumnType.CURRENCY)
        .column("category", "Category", ColumnType.STRING)    // may overflow to detail rows
        .columnNoWrap("code", "Code", ColumnType.STRING)      // no wrapping, truncate
        .rows(dataRows)                   // List, Iterable, or Iterator
        .summaryValues(Map.of("amount", totalAmount))
        .end()

    .build();
```

### 2. Render

```java
import com.fastreport.engine.ReportEngine;

var engine = new ReportEngine();

// To files
engine.renderPdf(report, Path.of("report.pdf"));
engine.renderXlsx(report, Path.of("report.xlsx"));

// To output streams
engine.renderPdf(report, outputStream);
engine.renderXlsx(report, outputStream);
```

### 3. Streaming from a database

Pass an `Iterator` instead of a `List` to avoid loading all rows into memory:

```java
// Example: wrap a JDBC ResultSet as an Iterator
Iterator<Map<String, Object>> cursor = new ResultSetIterator(resultSet);

new ReportBuilder()
    // ...
    .listSection()
        .rows(cursor)   // single-pass, streamed
        .end()
    .build();
```

Both PDF and XLSX renderers consume the iterator exactly once.

> **Note:** since an `Iterator` is single-pass, build a separate `ReportDefinition` for each output format (each with its own iterator/cursor).

## Section Types

### FullWidthRow

A horizontal band with up to three text slots (left, center, right). Use for subtitles, separators, or timestamps.

```java
.fullWidthRow()
    .left("Section Title")
    .center("Centered Text")
    .right("Right-aligned")
    .height(20f)
    .backgroundColor(Color.LIGHT_GRAY)
    .end()
```

### DetailSection

A key-value grid laid out in 1, 2, or 3 columns. Use for account summaries, headers, or metadata blocks.

```java
.detailSection()
    .title("Account Info")
    .columns(3)
    .field("Name", "Mario Rossi")
    .field("IBAN", "IT60 X054 ...")
    .field("Balance", new BigDecimal("1234.56"), ColumnType.CURRENCY)
    .end()
```

### ListSection

A data table with typed columns, responsive overflow, alternating rows, and optional summary.

**Column types:** `STRING`, `INTEGER`, `DECIMAL`, `CURRENCY`, `DATE`, `PERCENTAGE`, `BOOLEAN`

**Base columns** stay in the table header; non-base columns overflow to detail rows when the PDF page is too narrow:

```java
.listSection()
    .baseColumn("date", "Date", ColumnType.DATE)         // always visible
    .baseColumn("amount", "Amount", ColumnType.CURRENCY)  // always visible
    .column("notes", "Notes", ColumnType.STRING)           // overflows if needed
    .column("ref", "Reference", ColumnType.STRING)         // overflows if needed
    .rows(data)
    .end()
```

In XLSX, all columns are always shown as headers (Excel has horizontal scroll).

### SeparatorLine

A horizontal line spanning the full page width. Insert anywhere between sections.

```java
.separator()                              // default: blue accent, 1.5pt
.separator(Color.RED)                     // custom color
.separator(Color.GRAY, 0.5f)              // custom color + thickness
```

## Column Options

### Text Wrapping

All columns wrap by default — long text flows to multiple lines with dynamic row height in PDF and cell wrap in XLSX. Disable per-column:

```java
.columnNoWrap("code", "Code", ColumnType.STRING)          // truncates with "..."
.baseColumnNoWrap("id", "ID", ColumnType.STRING, 1.5f)    // base column, no wrap
```

Long words without spaces are also force-broken across lines.

### Horizontal Alignment

Auto-determined from column type (strings left, numbers right), or set explicitly:

```java
.column("name", "Name", ColumnType.STRING, 1.0f, Alignment.CENTER)
.baseColumn("total", "Total", ColumnType.CURRENCY, 1.3f, Alignment.RIGHT)
```

### Vertical Alignment

Default is `MIDDLE`. Set per-column when rows have varying heights due to wrapping:

```java
.baseColumn("desc", "Description", ColumnType.STRING, 2.0f,
            Alignment.LEFT, VerticalAlignment.TOP)

.column("amount", "Amount", ColumnType.CURRENCY, 1.3f,
        Alignment.RIGHT, VerticalAlignment.BOTTOM)
```

Values: `TOP`, `MIDDLE`, `BOTTOM` — applied in both PDF and XLSX.

## Theming

Start from defaults and override what you need:

```java
import com.fastreport.builder.ThemeBuilder;

var theme = ThemeBuilder.fromDefaults()
    .currencySymbol("$")
    .datePattern("MM/dd/yyyy")
    .titleStyle(FontStyle.builder().fontSize(18).bold(true).color(Color.BLUE).build())
    .build();

new ReportBuilder()
    .theme(theme)
    // ...
```

Or use the Lombok-generated builder on `ReportTheme` directly:

```java
var theme = ReportTheme.defaults().toBuilder()
    .currencySymbol("£")
    .build();
```

## Project Structure

```
src/main/java/com/fastreport/
├── App.java                        # 35k-row demo (all in memory)
├── StreamingApp.java               # 100k-row chunked iterator demo
├── builder/                        # Fluent builder API
│   ├── ReportBuilder.java
│   ├── ListSectionBuilder.java
│   ├── DetailSectionBuilder.java
│   ├── FullWidthRowBuilder.java
│   └── ThemeBuilder.java
├── engine/
│   └── ReportEngine.java           # Facade: renderPdf() / renderXlsx()
├── layout/
│   ├── ResponsiveTableLayout.java  # Base vs detail column splitting
│   ├── RowLayoutCalculator.java    # Dynamic row height with wrapping
│   └── TextWrapper.java            # Word-wrap + force-break
├── model/
│   ├── ReportDefinition.java
│   ├── ReportOrientation.java
│   ├── ReportTheme.java
│   ├── column/                     # ColumnDef, ColumnType
│   ├── content/                    # TextContent, DetailField
│   ├── section/                    # ReportSection (sealed), FullWidthRow, DetailSection, ListSection, SeparatorLine
│   └── style/                      # FontStyle, CellStyle, TableStyle, Alignment, VerticalAlignment
└── renderer/
    ├── ReportRenderer.java         # Interface
    ├── pdf/                        # PDFBox renderer
    └── xlsx/                       # FastExcel renderer
```

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| [Apache PDFBox](https://pdfbox.apache.org/) | 3.0.3 | PDF rendering |
| [FastExcel](https://github.com/dhatim/fastexcel) | 0.18.4 | XLSX rendering |
| [Lombok](https://projectlombok.org/) | 1.18.34 | Builders, `@Data` |
