package com.faturalab.automation.reporting;

import com.faturalab.automation.utils.ReportOpener;

import java.io.File;

/**
 * Testi TEKRAR KOŞMADAN, mevcut cucumber.json'dan extended (masterthought) raporu
 * yeniden üretir ve (opsiyonel) tarayıcıda açar.
 *
 * Kronik sorun için güvence: {@code mvn test} build FAILURE dönse bile wrapper script
 * bu sınıfı çağırarak GÜNCEL raporu kesinlikle üretip açar — TestNG @AfterSuite'in
 * fail durumundaki davranışına hiç bağımlı kalmadan.
 *
 * Kullanım:
 *   java -cp <cp> com.faturalab.automation.reporting.ReportRegen uat --open
 *   java -cp <cp> com.faturalab.automation.reporting.ReportRegen ui
 *
 * Argümanlar:
 *   [0] suite: uat | ui | api  (varsayılan: uat)
 *   [1] --open verilirse rapor tarayıcıda açılır
 */
public final class ReportRegen {

    private ReportRegen() {
    }

    public static void main(String[] args) {
        String suite = (args.length > 0 && !args[0].startsWith("--"))
                ? args[0].trim().toLowerCase()
                : "uat";
        boolean open = false;
        for (String a : args) {
            if ("--open".equalsIgnoreCase(a)) {
                open = true;
            }
        }

        String base;
        String projectName;
        switch (suite) {
            case "ui":
                base = "target/cucumber-reports/ui";
                projectName = "Faturalab UI Automation";
                break;
            case "api":
                base = "target/cucumber-reports";
                projectName = "Faturalab API Automation";
                break;
            case "uat":
            default:
                base = "target/cucumber-reports/uat";
                projectName = "Faturalab UAT Automation";
                break;
        }

        File json = new File(base + "/cucumber.json");
        File outDir = new File(base + "/advanced-reports");

        System.out.println("[ReportRegen] Suite: " + suite + " | JSON: " + json.getAbsolutePath());
        boolean ok = CucumberExtendedReportGenerator.generate(json, outDir, projectName);
        System.out.println("[ReportRegen] Üretim: " + (ok ? "BAŞARILI" : "BAŞARISIZ"));

        if (open) {
            // ReportOpener suite'e göre doğru raporu (extended yoksa index.html'e düşerek) açar.
            System.setProperty("faturalab.cucumber.report.suite", suite);
            ReportOpener.main(new String[]{});
        }
    }
}
