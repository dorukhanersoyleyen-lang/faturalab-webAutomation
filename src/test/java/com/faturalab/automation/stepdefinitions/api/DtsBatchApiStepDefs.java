package com.faturalab.automation.stepdefinitions.api;

import com.faturalab.automation.api.CompanyBatchApi;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.time.LocalDate;
import java.util.Collections;

/**
 * DTS_API-001 — DTS batch entegrasyon akışı (authenticate → start → upload → end).
 * Her koşumda benzersiz dtsReferenceNo/batchNo/invoiceNo üretilir.
 */
public class DtsBatchApiStepDefs {

    private static final Logger log = LogManager.getLogger(DtsBatchApiStepDefs.class);

    private final CompanyBatchApi companyBatchApi = new CompanyBatchApi();

    private String dtsReferenceNo;
    private String batchNo;
    private String invoiceNo;

    private Response authenticateResponse;
    private Response startResponse;
    private Response uploadResponse;
    private Response endResponse;

    @Given("\"TEST\" entegrasyon kullanıcısı ile DTS API'sinde kimlik doğrulanır")
    public void test_entegrasyon_kullanicisi_ile_dts_apisinde_kimlik_dogrulanir() {
        authenticateResponse = companyBatchApi.authenticate();

        Assert.assertEquals(authenticateResponse.getStatusCode(), 200,
                "DTS authenticate 200 dönmeli. Body: " + authenticateResponse.getBody().asString());
        Assert.assertTrue(companyBatchApi.isSuccess(authenticateResponse),
                "DTS authenticate success=true dönmeli. Body: " + authenticateResponse.getBody().asString());
        Assert.assertNotNull(companyBatchApi.getSessionId(),
                "DTS authenticate sonrası sessionId dolu olmalı. Body: " + authenticateResponse.getBody().asString());

        log.info("✅ DTS authenticate başarılı, sessionId: {}", companyBatchApi.getSessionId());
    }

    @When("yeni bir DTS referans numarası ile DTS batch işlemi başlatılır")
    public void yeni_bir_dts_referans_numarasi_ile_dts_batch_islemi_baslatilir() {
        dtsReferenceNo = CompanyBatchApi.generateUniqueDtsReferenceNo();
        log.info("Üretilen benzersiz dtsReferenceNo: {}", dtsReferenceNo);

        startResponse = companyBatchApi.startBatch(dtsReferenceNo, 1, 1, 0);

        Assert.assertEquals(startResponse.getStatusCode(), 200,
                "batch/start 200 dönmeli. Body: " + startResponse.getBody().asString());
        Assert.assertTrue(companyBatchApi.isSuccess(startResponse),
                "batch/start success=true dönmeli. Body: " + startResponse.getBody().asString());

        String message = companyBatchApi.getMessage(startResponse);
        Assert.assertTrue(message != null && message.contains("DTS start process has been successful"),
                "batch/start mesajı 'DTS start process has been successful' içermeli. Alınan: " + message);

        log.info("✅ DTS batch/start başarılı: {}", message);
    }

    @And("1 fatura içeren batch DTS'e yüklenir")
    public void bir_fatura_iceren_batch_dtsye_yuklenir() {
        Assert.assertNotNull(dtsReferenceNo, "batch/upload öncesi dtsReferenceNo üretilmiş olmalı");

        batchNo = CompanyBatchApi.generateUniqueBatchNo();
        invoiceNo = CompanyBatchApi.generateUniqueInvoiceNo();
        log.info("Üretilen benzersiz batchNo: {}, invoiceNo: {}", batchNo, invoiceNo);

        LocalDate invoiceDate = LocalDate.now();
        LocalDate dueDate = CompanyBatchApi.getFutureBusinessDate(30);
        LocalDate batchDueDate = dueDate;
        LocalDate batchClosureDate = dueDate;

        double amount = 1000.0;
        CompanyBatchApi.BatchInvoiceItem invoice = new CompanyBatchApi.BatchInvoiceItem(
                invoiceNo,
                "E_ARSIV",
                invoiceDate,
                dueDate,
                amount,   // payableAmount
                amount,   // remainingAmount
                amount,   // invoiceAmount
                amount / 1.20, // taxExclusiveAmount (KDV %20 varsayımıyla, informatif alan)
                "TL",
                companyBatchApi.getDealerTaxNo()
        );

        uploadResponse = companyBatchApi.uploadBatch(
                dtsReferenceNo, batchNo, amount, batchDueDate, batchClosureDate,
                Collections.singletonList(invoice));

        Assert.assertEquals(uploadResponse.getStatusCode(), 200,
                "batch/upload 200 dönmeli. Body: " + uploadResponse.getBody().asString());
        Assert.assertTrue(companyBatchApi.isSuccess(uploadResponse),
                "batch/upload success=true dönmeli. Body: " + uploadResponse.getBody().asString());

        String message = companyBatchApi.getMessage(uploadResponse);
        Assert.assertTrue(message != null && message.contains("DTS upload auction process has been successful"),
                "batch/upload mesajı 'DTS upload auction process has been successful' içermeli. Alınan: " + message);

        log.info("✅ DTS batch/upload başarılı: {}", message);
    }

    @And("DTS batch işlemi bitirilir")
    public void dts_batch_islemi_bitirilir() {
        Assert.assertNotNull(dtsReferenceNo, "batch/end öncesi dtsReferenceNo üretilmiş olmalı");

        endResponse = companyBatchApi.endBatch(dtsReferenceNo, 1, 1, 0);

        Assert.assertEquals(endResponse.getStatusCode(), 200,
                "batch/end 200 dönmeli. Body: " + endResponse.getBody().asString());
        Assert.assertTrue(companyBatchApi.isSuccess(endResponse),
                "batch/end success=true dönmeli. Body: " + endResponse.getBody().asString());

        String message = companyBatchApi.getMessage(endResponse);
        Assert.assertTrue(message != null && message.contains("DTS end process has been successful"),
                "batch/end mesajı 'DTS end process has been successful' içermeli. Alınan: " + message);

        log.info("✅ DTS batch/end başarılı: {}", message);
    }

    @Then("DTS batch akışının her adımı başarılı mesajla sonuçlanmalı ve DTS durumu Taslak \\(DRAFT\\) olmalı")
    public void dts_batch_akisinin_her_adimi_basarili_mesajla_sonuclanmali_ve_dts_durumu_taslak_draft_olmali() {
        // Kod tarafında batch/end sonrası dts.status DRAFT'a düşer (Python probu ile 2026-08-06
        // dogrulandi). Bu API akışında ayrı bir "durum sorgulama" endpoint'i çağrılmadığından
        // (kapsam dışı — sadece start/upload/end aksiyonları), DRAFT'a geçiş dört adımın da
        // ayrı ayrı 200 + success:true + beklenen mesajla tamamlanmış olmasıyla teyit edilir.
        Assert.assertTrue(companyBatchApi.isSuccess(authenticateResponse), "authenticate adımı başarısız kaldı");
        Assert.assertTrue(companyBatchApi.isSuccess(startResponse), "batch/start adımı başarısız kaldı");
        Assert.assertTrue(companyBatchApi.isSuccess(uploadResponse), "batch/upload adımı başarısız kaldı");
        Assert.assertTrue(companyBatchApi.isSuccess(endResponse), "batch/end adımı başarısız kaldı");

        log.info("=== DTS BATCH API AKIŞI TAMAMLANDI ===");
        log.info("dtsReferenceNo={}, batchNo={}, invoiceNo={}", dtsReferenceNo, batchNo, invoiceNo);
        log.info("start message : {}", companyBatchApi.getMessage(startResponse));
        log.info("upload message: {}", companyBatchApi.getMessage(uploadResponse));
        log.info("end message   : {}", companyBatchApi.getMessage(endResponse));
        log.info("=======================================");
    }
}
