package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Dashboard Page Object - Login sonrası ana sayfa
 * Vaadin framework kullanıldığı için CSS selector'lar buna uygun tasarlanmıştır
 */
public class DashboardPage extends BasePageObject {
    
    // ==================== DASHBOARD LOCATORS ====================
    
    // Ana menü container - Vaadin menü yapısı (Sol menü)
    private final By MAIN_MENU_CONTAINER = By.cssSelector(".menu-area");
    
    // Dashboard başlık - Sağ taraftaki ana içerik başlığı
    private final By DASHBOARD_TITLE = By.cssSelector(".v-panel-caption");
    
    // Ana Sayfa butonu - Dashboard'ın yüklendiğini doğrular (Sol menüde)
    // NOT: "Ana <br> Sayfa" şeklinde HTML içinde <br> tag'i var
    private final By HOME_PAGE_BUTTON = By.cssSelector("div[role='button'].menu-home-button");
    private final By HOME_PAGE_BUTTON_ALT = By.cssSelector(".v-button.menu-inline-button.menu-home-button");
    private final By HOME_PAGE_BUTTON_ALT2 = By.cssSelector(".menu-home-button .v-button-caption");
    
    // Kullanıcı profil menüsü (Sol menüde üstte)
    private final By USER_PROFILE_MENU = By.cssSelector(".profile-pic");
    
    // Çıkış yapma butonu (Sol menüde alt kısım)
    private final By LOGOUT_BUTTON = By.cssSelector(".menu-logout-button");
    
    // ==================== NAVIGATION MENU LOCATORS ====================
    
    // Üst inline menü butonları (Ana Sayfa, Yönetim Paneli, Hesap Ayarları)
    private final By MANAGEMENT_PANEL_BUTTON = By.xpath("//div[@role='button']//span[contains(text(), 'Yönetim Paneli')]");
    private final By ACCOUNT_SETTINGS_BUTTON = By.xpath("//div[@role='button']//span[contains(text(), 'Hesap Ayarları')]");
    
    // Alt menü butonları (Yönetim Paneli altında açılan menüler)
    private final By REPORTS_MENU = By.xpath("//div[@role='button'][contains(@class, 'menu-button')]//span[contains(text(), 'Raporlar')]");
    private final By USERS_MENU = By.xpath("//div[@role='button'][contains(@class, 'menu-button')]//span[contains(text(), 'Kullanıcılar')]");
    private final By COMPANY_LIST_MENU = By.xpath("//div[@role='button'][contains(@class, 'menu-button')]//span[contains(text(), 'Firma Listesi')]");
    
    // Firma Listesi menü butonu - Tedarikçi yönetimi bu menü altında
    private final By FIRMA_LISTESI_MENU = By.xpath("//div[@role='button']//span[contains(text(), 'Firma Listesi')]");
    
    // Tedarikçiler TAB butonu - Firma Listesi sayfasında
    private final By TEDARIKCI_TAB = By.xpath("//button[contains(text(), 'Tedarikçiler')]");
    private final By TEDARIKCI_TAB_ALT = By.xpath("//*[contains(text(), 'Tedarikçiler') and not(contains(text(), 'Onay'))]");
    
    // Fatura Yönetimi menü
    private final By INVOICE_MANAGEMENT_MENU = By.xpath("//div[@role='button'][contains(@class, 'menu-button')]//span[contains(text(), 'Fatura')]");
    
    // Ayarlar menü
    private final By SETTINGS_MENU = By.xpath("//div[@role='button'][contains(@class, 'menu-button')]//span[contains(text(), 'Ayar')]");
    
    // ==================== DASHBOARD CONTENT LOCATORS ====================
    
    // Dashboard ana içerik alanı
    private final By DASHBOARD_CONTENT = By.cssSelector(".v-panel-content");
    
    // Bildirim alanı
    private final By NOTIFICATION_AREA = By.cssSelector(".v-Notification");
    
    // Başarı bildirimi
    private final By SUCCESS_NOTIFICATION = By.cssSelector(".v-Notification.notification-success");
    
    // Hata bildirimi
    private final By ERROR_NOTIFICATION = By.cssSelector(".v-Notification.notification-error");
    
    // Loading indicator
    private final By LOADING_INDICATOR = By.cssSelector(".v-loading-indicator");
    
    // ==================== PAGE ELEMENTS WITH FINDBY ====================
    
    @FindBy(css = ".v-panel-caption")
    private WebElement dashboardTitleElement;
    
    @FindBy(css = ".v-menubar")
    private WebElement mainMenuElement;
    
    // ==================== CONSTRUCTOR ====================
    
    public DashboardPage(WebDriver driver) {
        super(driver);
    }
    
    // ==================== PAGE VERIFICATION METHODS ====================
    
    /**
     * Dashboard sayfasının yüklendiğini doğrular
     * "Ana Sayfa" butonunun görünürlüğü ile kontrol eder
     * @return true if dashboard is loaded
     */
    public boolean isDashboardLoaded() {
        try {
            log.info("Checking if dashboard is loaded by looking for 'Ana Sayfa' button...");
            
            // Wait a bit for page to load
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Strategy 1: Try primary "Ana Sayfa" locator
            try {
                WebElement homeButton = waitForVisibility(HOME_PAGE_BUTTON, 5);
                if (homeButton != null && homeButton.isDisplayed()) {
                    log.info("✅ Dashboard loaded - 'Ana Sayfa' button found (primary locator)");
                    return true;
                }
            } catch (Exception e1) {
                log.debug("Primary 'Ana Sayfa' locator failed, trying alternative");
            }
            
            // Strategy 2: Try alternative locator
            try {
                WebElement homeButton = waitForVisibility(HOME_PAGE_BUTTON_ALT, 5);
                if (homeButton != null && homeButton.isDisplayed()) {
                    log.info("✅ Dashboard loaded - 'Ana Sayfa' button found (alternative locator)");
                    return true;
                }
            } catch (Exception e2) {
                log.debug("Alternative 'Ana Sayfa' locator failed, trying broad search");
            }
            
            // Strategy 3: Broad search for any element with "Ana Sayfa" text
            try {
                WebElement homeButton = driver.findElement(HOME_PAGE_BUTTON_ALT2);
                if (homeButton != null && homeButton.isDisplayed()) {
                    log.info("✅ Dashboard loaded - 'Ana Sayfa' button found (broad search)");
                    return true;
                }
            } catch (Exception e3) {
                log.debug("Broad 'Ana Sayfa' search failed");
            }
            
            // Fallback: Check if main menu is visible
            try {
                WebElement mainMenu = driver.findElement(MAIN_MENU_CONTAINER);
                if (mainMenu != null && mainMenu.isDisplayed()) {
                    log.info("✅ Dashboard loaded - Main menu is visible");
                    return true;
                }
            } catch (Exception e4) {
                log.debug("Main menu check failed");
            }
            
            log.warn("❌ Dashboard not loaded - 'Ana Sayfa' button not found");
            return false;
            
        } catch (Exception e) {
            log.error("❌ Dashboard not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Ana menünün görünür olduğunu kontrol eder
     * @return true if main menu is visible
     */
    public boolean isMainMenuVisible() {
        try {
            WebElement mainMenu = waitForVisibility(MAIN_MENU_CONTAINER, 5);
            boolean isVisible = mainMenu.isDisplayed();
            log.info("Main menu visible: {}", isVisible);
            return isVisible;
        } catch (Exception e) {
            log.warn("Main menu not visible: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Dashboard sayfasının title'ını alır
     * @return Dashboard page title
     */
    public String getDashboardTitle() {
        try {
            WebElement titleElement = waitForVisibility(DASHBOARD_TITLE, 5);
            String title = titleElement.getText();
            log.info("Dashboard title: {}", title);
            return title;
        } catch (Exception e) {
            log.error("Could not get dashboard title: {}", e.getMessage());
            return "";
        }
    }
    
    // ==================== NAVIGATION METHODS ====================
    
    /**
     * Tedarikçi Yönetimi sayfasına gider
     * NOT: Tedarikçi yönetimi "Firma Listesi" menüsü altında bir TAB'dır
     * @return SupplierManagementPage instance
     */
    public SupplierManagementPage navigateToSupplierManagement() {
        try {
            // Önce ana menünün yüklenmesini bekle
            waitForVisibility(MAIN_MENU_CONTAINER, 10);
            log.info("Navigating to Supplier Management via Firma Listesi menu");
            
            // 1. Adım: Firma Listesi menüsüne tıkla
            WebElement firmaListesiMenu = waitForElementToBeClickable(FIRMA_LISTESI_MENU);
            firmaListesiMenu.click();
            log.info("Clicked on 'Firma Listesi' menu");
            
            // Sayfa yüklenmesini bekle
            Thread.sleep(2000);
            
            // 2. Adım: Tedarikçiler tab'ına tıkla
            WebElement tedarikciTab = null;
            try {
                tedarikciTab = waitForElementToBeClickable(TEDARIKCI_TAB);
                log.info("Found 'Tedarikçiler' tab using primary locator");
            } catch (Exception e) {
                // Alternatif locator'ı dene
                log.warn("Primary Tedarikçiler tab locator failed, trying alternative");
                tedarikciTab = waitForElementToBeClickable(TEDARIKCI_TAB_ALT);
                log.info("Found 'Tedarikçiler' tab using alternative locator");
            }
            
            tedarikciTab.click();
            log.info("Clicked on 'Tedarikçiler' tab");
            
            // Tab içeriğinin yüklenmesini bekle
            Thread.sleep(2000);
            
            return new SupplierManagementPage(driver);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted: {}", e.getMessage());
            throw new RuntimeException("Navigation interrupted", e);
        } catch (Exception e) {
            log.error("Failed to navigate to Supplier Management: {}", e.getMessage());
            throw new RuntimeException("Could not navigate to Supplier Management page", e);
        }
    }
    
    /**
     * Fatura Yönetimi sayfasına gider
     */
    public void navigateToInvoiceManagement() {
        WebElement invoiceMenu = waitForElementToBeClickable(INVOICE_MANAGEMENT_MENU);
        invoiceMenu.click();
        log.info("Navigated to Invoice Management");
        waitForPageLoad();
    }
    
    /**
     * Raporlar sayfasına gider
     */
    public void navigateToReports() {
        WebElement reportsMenu = waitForElementToBeClickable(REPORTS_MENU);
        reportsMenu.click();
        log.info("Navigated to Reports");
        waitForPageLoad();
    }
    
    /**
     * Ayarlar sayfasına gider
     */
    public void navigateToSettings() {
        WebElement settingsMenu = waitForElementToBeClickable(SETTINGS_MENU);
        settingsMenu.click();
        log.info("Navigated to Settings");
        waitForPageLoad();
    }
    
    // ==================== USER ACTIONS ====================
    
    /**
     * Kullanıcı profil menüsünü açar
     */
    public void openUserProfileMenu() {
        WebElement profileMenu = waitForElementToBeClickable(USER_PROFILE_MENU);
        profileMenu.click();
        log.info("Opened user profile menu");
    }
    
    /**
     * Sistemden çıkış yapar
     * @return HomePage instance
     */
    public HomePage logout() {
        try {
            openUserProfileMenu();
            WebElement logoutBtn = waitForElementToBeClickable(LOGOUT_BUTTON);
            logoutBtn.click();
            log.info("Logged out successfully");
            
            // Login sayfasına dönüş için bekle
            waitForPageLoad();
            
            return new HomePage(driver);
            
        } catch (Exception e) {
            log.error("Failed to logout: {}", e.getMessage());
            throw new RuntimeException("Logout failed", e);
        }
    }
    
    // ==================== NOTIFICATION METHODS ====================
    
    /**
     * Başarı bildiriminin görünüp görünmediğini kontrol eder
     * @return true if success notification is visible
     */
    public boolean isSuccessNotificationVisible() {
        try {
            WebElement notification = waitForVisibility(SUCCESS_NOTIFICATION, 3);
            boolean isVisible = notification.isDisplayed();
            log.info("Success notification visible: {}", isVisible);
            return isVisible;
        } catch (Exception e) {
            log.debug("Success notification not visible: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Hata bildiriminin görünüp görünmediğini kontrol eder
     * @return true if error notification is visible
     */
    public boolean isErrorNotificationVisible() {
        try {
            WebElement notification = waitForVisibility(ERROR_NOTIFICATION, 3);
            boolean isVisible = notification.isDisplayed();
            log.info("Error notification visible: {}", isVisible);
            return isVisible;
        } catch (Exception e) {
            log.debug("Error notification not visible: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Bildirim mesajını alır
     * @return Notification message text
     */
    public String getNotificationMessage() {
        try {
            WebElement notification = waitForVisibility(NOTIFICATION_AREA, 5);
            String message = notification.getText();
            log.info("Notification message: {}", message);
            return message;
        } catch (Exception e) {
            log.warn("Could not get notification message: {}", e.getMessage());
            return "";
        }
    }
    
    // ==================== LOADING METHODS ====================
    
    /**
     * Sayfa yüklenmesinin tamamlanmasını bekler
     * Ana Sayfa butonunun görünür olmasını bekler
     */
    public void waitForDashboardLoad() {
        try {
            // Loading indicator'ın kaybolmasını bekle (varsa)
            try {
                wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_INDICATOR));
                log.debug("Loading indicator disappeared");
            } catch (Exception e) {
                log.debug("Loading indicator not found or already gone");
            }
            
            // Ana Sayfa butonunun görünmesini bekle (daha güvenilir)
            try {
                waitForVisibility(HOME_PAGE_BUTTON, 15);
                log.info("Dashboard loading completed - 'Ana Sayfa' button is visible");
            } catch (Exception e) {
                log.warn("Ana Sayfa button not found, trying main menu container");
                // Fallback: Main menu container'ı bekle
                waitForVisibility(MAIN_MENU_CONTAINER, 10);
                log.info("Dashboard loading completed - Main menu is visible");
            }
            
        } catch (Exception e) {
            log.warn("Dashboard loading timeout: {}", e.getMessage());
        }
    }
}


