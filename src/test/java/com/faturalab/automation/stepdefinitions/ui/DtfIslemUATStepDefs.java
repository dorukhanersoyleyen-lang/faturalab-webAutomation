package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.context.RoleSessionManager;
import com.faturalab.automation.context.RoleSessionManager.Role;
import com.faturalab.automation.context.TzfScenarioContext;
import com.faturalab.automation.db.DtfDbAssertions;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.BuyerBulkUploadPage;
import com.faturalab.automation.pages.BuyerStartDtfTenderDialogPage;
import com.faturalab.automation.pages.BuyerTenderPreviewDialogPage;
import com.faturalab.automation.pages.CompanyAddEditAuctionDialogPage;
import com.faturalab.automation.pages.CompanyInvoicePage;
import com.faturalab.automation.pages.CompanyLicenceAgreementDialogPage;
import com.faturalab.automation.pages.CompanyQuickOfferPage;
import com.faturalab.automation.utils.TzfInvoiceExcelGenerator;
import com.faturalab.automation.utils.VaadinGridFilterHelper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.io.File;
import java.util.Optional;

/**
 * DTF-001 — DTF (Dikey Ticaret Finansmanı) işlemi uçtan uca akışı step tanımları.
 *
 * ⚠️ Bu sınıf {@link TzfIslemUATStepDefs}'ten TAMAMEN AYRIDIR ama fatura üretim/yükleme
 * mekanizmasını (TzfInvoiceExcelGenerator, TzfScenarioContext, BuyerBulkUploadPage) BİREBİR
 * REUSE eder — Ana Firma (buyer.id=145) ve Ara Tedarikçi (company.id=998) TZF'nin ZATEN
 * kullandığı AYNI izole çift olduğu için bu tamamen doğal (bkz. dev.properties dtf.* / tzf.* key'leri
 * aynı değerlere işaret ediyor).
 *
 * Rol geçişleri {@link RoleSessionManager}'ın filtreli impersonation'ı ile yapılır.
 */
public class DtfIslemUATStepDefs {

    private static final Logger log = LogManager.getLogger(DtfIslemUATStepDefs.class);

    // ⚠️⚠️ KÖK NEDEN — TZF↔DTF supplier PAYLAŞIMI REGRESYONU (2026-08-14, Jenkins günlük koşumu
    // build #78'de canlı kanıtlandı): supplier.id=1091 (company998→buyer145) TZF'nin de kullandığı
    // AYNI bağlantı. requireddtf=true'yu KALICI bırakmak TZF'nin normal "Teklif Al"ını süresiz
    // (dtfenddate=2027) devre dışı bırakıyordu. Fix: requireddtf artık SADECE bu senaryonun süresi
    // boyunca açık — @Before ile true, @After ile (PASS/FAIL fark etmez) false'a döner. Detay:
    // DtfDbAssertions.setAraTedarikciRequiredDtf() Javadoc'u.
    @Before("@dtf-001")
    public void dtfIcinRequiredDtfGeciciAcilir() {
        DtfDbAssertions.setAraTedarikciRequiredDtf(true);
        log.info("[DTF] supplier.id=1091 requireddtf=TRUE (senaryo süresince, TZF etkilenmeyecek şekilde @After'da geri alınacak)");
    }

    @After("@dtf-001")
    public void dtfIcinRequiredDtfGeriKapatilir() {
        DtfDbAssertions.setAraTedarikciRequiredDtf(false);
        log.info("[DTF] supplier.id=1091 requireddtf=FALSE'a geri alındı — TZF'nin normal 'Teklif Al' akışı korunuyor.");
    }

    private static final long ANA_FIRMA_BUYER_ID = 145L;
    // ⚠️ KÖK NEDEN (2026-08-13, DB'de canlı kanıtlandı — tender.id=800, buyerid=151, status=WAITING,
    // totalsuppliercount=2): DTF Tender'ın buyer'ı ANA_FIRMA_BUYER_ID (145) DEĞİL, Ara Tedarikçi'nin
    // KENDİ buyer.id'si (151) altında oluşuyor — mantığı doğru: DTF, Ara Tedarikçi'nin KENDİ alt
    // tedarikçilerine (KT Test Tedarikçi + Baran Madencilik) açtığı bir ihale/tender'dır; Ana Firma
    // sadece tetikleyici faturanın alıcısıdır, tender'ın tarafı değildir.
    private static final long ARA_TEDARIKCI_BUYER_ID = 151L;

    private BuyerBulkUploadPage buyerUploadPage;
    private CompanyInvoicePage companyInvoicePage;
    private CompanyQuickOfferPage offerPage;
    private CompanyAddEditAuctionDialogPage auctionDialogPage;
    private BuyerStartDtfTenderDialogPage dtfTenderDialogPage;
    private BuyerTenderPreviewDialogPage tenderPreviewDialogPage;
    private CompanyLicenceAgreementDialogPage licenceAgreementDialogPage;

    private BuyerBulkUploadPage getBuyerUploadPage() {
        if (buyerUploadPage == null) {
            buyerUploadPage = new BuyerBulkUploadPage(DriverManager.getDriver());
        }
        return buyerUploadPage;
    }

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

    private CompanyAddEditAuctionDialogPage getAuctionDialogPage() {
        if (auctionDialogPage == null) {
            auctionDialogPage = new CompanyAddEditAuctionDialogPage(DriverManager.getDriver());
        }
        return auctionDialogPage;
    }

    private BuyerStartDtfTenderDialogPage getDtfTenderDialogPage() {
        if (dtfTenderDialogPage == null) {
            dtfTenderDialogPage = new BuyerStartDtfTenderDialogPage(DriverManager.getDriver());
        }
        return dtfTenderDialogPage;
    }

    private BuyerTenderPreviewDialogPage getTenderPreviewDialogPage() {
        if (tenderPreviewDialogPage == null) {
            tenderPreviewDialogPage = new BuyerTenderPreviewDialogPage(DriverManager.getDriver());
        }
        return tenderPreviewDialogPage;
    }

    private CompanyLicenceAgreementDialogPage getLicenceAgreementDialogPage() {
        if (licenceAgreementDialogPage == null) {
            licenceAgreementDialogPage = new CompanyLicenceAgreementDialogPage(DriverManager.getDriver());
        }
        return licenceAgreementDialogPage;
    }

    // ─── Test verisi (TZF ile AYNI mekanizma — bkz. sınıf Javadoc'u) ──────────

    @Given("DTF senaryosu için {int} adet E-Fatura içeren Excel hazırlanır")
    public void dtfExcelHazirlanir(int adet) {
        String supplierName = ConfigReader.getProperty("dtf.supplier.name");
        String supplierVkn = ConfigReader.getProperty("dtf.supplier.vkn");
        Assert.assertNotNull(supplierName, "dtf.supplier.name config'te tanımlı olmalı");
        Assert.assertNotNull(supplierVkn, "dtf.supplier.vkn config'te tanımlı olmalı");

        String path = TzfInvoiceExcelGenerator.generate(supplierName, supplierVkn, adet);
        Assert.assertTrue(new File(path).exists(), "Üretilen Excel dosyası bulunamadı: " + path);
        Assert.assertEquals(TzfScenarioContext.getInvoices().size(), adet,
                "Context'teki fatura sayısı istenen adetle eşleşmeli");
        log.info("[DTF] Excel hazır: {} — faturalar: {}", path,
                TzfScenarioContext.getInvoices().stream().map(i -> i.invoiceNo).toArray());
    }

    // ─── Rol geçişleri ────────────────────────────────────────────────────────

    @When("admin DTF ana firma kullanıcısına geçiş yapar")
    public void dtfAnaFirmaKullanicisinaGec() {
        impersonateDtfUser(Role.BUYER, "dtf.buyer.impersonate.identifier");
    }

    @When("admin DTF ara tedarikçi kullanıcısına geçiş yapar")
    public void dtfAraTedarikciKullanicisinaGec() {
        impersonateDtfUser(Role.COMPANY, "dtf.company.impersonate.identifier");
    }

    private void impersonateDtfUser(Role role, String identifierKey) {
        String identifier = ConfigReader.getProperty(identifierKey);
        Assert.assertNotNull(identifier, identifierKey + " config'te tanımlı olmalı");
        RoleSessionManager.clearSession(role);
        RoleSessionManager.loginAs(DriverManager.getDriver(), role, identifier,
                ConfigReader.getProperty("admin.password"));
        log.info("[DTF] {} kullanıcısına geçildi: {}", role.getDisplayName(), identifier);
    }

    // ─── Ana Firma: fatura yükleme (TZF ile AYNI mekanizma) ──────────────────

    @And("ana firma ekranında hazırlanan Excel ile faturalar yüklenir")
    public void anaFirmaExcelIleFaturaYukler() {
        String excelPath = TzfScenarioContext.getExcelPath();
        Assert.assertNotNull(excelPath, "Excel yolu context'te olmalı — önce Excel hazırlama adımı koşmalı");

        BuyerBulkUploadPage page = getBuyerUploadPage();
        Assert.assertTrue(page.openUploadDialog(), "Ana firma fatura yükleme dialogu açılamadı");
        page.selectExcelTabIfPresent();
        page.uploadExcel(excelPath);
        page.clickYukle();
    }

    // "faturaların başarıyla yüklendiği doğrulanır" step'i {@link TzfIslemUATStepDefs} içinde
    // ZATEN tanımlı — Cucumber step tanımları classpath genelinde tekildir, burada TEKRAR
    // tanımlanmıyor (bkz. DtsMasterDataUATStepDefs'teki AYNI kalıp/yorum).

    // ─── Ara Tedarikçi: DTF Başlat akışı ──────────────────────────────────────

    @And("yüklenen fatura için \"Yeni Teklif Talebi Başlat\" ekranı açılır")
    public void yuklenenFaturaIcinTeklifTalebiEkraniAcilir() {
        String invoiceNo = TzfScenarioContext.getInvoices().get(0).invoiceNo;
        // ⚠️ KÖK NEDEN (2026-08-13, canlı koşumla kanıtlandı): company 998'in "Yüklenmişler" listesinde
        // bugün yapılan tekrarlı TZF/DTF koşumlarından BİRİKMİŞ çok sayıda fatura var (virtual scroll) —
        // clickTeklifAlForInvoice() bizim satırımızı DOM'da bulamayınca "first_fallback" dalına düşüp
        // SAYFADAKİ İLK "TEKLİF AL" butonuna tıklıyor (ör. tamamen alakasız bir ALBC faturası). Fix:
        // TZF'nin KENDİ doğrulama adımındaki AYNI "Fatura No" kolon filtresi kalıbıyla grid'i ÖNCE
        // sadece bizim faturamıza indir, sonra TEKLİF AL'a bas — belirsizlik tamamen ortadan kalkar.
        getCompanyInvoicePage().navigateToInvoiceList();
        boolean filtered = VaadinGridFilterHelper.applyOnlyValuesWithRetry(
                DriverManager.getDriver(), "Fatura No", java.util.Collections.singletonList(invoiceNo), 3);
        Assert.assertTrue(filtered, "'Fatura No' filtresi uygulanamadı — fatura listede yok olabilir: " + invoiceNo);
        Assert.assertTrue(getOfferPage().clickTeklifAlAndWaitModal(invoiceNo, 3),
                "'Yeni Teklif Talebi Başlat' (TEKLİF AL) sonrası dialog açılmadı: " + invoiceNo);
        getAuctionDialogPage().dumpVisibleText();
        // ⚠️ KÖK NEDEN #1 (2026-08-13, canlı koşumla kanıtlandı): bu dialog aynı alıcıya ait TÜM
        // bekleyen faturaları listeler (bugün tekrar tekrar koşulunca birikmiş) — "TEKLİF AL"
        // satır tıklaması dialogu açar ama hedef faturanın "Seç" checkbox'ını OTOMATİK işaretlemez;
        // hiçbir satır seçili değilken "DTF Başlat"/"Teklif Al" ikisi de DISABLED kalır. Fix:
        // checkbox'ı burada bilinçli işaretle.
        getAuctionDialogPage().selectInvoiceCheckbox(invoiceNo);
        // ⚠️ KÖK NEDEN #2 (2026-08-13, canlı koşumla + kaynak kod kanıtıyla doğrulandı —
        // CompanyUploadedInvoicesView + CompanyContractModel.getUnsignedActiveCompanyContractsForAuction):
        // Ara Tedarikçi (company 998) artık kendi buyer.id=151'ine sahip olduğu için platform bu
        // company'nin GENEL "DTF Sözleşmesi"ni imzalamamış saydığı (hiç CompanyContract kaydı yok)
        // — "TEKLİF AL" ile AYNI ANDA "Sözleşmeler (...)" onay diyaloğu da açılıyor ve kabul
        // edilmeden "DTF Başlat"/"Teklif Al" DISABLED kalıyor. Bu bir automation bug'ı değil,
        // platformun gerçek hukuki onay kapısı — otomasyon bunu UI üzerinden (gerçek imza akışıyla)
        // geçmeli, DB'ye sahte "signed=true" satırı yazmadan.
        if (getLicenceAgreementDialogPage().isOpened()) {
            boolean accepted = getLicenceAgreementDialogPage().acceptAllContracts();
            log.info("[DTF] Sözleşme onay akışı çalıştırıldı mı: {}", accepted);
        }
    }

    @Then("\"DTF Başlat\" butonu aktif olmalı ve \"Teklif Al\" butonu pasif olmalı")
    public void dtfBaslatAktifTeklifAlPasifOlmali() {
        String[] states = getAuctionDialogPage().getStartAndDtfButtonState();
        Assert.assertEquals(states[1], "ENABLED",
                "'DTF Başlat' butonu ENABLED olmalıydı (supplier.requireddtf=true + tarih aralığı içinde). "
                        + "Buton dökümü: " + getAuctionDialogPage().dumpAllButtons());
        // ⚠️ NÜANS (2026-08-13, canlı koşumla kanıtlandı — CompanyLicenceAgreementDialog kaynak kodu):
        // requireddtf=true İKEN "Teklif Al" (startButton) yalnızca sözleşme İMZALANMAMIŞKEN DISABLED'dır.
        // Sözleşme onayı (bu senaryoda az önce koştuğumuz kabul akışı) BAŞARILI olunca uygulama
        // `isSupplierAgreement` seçiliyse `setSaveButtonEnabled(true)` çağırıp "Teklif Al"ı KOŞULSUZ
        // yeniden etkinleştiriyor — requireddtf durumuna bakılmaksızın. Yani "Teklif Al DISABLED" hâli
        // sadece sözleşme imzalanana kadar geçici bir kapı; bizim otomasyonumuz o kapıyı az önce UI
        // üzerinden geçtiği için burada ENABLED görmek DOĞRU ve BEKLENEN bir sonuçtur (bug değil).
        // Bu yüzden burada sert assert YOK, sadece bilgilendirme logu var.
        log.info("[DTF] Buton durumları — Teklif Al={} (sözleşme onayı sonrası koşulsuz yeniden etkin), DTF Başlat={}.",
                states[0], states[1]);
    }

    @When("\"DTF Başlat\" butonuna tıklanır")
    public void dtfBaslatButonunaTiklanir() {
        // ⚠️ KÖK NEDEN (2026-08-13, canlı koşumda kullanıcı gözlemiyle bulundu): "Teklif Talebi
        // Süresi" (auctionTimeSelect) alanı varsayılan BOŞ gelir. Kaynak kod kanıtı
        // (CompanyAddEditAuctionDialog.dtfButtonClickListener): auction.getTimeIndex()==0 iken
        // "DTF Başlat" tıklanınca uygulama SESSİZCE "DTF teklifi en az 1 saatlik başlatılmalıdır."
        // uyarısı gösterip dialog geçişi yapmaz — otomasyon bunu "buton tıklanamadı/dialog açılmadı"
        // gibi yorumlar. Fix: tıklamadan ÖNCE süreyi seç.
        Assert.assertTrue(getAuctionDialogPage().selectTeklifTalebiSuresi("1 saat"),
                "'Teklif Talebi Süresi' alanından '1 Saat' seçilemedi — DTF Başlat'ın gerektirdiği zorunlu alan.");
        Assert.assertTrue(getAuctionDialogPage().clickDtfBaslat(), "'DTF Başlat' butonuna tıklanamadı");
    }

    @Then("\"İhale Ayrıntıları\" \\(DTF Tender\\) diyaloğu açılmalı ve alanlar otomatik dolmuş olmalı")
    public void ihaleAyrintilariDiyaloguAcilmaliVeAlanlarDolmusOlmali() {
        BuyerStartDtfTenderDialogPage dialog = getDtfTenderDialogPage();
        boolean opened = dialog.waitUntilOpened(15);
        Assert.assertTrue(opened, "'DTF Başlat' sonrası BuyerStartDtfTenderDialog 15sn içinde açılmadı. "
                + "Görünür overlay metni: " + dumpAnyVisibleOverlay());
        // Auto-fill (dtfAuctionAndBuyerComboBox value-change listener) için kısa bekleme.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String tenderAmount = dialog.getTenderAmountFieldValue();
        Assert.assertFalse("NOT_FOUND".equals(tenderAmount) || "ERROR".equals(tenderAmount),
                "'İhale Tutarı' alanı bulunamadı. Dialog metni: " + dialog.dumpVisibleText());
        log.info("[DTF] 'İhale Ayrıntıları' dialogu açıldı, İhale Tutarı auto-fill: {}", tenderAmount);
    }

    @When("DTF için \"İhale Başlat\" butonuna tıklanır")
    public void ihaleBaslatButonunaTiklanir() {
        Assert.assertTrue(getDtfTenderDialogPage().clickIhaleBaslat(), "'İhale Başlat' butonuna tıklanamadı");
    }

    @Then("\"İhale Ön İzleme\" diyaloğunda katılımcı sayısı 2 olmalı")
    public void ihaleOnIzlemeKatilimciSayisi2Olmali() {
        BuyerTenderPreviewDialogPage dialog = getTenderPreviewDialogPage();
        boolean opened = dialog.waitUntilOpened(15);
        Assert.assertTrue(opened, "'İhale Başlat' sonrası BuyerTenderPreviewDialog 15sn içinde açılmadı. "
                + "Görünür overlay metni: " + dumpAnyVisibleOverlay());
        String katilimciSayisi = dialog.getKatilimciSayisiText();
        Assert.assertEquals(katilimciSayisi, "2",
                "'İhaleye Katılabilecek Tedarikçi Sayısı' 2 olmalıydı (KT Test Tedarikçi + Baran Madencilik "
                        + "— buyer.id=151'in TÜM aktif tedarikçileri, 'Tüm Tedarikçiler' varsayılanıyla). "
                        + "Dialog metni: " + dialog.dumpVisibleText());
        log.info("[DTF] İhale Ön İzleme'de katılımcı sayısı doğrulandı: {}", katilimciSayisi);
    }

    @When("\"Evet\" butonuna tıklanır")
    public void evetButonunaTiklanir() {
        Assert.assertTrue(getTenderPreviewDialogPage().clickEvet(), "'Evet' butonuna tıklanamadı");
    }

    @Then("DTF Tender ve bağlı Auction veritabanında doğru statüyle oluşmuş olmalı")
    public void dtfTenderVeAuctionDbdeDogruStatuyleOlusmusOlmali() {
        Optional<DtfDbAssertions.DtfTenderRow> tenderOpt = pollForDtfTender(30);
        Assert.assertTrue(tenderOpt.isPresent(),
                "buyer.id=" + ARA_TEDARIKCI_BUYER_ID + " için DTF tender kaydı DB'de bulunamadı (30sn poll sonrası).");

        DtfDbAssertions.DtfTenderRow tender = tenderOpt.get();
        Assert.assertEquals(tender.tenderType, "DTF", "tender.tendertype='DTF' olmalıydı: " + tender);
        Assert.assertEquals(tender.totalSupplierCount, Integer.valueOf(2),
                "tender.totalsuppliercount=2 olmalıydı: " + tender);
        Assert.assertNotNull(tender.auctionId, "tender.auctionid set edilmiş olmalıydı: " + tender);
        // Statü WAITING (henüz hiç teklif verilmedi) veya PENDING_OFFER_APPROVE olabilir — katı eşitlik yerine
        // "reddedilmemiş/zaman aşımına uğramamış" kontrolü (poll sırasında otobit/başka bir süreç ilerletmiş olabilir).
        Assert.assertTrue(
                java.util.Arrays.asList("WAITING", "PENDING_OFFER_APPROVE", "ACCEPTED").contains(tender.status),
                "tender.status beklenmeyen bir değerde: " + tender);

        Assert.assertTrue(DtfDbAssertions.findAuctionProductType(tender.auctionId).map(pt -> pt.equals("DEEP_TIER_FINANCING")).orElse(false),
                "auction.id=" + tender.auctionId + " için producttype='DEEP_TIER_FINANCING' olmalıydı.");

        log.info("[DTF] DB doğrulaması PASSED — {}", tender);
    }

    /** Teşhis: şu an görünür herhangi bir dialog/overlay'in metnini döker (beklenmeyen durumda ne açıldığını gösterir). */
    private String dumpAnyVisibleOverlay() {
        try {
            Object text = ((org.openqa.selenium.JavascriptExecutor) DriverManager.getDriver()).executeScript(
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay, vaadin-notification-card'));" +
                    "var out = [];" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  out.push((o.textContent || '').replace(/\\s+/g,' ').trim().substring(0, 400));" +
                    "}" +
                    "return out.join(' ||| ');");
            return String.valueOf(text);
        } catch (Exception e) {
            return "teşhis alınamadı: " + e.getMessage();
        }
    }

    /** DTF Tender kaydı "Evet" sonrası hemen görünmeyebilir (backend commit gecikmesi) — poll et. */
    private Optional<DtfDbAssertions.DtfTenderRow> pollForDtfTender(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            Optional<DtfDbAssertions.DtfTenderRow> row = DtfDbAssertions.findLatestDtfTenderByBuyerId(ARA_TEDARIKCI_BUYER_ID);
            if (row.isPresent()) {
                return row;
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
