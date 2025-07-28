package com.faturalab.automation.api;

import com.faturalab.automation.config.EnvironmentManager;
import com.faturalab.automation.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private EnvironmentManager.EnvironmentConfig environment;
    private Response lastResponse;
    private String sessionId;
    
    public FaturalabAPI(String environmentName) {
        this.environment = EnvironmentManager.loadEnvironment(environmentName);
        this.sessionId = environment.getSessionId();
        
        // Set UTF-8 encoding for all RestAssured requests
        RestAssured.config = RestAssured.config().encoderConfig(
            io.restassured.config.EncoderConfig.encoderConfig()
                .defaultContentCharset("UTF-8")
                .defaultQueryParameterCharset("UTF-8")
                .encodeContentTypeAs("application/x-www-form-urlencoded", io.restassured.http.ContentType.URLENC)
        );
        
        log.info("FaturalabAPI initialized with environment: {} (UTF-8 encoding enabled)", environmentName);
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
            
            // Use Jackson ObjectNode for proper character escaping
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
            log.info("Environment Details:");
            log.info("  Alias: '{}'", environment.getAlias());
            log.info("  Tax Number: '{}'", environment.getTaxNumber());
            log.info("  API Key: '{}'", environment.getApiKey());
            log.info("================================");
            
            // Send request with UTF-8 encoding
            log.info("Sending authentication request with UTF-8 encoding...");
            
            lastResponse = getBaseRequest()
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("authenticateParam", requestParam)
                    .post("/authenticate");
                    
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
                            new TypeReference<ApiResponse<AuthenticateResponse>>() {}
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
            log.info("Request Object Details:");
            log.info("  Invoice No: {}", request.getInvoiceNo());
            log.info("  Supplier Tax No: {}", request.getSupplierTaxNo());
            log.info("  Invoice Amount: {}", request.getInvoiceAmount());
            log.info("  Invoice Type: {}", request.getInvoiceType());
            log.info("  Currency: {}", request.getCurrencyType());
            log.info("  Invoice Date: {}", request.getInvoiceDate());
            log.info("  Due Date: {}", request.getDueDate());
            log.info("================================");
            
            lastResponse = getAuthenticatedRequest()
                    .formParam("uploadInvoiceParam", requestParam)
                    .post("/invoice/upload");
                    
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
            log.info("================================");
            
            lastResponse = getAuthenticatedRequest()
                    .formParam("invoiceHistoryParam", requestParam)
                    .post("/invoice/history");
                    
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
            log.info("================================");
            
            lastResponse = getAuthenticatedRequest()
                    .formParam("deleteInvoiceParam", requestParam)
                    .post("/invoice/delete");
                    
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
} 