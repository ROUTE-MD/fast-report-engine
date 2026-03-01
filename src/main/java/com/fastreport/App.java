package com.fastreport;

import com.fastreport.builder.ReportBuilder;
import com.fastreport.engine.ReportEngine;
import com.fastreport.model.ReportDefinition;
import com.fastreport.model.ReportOrientation;
import com.fastreport.model.column.ColumnType;
import com.fastreport.model.style.Alignment;

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

public class App {

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
        System.out.println("=== Fast Report Engine Demo ===");
        System.out.println("Generating " + ROW_COUNT + " banking transactions...");
        long t0 = System.currentTimeMillis();

        List<Map<String, Object>> rows = generateData();
        Map<String, Object> totals = computeTotals(rows);
        System.out.printf("Data ready in %d ms%n%n", System.currentTimeMillis() - t0);

        // Logo (optional)
        byte[] logo = null;
        Path logoPath = Path.of("asti.png");
        if (Files.exists(logoPath)) {
            logo = Files.readAllBytes(logoPath);
            System.out.println("Logo loaded: " + logoPath);
        } else {
            System.out.println("No asti.png found - rendering without logo.");
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // ── Build report ──
        var rb = new ReportBuilder()
                .title("Banca di Asti - Estratto Conto Transazioni")
                .meta("Codice Filiale", "IT-AST-001")
                .meta("Periodo", "01/01/2024 - 31/12/2024")
                .meta("Generato", now)
                .meta("Transazioni Totali", String.valueOf(ROW_COUNT));

        if (logo != null) rb.logo(logo, 80f, 40f);

        // Account summary
        rb.detailSection()
                .title("Riepilogo Conto")
                .columns(3)
                .field("Intestatario", "Rossi Mario")
                .field("IBAN", "IT60 X054 2801 0000 0000 1234 567")
                .field("Tipo Conto", "Conto Corrente Ordinario")
                .field("Filiale", "Asti Centro - Via Vittorio Alfieri 12")
                .field("Data Apertura", LocalDate.of(2018, 3, 15), ColumnType.DATE)
                .field("Saldo Attuale", new BigDecimal("42567.89"), ColumnType.CURRENCY)
                .end();

        // Separator
        rb.fullWidthRow()
                .left("Dettaglio Movimenti")
                .right("Generato: " + now)
                .end();

        // Transaction table (10 columns → responsive overflow in PDF)
        rb.listSection()
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

        var engine = new ReportEngine();

        // 1) Landscape PDF
        ReportDefinition report = rb.landscape().build();
        render("banking_landscape.pdf", () -> engine.renderPdf(report, Path.of("banking_landscape.pdf")));

        // 2) Portrait PDF
        ReportDefinition portrait = report.toBuilder().orientation(ReportOrientation.PORTRAIT).build();
        render("banking_portrait.pdf", () -> engine.renderPdf(portrait, Path.of("banking_portrait.pdf")));

        // 3) XLSX
        render("banking_report.xlsx", () -> engine.renderXlsx(report, Path.of("banking_report.xlsx")));

        // 4) Compact 4-col PDF
        var compact = new ReportBuilder()
                .title("Banca di Asti - Movimenti (Vista Compatta)")
                .titleAlignment(Alignment.CENTER)
                .separator()
                .meta("Periodo", "01/01/2024 - 31/12/2024")
                .meta("Generato", now);

        if (logo != null) compact.logo(logo, 80f, 40f);
        compact.listSection()
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
        ReportDefinition compactReport = compact.landscape().build();
        render("banking_4col.pdf", () -> engine.renderPdf(compactReport, Path.of("banking_4col.pdf")));

        System.out.println("\nDone. All files in: " + Path.of("").toAbsolutePath());
    }

    // ── helpers ──

    private static void render(String name, IORunnable task) throws IOException {
        System.out.printf("  %-28s", name);
        long t = System.currentTimeMillis();
        task.run();
        System.out.printf(" %,7d ms%n", System.currentTimeMillis() - t);
    }

    @FunctionalInterface
    private interface IORunnable { void run() throws IOException; }

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
            boolean isCredit = catIdx == 0 || catIdx == 8;
            BigDecimal amount = BigDecimal.valueOf(rng.nextDouble(10, isCredit ? 5000 : 2000))
                    .setScale(2, RoundingMode.HALF_UP);
            if (!isCredit) amount = amount.negate();
            balance = balance.add(amount);

            BigDecimal fee = amount.abs().compareTo(new BigDecimal("500")) > 0
                    ? new BigDecimal("2.50") : BigDecimal.ZERO;

            var row = new LinkedHashMap<String, Object>();
            row.put("date", date);
            row.put("id", String.format("TXN%08d", i + 1));
            String desc = CATEGORIES[catIdx] + " - " + COUNTERPARTIES[catIdx];
            // Every 500th row gets a long description to demonstrate text wrapping
            if (i % 500 == 0) {
                desc = "Bonifico SEPA ordinario a favore di " + COUNTERPARTIES[catIdx]
                        + " per pagamento fattura n. " + (i + 1000) + "/" + date.getYear()
                        + " relativa a servizi di consulenza amministrativa e gestione contabile"
                        + " del periodo " + date.getMonth() + " " + date.getYear()
                        + " - Riferimento interno: REF-" + String.format("%06d", i);
            }
            row.put("description", desc);
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
