package com.faturalab.automation.models.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadInvoiceRequest {
    
    @JsonProperty("userEmail")
    private String userEmail;
    
    @JsonProperty("supplierTaxNo")
    private String supplierTaxNo;
    
    @JsonProperty("invoiceAmount")
    private int invoiceAmount;
    
    @JsonProperty("remainingAmount")
    private int remainingAmount;
    
    @JsonProperty("currencyType")
    private String currencyType;
    
    @JsonProperty("invoiceDate")
    private String invoiceDate;
    
    @JsonProperty("dueDate")
    private String dueDate;
    
    @JsonProperty("additionalDueDate")
    private String additionalDueDate;
    
    @JsonProperty("invoiceNo")
    private String invoiceNo;
    
    @JsonProperty("invoiceType")
    private String invoiceType;
    
    @JsonProperty("hashCode")
    private String hashCode;
    
    @JsonProperty("taxExclusiveAmount")
    private int taxExclusiveAmount;
    
    // Constructors
    public UploadInvoiceRequest() {}
    
    // Builder pattern
    public static UploadInvoiceRequestBuilder builder() {
        return new UploadInvoiceRequestBuilder();
    }
    
    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getSupplierTaxNo() {
        return supplierTaxNo;
    }
    
    public void setSupplierTaxNo(String supplierTaxNo) {
        this.supplierTaxNo = supplierTaxNo;
    }
    
    public int getInvoiceAmount() {
        return invoiceAmount;
    }
    
    public void setInvoiceAmount(int invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }
    
    public int getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(int remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public String getCurrencyType() {
        return currencyType;
    }
    
    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }
    
    public String getInvoiceDate() {
        return invoiceDate;
    }
    
    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    
    public String getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getAdditionalDueDate() {
        return additionalDueDate;
    }
    
    public void setAdditionalDueDate(String additionalDueDate) {
        this.additionalDueDate = additionalDueDate;
    }
    
    public String getInvoiceNo() {
        return invoiceNo;
    }
    
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    
    public String getInvoiceType() {
        return invoiceType;
    }
    
    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }
    
    public String getHashCode() {
        return hashCode;
    }
    
    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }
    
    public int getTaxExclusiveAmount() {
        return taxExclusiveAmount;
    }
    
    public void setTaxExclusiveAmount(int taxExclusiveAmount) {
        this.taxExclusiveAmount = taxExclusiveAmount;
    }
    
    // Builder class
    public static class UploadInvoiceRequestBuilder {
        private UploadInvoiceRequest request = new UploadInvoiceRequest();
        
        public UploadInvoiceRequestBuilder userEmail(String userEmail) {
            request.setUserEmail(userEmail);
            return this;
        }
        
        public UploadInvoiceRequestBuilder supplierTaxNo(String supplierTaxNo) {
            request.setSupplierTaxNo(supplierTaxNo);
            return this;
        }
        
        public UploadInvoiceRequestBuilder invoiceAmount(int invoiceAmount) {
            request.setInvoiceAmount(invoiceAmount);
            return this;
        }
        
        public UploadInvoiceRequestBuilder remainingAmount(int remainingAmount) {
            request.setRemainingAmount(remainingAmount);
            return this;
        }
        
        public UploadInvoiceRequestBuilder currencyType(String currencyType) {
            request.setCurrencyType(currencyType);
            return this;
        }
        
        public UploadInvoiceRequestBuilder invoiceDate(String invoiceDate) {
            request.setInvoiceDate(invoiceDate);
            return this;
        }
        
        public UploadInvoiceRequestBuilder dueDate(String dueDate) {
            request.setDueDate(dueDate);
            return this;
        }
        
        public UploadInvoiceRequestBuilder additionalDueDate(String additionalDueDate) {
            request.setAdditionalDueDate(additionalDueDate);
            return this;
        }
        
        public UploadInvoiceRequestBuilder invoiceNo(String invoiceNo) {
            request.setInvoiceNo(invoiceNo);
            return this;
        }
        
        public UploadInvoiceRequestBuilder invoiceType(String invoiceType) {
            request.setInvoiceType(invoiceType);
            return this;
        }
        
        public UploadInvoiceRequestBuilder hashCode(String hashCode) {
            request.setHashCode(hashCode);
            return this;
        }
        
        public UploadInvoiceRequestBuilder taxExclusiveAmount(int taxExclusiveAmount) {
            request.setTaxExclusiveAmount(taxExclusiveAmount);
            return this;
        }
        
        public UploadInvoiceRequest build() {
            return request;
        }
    }
} 