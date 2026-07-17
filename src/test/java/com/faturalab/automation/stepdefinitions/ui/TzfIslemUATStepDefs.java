package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.context.RoleSessionManager;
import com.faturalab.automation.context.RoleSessionManager.Role;
import com.faturalab.automation.context.TzfScenarioContext;
import com.faturalab.automation.context.TzfScenarioContext.TzfInvoice;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.AdminReportsPage;
import com.faturalab.automation.pages.BuyerBulkUploadPage;
import com.faturalab.automation.pages.CompanyInvoicePage;
import com.faturalab.automation.pages.CompanyQuickOfferPage;
import com.faturalab.automation.utils.TzfInvoiceExcelGenerator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.io.File;

/**
 * TZF-001 — TZF işlemi uçtan uca akışı step tanımları.
 *
 * Rol geçişleri RoleSessionManager'ın filtreli impersonation'ı ile yapılır
 * (Kullanıcılar → sekme → kolon filtresi → GİT → Evet). Test verisi
 * TzfScenarioContext üzerinden adımlar arasında taşınır.
 */
public class TzfIslemUATStepDefs {

    private static final Logger log = LogManager.getLogger(TzfIslemUATStepDefs.class);

    private BuyerBulkUploadPage buyerUploadPage;
    private CompanyInvoicePage companyInvoicePage;
    private CompanyQuickOfferPage offerPage;
    private AdminReportsPage reportsPage;

    private BuyerBulkUploadPage getBuyerUploadPage() {
        if (buyerUploadPage == null) {
            buyerUploadPage = new BuyerBulkUploadPage(DriverManager.getDriver());
        }
        return buyerUploadPage;
    }

    private CompanyInvoicePage getCompanyInvoicePage() {
        if (companyInvoicePage == null) {
            companyInvoicePage = new CompanyInvoicePage(DriverManager.getDriver());
        }
        return companyInvoicePage;
    }

    private CompanyQuickOfferPage getOfferPage() {
        if (offerPage == null) {
            offerPage = new CompanyQuickOfferPage(DriverManager.getDriver());
        }
        return offerPage;
    }

    private AdminReportsPage getReportsPage() {
        if (reportsPage == null) {
            reportsPage = new AdminReportsPage(DriverManager.getDriver());
        }
        return reportsPage;
    }

    // ─── Test verisi ──────────────────────────────────────────────────────────

    @Given("TZF senaryosu için {int} adet E-Fatura içeren Excel hazırlanır")
    public void tzfExcelHazirlanir(int adet) {
        String supplierName = ConfigReader.getProperty("tzf.supplier.name");
        String supplierVkn = ConfigReader.getProperty("tzf.supplier.vkn");
        Assert.assertNotNull(supplierName, "tzf.supplier.name config'te tanımlı olmalı");
        Assert.assertNotNull(supplierVkn, "tzf.supplier.vkn config'te tanımlı olmalı");

        String path = TzfInvoiceExcelGenerator.generate(supplierName, supplierVkn, adet);
        Assert.assertTrue(new File(path).exists(), "Üretilen Excel dosyası bulunamadı: " + path);
        Assert.assertEquals(TzfScenarioContext.getInvoices().size(), adet,
                "Context'teki fatura sayısı istenen adetle eşleşmeli");
        log.info("TZF Excel hazır: {} — faturalar: {}", path,
                TzfScenarioContext.getInvoices().stream().map(i -> i.invoiceNo).toArray());
    }

    // ─── Rol geçişleri ────────────────────────────────────────────────────────

    @When("admin TZF alıcı kullanıcısına geçiş yapar")
    public void tzfAliciKullanicisinaGec() {
        impersonateTzfUser(Role.BUYER, "tzf.buyer.impersonate.identifier");
    }

    @When("admin TZF tedarikçi kullanıcısına geçiş yapar")
    public void tzfTedarikciKullanicisinaGec() {
        impersonateTzfUser(Role.COMPANY, "tzf.company.impersonate.identifier");
    }

    /**
     * loginAs her seferinde cookie'leri silip admin login + filtreli GİT yapar;
     * senaryodaki "log out ol → admin ile giriş yap → kullanıcıya git" adımlarının
     * tamamını karşılar.
     */
    private void impersonateTzfUser(Role role, String identifierKey) {
        String identifier = ConfigReader.getProperty(identifierKey);
        Assert.assertNotNull(identifier, identifierKey + " config'te tanımlı olmalı");
        RoleSessionManager.clearSession(role);
        RoleSessionManager.loginAs(DriverManager.getDriver(), role, identifier,
                ConfigReader.getProperty("admin.password"));
        log.info("[TZF] {} kullanıcısına geçildi: {}", role.getDisplayName(), identifier);
    }

    // ─── Alıcı: fatura yükleme ───────────────────────────────────────────────

    @And("alıcı ekranında hazırlanan Excel ile faturalar yüklenir")
    public void aliciExcelIleFaturaYukler() {
        String excelPath = TzfScenarioContext.getExcelPath();
        Assert.assertNotNull(excelPath, "Excel yolu context'te olmalı — önce Excel hazırlama adımı koşmalı");

        BuyerBulkUploadPage page = getBuyerUploadPage();
        Assert.assertTrue(page.openUploadDialog(), "Alıcı fatura yükleme dialogu açılamadı");
        page.selectExcelTabIfPresent();
        page.uploadExcel(excelPath);
        page.clickYukle();
    }

    @Then("faturaların başarıyla yüklendiği doğrulanır")
    public void faturaYuklemeDogrulanir() {
        boolean success = getBuyerUploadPage().waitForUploadSuccess(40);
        Assert.assertTrue(success,
                "Fatura yükleme başarı bildirimi görülmedi (veya hata bildirimi geldi)");
    }

    // ─── Tedarikçi: liste doğrulama + teklif akışı ───────────────────────────

    @Then("yüklenen faturalar tedarikçi listesinde görünmeli")
    public void faturalarTedarikciListesindeGorunmeli() {
        CompanyInvoicePage page = getCompanyInvoicePage();
        page.navigateToInvoiceList();

        // Liste virtual scroll kullanır — bizim satırlar render edilmemiş olabilir.
        // Fatura No kolon filtresiyle grid'i sadece bizim faturalara indir.
        java.util.List<String> invoiceNos = TzfScenarioContext.getInvoices().stream()
                .map(i -> i.invoiceNo)
                .collect(java.util.stream.Collectors.toList());
        boolean filtered = com.faturalab.automation.utils.VaadinGridFilterHelper
                .applyOnlyValues(DriverManager.getDriver(), "Fatura No", invoiceNos);
        Assert.assertTrue(filtered,
                "Fatura No filtresi uygulanamadı — faturalar listede yok olabilir: " + invoiceNos);

        for (TzfInvoice inv : TzfScenarioContext.getInvoices()) {
            String row = page.getInvoiceRowText(inv.invoiceNo);
            Assert.assertNotNull(row,
                    "Fatura tedarikçi listesinde bulunamadı: " + inv.invoiceNo);
            log.info("[TZF] Fatura listede doğrulandı: {}", inv.invoiceNo);
        }
    }

    @When("yüklenen faturalardan biri için teklif alınır")
    public void faturaIcinTeklifAlinir() {
        TzfInvoice first = TzfScenarioContext.getInvoices().get(0);
        TzfScenarioContext.setOfferedInvoiceNo(first.invoiceNo);
        // TEKLİF AL + modal açılışı retry ile (grid re-render flake'i — #5798 fix).
        Assert.assertTrue(getOfferPage().clickTeklifAlAndWaitModal(first.invoiceNo, 3),
                "TEKLİF AL sonrası teklif modalı açılmadı: " + first.invoiceNo);
    }

    @And("teklif modalı onaylanır ve işlemdekiler sayfasına yönlenilir")
    public void teklifModaliOnaylanir() {
        CompanyQuickOfferPage page = getOfferPage();
        Assert.assertTrue(page.isModalOpen(), "Teklif modalı açılmadı");
        Assert.assertTrue(page.confirmTeklifAlInModal(), "Modal içindeki 'Teklif Al' tıklanamadı");
        Assert.assertTrue(page.waitForIslemdekilerPage(25),
                "İşlemdekiler sayfasına yönlenme gerçekleşmedi");
    }

    @And("işlemdekiler sayfasında ilk teklif kabul edilir ve onaylanır")
    public void islemdekilerdeTeklifKabulEdilir() {
        // Kabul + onay + taze bordro doğrulaması retry ile (#5798 fix).
        // Taze bordro toast'ı gelmezse kabul commit olmamıştır (auction WAITING) → null → FAIL.
        String bordroNo = getOfferPage().acceptOfferWithRetryAndCapture(3);
        TzfScenarioContext.setBordroNo(bordroNo);
        Assert.assertNotNull(bordroNo,
                "Teklif kabulü commit olmadı — kabul sonrası taze bordro toast'ı gelmedi "
                + "(auction WAITING kalmış olabilir).");
        log.info("[TZF] Kabul commit oldu, taze bordro: {}", bordroNo);
    }

    @Then("bordro numarası yakalanır")
    public void bordroNumarasiYakalanir() {
        // Bordro önceki adımda taze toast'tan yakalandı; burada yalnızca doğrulanır.
        String bordroNo = TzfScenarioContext.getBordroNo();
        Assert.assertNotNull(bordroNo, "Kabul onayı sonrası bordro numarası yakalanamadı");
        log.info("[TZF] Bordro no doğrulandı: {}", bordroNo);
    }

    // ─── Admin: günlük işlemler doğrulaması ──────────────────────────────────

    @When("admin olarak günlük işlemler raporuna gidilir")
    public void adminGunlukIslemlereGidilir() {
        RoleSessionManager.clearSession(Role.ADMIN);
        RoleSessionManager.loginAsDefault(DriverManager.getDriver(), Role.ADMIN);
        AdminReportsPage page = getReportsPage();
        page.navigateToRaporlar();
        page.navigateToGunlukIslemler();
        Assert.assertTrue(page.isGridVisible(), "Günlük işlemler grid'i görüntülenemedi");
    }

    @Then("oluşturulan işlem günlük işlemler listesinde görünmeli")
    public void islemGunlukIslemlerdeGorunmeli() {
        AdminReportsPage page = getReportsPage();
        String bordroNo = TzfScenarioContext.getBordroNo();
        if (bordroNo != null && page.isTextVisibleInGrid(bordroNo)) {
            log.info("[TZF] Günlük işlemlerde bordro no doğrulandı: {}", bordroNo);
            return;
        }
        // Bordro no görünmüyorsa tedarikçi adıyla dolaylı doğrulama
        String supplierName = ConfigReader.getProperty("tzf.supplier.name");
        boolean bySupplier = supplierName != null && page.isTextVisibleInGrid(supplierName);
        Assert.assertTrue(bySupplier,
                "İşlem günlük işlemler listesinde bulunamadı (bordro: " + bordroNo
                        + ", tedarikçi: " + supplierName + ")");
        log.info("[TZF] Günlük işlemlerde tedarikçi adıyla doğrulandı: {}", supplierName);
    }
}
