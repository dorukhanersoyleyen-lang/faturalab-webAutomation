package com.faturalab.automation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticateRequest {
    
    @JsonProperty("alias")
    private String alias;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("taxNumber")
    private String taxNumber;
    
    public AuthenticateRequest() {}
    
    public AuthenticateRequest(String alias, String password, String taxNumber) {
        this.alias = alias;
        this.password = password;
        this.taxNumber = taxNumber;
    }
    
    // Getters and Setters
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getTaxNumber() {
        return taxNumber;
    }
    
    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }
    
    @Override
    public String toString() {
        return "AuthenticateRequest{" +
                "alias='" + alias + '\'' +
                ", taxNumber='" + taxNumber + '\'' +
                '}';
    }
} 