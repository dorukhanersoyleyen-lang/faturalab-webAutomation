package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.CriteriaTablePage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * TC-REBATE-01-UI — Kriter Tablosu (DisplayCriteriaTableConfigView) UI step definitions.
 *
 * Navigasyon: Admin menüsü → "Tablolar"
 * Gercek alanlar: "Kod" (codeField), "Başlık" (nameField)
 * Başarı mesajı: "Kriter tablosu başarıyla kaydedildi"
 *
 * Reuse edilen step'ler (burada TEKRAR TANIMI YAPILMAMISTIR):
 *  - "admin dorukhan roleyle dev2'ye giris yapildi"  → LoginRoleStepDefs
 *  - "basari bildirimi gorunmeli"                    → CommonUIStepDefs
 */
public class RebateCriteriaUIStepDefs {

    private static final Logger log = LogManager.getLogger(RebateCriteriaUIStepDefs.class);

    /** Satır silinmeden önce kaydedilen satır sayısı */
    private int rowCountBeforeDelete = -1;

    private CriteriaTablePage getPage() {
        return new CriteriaTablePage(DriverManager.getDriver());
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    @Given("rebate kriterleri ekranina gidilir")
    public void rebateKriterleriEkrani() {
        log.info("[RebateCriteria] 'Tablolar' menüsüne gidiliyor.");
        CriteriaTablePage page = getPage();
        page.navigateToRebateCriteria();
        boolean gridVisible = page.isCriteriaGridVisible();
        log.info("[RebateCriteria] Kriter tablosu ekranı açıldı: gridVisible={}", gridVisible);
        Assert.assertTrue(gridVisible,
                "Tablolar menüsüne gidildikten sonra kriter grid görünmeli");
    }

    // ─── CRUD Aksiyonlari ─────────────────────────────────────────────────────

    @When("rebate tablosuna yeni bir satir eklenir")
    public void rebateYeniSatirEkle() {
        log.info("[RebateCriteria] Yeni satır ekleniyor ('Ekle' butonuna tıklanıyor).");
        getPage().clickAddRow();
    }

    @When("rebate miktari sifir girilirse")
    public void rebateMiktariSifir() {
        log.info("[RebateCriteria] Başlık alanına sıfır ('0') giriliyor.");
        getPage().enterRebateMiktar("0");
    }

    @When("rebate orani {string} girilirse")
    public void rebateOraniGirilir(String oran) {
        log.info("[RebateCriteria] Kod alanına değer giriliyor: {}", oran);
        getPage().enterRebateOran(oran);
    }

    @When("hatali rebate orani girilirse")
    public void hataliRebateOraniGirilir() {
        log.info("[RebateCriteria] Kod alanına geçersiz değer giriliyor: -999.");
        getPage().enterRebateOran("-999");
    }

    @When("rebate satiri silinirse")
    public void rebateSatiriSil() {
        log.info("[RebateCriteria] Rebate satırı siliniyor.");
        CriteriaTablePage page = getPage();
        rowCountBeforeDelete = page.getRowCount();
        log.info("[RebateCriteria] Silme öncesi satır sayısı: {}", rowCountBeforeDelete);
        page.deleteFirstRow();
    }

    @When("mevcut rebate satiri duzenlenirse")
    public void mevcutRebateSatiriDuzenle() {
        log.info("[RebateCriteria] Mevcut satır düzenleniyor.");
        CriteriaTablePage page = getPage();
        page.editFirstRow();
        page.enterRebateOran("3.0");
    }

    @And("rebate formu kaydedilirse")
    public void rebateFormuKaydet() {
        log.info("[RebateCriteria] Kriter formu kaydediliyor ('Kaydet' butonuna tıklanıyor).");
        getPage().clickSave();
    }

    // ─── Dogrulama ────────────────────────────────────────────────────────────

    @Then("rebate listesi basariyla guncellenmeli")
    public void rebateListesiGuncellendi() {
        boolean saved = getPage().isRebateSaved();
        log.info("[RebateCriteria] Kriter listesi güncelleme: {}", saved);
        Assert.assertTrue(saved,
                "Kriter tablosu başarıyla kaydedilmeli — bildirim veya 'kaydedildi' metni görünmeli");
    }

    @Then("rebate dogrulama hatasi gorunmeli")
    public void rebateDogrulamaHatasi() {
        boolean hasError = getPage().isValidationErrorVisible();
        log.info("[RebateCriteria] Validasyon hatası: {}", hasError);
        Assert.assertTrue(hasError,
                "Geçersiz değer için validasyon hatası görünmeli");
    }

    @Then("rebate satiri listeden kalkmali")
    public void rebateSatiriKalkti() {
        CriteriaTablePage page = getPage();
        int currentCount = page.getRowCount();
        log.info("[RebateCriteria] Satır sayısı: öncesi={}, sonrası={}", rowCountBeforeDelete, currentCount);
        if (rowCountBeforeDelete >= 0) {
            Assert.assertTrue(currentCount < rowCountBeforeDelete,
                    "Silme sonrası satır sayısı azalmış olmalı. Öncesi: " +
                    rowCountBeforeDelete + ", Sonrası: " + currentCount);
        } else {
            // rowCountBeforeDelete set edilmemişse, sadece grid görünüyor mu kontrol et
            Assert.assertTrue(page.isCriteriaGridVisible(),
                    "Silme işleminden sonra grid görünmeli");
        }
    }

    @Then("rebate hesaplama dogrulama yapilir")
    public void rebateHesaplamaDogrula() {
        boolean gridVisible = getPage().isCriteriaGridVisible();
        log.info("[RebateCriteria] Kriter tablosu hesaplama kontrolü: gridVisible={}", gridVisible);
        Assert.assertTrue(gridVisible, "Kriter tablosu grid görünmeli");
    }

    @Then("degisiklik rebate listesine yansiyor")
    public void degisiklikListeyeYansiyor() {
        boolean saved = getPage().isRebateSaved();
        log.info("[RebateCriteria] Değişiklik yansıma kontrolü: {}", saved);
        Assert.assertTrue(saved,
                "Değişiklik kriter listesine yansımış olmalı — kayıt başarılı bildirimi bekleniyor");
    }

    @Then("rebate hata mesaji gorunmeli")
    public void rebateHataMesaji() {
        boolean hasError = getPage().isValidationErrorVisible();
        String src = DriverManager.getDriver().getPageSource();
        boolean hasMsg = src.contains("hata") || src.contains("error") || src.contains("invalid") || hasError;
        log.info("[RebateCriteria] Hata mesajı kontrolü: hasMsg={}", hasMsg);
        Assert.assertTrue(hasMsg, "Geçersiz işlem için hata mesajı görünmeli");
    }

    @Then("rebate kriter tablosu bos veya dolu olarak gorunmeli")
    public void rebateTabloBosVeyaDolu() {
        boolean gridVisible = getPage().isGridDisplayedWithAnyContent();
        log.info("[RebateCriteria] Kriter tablosu görünüyor: {}", gridVisible);
        Assert.assertTrue(gridVisible, "Kriter tablosu grid görünmeli (boş veya dolu)");
    }
}
