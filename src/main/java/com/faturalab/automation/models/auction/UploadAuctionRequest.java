package com.faturalab.automation.models.auction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class UploadAuctionRequest {
    
    @JsonProperty("invoices")
    private List<AuctionInvoice> invoices;
    
    @JsonProperty("locked")
    private Boolean locked;
    
    @JsonProperty("totalPayableAmount")
    private Double totalPayableAmount;
    
    @JsonProperty("totalRequestedAmount")
    private Double totalRequestedAmount;
    
    @JsonProperty("referenceNo")
    private String referenceNo;
    
    @JsonProperty("userEmail")
    private String userEmail;
    
    // Constructors
    public UploadAuctionRequest() {}
    
    public UploadAuctionRequest(List<AuctionInvoice> invoices, String referenceNo, String userEmail) {
        this.invoices = invoices;
        this.referenceNo = referenceNo;
        this.userEmail = userEmail;
        this.locked = true; // ALWAYS TRUE as per requirements
        
        // Calculate totals from invoices
        this.totalPayableAmount = invoices.stream()
                .mapToDouble(invoice -> invoice.getInvoiceAmount() != null ? invoice.getInvoiceAmount() : 0.0)
                .sum();
        this.totalRequestedAmount = invoices.stream()
                .mapToDouble(invoice -> invoice.getRequestedAmount() != null ? invoice.getRequestedAmount() : 0.0)
                .sum();
    }
    
    // Getters and Setters
    public List<AuctionInvoice> getInvoices() {
        return invoices;
    }
    
    public void setInvoices(List<AuctionInvoice> invoices) {
        this.invoices = invoices;
    }
    
    public Boolean getLocked() {
        return locked;
    }
    
    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
    
    public Double getTotalPayableAmount() {
        return totalPayableAmount;
    }
    
    public void setTotalPayableAmount(Double totalPayableAmount) {
        this.totalPayableAmount = totalPayableAmount;
    }
    
    public Double getTotalRequestedAmount() {
        return totalRequestedAmount;
    }
    
    public void setTotalRequestedAmount(Double totalRequestedAmount) {
        this.totalRequestedAmount = totalRequestedAmount;
    }
    
    public String getReferenceNo() {
        return referenceNo;
    }
    
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
} 