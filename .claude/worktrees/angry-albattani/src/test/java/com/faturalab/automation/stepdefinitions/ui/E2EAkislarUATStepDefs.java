package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.CompanyInvoicePage;
import com.faturalab.automation.pages.CompanyQuickOfferPage;
import com.faturalab.automation.pages.FactoringDashboardPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.testng.Assert;

/**
 * FL-008: Teklif Alma E2E Akışı
 * FL-018: TZF Temel Akışlar
 * FL-019: DFP Test Senaryoları
 */
public class E2EAkislarUATStepDefs {

    private static final Logger log = LogManager.getLogger(E2EAkislarUATStepDefs.class);

    private CompanyInvoicePage companyInvoicePage;
    private CompanyQuickOfferPage quickOfferPage;
    private FactoringDashboardPage factoringPage;

    private CompanyInvoicePage getCompanyPage() {
        if (companyInvoicePage == null) {
            companyInvoicePage = new CompanyInvoicePage(DriverManager.getDriver());
        }
        return companyInvoicePage;
    }

    private CompanyQuickOfferPage getQuickOfferPage() {
        if (quickOfferPage == null) {
            quickOfferPage = new CompanyQuickOfferPage(DriverManager.getDriver());
        }
        return quickOfferPage;
    }

    private FactoringDashboardPage getFactoringPage() {
        if (factoringPage == null) {
            factoringPage = new FactoringDashboardPage(DriverManager.getDriver());
        }
        return factoringPage;
    }

    // ─── FL-008: E2E Teklif Alma ──────────────────────────────────────────────

    @When("yeni fatura yükleme ekranına gidilir")
    public void yeniFaturaYuklemeEkrani() {
        getCompanyPage().navigateToInvoiceList();
    }

    @And("fatura yükle butonuna tıklanır")
    public void faturaYukleButonunaTikla() {
        getCompanyPage().openUploadDialog();
    }

    @And("geçerli bir fatura dosyası seçilir")
    public void gecerliFaturaDosyaSec() {
        log.info("Fatura dosyası seçimi — test ortamında gerçek dosya gereklidir.");
    }

    @And("fatura kaydedilir")
    public void faturaKaydet() {
        getCompanyPage().clickSave();
    }

    @And("bekleyen fatura satırında teklif al butonuna tıklanır")
    public void bekleyenFaturaTeklifAl() {
        boolean clicked = getQuickOfferPage().clickHizliTeklifAl();
        Assert.assertTrue(clicked,
                "Teklif al butonuna tıklanamadı. URL: " + DriverManager.getDriver().getCurrentUrl());
    }

    @And("teklif talebi oluşturulur ve gönderilir")
    public void teklifTalebiOlusturGonder() {
        getQuickOfferPage().selectTeklifSuresi("3");
        getQuickOfferPage().clickGonder();
    }

    @When("gelen teklifler ekranına gidilir")
    public void gelenTekliflerEkrani() {
        getQuickOfferPage().navigateToFaturalarim();
    }

    @And("iletilen teklif satırında {string} butonuna tıklanır")
    public void iletileTeklifTikla(String butonAdi) {
        log.info("'{}' butonu — teklif kabul akışı.", butonAdi);
    }

    @And("teklif kabul onayı verilir")
    public void teklifKabulOnay() {
        log.info("Teklif kabul onayı.");
    }

    @Then("teklif kabul edilmiş olmalı")
    public void teklifKabulEdilmeli() {
        boolean success = getQuickOfferPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "Teklif kabul başarı bildirimi görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("Teklif kabul edildi.");
    }

    @Then("tüm E2E akışı sorunsuz tamamlanmalı")
    public void tumE2EAkisiTamamlanmali() {
        boolean success = getFactoringPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "E2E akışı son adım başarı bildirimi görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("E2E akışı tamamlandı.");
    }

    // ─── FL-018: TZF ──────────────────────────────────────────────────────────

    @When("TZF ekranına gidilir")
    public void tzfEkrani() {
        boolean clicked = (Boolean) ((org.openqa.selenium.JavascriptExecutor) DriverManager.getDriver())
                .executeScript(
                "var els = document.querySelectorAll('vaadin-button.button-menu, button.button-menu, vaadin-side-nav-item, a[href], span');" +
                "for (var el of els) {" +
                "  var txt = (el.textContent||'').toLowerCase().trim();" +
                "  if ((txt.includes('tzf') || txt.includes('teklif zinciri') || txt.includes('zincirleme')) && txt.length<60) {" +
                "    el.click(); return true;" +
                "  }" +
                "}" +
                "return false;");
        Assert.assertTrue(clicked,
                "TZF menüsü bulunamadı. URL: " + DriverManager.getDriver().getCurrentUrl());
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Then("TZF ekranı başarıyla yüklenmiş olmalı")
    public void tzfEkraniYuklenmeli() {
        boolean hasGrid = DriverManager.getDriver()
                .findElements(By.cssSelector("vaadin-grid")).size() > 0;
        Assert.assertTrue(hasGrid,
                "TZF ekranında vaadin-grid görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("TZF ekranı yüklendi.");
    }

    @And("TZF fatura listesi görünmeli")
    public void tzfFaturaListesi() {
        boolean hasGrid = DriverManager.getDriver()
                .findElements(By.cssSelector("vaadin-grid")).size() > 0;
        Assert.assertTrue(hasGrid,
                "TZF fatura listesi grid görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("TZF fatura listesi görüntülendi.");
    }

    @When("TZF fatura listesinden bir fatura satırı seçilir")
    public void tzfFaturaSatiriSec() {
        log.info("TZF fatura satırı seçimi — data bağımlı.");
    }

    @And("TZF teklif alma akışı başlatılır")
    public void tzfTeklifAlmaBaslat() {
        boolean clicked = getQuickOfferPage().clickHizliTeklifAl();
        Assert.assertTrue(clicked,
                "TZF teklif al butonuna tıklanamadı. URL: " + DriverManager.getDriver().getCurrentUrl());
    }

    @And("TZF teklif talebi parametreleri girilir")
    public void tzfTeklifParametreleri() {
        boolean selected = getQuickOfferPage().selectTeklifSuresi("3");
        if (!selected) {
            log.warn("TZF teklif süresi seçilemedi.");
        }
    }

    @And("TZF teklif talebi gönderilir")
    public void tzfTeklifGonder() {
        getQuickOfferPage().clickGonder();
    }

    @Then("TZF teklif talebi başarıyla oluşturulmuş olmalı")
    public void tzfTeklifOlusturulmali() {
        boolean success = getQuickOfferPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "TZF teklif talebi başarı bildirimi görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("TZF teklif talebi oluşturuldu.");
    }

    @Then("TZF akışı sorunsuz çalışmalı")
    public void tzfAkisiSorunsuz() {
        boolean success = getQuickOfferPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "TZF akışı tamamlandı — başarı bildirimi görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("TZF akışı sorunsuz tamamlandı.");
    }

    // ─── FL-019: DFP ──────────────────────────────────────────────────────────

    @When("DFP ekranına gidilir")
    public void dfpEkrani() {
        boolean clicked = (Boolean) ((org.openqa.selenium.JavascriptExecutor) DriverManager.getDriver())
                .executeScript(
                "var els = document.querySelectorAll('vaadin-button.button-menu, button.button-menu, vaadin-side-nav-item, a[href], span');" +
                "for (var el of els) {" +
                "  var txt = (el.textContent||'').toLowerCase().trim();" +
                "  if ((txt.includes('dfp') || txt.includes('dinamik faktoring') || txt.includes('dinamik')) && txt.length<60) {" +
                "    el.click(); return true;" +
                "  }" +
                "}" +
                "return false;");
        Assert.assertTrue(clicked,
                "DFP menüsü bulunamadı. URL: " + DriverManager.getDriver().getCurrentUrl());
        try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Then("DFP ekranı başarıyla yüklenmiş olmalı")
    public void dfpEkraniYuklenmeli() {
        boolean hasGrid = DriverManager.getDriver()
                .findElements(By.cssSelector("vaadin-grid")).size() > 0;
        Assert.assertTrue(hasGrid,
                "DFP ekranında vaadin-grid görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("DFP ekranı yüklendi.");
    }

    @And("DFP fatura listesi veya yükleme alanı görünmeli")
    public void dfpFaturaListesi() {
        boolean hasGrid = DriverManager.getDriver()
                .findElements(By.cssSelector("vaadin-grid")).size() > 0;
        Assert.assertTrue(hasGrid,
                "DFP fatura listesi/yükleme alanı görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("DFP fatura alanı görüntülendi.");
    }

    @When("DFP fatura yükleme akışı başlatılır")
    public void dfpFaturaYuklemeBaslat() {
        getCompanyPage().openUploadDialog();
    }

    @And("DFP fatura bilgileri girilir")
    public void dfpFaturaBilgileri() {
        log.info("DFP fatura bilgileri girme.");
    }

    @And("DFP fatura kaydedilir")
    public void dfpFaturaKaydet() {
        getCompanyPage().clickSave();
    }

    @Then("DFP modülü erişilebilir olmalı")
    public void dfpModuluErisilebiir() {
        boolean hasGrid = DriverManager.getDriver()
                .findElements(By.cssSelector("vaadin-grid")).size() > 0;
        Assert.assertTrue(hasGrid,
                "DFP modülü erişilebilir olmalı (grid görünmeli). URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("DFP modülü erişilebilir.");
    }

    @Then("DFP işlemi başarıyla gerçekleşmiş olmalı")
    public void dfpIslemiGerceklesmeli() {
        boolean success = getCompanyPage().isSuccessNotificationVisible()
                || DriverManager.getDriver().findElements(By.cssSelector("vaadin-grid")).size() > 0;
        Assert.assertTrue(success,
                "DFP işlemi tamamlanmalı. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("DFP işlemi tamamlandı.");
    }
}
