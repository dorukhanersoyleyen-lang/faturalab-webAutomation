package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Alıcı (Buyer) — Fatura Onay ekranı.
 *
 * Navigasyon: Buyer sidebar → "Onay Bekleyenler" (class: menu-button)
 * Sayfa başlığı: "ONAY BEKLEYEN FATURALAR"
 *
 * Akış:
 *  1. Sidebar'dan "Onay Bekleyenler"e git
 *  2. Grid'deki fatura satırında "GÖZAT" butonuna tıkla
 *  3. Açılan "Fatura Bilgileri" dialog'u üzerinden işlemler yap
 */
public class BuyerInvoicePage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /** Buyer sidebar "Onay Bekleyenler" butonu */
    private final By ONAY_BEKLEYENLER_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='Onay Bekleyenler']");

    /** Buyer sidebar "Fatura Yükleme Talepleri" (home view) */
    private final By FATURA_YUKLEME_TALEPLERI_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and contains(normalize-space(),'Fatura Yükleme Talepleri')]");

    // ─── Sayfa Başlığı ────────────────────────────────────────────────────────

    private final By PAGE_TITLE = By.xpath(
            "//*[contains(normalize-space(),'ONAY BEKLEYEN FATURALAR')]");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By INVOICE_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS   = By.cssSelector("vaadin-grid-cell-content");

    // ─── Satır Butonu ─────────────────────────────────────────────────────────

    private final By GOZAT_BTN = By.xpath(
            "//vaadin-button[normalize-space()='GÖZAT']");

    // ─── Fatura Bilgileri Dialog ──────────────────────────────────────────────

    private final By DIALOG_OVERLAY = By.cssSelector("vaadin-dialog-overlay");

    private final By DIALOG_TITLE = By.xpath(
            "//vaadin-dialog-overlay//*[contains(normalize-space(),'Fatura Bilgileri')]");

    private final By DIALOG_SIL_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Sil']");

    private final By DIALOG_KAPAT_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kapat']");

    // ─── Onay Dialog ──────────────────────────────────────────────────────────

    private final By CONFIRM_YES_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector("vaadin-notification-container");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public BuyerInvoicePage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Buyer sidebar'daki "Onay Bekleyenler" butonuna tıklayarak sayfaya gider.
     */
    public void navigateToOnayBekleyenler() {
        try {
            WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(ONAY_BEKLEYENLER_MENU));
            menu.click();
            log.info("'Onay Bekleyenler' menüsüne tıklandı.");
            waitForVaadinNavigation();
            try {
                new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.visibilityOfElementLocated(INVOICE_GRID));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.warn("Onay Bekleyenler navigasyonu başarısız: {}", e.getMessage());
        }
    }

    /** @deprecated {@link #navigateToOnayBekleyenler()} kullanın */
    public void navigateToPaperInvoiceUpload() {
        navigateToOnayBekleyenler();
    }

    // ─── GÖZAT ────────────────────────────────────────────────────────────────

    /**
     * Grid'deki belirtilen fatura no/metni içeren satırın GÖZAT butonuna tıklar.
     */
    public void clickGozatForInvoice(String invoiceIdentifier) {
        try {
            By rowBtn = By.xpath(
                    "//*[contains(normalize-space(),'" + invoiceIdentifier + "')]" +
                    "/ancestor::*[contains(@part,'row') or self::tr]" +
                    "//vaadin-button[normalize-space()='GÖZAT']");
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.elementToBeClickable(rowBtn));
                btn.click();
                log.info("GÖZAT tıklandı (satır): {}", invoiceIdentifier);
            } catch (Exception ex) {
                // Fallback: ilk GÖZAT butonu
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.elementToBeClickable(GOZAT_BTN));
                btn.click();
                log.info("GÖZAT tıklandı (genel).");
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("GÖZAT tıklanamadı [{}]: {}", invoiceIdentifier, e.getMessage());
        }
    }

    /**
     * Grid'deki ilk görünür GÖZAT butonuna tıklar.
     */
    public void clickFirstGozat() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(GOZAT_BTN));
            btn.click();
            log.info("İlk GÖZAT butonuna tıklandı.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("İlk GÖZAT tıklanamadı: {}", e.getMessage());
        }
    }

    // ─── Dialog İşlemleri ─────────────────────────────────────────────────────

    public boolean isFaturaBilgileriDialogOpen() {
        try {
            Thread.sleep(500);
            List<WebElement> dialogs = driver.findElements(DIALOG_OVERLAY);
            return dialogs.stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    public void clickDialogKapat() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(DIALOG_KAPAT_BTN));
            btn.click();
            log.info("Dialog 'Kapat' tıklandı.");
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Kapat butonu tıklanamadı: {}", e.getMessage());
        }
    }

    public void clickDialogSil() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(DIALOG_SIL_BTN));
            btn.click();
            log.info("Dialog 'Sil' tıklandı.");
            confirmIfDialogAppears();
        } catch (Exception e) {
            log.warn("Sil butonu tıklanamadı: {}", e.getMessage());
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public boolean isOnayBekleyenlerPageLoaded() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(PAGE_TITLE))
                    .isDisplayed();
        } catch (Exception e) {
            // Grid varlığını kontrol et
            try {
                return new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(INVOICE_GRID))
                        .isDisplayed();
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public boolean hasInvoiceInGrid(String invoiceNo) {
        try {
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            for (WebElement cell : cells) {
                String txt = cell.getText();
                if (txt != null && txt.contains(invoiceNo)) return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isUploadSuccessful() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_NOTIFICATION))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Legacy compat ────────────────────────────────────────────────────────

    /** @deprecated Buyer'ın fatura yükleme akışı yoktur; uploadImageFile geçersizdir. */
    public void uploadImageFile() {
        log.warn("uploadImageFile() buyer akışında desteklenmez.");
    }

    public void uploadImageFile(String filePath) {
        log.warn("uploadImageFile() buyer akışında desteklenmez.");
    }

    public void fillInvoiceForm(String supplierTaxNo, String amount, int tenorDays) {
        log.warn("fillInvoiceForm() buyer akışında desteklenmez.");
    }

    public void clickYukle() {
        log.warn("clickYukle() buyer akışında desteklenmez.");
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    private void confirmIfDialogAppears() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(CONFIRM_YES_BTN));
            btn.click();
            log.info("Onay dialogu onaylandı.");
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }
}
