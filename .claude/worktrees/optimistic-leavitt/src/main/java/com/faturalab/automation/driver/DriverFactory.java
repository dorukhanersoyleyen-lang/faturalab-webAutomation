package com.faturalab.automation.driver;

import com.faturalab.automation.config.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

public class DriverFactory {
    
    private static final Logger log = LogManager.getLogger(DriverFactory.class);
    
    private DriverFactory() {
        // Private constructor to prevent instantiation
    }
    
    public static WebDriver createDriver() {
        String browser = ConfigReader.getProperty("browser", "chrome").toLowerCase();
        boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless", "false"));
        
        log.info("Creating WebDriver instance for browser: {}, headless: {}", browser, headless);
        
        switch (browser) {
            case "chrome":
                return createChromeDriver(headless);
            case "firefox":
                return createFirefoxDriver(headless);
            case "edge":
                return createEdgeDriver(headless);
            case "safari":
                return createSafariDriver();
            default:
                log.warn("Unknown browser: {}. Defaulting to Chrome", browser);
                return createChromeDriver(headless);
        }
    }
    
    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        
        // Server/CI environment stability options
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--ignore-certificate-errors");
        
        log.info("Chrome WebDriver initialized with options: {}", options);
        return new ChromeDriver(options);
    }
    
    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        
        if (headless) {
            options.addArguments("-headless");
        }
        
        log.info("Firefox WebDriver initialized with options: {}", options);
        return new FirefoxDriver(options);
    }
    
    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        
        if (headless) {
            options.addArguments("--headless=new");
        }
        
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        
        log.info("Edge WebDriver initialized with options: {}", options);
        return new EdgeDriver(options);
    }
    
    private static WebDriver createSafariDriver() {
        WebDriverManager.safaridriver().setup();
        SafariOptions options = new SafariOptions();
        
        log.info("Safari WebDriver initialized with options: {}", options);
        return new SafariDriver(options);
    }
} 