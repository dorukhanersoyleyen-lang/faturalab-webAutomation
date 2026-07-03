package com.faturalab.automation.utils;

import com.faturalab.automation.reporting.CucumberExtendedReportGenerator;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Koşudan sonra Cucumber HTML / extended (masterthought) raporunu tarayıcıda açar.
 * {@code faturalab.cucumber.report.suite} ({@code uat}, {@code ui}, {@code api}) hangi extended öncelikli.
 */
public class ReportOpener {

    public static void main(String[] args) {
        System.out.println("Opening test reports in browser...");

        try {
            boolean reportOpened = false;

            for (String reportPath : orderedReportPaths()) {
                File reportFile = new File(reportPath);
                if (reportFile.exists()) {
                    System.out.println("Found report: " + reportPath);

                    if (openInBrowser(reportFile)) {
                        System.out.println("Report opened successfully: " + reportPath);
                        reportOpened = true;
                        break;
                    }
                }
            }

            if (!reportOpened) {
                System.out.println("No test reports found or unable to open browser");
                System.out.println("Check these locations:");
                for (String path : orderedReportPaths()) {
                    System.out.println("   - " + path);
                }
            }

        } catch (Exception e) {
            System.err.println("Error opening reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static List<String> orderedReportPaths() {
        String suite = System.getProperty("faturalab.cucumber.report.suite", "").trim().toLowerCase(Locale.ROOT);
        String extRel = CucumberExtendedReportGenerator.OVERVIEW_FEATURES_HTML;

        List<String> paths = new ArrayList<>();

        if ("uat".equals(suite)) {
            paths.add(pathUatExtended(extRel));
            paths.add("target/cucumber-reports/uat/index.html");
        } else if ("ui".equals(suite)) {
            paths.add(pathUiExtended(extRel));
            paths.add("target/cucumber-reports/ui/index.html");
        } else if ("api".equals(suite)) {
            paths.add(pathRootExtended(extRel));
            paths.add("target/cucumber-reports/index.html");
        } else {
            paths.add(pathRootExtended(extRel));
            paths.add(pathUatExtended(extRel));
            paths.add(pathUiExtended(extRel));
            paths.add("target/cucumber-reports/uat/index.html");
            paths.add("target/cucumber-reports/ui/index.html");
            paths.add("target/cucumber-reports/index.html");
            paths.add("target/surefire-reports/index.html");
            return paths;
        }

        paths.add(pathRootExtended(extRel));
        paths.add("target/cucumber-reports/index.html");
        paths.add("target/surefire-reports/index.html");
        return paths;
    }

    private static String pathUatExtended(String extRel) {
        return "target/cucumber-reports/uat/advanced-reports/" + extRel;
    }

    private static String pathUiExtended(String extRel) {
        return "target/cucumber-reports/ui/advanced-reports/" + extRel;
    }

    private static String pathRootExtended(String extRel) {
        return "target/cucumber-reports/advanced-reports/" + extRel;
    }

    private static boolean openInBrowser(File htmlFile) {
        if (htmlFile == null || !htmlFile.exists() || !htmlFile.isFile()) {
            return false;
        }
        try {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            String filePath = htmlFile.getCanonicalPath();

            // Windows: Desktop.open() sistem varsayılan uygulamasını açar; HTML dosyası
            // Outlook'a bağlıysa Outlook açılır. Bu yüzden doğrudan "start" komutu kullanıyoruz.
            if (os.contains("win")) {
                new ProcessBuilder("cmd.exe", "/c", "start", "", filePath).start();
                return true;
            }
            if (os.contains("mac")) {
                new ProcessBuilder("open", filePath).start();
                return true;
            }
            if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", filePath).start();
                return true;
            }
        } catch (IOException e) {
            System.err.println("Failed to open report: " + e.getMessage());
        }

        return false;
    }
}
