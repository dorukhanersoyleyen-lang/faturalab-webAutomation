package com.faturalab.automation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InvoiceHistoryRequest {
    
    @JsonProperty("fromDate")
    private String fromDate;
    
    @JsonProperty("onlyLastState")
    private boolean onlyLastState;
    
    @JsonProperty("toDate")
    private String toDate;
    
    @JsonProperty("invoiceHistoryState")
    private String invoiceHistoryState;
    
    @JsonProperty("invoiceType")
    private String invoiceType;
    
    @JsonProperty("currencyType")
    private String currencyType;
    
    @JsonProperty("minInvoiceAmount")
    private Double minInvoiceAmount;
    
    @JsonProperty("maxInvoiceAmount")
    private Double maxInvoiceAmount;
    
    public InvoiceHistoryRequest() {}
    
    public InvoiceHistoryRequest(String fromDate, boolean onlyLastState) {
        this.fromDate = fromDate;
        this.onlyLastState = onlyLastState;
    }
    
    // Getters and Setters
    public String getFromDate() {
        return fromDate;
    }
    
    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }
    
    public boolean isOnlyLastState() {
        return onlyLastState;
    }
    
    public void setOnlyLastState(boolean onlyLastState) {
        this.onlyLastState = onlyLastState;
    }
    
    public String getToDate() {
        return toDate;
    }
    
    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
    
    public String getInvoiceHistoryState() {
        return invoiceHistoryState;
    }
    
    public void setInvoiceHistoryState(String invoiceHistoryState) {
        this.invoiceHistoryState = invoiceHistoryState;
    }
    
    public String getInvoiceType() {
        return invoiceType;
    }
    
    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }
    
    public String getCurrencyType() {
        return currencyType;
    }
    
    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }
    
    public Double getMinInvoiceAmount() {
        return minInvoiceAmount;
    }
    
    public void setMinInvoiceAmount(Double minInvoiceAmount) {
        this.minInvoiceAmount = minInvoiceAmount;
    }
    
    public Double getMaxInvoiceAmount() {
        return maxInvoiceAmount;
    }
    
    public void setMaxInvoiceAmount(Double maxInvoiceAmount) {
        this.maxInvoiceAmount = maxInvoiceAmount;
    }
    
    @Override
    public String toString() {
        return "InvoiceHistoryRequest{" +
                "fromDate='" + fromDate + '\'' +
                ", onlyLastState=" + onlyLastState +
                '}';
    }
} 