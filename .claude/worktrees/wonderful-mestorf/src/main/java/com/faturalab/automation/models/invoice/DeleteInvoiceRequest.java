package com.faturalab.automation.models.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteInvoiceRequest {
    
    @JsonProperty("userEmail")
    private String userEmail;
    
    @JsonProperty("invoiceNo")
    private String invoiceNo;
    
    @JsonProperty("supplierTaxNo")
    private String supplierTaxNo;
    
    // Constructors
    public DeleteInvoiceRequest() {}
    
    public DeleteInvoiceRequest(String userEmail, String invoiceNo, String supplierTaxNo) {
        this.userEmail = userEmail;
        this.invoiceNo = invoiceNo;
        this.supplierTaxNo = supplierTaxNo;
    }
    
    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getInvoiceNo() {
        return invoiceNo;
    }
    
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    
    public String getSupplierTaxNo() {
        return supplierTaxNo;
    }
    
    public void setSupplierTaxNo(String supplierTaxNo) {
        this.supplierTaxNo = supplierTaxNo;
    }
} 