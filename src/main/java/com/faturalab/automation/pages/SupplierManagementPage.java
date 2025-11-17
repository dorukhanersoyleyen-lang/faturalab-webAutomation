package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Map;

/**
 * Supplier Management Page Object - Tedarikçi Yönetimi sayfası
 * Vaadin framework kullanıldığı için CSS selector'lar buna uygun tasarlanmıştır
 */
public class SupplierManagementPage extends BasePageObject {
    
    // ==================== PAGE TITLE & HEADER LOCATORS ====================
    
    // Sayfa başlığı - "TEDARİKÇİ ŞİRKETLER" veya sadece tab seçili olduğunu kontrol et
    private final By PAGE_TITLE = By.xpath("//*[contains(text(), 'TEDARİKÇİ ŞİRKETLER')]");
    
    // Sayfa başlık alternatifi - Tedarikçiler tab'ının seçili olduğunu kontrol et
    private final By PAGE_TITLE_ALT = By.xpath("//button[contains(text(), 'Tedarikçiler') and contains(@class, 'focus')]");
    
    // Tedarikçiler tabının herhangi bir durumda olduğunu kontrol et
    private final By TEDARIKCI_TAB_PRESENT = By.xpath("//button[contains(text(), 'Tedarikçiler')]");
    
    // ==================== ACTION BUTTON LOCATORS ====================
    
    // Yeni Tedarikçi Ekle butonu
    private final By ADD_NEW_SUPPLIER_BUTTON = By.cssSelector(".v-button:contains('Yeni'), .v-button:contains('Ekle')");
    
    // Alternatif Yeni Tedarikçi Ekle butonu
    private final By ADD_NEW_SUPPLIER_BUTTON_ALT = By.xpath("//div[contains(@class, 'v-button') and (contains(text(), 'Yeni') or contains(text(), 'Ekle'))]");
    
    // Kaydet butonu
    private final By SAVE_BUTTON = By.cssSelector(".v-button:contains('Kaydet')");
    
    // İptal butonu
    private final By CANCEL_BUTTON = By.cssSelector(".v-button:contains('İptal')");
    
    // Düzenle butonu
    private final By EDIT_BUTTON = By.cssSelector(".v-button:contains('Düzenle')");
    
    // Sil butonu
    private final By DELETE_BUTTON = By.cssSelector(".v-button:contains('Sil')");
    
    // ==================== FORM FIELD LOCATORS ====================
    
    // Firma Adı input
    private final By COMPANY_NAME_INPUT = By.cssSelector("input[class*='v-textfield']:not([type='password'])");
    
    // VKN (Vergi Kimlik Numarası) input
    private final By TAX_NUMBER_INPUT = By.cssSelector("input[class*='v-textfield'][maxlength='11']");
    
    // Kullanıcı Adı input
    private final By USERNAME_INPUT = By.cssSelector("input[class*='v-textfield']:nth-of-type(3)");
    
    // E-posta input
    private final By EMAIL_INPUT = By.cssSelector("input[type='email'], input[class*='v-textfield'][placeholder*='mail']");
    
    // Ürün Tipi dropdown
    private final By PRODUCT_TYPE_DROPDOWN = By.cssSelector(".v-filterselect, .v-combobox");
    
    // Ürün Tipi dropdown input
    private final By PRODUCT_TYPE_INPUT = By.cssSelector(".v-filterselect-input, .v-combobox-input");
    
    // Ürün Tipi dropdown button
    private final By PRODUCT_TYPE_BUTTON = By.cssSelector(".v-filterselect-button, .v-combobox-button");
    
    // ==================== SEARCH & FILTER LOCATORS ====================
    
    // Arama kutusu
    private final By SEARCH_INPUT = By.cssSelector("input[placeholder*='Ara'], input[class*='search']");
    
    // Arama butonu
    private final By SEARCH_BUTTON = By.cssSelector(".v-button:contains('Ara')");
    
    // Filtre temizle butonu
    private final By CLEAR_FILTER_BUTTON = By.cssSelector(".v-button:contains('Temizle')");
    
    // ==================== TABLE & LIST LOCATORS ====================
    
    // Tedarikçi tablosu
    private final By SUPPLIER_TABLE = By.cssSelector(".v-table, .v-grid");
    
    // Tablo satırları
    private final By TABLE_ROWS = By.cssSelector(".v-table-row, .v-grid-row");
    
    // Tablo başlıkları
    private final By TABLE_HEADERS = By.cssSelector(".v-table-header, .v-grid-header");
    
    // Tedarikçi listesi (alternatif)
    private final By SUPPLIER_LIST = By.cssSelector(".v-panel-content .v-verticallayout");
    
    // ==================== DROPDOWN OPTIONS LOCATORS ====================
    
    // Ürün tipi seçenekleri
    private final By PRODUCT_TYPE_OPTIONS = By.cssSelector(".v-filterselect-suggestmenu .v-filterselect-item");
    
    // Tedarikçi Finansmanı seçeneği
    private final By SUPPLIER_FINANCING_OPTION = By.xpath("//div[contains(@class, 'v-filterselect-item') and contains(text(), 'Tedarikçi Finansmanı')]");
    
    // ==================== NOTIFICATION LOCATORS ====================
    
    // Başarı bildirimi
    private final By SUCCESS_NOTIFICATION = By.cssSelector(".v-Notification.notification-success");
    
    // Hata bildirimi
    private final By ERROR_NOTIFICATION = By.cssSelector(".v-Notification.notification-error");
    
    // Bildirim mesajı
    private final By NOTIFICATION_MESSAGE = By.cssSelector(".v-Notification-description");
    
    // ==================== CONFIRMATION DIALOG LOCATORS ====================
    
    // Onay dialog'u
    private final By CONFIRMATION_DIALOG = By.cssSelector(".v-window");
    
    // Onay butonu
    private final By CONFIRM_BUTTON = By.cssSelector(".v-window .v-button:contains('Evet'), .v-window .v-button:contains('Onayla')");
    
    // Reddet butonu
    private final By REJECT_BUTTON = By.cssSelector(".v-window .v-button:contains('Hayır'), .v-window .v-button:contains('İptal')");
    
    // ==================== FINDBY ELEMENTS ====================
    
    @FindBy(css = ".v-panel-caption")
    private WebElement pageTitleElement;
    
    @FindBy(css = ".v-table, .v-grid")
    private WebElement supplierTableElement;
    
    // ==================== CONSTRUCTOR ====================
    
    public SupplierManagementPage(WebDriver driver) {
        super(driver);
    }
    
    // ==================== PAGE VERIFICATION METHODS ====================
    
    /**
     * Tedarikçi yönetimi sayfasının yüklendiğini doğrular
     * Tedarikçiler tab'ının varlığını ve/veya "TEDARİKÇİ ŞİRKETLER" başlığını kontrol eder
     * @return true if page is loaded
     */
    public boolean isSupplierManagementPageLoaded() {
        try {
            log.info("Checking if Supplier Management page is loaded...");
            
            // Strategy 1: "TEDARİKÇİ ŞİRKETLER" başlığını ara
            try {
                WebElement pageTitle = waitForVisibility(PAGE_TITLE, 5);
                if (pageTitle != null && pageTitle.isDisplayed()) {
                    log.info("✅ Supplier Management page loaded - Title 'TEDARİKÇİ ŞİRKETLER' found");
                    return true;
                }
            } catch (Exception e) {
                log.debug("'TEDARİKÇİ ŞİRKETLER' title not found, trying tab check");
            }
            
            // Strategy 2: Tedarikçiler tab'ının varlığını kontrol et
            try {
                WebElement tedarikciTab = driver.findElement(TEDARIKCI_TAB_PRESENT);
                if (tedarikciTab != null && tedarikciTab.isDisplayed()) {
                    log.info("✅ Supplier Management page loaded - 'Tedarikçiler' tab is present");
                    return true;
                }
            } catch (Exception e) {
                log.debug("Tedarikçiler tab not found");
            }
            
            // Strategy 3: Tedarikçi tablosunu kontrol et
            try {
                if (isSupplierTableVisible()) {
                    log.info("✅ Supplier Management page loaded - Supplier table is visible");
                    return true;
                }
            } catch (Exception e) {
                log.debug("Supplier table check failed");
            }
            
            log.warn("❌ Supplier Management page not loaded - All checks failed");
            return false;
            
        } catch (Exception e) {
            log.error("❌ Supplier Management page not loaded: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Tedarikçi tablosunun görünür olduğunu kontrol eder
     * @return true if supplier table is visible
     */
    public boolean isSupplierTableVisible() {
        try {
            WebElement table = waitForVisibility(SUPPLIER_TABLE, 5);
            boolean isVisible = table.isDisplayed();
            log.info("Supplier table visible: {}", isVisible);
            return isVisible;
        } catch (Exception e) {
            log.warn("Supplier table not visible: {}", e.getMessage());
            return false;
        }
    }
    
    // ==================== SUPPLIER CREATION METHODS ====================
    
    /**
     * Yeni Tedarikçi Ekle butonuna tıklar
     */
    public void clickAddNewSupplierButton() {
        try {
            WebElement addButton = null;
            try {
                addButton = waitForElementToBeClickable(ADD_NEW_SUPPLIER_BUTTON);
            } catch (Exception e) {
                log.warn("Primary add button locator failed, trying alternative");
                addButton = waitForElementToBeClickable(ADD_NEW_SUPPLIER_BUTTON_ALT);
            }
            
            addButton.click();
            log.info("Clicked Add New Supplier button");
            
            // Form yüklenmesini bekle
            waitForPageLoad();
            
        } catch (Exception e) {
            log.error("Failed to click Add New Supplier button: {}", e.getMessage());
            throw new RuntimeException("Could not click Add New Supplier button", e);
        }
    }
    
    /**
     * Tedarikçi bilgilerini form alanlarına girer
     * @param supplierData Tedarikçi bilgileri map'i
     */
    public void enterSupplierInformation(Map<String, String> supplierData) {
        try {
            // Firma Adı
            if (supplierData.containsKey("Firma Adı")) {
                WebElement companyNameField = waitForElementToBeClickable(COMPANY_NAME_INPUT);
                companyNameField.clear();
                companyNameField.sendKeys(supplierData.get("Firma Adı"));
                log.info("Entered company name: {}", supplierData.get("Firma Adı"));
            }
            
            // VKN
            if (supplierData.containsKey("VKN")) {
                WebElement taxNumberField = waitForElementToBeClickable(TAX_NUMBER_INPUT);
                taxNumberField.clear();
                taxNumberField.sendKeys(supplierData.get("VKN"));
                log.info("Entered tax number: {}", supplierData.get("VKN"));
            }
            
            // Kullanıcı Adı
            if (supplierData.containsKey("Kullanıcı Adı")) {
                WebElement usernameField = waitForElementToBeClickable(USERNAME_INPUT);
                usernameField.clear();
                usernameField.sendKeys(supplierData.get("Kullanıcı Adı"));
                log.info("Entered username: {}", supplierData.get("Kullanıcı Adı"));
            }
            
            // E-posta
            if (supplierData.containsKey("E-posta")) {
                WebElement emailField = waitForElementToBeClickable(EMAIL_INPUT);
                emailField.clear();
                emailField.sendKeys(supplierData.get("E-posta"));
                log.info("Entered email: {}", supplierData.get("E-posta"));
            }
            
        } catch (Exception e) {
            log.error("Failed to enter supplier information: {}", e.getMessage());
            throw new RuntimeException("Could not enter supplier information", e);
        }
    }
    
    /**
     * Ürün tipini seçer
     * @param productType Seçilecek ürün tipi
     */
    public void selectProductType(String productType) {
        try {
            // Dropdown'ı aç
            WebElement dropdownButton = waitForElementToBeClickable(PRODUCT_TYPE_BUTTON);
            dropdownButton.click();
            log.info("Opened product type dropdown");
            
            // Seçeneklerin yüklenmesini bekle
            waitForVisibility(PRODUCT_TYPE_OPTIONS, 5);
            
            // Belirli seçeneği bul ve tıkla
            if ("Tedarikçi Finansmanı".equals(productType)) {
                WebElement option = waitForElementToBeClickable(SUPPLIER_FINANCING_OPTION);
                option.click();
                log.info("Selected product type: {}", productType);
            } else {
                // Genel seçenek arama
                List<WebElement> options = driver.findElements(PRODUCT_TYPE_OPTIONS);
                for (WebElement option : options) {
                    if (option.getText().contains(productType)) {
                        option.click();
                        log.info("Selected product type: {}", productType);
                        return;
                    }
                }
                log.warn("Product type not found: {}", productType);
            }
            
        } catch (Exception e) {
            log.error("Failed to select product type: {}", e.getMessage());
            throw new RuntimeException("Could not select product type: " + productType, e);
        }
    }
    
    /**
     * Ürün tipi seçmez (boş bırakır)
     */
    public void leaveProductTypeEmpty() {
        log.info("Product type left empty as requested");
        // Ürün tipi seçimi yapılmaz
    }
    
    /**
     * Kaydet butonuna tıklar
     */
    public void clickSaveButton() {
        try {
            WebElement saveButton = waitForElementToBeClickable(SAVE_BUTTON);
            saveButton.click();
            log.info("Clicked Save button");
            
            // Kaydetme işleminin tamamlanmasını bekle
            waitForPageLoad();
            
        } catch (Exception e) {
            log.error("Failed to click Save button: {}", e.getMessage());
            throw new RuntimeException("Could not click Save button", e);
        }
    }
    
    // ==================== SUPPLIER SEARCH & VERIFICATION METHODS ====================
    
    /**
     * Tedarikçi arar
     * @param searchTerm Arama terimi
     */
    public void searchSupplier(String searchTerm) {
        try {
            WebElement searchField = waitForElementToBeClickable(SEARCH_INPUT);
            searchField.clear();
            searchField.sendKeys(searchTerm);
            log.info("Entered search term: {}", searchTerm);
            
            // Arama butonuna tıkla (varsa)
            try {
                WebElement searchButton = waitForElementToBeClickable(SEARCH_BUTTON);
                searchButton.click();
                log.info("Clicked search button");
            } catch (Exception e) {
                log.debug("Search button not found, search might be auto-triggered");
            }
            
            // Arama sonuçlarının yüklenmesini bekle
            waitForPageLoad();
            
        } catch (Exception e) {
            log.error("Failed to search supplier: {}", e.getMessage());
            throw new RuntimeException("Could not search supplier: " + searchTerm, e);
        }
    }
    
    /**
     * Tedarikçinin listede görünüp görünmediğini kontrol eder
     * @param supplierName Tedarikçi adı
     * @return true if supplier is found in list
     */
    public boolean isSupplierVisibleInList(String supplierName) {
        try {
            // Tablo satırlarını kontrol et
            List<WebElement> rows = driver.findElements(TABLE_ROWS);
            for (WebElement row : rows) {
                if (row.getText().contains(supplierName)) {
                    log.info("Supplier found in list: {}", supplierName);
                    return true;
                }
            }
            
            log.info("Supplier not found in list: {}", supplierName);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking supplier visibility: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Tedarikçinin ürün tipini kontrol eder
     * @param supplierName Tedarikçi adı
     * @param expectedProductType Beklenen ürün tipi
     * @return true if product type matches
     */
    public boolean verifySupplierProductType(String supplierName, String expectedProductType) {
        try {
            List<WebElement> rows = driver.findElements(TABLE_ROWS);
            for (WebElement row : rows) {
                if (row.getText().contains(supplierName)) {
                    String rowText = row.getText();
                    if (expectedProductType == null || expectedProductType.isEmpty()) {
                        // Ürün tipi boş olmalı
                        boolean isEmpty = !rowText.contains("Tedarikçi Finansmanı");
                        log.info("Supplier {} product type is empty: {}", supplierName, isEmpty);
                        return isEmpty;
                    } else {
                        // Belirli ürün tipi olmalı
                        boolean matches = rowText.contains(expectedProductType);
                        log.info("Supplier {} product type matches {}: {}", supplierName, expectedProductType, matches);
                        return matches;
                    }
                }
            }
            
            log.warn("Supplier not found for product type verification: {}", supplierName);
            return false;
            
        } catch (Exception e) {
            log.error("Error verifying supplier product type: {}", e.getMessage());
            return false;
        }
    }
    
    // ==================== SUPPLIER EDIT & UPDATE METHODS ====================
    
    /**
     * Belirli tedarikçiyi bulur ve düzenle butonuna tıklar
     * @param supplierName Düzenlenecek tedarikçi adı
     */
    public void editSupplier(String supplierName) {
        try {
            // Önce tedarikçiyi ara
            searchSupplier(supplierName);
            
            // Tedarikçi satırını bul
            List<WebElement> rows = driver.findElements(TABLE_ROWS);
            for (WebElement row : rows) {
                if (row.getText().contains(supplierName)) {
                    // Satırdaki düzenle butonunu bul ve tıkla
                    WebElement editButton = row.findElement(EDIT_BUTTON);
                    editButton.click();
                    log.info("Clicked edit button for supplier: {}", supplierName);
                    
                    // Form yüklenmesini bekle
                    waitForPageLoad();
                    return;
                }
            }
            
            throw new RuntimeException("Supplier not found for editing: " + supplierName);
            
        } catch (Exception e) {
            log.error("Failed to edit supplier: {}", e.getMessage());
            throw new RuntimeException("Could not edit supplier: " + supplierName, e);
        }
    }
    
    /**
     * Ürün tipini günceller
     * @param newProductType Yeni ürün tipi
     */
    public void updateProductType(String newProductType) {
        selectProductType(newProductType);
        log.info("Updated product type to: {}", newProductType);
    }
    
    // ==================== SUPPLIER DELETE METHODS ====================
    
    /**
     * Tedarikçiyi siler
     * @param supplierName Silinecek tedarikçi adı
     */
    public void deleteSupplier(String supplierName) {
        try {
            // Önce tedarikçiyi ara
            searchSupplier(supplierName);
            
            // Tedarikçi satırını bul
            List<WebElement> rows = driver.findElements(TABLE_ROWS);
            for (WebElement row : rows) {
                if (row.getText().contains(supplierName)) {
                    // Satırdaki sil butonunu bul ve tıkla
                    WebElement deleteButton = row.findElement(DELETE_BUTTON);
                    deleteButton.click();
                    log.info("Clicked delete button for supplier: {}", supplierName);
                    return;
                }
            }
            
            throw new RuntimeException("Supplier not found for deletion: " + supplierName);
            
        } catch (Exception e) {
            log.error("Failed to delete supplier: {}", e.getMessage());
            throw new RuntimeException("Could not delete supplier: " + supplierName, e);
        }
    }
    
    /**
     * Silme işlemini onaylar
     */
    public void confirmDeletion() {
        try {
            WebElement confirmButton = waitForElementToBeClickable(CONFIRM_BUTTON);
            confirmButton.click();
            log.info("Confirmed deletion");
            
            // İşlemin tamamlanmasını bekle
            waitForPageLoad();
            
        } catch (Exception e) {
            log.error("Failed to confirm deletion: {}", e.getMessage());
            throw new RuntimeException("Could not confirm deletion", e);
        }
    }
    
    // ==================== NOTIFICATION METHODS ====================
    
    /**
     * Başarı bildiriminin görünüp görünmediğini kontrol eder
     * @return true if success notification is visible
     */
    public boolean isSuccessNotificationVisible() {
        try {
            WebElement notification = waitForVisibility(SUCCESS_NOTIFICATION, 5);
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
            WebElement notification = waitForVisibility(ERROR_NOTIFICATION, 5);
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
            WebElement notification = waitForVisibility(NOTIFICATION_MESSAGE, 5);
            String message = notification.getText();
            log.info("Notification message: {}", message);
            return message;
        } catch (Exception e) {
            log.warn("Could not get notification message: {}", e.getMessage());
            return "";
        }
    }
    
    // ==================== VALIDATION METHODS ====================
    
    /**
     * Tedarikçinin başarıyla kaydedildiğini doğrular
     * @param supplierName Kaydedilen tedarikçi adı
     * @return true if supplier was saved successfully
     */
    public boolean isSupplierSavedSuccessfully(String supplierName) {
        // Başarı bildirimi kontrolü
        boolean hasSuccessNotification = isSuccessNotificationVisible();
        
        // Tedarikçinin listede görünme kontrolü
        boolean isVisibleInList = isSupplierVisibleInList(supplierName);
        
        boolean isSaved = hasSuccessNotification && isVisibleInList;
        log.info("Supplier {} saved successfully: {}", supplierName, isSaved);
        
        return isSaved;
    }
    
    /**
     * Tedarikçinin başarıyla güncellendiğini doğrular
     * @param supplierName Güncellenen tedarikçi adı
     * @return true if supplier was updated successfully
     */
    public boolean isSupplierUpdatedSuccessfully(String supplierName) {
        return isSupplierSavedSuccessfully(supplierName);
    }
    
    /**
     * Tedarikçinin başarıyla silindiğini doğrular
     * @param supplierName Silinen tedarikçi adı
     * @return true if supplier was deleted successfully
     */
    public boolean isSupplierDeletedSuccessfully(String supplierName) {
        // Başarı bildirimi kontrolü
        boolean hasSuccessNotification = isSuccessNotificationVisible();
        
        // Tedarikçinin listede görünmeme kontrolü
        boolean isNotVisibleInList = !isSupplierVisibleInList(supplierName);
        
        boolean isDeleted = hasSuccessNotification && isNotVisibleInList;
        log.info("Supplier {} deleted successfully: {}", supplierName, isDeleted);
        
        return isDeleted;
    }
}
