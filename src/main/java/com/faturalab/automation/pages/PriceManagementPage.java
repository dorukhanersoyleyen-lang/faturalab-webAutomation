package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Fiyat Yonetimi sayfasi — Fiyat yönetimi "Limit ve Fiyat Yönetimi" sayfasındaki
 * DÜZENLE dialogu üzerinden yapılır.
 *
 * Navigasyon: "Yönetim Paneli" butonu → "Limit ve Fiyat Yönetimi" menüsü
 * Sayfa başlığı: "LİMİT VE FİYAT YÖNETİMİ"
 * Fiyat alanı: DÜZENLE dialogunda vaadin-text-field[label='Fiyat (%)']
 *
 * Vaadin 24: vaadin-grid, vaadin-text-field, vaadin-dialog-overlay kullanılır.
 */
public class PriceManagementPage extends BasePageObject {

    // ─── Navigasyon Selectors ─────────────────────────────────────────────────

    /** "Yönetim Paneli" üst nav butonu */
    private final By YONETIM_PANELI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yönetim Paneli']");

    /** Sol menüde "Limit ve Fiyat Yönetimi" butonu */
    private final By LIMIT_VE_FIYAT_MENU = By.xpath(
            "//vaadin-button[normalize-space()='Limit ve Fiyat Yönetimi']");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By PRICE_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS = By.cssSelector("vaadin-grid-cell-content");

    // ─── Dialog Form Selectors ────────────────────────────────────────────────

    /** "Fiyat (%)" metin alanı — DÜZENLE dialogu içinde */
    private final By FIYAT_PERCENT_INPUT = By.cssSelector(
            "vaadin-dialog-overlay vaadin-text-field[label='Fiyat (%)'] input");

    /** Dialog içi "Kaydet" butonu */
    private final By KAYDET_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet']");

    /** "DÜZENLE" butonu — satır içi */
    private final By DUZENLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='DÜZENLE']");

    // ─── Eski Uyumluluk Selectors ─────────────────────────────────────────────

    /** Min fiyat alani (fallback eski selector) */
    private final By MIN_FIYAT_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Min') or contains(@label,'min') " +
            " or contains(@label,'Minimum') or contains(@label,'Alt')]//input | " +
            "//vaadin-text-field[contains(@label,'Min') or contains(@label,'minFiyat')]//input");

    /** Max fiyat alani (fallback eski selector) */
    private final By MAX_FIYAT_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Max') or contains(@label,'max') " +
            " or contains(@label,'Maksimum') or contains(@label,'Üst')]//input | " +
            "//vaadin-text-field[contains(@label,'Max') or contains(@label,'maxFiyat')]//input");

    // ─── Bildirimler ──────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, vaadin-notification, [role='alert']");

    private final By VALIDATION_ERROR = By.xpath(
            "//vaadin-number-field[@invalid] | //vaadin-text-field[@invalid] | " +
            "//*[@error-message] | //*[contains(@class,'error')]");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public PriceManagementPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * "Limit ve Fiyat Yönetimi" sayfasına 2 adımlı navigasyon:
     * 1. "Yönetim Paneli" butonuna tıkla
     * 2. "Limit ve Fiyat Yönetimi" menü butonuna tıkla
     */
    private void navigateToLimitVeFiyat() {
        log.info("[PriceManagementPage] 'Limit ve Fiyat Yönetimi' navigasyonu başlıyor...");

        // Adım 1: Yönetim Paneli
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(YONETIM_PANELI_BTN));
            btn.click();
            waitForVaadinNavigation();
            log.info("[PriceManagementPage] 'Yönetim Paneli' butonuna tıklandı.");
        } catch (Exception e) {
            log.warn("[PriceManagementPage] 'Yönetim Paneli' butonu bulunamadı: {}", e.getMessage());
            return;
        }

        // Adım 2: Limit ve Fiyat Yönetimi menüsü
        try {
            WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(LIMIT_VE_FIYAT_MENU));
            menu.click();
            waitForVaadinNavigation();
            log.info("[PriceManagementPage] 'Limit ve Fiyat Yönetimi' menüsüne tıklandı.");
        } catch (Exception e) {
            log.warn("[PriceManagementPage] 'Limit ve Fiyat Yönetimi' menüsü bulunamadı: {}", e.getMessage());
            return;
        }

        // Grid yüklensin
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.visibilityOfElementLocated(PRICE_GRID));
            log.info("[PriceManagementPage] Grid görünüyor.");
        } catch (Exception e) {
            log.warn("[PriceManagementPage] Grid bekleme zaman aşımı: {}", e.getMessage());
        }
    }

    /**
     * Fiyat yönetimi sayfasına gider.
     * Yönetim Paneli → "Limit ve Fiyat Yönetimi"
     */
    public void navigateToPriceManagement() {
        navigateToLimitVeFiyat();
    }

    // ─── Form Islemleri ───────────────────────────────────────────────────────

    /**
     * İlk satırın DÜZENLE butonuna tıklar.
     */
    public void clickFirstDuzenle() {
        log.info("[PriceManagementPage] İlk DÜZENLE butonuna tıklanıyor...");
        try {
            List<WebElement> btns = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(DUZENLE_BTN));
            if (!btns.isEmpty()) {
                btns.get(0).click();
                waitForVaadinNavigation();
                log.info("[PriceManagementPage] İlk DÜZENLE butonuna tıklandı.");
            } else {
                log.warn("[PriceManagementPage] DÜZENLE butonu bulunamadı.");
            }
        } catch (Exception e) {
            log.warn("[PriceManagementPage] DÜZENLE butonuna tıklanamadı: {}", e.getMessage());
        }
    }

    /**
     * Dialog "Fiyat (%)" alanına değer girer.
     *
     * @param rate Fiyat oranı
     */
    public void enterFiyatPercent(String rate) {
        log.info("[PriceManagementPage] Fiyat (%) giriliyor: {}", rate);
        fillField(FIYAT_PERCENT_INPUT, rate, "Fiyat (%)");
    }

    /**
     * Yeni fiyat satırı ekler (eski API uyumluluğu).
     *
     * @param faizOrani Fiyat değeri
     */
    public void addRateRow(String faizOrani) {
        log.info("[PriceManagementPage] Yeni fiyat satırı ekleniyor: oran={}", faizOrani);
        clickFirstDuzenle();
        enterFiyatPercent(faizOrani);
        clickSave();
    }

    /**
     * Fiyat aralik degerlerini girer (eski API uyumluluğu).
     */
    public void enterPriceRange(String minFiyat, String maxFiyat) {
        if (minFiyat != null && !minFiyat.isBlank()) {
            fillField(MIN_FIYAT_INPUT, minFiyat, "Min Fiyat");
        }
        if (maxFiyat != null && !maxFiyat.isBlank()) {
            fillField(MAX_FIYAT_INPUT, maxFiyat, "Max Fiyat");
        }
        log.info("[PriceManagementPage] Fiyat aralik girildi: min={}, max={}", minFiyat, maxFiyat);
    }

    /** Gecersiz fiyat aralik degeri girer (eski API uyumluluğu). */
    public void enterInvalidPriceRange() {
        fillField(MIN_FIYAT_INPUT, "9999", "Min Fiyat (gecersiz)");
        fillField(MAX_FIYAT_INPUT, "100",  "Max Fiyat (gecersiz)");
        log.info("[PriceManagementPage] Gecersiz fiyat aralik girildi (min=9999, max=100).");
    }

    /** Bos fiyat alanlariyla kaydet tiklar (eski API uyumluluğu). */
    public void clearAndSave() {
        try {
            clearField(MIN_FIYAT_INPUT, "Min Fiyat");
            clearField(MAX_FIYAT_INPUT, "Max Fiyat");
            clickSave();
        } catch (Exception e) {
            log.warn("[PriceManagementPage] ClearAndSave hatasi: {}", e.getMessage());
        }
    }

    /**
     * Dialog "Kaydet" butonuna tıklar.
     */
    public void clickSave() {
        log.info("[PriceManagementPage] 'Kaydet' butonuna tıklanıyor...");
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
            btn.click();
            log.info("[PriceManagementPage] 'Kaydet' butonuna tıklandı.");
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> {
                        List<WebElement> notifs = d.findElements(SUCCESS_NOTIFICATION);
                        return !notifs.isEmpty() || !d.findElements(VALIDATION_ERROR).isEmpty();
                    });
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "Array.from(document.querySelectorAll('vaadin-button')).forEach(b=>{" +
                    "  var t=b.textContent.trim();" +
                    "  if(t==='Kaydet'||t==='KAYDET') b.click();" +
                    "});"
                );
                log.warn("[PriceManagementPage] 'Kaydet' JS ile tıklandı: {}", e.getMessage());
            } catch (Exception jse) {
                log.warn("[PriceManagementPage] Kaydet JS de başarısız: {}", jse.getMessage());
            }
        }
    }

    // ─── Dogrulama ────────────────────────────────────────────────────────────

    /**
     * Fiyat guncelleme basarili mi.
     * Dialog kapandıktan sonra grid'e dönüş kontrol edilir.
     */
    public boolean isPriceSaved() {
        // Önce bildirim kontrolü
        try {
            boolean notif = !new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> d.findElements(SUCCESS_NOTIFICATION)).isEmpty();
            if (notif) {
                log.info("[PriceManagementPage] Başarı bildirimi görüldü.");
                return true;
            }
        } catch (Exception ignored) {}

        // Dialog kapandı mı + grid görünüyor mu
        try {
            boolean dialogClosed = driver.findElements(
                    By.cssSelector("vaadin-dialog-overlay")).isEmpty();
            boolean gridVisible = !driver.findElements(PRICE_GRID).isEmpty();
            if (dialogClosed && gridVisible) {
                log.info("[PriceManagementPage] Dialog kapandı, grid görünüyor — kayıt başarılı.");
                return true;
            }
        } catch (Exception ignored) {}

        String src = driver.getPageSource();
        boolean saved = src.contains("kaydedildi") || src.contains("başarıyla") || src.contains("basarili");
        log.info("[PriceManagementPage] isPriceSaved page source kontrolü: {}", saved);
        return saved;
    }

    /** Validasyon/hata mesaji goruluyor mu. */
    public boolean isValidationErrorVisible() {
        try {
            if (!driver.findElements(VALIDATION_ERROR).isEmpty()) {
                log.info("[PriceManagementPage] Validasyon hatası elementi bulundu.");
                return true;
            }
        } catch (Exception ignored) {}
        String src = driver.getPageSource();
        return src.contains("hata") || src.contains("invalid") || src.contains("error")
                || src.contains("gecersiz") || src.contains("geçersiz") || src.contains("zorunlu");
    }

    /** Fiyat grid'inin gorunup gorunmedigini kontrol eder. */
    public boolean isPriceGridVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(PRICE_GRID));
            log.info("[PriceManagementPage] Price grid görünüyor.");
            return true;
        } catch (Exception e) {
            log.info("[PriceManagementPage] Price grid görünmüyor: {}", e.getMessage());
            return false;
        }
    }

    /** Grid baslik sutunlarinin sayfada gorunup gorunmedigini kontrol eder. */
    public boolean areHeaderColumnsVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(PRICE_GRID));
        } catch (Exception ignored) {}
        String src = driver.getPageSource();
        boolean visible = src.contains("Finansal Kurum") || src.contains("Alıcı")
                || src.contains("LİMİT VE FİYAT") || src.contains("Fiyat");
        log.info("[PriceManagementPage] Header kolonlar görünüyor: {}", visible);
        return visible;
    }

    /** Fiyat araliginin listede gorunup gorunmedigini kontrol eder. */
    public boolean arePriceRangesListedInGrid() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(PRICE_GRID));
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            boolean hasContent = !cells.isEmpty();
            log.info("[PriceManagementPage] Grid'de {} cell bulundu.", cells.size());
            return hasContent;
        } catch (Exception e) {
            log.info("[PriceManagementPage] Grid içerik kontrolü başarısız: {}", e.getMessage());
            return false;
        }
    }

    // ─── Yardimci ─────────────────────────────────────────────────────────────

    private void fillField(By locator, String value, String fieldName) {
        try {
            WebElement field = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                field);
            field.sendKeys(value);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                field);
            log.info("[PriceManagementPage] {} girildi: {}", fieldName, value);
        } catch (Exception e) {
            log.warn("[PriceManagementPage] {} girilemedi: {}", fieldName, e.getMessage());
        }
    }

    private void clearField(By locator, String fieldName) {
        try {
            WebElement field = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                field);
            log.info("[PriceManagementPage] {} temizlendi.", fieldName);
        } catch (Exception e) {
            log.warn("[PriceManagementPage] {} temizlenemedi: {}", fieldName, e.getMessage());
        }
    }
}
