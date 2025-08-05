package com.faturalab.automation.utils.auction;

import com.faturalab.automation.models.auction.*;
import com.faturalab.automation.models.common.ValidationResult;
import io.cucumber.java.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * Utility class for enhanced auction assertions and validations
 * Provides detailed error logging and Cucumber report integration
 */
public class AuctionAssertionUtils {
    
    private static final Logger log = LogManager.getLogger(AuctionAssertionUtils.class);
    
    /**
     * Asserts auction status with detailed error reporting
     */
    public static void assertAuctionStatus(String expectedStatus, AuctionStatusInfo statusInfo, 
                                         String referenceNo, Scenario scenario) {
        if (statusInfo == null) {
            String error = "Status info is null for reference: " + referenceNo;
            log.error("❌ {}", error);
            if (scenario != null) {
                scenario.log("❌ " + error);
            }
            Assert.fail(error);
            return;
        }
        
        String actualStatus = statusInfo.getStatus();
        
        log.info("=== AUCTION STATUS ASSERTION ===");
        log.info("Reference: {}", referenceNo);
        log.info("Expected: {}", expectedStatus);
        log.info("Actual: {}", actualStatus);
        log.info("HTTP Status: {}", statusInfo.getHttpStatusCode());
        log.info("API Success: {}", statusInfo.isSuccess());
        log.info("===============================");
        
        // Cucumber reporting
        if (scenario != null) {
            scenario.log("🔍 AUCTION STATUS VERIFICATION:");
            scenario.log("Reference: " + referenceNo);
            scenario.log("Expected: " + expectedStatus);
            scenario.log("Actual: " + actualStatus);
            scenario.log("HTTP Status: " + statusInfo.getHttpStatusCode());
            scenario.log("API Success: " + statusInfo.isSuccess());
        }
        
        if (!expectedStatus.equals(actualStatus)) {
            String errorMsg = String.format(
                "Auction status mismatch!\n" +
                "Reference: %s\n" +
                "Expected: %s\n" +
                "Actual: %s\n" +
                "HTTP Status: %d\n" +
                "API Success: %s\n" +
                "Message: %s\n" +
                "Raw Response: %s",
                referenceNo, expectedStatus, actualStatus,
                statusInfo.getHttpStatusCode(), statusInfo.isSuccess(),
                statusInfo.getMessage(), statusInfo.getRawResponse()
            );
            
            log.error("❌ STATUS ASSERTION FAILED:\n{}", errorMsg);
            if (scenario != null) {
                scenario.log("❌ STATUS ASSERTION FAILED:");
                scenario.log(errorMsg);
            }
            
            Assert.fail(errorMsg);
        }
        
        log.info("✅ Status assertion passed: {}", expectedStatus);
    }
    
    /**
     * Asserts rejection reason presence with detailed validation
     */
    public static void assertRejectionReason(AuctionStatusInfo statusInfo, String referenceNo, 
                                           String rawResponse, Scenario scenario) {
        boolean hasRejectionReason = false;
        String rejectionDetails = "";
        
        if (statusInfo != null && statusInfo.hasRejectionInfo()) {
            hasRejectionReason = true;
            rejectionDetails = "Structured reason: " + statusInfo.getRejectionReason();
            if (statusInfo.getRejectDate() != null) {
                rejectionDetails += ", Date: " + statusInfo.getRejectDate();
            }
        } else if (rawResponse != null) {
            // Fallback check
            hasRejectionReason = rawResponse.contains("reject") || 
                               rawResponse.contains("reason") ||
                               rawResponse.contains("REJECTED");
            rejectionDetails = "Found in raw response content";
        }
        
        log.info("=== REJECTION REASON VERIFICATION ===");
        log.info("Reference: {}", referenceNo);
        log.info("Reason Found: {}", hasRejectionReason);
        log.info("Details: {}", rejectionDetails);
        log.info("====================================");
        
        if (scenario != null) {
            scenario.log("🔍 REJECTION REASON VERIFICATION:");
            scenario.log("Reference: " + referenceNo);
            scenario.log("Reason Found: " + hasRejectionReason);
            scenario.log("Details: " + rejectionDetails);
        }
        
        if (!hasRejectionReason) {
            String errorMsg = String.format(
                "Rejection reason not found!\n" +
                "Reference: %s\n" +
                "Raw Response: %s",
                referenceNo, rawResponse
            );
            
            log.error("❌ REJECTION REASON NOT FOUND:\n{}", errorMsg);
            if (scenario != null) {
                scenario.log("❌ REJECTION REASON NOT FOUND:");
                scenario.log(errorMsg);
            }
            
            Assert.fail(errorMsg);
        }
        
        log.info("✅ Rejection reason verification passed");
    }
    
    /**
     * Validates auction amounts with detailed reporting
     */
    public static void validateAuctionAmounts(UploadAuctionRequest request, Scenario scenario) {
        if (request == null) {
            String error = "Upload request is null";
            log.error("❌ {}", error);
            if (scenario != null) {
                scenario.log("❌ " + error);
            }
            Assert.fail(error);
            return;
        }
        
        // Validate basic requirements
        Assert.assertTrue(request.getLocked() != null && request.getLocked(), 
                "Auction must be locked (locked: true)");
        
        Assert.assertTrue(request.getReferenceNo() != null && request.getReferenceNo().startsWith("TEST-AUC-"),
                "Reference number must follow format: TEST-AUC-<UUID>");
        
        Assert.assertTrue(request.getTotalPayableAmount() != null && request.getTotalPayableAmount() > 0,
                "Total payable amount must be > 0. Actual: " + request.getTotalPayableAmount());
        
        Assert.assertTrue(request.getTotalRequestedAmount() != null && request.getTotalRequestedAmount() > 0,
                "Total requested amount must be > 0. Actual: " + request.getTotalRequestedAmount());
        
        // Check consistency
        double calculatedPayable = request.getInvoices().stream()
                .mapToDouble(inv -> inv.getInvoiceAmount() != null ? inv.getInvoiceAmount() : 0.0)
                .sum();
        
        Assert.assertEquals(request.getTotalPayableAmount(), calculatedPayable, 0.01,
                "Total payable amount mismatch. Declared: " + request.getTotalPayableAmount() + 
                ", Calculated: " + calculatedPayable);
        
        log.info("✅ Auction amount validation passed");
        if (scenario != null) {
            scenario.log("✅ AUCTION VALIDATION PASSED:");
            scenario.log("Reference: " + request.getReferenceNo());
            scenario.log("Total Amount: " + request.getTotalPayableAmount());
            scenario.log("Invoice Count: " + request.getInvoices().size());
            scenario.log("Locked: " + request.getLocked());
        }
    }
    
    /**
     * Creates detailed assertion failure message
     */
    public static String createDetailedErrorMessage(String operation, String referenceNo, 
                                                   int httpStatus, String response) {
        return String.format(
            "=== %s FAILED ===\n" +
            "Reference: %s\n" +
            "HTTP Status: %d\n" +
            "Timestamp: %s\n" +
            "Response: %s\n" +
            "========================",
            operation.toUpperCase(), referenceNo, httpStatus, 
            java.time.LocalDateTime.now(), response
        );
    }
    
    /**
     * Logs success message with details
     */
    public static void logSuccess(String operation, String referenceNo, String details, Scenario scenario) {
        String message = String.format("✅ %s SUCCESS - Reference: %s, %s", 
                operation.toUpperCase(), referenceNo, details);
        
        log.info(message);
        if (scenario != null) {
            scenario.log(message);
        }
    }
} 