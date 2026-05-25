package com.faturalab.automation.hooks;

import com.faturalab.automation.api.FaturalabAPI;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CucumberHooks {
    
    private static final Logger log = LogManager.getLogger(CucumberHooks.class);
    
    // Static shared API instance
    private static FaturalabAPI sharedFaturalabAPI;
    // Static shared last response for cross-step fallback (auction ↔ invoice)
    private static Response sharedLastResponse;
    
    // Static methods to share API instance
    public static void setSharedAPI(FaturalabAPI api) {
        sharedFaturalabAPI = api;
        log.info("Shared FaturalabAPI instance set in CucumberHooks");
    }
    
    public static FaturalabAPI getSharedAPI() {
        return sharedFaturalabAPI;
    }
    
    // Shared response accessors (optional fallback)
    public static void setSharedLastResponse(Response response) {
        sharedLastResponse = response;
    }
    
    public static Response getSharedLastResponse() {
        return sharedLastResponse;
    }
    
    @Before
    public void setUp(Scenario scenario) {
        log.info("=== SCENARIO STARTED ===");
        log.info("Scenario Name: {}", scenario.getName());
        log.info("Scenario Tags: {}", scenario.getSourceTagNames());
        log.info("=======================");
        
        // Log scenario info to report using scenario.log()
        scenario.log("=== 🎯 SCENARIO INFORMATION ===");
        scenario.log("📝 Scenario: " + scenario.getName());
        scenario.log("🏷️ Tags: " + scenario.getSourceTagNames());
        scenario.log("⏰ Started: " + new java.util.Date());
        scenario.log("📊 Status: RUNNING");
        scenario.log("================================");
        scenario.log("");
    }
    
    @After
    public void tearDown(Scenario scenario) {
        log.info("=== SCENARIO FINISHED ===");
        log.info("Scenario Name: {}", scenario.getName());
        log.info("Scenario Status: {}", scenario.getStatus());
        log.info("========================");
        
        // Log scenario summary to report using scenario.log()
        scenario.log("");
        scenario.log("=== " + (scenario.isFailed() ? "❌ SCENARIO FAILED" : "✅ SCENARIO PASSED") + " ===");
        scenario.log("📝 Scenario: " + scenario.getName());
        scenario.log("📊 Status: " + scenario.getStatus());
        scenario.log("🏷️ Tags: " + scenario.getSourceTagNames());
        scenario.log("⏰ Finished: " + new java.util.Date());
        scenario.log("");
        
        if (scenario.isFailed()) {
            scenario.log("⚠️ Test failed! Check the detailed API logs above for error information.");
        } else {
            scenario.log("🎉 All API calls completed successfully!");
        }
        scenario.log("=======================");
    }
} 