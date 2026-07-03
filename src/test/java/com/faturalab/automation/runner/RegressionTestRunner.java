package com.faturalab.automation.runner;

import com.faturalab.automation.reporting.CucumberExtendedReportGenerator;
import com.faturalab.automation.utils.ReportOpener;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.io.File;

/**
 * Regression Test Runner — geniş kapsamlı {@code @regression} senaryoları.
 *
 * Bu scope, günlük API/UI sağlık koşumlarının DIŞINDA tutulan senaryoları barındırır:
 * step definition'ları henüz yazılmamış spec'ler (implementasyon backlog'u) ve
 * ileride eklenecek geniş kapsamlı regresyon senaryoları. Step'ler implemente
 * edildikçe senaryolar burada koşulur; olgunlaşan feature'lar günlük kapsama
 * (@regression tag'i kaldırılarak) terfi ettirilir.
 *
 * Çalıştırma:
 *   mvn test -Dtest=RegressionTestRunner -Denv=dev
 *
 * Alt küme:
 *   mvn test -Dtest=RegressionTestRunner -Denv=dev -Dcucumber.filter.tags="@regression and @faktoring"
 */
@CucumberOptions(
        features = {"src/test/resources/features"},
        glue = {
                "com.faturalab.automation.stepdefinitions"
        },
        tags = "@regression and not @disabled",
        plugin = {
                "pretty",
                "html:target/cucumber-reports/regression/index.html",
                "json:target/cucumber-reports/regression/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        dryRun = false
)
public class RegressionTestRunner extends AbstractTestNGCucumberTests {

    @BeforeSuite
    public void ensureDirectoriesExist() {
        System.setProperty("faturalab.cucumber.report.suite", "regression");
        new File("target/cucumber-reports/regression").mkdirs();
        System.out.println("[RegressionTestRunner] Kapsam: @regression and not @disabled");
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    // alwaysRun=true: fail durumunda da rapor üretilsin (kronik bayat rapor fix'iyle uyumlu)
    @AfterSuite(alwaysRun = true)
    public void printReport() {
        File json = new File("target/cucumber-reports/regression/cucumber.json");
        File outDir = new File("target/cucumber-reports/regression/advanced-reports");
        try {
            CucumberExtendedReportGenerator.generate(json, outDir, "Faturalab Regression Automation");
        } catch (Throwable t) {
            System.err.println("[RegressionTestRunner] Extended rapor üretimi hata verdi: " + t.getMessage());
        }
        if (Boolean.parseBoolean(System.getProperty("faturalab.open.reports", "true"))) {
            try {
                ReportOpener.main(new String[]{});
            } catch (Throwable t) {
                System.err.println("[RegressionTestRunner] Rapor açma hata verdi: " + t.getMessage());
            }
        }
        System.out.println("[RegressionTestRunner] Extended report: target/cucumber-reports/regression/advanced-reports/"
                + CucumberExtendedReportGenerator.OVERVIEW_FEATURES_HTML);
    }
}
