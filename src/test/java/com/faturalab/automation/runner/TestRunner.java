package com.faturalab.automation.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;

import java.io.File;
import com.faturalab.automation.reporting.CucumberExtendedReportGenerator;
import com.faturalab.automation.stepdefinitions.HomePageSteps;
import com.faturalab.automation.stepdefinitions.Hooks;
import com.faturalab.automation.stepdefinitions.LoginPageElementsSteps;
import com.faturalab.automation.stepdefinitions.TedarikciYonetimiSteps;
// Force include invoice and auction stepdefs
import com.faturalab.automation.stepdefinitions.invoice.InvoiceManagementStepDefs;
import com.faturalab.automation.stepdefinitions.auction.AuctionInvoiceUploadStepDefs;

@CucumberOptions(
        features = {"src/test/resources/features"},
        glue = {
                "com.faturalab.automation.stepdefinitions"
        },
        tags = "not @disabled and not @ui",  // non-UI tests; @ui tests are handled by UITestRunner
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
    
    
    @BeforeTest
    public void registerStepDefinitions() {
        System.out.println("Starting step definition class registration process...");
        
        // Force create instances of step definition classes
        try {
            System.out.println("Loading HomePageSteps class...");
            new HomePageSteps();
            
            System.out.println("Loading Hooks class...");
            new Hooks();
            
            System.out.println("Loading LoginPageElementsSteps class...");
            new LoginPageElementsSteps();
            
            System.out.println("Loading TedarikciYonetimiSteps class...");
            new TedarikciYonetimiSteps();
            
            System.out.println("Loading InvoiceManagementStepDefs class...");
            new InvoiceManagementStepDefs();
            
            System.out.println("Loading AuctionInvoiceUploadStepDefs class...");
            new AuctionInvoiceUploadStepDefs();

            System.out.println("All step definition classes loaded successfully!");
        } catch (Exception e) {
            System.err.println("Error occurred while loading step definition classes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @BeforeSuite
    public void markReportSuite() {
        System.setProperty("faturalab.cucumber.report.suite", "api");
    }

    @BeforeSuite(dependsOnMethods = "markReportSuite")
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
        File jsonFile = new File("target/cucumber-reports/cucumber.json");
        File outDir = new File("target/cucumber-reports/advanced-reports");
        CucumberExtendedReportGenerator.generate(jsonFile, outDir, "Faturalab Web Automation");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // testng.xml + TestReportListener açıyorsa çift sekme olmasın
        boolean suiteListener = Boolean.parseBoolean(
                System.getProperty("faturalab.report.listener.active", "false"));
        if (!suiteListener && Boolean.parseBoolean(System.getProperty("faturalab.open.reports", "true"))) {
            try {
                com.faturalab.automation.utils.ReportOpener.main(new String[]{});
            } catch (Exception e) {
                System.err.println("Error opening report: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 