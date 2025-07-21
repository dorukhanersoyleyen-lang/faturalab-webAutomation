package com.faturalab.automation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("result")
    private T result;
    
    @JsonProperty("errorCode")
    private String errorCode;
    
    @JsonProperty("error")
    private ErrorInfo error;
    
    public static class ErrorInfo {
        @JsonProperty("errorCode")
        private String errorCode;
        
        @JsonProperty("errorDescription")
        private String errorDescription;
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorDescription() { return errorDescription; }
        public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }
        
        @Override
        public String toString() {
            return "ErrorInfo{errorCode='" + errorCode + "', errorDescription='" + errorDescription + "'}";
        }
    }
    
    public ApiResponse() {}
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getResult() {
        return result;
    }
    
    public void setResult(T result) {
        this.result = result;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public ErrorInfo getError() {
        return error;
    }
    
    public void setError(ErrorInfo error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", error=" + error +
                '}';
    }
} 