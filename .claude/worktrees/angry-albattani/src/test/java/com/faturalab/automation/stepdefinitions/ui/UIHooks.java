package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.context.RoleSessionManager;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.utils.QAHubReporter;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class UIHooks {

    private static final Logger log = LogManager.getLogger(UIHooks.class);

    @Before("@ui")
    public void uiSetup(Scenario scenario) {
        log.info("[UI] Senaryo baslatiliyor: {}", scenario.getName());
        QAHubReporter.initRun();
        DriverManager.getDriver(); // initialize driver
    }

    @After("@ui")
    public void uiTeardown(Scenario scenario) {
        log.info("[UI] Senaryo bitti: {}", scenario.getStatus());
        String errorMsg = scenario.isFailed() ? "Senaryo başarısız oldu." : null;
        QAHubReporter.reportResult(scenario.getName(), scenario.getStatus().name(), errorMsg);
        if (scenario.isFailed()) {
            try {
                WebDriver driver = DriverManager.getDriver();
                final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
            } catch (Exception e) {
                log.warn("Screenshot alinamadi: {}", e.getMessage());
            }
        }
        RoleSessionManager.clearAllSessions();
        DriverManager.quitDriver();
    }

    @AfterStep("@ui")
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            try {
                WebDriver driver = DriverManager.getDriver();
                final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failed step screenshot");
            } catch (Exception e) {
                log.warn("AfterStep screenshot alinamadi: {}", e.getMessage());
            }
        }
    }
}
