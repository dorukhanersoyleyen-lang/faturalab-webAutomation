package com.faturalab.automation.listeners;

import com.faturalab.automation.utils.ReportOpener;
import org.testng.ISuiteListener;
import org.testng.ISuite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestReportListener implements ISuiteListener {
    
    private static final Logger log = LogManager.getLogger(TestReportListener.class);
    
    @Override
    public void onFinish(ISuite suite) {
        // Console output for immediate visibility
        System.out.println("=".repeat(60));
        System.out.println("🎉 TEST SUITE COMPLETED!");
        System.out.println("Suite Name: " + suite.getName());
        System.out.println("=".repeat(60));
        
        log.info("=== ALL TESTS COMPLETED ===");
        log.info("Suite: {}", suite.getName());
        
        try {
            // Calculate test results
            int passed = suite.getResults().values().stream()
                    .mapToInt(r -> r.getTestContext().getPassedTests().size()).sum();
            int failed = suite.getResults().values().stream()
                    .mapToInt(r -> r.getTestContext().getFailedTests().size()).sum();
            int skipped = suite.getResults().values().stream()
                    .mapToInt(r -> r.getTestContext().getSkippedTests().size()).sum();
            
            System.out.println("📊 Test Results:");
            System.out.println("   ✅ Passed: " + passed);
            System.out.println("   ❌ Failed: " + failed);
            System.out.println("   ⏭️ Skipped: " + skipped);
            System.out.println("");
            
            log.info("Test results: Passed={}, Failed={}, Skipped={}", passed, failed, skipped);
            
        } catch (Exception e) {
            System.err.println("❌ Error calculating test results: " + e.getMessage());
            log.error("Error calculating test results: {}", e.getMessage());
        }
        
        log.info("===========================");
        
        // Open test reports in browser
        try {
            System.out.println("🌐 Starting report opener...");
            System.out.println("📊 Attempting to open test reports in browser...");
            
            // Run ReportOpener in a separate thread to avoid blocking
            Thread reportOpenerThread = new Thread(() -> {
                try {
                    System.out.println("⏳ Waiting 3 seconds for report files to be generated...");
                    Thread.sleep(3000); // Wait 3 seconds for files to be written
                    
                    System.out.println("🚀 Executing ReportOpener...");
                    ReportOpener.main(new String[]{});
                    
                } catch (InterruptedException e) {
                    System.err.println("⚠️ Report opener thread interrupted: " + e.getMessage());
                    log.error("Report opener thread interrupted: {}", e.getMessage());
                } catch (Exception e) {
                    System.err.println("❌ Error in report opener: " + e.getMessage());
                    log.error("Error opening reports: {}", e.getMessage());
                    e.printStackTrace();
                }
            });
            
            reportOpenerThread.setName("ReportOpener-Thread");
            reportOpenerThread.setDaemon(false); // Make it non-daemon so it completes
            reportOpenerThread.start();
            
            System.out.println("✅ Report opener thread started successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to start report opener: " + e.getMessage());
            log.error("Failed to start report opener: {}", e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=".repeat(60));
    }
    
    @Override
    public void onStart(ISuite suite) {
        System.out.println("=".repeat(60));
        System.out.println("🚀 STARTING TEST SUITE");
        System.out.println("Suite Name: " + suite.getName());
        System.out.println("=".repeat(60));
        
        log.info("=== STARTING TEST SUITE ===");
        log.info("Suite: {}", suite.getName());
        
        try {
            int testCount = suite.getAllMethods().size();
            System.out.println("📝 Total tests to run: " + testCount);
            log.info("Tests to run: {}", testCount);
        } catch (Exception e) {
            System.err.println("⚠️ Could not determine test count: " + e.getMessage());
            log.warn("Could not determine test count: {}", e.getMessage());
        }
        
        log.info("===========================");
        System.out.println("");
    }
} 