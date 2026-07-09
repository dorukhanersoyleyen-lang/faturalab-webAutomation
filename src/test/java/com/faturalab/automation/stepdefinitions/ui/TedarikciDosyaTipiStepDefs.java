package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.context.TzfScenarioContext;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.SupplierInvoiceUploadPage;
import com.faturalab.automation.utils.TzfInvoiceExcelGenerator;
import com.faturalab.automation.utils.UploadTestDataPaths;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.io.File;

/**
 * Tedarikçi kağıt fatura (görsel/PDF) dosya tipi yükleme adımları.
 *
 * Tedarikçiye geçiş ("admin TZF tedarikçi kullanıcısına geçiş yapar") TzfIslemUATStepDefs'te
 * yaşar ve yeniden kullanılır; bu sınıf dosya hazırlama + kağıt fatura türüyle yükleme
 * + başarı/red doğrulaması adımlarını ekler.
 */
public class TedarikciDosyaTipiStepDefs {

    private static final Logger log = LogManager.getLogger(TedarikciDosyaTipiStepDefs.class);

    private SupplierInvoiceUploadPage uploadPage;

    private SupplierInvoiceUploadPage page() {
        if (uploadPage == null) {
            uploadPage = new SupplierInvoiceUploadPage(DriverManager.getDriver());
        }
        return uploadPage;
    }

    @Given("tedarikçi kağıt fatura için {string} dosyası hazırlanır")
    public void kagitFaturaDosyasiHazirlanir(String tip) {
        String path;
        switch (tip.toUpperCase()) {
            case "PNG":
                path = TzfInvoiceExcelGenerator.generatePaperInvoiceImage();
                break;
            case "PDF":
                path = UploadTestDataPaths.samplePdf(); // mevcut testdata/test-invoice.pdf
                TzfScenarioContext.reset();
                TzfScenarioContext.setExcelPath(path);
                break;
            case "GECERSIZ":
                path = TzfInvoiceExcelGenerator.generateInvalidExtensionFile();
                TzfScenarioContext.reset();
                TzfScenarioContext.setExcelPath(path);
                break;
            default:
                throw new IllegalArgumentException("Bilinmeyen tedarikçi dosya tipi: " + tip);
        }
        Assert.assertTrue(new File(path).exists(), "Üretilen dosya bulunamadı: " + path);
        log.info("[TEDARIKCI-DOSYA-TIPI] {} dosyası hazır: {}", tip, path);
    }

    @And("tedarikçi kağıt fatura türüyle hazırlanan dosya yüklenir")
    public void kagitFaturaTuruyleYuklenir() {
        String path = TzfScenarioContext.getExcelPath();
        Assert.assertNotNull(path, "Yüklenecek dosya yolu context'te olmalı");
        SupplierInvoiceUploadPage p = page();
        Assert.assertTrue(p.openUploadDialog(), "Tedarikçi fatura yükleme dialogu açılamadı");
        p.selectKagitFatura();
        p.uploadFile(path);
    }

    @Given("tedarikçi için {int} adet dummy imzalı XML fatura hazırlanır")
    public void tedarikciXmlHazirlanir(int adet) {
        TzfScenarioContext.reset();
        String path = null;
        for (int i = 1; i <= adet; i++) {
            path = com.faturalab.automation.utils.XmlInvoiceGenerator.generateXml(String.valueOf(i));
        }
        TzfScenarioContext.setExcelPath(path);
        Assert.assertNotNull(path);
        log.info("[TEDARIKCI-DOSYA-TIPI] {} adet dummy imzalı XML hazır", adet);
    }

    @Given("tedarikçi için {int} adet dummy imzalı XML içeren ZIP hazırlanır")
    public void tedarikciXmlZipHazirlanir(int adet) {
        String path = com.faturalab.automation.utils.XmlInvoiceGenerator.generateXmlZip(adet);
        Assert.assertTrue(new File(path).exists(), "ZIP üretilemedi: " + path);
        log.info("[TEDARIKCI-DOSYA-TIPI] {} XML'li ZIP hazır: {}", adet, path);
    }

    @And("tedarikçi E-Fatura türüyle hazırlanan dosya yüklenir")
    public void eFaturaTuruyleYuklenir() {
        String path = TzfScenarioContext.getExcelPath();
        Assert.assertNotNull(path, "Yüklenecek dosya yolu context'te olmalı");
        SupplierInvoiceUploadPage p = page();
        Assert.assertTrue(p.openUploadDialog(), "Tedarikçi fatura yükleme dialogu açılamadı");
        p.selectEFatura();
        p.uploadFile(path);
        p.submitUpload(); // TC-COMP-01: XML seçildikten sonra "Yükle" gerekli
    }

    @And("kağıt fatura detay modalında zorunlu alanlar doldurulur ve kaydedilir")
    public void detayModaliDoldurulurKaydedilir() {
        String invoiceNo = TzfScenarioContext.getInvoices().isEmpty()
                ? "OTOIMG" + System.currentTimeMillis()
                : TzfScenarioContext.getInvoices().get(0).invoiceNo;
        Assert.assertTrue(page().fillPaperInvoiceDetailsAndSave(invoiceNo),
                "Kağıt fatura detay modalı doldurulup kaydedilemedi");
    }

    @Then("tedarikçi dosyası başarıyla yüklenmiş olmalı")
    public void tedarikciBasariyla() {
        Assert.assertTrue(page().waitForUploadSuccess(20),
                "Kağıt fatura yükleme başarı bildirimi görülmedi (veya hata/red bildirimi geldi)");
    }

    @Then("tedarikçi dosya tipi reddedildiği bildirimi gösterilmeli")
    public void tedarikciReddedilir() {
        // Kağıt fatura türünde red iki biçimde: görünür toast VEYA detay modalının hiç açılmaması.
        String msg = page().waitForRejectionOrNoProgress(15);
        Assert.assertNotNull(msg, "Desteklenmeyen uzantı reddedilmedi (toast yok ve detay modalı açıldı)");
        log.info("[TEDARIKCI-DOSYA-TIPI] Red doğrulandı: {}", msg);
    }
}
