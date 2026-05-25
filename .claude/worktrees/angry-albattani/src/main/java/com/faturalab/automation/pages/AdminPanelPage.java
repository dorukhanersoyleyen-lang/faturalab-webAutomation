package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Admin — Fatura onay/ret ekranı.
 *
 * Navigasyon: Sol menü → "Fatura Yönetimi" veya "Faturalar"
 * İşlemler: Arama, ONAYLA, REDDET, Durum doğrulama
 */
public class AdminPanelPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    private final By FATURA_YONETIMI_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'Fatura') and " +
            "(contains(normalize-space(),'Yönetim') or contains(normalize-space(),'Yonetim') " +
            " or contains(normalize-space(),'Onay') or contains(normalize-space(),'Liste'))]");

    // ─── Arama ────────────────────────────────────────────────────────────────

    private final By SEARCH_INPUT = By.xpath(
            "//vaadin-text-field[contains(@placeholder,'ara') or contains(@placeholder,'Ara') " +
            "or contains(@label,'Fatura') or contains(@placeholder,'fatura')]//input | " +
            "//input[contains(@placeholder,'ara') or contains(@placeholder,'Ara')]");

    private final By SEARCH_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Ara'] | " +
            "//vaadin-button[normalize-space()='Filtrele'] | " +
            "//button[normalize-space()='Ara']");

    // ─── Grid / Tablo ──────────────────────────────────────────────────────────

    private final By INVOICE_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_ROWS    = By.cssSelector("vaadin-grid-cell-content");

    // ─── Aksiyon Butonları ────────────────────────────────────────────────────

    private final By ONAYLA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='ONAYLA'] | " +
            "//vaadin-button[normalize-space()='Onayla'] | " +
            "//button[normalize-space()='ONAYLA']");

    private final By REDDET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='REDDET'] | " +
            "//vaadin-button[normalize-space()='Reddet'] | " +
            "//button[normalize-space()='REDDET']");

    private final By CONFIRM_YES_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam']");

    private final By CONFIRM_NO_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Hayır'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='İptal']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final By ERROR_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-error");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public AdminPanelPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    public void navigateToInvoiceManagement() {
        try {
            WebElement menu = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(FATURA_YONETIMI_MENU));
            menu.click();
            log.info("Fatura Yönetimi menüsüne XPath ile tıklandı.");
            waitForVaadinNavigation();
            return;
        } catch (Exception ignored) {}

        for (String kw : new String[]{"fatura", "invoice", "onay", "yonetim", "yönetim"}) {
            if (clickNavItemByText(kw)) {
                log.info("Fatura Yönetimi menüsüne JS nav ile tıklandı (kw='{}').", kw);
                waitForVaadinNavigation();
                if (!driver.findElements(INVOICE_GRID).isEmpty()) return;
            }
        }

        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var kws = ['fatura', 'invoice', 'onay'];" +
                "var els = document.querySelectorAll('a[href], vaadin-side-nav-item, [role=\"menuitem\"]');" +
                "for (var el of els) {" +
                "  var txt = (el.textContent || '').toLowerCase().trim();" +
                "  for (var kw of kws) { if (txt.includes(kw) && txt.length < 40) { el.click(); return true; } }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(clicked)) {
                log.info("Fatura Yönetimi nav (step3) tıklandı.");
                waitForVaadinNavigation();
                if (!driver.findElements(INVOICE_GRID).isEmpty()) return;
            }
        } catch (Exception ignored) {}

        log.warn("Fatura Yönetimi navigasyonu başarısız — mevcut sayfada devam ediliyor. URL: {}",
                driver.getCurrentUrl());
    }

    // ─── Arama ────────────────────────────────────────────────────────────────

    public void searchInvoice(String invoiceNo) {
        try {
            WebElement input = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
            input.clear();
            input.sendKeys(invoiceNo);
            log.info("Fatura arandı: {}", invoiceNo);

            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(SEARCH_BTN));
                btn.click();
            } catch (Exception ex) {
                input.sendKeys(Keys.ENTER);
            }
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Arama başarısız: {}", e.getMessage());
            throw new RuntimeException("Fatura arama başarısız: " + invoiceNo, e);
        }
    }

    // ─── Onaylama / Reddetme ──────────────────────────────────────────────────

    public void approveInvoice(String invoiceNo) {
        try {
            searchInvoice(invoiceNo);
            clickActionButtonInRow(invoiceNo, ONAYLA_BTN, "ONAYLA");
            confirmIfDialogAppears();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Fatura onaylanamadı [{}]: {}", invoiceNo, e.getMessage());
            throw new RuntimeException("Fatura onaylama başarısız: " + invoiceNo, e);
        }
    }

    public void rejectInvoice(String invoiceNo) {
        try {
            searchInvoice(invoiceNo);
            clickActionButtonInRow(invoiceNo, REDDET_BTN, "REDDET");
            confirmIfDialogAppears();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Fatura reddedilemedi [{}]: {}", invoiceNo, e.getMessage());
            throw new RuntimeException("Fatura ret başarısız: " + invoiceNo, e);
        }
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    private void clickActionButtonInRow(String invoiceNo, By buttonLocator, String buttonLabel) {
        try {
            By rowActionBtn = By.xpath(
                    "//*[contains(text(),'" + invoiceNo + "')]" +
                    "/ancestor::vaadin-grid-row//vaadin-button[normalize-space()='" + buttonLabel + "'] | " +
                    "//*[contains(text(),'" + invoiceNo + "')]" +
                    "/ancestor::tr//vaadin-button[normalize-space()='" + buttonLabel + "']");
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(rowActionBtn));
            btn.click();
            log.info("[{}] butonuna tıklandı, satır: {}", buttonLabel, invoiceNo);
            return;
        } catch (Exception ignored) {}

        WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(buttonLocator));
        btn.click();
        log.info("[{}] butonuna tıklandı (genel).", buttonLabel);
    }

    private void confirmIfDialogAppears() {
        try {
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(CONFIRM_YES_BTN));
            btn.click();
            log.info("Onay dialog'u onaylandı.");
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public String getInvoiceStatus(String invoiceNo) {
        try {
            searchInvoice(invoiceNo);
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(INVOICE_GRID));
            Thread.sleep(1000);

            List<WebElement> cells = driver.findElements(GRID_ROWS);
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && text.contains(invoiceNo)) {
                    log.info("Fatura satırı bulundu: {}", text);
                    return text;
                }
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("Durum alınamadı [{}]: {}", invoiceNo, e.getMessage());
            return null;
        }
    }

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorNotificationVisible() {
        try {
            return waitForVisibility(ERROR_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasPendingInvoices() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(INVOICE_GRID));
            List<WebElement> cells = driver.findElements(GRID_ROWS);
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && (text.contains("PENDING") || text.contains("Onay Bekliyor"))) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
