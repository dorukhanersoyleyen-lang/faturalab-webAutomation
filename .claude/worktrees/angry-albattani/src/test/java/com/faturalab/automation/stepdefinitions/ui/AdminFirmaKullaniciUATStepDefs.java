package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.AdminFirmaPage;
import com.faturalab.automation.pages.AdminKullaniciPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * FL-003: Firma Düzenleme
 * FL-004: Kullanıcı Düzenleme
 */
public class AdminFirmaKullaniciUATStepDefs {

    private static final Logger log = LogManager.getLogger(AdminFirmaKullaniciUATStepDefs.class);

    private AdminFirmaPage firmaPage;
    private AdminKullaniciPage kullaniciPage;
    private String activeContext = "firma";

    private AdminFirmaPage getFirmaPage() {
        if (firmaPage == null) firmaPage = new AdminFirmaPage(DriverManager.getDriver());
        return firmaPage;
    }

    private AdminKullaniciPage getKullaniciPage() {
        if (kullaniciPage == null) kullaniciPage = new AdminKullaniciPage(DriverManager.getDriver());
        return kullaniciPage;
    }

    // ─── FL-003: Firma Düzenleme ──────────────────────────────────────────────

    @When("admin firma yönetimi ekranına gidilir")
    public void adminFirmaYonetimi() {
        activeContext = "firma";
        getFirmaPage().navigateToFirmaYonetimi();
    }

    @And("firma listesi görüntülenir")
    public void firmaListesiGoruntur() {
        Assert.assertTrue(getFirmaPage().isFirmaGridVisible(),
                "Firma listesi grid'i görünmeli");
        log.info("Firma listesi grid'i görüntülendi.");
    }

    @And("ilk firma için düzenle butonuna tıklanır")
    public void ilkFirmaDuzenle() {
        getFirmaPage().clickFirstDuzenleButton();
        Assert.assertTrue(getFirmaPage().isFirmaDialogOpen(),
                "Firma düzenleme dialogu açılmalı");
    }

    @And("firma bilgileri güncellenir")
    public void firmaBilgileriGuncelle() {
        String yeniAdres = "Test Otomasyon Adres " + (System.currentTimeMillis() % 10000);
        getFirmaPage().updateFirmaAdres(yeniAdres);
        log.info("Firma adresi güncellendi: {}", yeniAdres);
    }

    @And("kaydet butonuna tıklanır")
    public void kaydetButonunaTikla() {
        if ("kullanici".equals(activeContext)) {
            getKullaniciPage().clickKaydet();
        } else {
            getFirmaPage().clickKaydetInDialog();
        }
    }

    @Then("firma bilgileri başarıyla güncellenmiş olmalı")
    public void firmaBilgileriGuncellenmeli() {
        boolean success = getFirmaPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "Firma güncelleme başarı bildirimi görünmeli. URL: " +
                DriverManager.getDriver().getCurrentUrl());
        log.info("Firma bilgileri başarıyla güncellendi.");
    }

    // ─── FL-004: Kullanıcı Düzenleme ─────────────────────────────────────────

    @When("admin kullanıcı yönetimi ekranına gidilir")
    public void adminKullaniciYonetimi() {
        activeContext = "kullanici";
        getKullaniciPage().navigateToKullaniciYonetimi();
    }

    @And("kullanıcı listesi görüntülenir")
    public void kullaniciListesiGoruntur() {
        Assert.assertTrue(getKullaniciPage().isKullaniciGridVisible(),
                "Kullanıcı listesi grid'i görünmeli");
        log.info("Kullanıcı listesi grid'i görüntülendi.");
    }

    @And("ilk kullanıcı satırına tıklanır")
    public void ilkKullaniciSatirina() {
        getKullaniciPage().clickFirstUserRow();
    }

    @And("kullanıcı düzenle işlemi yapılır")
    public void kullaniciDuzenle() {
        getKullaniciPage().clickDuzenle();
        // Wait briefly for dialog or page to settle, then update adres regardless of dialog/page mode
        try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        String yeniAdres = "Test Kullanıcı Adres " + (System.currentTimeMillis() % 10000);
        getKullaniciPage().updateKullaniciAdres(yeniAdres);
        log.info("Kullanıcı adresi güncellendi: {}", yeniAdres);
    }

    @Then("kullanıcı bilgileri başarıyla güncellenmiş olmalı")
    public void kullaniciBilgileriGuncellenmeli() {
        boolean success = getKullaniciPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "Kullanıcı güncelleme başarı bildirimi görünmeli. URL: " +
                DriverManager.getDriver().getCurrentUrl());
        log.info("Kullanıcı bilgileri başarıyla güncellendi.");
    }
}
