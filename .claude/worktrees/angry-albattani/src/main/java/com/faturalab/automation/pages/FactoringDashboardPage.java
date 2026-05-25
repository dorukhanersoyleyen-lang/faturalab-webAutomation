package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Finansman (Factoring) — Teklif Talebi Yönetimi ekranı.
 *
 * Ekran yapısı (screenshot):
 *  Tabs: Günlük Teklif Talebi | İptal Talebi | Güncelleme | Erken Ödeme Programı
 *  Grid sütunları: Bordro No, Bordro Durumu, ABF Durumu, Teklif Durumu, Ticari İşletme, Alıcı, Finansal Kurum
 *  Satır butonları: GÖZAT | İPTAL | ONAYLA
 *
 *  Bordro = Tedarikçinin ihale teklifini kabul etmesi sonrası oluşan finansal belge.
 *  Örnek Bordro No: A2025_73387
 */
public class FactoringDashboardPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    private final By TEKLIF_YONETIMI_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'Teklif') and " +
            "(contains(normalize-space(),'Yönetim') or contains(normalize-space(),'Yonetim') " +
            " or contains(normalize-space(),'Talep'))]");

    // ─── Tablar ───────────────────────────────────────────────────────────────

    private final By TAB_GUNLUK_TEKLIF = By.xpath(
            "//vaadin-tab[contains(normalize-space(),'Günlük Teklif')] | " +
            "//vaadin-tab[contains(normalize-space(),'Gunluk Teklif')]");

    private final By TAB_IPTAL_TALEBI = By.xpath(
            "//vaadin-tab[contains(normalize-space(),'İptal Talebi')] | " +
            "//vaadin-tab[contains(normalize-space(),'Iptal Talebi')]");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By BORDRO_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_ROWS   = By.cssSelector("vaadin-grid-cell-content");

    // ─── Satır Butonları ──────────────────────────────────────────────────────

    private final By GOZAT_BTN = By.xpath(
            "//vaadin-button[normalize-space()='GÖZAT'] | " +
            "//vaadin-button[normalize-space()='Gözat'] | " +
            "//vaadin-button[normalize-space()='GOZAT']");

    private final By IPTAL_BTN = By.xpath(
            "//vaadin-button[normalize-space()='İPTAL'] | " +
            "//vaadin-button[normalize-space()='İptal'] | " +
            "//vaadin-button[normalize-space()='IPTAL']");

    private final By ONAYLA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='ONAYLA'] | " +
            "//vaadin-button[normalize-space()='Onayla']");

    // ─── Onay Dialog ──────────────────────────────────────────────────────────

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

    public FactoringDashboardPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    public void navigateToOfferManagement() {
        try {
            try {
                WebElement menu = waitForElementToBeClickable(TEKLIF_YONETIMI_MENU);
                menu.click();
                log.info("Teklif Talebi Yönetimi menüsüne XPath ile tıklandı.");
            } catch (Exception ex) {
                log.info("XPath ile bulunamadı, JS nav deneniyor...");
                boolean clicked = clickNavItemByText("teklif") || clickNavItemByText("faktoring") || clickNavItemByText("finans");
                if (!clicked) {
                    throw new RuntimeException("Teklif Yönetimi menü öğesi JS ile de bulunamadı");
                }
            }
            waitForVaadinNavigation();
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(BORDRO_GRID));
            } catch (Exception gridEx) {
                log.warn("vaadin-grid bekleme timeout, devam ediliyor: {}", gridEx.getMessage());
            }
        } catch (Exception e) {
            log.error("Teklif Yönetimi navigasyonu başarısız: {}", e.getMessage());
            throw new RuntimeException("Faktoring dashboard ekranına gidilemedi", e);
        }
    }

    public void switchToGunlukTeklifTab() {
        try {
            WebElement tab = waitForElementToBeClickable(TAB_GUNLUK_TEKLIF);
            tab.click();
            log.info("'Günlük Teklif Talebi' tabına geçildi.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Günlük Teklif tab'ı tıklanamadı (zaten aktif olabilir): {}", e.getMessage());
        }
    }

    public void switchToIptalTalebiTab() {
        try {
            WebElement tab = waitForElementToBeClickable(TAB_IPTAL_TALEBI);
            tab.click();
            log.info("'İptal Talebi' tabına geçildi.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("İptal Talebi tabı tıklanamadı: {}", e.getMessage());
            throw new RuntimeException("Tab geçişi başarısız", e);
        }
    }

    // ─── Bordro İşlemleri ─────────────────────────────────────────────────────

    public void approveOffer(String bordroNo) {
        try {
            clickActionButtonInRow(bordroNo, "ONAYLA");
            confirmIfDialogAppears();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Bordro onaylanamadı [{}]: {}", bordroNo, e.getMessage());
            throw new RuntimeException("Bordro onaylama başarısız: " + bordroNo, e);
        }
    }

    public void cancelOffer(String bordroNo) {
        try {
            clickActionButtonInRow(bordroNo, "İPTAL");
            confirmIfDialogAppears();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Bordro iptal edilemedi [{}]: {}", bordroNo, e.getMessage());
            throw new RuntimeException("Bordro iptal başarısız: " + bordroNo, e);
        }
    }

    public void viewOffer(String bordroNo) {
        try {
            clickActionButtonInRow(bordroNo, "GÖZAT");
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Bordro detayı açılamadı [{}]: {}", bordroNo, e.getMessage());
            throw new RuntimeException("GÖZAT başarısız: " + bordroNo, e);
        }
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    private void clickActionButtonInRow(String bordroNo, String buttonLabel) {
        try {
            By rowBtn = By.xpath(
                    "//*[contains(text(),'" + bordroNo + "')]" +
                    "/ancestor::vaadin-grid-row//vaadin-button[normalize-space()='" + buttonLabel + "'] | " +
                    "//*[contains(text(),'" + bordroNo + "')]" +
                    "/ancestor::tr//vaadin-button[normalize-space()='" + buttonLabel + "']");
            WebElement btn = waitForElementToBeClickable(rowBtn);
            btn.click();
            log.info("[{}] butonuna tıklandı, bordro: {}", buttonLabel, bordroNo);
            return;
        } catch (Exception ignored) {}

        try {
            List<WebElement> cells = driver.findElements(GRID_ROWS);
            boolean found = false;
            for (WebElement cell : cells) {
                if (cell.getText() != null && cell.getText().contains(bordroNo)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("Bordro bulunamadı: " + bordroNo);
            }

            By fallbackBtn;
            if ("ONAYLA".equals(buttonLabel)) {
                fallbackBtn = ONAYLA_BTN;
            } else if ("İPTAL".equals(buttonLabel)) {
                fallbackBtn = IPTAL_BTN;
            } else {
                fallbackBtn = GOZAT_BTN;
            }
            WebElement btn = waitForElementToBeClickable(fallbackBtn);
            btn.click();
            log.info("[{}] butonuna tıklandı (fallback).", buttonLabel);
        } catch (Exception e) {
            throw new RuntimeException("Satır butonu tıklanamadı [" + buttonLabel + "]: " + bordroNo, e);
        }
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

    public String getBordroRowText(String bordroNo) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(BORDRO_GRID));
            Thread.sleep(1000);

            List<WebElement> cells = driver.findElements(GRID_ROWS);
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && text.contains(bordroNo)) {
                    log.info("Bordro bulundu: {}", text);
                    return text;
                }
            }
            WebElement row = driver.findElement(
                    By.xpath("//*[contains(text(),'" + bordroNo + "')]"));
            return row.getText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("Bordro listesinde bulunamadı: {}", bordroNo);
            return null;
        }
    }

    public String getBordroStatus(String bordroNo) {
        String rowText = getBordroRowText(bordroNo);
        if (rowText == null) return null;

        String[] statuses = {"APPROVED", "PENDING_APPROVAL", "CANCELLED", "REJECTED",
                             "Onaylandı", "Onay Bekliyor", "İptal", "Reddedildi"};
        for (String status : statuses) {
            if (rowText.contains(status)) return status;
        }
        return rowText;
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

    public boolean isGridVisible() {
        return !driver.findElements(BORDRO_GRID).isEmpty();
    }

    public boolean hasBordroRows() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(BORDRO_GRID));
            List<WebElement> cells = driver.findElements(GRID_ROWS);
            return !cells.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
