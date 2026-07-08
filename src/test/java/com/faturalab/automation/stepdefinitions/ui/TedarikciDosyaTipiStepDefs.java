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

    @Then("tedarikçi dosyası başarıyla yüklenmiş olmalı")
    public void tedarikciBasariyla() {
        Assert.assertTrue(page().waitForUploadSuccess(20),
                "Kağıt fatura yükleme başarı bildirimi görülmedi (veya hata/red bildirimi geldi)");
    }

    @Then("tedarikçi dosya tipi reddedildiği bildirimi gösterilmeli")
    public void tedarikciReddedilir() {
        String msg = page().waitForRejectionMessage(15);
        Assert.assertNotNull(msg, "Desteklenmeyen uzantı için red bildirimi gösterilmedi");
        log.info("[TEDARIKCI-DOSYA-TIPI] Red bildirimi doğrulandı: {}", msg);
    }
}
