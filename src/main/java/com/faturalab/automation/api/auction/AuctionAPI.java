package com.faturalab.automation.api.auction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.faturalab.automation.config.EnvironmentManager;
import com.faturalab.automation.models.auction.*;
import com.faturalab.automation.models.common.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Dedicated API class for Auction operations
 * Contains all auction-specific API methods separated from general FaturalabAPI
 */
public class AuctionAPI {
    
    private static final Logger log = LogManager.getLogger(AuctionAPI.class);
    private final ObjectMapper objectMapper;
    private final EnvironmentManager.EnvironmentConfig environment;
    private final String sessionId;
    private Response lastResponse;
    
    public AuctionAPI(EnvironmentManager.EnvironmentConfig environment, String sessionId) {
        this.environment = environment;
        this.sessionId = sessionId;
        this.objectMapper = new ObjectMapper();
        
        // Configure RestAssured
        RestAssured.baseURI = environment.getHost();
        RestAssured.basePath = "/app/api/integration/buyer/v0";
        RestAssured.config = RestAssured.config()
                .encoderConfig(RestAssured.config().getEncoderConfig().defaultContentCharset("UTF-8"));
        
        log.info("AuctionAPI initialized for environment: {}", environment.getHost());
    }
    
    /**
     * Creates authenticated request with proper headers
     */
    private RequestSpecification getAuthenticatedRequest() {
        try {
            Map<String, String> headerParams = new HashMap<>();
            headerParams.put("apiKey", environment.getApiKey());
            if (sessionId != null && !sessionId.isEmpty()) {
                headerParams.put("sessionId", sessionId);
            }
            
            String headerValue = objectMapper.writeValueAsString(headerParams);
            
            return RestAssured.given()
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept(ContentType.JSON);
                    
        } catch (Exception e) {
            log.error("Failed to create authenticated request", e);
            throw new RuntimeException("Failed to create authenticated request", e);
        }
    }
    
    /**
     * Upload auction invoices
     */
    public Response uploadAuction(UploadAuctionRequest request) {
        log.info("=== UPLOAD AUCTION ===");
        log.info("Reference: {}", request.getReferenceNo());
        log.info("Invoice Count: {}", request.getInvoices().size());
        log.info("Total Amount: {}", request.getTotalPayableAmount());
        log.info("Locked: {}", request.getLocked());
        
        try {
            String requestParam = objectMapper.writeValueAsString(request);
            
            log.info("=== UPLOAD AUCTION REQUEST ===");
            log.info("Endpoint: POST {}/auction", environment.getHost());
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
            log.info("  uploadAuctionParam={}", requestParam);
            log.info("Request Object Details:");
            log.info("  Reference No: {}", request.getReferenceNo());
            log.info("  User Email: {}", request.getUserEmail());
            log.info("  Total Invoices: {}", request.getInvoices().size());
            log.info("  Total Amount: {}", request.getTotalPayableAmount());
            log.info("🚀 FULL URL: {}/auction", environment.getHost());
            log.info("================================");
            
            // Create headers manually like in FaturalabAPI authenticate()
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
                    .formParam("uploadAuctionParam", requestParam)
                    .when()
                    .post(environment.getHost() + "/auction");
                    
            log.info("=== UPLOAD AUCTION RESPONSE ===");
            log.info("Status Code: {}", lastResponse.getStatusCode());
            log.info("Response Headers: {}", lastResponse.getHeaders());
            log.info("Response Body: {}", lastResponse.getBody().asString());
            log.info("==================================");
                    
            log.info("Upload auction response status: {}", lastResponse.getStatusCode());
            return lastResponse;
            
        } catch (Exception e) {
            log.error("Failed to upload auction", e);
            throw new RuntimeException("Failed to upload auction", e);
        }
    }
    
    /**
     * Get auction detail by reference number
     */
    public Response getAuctionDetail(AuctionDetailRequest request) {
        log.info("=== GET AUCTION DETAIL ===");
        log.info("Reference: {}", request.getReferenceNo());
        log.info("User Email: {}", request.getUserEmail());
        
        try {
            String requestParam = objectMapper.writeValueAsString(request);
            
            log.info("=== GET AUCTION DETAIL REQUEST ===");
            log.info("Endpoint: POST {}/auction/detail", environment.getHost());
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
            log.info("  auctionDetailParam={}", requestParam);
            log.info("Request Object Details:");
            log.info("  Reference No: {}", request.getReferenceNo());
            log.info("  User Email: {}", request.getUserEmail());
            log.info("🚀 FULL URL: {}/auction/detail", environment.getHost());
            log.info("================================");
            
            String headerValue = objectMapper.writeValueAsString(headerParams);
            
            lastResponse = RestAssured.given()
                    .log().all() // LOG EVERYTHING!
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("auctionDetailParam", requestParam)
                    .when()
                    .post(environment.getHost() + "/auction/detail");
                    
            log.info("=== GET AUCTION DETAIL RESPONSE ===");
            log.info("Status Code: {}", lastResponse.getStatusCode());
            log.info("Response Headers: {}", lastResponse.getHeaders());
            log.info("Response Body: {}", lastResponse.getBody().asString());
            log.info("==================================");
                    
            log.info("Auction detail response status: {}", lastResponse.getStatusCode());
            return lastResponse;
            
        } catch (Exception e) {
            log.error("Failed to get auction detail", e);
            throw new RuntimeException("Failed to get auction detail", e);
        }
    }
    
    /**
     * Reject auction by reference number
     */
    public Response rejectAuction(RejectAuctionRequest request) {
        log.info("=== REJECT AUCTION ===");
        log.info("Reference: {}", request.getReferenceNo());
        log.info("User Email: {}", request.getUserEmail());
        
        try {
            String requestParam = objectMapper.writeValueAsString(request);
            
            log.info("=== REJECT AUCTION REQUEST ===");
            log.info("Endpoint: POST {}/auction/reject", environment.getHost());
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
            log.info("  rejectAuctionParam={}", requestParam);
            log.info("Request Object Details:");
            log.info("  Reference No: {}", request.getReferenceNo());
            log.info("  User Email: {}", request.getUserEmail());
            log.info("🚀 FULL URL: {}/auction/reject", environment.getHost());
            log.info("================================");
            
            String headerValue = objectMapper.writeValueAsString(headerParams);
            
            lastResponse = RestAssured.given()
                    .log().all() // LOG EVERYTHING!
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("rejectAuctionParam", requestParam)
                    .when()
                    .post(environment.getHost() + "/auction/reject");
                    
            log.info("=== REJECT AUCTION RESPONSE ===");
            log.info("Status Code: {}", lastResponse.getStatusCode());
            log.info("Response Headers: {}", lastResponse.getHeaders());
            log.info("Response Body: {}", lastResponse.getBody().asString());
            log.info("==================================");
                    
            log.info("Reject auction response status: {}", lastResponse.getStatusCode());
            return lastResponse;
            
        } catch (Exception e) {
            log.error("Failed to reject auction", e);
            throw new RuntimeException("Failed to reject auction", e);
        }
    }
    
    /**
     * Extract auction status from last response
     */
    public String getAuctionStatus() {
        if (lastResponse == null) {
            log.warn("No response available for auction status extraction");
            return null;
        }
        
        try {
            String responseBody = lastResponse.getBody().asString();
            log.info("Extracting auction status from response: {}", responseBody);
            
            // Parse JSON response to extract actual status
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // Try different possible status field locations
            String status = null;
            
            // Check if response has 'success' flag and 'result' object
            if (jsonNode.has("success") && jsonNode.get("success").asBoolean()) {
                JsonNode resultNode = jsonNode.get("result");
                if (resultNode != null) {
                    // Try common status field names
                    if (resultNode.has("status")) {
                        status = resultNode.get("status").asText();
                    } else if (resultNode.has("auctionStatus")) {
                        status = resultNode.get("auctionStatus").asText();
                    } else if (resultNode.has("state")) {
                        status = resultNode.get("state").asText();
                    }
                }
            }
            
            // If no status found in result, check root level
            if (status == null) {
                if (jsonNode.has("status")) {
                    status = jsonNode.get("status").asText();
                } else if (jsonNode.has("auctionStatus")) {
                    status = jsonNode.get("auctionStatus").asText();
                }
            }
            
            // If still no status, try to infer from response content
            if (status == null) {
                if (responseBody.contains("\"success\":true")) {
                    // If API call was successful but no explicit status, assume UPLOADED/ACTIVE
                    status = "UPLOADED";
                    log.info("No explicit status found, inferring UPLOADED from successful response");
                } else {
                    log.warn("Could not extract status from response, no explicit status field found");
                }
            }
            
            log.info("Extracted auction status: {}", status);
            return status;
            
        } catch (Exception e) {
            log.error("Error extracting auction status from response", e);
            log.error("Response body: {}", lastResponse.getBody().asString());
            return null;
        }
    }
    
    /**
     * Get detailed auction status information
     */
    public AuctionStatusInfo getDetailedAuctionStatus() {
        if (lastResponse == null) {
            return null;
        }
        
        try {
            String responseBody = lastResponse.getBody().asString();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            AuctionStatusInfo statusInfo = new AuctionStatusInfo();
            statusInfo.setRawResponse(responseBody);
            statusInfo.setHttpStatusCode(lastResponse.getStatusCode());
            statusInfo.setSuccess(jsonNode.has("success") && jsonNode.get("success").asBoolean());
            
            if (statusInfo.isSuccess() && jsonNode.has("result")) {
                JsonNode resultNode = jsonNode.get("result");
                
                // Check if there's an auction object in the result
                JsonNode auctionNode = resultNode.has("auction") ? resultNode.get("auction") : resultNode;
                
                // Extract various status fields from auction node first, then fallback to result node
                statusInfo.setStatus(extractFieldAsString(auctionNode, "status", "auctionStatus", "state") != null ? 
                                   extractFieldAsString(auctionNode, "status", "auctionStatus", "state") : 
                                   extractFieldAsString(resultNode, "status", "auctionStatus", "state"));
                
                statusInfo.setReferenceNo(extractFieldAsString(auctionNode, "referenceNo", "reference") != null ? 
                                        extractFieldAsString(auctionNode, "referenceNo", "reference") : 
                                        extractFieldAsString(resultNode, "referenceNo", "reference"));
                
                statusInfo.setMessage(extractFieldAsString(resultNode, "message", "description"));
                
                // Extract financial information from auction node first
                if (auctionNode.has("totalPayableAmount")) {
                    statusInfo.setTotalAmount(auctionNode.get("totalPayableAmount").asDouble());
                } else if (resultNode.has("totalAmount")) {
                    statusInfo.setTotalAmount(resultNode.get("totalAmount").asDouble());
                }
                
                if (auctionNode.has("totalFactoringCount")) {
                    statusInfo.setInvoiceCount(auctionNode.get("totalFactoringCount").asInt());
                } else if (resultNode.has("invoiceCount")) {
                    statusInfo.setInvoiceCount(resultNode.get("invoiceCount").asInt());
                }
                
                // Extract rejection information if available
                if (resultNode.has("rejectionReason")) {
                    statusInfo.setRejectionReason(resultNode.get("rejectionReason").asText());
                }
                if (resultNode.has("rejectDate")) {
                    statusInfo.setRejectDate(resultNode.get("rejectDate").asText());
                }
            } else {
                // Extract error information
                statusInfo.setErrorMessage(extractFieldAsString(jsonNode, "error", "message", "errorMessage"));
            }
            
            return statusInfo;
            
        } catch (Exception e) {
            log.error("Error creating detailed auction status info", e);
            return null;
        }
    }
    
    /**
     * Validate auction amounts and structure
     */
    public ValidationResult validateAuctionAmounts(UploadAuctionRequest request) {
        ValidationResult result = new ValidationResult();
        
        try {
            // Check if request is null
            if (request == null) {
                result.addError("Upload request is null");
                return result;
            }
            
            // Check if invoices exist
            if (request.getInvoices() == null || request.getInvoices().isEmpty()) {
                result.addError("Invoice list is null or empty");
                return result;
            }
            
            // Validate individual invoice amounts
            for (int i = 0; i < request.getInvoices().size(); i++) {
                AuctionInvoice invoice = request.getInvoices().get(i);
                
                if (invoice.getInvoiceAmount() == null || invoice.getInvoiceAmount() <= 0) {
                    result.addError("Invoice " + (i+1) + " has invalid invoice amount: " + invoice.getInvoiceAmount());
                }
                
                if (invoice.getRequestedAmount() == null || invoice.getRequestedAmount() <= 0) {
                    result.addError("Invoice " + (i+1) + " has invalid requested amount: " + invoice.getRequestedAmount());
                }
                
                // Check consistency between invoice amount and requested amount
                if (invoice.getInvoiceAmount() != null && invoice.getRequestedAmount() != null) {
                    if (!invoice.getInvoiceAmount().equals(invoice.getRequestedAmount())) {
                        result.addWarning("Invoice " + (i+1) + " has mismatched amounts: invoice=" + 
                                invoice.getInvoiceAmount() + ", requested=" + invoice.getRequestedAmount());
                    }
                }
            }
            
            // Validate total amounts
            if (request.getTotalPayableAmount() == null || request.getTotalPayableAmount() <= 0) {
                result.addError("Total payable amount is invalid: " + request.getTotalPayableAmount());
            }
            
            if (request.getTotalRequestedAmount() == null || request.getTotalRequestedAmount() <= 0) {
                result.addError("Total requested amount is invalid: " + request.getTotalRequestedAmount());
            }
            
            // Check consistency between totals and individual invoices
            if (request.getTotalPayableAmount() != null && request.getTotalRequestedAmount() != null) {
                double calculatedPayable = request.getInvoices().stream()
                        .mapToDouble(inv -> inv.getInvoiceAmount() != null ? inv.getInvoiceAmount() : 0.0)
                        .sum();
                double calculatedRequested = request.getInvoices().stream()
                        .mapToDouble(inv -> inv.getRequestedAmount() != null ? inv.getRequestedAmount() : 0.0)
                        .sum();
                
                if (Math.abs(calculatedPayable - request.getTotalPayableAmount()) > 0.01) {
                    result.addError("Total payable amount mismatch: declared=" + request.getTotalPayableAmount() + 
                            ", calculated=" + calculatedPayable);
                }
                
                if (Math.abs(calculatedRequested - request.getTotalRequestedAmount()) > 0.01) {
                    result.addError("Total requested amount mismatch: declared=" + request.getTotalRequestedAmount() + 
                            ", calculated=" + calculatedRequested);
                }
            }
            
            // Check locked field
            if (request.getLocked() == null || !request.getLocked()) {
                result.addWarning("Auction should be locked (locked: true)");
            }
            
            // Check reference number format
            if (request.getReferenceNo() == null || request.getReferenceNo().trim().isEmpty()) {
                result.addError("Reference number is null or empty");
            } else if (!request.getReferenceNo().startsWith("TEST-AUC-")) {
                result.addWarning("Reference number does not follow recommended format: TEST-AUC-<UUID>");
            }
            
            result.setValid(result.getErrors().isEmpty());
            
        } catch (Exception e) {
            result.addError("Validation exception: " + e.getMessage());
            log.error("Error during auction validation", e);
        }
        
        return result;
    }
    
    /**
     * Check if last response was successful
     */
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
    
    /**
     * Helper method to extract field value with multiple possible field names
     */
    private String extractFieldAsString(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                return node.get(fieldName).asText();
            }
        }
        return null;
    }
    
    // Getters
    public Response getLastResponse() {
        return lastResponse;
    }
    
    public int getLastStatusCode() {
        return lastResponse != null ? lastResponse.getStatusCode() : -1;
    }
    
    public EnvironmentManager.EnvironmentConfig getEnvironment() {
        return environment;
    }
    
    public String getSessionId() {
        return sessionId;
    }
} 