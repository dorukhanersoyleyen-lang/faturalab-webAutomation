package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.context.TzfScenarioContext;
import com.faturalab.automation.context.TzfScenarioContext.TzfInvoice;
import com.faturalab.automation.db.InvoiceDbAssertions;
import com.faturalab.automation.db.InvoiceDbAssertions.InvoiceAmountRow;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.CompanyInvoicePage;
import com.faturalab.automation.pages.CompanyQuickOfferPage;
import com.faturalab.automation.utils.VaadinGridFilterHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * "Teklif Talebi İptal Akışı" UAT regresyon senaryosunun step tanımları.
 *
 * Kaynak: #5649 canlı reprosunun (WP5649FreshInvoiceRepro, tek seferlik) kalıcı UAT
 * regresyon senaryosuna dönüştürülmüş hâli (2026-08-19). Ortak fatura yükleme/teklif alma
 * adımları {@link TzfIslemUATStepDefs}'ten (aynı glue paketi) reuse edilir — burada sadece
 * iptal + DB doğrulama + tekrar teklif alma adımları tanımlanır.
 */
public class TeklifTalebiIptalUATStepDefs {

    private static final Logger log = LogManager.getLogger(TeklifTalebiIptalUATStepDefs.class);
    private static final Pattern BORDRO_FORMAT = Pattern.compile("[A-Z]\\d{4}_\\d{2,}");

    private CompanyInvoicePage companyInvoicePage;
    private CompanyQuickOfferPage offerPage;

    private CompanyInvoicePage getCompanyInvoicePage() {
        if (companyInvoicePage == null) {
            companyInvoicePage = new CompanyInvoicePage(DriverManager.getDriver());
        }
        return companyInvoicePage;
    }

    private CompanyQuickOfferPage getOfferPage() {
        if (offerPage == null) {
            offerPage = new CompanyQuickOfferPage(DriverManager.getDriver());
        }
        return offerPage;
    }

    @Then("oluşan teklif talebi \\(bordro\\) işlemdekiler sayfasında iptal edilir")
    public void olusanTeklifTalebiIptalEdilir() {
        CompanyQuickOfferPage page = getOfferPage();

        String bordro = null;
        long deadline = System.currentTimeMillis() + 20000L;
        while (System.currentTimeMillis() < deadline && bordro == null) {
            bordro = page.findLatestBordroInGrid();
            if (bordro == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        Assert.assertNotNull(bordro, "İşlemdekiler listesinde teklif talebi (bordro) satırı görünmedi");
        Assert.assertTrue(BORDRO_FORMAT.matcher(bordro).matches(),
                "Bordro no beklenen formatta değil (ör. A2026_78207): " + bordro);
        log.info("[TEKLIF-IPTAL] İptal edilecek bordro: {}", bordro);
        TzfScenarioContext.setBordroNo(bordro);

        boolean cancelled = page.cancelAuctionForBordro(bordro);
        Assert.assertTrue(cancelled,
                "'Teklif talebi iptal edildi.' başarı toast'ı görülmedi — bordro " + bordro);
        log.info("[TEKLIF-IPTAL] Bordro {} başarıyla iptal edildi.", bordro);
    }

    @And("faturanın kalan tutarı DB'de orijinal ödenebilir tutara geri döndüğü doğrulanır")
    public void faturaninKalanTutariDbdeOrijinalOdenebilirTutaraGeriDondugu() {
        TzfInvoice inv = TzfScenarioContext.getInvoices().get(0);
        String invoiceNo = inv.invoiceNo;
        BigDecimal expectedFromExcel = new BigDecimal(inv.amount.replace(".", "").replace(",", "."));

        // Backend commit (remainingAmount restore) toast'tan birazcık geç tamamlanabiliyor
        // (bkz. web-automation.md "backend commit UI toast'tan geç gelir" kalıbı) — tek seferlik
        // okuma yerine kısa aralıklı poll ile taze veriyi bekle.
        Optional<InvoiceAmountRow> rowOpt = Optional.empty();
        long deadline = System.currentTimeMillis() + 15000L;
        while (System.currentTimeMillis() < deadline) {
            rowOpt = InvoiceDbAssertions.findAmountsByInvoiceNo(invoiceNo);
            if (rowOpt.isPresent() && rowOpt.get().remainingAmount != null
                    && rowOpt.get().remainingAmount.compareTo(rowOpt.get().payableAmount) == 0) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        Assert.assertTrue(rowOpt.isPresent(), "DB'de invoiceno=" + invoiceNo + " için kayıt bulunamadı");
        InvoiceAmountRow row = rowOpt.get();
        log.info("[TEKLIF-IPTAL][DB] {}", row);

        // Pozitif kanıt: remainingAmount == payableAmount (rezervasyon TAMAMEN geri alındı) VE
        // payableAmount, Excel'e yazdığımız orijinal tutarla eşleşiyor (yanlış/eski satırı okumadık).
        Assert.assertEquals(row.payableAmount.setScale(2, java.math.RoundingMode.HALF_UP).compareTo(
                        expectedFromExcel.setScale(2, java.math.RoundingMode.HALF_UP)), 0,
                "DB'deki payableAmount, Excel'e yazılan orijinal tutarla eşleşmiyor: DB=" + row.payableAmount
                        + " beklenen=" + expectedFromExcel);
        Assert.assertEquals(row.remainingAmount.setScale(2, java.math.RoundingMode.HALF_UP).compareTo(
                        row.payableAmount.setScale(2, java.math.RoundingMode.HALF_UP)), 0,
                "İptal sonrası remainingAmount, payableAmount'a geri dönmedi (rezervasyon serbest "
                        + "bırakılmamış olabilir): remainingAmount=" + row.remainingAmount
                        + " payableAmount=" + row.payableAmount);

        // ⚠️ Bilinen açık defect #5905 ("İptal Edilince Faturanın auctionCount Alanı Düşmüyor") —
        // kesin/katı assert BİLİNÇLİ OLARAK eklenmiyor, sadece raporlanıyor. #5905 kapandığında
        // burada Assert.assertEquals(row.auctionCount, <iptalden önceki değer>) eklenebilir.
        log.info("[TEKLIF-IPTAL][BİLİNEN DEFECT #5905] invoiceno={} auctionCount={} (iptal sonrası "
                + "düşmesi bekleniyor ama düşmüyor — bu senaryoda ASSERT EDİLMİYOR, sadece raporlanıyor)",
                invoiceNo, row.auctionCount);
    }

    @When("aynı fatura için Yüklenmişler ekranından tekrar teklif alınır")
    public void ayniFaturaIcinTekrarTeklifAlinir() {
        TzfInvoice inv = TzfScenarioContext.getInvoices().get(0);
        String invoiceNo = inv.invoiceNo;

        CompanyInvoicePage invoicePage = getCompanyInvoicePage();
        invoicePage.navigateToInvoiceListForced();

        boolean filtered = VaadinGridFilterHelper.applyOnlyValuesWithRetry(
                DriverManager.getDriver(), "Fatura No", Collections.singletonList(invoiceNo), 3);
        Assert.assertTrue(filtered,
                "Yüklenmişler ekranında Fatura No filtresi uygulanamadı: " + invoiceNo);

        // Filtre uygulanır uygulanmaz grid'in fiilen yeniden çizilmesini bekle (virtual scroll +
        // filtre re-render gecikmesi) — aksi halde clickTeklifAlForInvoice hücrede invoiceNo'yu
        // bulamayıp 'first_fallback'e düşüyor ve tıklama sunucuya işlemeden NEITHER dönüyordu
        // (build kanıtı: filtre sonrası hemen tıklamada satır eşleşmedi).
        String row = null;
        long rowDeadline = System.currentTimeMillis() + 10000L;
        while (System.currentTimeMillis() < rowDeadline && row == null) {
            row = invoicePage.getInvoiceRowText(invoiceNo);
            if (row == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        Assert.assertNotNull(row, "Yüklenmişler ekranında fatura tekrar görünmedi: " + invoiceNo);

        // clickTeklifAlForInvoice'un JS click'i Vaadin grid yeniden render sırasında sunucuya
        // işlemeyebilir (web-automation.md tuzak #2) — bu durumda MODAL_OPENED/ERROR_TOAST değil
        // NEITHER döner. NEITHER = tıklama kaybolmuş demektir, tekrar denenir. ERROR_TOAST/
        // MODAL_OPENED ise KESİN bir iş sonucudur, retry edilmez.
        String result = "NEITHER";
        for (int attempt = 1; attempt <= 3 && "NEITHER".equals(result); attempt++) {
            result = getOfferPage().retryTeklifAlAndObserve(invoiceNo, 15);
            log.info("[TEKLIF-IPTAL] Tekrar TEKLİF AL denemesi {}/3 sonucu: {}", attempt, result);
            if ("NEITHER".equals(result) && attempt < 3) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        TzfScenarioContext.setRetryResult(result);
        log.info("[TEKLIF-IPTAL] Tekrar TEKLİF AL gözlem sonucu (final): {}", result);
    }

    @Then("tekrar teklif alma sırasında Temlik tutarı hatası oluşmadığı doğrulanır")
    public void tekrarTeklifAlmaSirasindaTemlikTutariHatasiOlusmadigiDogrulanir() {
        String result = TzfScenarioContext.getRetryResult();
        Assert.assertNotNull(result, "Tekrar TEKLİF AL denemesi sonucu context'te yok");

        // Bu, #5649'un ana doğrulama noktası: taze/organik tek-döngü senaryoda "Temlik tutarı,
        // kalan tutardan yüksek olamaz." (AuctionInvoiceGroupModel.1) hatası ARTIK OLUŞMAMALI.
        Assert.assertFalse(result.startsWith("ERROR_TOAST"),
                "Tekrar TEKLİF AL sonrası hata toast'ı çıktı (regresyon şüphesi!): " + result);
        Assert.assertEquals(result, "MODAL_OPENED",
                "Tekrar TEKLİF AL sonrası teklif modalı açılmadı (belirsiz sonuç — ne hata ne modal "
                        + "görüldü, pozitif kanıt yok): " + result);
        log.info("[TEKLIF-IPTAL][SONUÇ] Temlik tutarı hatası OLUŞMADI — teklif modalı sorunsuz açıldı.");
    }
}
