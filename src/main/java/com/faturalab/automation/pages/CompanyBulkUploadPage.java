package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Tedarikçi — Excel / CSV / Manuel fatura yükleme sekmeleri (UAT FL-014–017).
 */
public class CompanyBulkUploadPage extends BasePageObject {

    private final CompanyInvoicePage invoice;

    public CompanyBulkUploadPage(WebDriver driver) {
        super(driver);
        this.invoice = new CompanyInvoicePage(driver);
    }

    public void navigateToFaturaYukle() {
        invoice.navigateToInvoiceList();
    }

    public boolean clickFaturaYukleButonu() {
        invoice.openUploadDialog();
        return isDialogOpen();
    }

    public boolean selectUploadMethod(String method) {
        String m = method == null ? "" : method.toLowerCase();
        String needle = m.contains("excel") ? "excel" : m.contains("csv") ? "csv" : "manuel";
        try {
            Boolean ok = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return false;" +
                    "var kw = arguments[0];" +
                    "var els = overlay.querySelectorAll('vaadin-tab, [role=\"tab\"], vaadin-button, button');" +
                    "for (var el of els) {" +
                    "  var txt = (el.textContent || '').toLowerCase().replace(/\\s+/g,' ');" +
                    "  if (txt.includes(kw)) { el.click(); return true; }" +
                    "}" +
                    "return false;",
                    needle);
            waitForVaadinNavigation();
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            log.warn("Sekme seçimi: {}", e.getMessage());
            return false;
        }
    }

    public boolean clickTemplateIndir() {
        try {
            Boolean ok = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "var root = overlay || document;" +
                    "var btns = root.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase();" +
                    "  if (t.includes('şablon') || t.includes('sablon') || t.includes('indir') || t.includes('taslak')) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;");
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean clickKaydet() {
        invoice.clickSave();
        return true;
    }

    /**
     * Açık yükleme diyalogundaki {@code vaadin-upload} / file input'a dosya yolu gönderir (native dosya penceresi açmaz).
     */
    public void uploadFileInDialog(String absoluteFilePath) {
        invoice.uploadFile(absoluteFilePath);
    }

    public boolean isDialogOpen() {
        return invoice.isUploadDialogOpen();
    }

    public boolean isSuccessNotificationVisible() {
        return invoice.isSuccessNotificationVisible();
    }

    public boolean isUploadResultVisible() {
        try {
            return driver.findElements(By.cssSelector("vaadin-grid")).size() > 0
                    || invoice.isSuccessNotificationVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean selectAlici(String keyword) {
        try {
            WebElement combo = driver.findElement(By.cssSelector(
                    "vaadin-dialog-overlay vaadin-combo-box, vaadin-dialog-overlay vaadin-select"));
            combo.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            ((JavascriptExecutor) driver).executeScript(
                    "var items = document.querySelectorAll('vaadin-item, [role=\"option\"]');" +
                    "for (var it of items) { it.click(); return true; }" +
                    "return false;");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean enterTutar(String tutar) {
        try {
            WebElement input = driver.findElement(By.xpath(
                    "//vaadin-dialog-overlay//vaadin-integer-field//input | " +
                            "//vaadin-dialog-overlay//vaadin-number-field//input | " +
                            "//vaadin-dialog-overlay//vaadin-text-field[contains(@label,'Tutar')]//input"));
            input.clear();
            input.sendKeys(tutar);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean enterVadeTarihi(String tarih) {
        try {
            WebElement input = driver.findElement(By.cssSelector(
                    "vaadin-dialog-overlay vaadin-date-picker input, vaadin-dialog-overlay input[type='date']"));
            input.clear();
            input.sendKeys(tarih);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
