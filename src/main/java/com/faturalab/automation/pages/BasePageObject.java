package com.faturalab.automation.pages;

import com.faturalab.automation.utils.WaitHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePageObject {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WaitHelper waitHelper;
    protected static final Logger log = LogManager.getLogger(BasePageObject.class);
    
    public BasePageObject(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.waitHelper = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
    }
    
    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.click();
        log.info("Clicked on element: {}", element);
    }
    
    protected void sendKeys(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
        log.info("Entered text '{}' into element: {}", text, element);
    }
    
    protected String getText(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        String text = element.getText();
        log.info("Retrieved text '{}' from element: {}", text, element);
        return text;
    }
    
    protected boolean isDisplayed(WebElement element) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            return element.isDisplayed();
        } catch (Exception e) {
            log.error("Element is not displayed: {}", e.getMessage());
            return false;
        }
    }
    
    protected void scrollToElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        log.info("Scrolled to element: {}", element);
    }
    
    protected void waitForPageLoad() {
        waitHelper.waitForPageLoad();
    }
    
    /**
     * Finds an element using By locator
     * @param locator By locator
     * @return WebElement
     */
    protected WebElement findElement(By locator) {
        WebElement element = driver.findElement(locator);
        log.debug("Found element using locator: {}", locator);
        return element;
    }
    
    /**
     * Waits for element to be clickable
     * @param locator By locator
     * @return WebElement that is clickable
     */
    protected WebElement waitForElementToBeClickable(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        log.debug("Element is clickable: {}", locator);
        return element;
    }
    
    /**
     * Waits for element visibility with custom timeout
     * @param locator By locator
     * @param timeoutInSeconds Custom timeout in seconds
     * @return WebElement that is visible
     */
    protected WebElement waitForVisibility(By locator, int timeoutInSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        WebElement element = customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        log.debug("Element is visible: {}", locator);
        return element;
    }
} 