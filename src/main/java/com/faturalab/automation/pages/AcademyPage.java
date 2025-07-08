package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class AcademyPage extends BasePageObject {
    
    // XPath locators as provided in the task
    private final String INSTRUCTORS_LINK_XPATH = "/html/body/div/div[1]/div/div[3]/ul/li[2]";
    
    // CSS selectors for instructor items
    private final String INSTRUCTOR_ITEMS_CSS = "div.instructor-item";
    private final String FIRST_INSTRUCTOR_CSS = "div.instructor-item:nth-child(1)";
    
    @FindBy(xpath = "/html/body/div/div[1]/div/div[3]/ul/li[2]")
    private WebElement instructorsLink;
    
    public AcademyPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToAcademyPage() {
        driver.get("https://academy.faturalab.com/");
        log.info("Navigated to Faturalab Academy homepage");
        waitForPageLoad();
    }
    
    public void clickInstructorsPage() {
        // Wait for the instructors link to be clickable using the XPath
        WebElement instructorsLinkElement = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(INSTRUCTORS_LINK_XPATH)));
        
        // Highlight the element for visibility (useful for debugging)
        highlightElement(instructorsLinkElement);
        
        // Click the element
        instructorsLinkElement.click();
        
        // Wait for the URL to contain "instructors"
        wait.until(ExpectedConditions.urlContains("instructors"));
        
        log.info("Clicked on instructors page link");
        waitForPageLoad();
    }
    
    public boolean isInstructorListEmpty() {
        try {
            // Wait for the first instructor to be visible using CSS selector
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(FIRST_INSTRUCTOR_CSS)));
            
            // Scroll down to load all instructors
            scrollToBottom();
            
            // Get all instructor elements using CSS selector
            List<WebElement> instructors = driver.findElements(By.cssSelector(INSTRUCTOR_ITEMS_CSS));
            log.info("Found {} instructor elements", instructors.size());
            
            // If we found instructors, the list is NOT empty
            return instructors.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if instructor list is empty: {}", e.getMessage());
            // If there was an error finding elements, we'll assume list is empty
            return true;
        }
    }
    
    public int getInstructorCount() {
        try {
            // Wait for the first instructor to be visible using CSS selector
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(FIRST_INSTRUCTOR_CSS)));
            
            // Scroll down to load all instructors
            scrollToBottom();
            
            // Get all instructor elements using CSS selector
            List<WebElement> instructors = driver.findElements(By.cssSelector(INSTRUCTOR_ITEMS_CSS));
            int count = instructors.size();
            
            log.info("Number of instructors found: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Error counting instructors: {}", e.getMessage());
            return 0;
        }
    }
    
    // Method to scroll to the bottom of the page
    private void scrollToBottom() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        
        // Short pause to allow content to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for page to scroll: {}", e.getMessage());
        }
        
        log.info("Scrolled to bottom of page");
    }
    
    // Method to highlight an element (useful for debugging)
    private void highlightElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].setAttribute('style', 'border: 2px solid red;');", element);
    }
} 