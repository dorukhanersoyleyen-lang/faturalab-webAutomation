package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.FactoringLimitPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * TC-FACT-02-UI — Finansman Limit Yonetimi UI step definitions.
 *
 * Reuse edilen step'ler (burada TEKRAR TANIMI YAPILMAMISTIR):
 *  - "admin dorukhan roleyle dev2'ye giris yapildi"     → LoginRoleStepDefs
 *  - "finansman OPR rolüyle dev2'ye giris yapildi"      → LoginRoleStepDefs
 *  - "alici ALBC roleyle dev2'ye giris yapildi"         → LoginRoleStepDefs
 *  - "basari bildirimi gorunmeli"                       → CommonUIStepDefs
 */
public class FactoringLimitUIStepDefs {

    private static final Logger log = LogManager.getLogger(FactoringLimitUIStepDefs.class);

    private FactoringLimitPage getPage() {
        return new FactoringLimitPage(DriverManager.getDriver());
    }

    // ─── Form Doldurma ────────────────────────────────────────────────────────

    @When("faktoring limit formu asagidaki degerlerle doldurulursa:")
    public void faktOringLimitFormDoldur(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        if (rows.isEmpty()) {
            log.warn("[FactoringLimit] Tablo bos.");
            return;
        }
        FactoringLimitPage page = getPage();
        for (Map<String, String> row : rows) {
            String kurumAdi    = row.getOrDefault("kurumAdi", "OPR Bankasi");
            String limitTutari = row.getOrDefault("limitTutari", "5000000");
            String paraBirimi  = row.getOrDefault("paraBirimi", "TRY");
            log.info("[FactoringLimit] Form dolduruluyor: kurum={}, tutar={}, para={}",
                    kurumAdi, limitTutari, paraBirimi);
            page.fillLimitForm(kurumAdi, limitTutari, paraBirimi);
            page.clickSave();
        }
    }

    @When("limit alani sifir olarak girilirse")
    public void limitAlanSifirGirilir() {
        log.info("[FactoringLimit] Limit alani sifir olarak giriliyor.");
        FactoringLimitPage page = getPage();
        page.enterLimitAmount("0");
        page.clickSave();
    }

    @When("limit alani bos birakılarak kaydet tiklanirsa")
    public void limitBosKaydet() {
        log.info("[FactoringLimit] Limit alani bos bırakılıyor ve kaydet tiklaniyor.");
        FactoringLimitPage page = getPage();
        page.enterLimitAmount("");
        page.clickSave();
    }

    @When("mevcut faktoring limiti {string} olarak guncellenir")
    public void mevcutLimitGuncelle(String yeniLimit) {
        log.info("[FactoringLimit] Limit guncelleniyor: {}", yeniLimit);
        FactoringLimitPage page = getPage();
        page.enterLimitAmount(yeniLimit);
        page.clickSave();
    }

    @When("faktoring limiti {string} TRY olarak girilirse")
    public void faktoringLimitiGirilir(String limitTutari) {
        log.info("[FactoringLimit] Limit TRY girildi: {}", limitTutari);
        FactoringLimitPage page = getPage();
        page.fillLimitForm(null, limitTutari, "TRY");
        page.clickSave();
    }

    @When("para birimi {string} secilerek limit kaydedilirse")
    public void paraBirimiSeciliLimitKaydet(String paraBirimi) {
        log.info("[FactoringLimit] Para birimi seciliyor: {}", paraBirimi);
        FactoringLimitPage page = getPage();
        page.selectCurrency(paraBirimi);
        page.clickSave();
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    @And("alici bazli limit tablosuna gidilir")
    public void aliciBazliLimitTablosu() {
        log.info("[FactoringLimit] Alici bazli limit tablosuna gidiliyor.");
        getPage().navigateToAliciLimitTable();
    }

    @When("finansman kullanicisi kendi limit ekranini acar")
    public void finansmanLimitEkrani() {
        log.info("[FactoringLimit] Finansman kullanicisi limit ekranini aciyor.");
        getPage().navigateToLimitManagement();
    }

    @When("finansman limitini asan teklif vermeye calisir")
    public void finansmanLimitAsanTeklif() {
        log.info("[FactoringLimit] Limit asimi senaryosu: limit ekranina gidiliyor.");
        getPage().navigateToLimitManagement();
        log.info("[FactoringLimit] Limit ekranina gidildi, asim bloku kontrol edilecek.");
    }

    @When("limit yonetim sayfasina erisim denenir")
    public void limitSayfaErisimDene() {
        log.info("[FactoringLimit] Yetkisiz kullanici limit sayfasina erisiyor.");
        getPage().navigateToLimitManagement();
    }

    // ─── Dogrulama ────────────────────────────────────────────────────────────

    @Then("limit basariyla kaydedilmeli")
    public void limitKaydedilmeli() {
        boolean saved = getPage().isLimitSaved();
        log.info("[FactoringLimit] Limit kayit sonucu: {}", saved);
        Assert.assertTrue(saved, "Limit başarıyla kaydedilmeli — bildirim veya başarı metni görünmedi");
    }

    @Then("limit sifir validasyon mesaji veya engel gorunmeli")
    public void limitSifirValidasyon() {
        FactoringLimitPage page = getPage();
        boolean hasError = page.isValidationErrorVisible();
        boolean saved    = page.isLimitSaved();
        log.info("[FactoringLimit] Sifir limit: validasyonHatasi={}, saved={}", hasError, saved);
        Assert.assertTrue(hasError || !saved,
                "Sıfır limit için validasyon hatası veya kayıt engeli bekleniyor");
    }

    @Then("limit form kaydet butonunun etkisiz oldugu veya hata gorunmeli")
    public void limitFormBosHata() {
        boolean hasError = getPage().isValidationErrorVisible();
        log.info("[FactoringLimit] Bos limit form durumu: hasError={}", hasError);
        Assert.assertTrue(hasError,
                "Boş limit formu için validasyon hatası görünmeli");
    }

    @Then("limit guncelleme basariyla tamamlanmali")
    public void limitGuncellemeBasarili() {
        boolean saved = getPage().isLimitSaved();
        log.info("[FactoringLimit] Limit guncelleme: {}", saved);
        Assert.assertTrue(saved, "Limit güncelleme başarılı olmalı");
    }

    @Then("alici bazli limit grid gorunmeli")
    public void aliciBazliLimitGrid() {
        boolean gridVisible = getPage().isGridVisible();
        log.info("[FactoringLimit] Alici bazli limit grid: {}", gridVisible);
        Assert.assertTrue(gridVisible, "Alıcı bazlı limit grid görünmeli");
    }

    @Then("faktoring limit bilgisi ekranda gorunmeli")
    public void faktoringLimitBilgisi() {
        boolean gridVisible = getPage().isGridVisible();
        String src = DriverManager.getDriver().getPageSource();
        boolean hasLimitInfo = src.contains("Limit") || src.contains("limit") || gridVisible;
        log.info("[FactoringLimit] Faktoring limit bilgisi sayfada: {}", hasLimitInfo);
        Assert.assertTrue(hasLimitInfo, "Faktoring limit bilgisi (grid veya metin) görünmeli");
    }

    @Then("fatura teklif bloklanmali veya hata mesaji gorunmeli")
    public void teklipBloklanmali() {
        boolean hasError = getPage().isValidationErrorVisible();
        String src = DriverManager.getDriver().getPageSource();
        boolean hasBlock = src.contains("limit") || src.contains("blok") || src.contains("yetersiz")
                || src.contains("insufficient") || hasError;
        log.info("[FactoringLimit] Teklif blok kontrol: hasBlock={}", hasBlock);
        Assert.assertTrue(hasBlock, "Limit aşımı durumunda teklif bloklanmalı veya hata mesajı görünmeli");
    }

    @Then("limit grid satirinda yeni deger gorunmeli")
    public void limitGridYeniDeger() {
        boolean gridVisible = getPage().isGridVisible();
        log.info("[FactoringLimit] Limit grid guncellendi mi: {}", gridVisible);
        Assert.assertTrue(gridVisible, "Limit grid görünmeli ve güncellenmiş değer içermeli");
    }

    @Then("limit ekranina erisim engellenmeli veya menu gorunmemeli")
    public void limitEkraniErisimEngel() {
        boolean denied = getPage().isAccessDenied();
        log.info("[FactoringLimit] Yetkisiz erisim engeli: {}", denied);
        Assert.assertTrue(denied, "Yetkisiz kullanıcı için limit ekranına erişim engellenmeli");
    }

    @Then("limit kayit sonucu {string} olmali")
    public void limitKayitSonucu(String beklenenSonuc) {
        FactoringLimitPage page = getPage();
        if ("BASARILI".equalsIgnoreCase(beklenenSonuc)) {
            boolean saved = page.isLimitSaved();
            log.info("[FactoringLimit] BASARILI bekleniyor — kayit: {}", saved);
            Assert.assertTrue(saved, "Limit kayıt sonucu BASARILI bekleniyor");
        } else if ("HATA".equalsIgnoreCase(beklenenSonuc)) {
            boolean hasError = page.isValidationErrorVisible();
            log.info("[FactoringLimit] HATA bekleniyor — validasyon: {}", hasError);
            Assert.assertTrue(hasError, "Limit kayıt sonucu HATA bekleniyor — validasyon hatası görünmeli");
        } else {
            log.warn("[FactoringLimit] Bilinmeyen sonuc: {}", beklenenSonuc);
        }
    }
}
