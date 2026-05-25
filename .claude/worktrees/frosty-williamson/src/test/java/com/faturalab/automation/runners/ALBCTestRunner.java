package com.faturalab.automation.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    features = {"src/test/resources/features/FaturaUploadFlow.feature"},
    glue = {"com.faturalab.automation.stepdefinitions"},
    tags = "@albc",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/albc-test-results.html",
        "json:target/cucumber-reports/albc-test-results.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = true,
    dryRun = false
)
public class ALBCTestRunner extends AbstractTestNGCucumberTests {
    
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
} 