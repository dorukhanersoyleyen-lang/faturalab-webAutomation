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

        // Extended raporu Maven JVM kapanmadan önce aç: aynı thread (bloklar) — arka planda thread Maven ile kesiliyordu
        if (!Boolean.parseBoolean(System.getProperty("faturalab.open.reports", "true"))) {
            log.info("Rapor otomatik acilmiyor (faturalab.open.reports=false ile kapatildi).");
        } else try {
            int waitMs = Integer.parseInt(System.getProperty("faturalab.report.open.delay.ms", "5000"));
            System.out.println("Extended rapor icin " + (waitMs / 1000) + " sn bekleniyor, sonra tarayici acilacak...");
            Thread.sleep(waitMs);
            ReportOpener.main(new String[]{});
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Report opener interrupted: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error opening reports: {}", e.getMessage(), e);
        }
        
        System.out.println("=".repeat(60));
    }
    
    @Override
    public void onStart(ISuite suite) {
        System.setProperty("faturalab.report.listener.active", "true");

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