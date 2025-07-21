package com.faturalab.automation.utils;

import com.faturalab.automation.models.UploadInvoiceRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class InvoiceTestDataGenerator {
    
    private static final String[] INVOICE_TYPES = {"E_FATURA", "E_ARSIV", "PAPER"};
    private static final String[] CURRENCIES = {"TL", "USD", "EUR"};
    private static final String[] SUPPLIER_TAX_NUMBERS = {
        "1234567890", "9876543210", "5555555555", "1111111111"
    };
    
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
                .taxExclusiveAmount(invoiceType.equals("E_ARSIV") ? 100.0 : 0.0)
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
            case "pastDueDate":
                validInvoice.setDueDate("2020-01-01");
                break;
            case "holidayDueDate":
                validInvoice.setDueDate("2025-12-25"); // Christmas
                break;
            case "eFaturaWithoutHashCode":
                validInvoice.setInvoiceType("E_FATURA");
                validInvoice.setHashCode("");
                break;
            case "eArsivWithoutTaxExclusive":
                validInvoice.setInvoiceType("E_ARSIV");
                validInvoice.setTaxExclusiveAmount(0);
                break;
        }
        
        return validInvoice;
    }
    
    public static String generateUniqueInvoiceNo() {
        return "INV" + System.currentTimeMillis();
    }
    
    public static String generateHashCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
    
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
    
    public static String getFutureWorkingDate(int daysFromNow) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysFromNow);
        
        // Skip weekends and holidays
        while (isWeekendOrHoliday(cal)) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }
    
    public static String getCurrentDateTimeISO() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        return sdf.format(new Date());
    }
    
    private static boolean isWeekendOrHoliday(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return true;
        }
        
        // Check for common holidays
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        
        // New Year's Day
        if (month == Calendar.JANUARY && day == 1) return true;
        
        // Christmas
        if (month == Calendar.DECEMBER && day == 25) return true;
        
        // Add more holidays as needed
        
        return false;
    }
    
    private static String getRandomSupplierTaxNo() {
        int randomIndex = (int) (Math.random() * SUPPLIER_TAX_NUMBERS.length);
        return SUPPLIER_TAX_NUMBERS[randomIndex];
    }
    
    private static double getRandomAmount() {
        return Math.round((Math.random() * 10000 + 100) * 100.0) / 100.0; // Between 100-10100
    }
} 