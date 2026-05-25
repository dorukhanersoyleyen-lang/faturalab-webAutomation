package com.faturalab.automation.utils;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classpath altındaki test dosyalarını Selenium {@code sendKeys} için mutlak dosya yoluna çevirir.
 */
public final class UploadTestDataPaths {

    private UploadTestDataPaths() {
    }

    public static String toAbsolutePath(String classpathResource) {
        URL url = UploadTestDataPaths.class.getResource(classpathResource);
        if (url == null) {
            throw new IllegalStateException("Test verisi bulunamadi: " + classpathResource
                    + " (src/test/resources altina ekleyin)");
        }
        try {
            Path p = Paths.get(url.toURI());
            return p.toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Test verisi URI hatasi: " + classpathResource, e);
        }
    }

    /** UBL e-Fatura örneği */
    public static String eInvoiceXml() {
        return toAbsolutePath("/testdata/test-invoice.xml");
    }

    /** Toplu / CSV yükleme için basit CSV */
    public static String bulkCsv() {
        return toAbsolutePath("/testdata/upload/sample-bulk.csv");
    }

    /** Kağıt fatura / PDF örneği */
    public static String samplePdf() {
        return toAbsolutePath("/testdata/test-invoice.pdf");
    }

    /** Küçük görüntü (geçersiz fatura formatı testleri için) */
    public static String tinyPng() {
        return toAbsolutePath("/testdata/upload/tiny.png");
    }

    /**
     * Excel sekmesi için: ortamda gerçek .xlsx yoksa CSV yolu döner (uygulama kabul ediyorsa).
     * Öncelik: {@code /testdata/upload/sample-bulk.xlsx} varsa onu kullan.
     */
    public static String bulkExcelOrCsv() {
        URL xlsx = UploadTestDataPaths.class.getResource("/testdata/upload/sample-bulk.xlsx");
        if (xlsx != null) {
            try {
                return Paths.get(xlsx.toURI()).toAbsolutePath().toString();
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }
        return bulkCsv();
    }
}
