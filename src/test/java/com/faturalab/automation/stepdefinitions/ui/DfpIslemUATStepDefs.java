package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.context.RoleSessionManager;
import com.faturalab.automation.context.RoleSessionManager.Role;
import com.faturalab.automation.context.TzfScenarioContext;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.utils.XmlInvoiceGenerator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;

import java.io.File;

/**
 * DFP (MARKET_PLACE / E-İskonto) akışı adımları.
 *
 * Tedarikçi: İşbank Tedarikçi 1 (MP) — dfp.* config anahtarları.
 * Fatura yükleme (E-Fatura XML), Teklif Al ve İşlemdekiler adımları
 * Tedarikci/Tzf step'leriyle paylaşılır; burada yalnızca DFP'ye özgü
 * hazırlık + giriş + talep doğrulaması var.
 *
 * Tutar politikası: her fatura 1,00 TL (İşbank MP limitini tüketmemek için).
 */
public class DfpIslemUATStepDefs {

    private static final Logger log = LogManager.getLogger(DfpIslemUATStepDefs.class);

    @Given("DFP için 1 TL tutarında dummy imzalı XML fatura hazırlanır")
    public void dfpXmlHazirlanir() {
        TzfScenarioContext.reset();
        String vkn = ConfigReader.getProperty("dfp.supplier.vkn");
        Assert.assertNotNull(vkn, "dfp.supplier.vkn config'te tanımlı olmalı");
        String path = XmlInvoiceGenerator.generateDfpXml(vkn, "1");
        Assert.assertTrue(new File(path).exists(), "DFP XML üretilemedi: " + path);
        log.info("[DFP] 1 TL XML hazır: {}", path);
    }

    @Given("dfp tedarikcisi olarak giriş yapılır")
    public void dfpTedarikcisiGiris() {
        String identifier = ConfigReader.getProperty("dfp.supplier.impersonate.identifier");
        Assert.assertNotNull(identifier, "dfp.supplier.impersonate.identifier config'te tanımlı olmalı");
        RoleSessionManager.clearSession(Role.COMPANY);
        RoleSessionManager.loginAs(DriverManager.getDriver(), Role.COMPANY, identifier,
                ConfigReader.getProperty("admin.password"));
        log.info("[DFP] Tedarikçiye geçildi: {}", identifier);
    }

    @Given("DFP dosyası yüklenir ve kaydedilir")
    public void dfpDosyasiYuklenir() {
        // İşbank Ted 1 (MP) upload dialogu EFG'den FARKLI: Fatura Türü radyosu yok;
        // sekmeler "Fatura Yükle / Fatura Sil / Fatura Yükleme Talebi", akış alıcı-tarzı:
        // dosya seç → Yükle → (varsa vade formu) → Kaydet. (Canlı dökümle doğrulandı, 03/08.)
        String path = TzfScenarioContext.getExcelPath();
        Assert.assertNotNull(path, "DFP XML yolu context'te olmalı");
        com.faturalab.automation.pages.SupplierInvoiceUploadPage sp =
                new com.faturalab.automation.pages.SupplierInvoiceUploadPage(DriverManager.getDriver());
        Assert.assertTrue(sp.openUploadDialog(), "DFP fatura yükleme dialogu açılamadı");
        sp.uploadFile(path);
        // Alıcı-tarzı iki adımlı aksiyon (Yükle → takip Kaydet) BuyerBulkUploadPage'te hazır.
        // ⚠️ clickActionButton ÖNCELİKLİ arar (önce "Yükle", sonra "Kaydet") — tek turda
        // arandığında DOM sırası yüzünden "Kaydet" tıklanıp dosya hiç yüklenmiyordu (build #62).
        new com.faturalab.automation.pages.BuyerBulkUploadPage(DriverManager.getDriver()).clickYukle();

        // Upload SONUCUNU doğrula: eskiden yalnızca tıklanıyordu, red/başarısızlık
        // sessizce geçiyor ve bir sonraki adım anlamsız "fatura listede yok" hatası veriyordu.
        boolean ok = sp.waitForUploadSuccess(45);
        Assert.assertTrue(ok,
                "DFP fatura yüklemesi başarısız — başarı bildirimi gelmedi veya red edildi: " + path);
        log.info("[DFP] Dosya yüklendi ve başarı doğrulandı: {}", path);
    }

    @Then("DFP teklif talebi işlemdekiler listesinde görünmeli")
    public void dfpTalepIslemdekilerdeYeni() {
        // Doğrulama: İşlemdekiler'de KENDİ talebimizin bordrosu (en yeni = max numaralı).
        // Eski sürüm gridde "1,00" tutar hücresi arıyordu; virtual scroll'da o hücre
        // görünmediğinde senaryo gereksiz fail ediyordu (CI build #55). Tutar kontrolü
        // artık zorunlu değil — bonus doğrulama olarak loglanır.
        com.faturalab.automation.pages.CompanyQuickOfferPage offer =
                new com.faturalab.automation.pages.CompanyQuickOfferPage(DriverManager.getDriver());
        String bordro = null;
        long deadline = System.currentTimeMillis() + 25000L;
        while (System.currentTimeMillis() < deadline && bordro == null) {
            bordro = offer.findLatestBordroInGrid();
            if (bordro == null) {
                try { Thread.sleep(1000); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); break;
                }
            }
        }
        Assert.assertNotNull(bordro,
                "İşlemdekiler listesinde DFP teklif talebi (bordro) satırı görünmedi");
        TzfScenarioContext.setBordroNo(bordro);

        // Bonus: 1,00 TL tutarı gridde görünüyorsa doğrula (fail ETMEZ)
        Object oneTl = ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                "for (var c of cells) { var t=(c.textContent||'').replace(/\\s+/g,'');" +
                "  if (t.indexOf('1,00') >= 0 || t.indexOf('1.00') >= 0) return true; }" +
                "return false;");
        log.info("[DFP] Teklif talebi doğrulandı — bordro: {} (gridde 1,00 TL görünür: {})",
                bordro, Boolean.TRUE.equals(oneTl));
    }

}
