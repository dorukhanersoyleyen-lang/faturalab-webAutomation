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
            
            log.info("=== AUTHENTICATION REQUEST DEBUG ===");
            log.info("Environment Details:");
            log.info("  Alias: '{}'", environment.getAlias());
            log.info("  Password: '{}'", environment.getPassword());
            log.info("  Tax Number: '{}'", environment.getTaxNumber());
            log.info("  API Key: '{}'", environment.getApiKey());
            log.info("JSON Payload: {}", requestParam);
            log.info("Header Value: {}", headerValue);
            log.info("=====================================");
            
            // Send request with UTF-8 encoding
            log.info("Sending authentication request with UTF-8 encoding...");
            
            lastResponse = getBaseRequest()
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("authenticateParam", requestParam)
                    .post("/authenticate");
                    
            log.info("Authentication response - Status: {}, Body: {}", lastResponse.getStatusCode(), lastResponse.getBody().asString());
                    
            log.info("Authentication response status: {}", lastResponse.getStatusCode());
            String responseBody = lastResponse.getBody().asString();
            log.info("Response body: {}", responseBody);
            
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
                        log.info("Session ID retrieved: {}", sessionId);
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
            
            lastResponse = getAuthenticatedRequest()
                    .formParam("uploadInvoiceParam", requestParam)
                    .post("/invoice/upload");
                    
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
            
            lastResponse = getAuthenticatedRequest()
                    .formParam("invoiceHistoryParam", requestParam)
                    .post("/invoice/history");
                    
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
            
            lastResponse = getAuthenticatedRequest()
                    .formParam("deleteInvoiceParam", requestParam)
                    .post("/invoice/delete");
                    
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
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