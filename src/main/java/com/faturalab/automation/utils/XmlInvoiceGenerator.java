package com.faturalab.automation.utils;

import com.faturalab.automation.context.TzfScenarioContext;
import com.faturalab.automation.context.TzfScenarioContext.TzfInvoice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Dummy imzalı UBL e-fatura XML'i ve XML'li ZIP üretir.
 *
 * Şablon: testdata/test-invoice.xml — dev ortamının kabul ettiği kanıtlı
 * (TC-COMP-01-001 bu şablonla geçiyor). Placeholder'lar: {INVOICE_ID}, {UUID},
 * {ISSUE_DATE}, {SIGN_DATE}, {DUE_DATE}. İmza bloğu şablondaki dummy imzadır —
 * dev ortamı kabul eder (kullanıcı onayı ile).
 *
 * Şablondaki taraflar SABİTTİR: tedarikçi EFG (3960656675), alıcı ALBC (3456789010).
 * Bu yüzden üretilen XML/ZIP yalnızca ALBC alıcısıyla (veya EFG tedarikçisiyle)
 * yüklenebilir — buyer TIN eşleşmezse API AddInvoiceListDialog.106 hatası verir.
 */
public final class XmlInvoiceGenerator {

    private static final Logger log = LogManager.getLogger(XmlInvoiceGenerator.class);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private XmlInvoiceGenerator() {
    }

    /** Benzersiz numaralı tek XML üretir; fatura no'yu context'e ekler. Dönen: dosya yolu. */
    public static String generateXml(String seqSuffix) {
        try {
            String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
            String stamp = java.time.LocalDateTime.now().format(STAMP);
            String invoiceId = "EFG" + year + stamp.substring(2) + seqSuffix; // EFG{yyyy}{MMddHHmmss}{seq}
            String content = fillTemplate(invoiceId);

            File outDir = ensureOutDir();
            File xml = new File(outDir, invoiceId + ".xml");
            Files.write(xml.toPath(), content.getBytes(StandardCharsets.UTF_8));
            TzfScenarioContext.addInvoice(new TzfInvoice(
                    TzfScenarioContext.getInvoices().size() + 1, invoiceId, "", "", "", ""));
            log.info("Dummy imzalı XML üretildi: {} ({})", invoiceId, xml.getAbsolutePath());
            return xml.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException("XML üretimi başarısız: " + e.getMessage(), e);
        }
    }

    /**
     * İçinde N adet dummy imzalı XML bulunan ZIP üretir (alıcı "Fatura Yükle"
     * sekmesi ZIP'i imzalı-XML paketi olarak açar — isXmlZipFile=true).
     * Fatura no'ları context'e yazılır; yol context.excelPath'e konur.
     */
    public static String generateXmlZip(int count) {
        TzfScenarioContext.reset();
        String stamp = java.time.LocalDateTime.now().format(STAMP);
        try {
            File outDir = ensureOutDir();
            File zip = new File(outDir, "xml-invoices-" + stamp + ".zip");
            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
                for (int i = 1; i <= count; i++) {
                    String invoiceId = "EFG" + LocalDate.now().getYear() + stamp.substring(2) + i;
                    String content = fillTemplate(invoiceId);
                    zos.putNextEntry(new ZipEntry(invoiceId + ".xml"));
                    zos.write(content.getBytes(StandardCharsets.UTF_8));
                    zos.closeEntry();
                    TzfScenarioContext.addInvoice(new TzfInvoice(i, invoiceId, "", "", "", ""));
                }
            }
            TzfScenarioContext.setExcelPath(zip.getAbsolutePath());
            log.info("XML'li ZIP üretildi: {} ({} fatura)", zip.getAbsolutePath(), count);
            return zip.getAbsolutePath();
        } catch (Exception e) {
            throw new IllegalStateException("XML ZIP üretimi başarısız: " + e.getMessage(), e);
        }
    }

    private static String fillTemplate(String invoiceId) throws Exception {
        String template = readTemplate();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate due = LocalDate.now().plusDays(90);
        while (due.getDayOfWeek() == DayOfWeek.SATURDAY || due.getDayOfWeek() == DayOfWeek.SUNDAY) {
            due = due.plusDays(1);
        }
        return template
                .replace("{INVOICE_ID}", invoiceId)
                .replace("{UUID}", UUID.randomUUID().toString().toUpperCase())
                .replace("{ISSUE_DATE}", today)
                .replace("{SIGN_DATE}", today)
                .replace("{DUE_DATE}", due.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    private static String readTemplate() throws Exception {
        URL url = XmlInvoiceGenerator.class.getClassLoader().getResource("testdata/test-invoice.xml");
        if (url != null) {
            return new String(Files.readAllBytes(new File(url.toURI()).toPath()), StandardCharsets.UTF_8);
        }
        return new String(Files.readAllBytes(
                new File("src/test/resources/testdata/test-invoice.xml").toPath()), StandardCharsets.UTF_8);
    }

    private static File ensureOutDir() {
        File outDir = new File("target/test-data");
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IllegalStateException("target/test-data oluşturulamadı");
        }
        return outDir;
    }
}
