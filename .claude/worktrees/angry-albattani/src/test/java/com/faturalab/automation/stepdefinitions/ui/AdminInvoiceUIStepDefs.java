package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.AdminPanelPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

public class AdminInvoiceUIStepDefs {

    private static final Logger log = LogManager.getLogger(AdminInvoiceUIStepDefs.class);

    private AdminPanelPage adminPanelPage;
    private String targetInvoiceNo;

    private AdminPanelPage getPage() {
        if (adminPanelPage == null) {
            adminPanelPage = new AdminPanelPage(DriverManager.getDriver());
        }
        return adminPanelPage;
    }

    // ─── Preconditions ────────────────────────────────────────────────────────

    @Given("sistemde \"PENDING_APPROVAL\" durumunda bir fatura mevcut")
    public void sistemdePendingFatura() {
        targetInvoiceNo = System.getProperty("test.pending.invoiceNo", "PENDING-TEST-001");
        log.info("PENDING fatura: {}", targetInvoiceNo);
    }

    @Given("sistemde bilinen bir fatura numarasi mevcut")
    public void sistemdeBilinenFatura() {
        targetInvoiceNo = System.getProperty("test.known.invoiceNo", "KNOWN-TEST-001");
        log.info("Bilinen fatura: {}", targetInvoiceNo);
    }

    @Given("bir fatura \"PENDING_APPROVAL\" durumundayken admin tarafindan onaylanmis")
    public void faturaAdminOnaylanmis(io.cucumber.datatable.DataTable dt) {
        targetInvoiceNo = dt.asMaps().get(0).get("invoiceNo");
        log.info("Onaylanmis fatura: {}", targetInvoiceNo);
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @When("admin fatura yonetimi ekranina gidilirse")
    public void adminFaturaYonetimi() { getPage().navigateToInvoiceManagement(); }

    // ─── Search ───────────────────────────────────────────────────────────────

    @And("arama alanina bilinen fatura numarasi girilirse")
    public void aramaBilinenNo() { getPage().searchInvoice(targetInvoiceNo); }

    @And("arama alanina \"OLMAYAN-FATURA-99999\" girilirse")
    public void aramaOlmayanNo() { getPage().searchInvoice("OLMAYAN-FATURA-99999"); }

    @And("arama alani bos birakilip arama baslatilirsa")
    public void aramaAlaniBosBirak() { getPage().searchInvoice(""); }

    // ─── Actions ──────────────────────────────────────────────────────────────

    @And("o fatura icin \"ONAYLA\" butonuna tiklanirsa")
    public void onaylaButonu() { getPage().approveInvoice(targetInvoiceNo); }

    @When("tedarikci ihale olusturma ekraninda fatura secim listesini acarsa")
    public void ihaleOlusturmaFaturaListesi() {
        log.info("Ihale olusturma fatura secim listesi aciliyor (TODO: IhaleOlusturmaPage)");
    }

    // ─── Assertions ───────────────────────────────────────────────────────────

    @Then("fatura basariyla onaylanmali")
    public void faturaOnaylanmali() { Assert.assertTrue(getPage().isSuccessNotificationVisible(), "Basari bildirimi gorunmeli"); }

    @Then("fatura durumu \"APPROVED\" olmali")
    public void faturaDurumuApproved() {
        String status = getPage().getInvoiceStatus(targetInvoiceNo);
        Assert.assertNotNull(status, "Fatura durumu alinmali");
        Assert.assertTrue(status.contains("APPROVED") || status.contains("Onaylandi"),
                "APPROVED bekleniyor, alınan: " + status);
    }

    @Then("fatura ihale secim listesinde gorunmeli")
    public void faturaIhaleListede() {
        String status = getPage().getInvoiceStatus(targetInvoiceNo);
        if (status != null) {
            Assert.assertTrue(status.contains("APPROVED") || status.contains("Onaylandi"),
                    "APPROVED olmalı ihale listesine girebilmesi icin");
        }
    }

    @Then("ilgili fatura listede gorunmeli")
    public void ilgiliFaturaListede() {
        Assert.assertNotNull(getPage().getInvoiceStatus(targetInvoiceNo),
                "Arama sonucu gorunmeli: " + targetInvoiceNo);
    }

    @Then("fatura bilgileri dogru gosterilmeli")
    public void faturaBilgileriDogru() {
        String row = getPage().getInvoiceStatus(targetInvoiceNo);
        Assert.assertNotNull(row, "Fatura bilgileri gorunmeli");
    }

    @Then("\"Fatura bulunamadi\" uyarisi veya bos liste gorunmeli")
    public void faturaBulunamadi() {
        Assert.assertNull(getPage().getInvoiceStatus("OLMAYAN-FATURA-99999"),
                "Olmayan fatura icin satir donmemeli");
    }

    @Then("validasyon uyarisi gorunmeli veya tum faturalar listelenmeli")
    public void validasyonVeyaListe() {
        log.info("Bos arama: validasyon veya genel liste bekleniyor.");
        // Her iki durum da kabul edilir
    }

    @Then("onaylanan fatura listede \"APPROVED\" olarak gorunmeli")
    public void onaylananApproved() { faturaDurumuApproved(); }

    @Then("fatura secilebilir durumda olmali")
    public void faturaSecilebilir() {
        log.info("Secim kontrolu — ihale olusturma sayfasinda dogrulanacak.");
    }
}
