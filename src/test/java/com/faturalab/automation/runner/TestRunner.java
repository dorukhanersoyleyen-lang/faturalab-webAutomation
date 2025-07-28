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
        glue = {"com.faturalab.automation.stepdefinitions", "com.faturalab.automation.hooks"},
        tags = "not @disabled",  // ALL TESTS except disabled
        plugin = {
                "pretty",
                "html:target/cucumber-reports/index.html",
                "json:target/cucumber-reports/cucumber.json",
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
        try {
            File jsonFile = new File("target/cucumber-reports/cucumber.json");
            
            // Check if JSON file exists and has content
            if (!jsonFile.exists()) {
                System.out.println("Cucumber JSON report not found. Skipping advanced report generation.");
                return;
            }
            
            if (jsonFile.length() == 0) {
                System.out.println("Cucumber JSON report is empty. Skipping advanced report generation.");
                return;
            }
            
            // Read file content to validate JSON
            String jsonContent = new String(java.nio.file.Files.readAllBytes(jsonFile.toPath()));
            if (jsonContent.trim().isEmpty() || jsonContent.equals("[]")) {
                System.out.println("Cucumber JSON report contains no test results. Skipping advanced report generation.");
                return;
            }
            
            File reportOutputDirectory = new File("target/cucumber-reports/advanced-reports");
            List<String> jsonFiles = new ArrayList<>();
            jsonFiles.add("target/cucumber-reports/cucumber.json");

            // Build Configuration
            String projectName = "Faturalab Web Automation";
            Configuration configuration = new Configuration(reportOutputDirectory, projectName);
            configuration.setBuildNumber("1.0");
            configuration.addClassifications("Platform", System.getProperty("os.name"));
            configuration.addClassifications("Browser", "Chrome");
            configuration.addClassifications("Environment", "Test");

            // Build Report
            ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
            reportBuilder.generateReports();
            
            System.out.println("Cucumber Advanced Reports generated at: " + reportOutputDirectory.getAbsolutePath());
            System.out.println("Advanced Report File: target/cucumber-reports/advanced-reports/cucumber-html-reports/overview-features.html");
            
            // Wait for files to be written completely
            try {
                Thread.sleep(2000); // Wait 2 seconds for advanced report files
                System.out.println("⏳ Waiting for advanced report files to be written...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Open report automatically using ReportOpener
            try {
                System.out.println("🌐 Opening test reports using ReportOpener...");
                System.out.println("📊 Priority: Advanced Report > Basic Report > TestNG Report");
                com.faturalab.automation.utils.ReportOpener.main(new String[]{});
            } catch (Exception e) {
                System.err.println("❌ Error occurred while opening report: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            System.err.println("Error occurred while generating cucumber report: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 