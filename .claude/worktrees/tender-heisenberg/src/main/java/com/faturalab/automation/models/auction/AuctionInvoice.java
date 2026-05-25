package com.faturalab.automation.models.auction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class AuctionInvoice {
    
    @JsonProperty("currencyType")
    private String currencyType;
    
    @JsonProperty("dueDate")
    private String dueDate;
    
    @JsonProperty("extraInvoiceDueDay")
    private Integer extraInvoiceDueDay;
    
    @JsonProperty("taxExclusiveAmount")
    @JsonSerialize(using = AmountSerializer.class)
    private Double taxExclusiveAmount;
    
    @JsonProperty("invoiceAmount")
    @JsonSerialize(using = AmountSerializer.class)
    private Double invoiceAmount;
    
    @JsonProperty("requestedAmount") 
    @JsonSerialize(using = AmountSerializer.class)
    private Double requestedAmount;
    
    @JsonProperty("invoiceDate")
    private String invoiceDate;
    
    @JsonProperty("invoiceType")
    private String invoiceType;
    
    @JsonProperty("invoiceETTN")
    private String invoiceETTN;
    
    @JsonProperty("invoiceTypeCode")
    private String invoiceTypeCode;
    
    @JsonProperty("packageNo")
    private String packageNo;
    
    @JsonProperty("orderNo")
    private String orderNo;
    
    @JsonProperty("itemNo")
    private String itemNo;
    
    @JsonProperty("supplierTaxNo")
    private String supplierTaxNo;
    
    @JsonProperty("invoiceNo")
    private String invoiceNo;
    
    // Constructors
    public AuctionInvoice() {}
    
    public AuctionInvoice(String packageNo, String supplierTaxNo, Double invoiceAmount, String invoiceType) {
        this.packageNo = packageNo;
        this.supplierTaxNo = supplierTaxNo;
        this.invoiceAmount = invoiceAmount;
        this.requestedAmount = invoiceAmount * 0.95; // 95% of invoice amount to avoid validation error
        this.invoiceType = invoiceType;
        
        // Default values with future dates
        this.currencyType = "TL";
        this.dueDate = java.time.LocalDate.now().plusDays(30).toString(); // 30 days from now
        this.extraInvoiceDueDay = 0;
        this.taxExclusiveAmount = 0.0; // Set to 0 instead of null for validation
        this.invoiceDate = java.time.LocalDate.now().minusDays(1).toString(); // Yesterday
        this.invoiceETTN = "";
        this.invoiceTypeCode = "SATIS";
        
        // Safe generation for orderNo and itemNo - handle non-numeric packageNo
        String packageSuffix = packageNo.replaceAll("[^0-9]", ""); // Extract only numbers
        if (packageSuffix.length() >= 4) {
            this.orderNo = "1000" + packageSuffix.substring(packageSuffix.length() - 4); // Last 4 digits
            this.itemNo = "200" + packageSuffix.substring(Math.max(0, packageSuffix.length() - 6)); // Last 6 digits
        } else {
            // Fallback for short packageNo
            String timestamp = String.valueOf(System.currentTimeMillis() % 10000); // Last 4 digits of timestamp
            this.orderNo = "1000" + timestamp;
            this.itemNo = "200" + timestamp;
        }
    }
    
    // Getters and Setters
    public String getCurrencyType() { return currencyType; }
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public Integer getExtraInvoiceDueDay() { return extraInvoiceDueDay; }
    public void setExtraInvoiceDueDay(Integer extraInvoiceDueDay) { this.extraInvoiceDueDay = extraInvoiceDueDay; }
    @JsonSerialize(using = AmountSerializer.class)
    public Double getTaxExclusiveAmount() { return taxExclusiveAmount; }
    public void setTaxExclusiveAmount(Double taxExclusiveAmount) { this.taxExclusiveAmount = taxExclusiveAmount; }
    @JsonSerialize(using = AmountSerializer.class)
    public Double getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(Double invoiceAmount) { this.invoiceAmount = invoiceAmount; }
    @JsonSerialize(using = AmountSerializer.class)
    public Double getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(Double requestedAmount) { this.requestedAmount = requestedAmount; }
    public String getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    public String getInvoiceETTN() { return invoiceETTN; }
    public void setInvoiceETTN(String invoiceETTN) { this.invoiceETTN = invoiceETTN; }
    public String getInvoiceTypeCode() { return invoiceTypeCode; }
    public void setInvoiceTypeCode(String invoiceTypeCode) { this.invoiceTypeCode = invoiceTypeCode; }
    public String getPackageNo() { return packageNo; }
    public void setPackageNo(String packageNo) { this.packageNo = packageNo; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getItemNo() { return itemNo; }
    public void setItemNo(String itemNo) { this.itemNo = itemNo; }
    public String getSupplierTaxNo() { return supplierTaxNo; }
    public void setSupplierTaxNo(String supplierTaxNo) { this.supplierTaxNo = supplierTaxNo; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    // ==== int-overload setters for stricter integer inputs (keeps JSON integers with serializer) ====
    public void setTaxExclusiveAmount(int taxExclusiveAmount) { this.taxExclusiveAmount = (double) taxExclusiveAmount; }
    public void setInvoiceAmount(int invoiceAmount) { this.invoiceAmount = (double) invoiceAmount; }
    public void setRequestedAmount(int requestedAmount) { this.requestedAmount = (double) requestedAmount; }
} 