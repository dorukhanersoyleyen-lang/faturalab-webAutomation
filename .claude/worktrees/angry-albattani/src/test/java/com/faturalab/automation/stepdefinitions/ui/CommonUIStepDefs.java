package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import io.cucumber.java.en.Then;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class CommonUIStepDefs {

    @Then("basari bildirimi gorunmeli")
    public void basariBildirimi() {
        WebDriver driver = DriverManager.getDriver();
        boolean found = driver.findElements(
                By.cssSelector("vaadin-notification, .notification, [role='alert']")).size() > 0;
        if (!found) {
            String src = driver.getPageSource();
            found = src.contains("success") || src.contains("basarili") || src.contains("Basarili")
                    || src.contains("onaylandi") || src.contains("Onaylandi");
        }
        Assert.assertTrue(found, "Basari bildirimi gorunmeli");
    }
}
