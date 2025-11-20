package com.faturalab.automation.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faturalab.automation.api.auction.AuctionAPI;
import com.faturalab.automation.config.EnvironmentManager;
import com.faturalab.automation.models.common.*;
import com.faturalab.automation.models.auth.*;
import com.faturalab.automation.models.invoice.*;
import com.faturalab.automation.models.auction.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FaturalabAPI {
    
    private static final Logger log = LogManager.getLogger(FaturalabAPI.class);
    private final ObjectMapper objectMapper;
    private final EnvironmentManager.EnvironmentConfig environment;
    private String sessionId;
    private Response lastResponse;
    
    // Auction API delegate
    private AuctionAPI auctionAPI;
    
    public FaturalabAPI(EnvironmentManager.EnvironmentConfig environment) {
        this.environment = environment;
        this.objectMapper = new ObjectMapper();
        
        // Configure RestAssured
        RestAssured.baseURI = environment.getHost();
        RestAssured.basePath = "/app/api/integration/buyer/v0";
        // Ignore SSL certificate errors for server environment
        RestAssured.useRelaxedHTTPSValidation();
        
        RestAssured.config = RestAssured.config()
                .encoderConfig(RestAssured.config().getEncoderConfig().defaultContentCharset("UTF-8"));
        
        log.info("FaturalabAPI initialized for environment: {}", environment.getHost());
    }
    
    private RequestSpecification getBaseRequest() {
        return RestAssured.given()
                .baseUri(environment.getHost())
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Accept", "application/json");
    }
    
    private RequestSpecification getAuthenticatedRequest() {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("apiKey", environment.getApiKey());
        
        if (sessionId != null && !sessionId.isEmpty()) {
            headerParams.put("sessionId", sessionId);
        }
        
        try {
            String headerValue = objectMapper.writeValueAsString(headerParams);
            return getBaseRequest()
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue);
        } catch (Exception e) {
            log.error("Error creating authenticated request", e);
            throw new RuntimeException("Failed to create authenticated request", e);
        }
    }
    
    public Response authenticate() {
        log.info("Authenticating with environment: {}", environment.getAlias());
        
        try {
            Map<String, String> headerParams = new HashMap<>();
            headerParams.put("apiKey", environment.getApiKey());
            String headerValue = objectMapper.writeValueAsString(headerParams);
            
            // Create JSON payload exactly like cURL - UTF-8 safe
            com.fasterxml.jackson.databind.node.ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("alias", environment.getAlias());
            jsonNode.put("password", environment.getPassword());
            jsonNode.put("taxNumber", environment.getTaxNumber());
            String requestParam = objectMapper.writeValueAsString(jsonNode);
            
            log.info("=== AUTHENTICATION REQUEST ===");
            log.info("Endpoint: POST {}/authenticate", environment.getHost());
            log.info("Headers:");
            log.info("  Content-Type: application/x-www-form-urlencoded; charset=UTF-8");
            log.info("  Accept: application/json");
            log.info("  FLINTEGRATIONHEADERPARAMS: {}", headerValue);
            log.info("Request Body:");
            log.info("  authenticateParam={}", requestParam);
            log.info("🔍 ACTUAL REQUEST PAYLOAD: {}", requestParam);
            log.info("Environment Details:");
            log.info("  Alias: '{}'", environment.getAlias());
            log.info("  Password: '{}'", environment.getPassword());
            log.info("  Tax Number: '{}'", environment.getTaxNumber());
            log.info("  API Key: '{}'", environment.getApiKey());
            log.info("================================");
            
            // Send request with EXACT UTF-8 encoding like cURL
            log.info("Sending authentication request with UTF-8 encoding (cURL compatible)...");
            log.info("🚀 FULL URL: {}/authenticate", environment.getHost());
            
            lastResponse = RestAssured.given()
                    .log().all() // LOG EVERYTHING!
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("authenticateParam", requestParam)
                    .when()
                    .post(environment.getHost() + "/authenticate");
                    
            log.info("=== AUTHENTICATION RESPONSE ===");
            log.info("Status Code: {}", lastResponse.getStatusCode());
            log.info("Response Headers: {}", lastResponse.getHeaders());
            log.info("Response Body: {}", lastResponse.getBody().asString());
            log.info("==================================");
                    
            // Extract sessionId if successful
            if (lastResponse.getStatusCode() == 200) {
                try {
                    ApiResponse<AuthenticateResponse> apiResponse = objectMapper.readValue(
                            lastResponse.getBody().asString(),
                            new com.fasterxml.jackson.core.type.TypeReference<ApiResponse<AuthenticateResponse>>() {}
                    );
                    
                    if (apiResponse.isSuccess() && apiResponse.getResult() != null) {
                        this.sessionId = apiResponse.getResult().getSessionId();
                        environment.setSessionId(this.sessionId);
                        log.info("✅ Session ID extracted: {}", sessionId);
                    }
                } catch (Exception e) {
                    log.error("Error parsing authentication response", e);
                }
            }
            
            return lastResponse;
            
        } catch (Exception e) {
            log.error("Authentication failed", e);
            throw new RuntimeException("Authentication failed", e);
        }
    }
    
    public Response uploadInvoice(UploadInvoiceRequest request) {
        log.info("Uploading invoice: {}", request.getInvoiceNo());
        
        try {
            String requestParam = objectMapper.writeValueAsString(request);
            
            log.info("=== UPLOAD INVOICE REQUEST ===");
            log.info("Endpoint: POST {}/invoice/upload", environment.getHost());
            log.info("Headers:");
            log.info("  Content-Type: application/x-www-form-urlencoded; charset=UTF-8");
            log.info("  Accept: application/json");
            Map<String, String> headerParams = new HashMap<>();
            headerParams.put("apiKey", environment.getApiKey());
            if (sessionId != null && !sessionId.isEmpty()) {
                headerParams.put("sessionId", sessionId);
            }
            log.info("  FLINTEGRATIONHEADERPARAMS: {}", objectMapper.writeValueAsString(headerParams));
            log.info("Request Body:");
            log.info("  uploadInvoiceParam={}", requestParam);
            log.info("🔍 ACTUAL REQUEST PAYLOAD: {}", requestParam);
            log.info("Request Object Details:");
            log.info("  Invoice No: {}", request.getInvoiceNo());
            log.info("  Supplier Tax No: {}", request.getSupplierTaxNo());
            log.info("  Invoice Amount: {}", request.getInvoiceAmount());
            log.info("  Invoice Type: {}", request.getInvoiceType());
            log.info("  Currency: {}", request.getCurrencyType());
            log.info("  Invoice Date: {}", request.getInvoiceDate());
            log.info("  Due Date: {}", request.getDueDate());
            log.info("🚀 FULL URL: {}/invoice/upload", environment.getHost());
            log.info("================================");
            
            // Create headers manually like in authenticate()
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("apiKey", environment.getApiKey());
            if (sessionId != null && !sessionId.isEmpty()) {
                requestHeaders.put("sessionId", sessionId);
            }
            String headerValue = objectMapper.writeValueAsString(requestHeaders);
            
            lastResponse = RestAssured.given()
                    .log().all() // LOG EVERYTHING!
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("uploadInvoiceParam", requestParam)
                    .when()
                    .post(environment.getHost() + "/invoice/upload");
                    
            log.info("=== UPLOAD INVOICE RESPONSE ===");
            log.info("Status Code: {}", lastResponse.getStatusCode());
            log.info("Response Headers: {}", lastResponse.getHeaders());
            log.info("Response Body: {}", lastResponse.getBody().asString());
            log.info("==================================");
                    
            log.info("Upload invoice response status: {}", lastResponse.getStatusCode());
            return lastResponse;
            
        } catch (Exception e) {
            log.error("Failed to upload invoice", e);
            throw new RuntimeException("Failed to upload invoice", e);
        }
    }
    
    public Response getInvoiceHistory(InvoiceHistoryRequest request) {
        log.info("Getting invoice history from: {}", request.getFromDate());
        
        try {
            String requestParam = objectMapper.writeValueAsString(request);
            
            log.info("=== GET INVOICE HISTORY REQUEST ===");
            log.info("Endpoint: POST {}/invoice/history", environment.getHost());
            log.info("Headers:");
            log.info("  Content-Type: application/x-www-form-urlencoded; charset=UTF-8");
            log.info("  Accept: application/json");
            Map<String, String> headerParams = new HashMap<>();
            headerParams.put("apiKey", environment.getApiKey());
            if (sessionId != null && !sessionId.isEmpty()) {
                headerParams.put("sessionId", sessionId);
            }
            log.info("  FLINTEGRATIONHEADERPARAMS: {}", objectMapper.writeValueAsString(headerParams));
            log.info("Request Body:");
            log.info("  invoiceHistoryParam={}", requestParam);
            log.info("Request Object Details:");
            log.info("  From Date: {}", request.getFromDate());
            log.info("  To Date: {}", request.getToDate());
            log.info("  Only Last State: {}", request.isOnlyLastState());
            log.info("🚀 FULL URL: {}/invoice/history", environment.getHost());
            log.info("================================");
            
            // Create headers manually like in authenticate()
            Map<String, String> historyHeaders = new HashMap<>();
            historyHeaders.put("apiKey", environment.getApiKey());
            if (sessionId != null && !sessionId.isEmpty()) {
                historyHeaders.put("sessionId", sessionId);
            }
            String headerValue = objectMapper.writeValueAsString(historyHeaders);
            
            lastResponse = RestAssured.given()
                    .log().all() // LOG EVERYTHING!
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("invoiceHistoryParam", requestParam)
                    .when()
                    .post(environment.getHost() + "/invoice/history");
                    
            log.info("=== GET INVOICE HISTORY RESPONSE ===");
            log.info("Status Code: {}", lastResponse.getStatusCode());
            log.info("Response Headers: {}", lastResponse.getHeaders());
            log.info("Response Body: {}", lastResponse.getBody().asString());
            log.info("==================================");
                    
            log.info("Invoice history response status: {}", lastResponse.getStatusCode());
            return lastResponse;
            
        } catch (Exception e) {
            log.error("Failed to get invoice history", e);
            throw new RuntimeException("Failed to get invoice history", e);
        }
    }
    
    public Response deleteInvoice(DeleteInvoiceRequest request) {
        log.info("Deleting invoice: {}", request.getInvoiceNo());
        
        try {
            String requestParam = objectMapper.writeValueAsString(request);
            
            log.info("=== DELETE INVOICE REQUEST ===");
            log.info("Endpoint: POST {}/invoice/delete", environment.getHost());
            log.info("Headers:");
            log.info("  Content-Type: application/x-www-form-urlencoded; charset=UTF-8");
            log.info("  Accept: application/json");
            Map<String, String> headerParams = new HashMap<>();
            headerParams.put("apiKey", environment.getApiKey());
            if (sessionId != null && !sessionId.isEmpty()) {
                headerParams.put("sessionId", sessionId);
            }
            log.info("  FLINTEGRATIONHEADERPARAMS: {}", objectMapper.writeValueAsString(headerParams));
            log.info("Request Body:");
            log.info("  deleteInvoiceParam={}", requestParam);
            log.info("Request Object Details:");
            log.info("  Invoice No: {}", request.getInvoiceNo());
            log.info("  Supplier Tax No: {}", request.getSupplierTaxNo());
            log.info("🚀 FULL URL: {}/invoice/delete", environment.getHost());
            log.info("================================");
            
            String headerValue = objectMapper.writeValueAsString(headerParams);
            
            lastResponse = RestAssured.given()
                    .log().all() // LOG EVERYTHING!
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("deleteInvoiceParam", requestParam)
                    .when()
                    .post(environment.getHost() + "/invoice/delete");
                    
            log.info("=== DELETE INVOICE RESPONSE ===");
            log.info("Status Code: {}", lastResponse.getStatusCode());
            log.info("Response Headers: {}", lastResponse.getHeaders());
            log.info("Response Body: {}", lastResponse.getBody().asString());
            log.info("==================================");
                    
            log.info("Delete invoice response status: {}", lastResponse.getStatusCode());
            return lastResponse;
            
        } catch (Exception e) {
            log.error("Failed to delete invoice", e);
            throw new RuntimeException("Failed to delete invoice", e);
        }
    }
    
    // Utility methods
    public Response getLastResponse() {
        return lastResponse;
    }
    
    public int getLastStatusCode() {
        return lastResponse != null ? lastResponse.getStatusCode() : -1;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public EnvironmentManager.EnvironmentConfig getEnvironment() {
        return environment;
    }
    
    public String getCurrentDateTimeISO() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");  // XX = +0300, XXX = +03:00
        return sdf.format(new Date());
    }
    
    public boolean isInvoiceInHistory(String invoiceNo) {
        if (lastResponse == null) {
            return false;
        }
        
        try {
            String responseBody = lastResponse.getBody().asString();
            return responseBody.contains(invoiceNo);
        } catch (Exception e) {
            log.error("Error checking invoice in history", e);
            return false;
        }
    }
    
    public boolean isResponseSuccessful() {
        if (lastResponse == null) {
            return false;
        }
        
        try {
            String responseBody = lastResponse.getBody().asString();
            ApiResponse<?> apiResponse = objectMapper.readValue(responseBody, ApiResponse.class);
            return apiResponse.isSuccess();
        } catch (Exception e) {
            log.error("Error checking response success", e);
            return false;
        }
    }
    
    // ============== AUCTION APIs ==============
    
    public Response authenticateUser(AuthenticateRequest request) {
        try {
            String requestParam = objectMapper.writeValueAsString(request);
            
            log.info("=== USER AUTHENTICATION ===");
            log.info("Endpoint: POST {}/authentication", environment.getHost());
            log.info("Headers:");
            log.info("  Content-Type: application/x-www-form-urlencoded; charset=UTF-8");
            log.info("  Accept: application/json");
            Map<String, String> headerParams = new HashMap<>();
            headerParams.put("apiKey", environment.getApiKey());
            log.info("  FLINTEGRATIONHEADERPARAMS: {}", objectMapper.writeValueAsString(headerParams));
            log.info("Request Body:");
            log.info("  authenticateParam={}", requestParam);
            log.info("Request Object Details:");
            log.info("  User Alias: {}", request.getAlias()); // AuthenticateRequest uses getAlias() not getUserEmail()
            log.info("  Password: {}", "****");
            log.info("🚀 FULL URL: {}/authentication", environment.getHost());
            log.info("================================");
            
            String headerValue = objectMapper.writeValueAsString(headerParams);
            
            lastResponse = RestAssured.given()
                    .log().all() // LOG EVERYTHING!
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("authenticateParam", requestParam)
                    .when()
                    .post(environment.getHost() + "/authentication");
                    
            log.info("=== USER AUTHENTICATION RESPONSE ===");
            log.info("Status Code: {}", lastResponse.getStatusCode());
            log.info("Response Headers: {}", lastResponse.getHeaders());
            log.info("Response Body: {}", lastResponse.getBody().asString());
            log.info("====================================");
            
            if (lastResponse.getStatusCode() == 200) {
                // Parse authentication response to get session ID
                try {
                    ApiResponse<AuthenticateResponse> apiResponse = objectMapper.readValue(
                            lastResponse.getBody().asString(),
                            new com.fasterxml.jackson.core.type.TypeReference<ApiResponse<AuthenticateResponse>>() {}
                    );
                    
                    if (apiResponse.isSuccess() && apiResponse.getResult() != null) {
                        this.sessionId = apiResponse.getResult().getSessionId();
                        log.info("Authentication successful. Session ID: {}", sessionId);
                        
                        // Initialize AuctionAPI with session ID
                        this.auctionAPI = new AuctionAPI(environment, sessionId);
                        log.info("AuctionAPI initialized with session ID");
                        
                    } else {
                        log.error("Authentication failed. Response: {}", lastResponse.getBody().asString());
                    }
                } catch (Exception e) {
                    log.error("Failed to parse authentication response", e);
                }
            }
            
            return lastResponse;
            
        } catch (Exception e) {
            log.error("Failed to authenticate user", e);
            throw new RuntimeException("Failed to authenticate user", e);
        }
    }
    
    // === AUCTION API DELEGATE METHODS ===
    
    /**
     * Upload auction invoices - delegates to AuctionAPI
     */
    public Response uploadAuction(UploadAuctionRequest request) {
        log.info("=== AUCTION UPLOAD REQUEST ===");
        log.info("SessionID: {}", sessionId);
        log.info("AuctionAPI instance: {}", auctionAPI);
        log.info("Environment: {}", environment.getAlias());
        
        if (auctionAPI == null) {
            log.error("❌ AuctionAPI is null! This means authentication was not successful or AuctionAPI initialization failed.");
            log.error("Current sessionId: {}", sessionId);
            log.error("Current environment: {}", environment.getAlias());
            
            // Try to initialize AuctionAPI manually if we have sessionId
            if (sessionId != null && !sessionId.isEmpty()) {
                log.warn("🔧 Attempting manual AuctionAPI initialization...");
                try {
                    this.auctionAPI = new com.faturalab.automation.api.auction.AuctionAPI(environment, sessionId);
                    log.info("✅ AuctionAPI manually initialized successfully");
                } catch (Exception e) {
                    log.error("❌ Manual AuctionAPI initialization failed", e);
                    throw new IllegalStateException("AuctionAPI not initialized. Please authenticate first.");
                }
            } else {
                throw new IllegalStateException("AuctionAPI not initialized. Please authenticate first.");
            }
        }
        
        log.info("✅ AuctionAPI is available, delegating upload request...");
        lastResponse = auctionAPI.uploadAuction(request);
        return lastResponse;
    }
    
    /**
     * Get auction detail - delegates to AuctionAPI
     */
    public Response getAuctionDetail(AuctionDetailRequest request) {
        if (auctionAPI == null) {
            throw new IllegalStateException("AuctionAPI not initialized. Please authenticate first.");
        }
        lastResponse = auctionAPI.getAuctionDetail(request);
        return lastResponse;
    }
    
    /**
     * Reject auction - delegates to AuctionAPI
     */
    public Response rejectAuction(RejectAuctionRequest request) {
        if (auctionAPI == null) {
            throw new IllegalStateException("AuctionAPI not initialized. Please authenticate first.");
        }
        lastResponse = auctionAPI.rejectAuction(request);
        return lastResponse;
    }
    
    /**
     * Get auction status from last response - delegates to AuctionAPI
     */
    public String getAuctionStatus() {
        if (auctionAPI == null) {
            log.warn("AuctionAPI not initialized, using fallback status extraction");
            return extractBasicStatus();
        }
        return auctionAPI.getAuctionStatus();
    }
    
    /**
     * Get detailed auction status info - delegates to AuctionAPI
     */
    public AuctionStatusInfo getDetailedAuctionStatus() {
        if (auctionAPI == null) {
            log.warn("AuctionAPI not initialized, cannot provide detailed status");
            return null;
        }
        return auctionAPI.getDetailedAuctionStatus();
    }
    
    /**
     * Validate auction amounts - delegates to AuctionAPI
     */
    public ValidationResult validateAuctionAmounts(UploadAuctionRequest request) {
        if (auctionAPI == null) {
            ValidationResult result = new ValidationResult();
            result.addError("AuctionAPI not initialized. Please authenticate first.");
            return result;
        }
        return auctionAPI.validateAuctionAmounts(request);
    }
    
    /**
     * Fallback status extraction for when AuctionAPI is not available
     */
    private String extractBasicStatus() {
        if (lastResponse == null) {
            return null;
        }
        
        try {
            String responseBody = lastResponse.getBody().asString();
            if (responseBody.contains("\"success\":true")) {
                return "UPLOADED";
            }
            return null;
        } catch (Exception e) {
            log.error("Error in fallback status extraction", e);
            return null;
        }
    }
    
    /**
     * Helper method to extract field value with multiple possible field names
     */
    private String extractFieldAsString(com.fasterxml.jackson.databind.JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                return node.get(fieldName).asText();
            }
        }
        return null;
    }
}