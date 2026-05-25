package com.faturalab.automation.stepdefinitions.auction;

import com.faturalab.automation.api.FaturalabAPI;
import com.faturalab.automation.hooks.CucumberHooks;
import com.faturalab.automation.models.auction.*;
import com.faturalab.automation.models.common.ValidationResult;
import com.faturalab.automation.utils.InvoiceTestDataGenerator;
import com.faturalab.automation.utils.auction.AuctionAssertionUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.*;

public class AuctionInvoiceUploadStepDefs {
    
    private static final Logger log = LogManager.getLogger(AuctionInvoiceUploadStepDefs.class);
    private FaturalabAPI faturalabAPI;
    private Response lastResponse;
    private String lastReferenceNo;
    private UploadAuctionRequest lastAuctionRequest;
    
    // Cucumber Scenario for reporting
    private Scenario scenario;
    
    public AuctionInvoiceUploadStepDefs() {
        // Constructor
    }
    
    // Method to set scenario context (called by hooks)
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }
    
    // Method to set FaturalabAPI instance (called by hooks)
    public void setFaturalabAPI(FaturalabAPI faturalabAPI) {
        this.faturalabAPI = faturalabAPI;
        log.info("FaturalabAPI instance injected into AuctionInvoiceUploadStepDefs");
    }
    
    // Helper method to ensure API is initialized (lazy loading from hooks)
    private void ensureAPIInitialized() {
        if (this.faturalabAPI == null) {
            log.warn("FaturalabAPI not initialized, attempting to get from hooks...");
            this.faturalabAPI = CucumberHooks.getSharedAPI();
            
            if (this.faturalabAPI == null) {
                throw new RuntimeException("FaturalabAPI not initialized! Authentication step must be executed first.");
            }
            
            log.info("✅ FaturalabAPI successfully retrieved from hooks");
        }
        
        // Additional check for AuctionAPI availability
        log.info("🔍 FaturalabAPI Status Check:");
        log.info("  SessionID: {}", faturalabAPI.getSessionId());
        log.info("  Environment: {}", faturalabAPI.getEnvironment().getAlias());
        log.info("  User Email: {}", faturalabAPI.getEnvironment().getUserEmail());
        
        if (faturalabAPI.getSessionId() == null || faturalabAPI.getSessionId().isEmpty()) {
            throw new RuntimeException("SessionID is null or empty! Authentication may have failed.");
        }
    }
    
    // Helper method to log API details both to Cucumber report and console
    private void logAPIDetailsToReport(String apiCall, String requestDetails, String responseDetails) {
        String fullLog = "=== 🚀 " + apiCall + " ===" + "\n" +
                        "📤 REQUEST DETAILS:\n" + requestDetails + "\n\n" +
                        "📥 RESPONSE DETAILS:\n" + responseDetails + "\n" +
                        "================================\n";
        
        // Log to console/file
        log.info("\n{}", fullLog);
        
        // Log to Cucumber report if scenario is available
        if (scenario != null) {
            scenario.log("=== 🚀 " + apiCall + " ===");
            scenario.log("");
            scenario.log("📤 REQUEST DETAILS:");
            scenario.log(requestDetails);
            scenario.log("");
            scenario.log("📥 RESPONSE DETAILS:");
            scenario.log(responseDetails);
            scenario.log("================================");
            scenario.log("");
        }
        
        // Also try to print to System.out for additional visibility
        System.out.println("\n" + fullLog);
    }
    
    @When("^geçerli auction fatura bilgileri ile fatura yüklerse$")
    public void gecerli_auction_fatura_bilgileri_ile_fatura_yuklerse(DataTable dataTable) {
        List<Map<String, String>> invoiceData = dataTable.asMaps(String.class, String.class);
        log.info("Creating auction upload request with {} invoices", invoiceData.size());
        
        // Generate unique reference number using TestDataGenerator
        lastReferenceNo = InvoiceTestDataGenerator.generateUniqueReferenceNo();
        log.info("Generated reference number: {}", lastReferenceNo);
        
        // Create auction invoices from DataTable
        List<AuctionInvoice> auctionInvoices = new ArrayList<>();
        
        // Build exactly 3 invoices as per successful cURL (unique fields kept external)
        // Invoice 1
        AuctionInvoice inv1 = new AuctionInvoice("0030000049", "1083053674", 75000.0, "PAPER");
        inv1.setRequestedAmount(71000);
        inv1.setTaxExclusiveAmount(72000);
        inv1.setCurrencyType("TL");
        inv1.setDueDate(InvoiceTestDataGenerator.getFutureWorkingDate(33));
        inv1.setExtraInvoiceDueDay(0);
        inv1.setInvoiceDate(InvoiceTestDataGenerator.getCurrentDate());
        inv1.setInvoiceETTN("");
        inv1.setInvoiceTypeCode("SATIS");
        inv1.setOrderNo("1002000049");
        inv1.setItemNo("20004128");
        auctionInvoices.add(inv1);
        
        // Invoice 2
        AuctionInvoice inv2 = new AuctionInvoice("0000700081", "1083053674", 30000.0, "PAPER");
        inv2.setRequestedAmount(27000);
        inv2.setTaxExclusiveAmount(28000);
        inv2.setCurrencyType("TL");
        inv2.setDueDate(InvoiceTestDataGenerator.getFutureWorkingDate(48));
        inv2.setExtraInvoiceDueDay(0);
        inv2.setInvoiceDate(InvoiceTestDataGenerator.getCurrentDate());
        inv2.setInvoiceETTN("");
        inv2.setInvoiceTypeCode("SATIS");
        inv2.setOrderNo("1000400081");
        inv2.setItemNo("20100131");
        auctionInvoices.add(inv2);
        
        // Invoice 3
        AuctionInvoice inv3 = new AuctionInvoice("0007000082", "1083053674", 20000.0, "PAPER");
        inv3.setRequestedAmount(17000);
        inv3.setTaxExclusiveAmount(18000);
        inv3.setCurrencyType("TL");
        inv3.setDueDate(InvoiceTestDataGenerator.getFutureWorkingDate(18));
        inv3.setExtraInvoiceDueDay(0);
        inv3.setInvoiceDate(InvoiceTestDataGenerator.getCurrentDate());
        inv3.setInvoiceETTN("");
        inv3.setInvoiceTypeCode("SATIS");
        inv3.setOrderNo("1004000082");
        inv3.setItemNo("20010132");
        auctionInvoices.add(inv3);
        
        // Create upload request
        ensureAPIInitialized(); // Ensure API is available before using it
        String userEmail = faturalabAPI.getEnvironment().getUserEmail();
        lastAuctionRequest = new UploadAuctionRequest(auctionInvoices, lastReferenceNo, userEmail);
        
        log.info("🔍 UPLOAD AUCTION REQUEST DEBUG:");
        log.info("  totalPayableAmount: {}", lastAuctionRequest.getTotalPayableAmount());
        log.info("  totalRequestedAmount: {}", lastAuctionRequest.getTotalRequestedAmount());
        log.info("  invoices count: {}", lastAuctionRequest.getInvoices().size());
        
        log.info("Uploading auction with {} invoices, total amount: {}", 
                auctionInvoices.size(), lastAuctionRequest.getTotalPayableAmount());
        
        // Call API
        lastResponse = faturalabAPI.uploadAuction(lastAuctionRequest);
        CucumberHooks.setSharedLastResponse(lastResponse);
        
        // Prepare request details for report
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Endpoint: POST ").append(faturalabAPI.getEnvironment().getHost()).append("/auction\n");
        requestDetails.append("Reference No: ").append(lastReferenceNo).append("\n");
        requestDetails.append("User Email: ").append(userEmail).append("\n");
        requestDetails.append("Total Invoices: ").append(auctionInvoices.size()).append("\n");
        requestDetails.append("Total Amount: ").append(lastAuctionRequest.getTotalPayableAmount()).append("\n");
        requestDetails.append("Invoice Details:\n");
        for (AuctionInvoice invoice : auctionInvoices) {
            requestDetails.append("  - Package: ").append(invoice.getPackageNo())
                    .append(", Amount: ").append(invoice.getInvoiceAmount())
                    .append(", Type: ").append(invoice.getInvoiceType()).append("\n");
        }
        
        // Prepare response details for report
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(lastResponse.getStatusCode()).append("\n");
        responseDetails.append("Response Headers: ").append(lastResponse.getHeaders()).append("\n");
        responseDetails.append("Response Body: ").append(lastResponse.getBody().asString()).append("\n");
        
        // Attach to Cucumber report
        logAPIDetailsToReport("UPLOAD AUCTION", requestDetails.toString(), responseDetails.toString());
    }
    
    @Then("^auction fatura başarıyla yüklenmiş olmalı$")
    public void auction_fatura_basariyla_yuklenmiş_olmali() {
        Assert.assertNotNull(lastResponse, "Upload auction response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Upload auction should return 200 status");
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "Auction upload should be successful");
        
        // Validate auction amounts using enhanced validation
        if (lastAuctionRequest != null) {
            ValidationResult validation = faturalabAPI.validateAuctionAmounts(lastAuctionRequest);
            if (!validation.isValid()) {
                String validationErrors = validation.getFullSummary();
                log.error("Auction validation failed:\n{}", validationErrors);
                
                // Log to Cucumber report
                if (scenario != null) {
                    scenario.log("❌ AUCTION VALIDATION FAILED:");
                    scenario.log(validationErrors);
                }
                
                Assert.fail("Auction validation failed: " + validation.getErrorSummary());
            } else {
                log.info("✅ Auction validation passed");
                if (validation.hasWarnings()) {
                    log.warn("Auction validation warnings: {}", validation.getWarningSummary());
                }
            }
        }
        
        log.info("=== AUCTION UPLOAD SUCCESS VERIFICATION ===");
        log.info("✅ Auction upload successful!");
        log.info("Reference Number: {}", lastReferenceNo);
        log.info("Response Status: {}", lastResponse.getStatusCode());
        log.info("Response Body: {}", lastResponse.getBody().asString());
        log.info("=============================================");
    }
    
    @And("^auction fatura status'ü kontrol edilmeli$")
    public void auction_fatura_statusu_kontrol_edilmeli() {
        Assert.assertNotNull(lastReferenceNo, "Reference number should be available");
        log.info("Checking auction status for reference: {}", lastReferenceNo);
        
        // Wait for system processing
        try {
            log.info("⏳ Waiting 3 seconds for auction processing...");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create auction detail request using TestDataGenerator
        String userEmail = faturalabAPI.getEnvironment().getUserEmail();
        AuctionDetailRequest detailRequest = InvoiceTestDataGenerator.generateAuctionDetailRequest(lastReferenceNo, userEmail);
        
        // Call API
        lastResponse = faturalabAPI.getAuctionDetail(detailRequest);
        CucumberHooks.setSharedLastResponse(lastResponse);
        
        // Prepare request details for report
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Endpoint: POST ").append(faturalabAPI.getEnvironment().getHost()).append("/auction/detail\n");
        requestDetails.append("Reference No: ").append(lastReferenceNo).append("\n");
        requestDetails.append("User Email: ").append(userEmail).append("\n");
        
        // Prepare response details for report
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(lastResponse.getStatusCode()).append("\n");
        responseDetails.append("Response Headers: ").append(lastResponse.getHeaders()).append("\n");
        responseDetails.append("Response Body: ").append(lastResponse.getBody().asString()).append("\n");
        
        // Get detailed status information
        AuctionStatusInfo statusInfo = faturalabAPI.getDetailedAuctionStatus();
        if (statusInfo != null) {
            responseDetails.append("\n📊 DETAILED STATUS INFO:\n");
            responseDetails.append("Status: ").append(statusInfo.getStatus()).append("\n");
            responseDetails.append("Success: ").append(statusInfo.isSuccess()).append("\n");
            responseDetails.append("Message: ").append(statusInfo.getMessage()).append("\n");
            if (statusInfo.getErrorMessage() != null) {
                responseDetails.append("Error: ").append(statusInfo.getErrorMessage()).append("\n");
            }
            if (statusInfo.getTotalAmount() != null) {
                responseDetails.append("Total Amount: ").append(statusInfo.getTotalAmount()).append("\n");
            }
            if (statusInfo.getInvoiceCount() != null) {
                responseDetails.append("Invoice Count: ").append(statusInfo.getInvoiceCount()).append("\n");
            }
        }
        
        // Attach to Cucumber report
        logAPIDetailsToReport("GET AUCTION DETAIL", requestDetails.toString(), responseDetails.toString());
        
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Get auction detail should return 200 status");
        
        // Enhanced status assertion
        if (statusInfo != null) {
            Assert.assertTrue(statusInfo.isSuccess(), 
                    "Auction detail API should be successful. Error: " + statusInfo.getErrorMessage());
            Assert.assertNotNull(statusInfo.getStatus(), 
                    "Auction status should not be null. Response: " + statusInfo.getRawResponse());
            
            log.info("✅ Auction detail retrieved successfully. Status: {}", statusInfo.getStatus());
        } else {
            log.warn("⚠️ Could not extract detailed status information");
            log.info("✅ Auction detail retrieved successfully (basic validation)");
        }
    }
    
    @And("^auction fatura status'ü \"([^\"]*)\" olmalı$")
    public void auction_fatura_statusu_olmali(String expectedStatus) {
        Assert.assertNotNull(lastResponse, "Auction detail response should not be null");
        
        // Get detailed status info
        AuctionStatusInfo statusInfo = faturalabAPI.getDetailedAuctionStatus();
        
        // Use utility class for enhanced assertion
        AuctionAssertionUtils.assertAuctionStatus(expectedStatus, statusInfo, lastReferenceNo, scenario);
        
        log.info("✅ Auction status verified as: {}", expectedStatus);
    }
    
    @When("^auction faturası reddedilirse$")
    public void auction_faturasi_reddedilirse() {
        Assert.assertNotNull(lastReferenceNo, "Reference number should be available");
        log.info("Rejecting auction with reference: {}", lastReferenceNo);
        
        // Create reject request using TestDataGenerator
        String userEmail = faturalabAPI.getEnvironment().getUserEmail();
        RejectAuctionRequest rejectRequest = InvoiceTestDataGenerator.generateRejectAuctionRequest(lastReferenceNo, userEmail);
        
        // Call API
        lastResponse = faturalabAPI.rejectAuction(rejectRequest);
        CucumberHooks.setSharedLastResponse(lastResponse);
        
        // Prepare request details for report
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Endpoint: POST ").append(faturalabAPI.getEnvironment().getHost()).append("/auction/reject\n");
        requestDetails.append("Reference No: ").append(lastReferenceNo).append("\n");
        requestDetails.append("User Email: ").append(userEmail).append("\n");
        
        // Prepare response details for report
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(lastResponse.getStatusCode()).append("\n");
        responseDetails.append("Response Headers: ").append(lastResponse.getHeaders()).append("\n");
        responseDetails.append("Response Body: ").append(lastResponse.getBody().asString()).append("\n");
        
        // Attach to Cucumber report
        logAPIDetailsToReport("REJECT AUCTION", requestDetails.toString(), responseDetails.toString());
    }
    
    @Then("^auction fatura reddetme işlemi başarıyla tamamlanmış olmalı$")
    public void auction_fatura_reddetme_islemi_basariyla_tamamlanmis_olmali() {
        Assert.assertNotNull(lastResponse, "Reject auction response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Reject auction should return 200 status");
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "Auction rejection should be successful");
        
        log.info("=== AUCTION REJECTION SUCCESS VERIFICATION ===");
        log.info("✅ Auction rejection successful!");
        log.info("Reference Number: {}", lastReferenceNo);
        log.info("Response Status: {}", lastResponse.getStatusCode());
        log.info("Response Body: {}", lastResponse.getBody().asString());
        log.info("===============================================");
    }
    
    @And("^reddedilen auction fatura status'ü kontrol edilmeli$")
    public void reddedilen_auction_fatura_statusu_kontrol_edilmeli() {
        // Same as regular status check, but after rejection
        auction_fatura_statusu_kontrol_edilmeli();
    }
    
    @And("^auction fatura reddetme nedeni görünmeli$")
    public void auction_fatura_reddetme_nedeni_gorunmeli() {
        Assert.assertNotNull(lastResponse, "Auction detail response should not be null");
        
        // Get detailed status info for rejection reason
        AuctionStatusInfo statusInfo = faturalabAPI.getDetailedAuctionStatus();
        String responseBody = lastResponse.getBody().asString();
        
        // Use utility class for enhanced assertion
        AuctionAssertionUtils.assertRejectionReason(statusInfo, lastReferenceNo, responseBody, scenario);
        
        log.info("✅ Auction rejection reason verified");
    }
    
    // Negative case step definitions
    @When("^boş parametrelerle auction fatura yüklenmeye çalışılırsa$")
    public void bos_parametrelerle_auction_fatura_yuklenmeye_calisirilirsa() {
        // Ensure API is initialized
        ensureAPIInitialized();
        
        log.info("Attempting to upload auction with empty parameters");
        
        // Create empty auction request using TestDataGenerator
        String userEmail = faturalabAPI.getEnvironment().getUserEmail();
        lastAuctionRequest = InvoiceTestDataGenerator.getInvalidAmountPayload(userEmail, "emptyInvoiceList");
        lastReferenceNo = lastAuctionRequest.getReferenceNo();
        
        // Call API - this should fail
        lastResponse = faturalabAPI.uploadAuction(lastAuctionRequest);
        
        // Log for debugging
        log.info("Empty auction upload attempt - Status: {}, Response: {}", 
                lastResponse.getStatusCode(), lastResponse.getBody().asString());
    }
    
    @When("^geçersiz auction tip ile fatura yüklerse$")
    public void gecersiz_auction_tip_ile_fatura_yuklerse(DataTable dataTable) {
        // Ensure API is initialized
        ensureAPIInitialized();
        
        List<Map<String, String>> invoiceData = dataTable.asMaps(String.class, String.class);
        log.info("Creating auction upload request with invalid auction type");
        
        // Generate unique reference number
        lastReferenceNo = InvoiceTestDataGenerator.generateUniqueReferenceNo();
        
        // Create auction invoices with invalid type
        List<AuctionInvoice> auctionInvoices = new ArrayList<>();
        
        for (Map<String, String> row : invoiceData) {
            String invoiceNo = row.get("invoiceNo");
            String supplierTaxNo = row.get("supplierTaxNo");
            Double invoiceAmount = Double.parseDouble(row.get("invoiceAmount"));
            String invoiceType = row.get("invoiceType");
            String auctionType = row.get("auctionType"); // This will be INVALID_TYPE
            
            AuctionInvoice auctionInvoice = new AuctionInvoice(invoiceNo, supplierTaxNo, invoiceAmount, invoiceType);
            // Note: auctionType is part of request, not individual invoice
            auctionInvoices.add(auctionInvoice);
            
            log.info("Added invalid auction invoice: packageNo={}, auctionType={}", invoiceNo, auctionType);
        }
        
        // Create upload request
        String userEmail = faturalabAPI.getEnvironment().getUserEmail();
        lastAuctionRequest = new UploadAuctionRequest(auctionInvoices, lastReferenceNo, userEmail);
        
        // Call API - this should fail
        lastResponse = faturalabAPI.uploadAuction(lastAuctionRequest);
        
        log.info("Invalid auction type upload attempt - Status: {}, Response: {}", 
                lastResponse.getStatusCode(), lastResponse.getBody().asString());
    }
    
    @When("^zaten var olan invoice numarası ile auction fatura yüklerse$")
    public void zaten_var_olan_invoice_numarasi_ile_auction_fatura_yuklerse(DataTable dataTable) {
        // First upload: Ensure invoice exists
        gecerli_auction_fatura_bilgileri_ile_fatura_yuklerse(dataTable);
        
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "First upload should be successful to set up duplicate test");
        
        // Second upload: Try to upload SAME invoice again (same invoice numbers)
        log.info("Attempting duplicate invoice upload (second attempt)");
        
        // Generate NEW reference no to avoid Reference No duplication error (we want Invoice duplication error)
        lastReferenceNo = InvoiceTestDataGenerator.generateUniqueReferenceNo();
        // Create new request with same invoices but new reference
        lastAuctionRequest = new UploadAuctionRequest(lastAuctionRequest.getInvoices(), lastReferenceNo, lastAuctionRequest.getUserEmail());
        
        // Call API again with same invoices but new reference
        lastResponse = faturalabAPI.uploadAuction(lastAuctionRequest);
        CucumberHooks.setSharedLastResponse(lastResponse);
        
        log.info("Duplicate invoice upload attempt - Status: {}, Response: {}", 
                lastResponse.getStatusCode(), lastResponse.getBody().asString());
    }
    
    @Then("^auction fatura yüklenmemiş olmalı$")
    public void auction_fatura_yuklenmemis_olmali() {
        Assert.assertNotNull(lastResponse, "Response should not be null");
        
        // Check for error conditions
        boolean isError = lastResponse.getStatusCode() >= 400 || 
                         !faturalabAPI.isResponseSuccessful() ||
                         lastResponse.getBody().asString().contains("error") ||
                         lastResponse.getBody().asString().contains("fail");
        
        log.info("Verifying auction upload failed - Error detected: {}", isError);
        
        Assert.assertTrue(isError, 
                "Auction upload should have failed. Status: " + lastResponse.getStatusCode() + 
                ", Response: " + lastResponse.getBody().asString());
        
        log.info("✅ Auction upload correctly failed as expected");
    }
    
    @Then("^duplicate invoice hatası alınmalı$")
    public void duplicate_invoice_hatasi_alinmali() {
        Assert.assertNotNull(lastResponse, "Response should not be null");
        
        String responseBody = lastResponse.getBody().asString();
        boolean isDuplicateError = responseBody.contains("duplicate") ||
                                 responseBody.contains("exists") ||
                                 responseBody.contains("already") ||
                                 lastResponse.getStatusCode() == 409; // Conflict status
        
        log.info("Verifying duplicate invoice error - Error detected: {}", isDuplicateError);
        
        Assert.assertTrue(isDuplicateError, 
                "Should receive duplicate invoice error. Response: " + responseBody);
        
        log.info("✅ Duplicate invoice error correctly received");
    }
    
    // === ALTERNATIVE METHODS USING TEST DATA GENERATOR ===
    
    /**
     * Alternative method for creating auction upload using TestDataGenerator
     * Can be used for quick testing with predefined data
     */
    public void uploadAuctionUsingGenerator(int invoiceCount, BigDecimal totalAmount) {
        String userEmail = faturalabAPI.getEnvironment().getUserEmail();
        
        // Use TestDataGenerator for complete payload
        lastAuctionRequest = InvoiceTestDataGenerator.getDummyPayloadForAuctionUpload(userEmail, invoiceCount, totalAmount);
        lastReferenceNo = lastAuctionRequest.getReferenceNo();
        
        log.info("Using TestDataGenerator: {} invoices, total: {}", invoiceCount, totalAmount);
        
        // Call API
        lastResponse = faturalabAPI.uploadAuction(lastAuctionRequest);
        CucumberHooks.setSharedLastResponse(lastResponse);
    }
    
    /**
     * Quick test method using default values
     */
    public void uploadAuctionWithDefaults() {
        String userEmail = faturalabAPI.getEnvironment().getUserEmail();
        
        // Use default values: 3 invoices, 125,000 TL total
        lastAuctionRequest = InvoiceTestDataGenerator.getDummyPayloadForAuctionUpload(userEmail);
        lastReferenceNo = lastAuctionRequest.getReferenceNo();
        
        log.info("Using default TestDataGenerator payload");
        
        // Call API
        lastResponse = faturalabAPI.uploadAuction(lastAuctionRequest);
        CucumberHooks.setSharedLastResponse(lastResponse);
    }
} 