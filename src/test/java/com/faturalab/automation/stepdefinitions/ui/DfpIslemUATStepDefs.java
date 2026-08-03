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
        // Alıcı-tarzı iki adımlı aksiyon (Yükle → takip Kaydet) BuyerBulkUploadPage'te hazır
        new com.faturalab.automation.pages.BuyerBulkUploadPage(DriverManager.getDriver()).clickYukle();
        log.info("[DFP] Dosya yüklendi + Yükle/Kaydet zinciri koşuldu: {}", path);
    }

    @Then("DFP teklif talebi işlemdekiler listesinde görünmeli")
    public void dfpTalepIslemdekilerde() {
        // İşlemdekiler gridi FATURA no değil BORDRO no gösterir (canlı doğrulandı, 03/08).
        // Kriter: bir bordro (A2026_xxxx) hücresiyle AYNI satır civarında bizim 1,00 TL
        // tutarımızın görünmesi — 1 TL politikası bu talebi diğerlerinden ayırt eder.
        String bordro = null;
        long deadline = System.currentTimeMillis() + 20000L;
        while (System.currentTimeMillis() < deadline && bordro == null) {
            // Birden çok 1TL talep birikebilir (önceki koşumlar) → EN BÜYÜK numaralı
            // (= en yeni) bordroyu al; bu koşumun talebi her zaman en yüksek numaradır.
            Object r = ((JavascriptExecutor) DriverManager.getDriver()).executeScript(
                    "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                    "var re = /([A-Z]\\d{4}_(\\d{2,}))/;" +
                    "var best = null, bestNum = -1;" +
                    "for (var i = 0; i < cells.length; i++) {" +
                    "  var m = (cells[i].textContent||'').match(re);" +
                    "  if (!m) continue;" +
                    "  var r0 = cells[i].getBoundingClientRect(); if (r0.width < 2) continue;" +
                    "  for (var j = Math.max(0,i-10); j < Math.min(cells.length, i+10); j++) {" +
                    "    var t = (cells[j].textContent||'').replace(/\\s+/g,'');" +
                    "    if (t.indexOf('1,00') >= 0 || t.indexOf('1.00') >= 0) {" +
                    "      var n = parseInt(m[2], 10);" +
                    "      if (n > bestNum) { bestNum = n; best = m[1]; }" +
                    "      break;" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return best;");
            bordro = r != null ? r.toString() : null;
            if (bordro == null) {
                try { Thread.sleep(700); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }
        Assert.assertNotNull(bordro,
                "İşlemdekiler'de 1,00 TL tutarlı DFP talebi (bordro satırı) görünmedi");
        TzfScenarioContext.setBordroNo(bordro);
        log.info("[DFP] Teklif talebi İşlemdekiler'de doğrulandı — bordro: {} (1,00 TL)", bordro);
    }
}
