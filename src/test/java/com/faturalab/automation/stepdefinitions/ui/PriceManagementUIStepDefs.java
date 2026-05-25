package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.PriceManagementPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * TC-PRICE-01-UI — Fiyat Yönetimi (AdminBuyerFactoringRateView) UI step definitions.
 *
 * Navigasyon: Admin menüsü → "Fiyat ve Oranlar"
 * Sayfa başlığı: "Fiyat Aralıkları"
 * Başarı mesajı: "Fiyat aralığı başarıyla kaydedildi"
 *
 * Reuse edilen step'ler (burada TEKRAR TANIMI YAPILMAMISTIR):
 *  - "admin dorukhan roleyle dev2'ye giris yapildi"  → LoginRoleStepDefs
 *  - "basari bildirimi gorunmeli"                    → CommonUIStepDefs
 */
public class PriceManagementUIStepDefs {

    private static final Logger log = LogManager.getLogger(PriceManagementUIStepDefs.class);

    private PriceManagementPage getPage() {
        return new PriceManagementPage(DriverManager.getDriver());
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    @Given("fiyat yonetimi ekranina gidilir")
    public void fiyatYonetimiEkrani() {
        log.info("[PriceManagement] 'Fiyat ve Oranlar' ekranına gidiliyor.");
        PriceManagementPage page = getPage();
        page.navigateToPriceManagement();
        boolean gridVisible = page.isPriceGridVisible();
        log.info("[PriceManagement] Fiyat ekranı açıldı: gridVisible={}", gridVisible);
        Assert.assertTrue(gridVisible,
                "'Fiyat ve Oranlar' menüsüne gidildikten sonra fiyat grid görünmeli");
    }

    @And("fiyat ekrani kriter tablosu goruntulenir")
    public void fiyatEkraniKriterTablosu() {
        log.info("[PriceManagement] Fiyat ekranı kriter tablosu kontrol ediliyor.");
        // Grid yüklenene kadar bekle
        try {
            new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(
                            org.openqa.selenium.By.cssSelector("vaadin-grid")));
        } catch (Exception e) {
            log.info("[PriceManagement] Grid bekleme: {}", e.getMessage());
        }
        boolean gridVisible = getPage().isPriceGridVisible();
        Assert.assertTrue(gridVisible, "Fiyat ekranında vaadin-grid görünmeli");
    }

    // ─── Form Aksiyonlari ─────────────────────────────────────────────────────

    @When("fiyat aralik degerleri girilirse:")
    public void fiyatAralikDegerleriGirilir(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        if (rows.isEmpty()) {
            log.warn("[PriceManagement] Fiyat aralik tablosu bos.");
            return;
        }
        Map<String, String> row = rows.get(0);
        String minFiyat = row.getOrDefault("minFiyat", "");
        String maxFiyat = row.getOrDefault("maxFiyat", "");
        log.info("[PriceManagement] Fiyat aralik girildi: min={}, max={}", minFiyat, maxFiyat);
        getPage().enterPriceRange(minFiyat, maxFiyat);
    }

    @When("gecersiz fiyat aralik degeri girilirse")
    public void gecersizFiyatAralik() {
        log.info("[PriceManagement] Gecersiz fiyat aralik degeri giriliyor (min > max).");
        getPage().enterInvalidPriceRange();
    }

    @When("fiyat guncelleme formu kaydedilirse")
    public void fiyatGuncelleFormKaydet() {
        log.info("[PriceManagement] Fiyat guncelleme formu kaydediliyor.");
        getPage().clickSave();
    }

    @When("fiyat alanları bos birakılarak kaydet tiklanirsa")
    public void fiyatAlanlarBosKaydet() {
        log.info("[PriceManagement] Fiyat alanlari bos birakılarak kaydet tiklaniyor.");
        getPage().clearAndSave();
    }

    // ─── Dogrulama ────────────────────────────────────────────────────────────

    @Then("fiyat guncelleme basariyla tamamlanmali")
    public void fiyatGuncellemeBasarili() {
        boolean saved = getPage().isPriceSaved();
        log.info("[PriceManagement] Fiyat guncelleme: {}", saved);
        Assert.assertTrue(saved,
                "Fiyat aralığı başarıyla kaydedilmeli — bildirim veya 'kaydedildi' metni görünmeli");
    }

    @Then("kriter tablosu baslik sutunlari dogru gorunmeli")
    public void kriterTablosuBasliklar() {
        boolean headersVisible = getPage().areHeaderColumnsVisible();
        log.info("[PriceManagement] Kriter tablosu başlıklar: {}", headersVisible);
        Assert.assertTrue(headersVisible,
                "Fiyat grid başlık sütunları görünmeli (Fiyat / Aralık / Min / Max)");
    }

    @Then("fiyat aralik validasyon hatasi gorunmeli")
    public void fiyatAralikValidasyonHatasi() {
        boolean hasError = getPage().isValidationErrorVisible();
        log.info("[PriceManagement] Fiyat aralik validasyon hatasi: {}", hasError);
        Assert.assertTrue(hasError,
                "Geçersiz fiyat aralığı için validasyon hatası görünmeli");
    }

    @Then("tum fiyat araliklari listede gorunmeli")
    public void tumFiyatAraliklariListede() {
        boolean listed = getPage().arePriceRangesListedInGrid();
        log.info("[PriceManagement] Fiyat araliklari listede: {}", listed);
        Assert.assertTrue(listed,
                "Fiyat aralıkları grid'de listelenmeli — grid cell içeriği bekleniyor");
    }
}
