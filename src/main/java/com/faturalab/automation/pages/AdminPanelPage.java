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

/**
 * Admin — Günlük İşlemler / Fatura & Bordro listesi ekranı.
 *
 * Admin ana sayfasında "GÜNLÜK İŞLEMLER" tab'ı varsayılan olarak açıktır.
 * Filtreler: Fatura No, VKN, Bordro No — DOM'da label yoktur, span etiketiyle tanımlanır.
 *
 * Admin sidebar (Ana Sayfa görünümünde):
 *   Raporlar | Kullanıcılar | Firma Listesi | Kullanıcı Hareketleri |
 *   Entegrasyon Hareketleri | Bildirim Hareketleri | Firma Engelle - İzin Ver | Döviz Kurları
 *
 * Admin Yönetim Paneli sub-menu:
 *   Teklif Talebi Yönetimi | Limit ve Fiyat Yönetimi | DFP Tedarikçi Limitleri |
 *   Sözleşmeler | Bildirimler | Roller | Kriter Tabloları |
 *   Engellenen Mail Sunucuları | Tatiller | Uygulama Ayarları
 */
public class AdminPanelPage extends BasePageObject {

    // ─── Tab Butonları ────────────────────────────────────────────────────────

    /** Ana tab butonları (GÜNLÜK İŞLEMLER, vb.) */
    private final By TAB_GUNLUK_ISLEMLER = By.xpath(
            "//vaadin-button[contains(@class,'tab-button') and normalize-space()='GÜNLÜK İŞLEMLER']");

    // ─── Filtre Alanları (span label bazlı) ──────────────────────────────────

    /**
     * Fatura No filtresi — DOM'da "Fatura No:" span'ının yanındaki vaadin-text-field.
     * Vaadin 24: following-sibling traversal kullanılır.
     */
    private final By FATURA_NO_INPUT = By.xpath(
            "//span[normalize-space()='Fatura No:']/following-sibling::vaadin-text-field//input | " +
            "//label[normalize-space()='Fatura No:']/following-sibling::vaadin-text-field//input");

    private final By VKN_INPUT = By.xpath(
            "//span[normalize-space()='VKN:']/following-sibling::vaadin-text-field//input | " +
            "//label[normalize-space()='VKN:']/following-sibling::vaadin-text-field//input");

    /** Bordro No filtresi — {@link #resolveBordroNoFilterInput()} ile listeden son input seçilir. */
    private final By BORDRO_NO_INPUT_CANDIDATES = By.xpath(
            "//span[normalize-space()='Bordro No:']/following-sibling::vaadin-text-field//input | " +
            "//label[normalize-space()='Bordro No:']/following-sibling::vaadin-text-field//input");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By INVOICE_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_ROWS    = By.cssSelector("vaadin-grid-cell-content");

    // ─── Satır Butonları ──────────────────────────────────────────────────────

    private final By GOZAT_BTN = By.xpath(
            "//vaadin-button[normalize-space()='GÖZAT']");

    private final By ONAYLA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='ONAYLA'] | " +
            "//vaadin-button[normalize-space()='Onayla']");

    private final By REDDET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='REDDET'] | " +
            "//vaadin-button[normalize-space()='Reddet']");

    // ─── Onay Dialog ──────────────────────────────────────────────────────────

    private final By CONFIRM_YES_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam']");

    private final By CONFIRM_NO_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Hayır'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='İptal']");

    // ─── Admin Sidebar Butonları ───────────────────────────────────────────────

    /** Yönetim Paneli ana butonu (sub-menu açar) */
    private final By YONETIM_PANELI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yönetim Paneli']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector("vaadin-notification-container");
    private final By ERROR_NOTIFICATION   = By.cssSelector("vaadin-notification-container");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public AdminPanelPage(WebDriver driver) {
        super(driver);
    }

    // ─── Tab Navigasyon ───────────────────────────────────────────────────────

    /**
     * Verilen isimde tab butonuna tıklar.
     * @param tabName Tab üzerindeki metin (örn: "GÜNLÜK İŞLEMLER")
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

    public void clickGunlukIslemlerTab() {
        clickTab("GÜNLÜK İŞLEMLER");
    }

    // ─── Admin Sidebar Navigasyon ─────────────────────────────────────────────

    /**
     * Sol menüdeki sidebar butonuna tıklar (Ana Sayfa görünümü).
     * Örnek: clickSidebarItem("Raporlar"), clickSidebarItem("Firma Listesi")
     */
    public void clickSidebarItem(String itemName) {
        try {
            By locator = By.xpath(
                    "//vaadin-button[normalize-space()='" + itemName + "'] | " +
                    "//vaadin-button[contains(@class,'menu-button') and normalize-space()='" + itemName + "']");
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(locator));
            btn.click();
            log.info("Sidebar item tıklandı: {}", itemName);
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Sidebar item tıklanamadı [{}]: {}", itemName, e.getMessage());
        }
    }

    /**
     * Yönetim Paneli butonuna tıklar (sub-menu açar), sonra sub-item'a tıklar.
     * @param subItemName Alt menü item adı (örn: "Kriter Tabloları")
     */
    public void navigateToYonetimPanelItem(String subItemName) {
        try {
            // Önce Yönetim Paneli butonuna tıkla
            WebElement ypBtn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(YONETIM_PANELI_BTN));
            ypBtn.click();
            log.info("'Yönetim Paneli' butonuna tıklandı.");
            Thread.sleep(800);

            // Sonra sub-item'a tıkla
            By subLocator = By.xpath(
                    "//vaadin-button[normalize-space()='" + subItemName + "'] | " +
                    "//vaadin-button[contains(normalize-space(),'" + subItemName + "')]");
            WebElement subBtn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(subLocator));
            subBtn.click();
            log.info("YP sub-item tıklandı: {}", subItemName);
            waitForVaadinNavigation();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("YP sub-item navigasyonu başarısız [{}]: {}", subItemName, e.getMessage());
        }
    }

    // ─── Navigasyon (legacy) ──────────────────────────────────────────────────

    /**
     * Admin ana sayfasından fatura/bordro listesine gider.
     * Admin home'da "GÜNLÜK İŞLEMLER" tab'ı zaten varsayılan açıktır.
     * Bu method sadece tab'a tıklar (gerekirse).
     */
    public void navigateToInvoiceManagement() {
        try {
            // Grid varsa zaten doğru sayfadayız
            List<WebElement> grids = driver.findElements(INVOICE_GRID);
            if (!grids.isEmpty()) {
                log.info("Fatura listesi zaten yüklü.");
                return;
            }
            // GÜNLÜK İŞLEMLER tab'ını dene
            clickGunlukIslemlerTab();
        } catch (Exception e) {
            log.warn("navigateToInvoiceManagement: {}", e.getMessage());
        }
    }

    /** @deprecated {@link #navigateToInvoiceManagement()} kullanın */
    public void navigateToFaturaList() {
        navigateToInvoiceManagement();
    }

    // ─── Fatura Arama ─────────────────────────────────────────────────────────

    /**
     * Fatura No filtre alanına fatura numarası yazar.
     */
    public void searchByFaturaNo(String faturaNo) {
        try {
            WebElement input = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(FATURA_NO_INPUT));
            input.clear();
            input.sendKeys(faturaNo);
            input.sendKeys(Keys.ENTER);
            log.info("Fatura No ile arama yapıldı: {}", faturaNo);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Fatura No arama başarısız [{}]: {}", faturaNo, e.getMessage());
        }
    }

    public void searchByVkn(String vkn) {
        try {
            WebElement input = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(VKN_INPUT));
            input.clear();
            input.sendKeys(vkn);
            input.sendKeys(Keys.ENTER);
            log.info("VKN ile arama yapıldı: {}", vkn);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("VKN arama başarısız [{}]: {}", vkn, e.getMessage());
        }
    }

    /**
     * "Bordro No:" etiketli tüm filtre inputlarından sonuncusunu döndürür (ilk alan fatura no vb. olabiliyor).
     */
    private WebElement resolveBordroNoFilterInput() {
        List<WebElement> all = driver.findElements(BORDRO_NO_INPUT_CANDIDATES);
        if (all.isEmpty()) {
            return null;
        }
        WebElement input = all.get(all.size() - 1);
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(ExpectedConditions.elementToBeClickable(input));
        return input;
    }

    public void searchByBordroNo(String bordroNo) {
        try {
            WebElement input = resolveBordroNoFilterInput();
            if (input == null) {
                log.warn("Bordro No filtre inputu bulunamadı.");
                return;
            }
            input.clear();
            input.sendKeys(bordroNo);
            input.sendKeys(Keys.ENTER);
            log.info("Bordro No ile arama yapıldı (son filtre alanı): {}", bordroNo);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Bordro No arama başarısız [{}]: {}", bordroNo, e.getMessage());
        }
    }

    /** Fatura numarasını arar (legacy compat). */
    public void searchInvoice(String invoiceNo) {
        searchByFaturaNo(invoiceNo);
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
        }
    }

    public void clickGozatForRow(String rowIdentifier) {
        try {
            By rowBtn = By.xpath(
                    "//*[contains(normalize-space(),'" + rowIdentifier + "')]" +
                    "/ancestor::*[contains(@part,'row') or self::tr]" +
                    "//vaadin-button[normalize-space()='GÖZAT'] | " +
                    "//vaadin-button[normalize-space()='GÖZAT']");
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(rowBtn));
            btn.click();
            log.info("GÖZAT tıklandı: {}", rowIdentifier);
        } catch (Exception e) {
            log.warn("GÖZAT tıklanamadı [{}]: {}", rowIdentifier, e.getMessage());
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
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(rowActionBtn));
            btn.click();
            log.info("[{}] butonuna tıklandı, satır: {}", buttonLabel, invoiceNo);
        } catch (Exception ignored) {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(buttonLocator));
            btn.click();
            log.info("[{}] butonuna tıklandı (genel).", buttonLabel);
        }
    }

    private void confirmIfDialogAppears() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(3))
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
            new WebDriverWait(driver, Duration.ofSeconds(5))
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

    public boolean hasPendingInvoices() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
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
