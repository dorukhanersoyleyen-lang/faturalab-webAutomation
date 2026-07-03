package com.faturalab.automation.utils;

import com.faturalab.automation.context.TzfScenarioContext;
import com.faturalab.automation.context.TzfScenarioContext.TzfInvoice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Alıcı "Fatura Yükleme" ekranının Excel taslağına (invoice-list-template.xls)
 * birebir uyan .xls dosyası üretir — TZF işlemi otomasyonu test verisi.
 *
 * Şablon yapısı: 17 satır not bloğu + 13 sütunlu başlık + fatura satırları.
 * Fatura numaraları timestamp bazlı olduğundan her koşuda benzersizdir;
 * üretilen satırlar {@link TzfScenarioContext}'e yazılır ve sonraki
 * doğrulama adımları tam eşleşmeyle bu numaralar üzerinden çalışır.
 */
public final class TzfInvoiceExcelGenerator {

    private static final Logger log = LogManager.getLogger(TzfInvoiceExcelGenerator.class);

    private static final DateTimeFormatter TR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter INVOICE_NO_STAMP = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    /** Resmî TR tatilleri (sabit tarihli). Dini bayramlar yıla göre değiştiğinden kapsam dışı. */
    private static final Set<MonthDay> TR_HOLIDAYS = new HashSet<>(Arrays.asList(
            MonthDay.of(1, 1), MonthDay.of(4, 23), MonthDay.of(5, 1), MonthDay.of(5, 19),
            MonthDay.of(7, 15), MonthDay.of(8, 30), MonthDay.of(10, 29)));

    private static final String[] HEADERS = {
            "No", "Ticari İşletme Adı", "Ticari İşletme VKN*",
            "Fatura No*", "Fatura Tarihi*", "Vade Tarihi*", "Ek Vade Tarihi",
            "Fatura Tutarı*", "Ödenebilir Tutar",
            "Hash Code (E-Faturalar için)", "KDV'siz Tutar (E-Arşiv için)",
            "Fatura Tipi*", "Para Birimi*"
    };

    private TzfInvoiceExcelGenerator() {
    }

    /**
     * VKN'si verilen ticari işletme adına E-Fatura tipinde satırlar içeren .xls üretir.
     *
     * @return üretilen dosyanın mutlak yolu (context'e de yazılır)
     */
    public static String generate(String supplierName, String supplierVkn, int invoiceCount) {
        TzfScenarioContext.reset();

        LocalDate invoiceDate = previousBusinessDay(LocalDate.now());
        String stamp = java.time.LocalDateTime.now().format(INVOICE_NO_STAMP);
        Random random = new Random();

        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sayfa1");
            int rowIdx = writeNoteRows(sheet);
            writeHeaderRow(sheet, rowIdx++);

            for (int i = 1; i <= invoiceCount; i++) {
                String invoiceNo = "TZF" + stamp + "-" + i;
                String amount = formatTrAmount(BigDecimal.valueOf(5000 + random.nextInt(20000))
                        .setScale(2, java.math.RoundingMode.HALF_UP));
                String hash = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
                String fDate = invoiceDate.minusDays(businessDayOffset(invoiceDate, i - 1)).format(TR_DATE);
                String vDate = plusBusinessDays(invoiceDate, 30).format(TR_DATE);

                TzfInvoice inv = new TzfInvoice(i, invoiceNo, amount, fDate, vDate, hash);
                TzfScenarioContext.addInvoice(inv);
                writeInvoiceRow(sheet, rowIdx++, inv, supplierName, supplierVkn);
            }

            File outDir = new File("target/test-data");
            if (!outDir.exists() && !outDir.mkdirs()) {
                throw new IllegalStateException("target/test-data klasörü oluşturulamadı");
            }
            File outFile = new File(outDir, "tzf-invoice-list-" + stamp + ".xls");
            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                wb.write(fos);
            }

            String absolutePath = outFile.getAbsolutePath();
            TzfScenarioContext.setExcelPath(absolutePath);
            log.info("TZF fatura Excel'i üretildi: {} ({} fatura, tedarikçi: {} / {})",
                    absolutePath, invoiceCount, supplierName, supplierVkn);
            return absolutePath;
        } catch (Exception e) {
            throw new IllegalStateException("TZF Excel üretimi başarısız: " + e.getMessage(), e);
        }
    }

    // ─── Şablon satırları ─────────────────────────────────────────────────────

    /** Şablonun başındaki not bloğunu yazar; başlık satırının index'ini döner. */
    private static int writeNoteRows(Sheet sheet) {
        String[][] notes = {
                {"Not:", "'Fatura No' alanını girmek zorunludur."},
                {"Not:", "Fatura Tipi' alanına,"},
                {"", "E-Faturalar için E"},
                {"", "E-Arşiv için A"},
                {"", "Matbu faturalar için F"},
                {"", "E-Müstahsil makbuzu için M harflerini giriniz."},
                {"Not:", "E-Faturalar için Hash Code zorunludur."},
                {"Not:", "E-Arşiv için KDV'siz tutar zorunludur."},
                {"Not:", "Fatura No alanına, seri ve sıra numarasını araya boşluk bırakmadan birleşik giriniz."},
                {"Not:", "Tarih formatı gg/aa/yyyy şeklinde girilmelidir."},
                {"Not:", "KDV'siz tutar alanında ondalık ayrımı için virgül kullanınız."},
                {"Not:", "Para Birimi' alanına,"},
                {"", "Türk Lirası için TL"},
                {"", "Dolar için USD"},
                {"", "Euro için EUR"},
                {"", "İngiliz Sterlini için GBP"},
                {"", "Birleşik Arap Emirlikleri Dirhemi için AED harflerini giriniz."},
        };
        for (int i = 0; i < notes.length; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(notes[i][0]);
            row.createCell(1).setCellValue(notes[i][1]);
        }
        return notes.length;
    }

    private static void writeHeaderRow(Sheet sheet, int rowIdx) {
        Row row = sheet.createRow(rowIdx);
        for (int c = 0; c < HEADERS.length; c++) {
            row.createCell(c).setCellValue(HEADERS[c]);
        }
    }

    private static void writeInvoiceRow(Sheet sheet, int rowIdx, TzfInvoice inv,
                                        String supplierName, String supplierVkn) {
        Row row = sheet.createRow(rowIdx);
        String[] values = {
                String.valueOf(inv.no), supplierName, supplierVkn,
                inv.invoiceNo, inv.invoiceDate, inv.dueDate, "",
                inv.amount, "",
                inv.hashCode, "",
                "E", "TL"
        };
        for (int c = 0; c < values.length; c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(values[c]);
        }
    }

    // ─── Tarih / tutar yardımcıları ──────────────────────────────────────────

    private static boolean isBusinessDay(LocalDate d) {
        return d.getDayOfWeek() != DayOfWeek.SATURDAY
                && d.getDayOfWeek() != DayOfWeek.SUNDAY
                && !TR_HOLIDAYS.contains(MonthDay.from(d));
    }

    private static LocalDate previousBusinessDay(LocalDate d) {
        while (!isBusinessDay(d)) {
            d = d.minusDays(1);
        }
        return d;
    }

    private static LocalDate plusBusinessDays(LocalDate d, int days) {
        LocalDate result = d.plusDays(days);
        while (!isBusinessDay(result)) {
            result = result.plusDays(1);
        }
        return result;
    }

    /** i. fatura için tarihini 1-2 iş günü geriye kaydırır (şablon kuralı). */
    private static long businessDayOffset(LocalDate base, int index) {
        LocalDate d = base;
        for (int i = 0; i < index; i++) {
            d = previousBusinessDay(d.minusDays(1));
        }
        return java.time.temporal.ChronoUnit.DAYS.between(d, base);
    }

    /** 12500.00 → "12500,00" (şablon: ondalık ayrımı virgül, binlik ayraç yok). */
    private static String formatTrAmount(BigDecimal amount) {
        return amount.toPlainString().replace('.', ',');
    }
}
