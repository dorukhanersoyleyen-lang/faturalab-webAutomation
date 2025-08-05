package com.faturalab.automation.utils;

import com.faturalab.automation.models.invoice.UploadInvoiceRequest;
import com.faturalab.automation.models.auction.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

public class InvoiceTestDataGenerator {
    
    private static final String[] INVOICE_TYPES = {"E_FATURA", "E_ARSIV", "PAPER"};
    private static final String[] CURRENCIES = {"TL", "USD", "EUR"};
    private static final String[] SUPPLIER_TAX_NUMBERS = {
        "1234567890", "9876543210", "5555555555", "1111111111", "1083053674"
    };
    
    // ============== EXISTING INVOICE METHODS ==============
    
    public static UploadInvoiceRequest generateValidInvoice(String userEmail, String invoiceType) {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        
        return UploadInvoiceRequest.builder()
                .userEmail(userEmail)
                .supplierTaxNo(getRandomSupplierTaxNo())
                .invoiceAmount(getRandomAmount())
                .remainingAmount(getRandomAmount())
                .currencyType("TL")
                .invoiceDate(getCurrentDate())
                .dueDate(getFutureWorkingDate(60))
                .additionalDueDate(getFutureWorkingDate(60))
                .invoiceNo("TEST" + uniqueId)
                .invoiceType(invoiceType)
                .hashCode(invoiceType.equals("E_FATURA") ? generateHashCode() : "")
                .taxExclusiveAmount(invoiceType.equals("E_ARSIV") ? 85 : 0) // Simple value for E_ARSIV
                .build();
    }
    
    public static UploadInvoiceRequest generateInvalidInvoice(String userEmail, String invalidField) {
        UploadInvoiceRequest validInvoice = generateValidInvoice(userEmail, "E_FATURA");
        
        switch (invalidField) {
            case "emptyInvoiceNo":
                validInvoice.setInvoiceNo("");
                break;
            case "zeroAmount":
                validInvoice.setInvoiceAmount(0);
                break;
            case "negativeAmount":
                validInvoice.setInvoiceAmount(-100);
                break;
            case "emptySupplierTax":
                validInvoice.setSupplierTaxNo("");
                break;
            case "invalidDate":
                validInvoice.setInvoiceDate("invalid-date");
                break;
            case "futureInvoiceDate":
                validInvoice.setInvoiceDate(getFutureDate(30));
                break;
            case "pastDueDate":
                validInvoice.setDueDate(getPastDate(30));
                break;
            default:
                throw new IllegalArgumentException("Unknown invalid field: " + invalidField);
        }
        
        return validInvoice;
    }
    
    // ============== NEW AUCTION METHODS ==============
    
    /**
     * Generates a unique reference number for auction uploads
     * Format: TEST-AUC-<UUID>
     */
    public static String generateUniqueReferenceNo() {
        return "TEST-AUC-" + UUID.randomUUID().toString();
    }
    
    /**
     * Generates an array of auction invoices with specified count and total amount
     * Amounts are distributed evenly with realistic variations
     */
    public static List<AuctionInvoice> generateInvoiceArray(int count, BigDecimal totalAmount) {
        if (count <= 0) {
            throw new IllegalArgumentException("Invoice count must be greater than 0");
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be greater than 0");
        }
        
        List<AuctionInvoice> invoices = new ArrayList<>();
        Random random = new Random();
        
        // Calculate base amount per invoice
        BigDecimal baseAmount = totalAmount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        BigDecimal allocatedAmount = BigDecimal.ZERO;
        
        for (int i = 0; i < count; i++) {
            String packageNo = generatePackageNo(i + 1);
            String supplierTaxNo = getRandomSupplierTaxNo();
            String invoiceType = getRandomInvoiceType();
            
            BigDecimal invoiceAmount;
            
            if (i == count - 1) {
                // Last invoice gets the remaining amount to ensure exact total
                invoiceAmount = totalAmount.subtract(allocatedAmount);
            } else {
                // Add some variation (+/- 20%) but ensure positive amount
                double variation = 0.8 + (random.nextDouble() * 0.4); // 0.8 to 1.2
                invoiceAmount = baseAmount.multiply(BigDecimal.valueOf(variation))
                                        .setScale(2, RoundingMode.HALF_UP);
                
                // Ensure minimum amount of 100
                if (invoiceAmount.compareTo(BigDecimal.valueOf(100)) < 0) {
                    invoiceAmount = BigDecimal.valueOf(100);
                }
            }
            
            allocatedAmount = allocatedAmount.add(invoiceAmount);
            
            AuctionInvoice invoice = new AuctionInvoice(packageNo, supplierTaxNo, 
                                                       invoiceAmount.doubleValue(), invoiceType);
            
            // Set additional realistic data
            invoice.setCurrencyType("TL");
            invoice.setDueDate(getFutureDate(random.nextInt(60) + 30)); // 30-90 days
            invoice.setInvoiceDate(getPastDate(random.nextInt(30) + 1)); // 1-30 days ago
            invoice.setExtraInvoiceDueDay(random.nextInt(5)); // 0-4 extra days
            
            invoices.add(invoice);
        }
        
        return invoices;
    }
    
    /**
     * Creates a complete dummy payload for auction upload testing
     */
    public static UploadAuctionRequest getDummyPayloadForAuctionUpload(String userEmail, int invoiceCount, BigDecimal totalAmount) {
        String referenceNo = generateUniqueReferenceNo();
        List<AuctionInvoice> invoices = generateInvoiceArray(invoiceCount, totalAmount);
        
        return new UploadAuctionRequest(invoices, referenceNo, userEmail);
    }
    
    /**
     * Creates a dummy payload with predefined common values for quick testing
     */
    public static UploadAuctionRequest getDummyPayloadForAuctionUpload(String userEmail) {
        // Default: 3 invoices, total 125,000 TL (similar to curl example)
        return getDummyPayloadForAuctionUpload(userEmail, 3, BigDecimal.valueOf(125000));
    }
    
    /**
     * Creates test data for single auction invoice (for simple tests)
     */
    public static AuctionInvoice generateSingleAuctionInvoice(BigDecimal amount, String invoiceType) {
        String packageNo = generatePackageNo(1);
        String supplierTaxNo = getRandomSupplierTaxNo();
        
        AuctionInvoice invoice = new AuctionInvoice(packageNo, supplierTaxNo, amount.doubleValue(), invoiceType);
        
        // Set realistic defaults
        invoice.setCurrencyType("TL");
        invoice.setDueDate(getFutureDate(45));
        invoice.setInvoiceDate(getPastDate(7));
        invoice.setExtraInvoiceDueDay(0);
        
        return invoice;
    }
    
    /**
     * Creates test data with invalid amounts (for negative testing)
     */
    public static UploadAuctionRequest getInvalidAmountPayload(String userEmail, String invalidType) {
        String referenceNo = generateUniqueReferenceNo();
        List<AuctionInvoice> invoices = new ArrayList<>();
        
        switch (invalidType) {
            case "zeroAmount":
                invoices.add(generateSingleAuctionInvoice(BigDecimal.ZERO, "PAPER"));
                break;
            case "negativeAmount":
                invoices.add(generateSingleAuctionInvoice(BigDecimal.valueOf(-1000), "E_FATURA"));
                break;
            case "emptyInvoiceList":
                // invoices list remains empty
                break;
            default:
                throw new IllegalArgumentException("Unknown invalid type: " + invalidType);
        }
        
        return new UploadAuctionRequest(invoices, referenceNo, userEmail);
    }
    
    /**
     * Creates test data for auction detail/reject requests
     */
    public static AuctionDetailRequest generateAuctionDetailRequest(String referenceNo, String userEmail) {
        return new AuctionDetailRequest(referenceNo, userEmail);
    }
    
    public static RejectAuctionRequest generateRejectAuctionRequest(String referenceNo, String userEmail) {
        return new RejectAuctionRequest(referenceNo, userEmail);
    }
    
    // ============== HELPER METHODS ==============
    
    private static String generatePackageNo(int sequence) {
        // Format: 0030000049, 0000700081, etc.
        Random random = new Random();
        return String.format("%04d%06d", random.nextInt(1000), random.nextInt(1000000));
    }
    
    private static String getRandomInvoiceType() {
        Random random = new Random();
        return INVOICE_TYPES[random.nextInt(INVOICE_TYPES.length)];
    }
    
    private static String getRandomSupplierTaxNo() {
        Random random = new Random();
        return SUPPLIER_TAX_NUMBERS[random.nextInt(SUPPLIER_TAX_NUMBERS.length)];
    }
    
    private static int getRandomAmount() {
        Random random = new Random();
        return 1000 + random.nextInt(9000); // 1000-9999
    }
    
    private static String getFutureDate(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }
    
    private static String getPastDate(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }
    
    private static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
    
    private static String getFutureWorkingDate(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        
        // Skip weekends
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 2);
        } else if (dayOfWeek == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }
    
    public static String getCurrentDateTimeISO() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX");  // XX = +0300, XXX = +03:00
        return sdf.format(new Date());
    }
    
    private static String generateHashCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
} 