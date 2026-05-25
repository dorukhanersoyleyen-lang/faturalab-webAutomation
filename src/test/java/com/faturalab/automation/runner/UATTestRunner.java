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
 * UAT Test Runner — FaturaLab V2 UAT senaryoları (FL-001 … FL-019).
 *
 * Çalıştırma:
 *   mvn test -Denv=dev2 -DsuiteXmlFile=testng-uat.xml
 *
 * Etiket filtresi (ör. tek case):
 *   mvn test -Denv=dev2 -DsuiteXmlFile=testng-uat.xml -Dcucumber.filter.tags="@fl-001"
 *
 * Tüm UAT (runner içi etiket: @uat and not @disabled):
 *   mvn test -Denv=dev2 -DsuiteXmlFile=testng-uat.xml
 *
 * Sadece kritik senaryolar:
 *   mvn test -Denv=dev2 -DsuiteXmlFile=testng-uat.xml -Dcucumber.filter.tags="@uat and @kritik"
 *
 * Rol kimlik bilgileri dev2.properties içindedir:
 *   admin.email / admin.password        → Admin
 *   company.email / company.password    → EFG Gıda A.Ş. (Tedarikçi)
 *   buyer.email / buyer.password        → ALBC Marketler (Alıcı)
 *   factoring.email / factoring.password → OPR Bankası (Finansman)
 */
@CucumberOptions(
        features = {"src/test/resources/features/ui/uat"},
        glue = {
                "com.faturalab.automation.stepdefinitions.ui"
        },
        tags = "@uat and not @disabled",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/uat/index.html",
                "json:target/cucumber-reports/uat/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        dryRun = false
)
public class UATTestRunner extends AbstractTestNGCucumberTests {

    @BeforeSuite
    public void ensureDirectoriesExist() {
        System.setProperty("faturalab.cucumber.report.suite", "uat");
        if (System.getProperty("env") == null) {
            System.setProperty("env", "dev2");
            com.faturalab.automation.config.ConfigReader.setEnvironment("dev2");
        }
        new File("target/cucumber-reports/uat").mkdirs();
        new File("target/screenshots").mkdirs();
        System.out.println("[UATTestRunner] Ortam: " + System.getProperty("env", "dev2"));
        System.out.println("[UATTestRunner] UAT raporlama dizinleri hazırlandı.");
        System.out.println("[UATTestRunner] UAT senaryoları: FL-001 — FL-019");
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @AfterSuite
    public void printReport() {
        File json = new File("target/cucumber-reports/uat/cucumber.json");
        File outDir = new File("target/cucumber-reports/uat/advanced-reports");
        CucumberExtendedReportGenerator.generate(json, outDir, "Faturalab UAT Automation");
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
        System.out.println("[UATTestRunner] Extended report: target/cucumber-reports/uat/advanced-reports/"
                + CucumberExtendedReportGenerator.OVERVIEW_FEATURES_HTML);
    }
}
