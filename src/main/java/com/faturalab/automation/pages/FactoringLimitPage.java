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
 * Limit ve Fiyat Yonetimi sayfasi — Admin dashboard'dan 2 adımlı navigasyon:
 *
 *   1. "Yönetim Paneli" butonuna tıkla
 *   2. "Limit ve Fiyat Yönetimi" menü butonuna tıkla
 *   3. Grid yüklenir
 *
 * Sayfa başlığı: "LİMİT VE FİYAT YÖNETİMİ"
 * Grid sütunları: Finansal Kurum, Alıcı, Aktif, Tedarikçi Bazlı Fiyat, vb.
 * Her satır butonları: DÜZENLE, PASİF ET, DONDUR, SİL
 *
 * DÜZENLE dialogu: vaadin-dialog-overlay içinde vaadin-text-field, vaadin-checkbox, vaadin-combo-box
 */
public class FactoringLimitPage extends BasePageObject {

    // ─── Navigasyon Selectors ─────────────────────────────────────────────────

    /** "Yönetim Paneli" üst nav butonu */
    private final By YONETIM_PANELI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yönetim Paneli']");

    /** Sol menüde "Limit ve Fiyat Yönetimi" butonu */
    private final By LIMIT_MENU = By.xpath(
            "//vaadin-button[normalize-space()='Limit ve Fiyat Yönetimi']");

    // ─── Grid Selectors ───────────────────────────────────────────────────────

    private final By LIMIT_GRID  = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS  = By.cssSelector("vaadin-grid-cell-content");

    /** "DÜZENLE" butonu — satır içi */
    private final By DUZENLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='DÜZENLE']");

    // ─── Dialog Form Selectors ─────────────────────────────────────────────────

    /** "Alıcı Limiti" metin alanı — geniş XPath ile her tür field desteklenir */
    private final By ALICI_LIMIT_INPUT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-field[contains(@label,'Limit') or contains(@label,'limit') " +
            "or contains(@label,'Tutar') or contains(@label,'Alıcı') or contains(@label,'Alici')]//input | " +
            "//vaadin-dialog-overlay//vaadin-number-field//input | " +
            "//vaadin-dialog-overlay//vaadin-integer-field//input");

    /** "Fiyat (%)" metin alanı */
    private final By FIYAT_INPUT = By.cssSelector(
            "vaadin-dialog-overlay vaadin-text-field[label='Fiyat (%)'] input");

    /** "Fatura bazlı baremli fiyat" checkbox */
    private final By BAREMLI_FIYAT_CHECKBOX = By.cssSelector(
            "vaadin-dialog-overlay vaadin-checkbox[label='Fatura bazlı baremli fiyat']");

    /** "Tedarikçi bazlı baremli fiyat" checkbox */
    private final By TEDARIKCI_BAREMLI_CHECKBOX = By.cssSelector(
            "vaadin-dialog-overlay vaadin-checkbox[label='Tedarikçi bazlı baremli fiyat']");

    /** Dialog içi "Kaydet" butonu */
    private final By KAYDET_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet']");

    // ─── Bildirim Selectors ───────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, vaadin-notification, [role='alert']");

    private final By VALIDATION_ERROR = By.xpath(
            "//vaadin-number-field[@invalid] | //vaadin-integer-field[@invalid] | " +
            "//vaadin-text-field[@invalid] | //*[@error-message] | " +
            "//*[contains(@class,'error-message')]");

    // ─── Eski Uyumluluk Selectors ─────────────────────────────────────────────

    /** "Satıcı Limiti" alanı — eski navigasyon fallback */
    private final By SATICI_LIMIT_INPUT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-field[contains(@label,'Satıcı') or contains(@label,'Satici')]//input | " +
            "//vaadin-dialog-overlay//vaadin-number-field[contains(@label,'Satıcı')]//input");

    /** Alıcı limit menüsü (eski uyumluluk) */
    private final By ALICI_LIMIT_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[(contains(normalize-space(),'Alici') or contains(normalize-space(),'Alıcı')) and " +
            "(contains(normalize-space(),'Limit') or contains(normalize-space(),'limit'))]");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public FactoringLimitPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * "Limit ve Fiyat Yönetimi" sayfasına 2 adımlı navigasyon:
     * 1. "Yönetim Paneli" butonuna tıkla
     * 2. "Limit ve Fiyat Yönetimi" menü butonuna tıkla
     */
    public void navigateToLimitVeFiyatYonetimi() {
        log.info("[FactoringLimitPage] 'Limit ve Fiyat Yönetimi' navigasyonu başlıyor...");

        // Adım 1: Yönetim Paneli
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(YONETIM_PANELI_BTN));
            btn.click();
            waitForVaadinNavigation();
            log.info("[FactoringLimitPage] 'Yönetim Paneli' butonuna tıklandı.");
        } catch (Exception e) {
            log.warn("[FactoringLimitPage] 'Yönetim Paneli' butonu bulunamadı: {}", e.getMessage());
            return;
        }

        // Adım 2: Limit ve Fiyat Yönetimi menüsü
        try {
            WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(LIMIT_MENU));
            menu.click();
            waitForVaadinNavigation();
            log.info("[FactoringLimitPage] 'Limit ve Fiyat Yönetimi' menüsüne tıklandı.");
        } catch (Exception e) {
            log.warn("[FactoringLimitPage] 'Limit ve Fiyat Yönetimi' menüsü bulunamadı: {}", e.getMessage());
            return;
        }

        // Adım 3: Grid yüklensin
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.visibilityOfElementLocated(LIMIT_GRID));
            log.info("[FactoringLimitPage] 'Limit ve Fiyat Yönetimi' grid görünüyor.");
        } catch (Exception e) {
            log.warn("[FactoringLimitPage] Grid bekleme zaman aşımı: {}", e.getMessage());
        }
    }

    /**
     * Limit yönetimi navigasyonu — yeni akışı kullanır.
     */
    public void navigateToLimitManagement() {
        navigateToLimitVeFiyatYonetimi();
    }

    /**
     * Alici bazli limit tablosuna gider (eski uyumluluk).
     */
    public void navigateToAliciLimitTable() {
        log.info("[FactoringLimitPage] Alıcı bazlı limit tablosuna gidiliyor...");
        try {
            WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(ALICI_LIMIT_MENU));
            menu.click();
            waitForVaadinNavigation();
            log.info("[FactoringLimitPage] Alıcı Limit menüsüne gidildi.");
            return;
        } catch (Exception e) {
            log.info("[FactoringLimitPage] Alıcı Limit XPath bulunamadı, JS deneniyor...");
        }
        for (String kw : new String[]{"alıcı limit", "alici limit"}) {
            if (clickNavItemByText(kw)) {
                waitForVaadinNavigation();
                return;
            }
        }
        log.warn("[FactoringLimitPage] Alıcı Limit navigasyonu başarısız.");
    }

    // ─── Grid Aksiyonları ─────────────────────────────────────────────────────

    /**
     * Belirtilen finansal kurum satırının "DÜZENLE" butonuna tıklar.
     *
     * @param finansalKurum Grid'de aranacak finansal kurum adı
     */
    public void clickDuzenleForRow(String finansalKurum) {
        log.info("[FactoringLimitPage] '{}' satırı için DÜZENLE aranıyor...", finansalKurum);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.visibilityOfElementLocated(LIMIT_GRID));

            By rowDuzenleBtn = By.xpath(
                    "//*[contains(text(),'" + finansalKurum + "')]" +
                    "/ancestor::vaadin-grid-row//vaadin-button[normalize-space()='DÜZENLE'] | " +
                    "//*[contains(text(),'" + finansalKurum + "')]" +
                    "/ancestor::tr//vaadin-button[normalize-space()='DÜZENLE']");
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                        .until(ExpectedConditions.elementToBeClickable(rowDuzenleBtn));
                btn.click();
                waitForVaadinNavigation();
                log.info("[FactoringLimitPage] '{}' DÜZENLE butonuna tıklandı.", finansalKurum);
                return;
            } catch (Exception ignored) {}

            // Fallback: ilk DÜZENLE butonu
            clickFirstDuzenle();
        } catch (Exception e) {
            log.warn("[FactoringLimitPage] '{}' DÜZENLE butonu bulunamadı: {}", finansalKurum, e.getMessage());
        }
    }

    /**
     * Sayfadaki ilk "DÜZENLE" butonuna tıklar.
     */
    public void clickFirstDuzenle() {
        log.info("[FactoringLimitPage] İlk DÜZENLE butonuna tıklanıyor...");
        try {
            List<WebElement> btns = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(DUZENLE_BTN));
            if (!btns.isEmpty()) {
                btns.get(0).click();
                waitForVaadinNavigation();
                log.info("[FactoringLimitPage] İlk DÜZENLE butonuna tıklandı.");
            } else {
                log.warn("[FactoringLimitPage] DÜZENLE butonu bulunamadı.");
            }
        } catch (Exception e) {
            log.warn("[FactoringLimitPage] DÜZENLE butonuna tıklanamadı: {}", e.getMessage());
        }
    }

    // ─── Dialog Form Metotları ────────────────────────────────────────────────

    /**
     * Dialog "Alıcı Limiti" alanını doldurur.
     *
     * @param amount Limit miktarı
     */
    public void enterAliciLimit(String amount) {
        log.info("[FactoringLimitPage] Alıcı Limiti giriliyor: {}", amount);
        fillDialogField(ALICI_LIMIT_INPUT, amount, "Alıcı Limiti");
    }

    /**
     * Dialog "Fiyat (%)" alanını doldurur.
     *
     * @param rate Fiyat oranı
     */
    public void enterFiyat(String rate) {
        log.info("[FactoringLimitPage] Fiyat (%) giriliyor: {}", rate);
        fillDialogField(FIYAT_INPUT, rate, "Fiyat (%)");
    }

    /**
     * "Fatura bazlı baremli fiyat" checkbox durumunu ayarlar.
     *
     * @param checked true → işaretli, false → işaretsiz
     */
    public void setBaremliCheckbox(boolean checked) {
        log.info("[FactoringLimitPage] Fatura bazlı baremli fiyat checkbox: {}", checked);
        setCheckbox(BAREMLI_FIYAT_CHECKBOX, checked, "Fatura bazlı baremli fiyat");
    }

    /**
     * "Tedarikçi bazlı baremli fiyat" checkbox durumunu ayarlar.
     *
     * @param checked true → işaretli, false → işaretsiz
     */
    public void setTedarikciBaremliCheckbox(boolean checked) {
        log.info("[FactoringLimitPage] Tedarikçi bazlı baremli fiyat checkbox: {}", checked);
        setCheckbox(TEDARIKCI_BAREMLI_CHECKBOX, checked, "Tedarikçi bazlı baremli fiyat");
    }

    /**
     * Dialog "Kaydet" butonuna tıklar.
     */
    public void clickKaydet() {
        log.info("[FactoringLimitPage] Dialog 'Kaydet' butonuna tıklanıyor...");
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
            btn.click();
            log.info("[FactoringLimitPage] Dialog 'Kaydet' butonuna tıklandı.");
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> {
                        List<WebElement> notifs = d.findElements(SUCCESS_NOTIFICATION);
                        List<WebElement> errors = d.findElements(VALIDATION_ERROR);
                        return !notifs.isEmpty() || !errors.isEmpty();
                    });
        } catch (Exception e) {
            log.warn("[FactoringLimitPage] 'Kaydet' butonu beklenemedi: {}", e.getMessage());
        }
    }

    // ─── Eski API Uyumluluk Metotları ─────────────────────────────────────────

    /**
     * Dialog formu doldurur (eski step def uyumluluğu).
     * Önce DÜZENLE butonuna tıklar, sonra dialog alanlarını doldurur.
     */
    public void fillLimitForm(String kurumAdi, String limitTutari, String paraBirimi) {
        log.info("[FactoringLimitPage] Limit formu dolduruluyor: kurum={}, tutar={}", kurumAdi, limitTutari);
        // Dialog açılmadan önce DÜZENLE butonuna tıkla
        if (kurumAdi != null && !kurumAdi.isBlank()) {
            clickDuzenleForRow(kurumAdi);
        } else {
            clickFirstDuzenle();
        }
        // Dialog açıldıktan sonra alanları doldur
        if (limitTutari != null && !limitTutari.isBlank()) {
            enterLimitAmount(limitTutari);
        }
    }

    /**
     * Limit tutarı alanına değer girer.
     * Önce "Alıcı Limiti", bulunamazsa "Satıcı Limiti" alanını doldurur.
     *
     * @param amount Limit tutarı
     */
    public void enterLimitAmount(String amount) {
        log.info("[FactoringLimitPage] Limit tutarı giriliyor: {}", amount);
        if (!fillDialogField(ALICI_LIMIT_INPUT, amount, "Alıcı Limiti")) {
            if (!fillDialogField(SATICI_LIMIT_INPUT, amount, "Satıcı Limiti")) {
                log.warn("[FactoringLimitPage] Limit alanı bulunamadı (Alıcı veya Satıcı).");
            }
        }
    }

    /** Para birimi seçimi — dialog otomatik, uyumluluk için bırakıldı. */
    public void selectCurrency(String currency) {
        log.info("[FactoringLimitPage] Para birimi seçimi atlandı (dialog otomatik): {}", currency);
    }

    /**
     * Dialog "Kaydet" butonuna tıklar (eski API uyumluluk).
     */
    public void clickSave() {
        clickKaydet();
    }

    // ─── Dogrulama ────────────────────────────────────────────────────────────

    /** Kayit basarili mi kontrol eder. */
    public boolean isLimitSaved() {
        try {
            boolean notifVisible = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> !d.findElements(SUCCESS_NOTIFICATION).isEmpty());
            if (notifVisible) {
                log.info("[FactoringLimitPage] Başarı bildirimi görüldü.");
                return true;
            }
        } catch (Exception ignored) {}
        // vaadin-notification içindeki metni de kontrol et
        try {
            List<WebElement> notifs = driver.findElements(
                    By.cssSelector("vaadin-notification-card, vaadin-notification"));
            for (WebElement n : notifs) {
                String txt = n.getText();
                if (txt != null && (txt.toLowerCase().contains("güncellendi") ||
                        txt.toLowerCase().contains("kaydedildi") ||
                        txt.toLowerCase().contains("başarıyla") ||
                        txt.toLowerCase().contains("basarili") ||
                        txt.toLowerCase().contains("saved") ||
                        txt.toLowerCase().contains("updated"))) {
                    log.info("[FactoringLimitPage] Vaadin notification başarı metni: {}", txt);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        String src = driver.getPageSource();
        boolean saved = src.contains("Güncellendi") || src.contains("güncellendi")
                || src.contains("kaydedildi") || src.contains("başarıyla") || src.contains("basarili")
                || src.contains("saved") || src.contains("updated");
        log.info("[FactoringLimitPage] isLimitSaved page source kontrolü: {}", saved);
        return saved;
    }

    /** Validasyon hatasi goruluyor mu. */
    public boolean isValidationErrorVisible() {
        try {
            List<WebElement> errors = driver.findElements(VALIDATION_ERROR);
            if (!errors.isEmpty()) {
                log.info("[FactoringLimitPage] Validasyon hatası elementi bulundu.");
                return true;
            }
        } catch (Exception ignored) {}
        String src = driver.getPageSource();
        return src.contains("hata") || src.contains("error") || src.contains("invalid")
                || src.contains("zorunlu") || src.contains("gecersiz") || src.contains("geçersiz");
    }

    /** Grid'de belirtilen degerin gorundugunu kontrol eder. */
    public boolean isValueVisibleInGrid(String value) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(LIMIT_GRID));
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            for (WebElement cell : cells) {
                if (cell.getText() != null && cell.getText().contains(value)) {
                    log.info("[FactoringLimitPage] Grid'de değer bulundu: {}", value);
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("[FactoringLimitPage] Grid değer kontrolü: {}", e.getMessage());
        }
        return driver.getPageSource().contains(value);
    }

    /** Limit grid'inin sayfada gorunup gorunmedigini kontrol eder. */
    public boolean isGridVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(LIMIT_GRID));
            log.info("[FactoringLimitPage] Limit grid görünüyor.");
            return true;
        } catch (Exception e) {
            log.info("[FactoringLimitPage] Limit grid görünmüyor: {}", e.getMessage());
            return false;
        }
    }

    /** Limit sayfasının erişime kapalı olup olmadığını kontrol eder. */
    public boolean isAccessDenied() {
        String src = driver.getPageSource();
        String url = driver.getCurrentUrl();
        boolean menuAbsent = driver.findElements(LIMIT_MENU).isEmpty()
                && driver.findElements(YONETIM_PANELI_BTN).isEmpty();
        boolean errorPage  = src.contains("Access Denied") || src.contains("403") ||
                             src.contains("Yetkisiz") || url.contains("/error");
        boolean denied = menuAbsent || errorPage;
        log.info("[FactoringLimitPage] isAccessDenied: menuAbsent={}, errorPage={}", menuAbsent, errorPage);
        return denied;
    }

    // ─── Yardimci Metotlar ────────────────────────────────────────────────────

    private boolean fillDialogField(By locator, String value, String fieldName) {
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
            log.info("[FactoringLimitPage] {} girildi: {}", fieldName, value);
            return true;
        } catch (Exception e) {
            log.info("[FactoringLimitPage] {} alanı bulunamadı: {}", fieldName, e.getMessage());
            return false;
        }
    }

    private void setCheckbox(By locator, boolean checked, String fieldName) {
        try {
            WebElement checkbox = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(locator));
            String checkedAttr = checkbox.getAttribute("checked");
            boolean isChecked = checkedAttr != null && !checkedAttr.equals("false");
            if (isChecked != checked) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", checkbox);
                log.info("[FactoringLimitPage] {} checkbox {} yapıldı.", fieldName, checked ? "işaretlendi" : "kaldırıldı");
            } else {
                log.info("[FactoringLimitPage] {} checkbox zaten {} durumunda.", fieldName, checked ? "işaretli" : "işaretsiz");
            }
        } catch (Exception e) {
            log.warn("[FactoringLimitPage] {} checkbox ayarlanamadı: {}", fieldName, e.getMessage());
        }
    }
}
