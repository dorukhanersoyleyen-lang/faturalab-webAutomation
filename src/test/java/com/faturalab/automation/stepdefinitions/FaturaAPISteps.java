package com.faturalab.automation.stepdefinitions;

import com.faturalab.automation.api.FaturalabAPI;
import com.faturalab.automation.models.*;
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

import java.text.SimpleDateFormat;
import java.util.*;

public class FaturaAPISteps {
    
    private static final Logger log = LogManager.getLogger(FaturaAPISteps.class);
    private FaturalabAPI faturalabAPI;
    private Response lastResponse;
    private UploadInvoiceRequest lastInvoiceRequest;
    private String lastInvoiceNo;
    private String lastSupplierTaxNo;
    
    // Cucumber Scenario for reporting
    private Scenario scenario;
    
    public FaturaAPISteps() {
        // Constructor
    }
    
    // Method to set scenario context (called by hooks)
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
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
    
    @Given("^\"([^\"]*)\" ortamı kullanılıyor$")
    public void ortam_kullaniliyor(String environmentName) {
        log.info("Initializing environment: {}", environmentName);
        faturalabAPI = new FaturalabAPI(environmentName);
        Assert.assertNotNull(faturalabAPI, "FaturalabAPI should be initialized");
    }
    
    @And("^kullanıcı kimlik doğrulaması yapıldı$")
    public void kullanici_kimlik_dogrulamasi_yapildi() {
        log.info("Performing authentication with environment: {}", faturalabAPI.getEnvironment().getAlias());
        
        // Prepare request details for report
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Endpoint: POST ").append(faturalabAPI.getEnvironment().getHost()).append("/authenticate\n");
        requestDetails.append("Environment: ").append(faturalabAPI.getEnvironment().getAlias()).append("\n");
        requestDetails.append("API Key: ").append(faturalabAPI.getEnvironment().getApiKey()).append("\n");
        requestDetails.append("Tax Number: ").append(faturalabAPI.getEnvironment().getTaxNumber()).append("\n");
        requestDetails.append("User Email: ").append(faturalabAPI.getEnvironment().getUserEmail()).append("\n");
        
        Response response = faturalabAPI.authenticate();
        
        // Prepare response details for report
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(response.getStatusCode()).append("\n");
        responseDetails.append("Response Headers: ").append(response.getHeaders()).append("\n");
        responseDetails.append("Response Body: ").append(response.getBody().asString()).append("\n");
        
        // Attach to Cucumber report
        logAPIDetailsToReport("AUTHENTICATION", requestDetails.toString(), responseDetails.toString());
        
        // Basic response checks
        Assert.assertNotNull(response, "Authentication response should not be null");
        
        // Log detailed response information for debugging
        String responseBody = response.getBody().asString();
        log.info("=== AUTHENTICATION DEBUG ===");
        log.info("Response Status: {}", response.getStatusCode());
        log.info("Response Headers: {}", response.getHeaders());
        log.info("Response Body: {}", responseBody);
        log.info("=============================");
        
        Assert.assertEquals(response.getStatusCode(), 200, 
                "Authentication should return 200 status. Got: " + response.getStatusCode() + 
                " Body: " + responseBody);
        
        // Check if API response shows success
        boolean isSuccessful = faturalabAPI.isResponseSuccessful();
        log.info("API Response Success Status: {}", isSuccessful);
        
        if (!isSuccessful) {
            log.error("❌ Authentication FAILED! Response details:");
            log.error("Status Code: {}", response.getStatusCode());
            log.error("Response Body: {}", responseBody);
            Assert.fail("Authentication response should have success=true. Response: " + responseBody);
        }
        
        // Check if sessionId was retrieved and stored
        String sessionId = faturalabAPI.getSessionId();
        Assert.assertNotNull(sessionId, "Session ID should be retrieved from response");
        Assert.assertFalse(sessionId.trim().isEmpty(), "Session ID should not be empty");
        
        // Verify sessionId is stored in environment
        String envSessionId = faturalabAPI.getEnvironment().getSessionId();
        Assert.assertEquals(sessionId, envSessionId, "Session ID should be stored in environment");
        
        log.info("=== AUTHENTICATION SUCCESS VERIFICATION ===");
        log.info("✅ Authentication successful!");
        log.info("Environment: {}", faturalabAPI.getEnvironment().getAlias());
        log.info("SessionID: {}", sessionId);
        log.info("Response Status: {}", response.getStatusCode());
        log.info("Response Body: {}", response.getBody().asString());
        log.info("===========================================");
    }
    
    @When("^geçerli fatura bilgileri ile fatura yüklerse$")
    public void gecerli_fatura_bilgileri_ile_fatura_yuklerse(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> invoiceData = data.get(0);
        
        // Debug: Print all available keys
        log.info("DataTable keys: {}", invoiceData.keySet());
        log.info("DataTable values: {}", invoiceData);
        
        // Generate unique invoice number using environment prefix + timestamp + random
        String baseInvoiceNo = invoiceData.get("invoiceNo");
        String uniqueInvoiceNo = generateUniqueInvoiceNo(baseInvoiceNo);
        String supplierTaxNo = invoiceData.get("supplierTaxNo");
        
        // Safe parsing with null check
        String amountStr = invoiceData.get("invoiceAmount");
        log.info("invoiceAmount from DataTable: '{}'", amountStr);
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("invoiceAmount is null or empty in DataTable");
        }
        int invoiceAmount = Integer.parseInt(amountStr.trim());
        String invoiceType = invoiceData.get("invoiceType");
        
        // Store for later use
        lastInvoiceNo = uniqueInvoiceNo;
        lastSupplierTaxNo = supplierTaxNo;
        
        log.info("Uploading invoice: {} with amount: {}", uniqueInvoiceNo, invoiceAmount);
        
        // Create request with valid dates
        String today = getCurrentDate();
        String futureDate = getFutureDate(60); // 60 days from now
        
        lastInvoiceRequest = UploadInvoiceRequest.builder()
                .userEmail(faturalabAPI.getEnvironment().getUserEmail())
                .supplierTaxNo(supplierTaxNo)
                .invoiceAmount(invoiceAmount)
                .remainingAmount(invoiceAmount)
                .currencyType("TL")
                .invoiceDate(today)
                .dueDate(futureDate)
                .additionalDueDate(futureDate)
                .invoiceNo(uniqueInvoiceNo)
                .invoiceType(invoiceType)
                .hashCode(invoiceType.equals("E_FATURA") ? generateHashCode() : "")
                .taxExclusiveAmount(invoiceType.equals("E_ARSIV") ? (int)(invoiceAmount * 0.85) : 0)
                .build();
        
        lastResponse = faturalabAPI.uploadInvoice(lastInvoiceRequest);
        
        // Prepare request details for report
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Endpoint: POST ").append(faturalabAPI.getEnvironment().getHost()).append("/invoice/upload\n");
        requestDetails.append("Invoice Number: ").append(uniqueInvoiceNo).append("\n");
        requestDetails.append("Supplier Tax No: ").append(supplierTaxNo).append("\n");
        requestDetails.append("Invoice Amount: ").append(invoiceAmount).append("\n");
        requestDetails.append("Invoice Type: ").append(invoiceType).append("\n");
        requestDetails.append("Currency: ").append(lastInvoiceRequest.getCurrencyType()).append("\n");
        requestDetails.append("Invoice Date: ").append(lastInvoiceRequest.getInvoiceDate()).append("\n");
        requestDetails.append("Due Date: ").append(lastInvoiceRequest.getDueDate()).append("\n");
        requestDetails.append("User Email: ").append(lastInvoiceRequest.getUserEmail()).append("\n");
        
        // Prepare response details for report
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(lastResponse.getStatusCode()).append("\n");
        responseDetails.append("Response Headers: ").append(lastResponse.getHeaders()).append("\n");
        responseDetails.append("Response Body: ").append(lastResponse.getBody().asString()).append("\n");
        
        // Attach to Cucumber report
        logAPIDetailsToReport("UPLOAD INVOICE", requestDetails.toString(), responseDetails.toString());
    }
    
    @Then("^fatura başarıyla yüklenmiş olmalı$")
    public void fatura_basariyla_yuklenmiş_olmali() {
        Assert.assertNotNull(lastResponse, "Upload response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Upload should return 200 status");
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "Upload should be successful");
        
        log.info("=== UPLOAD SUCCESS VERIFICATION ===");
        log.info("✅ Invoice upload successful!");
        log.info("Invoice Number: {}", lastInvoiceNo);
        log.info("Response Status: {}", lastResponse.getStatusCode());
        log.info("Response Body: {}", lastResponse.getBody().asString());
        log.info("===================================");
    }
    
    @When("^boş parametrelerle fatura yüklenmeye çalışılırsa$")
    public void bos_parametrelerle_fatura_yuklenmeye_calisilirsa() {
        log.info("Attempting to upload invoice with empty parameters");
        
        UploadInvoiceRequest emptyRequest = new UploadInvoiceRequest();
        emptyRequest.setUserEmail(faturalabAPI.getEnvironment().getUserEmail());
        
        lastResponse = faturalabAPI.uploadInvoice(emptyRequest);
    }
    
    @Then("^hata mesajı alınmalı$")
    public void hata_mesaji_alinmali() {
        Assert.assertNotNull(lastResponse, "Response should not be null");
        Assert.assertTrue(lastResponse.getStatusCode() >= 400 || !faturalabAPI.isResponseSuccessful(), 
                "Should receive error response");
        log.info("Error response received as expected");
    }
    
    @And("^fatura yüklenmemiş olmalı$")
    public void fatura_yuklenmemis_olmali() {
        Assert.assertFalse(faturalabAPI.isResponseSuccessful(), "Invoice upload should not be successful");
        log.info("Invoice upload failed as expected");
    }
    
    @And("^fatura geçmişinde faturası görünmeli$")
    public void fatura_gecmisinde_faturasi_gorunmeli() {
        Assert.assertNotNull(lastInvoiceNo, "Invoice number should be available");
        log.info("Checking if invoice {} appears in history", lastInvoiceNo);
        
        // WAIT FOR SYSTEM INDEXING (ESKİ ÇÖZÜMÜMÜZ!)
        try {
            log.info("⏳ Waiting 5 seconds for system to index the uploaded invoice...");
            Thread.sleep(5000); // 5 saniye bekle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create invoice history request with TODAY'S START (ESKİ ÇÖZÜMÜMÜZ!)
        InvoiceHistoryRequest historyRequest = new InvoiceHistoryRequest();
        
        // Use TODAY'S BEGINNING instead of current time (DAHA GENİŞ ARAMA!)
        String todayStart = getTodayStartDateTime(); // "2025-07-23T00:00:00.000+0300"
        
        historyRequest.setFromDate(todayStart);
        historyRequest.setOnlyLastState(true);
        
        log.info("Invoice history request - FromDate: {} (Today's start), OnlyLastState: true", todayStart);
        
        // RETRY MECHANISM (ESKİ ÇÖZÜMÜMÜZ!)
        Response historyResponse = null;
        boolean invoiceFound = false;
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("📊 Attempt {}/{} - Searching for invoice in history...", attempt, maxRetries);
            
            historyResponse = faturalabAPI.getInvoiceHistory(historyRequest);
            Assert.assertEquals(historyResponse.getStatusCode(), 200, "Invoice history request should succeed");
            
            // Check if invoice exists in response
            String responseBody = historyResponse.getBody().asString();
            if (responseBody.contains(lastInvoiceNo)) {
                log.info("✅ Invoice {} FOUND in history on attempt {}", lastInvoiceNo, attempt);
                invoiceFound = true;
                break;
            } else {
                log.warn("⚠️ Invoice {} NOT FOUND on attempt {}. Response: {}", lastInvoiceNo, attempt, responseBody);
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(3000); // Wait 3 seconds before retry
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        
        // Prepare request details for report
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Endpoint: POST ").append(faturalabAPI.getEnvironment().getHost()).append("/invoice/history\n");
        requestDetails.append("From Date: ").append(todayStart).append(" (Today's start - WIDER SEARCH!)\n");
        requestDetails.append("Only Last State: true\n");
        requestDetails.append("Searching for Invoice: ").append(lastInvoiceNo).append("\n");
        requestDetails.append("Max Retries: ").append(maxRetries).append("\n");
        
        // Prepare response details for report
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(historyResponse.getStatusCode()).append("\n");
        responseDetails.append("Response Headers: ").append(historyResponse.getHeaders()).append("\n");
        responseDetails.append("Response Body: ").append(historyResponse.getBody().asString()).append("\n");
        responseDetails.append("Invoice Found: ").append(invoiceFound ? "YES ✅" : "NO ❌").append("\n");
        
        // Attach to Cucumber report
        logAPIDetailsToReport("GET INVOICE HISTORY", requestDetails.toString(), responseDetails.toString());
        
        // Final assertion
        Assert.assertTrue(invoiceFound, 
                "Invoice " + lastInvoiceNo + " should appear in history after " + maxRetries + " attempts. " +
                "Last response: " + historyResponse.getBody().asString());
        
        log.info("✅ Invoice {} found in history successfully", lastInvoiceNo);
    }
    
    @When("^faturası silinirse$")
    public void faturasi_silinirse() {
        log.info("Deleting invoice: {}", lastInvoiceNo);
        
        DeleteInvoiceRequest deleteRequest = new DeleteInvoiceRequest();
        deleteRequest.setInvoiceNo(lastInvoiceNo);
        deleteRequest.setSupplierTaxNo(lastSupplierTaxNo);
        deleteRequest.setUserEmail(faturalabAPI.getEnvironment().getUserEmail());
        
        lastResponse = faturalabAPI.deleteInvoice(deleteRequest);
        
        // Prepare request details for report
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Endpoint: POST ").append(faturalabAPI.getEnvironment().getHost()).append("/invoice/delete\n");
        requestDetails.append("Invoice Number: ").append(lastInvoiceNo).append("\n");
        requestDetails.append("Supplier Tax No: ").append(lastSupplierTaxNo).append("\n");
        requestDetails.append("User Email: ").append(faturalabAPI.getEnvironment().getUserEmail()).append("\n");
        
        // Prepare response details for report
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(lastResponse.getStatusCode()).append("\n");
        responseDetails.append("Response Headers: ").append(lastResponse.getHeaders()).append("\n");
        responseDetails.append("Response Body: ").append(lastResponse.getBody().asString()).append("\n");
        
        // Attach to Cucumber report
        logAPIDetailsToReport("DELETE INVOICE", requestDetails.toString(), responseDetails.toString());
        
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Delete request should succeed");
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "Invoice deletion should be successful");
        log.info("✅ Invoice {} deleted successfully", lastInvoiceNo);
    }
    
    @Then("^fatura başarıyla silinmiş olmalı$")
    public void fatura_basariyla_silinmis_olmali() {
        Assert.assertNotNull(lastResponse, "Delete response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Delete should return 200 status");
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "Delete should be successful");
        
        log.info("=== DELETE SUCCESS VERIFICATION ===");
        log.info("✅ Invoice delete successful!");
        log.info("Invoice Number: {}", lastInvoiceNo);
        log.info("Response Status: {}", lastResponse.getStatusCode());
        log.info("Response Body: {}", lastResponse.getBody().asString());
        log.info("===================================");
    }
    
    @And("^fatura geçmişinde faturası görünmemeli$")
    public void fatura_gecmisinde_faturasi_gorunmemeli() {
        Assert.assertNotNull(lastInvoiceNo, "Invoice number should be available");
        log.info("Checking if invoice {} is removed from history", lastInvoiceNo);
        
        // WAIT FOR SYSTEM INDEXING (ESKİ ÇÖZÜMÜMÜZ!)
        try {
            log.info("⏳ Waiting 5 seconds for system to index the deleted invoice...");
            Thread.sleep(5000); // 5 saniye bekle
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create invoice history request with TODAY'S START (ESKİ ÇÖZÜMÜMÜZ!)
        InvoiceHistoryRequest historyRequest = new InvoiceHistoryRequest();
        
        // Use TODAY'S BEGINNING instead of current time (DAHA GENİŞ ARAMA!)
        String todayStart = getTodayStartDateTime(); // "2025-07-23T00:00:00.000+0300"
        
        historyRequest.setFromDate(todayStart);
        historyRequest.setOnlyLastState(true);
        
        log.info("Invoice history request (after delete) - FromDate: {} (Today's start), OnlyLastState: true", todayStart);
        
        // RETRY MECHANISM (ESKİ ÇÖZÜMÜMÜZ!)
        Response historyResponse = null;
        boolean invoiceDeleted = false;
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("📊 Attempt {}/{} - Checking if invoice is deleted in history...", attempt, maxRetries);
            
            historyResponse = faturalabAPI.getInvoiceHistory(historyRequest);
            Assert.assertEquals(historyResponse.getStatusCode(), 200, "Invoice history request should succeed");
            
            String responseBody = historyResponse.getBody().asString();
            log.info("Invoice history response (after delete): {}", responseBody);
            
            // Check if invoice still appears in history but with "Silinmiş" status
            boolean invoiceFound = responseBody.contains(lastInvoiceNo);
            log.info("Invoice {} found in history after delete: {}", lastInvoiceNo, invoiceFound);
            
            if (invoiceFound) {
                // Invoice should be in "Silinmiş" status
                boolean isDeleted = responseBody.contains("\"invoiceHistoryDescription\":\"Silinmiş\"") ||
                        responseBody.contains("\"status\":\"Silinmiş\"") ||
                        responseBody.contains("Silinmiş");
                
                log.info("Invoice {} deletion status in response: {}", lastInvoiceNo, isDeleted ? "DELETED ✅" : "NOT DELETED ❌");
                
                if (isDeleted) {
                    log.info("✅ Invoice {} is marked as DELETED in history on attempt {}", lastInvoiceNo, attempt);
                    invoiceDeleted = true;
                    break;
                } else {
                    log.warn("⚠️ Invoice {} found but not marked as deleted on attempt {}.", lastInvoiceNo, attempt);
                }
            } else {
                // Invoice completely removed from history (also acceptable)
                log.info("✅ Invoice {} completely removed from history on attempt {}", lastInvoiceNo, attempt);
                invoiceDeleted = true;
                break;
            }
            
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(3000); // Wait 3 seconds before retry
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // Prepare request details for report
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Endpoint: POST ").append(faturalabAPI.getEnvironment().getHost()).append("/invoice/history\n");
        requestDetails.append("From Date: ").append(todayStart).append(" (Today's start - WIDER SEARCH!)\n");
        requestDetails.append("Only Last State: true\n");
        requestDetails.append("Checking for deleted Invoice: ").append(lastInvoiceNo).append("\n");
        requestDetails.append("Max Retries: ").append(maxRetries).append("\n");
        
        // Prepare response details for report
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("Status Code: ").append(historyResponse.getStatusCode()).append("\n");
        responseDetails.append("Response Headers: ").append(historyResponse.getHeaders()).append("\n");
        responseDetails.append("Response Body: ").append(historyResponse.getBody().asString()).append("\n");
        responseDetails.append("Invoice Deleted: ").append(invoiceDeleted ? "YES ✅" : "NO ❌").append("\n");
        
        // Attach to Cucumber report
        logAPIDetailsToReport("GET INVOICE HISTORY (AFTER DELETE)", requestDetails.toString(), responseDetails.toString());
        
        // Final assertion
        Assert.assertTrue(invoiceDeleted, 
                "Invoice " + lastInvoiceNo + " should be deleted or marked as 'Silinmiş' in history after " + maxRetries + " attempts. " +
                "Last response: " + historyResponse.getBody().asString());
        
        log.info("✅ Invoice {} delete verification successful", lastInvoiceNo);
    }
    
    @When("^geçersiz miktarla fatura yüklerse$")
    public void gecersiz_miktarla_fatura_yuklerse(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> invoiceData = data.get(0);
        
        // Debug: Print all available keys
        log.info("DataTable keys: {}", invoiceData.keySet());
        log.info("DataTable values: {}", invoiceData);
        
        String baseInvoiceNo = invoiceData.get("invoiceNo");
        String uniqueInvoiceNo = generateUniqueInvoiceNo(baseInvoiceNo);
        String supplierTaxNo = invoiceData.get("supplierTaxNo");
        
        // Safe parsing with null check
        String amountStr = invoiceData.get("invoiceAmount");
        log.info("invoiceAmount from DataTable: '{}'", amountStr);
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("invoiceAmount is null or empty in DataTable");
        }
        int invoiceAmount = Integer.parseInt(amountStr.trim());
        String invoiceType = invoiceData.get("invoiceType");
        
        lastInvoiceNo = uniqueInvoiceNo;
        lastSupplierTaxNo = supplierTaxNo;
        
        log.info("Uploading invalid invoice: {} with amount: {}", uniqueInvoiceNo, invoiceAmount);
        
        String today = getCurrentDate();
        String futureDate = getFutureDate(30);
        
        lastInvoiceRequest = UploadInvoiceRequest.builder()
                .userEmail(faturalabAPI.getEnvironment().getUserEmail())
                .supplierTaxNo(supplierTaxNo)
                .invoiceAmount(invoiceAmount)
                .remainingAmount(invoiceAmount)
                .currencyType("TL")
                .invoiceDate(today)
                .dueDate(futureDate)
                .additionalDueDate(futureDate)
                .invoiceNo(uniqueInvoiceNo)
                .invoiceType(invoiceType)
                .hashCode(invoiceType.equals("E_FATURA") ? generateHashCode() : "")
                .taxExclusiveAmount(invoiceType.equals("E_ARSIV") ? (int)(invoiceAmount * 0.85) : 0)
                .build();
        
        lastResponse = faturalabAPI.uploadInvoice(lastInvoiceRequest);
    }
    
    @When("^E-Arşiv fatura bilgileri ile fatura yüklerse$")
    public void e_arsiv_fatura_bilgileri_ile_fatura_yuklerse(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> invoiceData = data.get(0);
        
        // Debug: Print all available keys
        log.info("DataTable keys: {}", invoiceData.keySet());
        log.info("DataTable values: {}", invoiceData);
        
        String baseInvoiceNo = invoiceData.get("invoiceNo");
        String uniqueInvoiceNo = generateUniqueInvoiceNo(baseInvoiceNo);
        String supplierTaxNo = invoiceData.get("supplierTaxNo");
        
        // Safe parsing with null check
        String amountStr = invoiceData.get("invoiceAmount");
        log.info("invoiceAmount from DataTable: '{}'", amountStr);
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("invoiceAmount is null or empty in DataTable");
        }
        int invoiceAmount = Integer.parseInt(amountStr.trim());
        String invoiceType = invoiceData.get("invoiceType");
        
        lastInvoiceNo = uniqueInvoiceNo;
        lastSupplierTaxNo = supplierTaxNo;
        
        log.info("Uploading E-Arşiv invoice: {} with amount: {}", uniqueInvoiceNo, invoiceAmount);
        
        String today = getCurrentDate();
        String futureDate = getFutureDate(30);
        
        lastInvoiceRequest = UploadInvoiceRequest.builder()
                .userEmail(faturalabAPI.getEnvironment().getUserEmail())
                .supplierTaxNo(supplierTaxNo)
                .invoiceAmount(invoiceAmount)
                .remainingAmount(invoiceAmount)
                .currencyType("TL")
                .invoiceDate(today)
                .dueDate(futureDate)
                .additionalDueDate(futureDate)
                .invoiceNo(uniqueInvoiceNo)
                .invoiceType(invoiceType)
                .hashCode("") // E-Arşiv için hashCode gerekmiyor
                .taxExclusiveAmount((int)(invoiceAmount * 0.85)) // E-Arşiv için KDV hariç tutar gerekli
                .build();
        
        lastResponse = faturalabAPI.uploadInvoice(lastInvoiceRequest);
    }
    
    // Utility methods
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
    
    private String getFutureDate(int daysFromNow) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysFromNow);
        
        // Skip weekends - direction depends on whether we're going forward or backward
        if (daysFromNow > 0) {
            // Going forward - skip to next weekday
            while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
                   cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else if (daysFromNow < 0) {
            // Going backward - skip to previous weekday
            while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
                   cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    private String getTodayStartDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000XX");  // XX = +0300, dinamik timezone
        return sdf.format(new Date());
    }
    
    private String generateHashCode() {
        // Generate realistic base64 hashCode similar to real e-fatura
        String randomData = UUID.randomUUID().toString() + System.currentTimeMillis();
        return java.util.Base64.getEncoder().encodeToString(randomData.getBytes()).substring(0, 44) + "=";
    }
    
    private String generateUniqueInvoiceNo(String baseInvoiceNo) {
        // Get environment prefix from environment name
        String envName = faturalabAPI.getEnvironment().getAlias();
        
        // Handle different environment name patterns
        String envPrefix;
        if ("A101".equals(envName)) {
            envPrefix = "A101";
        } else if ("BIEN".equals(envName)) {
            envPrefix = "BIEN";
        } else {
            // Extract uppercase letters for other environments
            String upperCaseOnly = envName.replaceAll("[^A-Z]", "");
            envPrefix = upperCaseOnly.length() > 4 ? upperCaseOnly.substring(0, 4) : upperCaseOnly;
            // Ensure minimum length
            if (envPrefix.length() < 2) {
                envPrefix = envName.replaceAll("[^A-Za-z0-9]", "").substring(0, Math.min(4, envName.length()));
            }
        }
        
        // Generate unique components
        long timestamp = System.currentTimeMillis();
        int randomSuffix = (int) (Math.random() * 999999);
        String datePrefix = new SimpleDateFormat("yyyy").format(new Date());
        
        // Create unique invoice number: ENV + YEAR + 6_DIGIT_RANDOM (similar to DEF2025000900057)
        String uniqueInvoiceNo = String.format("%s%s%06d%03d", 
                envPrefix, datePrefix, (timestamp % 900000) + 100000, randomSuffix % 1000);
        
        log.info("Generated unique invoice number: {} from base: {} (env: {})", uniqueInvoiceNo, baseInvoiceNo, envName);
        return uniqueInvoiceNo;
    }
} 