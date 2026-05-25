package com.faturalab.automation.reporting;

import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
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
        if (jsonFile == null || !jsonFile.exists() || jsonFile.length() == 0) {
            System.out.println("[ExtendedReport] JSON yok veya boş: " + jsonFile);
            return false;
        }
        try {
            String content = Files.readString(jsonFile.toPath());
            if (content.trim().isEmpty() || content.equals("[]")) {
                System.out.println("[ExtendedReport] JSON içeriği boş veya [].");
                return false;
            }
        } catch (Exception e) {
            System.err.println("[ExtendedReport] JSON okunamadı: " + e.getMessage());
            return false;
        }
        try {
            outputDirectory.mkdirs();
            List<String> jsonFiles = new ArrayList<>();
            jsonFiles.add(jsonFile.getAbsolutePath());

            Configuration configuration = new Configuration(outputDirectory, projectName);
            configuration.setBuildNumber("1.0");
            configuration.addClassifications("Platform", System.getProperty("os.name", ""));
            configuration.addClassifications("Browser", "Chrome");
            configuration.addClassifications("Environment", System.getProperty("env", "test"));

            new ReportBuilder(jsonFiles, configuration).generateReports();

            File overview = overviewHtmlFile(outputDirectory);
            boolean ok = overview.exists();
            if (ok) {
                System.out.println("[ExtendedReport] Oluşturuldu: " + overview.getAbsolutePath());
            } else {
                System.err.println("[ExtendedReport] Üretim tamamlandı fakat overview bulunamadı: " + overview);
            }
            return ok;
        } catch (Exception e) {
            System.err.println("[ExtendedReport] Hata: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static File overviewHtmlFile(File outputDirectory) {
        return new File(outputDirectory, OVERVIEW_FEATURES_HTML);
    }
}
