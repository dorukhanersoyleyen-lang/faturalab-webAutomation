package com.faturalab.automation.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;

import java.io.File;


/**
 * UI Test Runner — dev2.faturalab.com üzerindeki Selenium tabanlı UI testleri.
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
        tags = "@ui and @dev2 and not @disabled",
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
        new File("target/cucumber-reports/ui").mkdirs();
        new File("target/screenshots").mkdirs();
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
        System.out.println("[UITestRunner] UI test raporu: target/cucumber-reports/ui/index.html");
    }
}
