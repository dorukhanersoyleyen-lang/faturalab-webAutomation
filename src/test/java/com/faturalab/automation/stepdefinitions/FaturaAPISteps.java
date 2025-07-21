package com.faturalab.automation.stepdefinitions;

import com.faturalab.automation.api.FaturalabAPI;
import com.faturalab.automation.models.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FaturaAPISteps {
    
    private static final Logger log = LogManager.getLogger(FaturaAPISteps.class);
    
    private FaturalabAPI faturalabAPI;
    private UploadInvoiceRequest lastInvoiceRequest;
    private String lastInvoiceNo;
    private String lastSupplierTaxNo;
    private Response lastResponse;
    
    @Given("^\"([^\"]*)\" ortamı kullanılıyor$")
    public void ortam_kullaniliyor(String environmentName) {
        log.info("Initializing environment: {}", environmentName);
        faturalabAPI = new FaturalabAPI(environmentName);
        Assert.assertNotNull(faturalabAPI, "FaturalabAPI should be initialized");
    }
    
    @And("^kullanıcı kimlik doğrulaması yapıldı$")
    public void kullanici_kimlik_dogrulamasi_yapildi() {
        log.info("Performing authentication with environment: {}", faturalabAPI.getEnvironment().getAlias());
        
        Response response = faturalabAPI.authenticate();
        
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
        
        log.info("✅ Authentication successful! SessionID: {} stored in environment for: {}", 
                sessionId, faturalabAPI.getEnvironment().getAlias());
    }
    
    @When("^geçerli fatura bilgileri ile fatura yüklerse$")
    public void gecerli_fatura_bilgileri_ile_fatura_yuklerse(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> invoiceData = data.get(0);
        
        // Enhanced Debug: Print everything about DataTable
        log.info("=== DataTable Debug ===");
        log.info("Raw DataTable: {}", dataTable);
        log.info("DataTable as maps size: {}", data.size());
        log.info("DataTable keys: {}", invoiceData.keySet());
        log.info("DataTable values: {}", invoiceData);
        
        // Print each key-value pair individually
        for (Map.Entry<String, String> entry : invoiceData.entrySet()) {
            log.info("Key: '{}' -> Value: '{}'", entry.getKey(), entry.getValue());
        }
        log.info("=== End DataTable Debug ===");
        
        // Generate unique invoice number using environment prefix + timestamp + random
        String baseInvoiceNo = invoiceData.get("invoiceNo");
        String uniqueInvoiceNo = generateUniqueInvoiceNo(baseInvoiceNo);
        String supplierTaxNo = invoiceData.get("supplierTaxNo");
        
        // Safe parsing with null check
        String amountStr = invoiceData.get("invoiceAmount");
        log.info("invoiceAmount from DataTable: '{}'", amountStr);
        
        // Check if key exists at all
        boolean hasInvoiceAmount = invoiceData.containsKey("invoiceAmount");
        log.info("DataTable contains 'invoiceAmount' key: {}", hasInvoiceAmount);
        
        if (amountStr == null || amountStr.trim().isEmpty()) {
            throw new IllegalArgumentException("invoiceAmount is null or empty in DataTable");
        }
        double invoiceAmount = Double.parseDouble(amountStr.trim());
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
                .taxExclusiveAmount(invoiceType.equals("E_ARSIV") ? invoiceAmount * 0.85 : 0)
                .build();
        
        lastResponse = faturalabAPI.uploadInvoice(lastInvoiceRequest);
    }
    
    @Then("^fatura başarıyla yüklenmiş olmalı$")
    public void fatura_basariyla_yuklenmiş_olmali() {
        Assert.assertNotNull(lastResponse, "Upload response should not be null");
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Upload should return 200 status");
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "Upload should be successful");
        log.info("Invoice uploaded successfully: {}", lastInvoiceNo);
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
        log.info("Checking if invoice {} appears in history", lastInvoiceNo);
        
        // Create invoice history request
        InvoiceHistoryRequest historyRequest = new InvoiceHistoryRequest();
        historyRequest.setFromDate(getCurrentDate());
        historyRequest.setToDate(getFutureDate(1));
        
        Response historyResponse = faturalabAPI.getInvoiceHistory(historyRequest);
        Assert.assertEquals(historyResponse.getStatusCode(), 200, "Invoice history request should succeed");
        
        String responseBody = historyResponse.getBody().asString();
        Assert.assertTrue(responseBody.contains(lastInvoiceNo), 
                "Invoice " + lastInvoiceNo + " should appear in history");
        log.info("✅ Invoice {} found in history", lastInvoiceNo);
    }
    
    @When("^faturası silinirse$")
    public void faturasi_silinirse() {
        log.info("Deleting invoice: {}", lastInvoiceNo);
        
        DeleteInvoiceRequest deleteRequest = new DeleteInvoiceRequest();
        deleteRequest.setInvoiceNo(lastInvoiceNo);
        deleteRequest.setSupplierTaxNo(lastSupplierTaxNo);
        deleteRequest.setUserEmail(faturalabAPI.getEnvironment().getUserEmail());
        
        lastResponse = faturalabAPI.deleteInvoice(deleteRequest);
        Assert.assertEquals(lastResponse.getStatusCode(), 200, "Delete request should succeed");
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "Invoice deletion should be successful");
        log.info("✅ Invoice {} deleted successfully", lastInvoiceNo);
    }
    
    @Then("^fatura başarıyla silinmiş olmalı$")
    public void fatura_basarıyla_silinmis_olmali() {
        Assert.assertTrue(faturalabAPI.isResponseSuccessful(), "Invoice deletion should be successful");
        log.info("✅ Invoice deletion confirmed");
    }
    
    @And("^fatura geçmişinde faturası görünmemeli$")
    public void fatura_gecmisinde_faturasi_gorunmemeli() {
        log.info("Checking if invoice {} is removed from history", lastInvoiceNo);
        
        // Create invoice history request
        InvoiceHistoryRequest historyRequest = new InvoiceHistoryRequest();
        historyRequest.setFromDate(getCurrentDate());
        historyRequest.setToDate(getFutureDate(1));
        
        Response historyResponse = faturalabAPI.getInvoiceHistory(historyRequest);
        Assert.assertEquals(historyResponse.getStatusCode(), 200, "Invoice history request should succeed");
        
        String responseBody = historyResponse.getBody().asString();
        Assert.assertFalse(responseBody.contains(lastInvoiceNo), 
                "Invoice " + lastInvoiceNo + " should not appear in history after deletion");
        log.info("✅ Invoice {} successfully removed from history", lastInvoiceNo);
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
        double invoiceAmount = Double.parseDouble(amountStr.trim());
        String invoiceType = invoiceData.get("invoiceType");
        
        lastInvoiceNo = uniqueInvoiceNo;
        lastSupplierTaxNo = supplierTaxNo;
        
        log.info("Uploading invoice with invalid amount: {} = {}", uniqueInvoiceNo, invoiceAmount);
        
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
                .taxExclusiveAmount(invoiceType.equals("E_ARSIV") ? invoiceAmount * 0.85 : 0)
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
        double invoiceAmount = Double.parseDouble(amountStr.trim());
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
                .taxExclusiveAmount(invoiceAmount * 0.85) // E-Arşiv için KDV hariç tutar gerekli
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
        
        // Skip weekends
        while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
               cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
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