package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.AdminFKAyarlariPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * FL-012: Cut-off Süresi Ayarlama (Admin FK Ayarları)
 */
public class AdminAyarlarUATStepDefs {

    private static final Logger log = LogManager.getLogger(AdminAyarlarUATStepDefs.class);

    private AdminFKAyarlariPage ayarlarPage;

    private AdminFKAyarlariPage getPage() {
        if (ayarlarPage == null) ayarlarPage = new AdminFKAyarlariPage(DriverManager.getDriver());
        return ayarlarPage;
    }

    @When("admin FK ayarları ekranına gidilir")
    public void adminFKAyarlariEkrani() {
        getPage().navigateToAyarlar();
        getPage().navigateToFKAyarlari();
    }

    @And("banka seçilir")
    public void bankaSec() {
        try {
            getPage().selectBanka("Denizbank");
        } catch (Exception e) {
            log.warn("Banka seçilemedi (test data yok olabilir): {}", e.getMessage());
        }
    }

    @And("cut-off saati {string} olarak girilir")
    public void cutoffSaatiGir(String saat) {
        getPage().setCutoffSaat(saat);
    }

    @And("cut-off ayarları kaydedilir")
    public void cutoffKaydet() {
        getPage().clickKaydet();
    }

    @Then("cut-off ayarı başarıyla kaydedilmiş olmalı")
    public void cutoffKaydedilmeli() {
        boolean success = getPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "Cut-off ayar başarı bildirimi görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("Cut-off ayarı başarıyla kaydedildi.");
    }

    @Then("FK ayarları ekranı görüntülenmeli")
    public void fkAyarlariEkraniGorunmeli() {
        boolean visible = getPage().isFKAyarlariVisible();
        Assert.assertTrue(visible,
                "FK Ayarları ekranı görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("FK Ayarları ekranı görüntülendi.");
    }
}
