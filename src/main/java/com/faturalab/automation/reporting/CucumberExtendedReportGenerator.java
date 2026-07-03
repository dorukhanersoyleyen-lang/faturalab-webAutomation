package com.faturalab.automation.reporting;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Masterthought / cucumber-reporting “extended” HTML (overview-features) üretir.
 * Cucumber JSON çıktısından rapor oluşturur; UAT/UI/API runner’lar aynı yardımcıyı kullanır.
 */
public final class CucumberExtendedReportGenerator {

    public static final String OVERVIEW_FEATURES_HTML = "cucumber-html-reports/overview-features.html";

    private CucumberExtendedReportGenerator() {
    }

    /**
     * @param jsonFile        Cucumber json plugin çıktısı
     * @param outputDirectory Genelde {@code .../advanced-reports} (içinde cucumber-html-reports oluşur)
     * @param projectName     Raporda görünen proje adı
     * @return overview HTML oluştuysa true
     */
    public static boolean generate(File jsonFile, File outputDirectory, String projectName) {
        // HER KOŞULDA önce eski çıktıyı sil — JSON boş/eksik olsa bile. Aksi halde
        // ReportOpener diskte kalan BAYAT extended raporu "güncelmiş gibi" açıyordu
        // (kronik sorunun asıl mekanizması). Eski rapor silinince opener, o suite'in
        // güncel cucumber index.html'ine düşer — yanlış veri göstermektense hiç göstermez.
        File htmlDir = new File(outputDirectory, "cucumber-html-reports");
        deleteRecursively(htmlDir);

        if (jsonFile == null || !jsonFile.exists() || jsonFile.length() == 0) {
            System.out.println("[ExtendedReport] JSON yok veya boş: " + jsonFile);
            return false;
        }
        try {
            String content = Files.readString(jsonFile.toPath());
            if (content.trim().isEmpty() || content.equals("[]")) {
                System.out.println("[ExtendedReport] JSON içeriği boş veya [] — eski rapor temizlendi, üretim atlandı.");
                return false;
            }
        } catch (Exception e) {
            System.err.println("[ExtendedReport] JSON okunamadı: " + e.getMessage());
            return false;
        }

        long startedAt = System.currentTimeMillis();
        try {
            outputDirectory.mkdirs();
            List<String> jsonFiles = new ArrayList<>();
            jsonFiles.add(jsonFile.getAbsolutePath());

            Configuration configuration = new Configuration(outputDirectory, projectName);
            configuration.setBuildNumber("1.0");
            configuration.addClassifications("Platform", System.getProperty("os.name", ""));
            configuration.addClassifications("Browser", "Chrome");
            configuration.addClassifications("Environment", System.getProperty("env", "test"));

            // TÜRKÇE LOCALE BUG'I: masterthought, status adını default locale ile
            // küçültür — TR makinede "FAILED".toLowerCase() = "faıled" (noktasız ı!)
            // olur, CSS'teki .failed kuralıyla eşleşmez ve fail satırları BEYAZ görünür
            // (SKIPPED→"skıpped" aynı şekilde). Üretim süresince locale'i İngilizce'ye
            // sabitleyip sonra geri yüklüyoruz.
            java.util.Locale previousLocale = java.util.Locale.getDefault();
            try {
                java.util.Locale.setDefault(java.util.Locale.ENGLISH);
                new ReportBuilder(jsonFiles, configuration).generateReports();
            } finally {
                java.util.Locale.setDefault(previousLocale);
            }

            File overview = overviewHtmlFile(outputDirectory);
            // Sadece "var mı" değil, "BU çağrıda mı yazıldı" doğrula (tazelik güvencesi).
            boolean fresh = overview.exists() && overview.lastModified() >= startedAt - 2000;
            if (fresh) {
                System.out.println("[ExtendedReport] Oluşturuldu (güncel): " + overview.getAbsolutePath());
                return true;
            }
            System.err.println("[ExtendedReport] UYARI: overview bu koşumda güncellenemedi -> "
                    + overview + " (exists=" + overview.exists() + ")");
            return false;
        } catch (Exception e) {
            System.err.println("[ExtendedReport] Hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Bir dizini (varsa) içeriğiyle birlikte siler; stale rapor kalıntısını önler. */
    private static void deleteRecursively(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        try (java.util.stream.Stream<java.nio.file.Path> walk = Files.walk(dir.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                .map(java.nio.file.Path::toFile)
                .forEach(File::delete);
        } catch (Exception e) {
            System.err.println("[ExtendedReport] Eski rapor temizlenemedi (" + dir + "): " + e.getMessage());
        }
    }

    public static File overviewHtmlFile(File outputDirectory) {
        return new File(outputDirectory, OVERVIEW_FEATURES_HTML);
    }
}
