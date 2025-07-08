package com.faturalab.automation.stepdefinitions;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.AcademyPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class FaturalabAcademySteps {
    
    private final WebDriver driver;
    private final AcademyPage academyPage;
    
    public FaturalabAcademySteps() {
        this.driver = DriverManager.getDriver();
        this.academyPage = new AcademyPage(driver);
    }
    
    @Given("user navigates to the Faturalab Academy page")
    public void user_navigates_to_the_faturalab_academy_page() {
        // Navigate to the actual Faturalab Academy page
        academyPage.navigateToAcademyPage();
        System.out.println("User navigated to the Faturalab Academy page");
    }
    
    @When("user clicks on the instructors page")
    public void user_clicks_on_the_instructors_page() {
        // Click on instructors page using the method in AcademyPage
        academyPage.clickInstructorsPage();
        System.out.println("User clicked on the instructors page");
    }
    
    @Then("the instructor list should not be empty")
    public void the_instructor_list_should_not_be_empty() {
        // Check that the instructor list is not empty using AcademyPage
        boolean isEmpty = academyPage.isInstructorListEmpty();
        System.out.println("Is instructor list empty? " + isEmpty);
        
        // If isEmpty is true, the list is empty. We expect it to be false (not empty)
        Assert.assertFalse(isEmpty, "The instructor list should not be empty");
        System.out.println("Verified that the instructor list is not empty");
    }
    
    @Then("the instructor count should be {int}")
    public void the_instructor_count_should_be(Integer expectedCount) {
        // Get and verify the instructor count using AcademyPage
        int actualCount = academyPage.getInstructorCount();
        System.out.println("Expected instructor count: " + expectedCount);
        System.out.println("Actual instructor count: " + actualCount);
        
        // Assert that the actual count matches the expected count
        Assert.assertEquals(actualCount, expectedCount.intValue(), 
                "Expected " + expectedCount + " instructors but found " + actualCount);
        
        System.out.println("Verified that the instructor count is " + expectedCount);
    }
} 