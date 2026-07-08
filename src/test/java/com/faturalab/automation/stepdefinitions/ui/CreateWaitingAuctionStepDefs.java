package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.context.TzfScenarioContext;
import io.cucumber.java.en.Then;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * WAITING-001 — Bağımsız WAITING auction oluşturma senaryosunun son adımı.
 *
 * Önceki tüm adımlar (Excel hazırlama, alıcı yükleme, tedarikçi teklif alma,
 * modal onayı) {@link TzfIslemUATStepDefs} içinde TANIMLI ve TZF-001 ile
 * BİREBİR AYNI step metinleriyle yeniden kullanılır — buraya kopyalanmaz.
 *
 * Bu senaryo kasıtlı olarak "Kabul / İptal" adımından ÖNCE durur; auction
 * WAITING durumunda açık kalır (reproduce testi için /offer/ deep-link
 * ihtiyacı).
 */
public class CreateWaitingAuctionStepDefs {

    private static final Logger log = LogManager.getLogger(CreateWaitingAuctionStepDefs.class);

    @Then("auction WAITING durumunda oluşturulduğu için işlem burada durur")
    public void auctionWaitingDurumundaBirakilir() {
        String offeredInvoiceNo = TzfScenarioContext.getOfferedInvoiceNo();
        String supplierName = ConfigReader.getProperty("tzf.supplier.name");
        String buyerIdentifier = ConfigReader.getProperty("tzf.buyer.impersonate.identifier");
        log.info("[WAITING-AUCTION] Kabul/İptal adımı BİLİNÇLİ OLARAK atlandı — auction WAITING durumunda bırakıldı.");
        log.info("[WAITING-AUCTION] Tedarikçi (identifier): {}", supplierName);
        log.info("[WAITING-AUCTION] Alıcı (identifier): {}", buyerIdentifier);
        log.info("[WAITING-AUCTION] Teklif alınan fatura no: {}", offeredInvoiceNo);
        TzfScenarioContext.getInvoices().forEach(inv ->
                log.info("[WAITING-AUCTION] Üretilen fatura: no={} tutar={} vade={}",
                        inv.invoiceNo, inv.amount, inv.dueDate));
    }
}
