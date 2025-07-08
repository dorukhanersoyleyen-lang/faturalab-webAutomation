package com.faturalab.automation.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.faturalab.automation.stepdefinitions.HomePageSteps;
import com.faturalab.automation.stepdefinitions.FaturalabAcademySteps;
import com.faturalab.automation.stepdefinitions.Hooks;

@CucumberOptions(
        features = {"src/test/resources/features"},
        glue = {"com.faturalab.automation.stepdefinitions"},
        plugin = {
                "pretty",
                "html:target/cucumber-reports/cucumber-pretty.html",
                "json:target/cucumber-reports/CucumberTestReport.json",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
        },
        monochrome = true,
        dryRun = false
)
public class TestRunner extends AbstractTestNGCucumberTests {
    
    static {
        // Ensure step definitions path is correctly set for the JVM
        String gluePackage = "com.faturalab.automation.stepdefinitions";
        System.setProperty("cucumber.glue", gluePackage);
        System.out.println("Cucumber glue package is set to: " + gluePackage);
        
        // Force step definition class loading
        try {
            System.out.println("Trying to force load step definition classes...");
            Class.forName("com.faturalab.automation.stepdefinitions.HomePageSteps");
            Class.forName("com.faturalab.automation.stepdefinitions.FaturalabAcademySteps");
            Class.forName("com.faturalab.automation.stepdefinitions.Hooks");
            System.out.println("Step definition classes loaded successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("Step definition classes could not be loaded: " + e.getMessage());
        }
    }
    
    @BeforeTest
    public void registerStepDefinitions() {
        System.out.println("Starting step definition class registration process...");
        
        // Force create instances of step definition classes
        try {
            System.out.println("Loading HomePageSteps class...");
            new HomePageSteps();
            
            System.out.println("Loading FaturalabAcademySteps class...");
            new FaturalabAcademySteps();
            
            System.out.println("Loading Hooks class...");
            new Hooks();
            
            System.out.println("All step definition classes loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error occurred while loading step definition classes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @BeforeSuite
    public void ensureDirectoriesExist() {
        try {
            // Ensure report directories exist
            File reportDir = new File("target/cucumber-reports");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
                System.out.println("Report directory created: " + reportDir.getAbsolutePath());
            }
            
            // Check step definition directory
            File stepDefinitionsDir = new File("target/test-classes/com/faturalab/automation/stepdefinitions");
            if (!stepDefinitionsDir.exists()) {
                System.out.println("Step definition directory doesn't exist yet, will be created when test runs: " 
                        + stepDefinitionsDir.getAbsolutePath());
            } else {
                System.out.println("Step definition directory exists: " + stepDefinitionsDir.getAbsolutePath());
                // Check directory contents
                File[] files = stepDefinitionsDir.listFiles();
                if (files != null && files.length > 0) {
                    System.out.println("Step definition classes found: " + files.length + " files");
                    for (File file : files) {
                        System.out.println(" - " + file.getName());
                    }
                } else {
                    System.out.println("Step definition directory is empty!");
                }
            }
        } catch (Exception e) {
            System.err.println("Error occurred while checking directories: " + e.getMessage());
        }
    }
    
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
    
    @AfterSuite
    public void generateReport() {
        File reportOutputDirectory = new File("target/cucumber-reports/advanced-reports");
        List<String> jsonFiles = new ArrayList<>();
        jsonFiles.add("target/cucumber-reports/CucumberTestReport.json");

        // Build Configuration
        String projectName = "Faturalab Web Automation";
        Configuration configuration = new Configuration(reportOutputDirectory, projectName);
        configuration.setBuildNumber("1.0");
        configuration.addClassifications("Platform", System.getProperty("os.name"));
        configuration.addClassifications("Browser", "Chrome"); // You can get this value from config file
        configuration.addClassifications("Environment", "Test"); // You can get this value from config file

        // Build Report
        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
        reportBuilder.generateReports();
        
        System.out.println("Cucumber Advanced Reports generated at: " + reportOutputDirectory.getAbsolutePath());
        
        // Open report automatically
        try {
            File htmlReport = new File("target/cucumber-reports/advanced-reports/cucumber-html-reports/overview-features.html");
            if (htmlReport.exists() && Desktop.isDesktopSupported()) {
                System.out.println("Opening report automatically: " + htmlReport.getAbsolutePath());
                Desktop.getDesktop().browse(htmlReport.toURI());
            } else {
                System.out.println("Report file not found or system does not support this feature: " + htmlReport.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error occurred while opening report: " + e.getMessage());
        }
    }
} 