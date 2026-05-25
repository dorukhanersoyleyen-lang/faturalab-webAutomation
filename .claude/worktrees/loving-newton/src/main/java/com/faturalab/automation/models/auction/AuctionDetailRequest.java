package com.faturalab.automation.models.auction;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuctionDetailRequest {
    
    @JsonProperty("referenceNo")
    private String referenceNo;
    
    @JsonProperty("userEmail")
    private String userEmail;
    
    // Constructors
    public AuctionDetailRequest() {}
    
    public AuctionDetailRequest(String referenceNo, String userEmail) {
        this.referenceNo = referenceNo;
        this.userEmail = userEmail;
    }
    
    // Getters and Setters
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