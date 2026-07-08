package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.context.TzfScenarioContext;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.BuyerBulkUploadPage;
import com.faturalab.automation.utils.TzfInvoiceExcelGenerator;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.io.File;

/**
 * Farklı dosya tipleriyle (XLS/XLSX/ZIP/geçersiz) alıcı fatura yükleme adımları.
 *
 * Alıcıya geçiş ("admin TZF alıcı kullanıcısına geçiş yapar") ve başarı doğrulaması
 * TzfIslemUATStepDefs'teki mevcut adımlarla paylaşılır; bu sınıf yalnızca dosya
 * hazırlama, format-agnostik yükleme ve red doğrulaması adımlarını ekler.
 */
public class DosyaTipiUploadStepDefs {

    private static final Logger log = LogManager.getLogger(DosyaTipiUploadStepDefs.class);

    private BuyerBulkUploadPage buyerUploadPage;

    private BuyerBulkUploadPage page() {
        if (buyerUploadPage == null) {
            buyerUploadPage = new BuyerBulkUploadPage(DriverManager.getDriver());
        }
        return buyerUploadPage;
    }

    @Given("TZF senaryosu için {int} adet fatura içeren {string} dosyası hazırlanır")
    public void dosyaHazirlanir(int adet, String tip) {
        String supplierName = ConfigReader.getProperty("tzf.supplier.name");
        String supplierVkn = ConfigReader.getProperty("tzf.supplier.vkn");
        String path;
        switch (tip.toUpperCase()) {
            case "XLS":
                path = TzfInvoiceExcelGenerator.generate(supplierName, supplierVkn, adet,
                        TzfInvoiceExcelGenerator.Format.XLS);
                break;
            case "XLSX":
                path = TzfInvoiceExcelGenerator.generate(supplierName, supplierVkn, adet,
                        TzfInvoiceExcelGenerator.Format.XLSX);
                break;
            case "ZIP":
                path = TzfInvoiceExcelGenerator.generateInvoiceZip(adet);
                break;
            case "GECERSIZ":
                path = TzfInvoiceExcelGenerator.generateInvalidExtensionFile();
                TzfScenarioContext.setExcelPath(path); // context'e yaz (üretici bunu yapmıyor)
                break;
            default:
                throw new IllegalArgumentException("Bilinmeyen dosya tipi: " + tip);
        }
        Assert.assertTrue(new File(path).exists(), "Üretilen dosya bulunamadı: " + path);
        log.info("[DOSYA-TIPI] {} dosyası hazırlandı: {}", tip, path);
    }

    @And("alıcı ekranında hazırlanan dosya yüklenir")
    public void hazirlananDosyaYuklenir() {
        String path = TzfScenarioContext.getExcelPath();
        Assert.assertNotNull(path, "Yüklenecek dosya yolu context'te olmalı");
        BuyerBulkUploadPage p = page();
        Assert.assertTrue(p.openUploadDialog(), "Alıcı fatura yükleme dialogu açılamadı");
        p.uploadExcel(path);
        p.clickYukle();
    }

    @Then("dosya başarıyla yüklenmiş olmalı")
    public void dosyaBasariyla() {
        Assert.assertTrue(page().waitForUploadSuccess(20),
                "Dosya yükleme başarı bildirimi görülmedi (veya hata bildirimi geldi)");
    }

    @Then("dosya tipi reddedildiği bildirimi gösterilmeli")
    public void dosyaTipiReddedilir() {
        String msg = page().waitForRejectionMessage(15);
        Assert.assertNotNull(msg,
                "Desteklenmeyen uzantı için red bildirimi gösterilmedi");
        log.info("[DOSYA-TIPI] Red bildirimi doğrulandı: {}", msg);
    }
}
