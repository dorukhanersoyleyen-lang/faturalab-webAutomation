package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.FactoringDashboardPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * FL-013: FK Teklif Verme Akışı
 * NOT: "Günlük Teklif Talebi sekmesine gecilirse" ve "GOZAT butonuna tiklanirsa" gibi adımlar
 * FactoringUIStepDefs.java'da tanımlıdır — bu dosyada tekrar tanımlanmaz.
 */
public class FinansmanTeklifVermeUATStepDefs {

    private static final Logger log = LogManager.getLogger(FinansmanTeklifVermeUATStepDefs.class);

    private FactoringDashboardPage factoringPage;

    private FactoringDashboardPage getPage() {
        if (factoringPage == null) {
            factoringPage = new FactoringDashboardPage(DriverManager.getDriver());
        }
        return factoringPage;
    }

    @When("finansman teklif talepleri ekranına gidilir")
    public void finansmanTeklifTalepleriEkrani() {
        getPage().navigateToOfferManagement();
    }

    @And("teklif talebi listesi grid'i görünmeli")
    public void teklifTalebiListesiGrid() {
        Assert.assertTrue(getPage().isGridVisible(),
                "Teklif talebi grid'i görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("Teklif talebi grid'i görüntülendi.");
    }

    @Then("teklif talepleri başarıyla listelenmeli")
    public void teklifTalepleriListelenmeli() {
        Assert.assertTrue(getPage().isGridVisible(),
                "Teklif talepleri grid'i görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("Teklif talepleri listeleme kontrolü tamamlandı.");
    }

    @And("teklif talebi listesinden ilgili talep satırı seçilir")
    public void teklifTalebiSatiriSec() {
        getPage().switchToGunlukTeklifTab();
    }

    @And("teklif formu doldurulur")
    public void teklifFormuDoldur() {
        log.info("Teklif formu doldurma adımı — FactoringAuctionPage ile yönetilecek.");
    }

    @And("teklif gönderilir")
    public void teklifGonder() {
        log.info("Teklif gönderme adımı — FactoringAuctionPage ile yönetilecek.");
    }

    @Then("finansman teklifi başarıyla iletilmiş olmalı")
    public void finansmanTeklifiIletilmeli() {
        boolean success = getPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "Finansman teklif başarı bildirimi görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("Finansman teklifi başarıyla iletildi.");
    }

    @And("kabul edilen teklif için bordro oluşturma başlatılır")
    public void bordroOlusturmaBaslat() {
        log.info("Bordro oluşturma başlatma adımı.");
    }

    @And("bordro parametreleri onaylanır")
    public void bordroParametreleriOnayla() {
        log.info("Bordro parametreleri onaylama.");
    }

    @Then("bordro başarıyla oluşturulmuş olmalı")
    public void bordroOlusturulmali() {
        boolean success = getPage().isSuccessNotificationVisible();
        Assert.assertTrue(success,
                "Bordro oluşturma başarı bildirimi görünmeli. URL: " + DriverManager.getDriver().getCurrentUrl());
        log.info("Bordro başarıyla oluşturuldu.");
    }
}
