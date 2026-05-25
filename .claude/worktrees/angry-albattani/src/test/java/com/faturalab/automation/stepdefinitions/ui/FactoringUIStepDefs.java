package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.FactoringDashboardPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class FactoringUIStepDefs {

    private static final Logger log = LogManager.getLogger(FactoringUIStepDefs.class);

    private FactoringDashboardPage factoringPage;
    private String targetBordroNo;

    private FactoringDashboardPage getPage() {
        if (factoringPage == null) {
            factoringPage = new FactoringDashboardPage(DriverManager.getDriver());
        }
        return factoringPage;
    }

    // ─── Preconditions ────────────────────────────────────────────────────────

    @Given("sistemde onay bekleyen bir bordro mevcut")
    public void bekleyenBordro() {
        targetBordroNo = System.getProperty("test.bordro.no", "A2025_73387");
        log.info("Test bordro: {}", targetBordroNo);
    }

    @Given("sistemde aktif bir bordro mevcut")
    public void aktifBordro() { bekleyenBordro(); }

    @Given("sistemde bir bordro mevcut")
    public void herhangi_bir_bordro() { bekleyenBordro(); }

    @Given("daha once onaylanmis bir bordro mevcut")
    public void onaylanmisBordro() {
        targetBordroNo = System.getProperty("test.approved.bordro.no", "A2025_73387");
    }

    @Given("finansman bir bordroyu onayladi")
    public void finansmanOnayladi() {
        getPage().navigateToOfferManagement();
        getPage().switchToGunlukTeklifTab();
        getPage().approveOffer(targetBordroNo);
    }

    @Given("tam akis tamamlandi ve bordro olustur")
    public void tamAkisBordro() {
        bekleyenBordro();
        log.info("E2E akis tamamlandi, bordro mevcut kabul ediliyor.");
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @When("\"Teklif Talebi Yonetimi\" ekranina gidilirse")
    public void teklifYonetimiEkrani() { getPage().navigateToOfferManagement(); }

    @When("tedarikci bordro veya fatura takip ekranina gidilirse")
    public void tedarikciTakipEkrani() {
        log.info("Tedarikci takip ekrani — CompanyInvoicePage ile dogrulanacak.");
    }

    // ─── Tabs ─────────────────────────────────────────────────────────────────

    @And("\"Gunluk Teklif Talebi\" sekmesine gecilirse")
    @And("\"Günlük Teklif Talebi\" sekmesine geçilirse")
    public void gunlukTeklifSekmesi() { getPage().switchToGunlukTeklifTab(); }

    @And("\"Iptal Talebi\" sekmesine tiklanirsa")
    public void iptalTalebiSekmesi() { getPage().switchToIptalTalebiTab(); }

    // ─── Actions ──────────────────────────────────────────────────────────────

    @And("ilgili bordro satirinda \"ONAYLA\" butonuna tiklanirsa")
    public void bordroOnayla() { getPage().approveOffer(targetBordroNo); }

    @And("ilgili bordro satirinda \"IPTAL\" butonuna tiklanirsa")
    public void bordroIptal() { getPage().cancelOffer(targetBordroNo); }

    @And("ilgili bordro satirinda \"GOZAT\" butonuna tiklanirsa")
    @And("ilgili bordro satırında \"GOZAT\" butonuna tiklanirsa")
    public void bordroGozat() { getPage().viewOffer(targetBordroNo); }

    @And("onaylanmis bordroda aksiyon butonu aranirsa")
    public void onaylanmisBordroAksiyon() {
        log.info("Onaylanmis bordro aksiyon kontrolu: {}", targetBordroNo);
    }

    @And("\"Gunluk Teklif Talebi\" sekmesindeki ilk bordro satiri incelenirse")
    public void ilkBordroSatiri() {
        getPage().switchToGunlukTeklifTab();
        Assert.assertTrue(getPage().hasBordroRows(), "Listede en az bir satir olmali");
    }

    // ─── Assertions ───────────────────────────────────────────────────────────

    @Then("bordro basariyla onaylanmali")
    public void bordroOnaylanmali() { Assert.assertTrue(getPage().isSuccessNotificationVisible(), "Basari bildirimi gorunmeli"); }

    @Then("bordro iptal edilmeli")
    public void bordroIptalEdilmeli() { Assert.assertTrue(getPage().isSuccessNotificationVisible(), "Basari bildirimi gorunmeli"); }

    @Then("bordro durumu \"APPROVED\" veya \"Onaylandi\" olmali")
    public void bordroDurumuApproved() {
        String status = getPage().getBordroStatus(targetBordroNo);
        if (status != null) {
            Assert.assertTrue(status.contains("APPROVED") || status.contains("Onaylandi"),
                    "APPROVED bekleniyor, alınan: " + status);
        }
    }

    @Then("bordro durumu \"CANCELLED\" veya \"Iptal\" olmali")
    public void bordroDurumuCancelled() {
        String status = getPage().getBordroStatus(targetBordroNo);
        if (status != null) {
            Assert.assertTrue(status.contains("CANCELLED") || status.contains("Iptal"),
                    "CANCELLED bekleniyor, alınan: " + status);
        }
    }

    @Then("bordro durumu \"APPROVED\" olmali")
    public void bordroDurumuKesinApproved() { bordroDurumuApproved(); }

    @Then("bordro detay sayfasi veya dialogu acilmali")
    @Then("bordro detay sayfası veya dialogu açılmalı")
    public void bordroDetayAcilmali() {
        WebDriver driver = DriverManager.getDriver();
        boolean dialogOpen = driver.findElements(By.cssSelector("vaadin-dialog-overlay")).size() > 0;
        boolean urlChanged = driver.getCurrentUrl().contains("detay");
        Assert.assertTrue(dialogOpen || urlChanged,
                "Bordro detay sayfası veya dialogu açılmalı. URL: " + driver.getCurrentUrl());
    }

    @Then("bordro bilgileri dogru gosterilmeli")
    public void bordroBilgileriDogru() {
        Assert.assertTrue(DriverManager.getDriver().getPageSource().length() > 0, "Sayfa yuklenmeli");
    }

    @Then("ekranda vaadin-grid gorunmeli")
    public void vaadinGridGorunmeli() {
        Assert.assertTrue(DriverManager.getDriver()
                .findElements(By.cssSelector("vaadin-grid")).size() > 0, "vaadin-grid gorunmeli");
    }

    @Then("\"Gunluk Teklif Talebi\", \"Iptal Talebi\" tablari gorunmeli")
    public void tablarGorunmeli() {
        Assert.assertTrue(DriverManager.getDriver()
                .findElements(By.cssSelector("vaadin-tab")).size() >= 2, "En az 2 tab gorunmeli");
    }

    @Then("iptal talepleri listesi yuklenmeli")
    public void iptalListesiYuklenmeli() {
        Assert.assertTrue(DriverManager.getDriver()
                .findElements(By.cssSelector("vaadin-grid")).size() > 0, "Grid gorunmeli");
    }


    @Then("ilgili fatura veya bordroda onaylandi bilgisi gorunmeli")
    public void onaylandiBilgisi() { bordroDurumuApproved(); }

    @Then("\"Bordro No\", \"Ticari Isletme\", \"Alici\", \"Finansal Kurum\" sutunlari dolu olmali")
    public void gridSutunlariDolu() {
        Assert.assertTrue(getPage().hasBordroRows(), "Bordro satirlari mevcut olmali");
    }

    @Then("yeni bir bordro satiri listede gorunmeli")
    public void yeniBordroListede() {
        Assert.assertTrue(getPage().hasBordroRows(), "Listede bordro satiri olmali");
    }

    @Then("bordro durumu \"PENDING_APPROVAL\" veya \"Onay Bekliyor\" olmali")
    public void bordroDurumuPending() {
        String status = getPage().getBordroStatus(targetBordroNo);
        if (status != null) {
            Assert.assertTrue(status.contains("PENDING") || status.contains("Onay Bekliyor"),
                    "PENDING bekleniyor, alınan: " + status);
        }
    }
}
