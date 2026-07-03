package com.faturalab.automation.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TZF işlemi senaryosu boyunca üretilen test verisini adımlar arasında taşır.
 *
 * Akış: Excel üretimi → alıcı yükleme → tedarikçi listede doğrulama →
 * teklif al/kabul → bordro no → admin günlük işlemler doğrulaması.
 *
 * UI testleri parallel=false koştuğundan statik saklama güvenlidir;
 * her senaryo başında {@link #reset()} çağrılır.
 */
public final class TzfScenarioContext {

    /** Excel'e yazılan tek bir fatura satırı. */
    public static final class TzfInvoice {
        public final int no;
        public final String invoiceNo;
        public final String amount;      // virgüllü ondalık, örn: 12500,00
        public final String invoiceDate; // gg/aa/yyyy
        public final String dueDate;     // gg/aa/yyyy
        public final String hashCode;    // 32 karakter büyük harf hex (E-Fatura)

        public TzfInvoice(int no, String invoiceNo, String amount,
                          String invoiceDate, String dueDate, String hashCode) {
            this.no = no;
            this.invoiceNo = invoiceNo;
            this.amount = amount;
            this.invoiceDate = invoiceDate;
            this.dueDate = dueDate;
            this.hashCode = hashCode;
        }
    }

    private static String excelPath;
    private static final List<TzfInvoice> invoices = new ArrayList<>();
    private static String offeredInvoiceNo;
    private static String bordroNo;

    private TzfScenarioContext() {
    }

    public static void reset() {
        excelPath = null;
        invoices.clear();
        offeredInvoiceNo = null;
        bordroNo = null;
    }

    public static String getExcelPath() {
        return excelPath;
    }

    public static void setExcelPath(String path) {
        excelPath = path;
    }

    public static List<TzfInvoice> getInvoices() {
        return Collections.unmodifiableList(invoices);
    }

    public static void addInvoice(TzfInvoice invoice) {
        invoices.add(invoice);
    }

    public static String getOfferedInvoiceNo() {
        return offeredInvoiceNo;
    }

    public static void setOfferedInvoiceNo(String invoiceNo) {
        offeredInvoiceNo = invoiceNo;
    }

    public static String getBordroNo() {
        return bordroNo;
    }

    public static void setBordroNo(String no) {
        bordroNo = no;
    }
}
