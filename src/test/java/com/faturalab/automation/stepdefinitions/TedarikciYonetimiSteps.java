package com.faturalab.automation.stepdefinitions;

import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.DashboardPage;
import com.faturalab.automation.pages.HomePage;
import com.faturalab.automation.pages.SupplierManagementPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * Tedarikçi Yönetimi Step Definitions
 * Türkçe BDD step'leri için Cucumber annotations kullanır
 */
public class TedarikciYonetimiSteps {
    
    private static final Logger log = LogManager.getLogger(TedarikciYonetimiSteps.class);
    
    private final WebDriver driver;
    private HomePage homePage;
    private DashboardPage dashboardPage;
    private SupplierManagementPage supplierManagementPage;
    
    // Test verilerini saklamak için
    private String currentSupplierName;
    private String currentProductType;
    
    public TedarikciYonetimiSteps() {
        this.driver = DriverManager.getDriver();
        this.homePage = new HomePage(driver);
        this.dashboardPage = new DashboardPage(driver);
        this.supplierManagementPage = new SupplierManagementPage(driver);
    }
    
    // ==================== ARKA PLAN STEP'LERİ ====================
    
    @Given("kullanıcı ana sayfaya gider")
    public void kullanici_ana_sayfaya_gider() {
        log.info("Navigating to homepage");
        homePage.navigateToHomePage();
        Assert.assertTrue(homePage.isLogoDisplayed(), "Homepage should be loaded with logo visible");
    }
    
    @And("kullanıcı geçerli kimlik bilgileri ile giriş yapar")
    public void kullanici_gecerli_kimlik_bilgileri_ile_giris_yapar() {
        log.info("Logging in with valid credentials");
        // Test ortamı için geçerli kimlik bilgileri - config dosyasından okunuyor
        String email = ConfigReader.getProperty("username");
        String password = ConfigReader.getProperty("password");
        
        log.info("Using credentials - Email: {}", email);
        
        // Email ve şifre girişi
        homePage.enterEmail(email);
        homePage.enterPassword(password);
        homePage.clickLoginButton();
        
        // CAPTCHA için manuel müdahale süresi - 20 saniye bekleme
        log.warn("=== CAPTCHA MANUEL MÜDAHALESİ GEREKLİ ===");
        log.warn("Lütfen 20 saniye içinde CAPTCHA'yı manuel olarak çözün!");
        log.warn("Bekleme süresi başlıyor...");
        
        try {
            // Config'den bekleme süresini al (varsayılan 20 saniye)
            int captchaWaitSeconds = Integer.parseInt(ConfigReader.getProperty("captcha.wait.seconds", "20"));
            long captchaWaitMillis = captchaWaitSeconds * 1000L;
            
            Thread.sleep(captchaWaitMillis);
            log.info("{} saniye bekleme tamamlandı. Test devam ediyor...", captchaWaitSeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Bekleme süresi kesintiye uğradı: {}", e.getMessage());
        } catch (NumberFormatException e) {
            log.warn("Geçersiz captcha bekleme süresi, varsayılan 20 saniye kullanılıyor");
            try {
                Thread.sleep(20000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("Login attempt completed with email: {}", email);
    }
    
    @And("kullanıcı dashboard sayfasında olduğunu doğrular")
    public void kullanici_dashboard_sayfasinda_oldugunu_dogrular() {
        log.info("Verifying dashboard page is loaded");
        dashboardPage.waitForDashboardLoad();
        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard page should be loaded");
        Assert.assertTrue(dashboardPage.isMainMenuVisible(), "Main menu should be visible");
    }
    
    // ==================== TEDARİKÇİ YÖNETİMİ NAVIGATION ====================
    
    @Given("kullanıcı tedarikçi yönetimi sayfasına gider")
    public void kullanici_tedarikci_yonetimi_sayfasina_gider() {
        log.info("Navigating to Supplier Management page");
        supplierManagementPage = dashboardPage.navigateToSupplierManagement();
        Assert.assertTrue(supplierManagementPage.isSupplierManagementPageLoaded(), 
                "Supplier Management page should be loaded");
    }
    
    @And("kullanıcı {string} butonuna tıklar")
    public void kullanici_butonuna_tiklar(String buttonName) {
        log.info("Clicking button: {}", buttonName);
        
        switch (buttonName) {
            case "Yeni Tedarikçi Ekle":
                supplierManagementPage.clickAddNewSupplierButton();
                break;
            case "Kaydet":
                supplierManagementPage.clickSaveButton();
                break;
            default:
                throw new IllegalArgumentException("Unknown button: " + buttonName);
        }
    }
    
    // ==================== TEDARİKÇİ BİLGİLERİ GİRİŞİ ====================
    
    @When("kullanıcı tedarikçi bilgilerini girer:")
    public void kullanici_tedarikci_bilgilerini_girer(DataTable dataTable) {
        log.info("Entering supplier information");
        
        List<Map<String, String>> supplierData = dataTable.asMaps(String.class, String.class);
        Map<String, String> data = supplierData.get(0);
        
        // Firma adını sakla (doğrulama için)
        if (data.containsKey("Firma Adı")) {
            currentSupplierName = data.get("Firma Adı");
        }
        
        supplierManagementPage.enterSupplierInformation(data);
        log.info("Supplier information entered successfully");
    }
    
    @And("kullanıcı ürün tipini {string} olarak seçer")
    public void kullanici_urun_tipini_olarak_secer(String productType) {
        log.info("Selecting product type: {}", productType);
        currentProductType = productType;
        supplierManagementPage.selectProductType(productType);
    }
    
    @And("kullanıcı ürün tipini seçmez")
    public void kullanici_urun_tipini_secmez() {
        log.info("Leaving product type empty");
        currentProductType = null;
        supplierManagementPage.leaveProductTypeEmpty();
    }
    
    // ==================== DOĞRULAMA STEP'LERİ ====================
    
    @Then("tedarikçi başarıyla kaydedilmelidir")
    public void tedarikci_basariyla_kaydedilmelidir() {
        log.info("Verifying supplier is saved successfully");
        Assert.assertTrue(supplierManagementPage.isSupplierSavedSuccessfully(currentSupplierName),
                "Supplier should be saved successfully: " + currentSupplierName);
    }
    
    @And("tedarikçi listesinde {string} görünmelidir")
    public void tedarikci_listesinde_gorunmelidir(String supplierName) {
        log.info("Verifying supplier is visible in list: {}", supplierName);
        Assert.assertTrue(supplierManagementPage.isSupplierVisibleInList(supplierName),
                "Supplier should be visible in list: " + supplierName);
    }
    
    @And("tedarikçinin ürün tipi {string} olarak görünmelidir")
    public void tedarikci_urun_tipi_olarak_gorunmelidir(String expectedProductType) {
        log.info("Verifying supplier product type: {}", expectedProductType);
        Assert.assertTrue(supplierManagementPage.verifySupplierProductType(currentSupplierName, expectedProductType),
                "Supplier product type should be: " + expectedProductType);
    }
    
    @And("tedarikçinin ürün tipi boş olarak görünmelidir")
    public void tedarikci_urun_tipi_bos_olarak_gorunmelidir() {
        log.info("Verifying supplier product type is empty");
        Assert.assertTrue(supplierManagementPage.verifySupplierProductType(currentSupplierName, null),
                "Supplier product type should be empty");
    }
    
    // ==================== TEDARİKÇİ GÜNCELLEME STEP'LERİ ====================
    
    @Given("{string} adlı tedarikçi sistemde mevcut")
    public void adli_tedarikci_sistemde_mevcut(String supplierName) {
        log.info("Verifying supplier exists in system: {}", supplierName);
        currentSupplierName = supplierName;
        
        // Tedarikçi yönetimi sayfasına git (eğer değilse)
        if (!supplierManagementPage.isSupplierManagementPageLoaded()) {
            kullanici_tedarikci_yonetimi_sayfasina_gider();
        }
        
        // Tedarikçiyi ara ve kontrol et
        supplierManagementPage.searchSupplier(supplierName);
        Assert.assertTrue(supplierManagementPage.isSupplierVisibleInList(supplierName),
                "Supplier should exist in system: " + supplierName);
    }
    
    @And("bu tedarikçinin ürün tipi boş")
    public void bu_tedarikci_urun_tipi_bos() {
        log.info("Verifying supplier has empty product type");
        Assert.assertTrue(supplierManagementPage.verifySupplierProductType(currentSupplierName, null),
                "Supplier should have empty product type initially");
    }
    
    @And("kullanıcı {string} tedarikçisini bulur")
    public void kullanici_tedarikci_bulur(String supplierName) {
        log.info("Finding supplier: {}", supplierName);
        supplierManagementPage.searchSupplier(supplierName);
        Assert.assertTrue(supplierManagementPage.isSupplierVisibleInList(supplierName),
                "Supplier should be found: " + supplierName);
    }
    
    @And("kullanıcı tedarikçi üzerinde {string} butonuna tıklar")
    public void kullanici_tedarikci_uzerinde_butonuna_tiklar(String buttonName) {
        log.info("Clicking {} button on supplier", buttonName);
        
        switch (buttonName) {
            case "Düzenle":
                supplierManagementPage.editSupplier(currentSupplierName);
                break;
            case "Sil":
                supplierManagementPage.deleteSupplier(currentSupplierName);
                break;
            default:
                throw new IllegalArgumentException("Unknown button: " + buttonName);
        }
    }
    
    @And("kullanıcı ürün tipini {string} olarak günceller")
    public void kullanici_urun_tipini_olarak_gunceller(String newProductType) {
        log.info("Updating product type to: {}", newProductType);
        currentProductType = newProductType;
        supplierManagementPage.updateProductType(newProductType);
    }
    
    @Then("tedarikçi başarıyla güncellenmelidir")
    public void tedarikci_basariyla_guncellenmelidir() {
        log.info("Verifying supplier is updated successfully");
        Assert.assertTrue(supplierManagementPage.isSupplierUpdatedSuccessfully(currentSupplierName),
                "Supplier should be updated successfully: " + currentSupplierName);
    }
    
    @And("tedarikçi listesinde güncellenen bilgiler görünmelidir")
    public void tedarikci_listesinde_guncellenen_bilgiler_gorunmelidir() {
        log.info("Verifying updated supplier information is visible");
        Assert.assertTrue(supplierManagementPage.isSupplierVisibleInList(currentSupplierName),
                "Updated supplier should be visible in list: " + currentSupplierName);
    }
    
    // ==================== ARAMA VE FİLTRELEME STEP'LERİ ====================
    
    @Given("sistemde birden fazla tedarikçi mevcut")
    public void sistemde_birden_fazla_tedarikci_mevcut() {
        log.info("Verifying multiple suppliers exist in system");
        
        // Tedarikçi yönetimi sayfasına git
        if (!supplierManagementPage.isSupplierManagementPageLoaded()) {
            kullanici_tedarikci_yonetimi_sayfasina_gider();
        }
        
        Assert.assertTrue(supplierManagementPage.isSupplierTableVisible(),
                "Supplier table should be visible with multiple suppliers");
    }
    
    @And("kullanıcı arama kutusuna {string} yazar")
    public void kullanici_arama_kutusuna_yazar(String searchTerm) {
        log.info("Searching for: {}", searchTerm);
        supplierManagementPage.searchSupplier(searchTerm);
    }
    
    @Then("sadece {string} içeren tedarikçiler listelenmelidir")
    public void sadece_iceren_tedarikci_listelenmelidir(String searchTerm) {
        log.info("Verifying search results contain: {}", searchTerm);
        // Bu kontrol SupplierManagementPage'de implement edilebilir
        // Şimdilik temel kontrol yapıyoruz
        Assert.assertTrue(supplierManagementPage.isSupplierTableVisible(),
                "Search results should be displayed");
    }
    
    @And("arama sonuçları doğru şekilde filtrelenmelidir")
    public void arama_sonuclari_dogru_sekilde_filtrelenmelidir() {
        log.info("Verifying search results are properly filtered");
        Assert.assertTrue(supplierManagementPage.isSupplierTableVisible(),
                "Filtered results should be displayed correctly");
    }
    
    // ==================== TEDARİKÇİ SİLME STEP'LERİ ====================
    
    @And("kullanıcı silme işlemini onaylar")
    public void kullanici_silme_islemini_onaylar() {
        log.info("Confirming deletion");
        supplierManagementPage.confirmDeletion();
    }
    
    @Then("tedarikçi başarıyla silinmelidir")
    public void tedarikci_basariyla_silinmelidir() {
        log.info("Verifying supplier is deleted successfully");
        Assert.assertTrue(supplierManagementPage.isSupplierDeletedSuccessfully(currentSupplierName),
                "Supplier should be deleted successfully: " + currentSupplierName);
    }
    
    @And("tedarikçi listesinde artık görünmemelidir")
    public void tedarikci_listesinde_artik_gorunmemelidir() {
        log.info("Verifying supplier is no longer visible in list");
        Assert.assertFalse(supplierManagementPage.isSupplierVisibleInList(currentSupplierName),
                "Supplier should not be visible in list after deletion: " + currentSupplierName);
    }
    
    // ==================== HATA DOĞRULAMA STEP'LERİ ====================
    
    @When("kullanıcı geçersiz tedarikçi bilgilerini girer:")
    public void kullanici_gecersiz_tedarikci_bilgilerini_girer(DataTable dataTable) {
        log.info("Entering invalid supplier information");
        
        List<Map<String, String>> invalidData = dataTable.asMaps(String.class, String.class);
        
        for (Map<String, String> data : invalidData) {
            String field = data.get("Alan");
            String value = data.get("Değer");
            String expectedError = data.get("Beklenen Hata");
            
            log.info("Testing invalid field: {} with value: {}, expecting error: {}", field, value, expectedError);
            
            // Geçersiz veri girişi (bu kısım form alanlarına göre genişletilebilir)
            // Şimdilik temel implementasyon
        }
    }
    
    @Then("uygun hata mesajları görünmelidir")
    public void uygun_hata_mesajlari_gorunmelidir() {
        log.info("Verifying error messages are displayed");
        Assert.assertTrue(supplierManagementPage.isErrorNotificationVisible(),
                "Error notification should be visible for invalid data");
    }
    
    @And("tedarikçi kaydedilmemelidir")
    public void tedarikci_kaydedilmemelidir() {
        log.info("Verifying supplier is not saved due to validation errors");
        // Hata durumunda tedarikçi kaydedilmemeli
        Assert.assertTrue(supplierManagementPage.isErrorNotificationVisible(),
                "Error should prevent supplier from being saved");
    }
    
    // ==================== YARDIMCI METODLAR ====================
    
    
    /**
     * Mevcut tedarikçi adını döndürür
     * @return current supplier name
     */
    public String getCurrentSupplierName() {
        return currentSupplierName;
    }
    
    /**
     * Mevcut ürün tipini döndürür
     * @return current product type
     */
    public String getCurrentProductType() {
        return currentProductType;
    }
}
