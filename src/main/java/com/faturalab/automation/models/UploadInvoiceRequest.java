package com.faturalab.automation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadInvoiceRequest {
    
    @JsonProperty("userEmail")
    private String userEmail;
    
    @JsonProperty("supplierTaxNo")
    private String supplierTaxNo;
    
    @JsonProperty("invoiceAmount")
    private double invoiceAmount;
    
    @JsonProperty("remainingAmount")
    private double remainingAmount;
    
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
    private String invoiceType; // E_FATURA, E_ARSIV, PAPER
    
    @JsonProperty("hashCode")
    private String hashCode;
    
    @JsonProperty("taxExclusiveAmount")
    private double taxExclusiveAmount;
    
    @JsonProperty("fileName")
    private String fileName;
    
    @JsonProperty("fileData")
    private String fileData;
    
    @JsonProperty("packageNo")
    private String packageNo;
    
    @JsonProperty("orderNo")
    private String orderNo;
    
    @JsonProperty("itemNo")
    private String itemNo;
    
    @JsonProperty("extraInvoiceDueDay")
    private String extraInvoiceDueDay;
    
    public UploadInvoiceRequest() {}
    
    // Builder pattern for easy construction
    public static class Builder {
        private UploadInvoiceRequest request = new UploadInvoiceRequest();
        
        public Builder userEmail(String userEmail) {
            request.userEmail = userEmail;
            return this;
        }
        
        public Builder supplierTaxNo(String supplierTaxNo) {
            request.supplierTaxNo = supplierTaxNo;
            return this;
        }
        
        public Builder invoiceAmount(double invoiceAmount) {
            request.invoiceAmount = invoiceAmount;
            return this;
        }
        
        public Builder remainingAmount(double remainingAmount) {
            request.remainingAmount = remainingAmount;
            return this;
        }
        
        public Builder currencyType(String currencyType) {
            request.currencyType = currencyType;
            return this;
        }
        
        public Builder invoiceDate(String invoiceDate) {
            request.invoiceDate = invoiceDate;
            return this;
        }
        
        public Builder dueDate(String dueDate) {
            request.dueDate = dueDate;
            return this;
        }
        
        public Builder additionalDueDate(String additionalDueDate) {
            request.additionalDueDate = additionalDueDate;
            return this;
        }
        
        public Builder invoiceNo(String invoiceNo) {
            request.invoiceNo = invoiceNo;
            return this;
        }
        
        public Builder invoiceType(String invoiceType) {
            request.invoiceType = invoiceType;
            return this;
        }
        
        public Builder hashCode(String hashCode) {
            request.hashCode = hashCode;
            return this;
        }
        
        public Builder taxExclusiveAmount(double taxExclusiveAmount) {
            request.taxExclusiveAmount = taxExclusiveAmount;
            return this;
        }
        
        public Builder orderNo(String orderNo) {
            request.orderNo = orderNo;
            return this;
        }
        
        public UploadInvoiceRequest build() {
            return request;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getSupplierTaxNo() { return supplierTaxNo; }
    public void setSupplierTaxNo(String supplierTaxNo) { this.supplierTaxNo = supplierTaxNo; }
    
    public double getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(double invoiceAmount) { this.invoiceAmount = invoiceAmount; }
    
    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }
    
    public String getCurrencyType() { return currencyType; }
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }
    
    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }
    
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    
    public String getAdditionalDueDate() { return additionalDueDate; }
    public void setAdditionalDueDate(String additionalDueDate) { this.additionalDueDate = additionalDueDate; }
    
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    
    public String getHashCode() { return hashCode; }
    public void setHashCode(String hashCode) { this.hashCode = hashCode; }
    
    public double getTaxExclusiveAmount() { return taxExclusiveAmount; }
    public void setTaxExclusiveAmount(double taxExclusiveAmount) { this.taxExclusiveAmount = taxExclusiveAmount; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFileData() { return fileData; }
    public void setFileData(String fileData) { this.fileData = fileData; }
    
    public String getPackageNo() { return packageNo; }
    public void setPackageNo(String packageNo) { this.packageNo = packageNo; }
    
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    
    public String getItemNo() { return itemNo; }
    public void setItemNo(String itemNo) { this.itemNo = itemNo; }
    
    public String getExtraInvoiceDueDay() { return extraInvoiceDueDay; }
    public void setExtraInvoiceDueDay(String extraInvoiceDueDay) { this.extraInvoiceDueDay = extraInvoiceDueDay; }
    
    @Override
    public String toString() {
        return "UploadInvoiceRequest{" +
                "invoiceNo='" + invoiceNo + '\'' +
                ", invoiceAmount=" + invoiceAmount +
                ", invoiceType='" + invoiceType + '\'' +
                ", supplierTaxNo='" + supplierTaxNo + '\'' +
                '}';
    }
} 