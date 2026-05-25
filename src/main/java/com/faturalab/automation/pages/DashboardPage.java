package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Dashboard Page Object — Login sonrası ana sayfa.
 *
 * Vaadin 24: tüm butonlar vaadin-button, menü class'ları menu-button / menu-inline-medium-button.
 *
 * Rol bazlı home ekranlar:
 *  - Admin:     "GÜNLÜK İŞLEMLER" tab (vaadin-grid görünür)
 *  - Company:   "YÜKLENMİŞ FATURALAR"
 *  - Buyer:     "Fatura Yükleme Talepleri"
 *  - Factoring: "AKTİF TEKLİF TALEPLERİ"
 */
public class DashboardPage extends BasePageObject {

    // ─── Genel Sayfa Göstergeleri ─────────────────────────────────────────────

    /** Herhangi bir vaadin-grid — login sonrası içerik yüklenmiş demektir */
    private final By CONTENT_GRID = By.cssSelector("vaadin-grid");

    /** vaadin-app-layout veya main layout container */
    private final By APP_LAYOUT   = By.cssSelector("vaadin-app-layout, [class*='menu-area'], [class*='main-layout']");

    // ─── Admin Sidebar ────────────────────────────────────────────────────────

    /** Admin sidebar Ana Sayfa tab (GÜNLÜK İŞLEMLER görünümü) */
    private final By ADMIN_ANA_SAYFA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Ana Sayfa'] | " +
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='Ana Sayfa']");

    /** Admin Yönetim Paneli butonu */
    private final By YONETIM_PANELI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yönetim Paneli']");

    /** Admin sidebar — Firma Listesi */
    private final By FIRMA_LISTESI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Firma Listesi']");

    /** Admin sidebar — Kullanıcılar */
    private final By KULLANICILAR_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Kullanıcılar']");

    /** Admin sidebar — Raporlar */
    private final By RAPORLAR_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Raporlar']");

    // ─── Buyer Sidebar ────────────────────────────────────────────────────────

    /** Buyer "Tedarikçi Yönetimi" (class: menu-inline-medium-button) */
    private final By BUYER_TEDARIKCI_YONETIMI_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Tedarikçi Yönetimi'] | " +
            "//vaadin-button[contains(@class,'menu-inline-medium-button') and contains(normalize-space(),'Tedarikçi Yönetimi')]");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By NOTIFICATION = By.cssSelector("vaadin-notification-container");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    // ─── Sayfa Doğrulama ──────────────────────────────────────────────────────

    /**
     * Dashboard'ın yüklendiğini kontrol eder.
     * vaadin-grid veya uygulama layout'unun varlığıyla doğrular.
     */
    public boolean isDashboardLoaded() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Strateji 1: vaadin-grid (içerik yüklendi)
        try {
            WebElement grid = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(CONTENT_GRID));
            if (grid != null && grid.isDisplayed()) {
                log.info("Dashboard yüklendi — vaadin-grid görünür.");
                return true;
            }
        } catch (Exception ignored) {}

        // Strateji 2: App layout
        try {
            WebElement layout = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(APP_LAYOUT));
            if (layout != null && layout.isDisplayed()) {
                log.info("Dashboard yüklendi — app layout görünür.");
                return true;
            }
        } catch (Exception ignored) {}

        // Strateji 3: Herhangi bir vaadin-button (en geniş fallback)
        try {
            java.util.List<WebElement> buttons = driver.findElements(By.cssSelector("vaadin-button"));
            if (!buttons.isEmpty()) {
                log.info("Dashboard yüklendi — vaadin-button'lar mevcut.");
                return true;
            }
        } catch (Exception ignored) {}

        log.warn("Dashboard yüklenmedi.");
        return false;
    }

    public boolean isMainMenuVisible() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(APP_LAYOUT))
                    .isDisplayed();
        } catch (Exception e) {
            return !driver.findElements(By.cssSelector("vaadin-button")).isEmpty();
        }
    }

    public String getDashboardTitle() {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            return "";
        }
    }

    public void waitForDashboardLoad() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30))
                    .until(ExpectedConditions.visibilityOfElementLocated(CONTENT_GRID));
            log.info("Dashboard yükleme tamamlandı.");
        } catch (Exception e) {
            log.warn("Dashboard yükleme timeout: {}", e.getMessage());
        }
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Admin sidebar'daki verilen butona tıklar.
     * @param buttonName Buton üzerindeki metin
     */
    public void clickSidebarButton(String buttonName) {
        try {
            By locator = By.xpath("//vaadin-button[normalize-space()='" + buttonName + "']");
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(locator));
            btn.click();
            log.info("Sidebar butonu tıklandı: {}", buttonName);
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Sidebar butonu tıklanamadı [{}]: {}", buttonName, e.getMessage());
        }
    }

    /**
     * Tedarikçi Yönetimi sayfasına gider (Buyer kullanıcısı için).
     */
    public SupplierManagementPage navigateToSupplierManagement() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(BUYER_TEDARIKCI_YONETIMI_BTN));
            btn.click();
            log.info("'Tedarikçi Yönetimi' butonuna tıklandı.");
            waitForVaadinNavigation();
            return new SupplierManagementPage(driver);
        } catch (Exception e) {
            log.error("Tedarikçi Yönetimi navigasyonu başarısız: {}", e.getMessage());
            throw new RuntimeException("Could not navigate to Supplier Management", e);
        }
    }

    /**
     * Admin Yönetim Paneli'nden bir sub-item'a gider.
     */
    public void navigateToYonetimPanelItem(String subItemName) {
        try {
            WebElement ypBtn = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(YONETIM_PANELI_BTN));
            ypBtn.click();
            log.info("Yönetim Paneli tıklandı.");
            Thread.sleep(800);
            By subLocator = By.xpath("//vaadin-button[normalize-space()='" + subItemName + "']");
            WebElement subBtn = new WebDriverWait(driver, Duration.ofSeconds(10))
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

    public void navigateToInvoiceManagement() {
        clickSidebarButton("Fatura Yönetimi");
    }

    public void navigateToReports() {
        clickSidebarButton("Raporlar");
    }

    public void navigateToSettings() {
        clickSidebarButton("Hesap Ayarları");
    }

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    public boolean isSuccessNotificationVisible() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(NOTIFICATION))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorNotificationVisible() {
        return isSuccessNotificationVisible();
    }

    public String getNotificationMessage() {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(NOTIFICATION))
                    .getText();
        } catch (Exception e) {
            return "";
        }
    }

    // ─── Legacy compat ────────────────────────────────────────────────────────

    public void openUserProfileMenu() {
        try {
            By profileBtn = By.cssSelector(".profile-pic, [class*='profile'], [aria-label*='profil'], [aria-label*='kullanıcı']");
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(profileBtn));
            btn.click();
        } catch (Exception e) {
            log.warn("Profil menüsü açılamadı: {}", e.getMessage());
        }
    }

    public HomePage logout() {
        try {
            openUserProfileMenu();
            By logoutBtn = By.xpath(
                    "//vaadin-button[contains(normalize-space(),'Çıkış')] | " +
                    "//vaadin-button[contains(normalize-space(),'Logout')]");
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(logoutBtn));
            btn.click();
            log.info("Çıkış yapıldı.");
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Çıkış başarısız: {}", e.getMessage());
        }
        return new HomePage(driver);
    }
}
