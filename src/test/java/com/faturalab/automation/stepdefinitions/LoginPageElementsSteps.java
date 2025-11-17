package com.faturalab.automation.stepdefinitions;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.HomePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

/**
 * Step definitions for Login Page UI Elements validation
 * Tests verify the presence and functionality of critical login page elements:
 * - Email input field
 * - Password input field
 * - FaturaLab logo
 * - Live chat icon
 * - Language toggle button (EN/TR)
 */
public class LoginPageElementsSteps {
    
    private static final Logger log = LogManager.getLogger(LoginPageElementsSteps.class);
    private WebDriver driver;
    private HomePage homePage;
    
    public LoginPageElementsSteps() {
        this.driver = DriverManager.getDriver();
        this.homePage = new HomePage(driver);
    }
    
    @Given("user navigates to {string}")
    public void userNavigatesTo(String url) {
        log.info("Navigating to URL: {}", url);
        driver.get(url);
        
        // Wait for page to fully load - Vaadin takes time to render
        try {
            // Wait longer for Vaadin framework to initialize and render the page
            Thread.sleep(5000);
            log.info("Waited 5 seconds for Vaadin framework to initialize");
            
            // Additional wait to ensure all elements are rendered
            org.openqa.selenium.support.ui.WebDriverWait wait = new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15));
            wait.until(driver -> {
                // Wait until at least one input field is visible (sign that page is loaded)
                try {
                    return !driver.findElements(org.openqa.selenium.By.cssSelector("input")).isEmpty();
                } catch (Exception e) {
                    return false;
                }
            });
            log.info("✅ Page elements are now visible");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for page load");
        } catch (Exception e) {
            log.warn("Timeout waiting for page elements, but continuing: {}", e.getMessage());
        }
        
        log.info("Page loaded successfully");
    }
    
    @Then("email input field should be visible")
    public void emailInputFieldShouldBeVisible() {
        log.info("Verifying email input field is displayed");
        boolean isDisplayed = homePage.isEmailInputDisplayed();
        Assert.assertTrue(isDisplayed, "Email input field should be visible");
        log.info("✅ Email input field is displayed");
    }
    
    @Then("password input field should be visible")
    public void passwordInputFieldShouldBeVisible() {
        log.info("Verifying password input field is displayed");
        boolean isDisplayed = homePage.isPasswordInputDisplayed();
        Assert.assertTrue(isDisplayed, "Password input field should be visible");
        log.info("✅ Password input field is displayed");
    }
    
    @Then("FaturaLab logo should be visible")
    public void faturaLabLogoShouldBeVisible() {
        log.info("Verifying FaturaLab logo is displayed");
        boolean isDisplayed = homePage.isLogoButtonDisplayed();
        Assert.assertTrue(isDisplayed, "FaturaLab logo should be visible");
        log.info("✅ FaturaLab logo is displayed");
    }
    
    @Then("live chat icon should be visible")
    public void liveChatIconShouldBeVisible() {
        log.info("Verifying live chat icon is displayed");
        boolean isDisplayed = homePage.isLiveChatDisplayed();
        Assert.assertTrue(isDisplayed, "Live chat icon should be visible");
        log.info("✅ Live chat icon is displayed");
    }
    
    @Then("language toggle button should be visible")
    public void languageToggleButtonShouldBeVisible() {
        log.info("Verifying language toggle button is displayed");
        boolean isDisplayed = homePage.isLanguageButtonDisplayed();
        Assert.assertTrue(isDisplayed, "Language toggle button should be visible");
        log.info("✅ Language toggle button is displayed");
    }
    
    @Then("language button should display {string} text")
    public void languageButtonShouldDisplayText(String expectedLanguage) {
        log.info("Verifying language button shows text: {}", expectedLanguage);
        String actualLanguage = homePage.getLanguageButtonText();
        Assert.assertEquals(actualLanguage, expectedLanguage, 
                String.format("Language button should display '%s' but shows '%s'", 
                        expectedLanguage, actualLanguage));
        log.info("✅ Language button shows: {}", actualLanguage);
    }
    
    @When("user clicks the language toggle button")
    public void userClicksTheLanguageToggleButton() {
        clickLanguageButton();
    }
    
    private void clickLanguageButton() {
        log.info("Clicking language toggle button");
        homePage.clickLanguageButton();
        // Wait for page to update after language change
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("✅ Language toggle button clicked");
    }
    
    @And("page should be in Turkish language")
    public void pageShouldBeInTurkishLanguage() {
        log.info("Verifying page is in Turkish language");
        String currentLanguage = homePage.getLanguageButtonText();
        Assert.assertEquals(currentLanguage, "TR", 
                "Page should show 'TR' when switched to Turkish");
        log.info("✅ Page is in Turkish (button shows TR)");
    }
    
    @And("page should be in English language")
    public void pageShouldBeInEnglishLanguage() {
        log.info("Verifying page is in English language");
        String currentLanguage = homePage.getLanguageButtonText();
        Assert.assertEquals(currentLanguage, "EN", 
                "Page should show 'EN' when switched to English");
        log.info("✅ Page is in English (button shows EN)");
    }
}

