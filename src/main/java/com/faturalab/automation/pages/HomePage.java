package com.faturalab.automation.pages;

import com.faturalab.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePageObject {
    
    // Logo XPath as provided
    private final String LOGO_XPATH = "/html/body/div/div[1]/div/div[1]";
    
    @FindBy(xpath = "/html/body/div/div[1]/div/div[1]")
    private WebElement logoElement;
    
    public HomePage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToHomePage() {
        String baseUrl = ConfigReader.getProperty("base.url");
        driver.get(baseUrl);
        log.info("Navigated to homepage: {}", baseUrl);
        waitForPageLoad();
    }
    
    public String getPageTitle() {
        String title = driver.getTitle();
        log.info("Page title is: {}", title);
        return title;
    }
    
    public boolean isLogoDisplayed() {
        try {
            // Dynamic finding to ensure we have the latest state
            WebElement logo = driver.findElement(By.xpath(LOGO_XPATH));
            boolean isDisplayed = isDisplayed(logo);
            log.info("Faturalab logo is displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.error("Error checking if logo is displayed: {}", e.getMessage());
            return false;
        }
    }
} 