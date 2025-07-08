package com.faturalab.automation.stepdefinitions;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.HomePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class HomePageSteps {
    
    private final WebDriver driver;
    private final HomePage homePage;
    
    public HomePageSteps() {
        this.driver = DriverManager.getDriver();
        this.homePage = new HomePage(driver);
    }
    
    @Given("user navigates to the homepage")
    public void user_navigates_to_the_homepage() {
        homePage.navigateToHomePage();
    }
    
    @Then("the page title should contain {string}")
    public void the_page_title_should_contain(String expectedTitle) {
        String actualTitle = homePage.getPageTitle();
        Assert.assertTrue(actualTitle.contains(expectedTitle), 
                "Page title does not contain '" + expectedTitle + "'. Actual title: " + actualTitle);
    }
    
        @And("the Faturalab logo should be displayed")
    public void the_faturalab_logo_should_be_displayed() {
        Assert.assertTrue(homePage.isLogoDisplayed(),
                "Faturalab logo is not displayed");
        System.out.println("Verified that the Faturalab logo is displayed");
    }
} 