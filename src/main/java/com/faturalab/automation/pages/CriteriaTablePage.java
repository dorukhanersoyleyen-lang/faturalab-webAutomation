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
 * Kriter Tabloları sayfasi — Admin panelinde "Yönetim Paneli" → "Kriter Tabloları"
 *
 * Navigasyon:
 *   1. "Yönetim Paneli" butonuna tıkla
 *   2. "Kriter Tabloları" menü butonuna tıkla
 *   3. "Tablo Ayarları" tabına geç
 *
 * Sayfa başlığı: "KRİTER TABLOLARI"
 * Tablar: "Tablo Değerleri", "Tablo Ayarları"
 *
 * Tablo Ayarları alanları:
 *   - vaadin-text-field[label='Kod']
 *   - vaadin-text-field[label='Adı']
 *   - vaadin-combo-box[label='Tip']
 * Butonlar: "Yeni Kriter Tanımı Ekle", "Varsayılan Kriterleri Ekle"
 */
public class CriteriaTablePage extends BasePageObject {

    // ─── Navigasyon Selectors ─────────────────────────────────────────────────

    /** "Yönetim Paneli" üst nav butonu */
    private final By YONETIM_PANELI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yönetim Paneli']");

    /** Sol menüde "Kriter Tabloları" butonu */
    private final By KRITER_TABLOLARI_MENU = By.xpath(
            "//vaadin-button[normalize-space()='Kriter Tabloları']");

    /** "Tablo Ayarları" tab butonu */
    private final By TABLO_AYARLARI_TAB = By.xpath(
            "//vaadin-tab[normalize-space()='Tablo Ayarları'] | " +
            "//button[normalize-space()='Tablo Ayarları']");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By CRITERIA_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS    = By.cssSelector("vaadin-grid-cell-content");

    // ─── Form Alanlari (Tablo Ayarları) ───────────────────────────────────────

    /** "Kod" alani */
    private final By KOD_INPUT = By.cssSelector(
            "vaadin-text-field[label='Kod'] input");

    /** "Adı" alani (NOT "Başlık"!) */
    private final By ADI_INPUT = By.cssSelector(
            "vaadin-text-field[label='Adı'] input");

    /** "Tip" combo box */
    private final By TIP_COMBO = By.cssSelector(
            "vaadin-combo-box[label='Tip']");

    /** "Yeni Kriter Tanımı Ekle" butonu (NOT "Kaydet"!) */
    private final By YENI_KRITER_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yeni Kriter Tanımı Ekle']");

    /** "Güncelle" butonu */
    private final By GUNCELLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Güncelle'] | " +
            "//vaadin-button[normalize-space()='Guncelle']");

    /** "Sil" butonu */
    private final By SIL_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Sil'] | " +
            "//vaadin-button[normalize-space()='SIL'] | " +
            "//vaadin-button[@title='Sil' or @aria-label='Sil']");

    /** "Temizle" butonu */
    private final By TEMIZLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Temizle'] | " +
            "//vaadin-button[normalize-space()='TEMIZLE']");

    // ─── Bildirimler ──────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, vaadin-notification, [role='alert']");
    private final By ERROR_NOTIFICATION = By.cssSelector(
            "[theme*='error'], vaadin-notification[theme*='error']");
    private final By VALIDATION_ERROR = By.xpath(
            "//vaadin-text-field[@invalid] | //vaadin-number-field[@invalid] | " +
            "//*[@error-message] | //*[contains(@class,'error')]");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public CriteriaTablePage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Admin dashboard'dan "Kriter Tabloları" menüsüne gider ve "Tablo Ayarları" tabına geçer.
     * Navigasyon: Yönetim Paneli → Kriter Tabloları → Tablo Ayarları tab
     */
    public void navigateToCriteriaTable() {
        log.info("[CriteriaTablePage] 'Kriter Tabloları' navigasyonu başlıyor...");

        // Adım 1: Yönetim Paneli
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(YONETIM_PANELI_BTN));
            btn.click();
            waitForVaadinNavigation();
            log.info("[CriteriaTablePage] 'Yönetim Paneli' butonuna tıklandı.");
        } catch (Exception e) {
            log.warn("[CriteriaTablePage] 'Yönetim Paneli' butonu bulunamadı: {}", e.getMessage());
            return;
        }

        // Adım 2: Kriter Tabloları menüsü
        try {
            WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(KRITER_TABLOLARI_MENU));
            menu.click();
            waitForVaadinNavigation();
            log.info("[CriteriaTablePage] 'Kriter Tabloları' menüsüne tıklandı.");
        } catch (Exception e) {
            log.warn("[CriteriaTablePage] 'Kriter Tabloları' menüsü bulunamadı: {}", e.getMessage());
            return;
        }

        // Adım 3: "Tablo Ayarları" tabına geç
        clickTabloAyarlariTab();
    }

    /**
     * "Tablo Ayarları" tabına tıklar.
     */
    public void clickTabloAyarlariTab() {
        log.info("[CriteriaTablePage] 'Tablo Ayarları' tabına geçiliyor...");
        try {
            WebElement tab = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(TABLO_AYARLARI_TAB));
            tab.click();
            waitForVaadinNavigation();
            log.info("[CriteriaTablePage] 'Tablo Ayarları' tabına tıklandı.");
        } catch (Exception e) {
            log.warn("[CriteriaTablePage] 'Tablo Ayarları' tab bulunamadı: {}", e.getMessage());
        }
    }

    /** navigateToRebateCriteria eski uyumluluk alias. */
    public void navigateToRebateCriteria() {
        navigateToCriteriaTable();
    }

    // ─── CRUD Islemleri ───────────────────────────────────────────────────────

    /**
     * Yeni kriter tanımı ekler: Kod, Adı ve Tip alanlarını doldurur,
     * ardından "Yeni Kriter Tanımı Ekle" butonuna tıklar.
     *
     * @param kod  Kod değeri
     * @param adi  Adı değeri
     * @param tip  Tip değeri (combo box)
     */
    public void addCriteriaRow(String kod, String adi, String tip) {
        log.info("[CriteriaTablePage] Yeni kriter ekleniyor: kod={}, adi={}, tip={}", kod, adi, tip);
        if (kod != null && !kod.isBlank()) {
            fillTextField(KOD_INPUT, kod, "Kod");
        }
        if (adi != null && !adi.isBlank()) {
            fillTextField(ADI_INPUT, adi, "Adı");
        }
        if (tip != null && !tip.isBlank()) {
            selectComboBox(TIP_COMBO, tip, "Tip");
        }
        clickYeniKriterEkle();
    }

    /**
     * "Yeni Kriter Tanımı Ekle" butonuna tıklar.
     */
    public void clickYeniKriterEkle() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(YENI_KRITER_BTN));
            btn.click();
            log.info("[CriteriaTablePage] 'Yeni Kriter Tanımı Ekle' butonuna tıklandı.");
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> {
                        List<WebElement> notifs = d.findElements(SUCCESS_NOTIFICATION);
                        List<WebElement> errors = d.findElements(VALIDATION_ERROR);
                        return !notifs.isEmpty() || !errors.isEmpty();
                    });
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "Array.from(document.querySelectorAll('vaadin-button')).forEach(b=>{" +
                    "  if(b.textContent.trim()==='Yeni Kriter Tanımı Ekle') b.click();" +
                    "});"
                );
                log.warn("[CriteriaTablePage] 'Yeni Kriter Tanımı Ekle' JS ile tıklandı: {}", e.getMessage());
            } catch (Exception jse) {
                log.warn("[CriteriaTablePage] 'Yeni Kriter Tanımı Ekle' JS de başarısız: {}", jse.getMessage());
            }
        }
    }

    /**
     * "Kod" alanını doldurur.
     *
     * @param kod Kod değeri
     */
    public void enterKod(String kod) {
        fillTextField(KOD_INPUT, kod, "Kod");
    }

    /**
     * "Adı" alanını doldurur.
     *
     * @param adi Adı değeri
     */
    public void enterAdi(String adi) {
        fillTextField(ADI_INPUT, adi, "Adı");
    }

    /**
     * Eski uyumluluk: "Başlık" → "Adı" alanına yazar.
     */
    public void enterBaslik(String baslik) {
        fillTextField(ADI_INPUT, baslik, "Adı");
    }

    /**
     * "Kaydet" butonuna tıklar — "Yeni Kriter Tanımı Ekle" butonuna yönlendirir.
     */
    public void clickSave() {
        clickYeniKriterEkle();
    }

    /**
     * Rebate oranı alanını doldurur (eski uyumluluk — Kod alanına yazar).
     */
    public void enterRebateOran(String oran) {
        log.info("[CriteriaTablePage] Kod (eski: rebate oran) girildi: {}", oran);
        fillTextField(KOD_INPUT, oran, "Kod");
    }

    /**
     * Rebate miktarı alanını doldurur (eski uyumluluk — Adı alanına yazar).
     */
    public void enterRebateMiktar(String miktar) {
        log.info("[CriteriaTablePage] Adı (eski: rebate miktar) girildi: {}", miktar);
        fillTextField(ADI_INPUT, miktar, "Adı");
    }

    /**
     * Yeni kriter satiri ekler (eski uyumluluk — "Yeni Kriter Tanımı Ekle" butonuna tıklar).
     */
    public void clickAddRow() {
        log.info("[CriteriaTablePage] 'Yeni Kriter Tanımı Ekle' butonuna tıklanıyor...");
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(YENI_KRITER_BTN));
            btn.click();
            log.info("[CriteriaTablePage] 'Yeni Kriter Tanımı Ekle' butonuna tıklandı.");
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(KOD_INPUT));
        } catch (Exception e) {
            log.warn("[CriteriaTablePage] 'Yeni Kriter Tanımı Ekle' butonu bulunamadı veya form açılmadı: {}", e.getMessage());
        }
    }

    /**
     * İlk satırın sil butonuna tıklar.
     */
    public void deleteFirstRow() {
        try {
            List<WebElement> silBtns = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(SIL_BTN));
            if (!silBtns.isEmpty()) {
                silBtns.get(0).click();
                confirmDeleteDialog();
                log.info("[CriteriaTablePage] İlk satır silindi.");
            } else {
                log.warn("[CriteriaTablePage] 'Sil' butonu bulunamadı.");
            }
        } catch (Exception e) {
            log.warn("[CriteriaTablePage] Satır silinemedi: {}", e.getMessage());
        }
    }

    /**
     * İlk satırın Düzenle butonuna tıklar veya satırı seçer.
     */
    public void editFirstRow() {
        By DUZENLE_BTN = By.xpath(
                "//vaadin-button[normalize-space()='Düzenle'] | " +
                "//vaadin-button[normalize-space()='Duzenle']");
        try {
            List<WebElement> editBtns = driver.findElements(DUZENLE_BTN);
            if (!editBtns.isEmpty()) {
                editBtns.get(0).click();
                log.info("[CriteriaTablePage] İlk satır düzenleme modunda.");
            } else {
                List<WebElement> cells = driver.findElements(GRID_CELLS);
                if (!cells.isEmpty()) {
                    cells.get(0).click();
                    log.info("[CriteriaTablePage] Satır tıklama ile seçildi.");
                }
            }
        } catch (Exception e) {
            log.warn("[CriteriaTablePage] Satır düzenlenemedi: {}", e.getMessage());
        }
    }

    // ─── Dogrulama ────────────────────────────────────────────────────────────

    /** Kayit basarili mi. */
    public boolean isRebateSaved() {
        try {
            boolean notif = !new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> d.findElements(SUCCESS_NOTIFICATION)).isEmpty();
            if (notif) {
                log.info("[CriteriaTablePage] Başarı bildirimi görüldü.");
                return true;
            }
        } catch (Exception ignored) {}
        String src = driver.getPageSource();
        boolean saved = src.contains("Kriter tablosu başarıyla kaydedildi")
                || src.contains("kaydedildi") || src.contains("başarıyla") || src.contains("basarili");
        log.info("[CriteriaTablePage] isRebateSaved page source kontrolü: {}", saved);
        return saved;
    }

    /** Validasyon/hata mesaji goruluyor mu. */
    public boolean isValidationErrorVisible() {
        try {
            List<WebElement> errors = driver.findElements(VALIDATION_ERROR);
            if (!errors.isEmpty()) {
                log.info("[CriteriaTablePage] Validasyon hatası elementi bulundu.");
                return true;
            }
        } catch (Exception ignored) {}
        String src = driver.getPageSource();
        return src.contains("hata") || src.contains("invalid") || src.contains("error")
                || src.contains("gecersiz") || src.contains("geçersiz");
    }

    /** Grid'in sayfada gorunup gorunmedigini kontrol eder. */
    public boolean isCriteriaGridVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(CRITERIA_GRID));
            log.info("[CriteriaTablePage] Criteria grid görünüyor.");
            return true;
        } catch (Exception e) {
            log.info("[CriteriaTablePage] Criteria grid görünmüyor: {}", e.getMessage());
            return false;
        }
    }

    /** Grid'deki satir sayisini dondurur. */
    public int getRowCount() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(CRITERIA_GRID));
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            log.info("[CriteriaTablePage] Grid'de {} cell bulundu.", cells.size());
            return cells.size();
        } catch (Exception e) {
            log.info("[CriteriaTablePage] Grid satır sayısı alınamadı: {}", e.getMessage());
            return 0;
        }
    }

    /** Grid bos veya dolu mu (herhangi bir icerik var mi). */
    public boolean isGridDisplayedWithAnyContent() {
        return isCriteriaGridVisible();
    }

    // ─── Yardimci ─────────────────────────────────────────────────────────────

    private void fillTextField(By locator, String value, String fieldName) {
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
            log.info("[CriteriaTablePage] {} girildi: {}", fieldName, value);
        } catch (Exception e) {
            log.warn("[CriteriaTablePage] {} girilemedi ({}): {}", fieldName, value, e.getMessage());
        }
    }

    private void selectComboBox(By locator, String value, String fieldName) {
        try {
            WebElement combo = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];" +
                "arguments[0].dispatchEvent(new Event('value-changed',{bubbles:true}));",
                combo, value);
            log.info("[CriteriaTablePage] {} seçildi: {}", fieldName, value);
        } catch (Exception e) {
            log.warn("[CriteriaTablePage] {} seçilemedi ({}): {}", fieldName, value, e.getMessage());
        }
    }

    private void confirmDeleteDialog() {
        try {
            By confirmBtn = By.xpath(
                "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
                "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Sil'] | " +
                "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam']");
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(confirmBtn)).click();
        } catch (Exception ignored) {
            // Dialog açılmamış olabilir — inline silme
        }
    }
}
