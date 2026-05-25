package com.faturalab.automation.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import com.faturalab.automation.reporting.CucumberExtendedReportGenerator;
import com.faturalab.automation.utils.ReportOpener;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.io.File;


/**
 * UI Test Runner — https://dev.faturalab.com/app (dev2.properties base.url) Selenium UI testleri.
 *
 * Çalıştırma:
 *   mvn test -Denv=dev2 -Dcucumber.filter.tags="@ui and @dev2"
 *
 * Rol kimlik bilgileri dev2.properties içindedir:
 *   admin.email / admin.password        → Admin
 *   company.email / company.password    → EFG Gıda A.Ş. (Tedarikçi)
 *   buyer.email / buyer.password        → ALBC Marketler (Alıcı)
 *   factoring.email / factoring.password → OPR Bankası (Finansman)
 */
@CucumberOptions(
        features = {"src/test/resources/features/ui"},
        glue = {
                "com.faturalab.automation.stepdefinitions.ui"
        },
        tags = "@smoke and @happy-path and @kritik and not @disabled",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/ui/index.html",
                "json:target/cucumber-reports/ui/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        dryRun = false
)
public class UITestRunner extends AbstractTestNGCucumberTests {

    @BeforeSuite
    public void ensureDirectoriesExist() {
        System.setProperty("faturalab.cucumber.report.suite", "ui");
        // dev2 ortamını aktif et — ConfigReader dev2.properties yüklesin
        if (System.getProperty("env") == null) {
            System.setProperty("env", "dev2");
            com.faturalab.automation.config.ConfigReader.setEnvironment("dev2");
        }
        new File("target/cucumber-reports/ui").mkdirs();
        new File("target/screenshots").mkdirs();
        System.out.println("[UITestRunner] Ortam: " + System.getProperty("env", "dev2"));
        System.out.println("[UITestRunner] Raporlama dizinleri hazırlandı.");
        System.out.println("[UITestRunner] UI step definition sınıfları yüklendi (glue: ui.*).");
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @AfterSuite
    public void printReport() {
        File json = new File("target/cucumber-reports/ui/cucumber.json");
        File outDir = new File("target/cucumber-reports/ui/advanced-reports");
        CucumberExtendedReportGenerator.generate(json, outDir, "Faturalab UI Automation");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean suiteListener = Boolean.parseBoolean(
                System.getProperty("faturalab.report.listener.active", "false"));
        if (!suiteListener && Boolean.parseBoolean(System.getProperty("faturalab.open.reports", "true"))) {
            ReportOpener.main(new String[]{});
        }
        System.out.println("[UITestRunner] Extended report: target/cucumber-reports/ui/advanced-reports/"
                + CucumberExtendedReportGenerator.OVERVIEW_FEATURES_HTML);
    }
}
