package com.fastreport.demo;

import com.fastreport.builder.ReportBuilder;
import com.fastreport.builder.ThemeBuilder;
import com.fastreport.engine.ReportEngine;
import com.fastreport.model.ReportDefinition;
import com.fastreport.model.ReportOrientation;
import com.fastreport.model.column.ColumnType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates a 35,000-row Italian banking transaction report with:
 * - Logo (if asti.png present), timestamp, metadata
 * - DetailSection for account summary
 * - ListSection with 10 columns (responsive overflow in PDF)
 * - Summary row with totals
 *
 * Outputs:
 * - banking_landscape.pdf  (landscape, all 10 cols - some overflow to detail rows)
 * - banking_portrait.pdf   (portrait, even more overflow)
 * - banking_report.xlsx    (all columns as headers)
 * - banking_4col.pdf       (only 4 base columns, rest as detail)
 */
public class BankingReportDemo {

    private static final int ROW_COUNT = 35_000;
    private static final String[] CATEGORIES = {
            "Stipendio", "Affitto", "Utenze", "Spesa Alimentare",
            "Trasporto", "Ristorazione", "Shopping", "Assicurazione",
            "Investimento", "Abbonamenti", "Salute", "Istruzione"
    };
    private static final String[] COUNTERPARTIES = {
            "Azienda ABC S.r.l.", "Immobiliare Torino", "ENEL Energia",
            "Esselunga S.p.A.", "Trenitalia", "Ristorante Da Mario",
            "Amazon EU S.a.r.l.", "Generali Italia", "Fineco Bank",
            "Netflix International", "ASL TO1", "Universita degli Studi"
    };
    private static final String[] BRANCHES = {
            "Asti Centro", "Asti Ovest", "Torino Porta Nuova",
            "Milano Duomo", "Roma Termini", "Firenze SMN"
    };
    private static final String[] STATUSES = {"Completata", "In Elaborazione", "Annullata"};

    public static void main(String[] args) throws IOException {
        System.out.println("Generating banking report data (" + ROW_COUNT + " rows)...");
        long t0 = System.currentTimeMillis();

        List<Map<String, Object>> rows = generateData();
        Map<String, Object> totals = computeTotals(rows);

        System.out.printf("Data generated in %d ms%n", System.currentTimeMillis() - t0);

        // Try to load logo
        byte[] logo = null;
        Path logoPath = Path.of("asti.png");
        if (Files.exists(logoPath)) {
            logo = Files.readAllBytes(logoPath);
            System.out.println("Logo loaded: " + logoPath);
        } else {
            System.out.println("No asti.png found, rendering without logo.");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // ── Build the report definition ──
        var reportBuilder = new ReportBuilder()
                .title("Banca di Asti - Estratto Conto Transazioni")
                .meta("Codice Filiale", "IT-AST-001")
                .meta("Periodo", "01/01/2024 - 31/12/2024")
                .meta("Generato", timestamp)
                .meta("Transazioni Totali", String.valueOf(ROW_COUNT));

        if (logo != null) {
            reportBuilder.logo(logo, 80f, 40f);
        }

        // Account summary detail section
        reportBuilder.detailSection()
                .title("Riepilogo Conto")
                .columns(3)
                .field("Intestatario", "Rossi Mario")
                .field("IBAN", "IT60 X054 2801 0000 0000 1234 567")
                .field("Tipo Conto", "Conto Corrente Ordinario")
                .field("Filiale", "Asti Centro - Via Vittorio Alfieri 12")
                .field("Data Apertura", LocalDate.of(2018, 3, 15), ColumnType.DATE)
                .field("Saldo Attuale", new BigDecimal("42567.89"), ColumnType.CURRENCY)
                .end();

        // Full-width separator
        reportBuilder.fullWidthRow()
                .left("Dettaglio Movimenti")
                .right("Pagina generata: " + timestamp)
                .end();

        // Transaction list with 10 columns (triggers responsive in PDF)
        reportBuilder.listSection()
                .title("Elenco Transazioni")
                .baseColumn("date", "Data", ColumnType.DATE, 1.2f)
                .baseColumn("id", "ID Transazione", ColumnType.STRING, 1.5f)
                .baseColumn("description", "Descrizione", ColumnType.STRING, 2.0f)
                .baseColumn("amount", "Importo", ColumnType.CURRENCY, 1.3f)
                .column("category", "Categoria", ColumnType.STRING, 1.2f)
                .column("counterparty", "Controparte", ColumnType.STRING, 1.8f)
                .column("branch", "Filiale", ColumnType.STRING, 1.0f)
                .column("balance", "Saldo", ColumnType.CURRENCY, 1.3f)
                .column("status", "Stato", ColumnType.STRING, 0.8f)
                .column("fee", "Commissione", ColumnType.CURRENCY, 1.0f)
                .rows(rows)
                .summaryValues(totals)
                .end();

        // ── Render 4 outputs ──
        var engine = new ReportEngine();

        // 1. Landscape PDF (default)
        ReportDefinition landscapeReport = reportBuilder.landscape().build();
        System.out.print("Rendering banking_landscape.pdf... ");
        t0 = System.currentTimeMillis();
        engine.renderPdf(landscapeReport, Path.of("banking_landscape.pdf"));
        System.out.printf("done (%d ms)%n", System.currentTimeMillis() - t0);

        // 2. Portrait PDF
        ReportDefinition portraitReport = landscapeReport.toBuilder()
                .orientation(ReportOrientation.PORTRAIT).build();
        System.out.print("Rendering banking_portrait.pdf... ");
        t0 = System.currentTimeMillis();
        engine.renderPdf(portraitReport, Path.of("banking_portrait.pdf"));
        System.out.printf("done (%d ms)%n", System.currentTimeMillis() - t0);

        // 3. XLSX
        System.out.print("Rendering banking_report.xlsx... ");
        t0 = System.currentTimeMillis();
        engine.renderXlsx(landscapeReport, Path.of("banking_report.xlsx"));
        System.out.printf("done (%d ms)%n", System.currentTimeMillis() - t0);

        // 4. Compact 4-col PDF (only base columns in header)
        var compactBuilder = new ReportBuilder()
                .title("Banca di Asti - Movimenti (Vista Compatta)")
                .meta("Periodo", "01/01/2024 - 31/12/2024")
                .meta("Generato", timestamp);
        if (logo != null) compactBuilder.logo(logo, 80f, 40f);
        compactBuilder.listSection()
                .title("Movimenti")
                .baseColumn("date", "Data", ColumnType.DATE, 1.2f)
                .baseColumn("description", "Descrizione", ColumnType.STRING, 2.5f)
                .baseColumn("amount", "Importo", ColumnType.CURRENCY, 1.3f)
                .baseColumn("balance", "Saldo", ColumnType.CURRENCY, 1.3f)
                .column("id", "ID Transazione", ColumnType.STRING, 1.5f)
                .column("category", "Categoria", ColumnType.STRING, 1.0f)
                .column("counterparty", "Controparte", ColumnType.STRING, 1.5f)
                .column("branch", "Filiale", ColumnType.STRING, 1.0f)
                .column("status", "Stato", ColumnType.STRING, 0.8f)
                .column("fee", "Commissione", ColumnType.CURRENCY, 1.0f)
                .rows(rows)
                .summaryValues(totals)
                .end();
        ReportDefinition compactReport = compactBuilder.landscape().build();
        System.out.print("Rendering banking_4col.pdf... ");
        t0 = System.currentTimeMillis();
        engine.renderPdf(compactReport, Path.of("banking_4col.pdf"));
        System.out.printf("done (%d ms)%n", System.currentTimeMillis() - t0);

        System.out.println("\nAll files generated successfully.");
    }

    private static List<Map<String, Object>> generateData() {
        var rng = ThreadLocalRandom.current();
        var rows = new ArrayList<Map<String, Object>>(ROW_COUNT);
        BigDecimal balance = new BigDecimal("50000.00");
        LocalDate date = LocalDate.of(2024, 1, 1);

        for (int i = 0; i < ROW_COUNT; i++) {
            if (i > 0 && i % 100 == 0) {
                date = date.plusDays(1);
                if (date.isAfter(LocalDate.of(2024, 12, 31))) date = LocalDate.of(2024, 1, 1);
            }

            int catIdx = rng.nextInt(CATEGORIES.length);
            boolean isCredit = catIdx == 0 || catIdx == 8; // Stipendio or Investimento
            BigDecimal amount = BigDecimal.valueOf(rng.nextDouble(10, isCredit ? 5000 : 2000))
                    .setScale(2, RoundingMode.HALF_UP);
            if (!isCredit) amount = amount.negate();
            balance = balance.add(amount);

            BigDecimal fee = amount.abs().compareTo(new BigDecimal("500")) > 0
                    ? new BigDecimal("2.50") : BigDecimal.ZERO;

            var row = new LinkedHashMap<String, Object>();
            row.put("date", date);
            row.put("id", String.format("TXN%08d", i + 1));
            row.put("description", CATEGORIES[catIdx] + " - " + COUNTERPARTIES[catIdx]);
            row.put("amount", amount);
            row.put("category", CATEGORIES[catIdx]);
            row.put("counterparty", COUNTERPARTIES[catIdx]);
            row.put("branch", BRANCHES[rng.nextInt(BRANCHES.length)]);
            row.put("balance", balance.setScale(2, RoundingMode.HALF_UP));
            row.put("status", STATUSES[rng.nextInt(100) < 95 ? 0 : (rng.nextInt(100) < 80 ? 1 : 2)]);
            row.put("fee", fee);
            rows.add(row);
        }
        return rows;
    }

    private static Map<String, Object> computeTotals(List<Map<String, Object>> rows) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalFee = BigDecimal.ZERO;
        for (var row : rows) {
            totalAmount = totalAmount.add((BigDecimal) row.get("amount"));
            totalFee = totalFee.add((BigDecimal) row.get("fee"));
        }
        var totals = new LinkedHashMap<String, Object>();
        totals.put("amount", totalAmount);
        totals.put("fee", totalFee);
        totals.put("balance", rows.getLast().get("balance"));
        return totals;
    }
}
