package com.faturalab.automation.stepdefinitions;

import com.faturalab.automation.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Hooks {
    
    private static final Logger log = LogManager.getLogger(Hooks.class);
    
    @Before
    public void setUp(Scenario scenario) {
        log.info("Starting scenario: {}", scenario.getName());
        
        // Ensure WebDriver directory exists for screenshots
        File screenshotsDir = new File("target/screenshots");
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs();
        }
        
        // Initialize the WebDriver through the DriverManager
        WebDriver driver = DriverManager.getDriver();
        log.info("WebDriver initialized with session ID: {}", driver.toString());
    }
    
    @After
    public void tearDown(Scenario scenario) {
        log.info("Finishing scenario: {}, Status: {}", scenario.getName(), scenario.getStatus());
        
        if (scenario.isFailed()) {
            WebDriver driver = DriverManager.getDriver();
            captureScreenshot(scenario, driver);
        }
        
        // Quit the WebDriver
        DriverManager.quitDriver();
    }
    
    @AfterStep
    public void afterStep(Scenario scenario) {
        if (scenario.isFailed()) {
            WebDriver driver = DriverManager.getDriver();
            final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Failed step screenshot");
            log.info("Screenshot taken for failed step in scenario: {}", scenario.getName());
        }
    }
    
    private void captureScreenshot(Scenario scenario, WebDriver driver) {
        try {
            // Take screenshot as file
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            
            // Format filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String scenarioName = scenario.getName().replaceAll("\\s+", "_");
            String filename = "screenshot_" + scenarioName + "_" + timestamp + ".png";
            
            // Save file
            File destFile = new File("target/screenshots/" + filename);
            FileUtils.copyFile(srcFile, destFile);
            
            log.info("Screenshot saved as: {}", destFile.getAbsolutePath());
            
            // Attach to scenario report
            final byte[] screenshot = FileUtils.readFileToByteArray(srcFile);
            scenario.attach(screenshot, "image/png", "Failure Screenshot");
        } catch (IOException e) {
            log.error("Failed to capture screenshot: {}", e.getMessage());
        }
    }
} 