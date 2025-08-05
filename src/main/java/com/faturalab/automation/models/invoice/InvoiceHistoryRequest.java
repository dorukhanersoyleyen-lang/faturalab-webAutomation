package com.faturalab.automation.models.invoice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InvoiceHistoryRequest {
    
    @JsonProperty("fromDate")
    private String fromDate;
    
    @JsonProperty("toDate")
    private String toDate;
    
    @JsonProperty("onlyLastState")
    private boolean onlyLastState;
    
    // Constructors
    public InvoiceHistoryRequest() {}
    
    public InvoiceHistoryRequest(String fromDate, String toDate, boolean onlyLastState) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.onlyLastState = onlyLastState;
    }
    
    // Getters and Setters
    public String getFromDate() {
        return fromDate;
    }
    
    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }
    
    public String getToDate() {
        return toDate;
    }
    
    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
    
    public boolean isOnlyLastState() {
        return onlyLastState;
    }
    
    public void setOnlyLastState(boolean onlyLastState) {
        this.onlyLastState = onlyLastState;
    }
} 