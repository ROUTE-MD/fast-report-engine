package com.fastreport;

import com.fastreport.builder.ReportBuilder;
import com.fastreport.engine.ReportEngine;
import com.fastreport.model.ReportDefinition;
import com.fastreport.model.column.ColumnType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Demonstrates streaming 100k rows through the report engine using an Iterator.
 *
 * Rows are generated in chunks of 1,000 (simulating paginated DB fetches)
 * and never held entirely in memory — only the current chunk is alive at any time.
 */
public class StreamingApp {

    private static final int TOTAL_ROWS = 100_000;
    private static final int CHUNK_SIZE = 1_000;

    public static void main(String[] args) throws IOException {
        System.out.println("=== Streaming Report Demo (100k rows, chunked iterator) ===\n");

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Each render needs its own iterator (single-pass), so we build per output
        ReportDefinition report = buildReport(now, new ChunkedRowIterator(TOTAL_ROWS, CHUNK_SIZE));

        var engine = new ReportEngine();

        render("streaming_landscape.pdf",
                () -> engine.renderPdf(report, Path.of("streaming_landscape.pdf")));

        // Build a fresh report with a new iterator for XLSX (previous one is exhausted)
        ReportDefinition xlsxReport = buildReport(now, new ChunkedRowIterator(TOTAL_ROWS, CHUNK_SIZE));
        render("streaming_report.xlsx",
                () -> engine.renderXlsx(xlsxReport, Path.of("streaming_report.xlsx")));

        System.out.println("\nDone. Files in: " + Path.of("").toAbsolutePath());
    }

    private static ReportDefinition buildReport(String now, ChunkedRowIterator rowIterator) {
        return new ReportBuilder()
                .title("Banca di Asti - Report Massivo Streaming")
                .landscape()
                .meta("Periodo", "01/01/2024 - 31/12/2024")
                .meta("Generato", now)
                .meta("Righe Totali", String.format("%,d", TOTAL_ROWS))
                .meta("Modalita", "Streaming a blocchi di " + CHUNK_SIZE)
                .detailSection()
                    .title("Info Esecuzione")
                    .columns(2)
                    .field("Chunk Size", String.valueOf(CHUNK_SIZE))
                    .field("Total Rows", String.format("%,d", TOTAL_ROWS))
                    .field("Memory Mode", "Streaming (only 1 chunk in memory)")
                    .field("Data Source", "Simulated paginated DB cursor")
                    .end()
                .fullWidthRow()
                    .left("Dettaglio Movimenti")
                    .right("Generato: " + now)
                    .end()
                .listSection()
                    .title("Transazioni (streamed)")
                    .baseColumn("date", "Data", ColumnType.DATE, 1.2f)
                    .baseColumn("id", "ID", ColumnType.STRING, 1.2f)
                    .baseColumn("description", "Descrizione", ColumnType.STRING, 2.0f)
                    .baseColumn("amount", "Importo", ColumnType.CURRENCY, 1.3f)
                    .column("category", "Categoria", ColumnType.STRING, 1.2f)
                    .column("counterparty", "Controparte", ColumnType.STRING, 1.8f)
                    .column("branch", "Filiale", ColumnType.STRING, 1.0f)
                    .column("balance", "Saldo", ColumnType.CURRENCY, 1.3f)
                    .column("status", "Stato", ColumnType.STRING, 0.8f)
                    .column("fee", "Commissione", ColumnType.CURRENCY, 1.0f)
                    .rows((Iterator<Map<String, Object>>) rowIterator)
                    .end()
                .build();
    }

    // ── Chunked iterator simulating a paginated DB cursor ──

    /**
     * Generates rows on-the-fly in chunks, simulating a paginated database fetch.
     * Only one chunk (CHUNK_SIZE rows) is held in memory at any time.
     */
    static class ChunkedRowIterator implements Iterator<Map<String, Object>>, Iterable<Map<String, Object>> {

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

        private final int totalRows;
        private final int chunkSize;
        private final ThreadLocalRandom rng = ThreadLocalRandom.current();

        private int emittedTotal;
        private List<Map<String, Object>> currentChunk;
        private int posInChunk;
        private int chunksLoaded;

        private BigDecimal balance = new BigDecimal("50000.00");
        private LocalDate date = LocalDate.of(2024, 1, 1);

        ChunkedRowIterator(int totalRows, int chunkSize) {
            this.totalRows = totalRows;
            this.chunkSize = chunkSize;
        }

        @Override
        public Iterator<Map<String, Object>> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return emittedTotal < totalRows;
        }

        @Override
        public Map<String, Object> next() {
            if (!hasNext()) throw new NoSuchElementException();

            if (currentChunk == null || posInChunk >= currentChunk.size()) {
                currentChunk = fetchNextChunk();
                posInChunk = 0;
                chunksLoaded++;
                if (chunksLoaded % 20 == 0) {
                    System.out.printf("    [stream] chunk %d loaded (%,d / %,d rows)%n",
                            chunksLoaded, emittedTotal, totalRows);
                }
            }

            Map<String, Object> row = currentChunk.get(posInChunk++);
            emittedTotal++;
            return row;
        }

        private List<Map<String, Object>> fetchNextChunk() {
            // Simulate a DB page fetch with a small delay
            int remaining = totalRows - emittedTotal;
            int size = Math.min(chunkSize, remaining);
            var chunk = new ArrayList<Map<String, Object>>(size);

            for (int i = 0; i < size; i++) {
                int globalIdx = emittedTotal + i;
                if (globalIdx > 0 && globalIdx % 100 == 0) {
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
                row.put("id", String.format("TXN%08d", globalIdx + 1));
                row.put("description", CATEGORIES[catIdx] + " - " + COUNTERPARTIES[catIdx]);
                row.put("amount", amount);
                row.put("category", CATEGORIES[catIdx]);
                row.put("counterparty", COUNTERPARTIES[catIdx]);
                row.put("branch", BRANCHES[rng.nextInt(BRANCHES.length)]);
                row.put("balance", balance.setScale(2, RoundingMode.HALF_UP));
                row.put("status", STATUSES[rng.nextInt(100) < 95 ? 0 : (rng.nextInt(100) < 80 ? 1 : 2)]);
                row.put("fee", fee);
                chunk.add(row);
            }
            return chunk;
        }
    }

    // ── helpers ──

    private static void render(String name, IORunnable task) throws IOException {
        System.out.printf("  %-30s", name);
        long t = System.currentTimeMillis();
        task.run();
        System.out.printf(" %,7d ms%n", System.currentTimeMillis() - t);
    }

    @FunctionalInterface
    private interface IORunnable { void run() throws IOException; }
}
