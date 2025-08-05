package com.faturalab.automation.models.auction;

public class AuctionStatusInfo {
    
    private String status;
    private String referenceNo;
    private String message;
    private String errorMessage;
    private String rejectionReason;
    private String rejectDate;
    private Double totalAmount;
    private Integer invoiceCount;
    private boolean success;
    private int httpStatusCode;
    private String rawResponse;
    
    public AuctionStatusInfo() {}
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getReferenceNo() {
        return referenceNo;
    }
    
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getRejectDate() {
        return rejectDate;
    }
    
    public void setRejectDate(String rejectDate) {
        this.rejectDate = rejectDate;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Integer getInvoiceCount() {
        return invoiceCount;
    }
    
    public void setInvoiceCount(Integer invoiceCount) {
        this.invoiceCount = invoiceCount;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
    
    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
    
    public String getRawResponse() {
        return rawResponse;
    }
    
    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }
    
    // Helper methods
    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }
    
    public boolean isUploaded() {
        return "UPLOADED".equalsIgnoreCase(status) || "DRAFT".equalsIgnoreCase(status);
    }
    
    public boolean hasRejectionInfo() {
        return rejectionReason != null && !rejectionReason.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "AuctionStatusInfo{" +
                "status='" + status + '\'' +
                ", referenceNo='" + referenceNo + '\'' +
                ", success=" + success +
                ", httpStatusCode=" + httpStatusCode +
                ", message='" + message + '\'' +
                (errorMessage != null ? ", errorMessage='" + errorMessage + '\'' : "") +
                (rejectionReason != null ? ", rejectionReason='" + rejectionReason + '\'' : "") +
                '}';
    }
} 