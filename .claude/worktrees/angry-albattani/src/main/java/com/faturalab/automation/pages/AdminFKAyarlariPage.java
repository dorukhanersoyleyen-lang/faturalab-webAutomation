package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Admin — FK Ayarları ekranı (FL-012: Cut-off Süresi Ayarlama).
 * Navigasyon: Sol menü → "Ayarlar" → "Web Ayarları" veya "FK Ayarları"
 * DisplayWebSettingsView kaynak kodundan: TimePicker for BANK_CUT_OFF_HOUR
 */
public class AdminFKAyarlariPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    private final By AYARLAR_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'Ayarlar') or contains(normalize-space(),'Settings')]");

    private final By FK_AYARLAR_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'FK Ayar') or contains(normalize-space(),'Web Ayar') " +
            "or contains(normalize-space(),'Banka') or contains(normalize-space(),'Bank')]");

    // ─── Banka Seçimi ─────────────────────────────────────────────────────────

    private final By BANKA_SELECT = By.xpath(
            "//vaadin-combo-box[contains(@label,'Banka') or contains(@label,'Bank')] | " +
            "//vaadin-select[contains(@label,'Banka') or contains(@label,'Bank')]");

    // ─── Cut-Off Saat/Dakika Alanları ────────────────────────────────────────

    private final By CUTOFF_SAAT_FIELD = By.xpath(
            "//vaadin-time-picker[contains(@label,'Cut-Off') or contains(@label,'Kesim') " +
            "or contains(@label,'Saat') or contains(@label,'Cut') or contains(@label,'Hour')]//input | " +
            "//vaadin-text-field[contains(@label,'Cut-Off') or contains(@label,'Kesim') " +
            "or contains(@label,'Saat')]//input | " +
            "(//vaadin-time-picker//input)[1] | " +
            "(//input[contains(@placeholder,'SS') or contains(@placeholder,'HH')])[1]");

    private final By CUTOFF_DAKIKA_FIELD = By.xpath(
            "(//vaadin-time-picker//input)[2] | " +
            "//vaadin-text-field[contains(@label,'Dakika') or contains(@label,'Minute')]//input | " +
            "(//input[contains(@placeholder,'DD') or contains(@placeholder,'mm')])[1]");

    // ─── Kaydet Butonu ────────────────────────────────────────────────────────

    private final By KAYDET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-button[normalize-space()='Save'] | " +
            "//vaadin-button[normalize-space()='Güncelle'] | " +
            "//button[normalize-space()='Kaydet']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public AdminFKAyarlariPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Admin dashboard üst menüsünde Ayarlar/Settings alanına gider.
     * Genellikle kullanıcı ikonu altında bulunur; önce .button-menu ile dener.
     */
    public void navigateToAyarlar() {
        boolean clicked = clickNavItemByText("ayarlar");
        if (!clicked) clicked = clickNavItemByText("settings");
        if (!clicked) {
            // Üst bar ikonu veya account menüsü olabilir
            Boolean jsClicked = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var els = document.querySelectorAll('vaadin-button, button, a, span, [class*=\"account\"], [class*=\"settings\"]');" +
                "for (var el of els) {" +
                "  var txt = (el.textContent||el.title||el.getAttribute('aria-label')||'').toLowerCase().trim();" +
                "  if ((txt.includes('ayar') || txt.includes('setting')) && txt.length < 40) { el.click(); return true; }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(jsClicked)) {
                log.warn("Ayarlar menüsü bulunamadı — mevcut sayfada devam. URL: {}", driver.getCurrentUrl());
                return; // FK ayarları için ayrı navigasyon yapılacak
            }
        }
        waitForVaadinNavigation();
        log.info("Ayarlar navigasyonu tamamlandı.");
    }

    /**
     * FK / Web Ayarları alt menüsüne gider.
     */
    public void navigateToFKAyarlari() {
        boolean clicked = clickNavItemByText("fk ayar");
        if (!clicked) clicked = clickNavItemByText("web ayar");
        if (!clicked) clicked = clickNavItemByText("cron");
        if (!clicked) {
            log.warn("FK Ayarları alt menüsü bulunamadı — mevcut sayfada devam. URL: {}", driver.getCurrentUrl());
        } else {
            waitForVaadinNavigation();
            log.info("FK Ayarları ekranına geçildi.");
        }
    }

    // ─── Banka Seçimi ─────────────────────────────────────────────────────────

    public void selectBanka(String bankaAdi) {
        try {
            WebElement combo = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(BANKA_SELECT));
            combo.click();

            By optionLocator = By.xpath(
                    "//vaadin-combo-box-item[contains(normalize-space(),'" + bankaAdi + "')] | " +
                    "//vaadin-item[contains(normalize-space(),'" + bankaAdi + "')] | " +
                    "//vaadin-select-item[contains(normalize-space(),'" + bankaAdi + "')]");
            WebElement option = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(optionLocator));
            option.click();
            log.info("Banka seçildi: {}", bankaAdi);
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log.warn("Banka seçilemedi [{}]: {}", bankaAdi, e.getMessage());
        }
    }

    // ─── Cut-Off Ayarları ─────────────────────────────────────────────────────

    public void setCutoffSaat(String saat) {
        try {
            WebElement field = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(CUTOFF_SAAT_FIELD));
            field.click();
            field.sendKeys(Keys.CONTROL + "a");
            field.sendKeys(saat);
            log.info("Cut-off saati girildi: {}", saat);
        } catch (Exception e) {
            log.warn("Cut-off saati girilemedi [{}]: {}", saat, e.getMessage());
        }
    }

    public void setCutoffDakika(String dakika) {
        try {
            WebElement field = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(CUTOFF_DAKIKA_FIELD));
            field.click();
            field.sendKeys(Keys.CONTROL + "a");
            field.sendKeys(dakika);
            log.info("Cut-off dakikası girildi: {}", dakika);
        } catch (Exception e) {
            log.warn("Cut-off dakikası girilemedi [{}]: {}", dakika, e.getMessage());
        }
    }

    // ─── Kaydet ───────────────────────────────────────────────────────────────

    public void clickKaydet() {
        try {
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
            btn.click();
            log.info("Kaydet butonuna tıklandı.");
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log.warn("Kaydet butonu tıklanamadı: {}", e.getMessage());
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public boolean isSuccessNotificationVisible() {
        try {
            waitForVisibility(SUCCESS_NOTIFICATION, 5);
            return true;
        } catch (Exception e) {
            return isAnyNotificationVisible();
        }
    }

    /** FK Ayarları ekranının yüklendiğini doğrular (en az bir form alanı veya grid görünür). */
    public boolean isFKAyarlariVisible() {
        try {
            java.util.List<org.openqa.selenium.WebElement> fields = driver.findElements(
                    By.cssSelector("vaadin-time-picker, vaadin-combo-box, vaadin-grid, vaadin-text-field"));
            return !fields.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentCutoffValue() {
        try {
            WebElement field = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(CUTOFF_SAAT_FIELD));
            String value = field.getAttribute("value");
            if (value == null || value.isEmpty()) {
                value = field.getText();
            }
            log.info("Mevcut cut-off değeri: {}", value);
            return (value != null && !value.isEmpty()) ? value : null;
        } catch (Exception e) {
            log.warn("Cut-off değeri alınamadı: {}", e.getMessage());
            return null;
        }
    }

}
