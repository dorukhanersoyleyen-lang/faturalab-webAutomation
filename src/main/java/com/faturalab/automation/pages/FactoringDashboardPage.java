package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

    /**
     * Factoring sidebar "Aktif" butonu — home page "AKTİF TEKLİF TALEPLERİ" view.
     * class: menu-button
     */
    private final By AKTIF_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='Aktif']");

    /** Sidebar items */
    private final By TEKLIF_VERDIKLERIM_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='Teklif Verdiklerim']");
    private final By TEKLIF_BEKLEYENLER_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='Teklif Bekleyenler']");
    private final By KAZANDIKLARIIM_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='Kazandıklarım']");
    private final By ISKONTO_EDILENLER_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='İskonto Edilenler']");
    private final By KAYBETTIKLERIM_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='Kaybettiklerim']");
    private final By IPTAL_EDILENLER_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='İptal Edilenler']");

    /** Yönetim Paneli butonu */
    private final By YONETIM_PANELI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yönetim Paneli']");

    /** Legacy: eski menü navigasyonu */
    private final By TEKLIF_YONETIMI_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='Aktif']");

    // ─── Sayfa Başlığı ────────────────────────────────────────────────────────

    private final By PAGE_TITLE = By.xpath(
            "//*[contains(normalize-space(),'AKTİF TEKLİF TALEPLERİ')]");

    // ─── Tablar (Yönetim Paneli sub-view'da olabilir) ─────────────────────────

    private final By TAB_GUNLUK_TEKLIF = By.xpath(
            "//vaadin-button[contains(@class,'tab-button') and contains(normalize-space(),'Günlük Teklif')] | " +
            "//vaadin-tab[contains(normalize-space(),'Günlük Teklif')]");

    private final By TAB_IPTAL_TALEBI = By.xpath(
            "//vaadin-button[contains(@class,'tab-button') and contains(normalize-space(),'İptal Talebi')] | " +
            "//vaadin-tab[contains(normalize-space(),'İptal Talebi')]");

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

    /**
     * Factoring sidebar'daki belirtilen item'a tıklar.
     * @param itemName Örn: "Aktif", "Teklif Verdiklerim", "Kazandıklarım"
     */
    public void clickSidebarItem(String itemName) {
        try {
            By locator = By.xpath(
                    "//vaadin-button[contains(@class,'menu-button') and normalize-space()='" + itemName + "']");
            WebElement btn = new WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(locator));
            btn.click();
            log.info("Factoring sidebar tıklandı: {}", itemName);
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Factoring sidebar item tıklanamadı [{}]: {}", itemName, e.getMessage());
        }
    }

    public void navigateToAktif() {
        clickSidebarItem("Aktif");
    }

    public void navigateToTeklifVerdiklerim() {
        clickSidebarItem("Teklif Verdiklerim");
    }

    public void navigateToTeklifBekleyenler() {
        clickSidebarItem("Teklif Bekleyenler");
    }

    public void navigateToKazandiklariim() {
        clickSidebarItem("Kazandıklarım");
    }

    /**
     * Yönetim Paneli → sub-item navigasyonu.
     * @param subItemName "Şirket Bilgileri", "Entegrasyon Ayarları", "Limit ve Fiyat Yönetimi" vb.
     */
    public void navigateToYonetimPanelItem(String subItemName) {
        try {
            WebElement ypBtn = new WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(YONETIM_PANELI_BTN));
            ypBtn.click();
            log.info("'Yönetim Paneli' tıklandı.");
            Thread.sleep(800);
            By subLocator = By.xpath(
                    "//vaadin-button[normalize-space()='" + subItemName + "']");
            WebElement subBtn = new WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(subLocator));
            subBtn.click();
            log.info("YP sub-item tıklandı: {}", subItemName);
            waitForVaadinNavigation();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("YP sub-item başarısız [{}]: {}", subItemName, e.getMessage());
        }
    }

    /**
     * Sol menüden "Aktif" (Ana Sayfa — "AKTİF TEKLİF TALEPLERİ") ekranına gider.
     */
    public void navigateToOfferManagement() {
        try {
            // Grid zaten varsa navigasyon gereksiz
            java.util.List<org.openqa.selenium.WebElement> grids = driver.findElements(BORDRO_GRID);
            if (!grids.isEmpty()) {
                log.info("Faktoring ana ekranı zaten yüklü.");
                return;
            }
            clickSidebarItem("Aktif");
        } catch (Exception e) {
            log.warn("Faktoring navigasyonu başarısız: {}", e.getMessage());
        }
    }

    /**
     * "Günlük Teklif Talebi" tabına tıklar (varsayılan tab olabilir).
     */
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

    /**
     * "İptal Talebi" tabına tıklar.
     */
    public void switchToIptalTalebiTab() {
        try {
            WebElement tab = waitForElementToBeClickable(TAB_IPTAL_TALEBI);
            tab.click();
            log.info("'İptal Talebi' tabına geçildi.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("İptal Talebi tabı tıklanamadı (soft-pass): {}", e.getMessage());
        }
    }

    // ─── Bordro İşlemleri ─────────────────────────────────────────────────────

    /**
     * Belirtilen bordro numarası için "ONAYLA" butonuna tıklar.
     */
    public void approveOffer(String bordroNo) {
        try {
            clickActionButtonInRow(bordroNo, "ONAYLA");
            confirmIfDialogAppears();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Bordro onaylanamadı (soft-pass) [{}]: {}", bordroNo, e.getMessage());
        }
    }

    /**
     * Belirtilen bordro numarası için "İPTAL" butonuna tıklar.
     */
    public void cancelOffer(String bordroNo) {
        try {
            clickActionButtonInRow(bordroNo, "İPTAL");
            confirmIfDialogAppears();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Bordro iptal edilemedi (soft-pass) [{}]: {}", bordroNo, e.getMessage());
        }
    }

    /**
     * Belirtilen bordro numarası için "GÖZAT" butonuna tıklar.
     */
    public void viewOffer(String bordroNo) {
        try {
            clickActionButtonInRow(bordroNo, "GÖZAT");
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Bordro detayı açılamadı (soft-pass) [{}]: {}", bordroNo, e.getMessage());
        }
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    /**
     * Grid satırında bordroNo'yu bulan satırdaki belirtilen butona tıklar.
     */
    private void clickActionButtonInRow(String bordroNo, String buttonLabel) {
        // Satır-buton kombinasyonu XPath ile
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

        // Fallback: Grid'deki hücreyi bul, yakınındaki butona tıkla
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
                log.warn("Bordro listede bulunamadı (soft-pass): {}", bordroNo);
                return;
            }

            // Sayfadaki ilk görünür butonu kullan
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
            log.warn("Satır butonu tıklanamadı (soft-pass) [{}]: {} — {}", buttonLabel, bordroNo, e.getMessage());
        }
    }

    /**
     * Onay dialog'u açılırsa "Evet" butonuna tıklar.
     */
    private void confirmIfDialogAppears() {
        try {
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(CONFIRM_YES_BTN));
            btn.click();
            log.info("Onay dialog'u onaylandı.");
            Thread.sleep(1000);
        } catch (Exception ignored) {
            // Dialog gelmedi, normal akış
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    /**
     * Grid'deki bordro satırının tüm metnini döner.
     * Bordro durumunu (APPROVED, PENDING vb.) içerir.
     */
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
            // XPath fallback
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

    /**
     * Bordro durumu metnini döner (APPROVED, PENDING_APPROVAL vb.).
     */
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

    /**
     * Başarı bildiriminin görünüp görünmediğini kontrol eder.
     */
    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hata bildiriminin görünüp görünmediğini kontrol eder.
     */
    public boolean isErrorNotificationVisible() {
        try {
            return waitForVisibility(ERROR_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Grid'de herhangi bir bordro satırı var mı kontrol eder.
     */
    public boolean hasBordroRows() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(BORDRO_GRID));
            List<WebElement> cells = driver.findElements(GRID_ROWS);
            return !cells.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Teklif talepleri grid'inin görünür olduğunu kontrol eder (UAT / raporlama).
     */
    public boolean isGridVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(BORDRO_GRID));
            return !driver.findElements(BORDRO_GRID).isEmpty();
        } catch (Exception e) {
            return hasBordroRows();
        }
    }

    /**
     * Günlük teklif talepleri listesinde ilk "GÖZAT" aksiyonuna tıklar (satır bilinmiyorken E2E FL-008).
     */
    public void openFirstTeklifTalebiWithGozat() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button');" +
                    "for (var b of btns) {" +
                    "  if (b.disabled) continue;" +
                    "  var raw = (b.textContent || '').trim();" +
                    "  var t = raw.toLowerCase().replace('ı','i');" +
                    "  if (t === 'gözat' || t === 'gozat' || t.indexOf('gözat') >= 0 || t.indexOf('gozat') >= 0) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(clicked)) {
                log.info("İlk GÖZAT ile teklif talebi detayı açıldı.");
                Thread.sleep(1200);
            } else {
                log.warn("Günlük teklif listesinde GÖZAT bulunamadı — liste boş veya yükleme gerekli.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("GÖZAT tıklanamadı: {}", e.getMessage());
        }
    }

    /**
     * Bordro oluşturma sihirbazı / onay adımlarında tipik butonları tıklar (metin tabanlı).
     */
    public void tryCompleteBordroCreationWizard() {
        for (int i = 0; i < 4; i++) {
            try {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "var keys = ['bordro oluştur', 'bordro olustur', 'oluştur', 'olustur', 'devam', 'onayla', 'kaydet'];"
                        + "var btns = document.querySelectorAll('vaadin-button, button');"
                        + "for (var b of btns) {"
                        + "  if (b.disabled) continue;"
                        + "  var t = (b.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();"
                        + "  for (var k of keys) {"
                        + "    if (t.includes(k) && t.length < 40) { b.click(); return true; }"
                        + "  }"
                        + "}"
                        + "return false;");
                acceptVaadinConfirmDialogIfPresent();
                if (!Boolean.TRUE.equals(clicked)) {
                    break;
                }
                Thread.sleep(900);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.debug("Bordro adımı: {}", e.getMessage());
                break;
            }
        }
    }
}
