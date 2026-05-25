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
 * reCAPTCHA Entegrasyon Test Runner — TC-RCAP-01 ile TC-RCAP-14.
 *
 * Calistirma:
 *   mvn test -Denv=dev2 -Dtest=ReCaptchaTestRunner
 *
 * Yalnizca @smoke senaryolarini calistirmak icin:
 *   mvn test -Denv=dev2 -Dtest=ReCaptchaTestRunner -Dcucumber.filter.tags="@recaptcha and @smoke"
 *
 * Yavas senaryolar dahil (TC-07, TC-11, TC-12):
 *   mvn test -Denv=dev2 -Dtest=ReCaptchaTestRunner -Dcucumber.filter.tags="@recaptcha"
 */
@CucumberOptions(
        features = {"src/test/resources/features/ui/ReCaptchaFlow.feature"},
        glue    = {"com.faturalab.automation.stepdefinitions.ui"},
        tags    = "@recaptcha and @smoke and not @disabled",
        plugin  = {
                "pretty",
                "html:target/cucumber-reports/recaptcha/index.html",
                "json:target/cucumber-reports/recaptcha/cucumber.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        dryRun     = false
)
public class ReCaptchaTestRunner extends AbstractTestNGCucumberTests {

    @BeforeSuite
    public void setup() {
        System.setProperty("faturalab.cucumber.report.suite", "recaptcha");
        if (System.getProperty("env") == null) {
            System.setProperty("env", "dev2");
            com.faturalab.automation.config.ConfigReader.setEnvironment("dev2");
        }
        new File("target/cucumber-reports/recaptcha").mkdirs();
        new File("target/screenshots").mkdirs();
        System.out.println("[ReCaptchaTestRunner] Ortam: " + System.getProperty("env", "dev2"));
    }

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }

    @AfterSuite
    public void report() {
        File json   = new File("target/cucumber-reports/recaptcha/cucumber.json");
        File outDir = new File("target/cucumber-reports/recaptcha/advanced-reports");
        CucumberExtendedReportGenerator.generate(json, outDir, "FaturaLab reCAPTCHA Tests");
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if (Boolean.parseBoolean(System.getProperty("faturalab.open.reports", "true"))) {
            ReportOpener.main(new String[]{});
        }
    }
}
