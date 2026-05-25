package com.faturalab.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitHelper {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final Logger log = LogManager.getLogger(WaitHelper.class);
    
    public WaitHelper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }
    
    public void waitForPageLoad() {
        ExpectedCondition<Boolean> pageLoadCondition = driver -> {
            assert driver != null;
            return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
        };
        
        try {
            wait.until(pageLoadCondition);
            log.info("Page has been fully loaded");
        } catch (Exception e) {
            log.error("Timeout waiting for page load completion: {}", e.getMessage());
        }
    }
    
    public void waitForElementToBeClickable(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            log.info("Element is now clickable: {}", element);
        } catch (Exception e) {
            log.error("Element is not clickable: {}", e.getMessage());
        }
    }
    
    public void waitForElementToBeVisible(WebElement element) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            log.info("Element is now visible: {}", element);
        } catch (Exception e) {
            log.error("Element is not visible: {}", e.getMessage());
        }
    }
    
    public void waitForURLToContain(String partialURL) {
        try {
            wait.until(ExpectedConditions.urlContains(partialURL));
            log.info("URL contains: {}", partialURL);
        } catch (Exception e) {
            log.error("URL does not contain '{}': {}", partialURL, e.getMessage());
        }
    }
    
    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
            log.info("Waited for {} milliseconds", milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sleep interrupted: {}", e.getMessage());
        }
    }
} 