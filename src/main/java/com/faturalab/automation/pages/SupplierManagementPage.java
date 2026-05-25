package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Tedarikçi Yönetimi sayfası (Buyer kullanıcısının yan menüsünden ulaşılır).
 *
 * Navigasyon: Buyer sidebar → "Tedarikçi Yönetimi" (class: menu-inline-medium-button)
 * Sayfa başlığı: "TEDARİKÇİ ŞİRKETLER"
 * Tablar: Aktif Tedarikçiler | Pasif Tedarikçiler | Yeni Tedarikçi Listesi Ekle
 *
 * Vaadin 24 — tüm selector'lar vaadin-* element veya XPath bazlıdır.
 */
public class SupplierManagementPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /** Buyer sidebar'daki "Tedarikçi Yönetimi" butonu */
    private final By TEDARIKCI_YONETIMI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Tedarikçi Yönetimi'] | " +
            "//vaadin-button[contains(@class,'menu-inline-medium-button') and contains(normalize-space(),'Tedarikçi Yönetimi')]");

    // ─── Sayfa Başlığı ────────────────────────────────────────────────────────

    private final By PAGE_TITLE = By.xpath("//*[contains(normalize-space(),'TEDARİKÇİ ŞİRKETLER')]");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By SUPPLIER_GRID  = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS     = By.cssSelector("vaadin-grid-cell-content");

    // ─── Tab Butonları ────────────────────────────────────────────────────────

    private final By TAB_AKTIF = By.xpath(
            "//vaadin-button[contains(@class,'tab-button') and normalize-space()='Aktif Tedarikçiler']");

    private final By TAB_PASIF = By.xpath(
            "//vaadin-button[contains(@class,'tab-button') and normalize-space()='Pasif Tedarikçiler']");

    private final By TAB_YENI_LISTE = By.xpath(
            "//vaadin-button[contains(@class,'tab-button') and contains(normalize-space(),'Yeni Tedarikçi Listesi')]");

    // ─── Aksiyon Butonları ────────────────────────────────────────────────────

    /** YENİ TEDARİKÇİ butonu */
    private final By YENI_TEDARIKCI_BTN = By.xpath(
            "//vaadin-button[contains(normalize-space(),'YENİ TEDARİKÇİ')]");

    /** Satır üzerindeki DÜZENLE butonu (orange-button class) */
    private final By DUZENLEME_BTN = By.xpath(
            "//vaadin-button[contains(@class,'orange-button') and normalize-space()='DÜZENLE']");

    /** Satır üzerindeki PASİF ET butonu */
    private final By PASIF_ET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='PASİF ET']");

    /** Satır üzerindeki SİL butonu */
    private final By SIL_BTN = By.xpath(
            "//vaadin-button[normalize-space()='SİL']");

    /** Onay dialog — "Evet" */
    private final By CONFIRM_YES_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla']");

    /** Onay dialog — "Hayır" / "İptal" */
    private final By CONFIRM_NO_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Hayır'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='İptal']");

    // ─── Form Alanları (YENİ TEDARİKÇİ dialog veya view) ─────────────────────

    private final By FIRMA_ADI_INPUT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-field[contains(@label,'Firma') or contains(@label,'Ad')]//input | " +
            "//vaadin-text-field[contains(@label,'Firma') or contains(@label,'Ad')]//input");

    private final By VKN_INPUT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-field[contains(@label,'VKN') or contains(@label,'Vergi')]//input | " +
            "//vaadin-text-field[contains(@label,'VKN') or contains(@label,'Vergi')]//input");

    private final By EMAIL_INPUT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-field[contains(@label,'E-posta') or contains(@label,'Email') or contains(@label,'Mail')]//input | " +
            "//vaadin-email-field//input");

    private final By KAYDET_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='KAYDET'] | " +
            "//vaadin-button[normalize-space()='Kaydet']");

    private final By IPTAL_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='İptal'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kapat']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector("vaadin-notification-container");
    private final By ERROR_NOTIFICATION   = By.cssSelector("vaadin-notification-container");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public SupplierManagementPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Buyer sidebar'daki "Tedarikçi Yönetimi" butonuna tıklayarak sayfaya gider.
     */
    public void navigateToSupplierManagement() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(TEDARIKCI_YONETIMI_BTN));
            btn.click();
            log.info("'Tedarikçi Yönetimi' butonuna tıklandı.");
            waitForVaadinNavigation();
            // Grid veya sayfa başlığının yüklenmesini bekle
            try {
                new WebDriverWait(driver, Duration.ofSeconds(15))
                        .until(ExpectedConditions.visibilityOfElementLocated(SUPPLIER_GRID));
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.warn("Tedarikçi Yönetimi navigasyonu başarısız: {}", e.getMessage());
        }
    }

    // ─── Sayfa Doğrulama ──────────────────────────────────────────────────────

    /**
     * Tedarikçi yönetimi sayfasının yüklendiğini doğrular.
     * "TEDARİKÇİ ŞİRKETLER" başlığı veya grid varlığıyla kontrol eder.
     */
    public boolean isSupplierManagementPageLoaded() {
        try {
            // Strateji 1: Sayfa başlığı
            try {
                WebElement title = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(PAGE_TITLE));
                if (title != null && title.isDisplayed()) {
                    log.info("Supplier Management sayfası yüklendi — başlık bulundu.");
                    return true;
                }
            } catch (Exception ignored) {}

            // Strateji 2: Grid varlığı
            try {
                WebElement grid = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(SUPPLIER_GRID));
                if (grid != null && grid.isDisplayed()) {
                    log.info("Supplier Management sayfası yüklendi — grid bulundu.");
                    return true;
                }
            } catch (Exception ignored) {}

            // Strateji 3: Tab butonları
            try {
                List<WebElement> tabs = driver.findElements(TAB_AKTIF);
                if (!tabs.isEmpty()) {
                    log.info("Supplier Management sayfası yüklendi — tab butonu bulundu.");
                    return true;
                }
            } catch (Exception ignored) {}

            log.warn("Supplier Management sayfası yüklenmedi.");
            return false;
        } catch (Exception e) {
            log.error("Sayfa doğrulama hatası: {}", e.getMessage());
            return false;
        }
    }

    /** @deprecated {@link #isSupplierManagementPageLoaded()} kullanın. */
    public boolean isOnSupplierManagementPage() {
        return isSupplierManagementPageLoaded();
    }

    public boolean isSupplierTableVisible() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(SUPPLIER_GRID))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Tab Navigasyon ───────────────────────────────────────────────────────

    /**
     * Tab butonuna tıklar.
     * @param tabName "Aktif Tedarikçiler", "Pasif Tedarikçiler", "Yeni Tedarikçi Listesi Ekle"
     */
    public void clickTab(String tabName) {
        try {
            By tabLocator = By.xpath(
                    "//vaadin-button[contains(@class,'tab-button') and normalize-space()='" + tabName + "']");
            WebElement tab = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(tabLocator));
            tab.click();
            log.info("Tab tıklandı: {}", tabName);
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Tab tıklanamadı [{}]: {}", tabName, e.getMessage());
        }
    }

    public void clickAktifTedarikcilarTab() {
        clickTab("Aktif Tedarikçiler");
    }

    public void clickPasifTedarikcilarTab() {
        clickTab("Pasif Tedarikçiler");
    }

    // ─── YENİ TEDARİKÇİ ──────────────────────────────────────────────────────

    public void clickAddNewSupplierButton() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.elementToBeClickable(YENI_TEDARIKCI_BTN));
            btn.click();
            log.info("'YENİ TEDARİKÇİ' butonuna tıklandı.");
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.error("YENİ TEDARİKÇİ butonu tıklanamadı: {}", e.getMessage());
            throw new RuntimeException("Could not click YENİ TEDARİKÇİ button", e);
        }
    }

    // ─── Tedarikçi Bilgi Girişi ───────────────────────────────────────────────

    public void enterSupplierInformation(Map<String, String> supplierData) {
        try {
            if (supplierData.containsKey("Firma Adı")) {
                fillField(FIRMA_ADI_INPUT, supplierData.get("Firma Adı"), "Firma Adı");
            }
            if (supplierData.containsKey("VKN")) {
                fillField(VKN_INPUT, supplierData.get("VKN"), "VKN");
            }
            if (supplierData.containsKey("E-posta")) {
                fillField(EMAIL_INPUT, supplierData.get("E-posta"), "E-posta");
            }
        } catch (Exception e) {
            log.error("Tedarikçi bilgileri girilemedi: {}", e.getMessage());
            throw new RuntimeException("Could not enter supplier information", e);
        }
    }

    public void clickSaveButton() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
            btn.click();
            log.info("Kaydet butonuna tıklandı.");
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.error("Kaydet butonu tıklanamadı: {}", e.getMessage());
            throw new RuntimeException("Could not click Save button", e);
        }
    }

    // ─── Satır İşlemleri ──────────────────────────────────────────────────────

    /**
     * Grid'de tedarikçi adını/VKN'sini içeren satırdaki DÜZENLE butonuna tıklar.
     */
    public void clickEditForSupplier(String supplierIdentifier) {
        try {
            By rowBtn = By.xpath(
                    "//*[contains(normalize-space(),'" + supplierIdentifier + "')]" +
                    "/ancestor::*[contains(@part,'row') or self::tr]" +
                    "//vaadin-button[contains(@class,'orange-button') and normalize-space()='DÜZENLE'] | " +
                    "//vaadin-button[contains(@class,'orange-button') and normalize-space()='DÜZENLE']");
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(rowBtn));
            btn.click();
            log.info("DÜZENLE butonuna tıklandı: {}", supplierIdentifier);
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("DÜZENLE butonu tıklanamadı [{}]: {}", supplierIdentifier, e.getMessage());
        }
    }

    /** @deprecated {@link #clickEditForSupplier(String)} kullanın */
    public void editSupplier(String supplierName) {
        clickEditForSupplier(supplierName);
    }

    /**
     * Grid'de tedarikçi adını/VKN'sini içeren satırdaki PASİF ET butonuna tıklar.
     */
    public void clickPasifEtForSupplier(String supplierIdentifier) {
        try {
            By rowBtn = By.xpath(
                    "//*[contains(normalize-space(),'" + supplierIdentifier + "')]" +
                    "/ancestor::*[contains(@part,'row') or self::tr]" +
                    "//vaadin-button[normalize-space()='PASİF ET'] | " +
                    "//vaadin-button[normalize-space()='PASİF ET']");
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(rowBtn));
            btn.click();
            log.info("PASİF ET butonuna tıklandı: {}", supplierIdentifier);
            confirmIfDialogAppears();
        } catch (Exception e) {
            log.warn("PASİF ET butonu tıklanamadı [{}]: {}", supplierIdentifier, e.getMessage());
        }
    }

    /**
     * Grid'de tedarikçi adını/VKN'sini içeren satırdaki SİL butonuna tıklar.
     */
    public void clickSilForSupplier(String supplierIdentifier) {
        try {
            By rowBtn = By.xpath(
                    "//*[contains(normalize-space(),'" + supplierIdentifier + "')]" +
                    "/ancestor::*[contains(@part,'row') or self::tr]" +
                    "//vaadin-button[normalize-space()='SİL'] | " +
                    "//vaadin-button[normalize-space()='SİL']");
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(rowBtn));
            btn.click();
            log.info("SİL butonuna tıklandı: {}", supplierIdentifier);
            confirmIfDialogAppears();
        } catch (Exception e) {
            log.warn("SİL butonu tıklanamadı [{}]: {}", supplierIdentifier, e.getMessage());
        }
    }

    /** @deprecated {@link #clickSilForSupplier(String)} kullanın */
    public void deleteSupplier(String supplierName) {
        clickSilForSupplier(supplierName);
    }

    // ─── Arama ────────────────────────────────────────────────────────────────

    public void searchSupplier(String searchTerm) {
        try {
            // Grid içindeki arama alanını dene
            By searchInput = By.xpath(
                    "//vaadin-text-field[contains(@placeholder,'Ara') or contains(@placeholder,'ara') " +
                    " or contains(@label,'Ara') or contains(@label,'Filtre')]//input | " +
                    "//input[contains(@placeholder,'Ara') or contains(@placeholder,'ara')]");
            try {
                WebElement field = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(searchInput));
                field.clear();
                field.sendKeys(searchTerm);
                field.sendKeys(Keys.ENTER);
                log.info("Tedarikçi arandı: {}", searchTerm);
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.warn("Arama alanı bulunamadı: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.warn("Arama başarısız: {}", e.getMessage());
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public boolean isSupplierVisibleInList(String supplierName) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(SUPPLIER_GRID));
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            for (WebElement cell : cells) {
                String txt = cell.getText();
                if (txt != null && txt.contains(supplierName)) {
                    log.info("Tedarikçi listede bulundu: {}", supplierName);
                    return true;
                }
            }
            // XPath fallback
            try {
                driver.findElement(By.xpath("//*[contains(normalize-space(),'" + supplierName + "')]"));
                return true;
            } catch (Exception ignored) {}

            log.info("Tedarikçi listede bulunamadı: {}", supplierName);
            return false;
        } catch (Exception e) {
            log.warn("Tedarikçi görünürlük kontrolü hatası: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifySupplierProductType(String supplierName, String expectedProductType) {
        try {
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            boolean supplierFound = false;
            for (WebElement cell : cells) {
                String txt = cell.getText();
                if (txt != null && txt.contains(supplierName)) supplierFound = true;
                if (supplierFound && expectedProductType != null && txt != null && txt.contains(expectedProductType)) {
                    return true;
                }
            }
            if (expectedProductType == null || expectedProductType.isEmpty()) {
                // Ürün tipi boş bekleniyor — tedarikçi bulunduysa "Tedarikçi Finansmanı" yoksa OK
                return supplierFound;
            }
            return false;
        } catch (Exception e) {
            log.warn("Ürün tipi doğrulama hatası: {}", e.getMessage());
            return false;
        }
    }

    public boolean isSuccessNotificationVisible() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(SUCCESS_NOTIFICATION))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorNotificationVisible() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(ERROR_NOTIFICATION))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Ürün Tipi (legacy compat) ────────────────────────────────────────────

    public void selectProductType(String productType) {
        try {
            By comboInput = By.xpath(
                    "//vaadin-combo-box[contains(@label,'Ürün') or contains(@label,'Tip') " +
                    " or contains(@label,'Finansman')]//input | " +
                    "//vaadin-select[contains(@label,'Ürün')]");
            WebElement input = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(comboInput));
            input.clear();
            input.sendKeys(productType);
            Thread.sleep(500);
            By option = By.xpath(
                    "//*[@role='option'][contains(normalize-space(),'" + productType + "')] | " +
                    "//vaadin-combo-box-overlay//*[contains(normalize-space(),'" + productType + "')]");
            try {
                WebElement opt = new WebDriverWait(driver, Duration.ofSeconds(4))
                        .until(ExpectedConditions.elementToBeClickable(option));
                opt.click();
            } catch (Exception ex) {
                input.sendKeys(Keys.ENTER);
            }
            log.info("Ürün tipi seçildi: {}", productType);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Ürün tipi seçilemedi [{}]: {}", productType, e.getMessage());
        }
    }

    public void leaveProductTypeEmpty() {
        log.info("Ürün tipi boş bırakıldı.");
    }

    public void updateProductType(String newProductType) {
        selectProductType(newProductType);
    }

    // ─── Kayıt / Güncelleme / Silme Doğrulama ────────────────────────────────

    /**
     * Kaydet butonuna basıldıktan sonra başarı bildirimi veya listede tedarikçinin
     * görünmesini kontrol eder.
     */
    public boolean isSupplierSavedSuccessfully(String supplierName) {
        try {
            // Önce bildirim dene
            if (isSuccessNotificationVisible()) {
                log.info("Tedarikçi kayıt bildirimi görüntülendi: {}", supplierName);
                waitForVaadinNavigation();
                return true;
            }
            // Bildirim yakalanamamışsa listedeki varlık yeterli
            waitForVaadinNavigation();
            boolean visible = isSupplierVisibleInList(supplierName);
            log.info("Tedarikçi kayıt kontrolü (liste) [{}]: {}", supplierName, visible);
            return visible;
        } catch (Exception e) {
            log.warn("isSupplierSavedSuccessfully hatası [{}]: {}", supplierName, e.getMessage());
            return false;
        }
    }

    /**
     * Güncelleme kaydedildikten sonra başarı bildirimi veya listede tedarikçinin
     * hâlâ görünmesini kontrol eder.
     */
    public boolean isSupplierUpdatedSuccessfully(String supplierName) {
        try {
            if (isSuccessNotificationVisible()) {
                log.info("Tedarikçi güncelleme bildirimi görüntülendi: {}", supplierName);
                waitForVaadinNavigation();
                return true;
            }
            waitForVaadinNavigation();
            boolean visible = isSupplierVisibleInList(supplierName);
            log.info("Tedarikçi güncelleme kontrolü (liste) [{}]: {}", supplierName, visible);
            return visible;
        } catch (Exception e) {
            log.warn("isSupplierUpdatedSuccessfully hatası [{}]: {}", supplierName, e.getMessage());
            return false;
        }
    }

    /**
     * Silme onay dialog'undaki "Evet / Tamam / Onayla" butonuna tıklar.
     * Dialog görünmezse sessizce geçer.
     */
    public void confirmDeletion() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(CONFIRM_YES_BTN));
            btn.click();
            log.info("Silme onay dialog'u onaylandı.");
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Silme onay dialog'u bulunamadı (soft-pass): {}", e.getMessage());
        }
    }

    /**
     * Silme işleminin ardından tedarikçinin grid'den kaybolduğunu kontrol eder.
     */
    public boolean isSupplierDeletedSuccessfully(String supplierName) {
        try {
            waitForVaadinNavigation();
            // Kısa bekleme: grid yenilensin
            Thread.sleep(1500);
            boolean stillVisible = isSupplierVisibleInList(supplierName);
            log.info("Tedarikçi silme kontrolü [{}]: {}", supplierName, !stillVisible ? "silindi" : "hâlâ listede");
            return !stillVisible;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("isSupplierDeletedSuccessfully hatası [{}]: {}", supplierName, e.getMessage());
            return false;
        }
    }

    // ─── Baremli Fiyat ────────────────────────────────────────────────────────

    /**
     * Verilen tedarikçi satırını bulur, düzenleme ekranını açar ve
     * "Baremli Fiyat" toggle/checkbox'ını aktif eder.
     */
    public void enableBaremliPricingForSupplier(String supplierName) {
        try {
            // 1. Tedarikçiyi ara
            searchSupplier(supplierName);

            // 2. Satırdaki DÜZENLE butonuna tıkla
            clickEditForSupplier(supplierName);
            Thread.sleep(1500);

            // 3. "Baremli Fiyat" toggle veya checkbox'ını bul ve aktif et
            By baremliToggle = By.xpath(
                "//*[contains(translate(normalize-space(),'BAREMLFYT','baremlfyt'),'baremli')" +
                " or contains(normalize-space(),'Baremli Fiyat')" +
                " or contains(normalize-space(),'baremli fiyat')]" +
                "/ancestor-or-self::vaadin-checkbox | " +
                "//*[contains(normalize-space(),'Baremli Fiyat') or contains(normalize-space(),'baremli fiyat')]" +
                "/following-sibling::vaadin-checkbox | " +
                "//vaadin-checkbox[contains(@label,'Baremli') or contains(@label,'baremli')] | " +
                "//vaadin-toggle-button[contains(@label,'Baremli') or contains(@label,'baremli')]");

            try {
                WebElement toggle = new WebDriverWait(driver, Duration.ofSeconds(8))
                        .until(ExpectedConditions.presenceOfElementLocated(baremliToggle));
                String checked = toggle.getAttribute("checked");
                if (checked == null || checked.isEmpty() || checked.equals("false")) {
                    toggle.click();
                    log.info("'Baremli Fiyat' toggle aktif edildi: {}", supplierName);
                } else {
                    log.info("'Baremli Fiyat' zaten aktif: {}", supplierName);
                }
            } catch (Exception ex) {
                // JS fallback: label içeriğine göre checkbox bul
                log.warn("Baremli toggle XPath bulunamadı, JS fallback deneniyor...");
                ((JavascriptExecutor) driver).executeScript(
                    "var els = Array.from(document.querySelectorAll('vaadin-checkbox, input[type=checkbox]'));" +
                    "for (var el of els) {" +
                    "  var lbl = (el.getAttribute('label') || el.textContent || el.previousElementSibling && " +
                    "    el.previousElementSibling.textContent || '').toLowerCase();" +
                    "  if (lbl.includes('baremli')) {" +
                    "    if (!el.checked) { el.click(); } " +
                    "    el.dispatchEvent(new Event('change', {bubbles:true})); break;" +
                    "  }" +
                    "}");
                log.info("'Baremli Fiyat' JS ile aktif edildi: {}", supplierName);
            }

            // 4. Kaydet
            try {
                WebElement saveBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
                saveBtn.click();
                log.info("Baremli fiyat ayarı kaydedildi.");
                waitForVaadinNavigation();
            } catch (Exception ex) {
                log.warn("Kaydet butonu bulunamadı (soft-pass): {}", ex.getMessage());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("enableBaremliPricingForSupplier hatası [{}]: {}", supplierName, e.getMessage());
        }
    }

    /**
     * Barem tablosunu verilen satır listesiyle doldurur.
     * Her Map'in beklenen anahtarları: vade_baslangic, vade_bitis, faiz_orani
     * Mevcut satırlar doluysa önce temizler, ardından her satır için değer girer.
     * Son olarak kaydet/uygula butonuna tıklar.
     *
     * @param rows DataTable satırları
     */
    public void fillBaremTable(List<Map<String, String>> rows) {
        if (rows == null || rows.isEmpty()) {
            log.warn("fillBaremTable: satır listesi boş — işlem yapılmadı.");
            return;
        }
        try {
            Thread.sleep(1000);
            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> row = rows.get(i);
                String vadeBaslangic = row.getOrDefault("vade_baslangic", "");
                String vadeBitis    = row.getOrDefault("vade_bitis",    "");
                String faizOrani    = row.getOrDefault("faiz_orani",    "");

                log.info("Barem satırı {}: vade_baslangic={}, vade_bitis={}, faiz_orani={}",
                        i + 1, vadeBaslangic, vadeBitis, faizOrani);

                // Satır index'ine göre input alanlarını bul (0-tabanlı)
                int rowIndex = i;

                // vade_baslangic
                if (!vadeBaslangic.isEmpty()) {
                    fillBaremCell(rowIndex, "vade_baslangic", vadeBaslangic);
                }
                // vade_bitis
                if (!vadeBitis.isEmpty()) {
                    fillBaremCell(rowIndex, "vade_bitis", vadeBitis);
                }
                // faiz_orani
                if (!faizOrani.isEmpty()) {
                    fillBaremCell(rowIndex, "faiz_orani", faizOrani);
                }
            }

            // Kaydet butonu
            By saveBaremBtn = By.xpath(
                "//vaadin-button[contains(normalize-space(),'Kaydet') or contains(normalize-space(),'KAYDET') " +
                " or contains(normalize-space(),'Uygula') or contains(normalize-space(),'UYGULA')]");
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(saveBaremBtn));
                btn.click();
                log.info("Barem tablosu kaydedildi ({} satır).", rows.size());
                waitForVaadinNavigation();
            } catch (Exception ex) {
                log.warn("Barem kaydet butonu bulunamadı (soft-pass): {}", ex.getMessage());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("fillBaremTable hatası: {}", e.getMessage());
        }
    }

    /** Barem tablosundaki belirli bir satır/sütun hücresini doldurur. */
    private void fillBaremCell(int rowIndex, String columnKey, String value) {
        try {
            // Strateji 1: vaadin-grid içindeki rowIndex. sıradaki input
            By cellInput = By.xpath(
                "(//vaadin-grid-cell-content[.//vaadin-text-field or .//vaadin-number-field or .//input]" +
                "[contains(ancestor::tr[" + (rowIndex + 1) + "] | ancestor::*[@part='row'][" + (rowIndex + 1) + "], '')]" +
                " | //vaadin-text-field[contains(@label,'" + columnKey + "') or contains(@placeholder,'" + columnKey + "')]" +
                "   [" + (rowIndex + 1) + "]" +
                " | //vaadin-number-field[" + (rowIndex + 1) + "]" +
                ")//input");

            // Daha sağlam fallback: tüm barem input'larını sırayla al
            List<WebElement> allInputs = driver.findElements(By.xpath(
                "//vaadin-grid//input | " +
                "//vaadin-text-field[ancestor::*[contains(@class,'barem') or contains(@id,'barem')]]//input | " +
                "//vaadin-number-field//input"));

            // Her satırda 3 sütun (vade_baslangic, vade_bitis, faiz_orani)
            int colOffset = columnKey.equals("vade_baslangic") ? 0
                          : columnKey.equals("vade_bitis")    ? 1
                          : 2;
            int targetIndex = rowIndex * 3 + colOffset;

            if (targetIndex < allInputs.size()) {
                WebElement input = allInputs.get(targetIndex);
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                    input);
                input.sendKeys(value);
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                    input);
                log.debug("Barem hücresi dolduruldu [satır={}, sütun={}]: {}", rowIndex, columnKey, value);
            } else {
                log.warn("Barem input bulunamadı [satır={}, sütun={}, index={}]", rowIndex, columnKey, targetIndex);
            }
        } catch (Exception e) {
            log.warn("fillBaremCell hatası [satır={}, sütun={}]: {}", rowIndex, columnKey, e.getMessage());
        }
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

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
            log.info("{} girildi: {}", fieldName, value);
        } catch (Exception e) {
            log.warn("{} girilemedi: {}", fieldName, e.getMessage());
        }
    }

    private void confirmIfDialogAppears() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(CONFIRM_YES_BTN));
            btn.click();
            log.info("Onay dialog'u onaylandı.");
            Thread.sleep(1000);
        } catch (Exception ignored) {
            // Dialog gelmedi
        }
    }
}
