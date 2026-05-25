package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Alıcı (Buyer) — İhale Onay/Ret ekranı.
 *
 * İhale akışı:
 *  1. Tedarikçi fatura yükler
 *  2. Admin onaylar
 *  3. Alıcı ihaleyi görür → Onayla / Reddet
 *  4. Alıcı onaylarsa finansmana gider (bordro oluşur)
 *
 * Ekran: "İhaleler" veya "Bekleyen İhaleler" menüsü
 */
public class BuyerAuctionPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    private final By IHALELER_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'İhale') or contains(normalize-space(),'Ihale') " +
            " or contains(normalize-space(),'Auction')]");

    private final By BEKLEYEN_IHALELER_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'Bekleyen') and " +
            "(contains(normalize-space(),'İhale') or contains(normalize-space(),'Ihale'))]");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By AUCTION_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_ROWS    = By.cssSelector("vaadin-grid-cell-content");

    // ─── Satır Butonları ──────────────────────────────────────────────────────

    private final By ONAYLA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='ONAYLA'] | " +
            "//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-button[normalize-space()='Kabul Et']");

    private final By REDDET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='REDDET'] | " +
            "//vaadin-button[normalize-space()='Reddet'] | " +
            "//vaadin-button[normalize-space()='Ret']");

    private final By DETAY_BTN = By.xpath(
            "//vaadin-button[normalize-space()='DETAY'] | " +
            "//vaadin-button[normalize-space()='Detay'] | " +
            "//vaadin-button[normalize-space()='GÖZAT']");

    // ─── Reddet Dialog ────────────────────────────────────────────────────────

    private final By REJECT_REASON_INPUT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-area//textarea | " +
            "//vaadin-dialog-overlay//textarea | " +
            "//vaadin-dialog-overlay//vaadin-text-field//input");

    private final By CONFIRM_YES_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Gönder'] | " +
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

    public BuyerAuctionPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    public void navigateToPendingAuctions() {
        try {
            try {
                WebElement menu = waitForElementToBeClickable(BEKLEYEN_IHALELER_MENU);
                menu.click();
                log.info("'Bekleyen İhaleler' menüsüne XPath ile tıklandı.");
            } catch (Exception ex1) {
                try {
                    WebElement menu = waitForElementToBeClickable(IHALELER_MENU);
                    menu.click();
                    log.info("'İhaleler' menüsüne XPath ile tıklandı.");
                } catch (Exception ex2) {
                    log.info("XPath ile bulunamadı, JS nav deneniyor...");
                    boolean clicked = clickNavItemByText("ihale") || clickNavItemByText("auction");
                    if (!clicked) {
                        throw new RuntimeException("İhale menü öğesi JS ile de bulunamadı");
                    }
                }
            }
            waitForVaadinNavigation();
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(AUCTION_GRID));
            } catch (Exception gridEx) {
                log.warn("vaadin-grid bekleme timeout, devam ediliyor: {}", gridEx.getMessage());
            }
        } catch (Exception e) {
            log.error("İhale ekranı navigasyonu başarısız: {}", e.getMessage());
            throw new RuntimeException("Alıcı ihale ekranına gidilemedi", e);
        }
    }

    // ─── İhale İşlemleri ──────────────────────────────────────────────────────

    public void approveAuction(String auctionId) {
        try {
            clickActionButtonInRow(auctionId, "ONAYLA");
            confirmIfDialogAppears(null);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("İhale onaylanamadı [{}]: {}", auctionId, e.getMessage());
            throw new RuntimeException("İhale onaylama başarısız: " + auctionId, e);
        }
    }

    public void rejectAuction(String auctionId, String reason) {
        try {
            clickActionButtonInRow(auctionId, "REDDET");
            confirmIfDialogAppears(reason);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("İhale reddedilemedi [{}]: {}", auctionId, e.getMessage());
            throw new RuntimeException("İhale ret başarısız: " + auctionId, e);
        }
    }

    public void viewAuctionDetail(String auctionId) {
        try {
            clickActionButtonInRow(auctionId, "DETAY");
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("İhale detayı açılamadı [{}]: {}", auctionId, e.getMessage());
            throw new RuntimeException("İhale detay başarısız: " + auctionId, e);
        }
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    private void clickActionButtonInRow(String auctionId, String buttonLabel) {
        try {
            By rowBtn = By.xpath(
                    "//*[contains(text(),'" + auctionId + "')]" +
                    "/ancestor::vaadin-grid-row//vaadin-button[normalize-space()='" + buttonLabel + "'] | " +
                    "//*[contains(text(),'" + auctionId + "')]" +
                    "/ancestor::tr//vaadin-button[normalize-space()='" + buttonLabel + "']");
            WebElement btn = waitForElementToBeClickable(rowBtn);
            btn.click();
            log.info("[{}] butonuna tıklandı, ihale: {}", buttonLabel, auctionId);
            return;
        } catch (Exception ignored) {}

        List<WebElement> cells = driver.findElements(GRID_ROWS);
        boolean rowFound = false;
        for (WebElement cell : cells) {
            if (cell.getText() != null && cell.getText().contains(auctionId)) {
                rowFound = true;
                break;
            }
        }
        if (!rowFound) {
            throw new RuntimeException("İhale satırı bulunamadı: " + auctionId);
        }

        By fallbackBtn;
        switch (buttonLabel) {
            case "ONAYLA": fallbackBtn = ONAYLA_BTN; break;
            case "REDDET": fallbackBtn = REDDET_BTN; break;
            default:       fallbackBtn = DETAY_BTN;  break;
        }
        WebElement btn = waitForElementToBeClickable(fallbackBtn);
        btn.click();
        log.info("[{}] butonuna tıklandı (fallback).", buttonLabel);
    }

    private void confirmIfDialogAppears(String reason) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("vaadin-dialog-overlay")));

            if (reason != null && !reason.isEmpty()) {
                try {
                    WebElement reasonInput = driver.findElement(REJECT_REASON_INPUT);
                    reasonInput.clear();
                    reasonInput.sendKeys(reason);
                    log.info("Red nedeni girildi: {}", reason);
                } catch (Exception ex) {
                    log.warn("Red nedeni alanı bulunamadı, atlandı.");
                }
            }

            WebElement confirmBtn = waitForElementToBeClickable(CONFIRM_YES_BTN);
            confirmBtn.click();
            log.info("Dialog onaylandı.");
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public String getAuctionRowText(String auctionId) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(AUCTION_GRID));
            Thread.sleep(1000);

            List<WebElement> cells = driver.findElements(GRID_ROWS);
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && text.contains(auctionId)) {
                    log.info("İhale satırı bulundu: {}", text);
                    return text;
                }
            }
            WebElement row = driver.findElement(
                    By.xpath("//*[contains(text(),'" + auctionId + "')]"));
            return row.getText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("İhale bulunamadı: {}", auctionId);
            return null;
        }
    }

    public String getAuctionStatus(String auctionId) {
        String rowText = getAuctionRowText(auctionId);
        if (rowText == null) return null;

        String[] statuses = {"APPROVED", "PENDING", "REJECTED", "CANCELLED",
                             "Onaylandı", "Onay Bekliyor", "Reddedildi", "İptal"};
        for (String status : statuses) {
            if (rowText.contains(status)) return status;
        }
        return rowText;
    }

    public boolean hasPendingAuctions() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(AUCTION_GRID));
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
}
