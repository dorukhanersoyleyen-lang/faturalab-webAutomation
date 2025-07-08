package com.faturalab.automation.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class DriverManager {
    
    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    
    private DriverManager() {
        // Private constructor to prevent instantiation
    }
    
    public static WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            log.info("Creating new WebDriver instance for thread: {}", Thread.currentThread().getId());
            WebDriver driver = DriverFactory.createDriver();
            
            // Set default timeouts
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
            
            driverThreadLocal.set(driver);
        }
        
        return driverThreadLocal.get();
    }
    
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            log.info("Quitting WebDriver for thread: {}", Thread.currentThread().getId());
            driver.quit();
            driverThreadLocal.remove();
        }
    }
} 