package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.api.CompanyBatchApi;
import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.context.RoleSessionManager;
import com.faturalab.automation.context.RoleSessionManager.Role;
import com.faturalab.automation.db.DtsDbAssertions;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.AdminDtsDialogPage;
import com.faturalab.automation.pages.AdminDtsReportPage;
import com.faturalab.automation.pages.CompanyDetailDtsSettingsDialogPage;
import com.faturalab.automation.pages.CompanyDtsAllocationDialogPage;
import com.faturalab.automation.pages.CompanyDtsListPage;
import com.faturalab.automation.pages.CompanyDtsSettingsDialogPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * DTS (Doğrudan Tahsilat Sistemi) — konsolide uçtan-uca UI senaryosu (DTS-001).
 *
 * Tedarikçi: Test Otomasyon Sadece Tedarikçi (company.id=998) — dts.* config anahtarları
 * (dev.properties). Her koşumda taze bir DTS taslak kaydı {@link CompanyBatchApi} REST
 * entegrasyonu (authenticate → batch/start → batch/upload → batch/end) ile üretilir; statik/
 * tükenmiş bir referans numarasına (eski Petek A.Ş. DTS_10 / DTS_110000) bağımlılık YOK.
 * Ardından tedarikçi rolüne geçilip UI'da ayar → hesaplama → "Başlat" akışı doğrulanır.
 */
public class DtsIslemUATStepDefs {

    private static final Logger log = LogManager.getLogger(DtsIslemUATStepDefs.class);

    private final CompanyBatchApi companyBatchApi = new CompanyBatchApi();

    private String dtsReferenceNo;
    /** API adımında üretilen fatura numarası — {@code dbsdocs.docno} ile DB çapraz doğrulaması için saklanır. */
    private String generatedInvoiceNo;
    /** API adımında üretilen fatura tutarı — {@code dbsdocs.dbsamount} ile DB çapraz doğrulaması için saklanır. */
    private double generatedInvoiceAmount;
    /** Beklenen finansal kurum (Akbank T.A.Ş., factoring.id=57) — Otomasyon Bayi'nin (dealer.id=38) TEK
     *  dealerlimit kaydı sadece Akbank'a bağlı olduğu için algoritma her koşumda bunu seçer (2026-08-06 DB kanıtlı). */
    private static final long BEKLENEN_FACTORING_ID = 57L;

    private CompanyDtsListPage dtsListPage;
    private CompanyDtsSettingsDialogPage dtsSettingsDialogPage;
    private CompanyDetailDtsSettingsDialogPage detailDtsSettingsDialogPage;
    private CompanyDtsAllocationDialogPage dtsAllocationDialogPage;
    private AdminDtsReportPage adminDtsReportPage;
    private AdminDtsDialogPage adminDtsDialogPage;

    private CompanyDtsAllocationDialogPage getDtsAllocationDialogPage() {
        if (dtsAllocationDialogPage == null) {
            dtsAllocationDialogPage = new CompanyDtsAllocationDialogPage(DriverManager.getDriver());
        }
        return dtsAllocationDialogPage;
    }

    private AdminDtsReportPage getAdminDtsReportPage() {
        if (adminDtsReportPage == null) {
            adminDtsReportPage = new AdminDtsReportPage(DriverManager.getDriver());
        }
        return adminDtsReportPage;
    }

    private AdminDtsDialogPage getAdminDtsDialogPage() {
        if (adminDtsDialogPage == null) {
            adminDtsDialogPage = new AdminDtsDialogPage(DriverManager.getDriver());
        }
        return adminDtsDialogPage;
    }

    private CompanyDtsListPage getDtsListPage() {
        if (dtsListPage == null) {
            dtsListPage = new CompanyDtsListPage(DriverManager.getDriver());
        }
        return dtsListPage;
    }

    private CompanyDtsSettingsDialogPage getDtsSettingsDialogPage() {
        if (dtsSettingsDialogPage == null) {
            dtsSettingsDialogPage = new CompanyDtsSettingsDialogPage(DriverManager.getDriver());
        }
        return dtsSettingsDialogPage;
    }

    private CompanyDetailDtsSettingsDialogPage getDetailDtsSettingsDialogPage() {
        if (detailDtsSettingsDialogPage == null) {
            detailDtsSettingsDialogPage = new CompanyDetailDtsSettingsDialogPage(DriverManager.getDriver());
        }
        return detailDtsSettingsDialogPage;
    }

    // ---- API ile taze DTS taslak kaydı üretimi ----

    @Given("API ile taze bir DTS taslak kaydı üretilir")
    public void apiIleTazeBirDtsTaslakKaydiUretilir() {
        Response authResponse = companyBatchApi.authenticate();
        Assert.assertEquals(authResponse.getStatusCode(), 200,
                "DTS authenticate 200 dönmeli. Body: " + authResponse.getBody().asString());
        Assert.assertTrue(companyBatchApi.isSuccess(authResponse),
                "DTS authenticate success=true dönmeli. Body: " + authResponse.getBody().asString());

        dtsReferenceNo = CompanyBatchApi.generateUniqueDtsReferenceNo();
        log.info("[DTS] Üretilen benzersiz dtsReferenceNo: {}", dtsReferenceNo);

        Response startResponse = companyBatchApi.startBatch(dtsReferenceNo, 1, 1, 0);
        Assert.assertTrue(companyBatchApi.isSuccess(startResponse),
                "DTS batch/start başarısız. Body: " + startResponse.getBody().asString());

        String batchNo = CompanyBatchApi.generateUniqueBatchNo();
        generatedInvoiceNo = CompanyBatchApi.generateUniqueInvoiceNo();
        LocalDate invoiceDate = LocalDate.now();
        LocalDate dueDate = CompanyBatchApi.getFutureBusinessDate(30);
        generatedInvoiceAmount = 1000.0;

        CompanyBatchApi.BatchInvoiceItem invoice = new CompanyBatchApi.BatchInvoiceItem(
                generatedInvoiceNo,
                "E_ARSIV",
                invoiceDate,
                dueDate,
                generatedInvoiceAmount,          // payableAmount
                generatedInvoiceAmount,          // remainingAmount
                generatedInvoiceAmount,          // invoiceAmount
                generatedInvoiceAmount / 1.20,   // taxExclusiveAmount
                "TL",
                companyBatchApi.getDealerTaxNo());

        Response uploadResponse = companyBatchApi.uploadBatch(
                dtsReferenceNo, batchNo, generatedInvoiceAmount, dueDate, dueDate, Collections.singletonList(invoice));
        Assert.assertTrue(companyBatchApi.isSuccess(uploadResponse),
                "DTS batch/upload başarısız. Body: " + uploadResponse.getBody().asString());

        Response endResponse = companyBatchApi.endBatch(dtsReferenceNo, 1, 1, 0);
        Assert.assertTrue(companyBatchApi.isSuccess(endResponse),
                "DTS batch/end başarısız. Body: " + endResponse.getBody().asString());

        log.info("[DTS] API ile taze taslak DTS kaydı üretildi: {}", dtsReferenceNo);
    }

    // ---- Rol geçişi + navigasyon ----

    @Given("dts tedarikcisi olarak giriş yapılır")
    public void dtsTedarikcisiGiris() {
        String identifier = ConfigReader.getProperty("dts.supplier.impersonate.identifier");
        Assert.assertNotNull(identifier, "dts.supplier.impersonate.identifier config'te tanımlı olmalı");
        RoleSessionManager.clearSession(Role.COMPANY);
        RoleSessionManager.loginAs(DriverManager.getDriver(), Role.COMPANY, identifier,
                ConfigReader.getProperty("admin.password"));
        log.info("[DTS] Tedarikçiye geçildi: {}", identifier);
    }

    @When("tedarikçi \"Doğrudan Tahsilat Sistemi\" menüsüne gider")
    public void dtsMenusuneGider() {
        Assert.assertTrue(getDtsListPage().navigateToDts(),
                "'Doğrudan Tahsilat Sistemi' menüsü bulunamadı/tıklanamadı");
    }

    // ---- Taslak kayıt doğrulama + ayar diyaloğu ----

    @Then("üretilen DTS kaydı Taslak durumunda listede görünmeli")
    public void uretilenDtsKaydiTaslakDurumundaListedeGorunmeli() {
        Assert.assertNotNull(dtsReferenceNo, "Bu adımdan önce 'API ile taze bir DTS taslak kaydı üretilir' çalışmış olmalı");
        CompanyDtsListPage page = getDtsListPage();
        boolean found = page.findDraftDtsByReferenceNo(dtsReferenceNo);
        Assert.assertTrue(found,
                dtsReferenceNo + " kaydı, varsayılan tarih filtre çipi kaldırıldıktan sonra dahi "
                        + "'DTS No' kolon filtresiyle bulunamadı.");
        List<String> rows = page.dumpVisibleRowsGrouped();
        boolean isDraftRow = rows.stream().anyMatch(r -> r.contains(dtsReferenceNo) && r.contains("Taslak"));
        Assert.assertTrue(isDraftRow,
                dtsReferenceNo + " gridde bulundu ama satırda 'Taslak' statüsü görünmüyor: " + rows);
        log.info("[DTS] {} Taslak statüsünde doğrulandı.", dtsReferenceNo);
    }

    @When("filtrelenmiş taslak DTS kaydı için \"Devam Et\" butonuna tıklanır")
    public void filtrelenmisTaslakDtsKaydiIcinDevamEtTiklanir() {
        // ⚠️ 2026-08-06 canlı gözlem: "DTS No" kolon filtresiyle grid tam 1 kayda indirilince
        // clickActionButtonForRowWithStatus'ün y-bazlı satır gruplama toleransı buton hücresini
        // metin hücrelerinden ayrı bir gruba düşürüyor (buton bileşeni render yüksekliği farkı) —
        // tek satır garantiliyken doğrudan görünür TEK aksiyon butonuna basılır.
        boolean clicked = getDtsListPage().clickOnlySingleRowActionButton();
        Assert.assertTrue(clicked, "Filtrelenmiş Taslak satırındaki 'Devam Et' butonu bulunamadı/tıklanamadı");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("\"Doğrudan Tahsilat Ayarı\" diyaloğu açılmalı")
    public void dogrudanTahsilatAyariDiyaloguAcilmali() {
        CompanyDtsSettingsDialogPage dialog = getDtsSettingsDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(),
                "'Devam Et' tıklamasından sonra CompanyDtsSettingsDialog ('Doğrudan Tahsilat Ayarı') açılmadı");
        log.info("[DTS] CompanyDtsSettingsDialog başarıyla doğrulandı.");
    }

    @When("Doğrudan Tahsilat Ayarı diyaloğunda \"Devam Et\" ile ilerlenir")
    public void dogrudanTahsilatAyariDiyaloguIleIlerlenir() {
        CompanyDtsSettingsDialogPage dialog = getDtsSettingsDialogPage();
        Assert.assertTrue(dialog.isOpened(), "CompanyDtsSettingsDialog açık değilken 'Devam Et' denenmiş");
        boolean continued = dialog.clickContinue();
        Assert.assertTrue(continued, "CompanyDtsSettingsDialog içindeki 'Devam Et' butonu bulunamadı/tıklanamadı");
    }

    @Then("\"Yeni Doğrudan Tahsilat Bilgileri\" diyaloğu açılmalı")
    public void yeniDogrudanTahsilatBilgileriDiyaloguAcilmali() {
        CompanyDetailDtsSettingsDialogPage dialog = getDetailDtsSettingsDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(),
                "'Devam Et' onayından sonra CompanyDetailDtsSettingsDialog ('Yeni Doğrudan Tahsilat Bilgileri') açılmadı");
        log.info("[DTS] CompanyDetailDtsSettingsDialog başarıyla doğrulandı.");
    }

    @When("\"Başlat\" butonuna tıklanıp hesaplama onaylanır")
    public void baslatButonunaTiklanipHesaplamaOnaylanir() {
        boolean started = getDetailDtsSettingsDialogPage().clickStart();
        Assert.assertTrue(started, "'Başlat' butonu bulunamadı/tıklanamadı veya onay dialogu kapanmadı");
    }

    @Then("üretilen DTS kaydının statüsü Hesaplanıyor'a geçmeli")
    public void uretilenDtsKaydininStatusuHesaplaniyoraGecmeli() {
        // ⚠️ 2026-08-06 canlı gözlem (DB kanıtlı): tek bordrolu/1 faturalı bu tip kayıtlar için
        // DtsAllocationModel.dtsCalculation() arka plan thread'i CALCULATING penceresini
        // saniyeler içinde geçip PENDING'e (Onay Bekliyor) ulaşabiliyor — 40sn'lik UI poll bu
        // geçici pencereyi hiç yakalamayabilir. Gerçek başarı sinyali: statü CALCULATING
        // *veya* PENDING (ikisi de "Başlat" hesaplamayı gerçekten tetikledi" demektir; DRAFT/
        // Taslak'ta kalması FAIL'dir).
        boolean advanced = getDtsListPage().waitForStatusFold("hesaplaniyor", 15)
                || getDtsListPage().waitForStatusFold("onay bekliyor", 30);
        Assert.assertTrue(advanced,
                "'Başlat' onayından sonra " + dtsReferenceNo + " satırı ne 'Hesaplanıyor' ne 'Onay Bekliyor' "
                        + "statüsüne geçti — hesaplama tetiklenmemiş olabilir.");
        log.info("[DTS] {} statüsü Taslak'tan ilerledi (Hesaplanıyor/Onay Bekliyor) doğrulandı.", dtsReferenceNo);
    }

    // ---- Company onayı: Gözat -> CompanyDtsAllocationDialog -> Onayla -> SEND (Gönderiliyor) ----

    @When("tedarikçi filtrelenmiş DTS kaydı için \"Gözat\" butonuna tıklar")
    public void tedarikciFiltrelenmisDtsKaydiIcinGozatButonunaTiklar() {
        Assert.assertNotNull(dtsReferenceNo, "Bu adımdan önce DTS kaydı üretilmiş olmalı");
        // Statü artık DRAFT değil (Hesaplanıyor/Onay Bekliyor) — liste "Devam Et" yerine "Gözat"
        // gösterir (CompanyTendersView.7, kaynak kod kanıtlı). Kolon filtresi tekrar uygulanır
        // (dialog kapanışından sonra grid/tarih çipi sıfırlanmış olabilir).
        boolean found = getDtsListPage().findDraftDtsByReferenceNo(dtsReferenceNo);
        Assert.assertTrue(found, dtsReferenceNo + " kaydı 'Gözat' öncesi kolon filtresiyle bulunamadı.");
        boolean clicked = getDtsListPage().clickOnlySingleRowActionButton();
        Assert.assertTrue(clicked, "Filtrelenmiş satırdaki 'Gözat' butonu bulunamadı/tıklanamadı");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("\"Doğrudan Tahsilat Bilgileri\" diyaloğu açılmalı")
    public void dogrudanTahsilatBilgileriDiyaloguAcilmali() {
        CompanyDtsAllocationDialogPage dialog = getDtsAllocationDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(),
                "'Gözat' tıklamasından sonra CompanyDtsAllocationDialog ('Doğrudan Tahsilat Bilgileri') açılmadı");
        log.info("[DTS] CompanyDtsAllocationDialog başarıyla doğrulandı.");
    }

    @When("tedarikçi \"Onayla\" butonuna tıklayıp DTS kaydını onaylar")
    public void tedarikciOnaylaButonunaTiklayipDtsKaydiniOnaylar() {
        boolean approved = getDtsAllocationDialogPage().clickApprove();
        Assert.assertTrue(approved, "'Onayla' butonu bulunamadı/tıklanamadı veya onay dialogu kapanmadı");
    }

    @Then("üretilen DTS kaydının statüsü Gönderiliyor'a geçmeli")
    public void uretilenDtsKaydininStatusuGonderiliyoraGecmeli() {
        boolean advanced = getDtsListPage().waitForStatusFold("gonderiliyor", 30);
        Assert.assertTrue(advanced,
                "Company 'Onayla' onayından sonra " + dtsReferenceNo + " satırı 'Gönderiliyor' statüsüne geçmedi.");
        log.info("[DTS] {} statüsü Gönderiliyor'a geçti doğrulandı (Company onayı).", dtsReferenceNo);
    }

    // ---- Admin nihai onayı: Raporlar > TAHSİLAT İŞLEMLERİ > Gözat -> AdminDtsDialog -> Onayla -> COMPLETED ----
    // NOT: "admin olarak giriş yapılır" adımı LoginRoleStepDefs.adminOlarakGiris()'te zaten
    // tanımlı (RoleSessionManager.loginAsDefault(driver, Role.ADMIN)) — burada TEKRAR
    // tanımlanmaz (Cucumber DuplicateStepDefinitionException verir, Given/When farklı olsa da
    // regex eşleşmesi aynı sayılır). Feature dosyasındaki "When admin olarak giriş yapılır" bu
    // mevcut step'i kullanır.

    @And("admin Raporlar \"TAHSİLAT İŞLEMLERİ\" sekmesine gider")
    public void adminRaporlarTahsilatIslemleriSekmesineGider() {
        boolean navigated = getAdminDtsReportPage().navigateToTahsilatIslemleri();
        Assert.assertTrue(navigated, "Admin 'Raporlar' > 'TAHSİLAT İŞLEMLERİ' sekmesine gidilemedi");
    }

    @And("admin filtrelenmiş DTS kaydı için \"Gözat\" butonuna tıklar")
    public void adminFiltrelenmisDtsKaydiIcinGozatButonunaTiklar() {
        Assert.assertNotNull(dtsReferenceNo, "Bu adımdan önce DTS kaydı üretilmiş olmalı");
        boolean found = getAdminDtsReportPage().findDtsByReferenceNo(dtsReferenceNo);
        Assert.assertTrue(found, dtsReferenceNo + " kaydı Admin 'TAHSİLAT İŞLEMLERİ' gridinde kolon filtresiyle bulunamadı.");
        boolean clicked = getAdminDtsReportPage().clickOnlySingleRowActionButton();
        Assert.assertTrue(clicked, "Admin gridindeki 'Gözat' butonu bulunamadı/tıklanamadı");
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("admin \"Doğrudan Tahsilat Bilgileri\" diyaloğunu görmeli")
    public void adminDogrudanTahsilatBilgileriDiyaloguGormeli() {
        AdminDtsDialogPage dialog = getAdminDtsDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(),
                "Admin 'Gözat' tıklamasından sonra AdminDtsDialog ('Doğrudan Tahsilat Bilgileri') açılmadı");
        log.info("[DTS] AdminDtsDialog başarıyla doğrulandı.");
    }

    @When("admin \"Onayla\" butonuna tıklayıp DTS kaydını nihai onaylar")
    public void adminOnaylaButonunaTiklayipDtsKaydiniNihaiOnaylar() {
        boolean approved = getAdminDtsDialogPage().clickApprove();
        Assert.assertTrue(approved, "Admin 'Onayla' butonu bulunamadı/tıklanamadı veya onay dialogu kapanmadı");
    }

    @Then("üretilen DTS kaydının statüsü Tamamlandı'ya geçmeli")
    public void uretilenDtsKaydininStatusuTamamlandiyaGecmeli() {
        boolean advanced = getAdminDtsReportPage().waitForStatusFold("tamamlandi", 30);
        Assert.assertTrue(advanced,
                "Admin 'Onayla' nihai onayından sonra " + dtsReferenceNo + " satırı 'Tamamlandı' statüsüne geçmedi.");
        log.info("[DTS] {} statüsü Tamamlandı'ya geçti doğrulandı (Admin nihai onayı).", dtsReferenceNo);
    }

    // ---- DB çapraz doğrulama: dbsdocs kaydı doğru finansal kuruma doğru tutarla oluşmuş mu ----
    // NOT: dbsdocs kaydı DTS COMPLETED olduğunda status='PENDING' ile oluşur (BU BEKLENEN DURUMDUR,
    // hata değil). "Gönderildi/Aktif" statüsüne geçiş + buna bağlı dealerlimit güncellemesi AYRI bir
    // sonraki banka-onay entegrasyon adımıdır (dbs-status-update servisi) — bu senaryonun kapsamı
    // DIŞINDA. dealerlimit'in değişip değişmediği KASITLI OLARAK assert edilmiyor: bu güncelleme
    // timing'e bağlı, ayrı bir arka plan/entegrasyon sürecinde gerçekleşiyor ve senaryonun bu adımı
    // koştuğu anda henüz gerçekleşmemiş olabilir — assert edilirse yanlış-negatif üretir.
    @Then("üretilen faturanın DBS belgesi doğru finansal kuruma doğru tutarla oluşmalı")
    public void uretilenFaturaninDbsBelgesiDogruFinansalKurumaDogruTutarlaOlusmali() {
        Assert.assertNotNull(generatedInvoiceNo,
                "Bu adımdan önce 'API ile taze bir DTS taslak kaydı üretilir' çalışmış olmalı (generatedInvoiceNo boş)");

        // dbsdocs kaydı, DTS COMPLETED statüsüne bağlı arka plan işlemiyle oluşuyor — Admin onayı
        // sonrası küçük bir gecikme olabileceğinden kısa aralıklı poll ile bekleniyor (statik tek
        // sorgu yerine; kodun diğer yerlerinde de CALCULATING->PENDING geçişi için aynı desen var).
        Optional<DtsDbAssertions.DbsDocRow> rowOpt = Optional.empty();
        final int maxAttempts = 10;
        for (int attempt = 1; attempt <= maxAttempts && rowOpt.isEmpty(); attempt++) {
            rowOpt = DtsDbAssertions.findDbsDocByDocNo(generatedInvoiceNo);
            if (rowOpt.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        Assert.assertTrue(rowOpt.isPresent(),
                "dbsdocs tablosunda docno=" + generatedInvoiceNo + " için kayıt bulunamadı "
                        + "(DTS " + dtsReferenceNo + " Tamamlandı statüsüne geçti ama DBS belgesi DB'de yok).");
        DtsDbAssertions.DbsDocRow row = rowOpt.get();
        log.info("[DTS-DB] Çapraz doğrulama satırı: {}", row);

        Assert.assertEquals(row.factoringId, Long.valueOf(BEKLENEN_FACTORING_ID),
                "dbsdocs.factoringid beklenen finansal kurumla (Akbank T.A.Ş., id=" + BEKLENEN_FACTORING_ID
                        + ") eşleşmiyor — Otomasyon Bayi'nin (dealer.id=38) tek dealerlimit kaydı sadece "
                        + "Akbank'a bağlı olduğu için algoritmanın başka bir kurum seçmesi beklenmiyordu. Satır: " + row);

        BigDecimal expectedAmount = BigDecimal.valueOf(generatedInvoiceAmount);
        Assert.assertEquals(row.dbsAmount.compareTo(expectedAmount), 0,
                "dbsdocs.dbsamount (" + row.dbsAmount + ") yüklenen fatura tutarıyla (" + expectedAmount
                        + ") eşleşmiyor. Satır: " + row);

        // Status için katı 'PENDING' eşitliği YERİNE "boş/null değil" kontrolü seçildi: DTS COMPLETED
        // olduğunda dbsdocs PENDING ile oluşuyor (bugünkü kanıtlı davranış) ama sonraki banka-onay
        // entegrasyon adımı bu değeri senaryo bitmeden değiştirebilir (timing'e bağlı) — katı eşitlik
        // bu durumda yanlış-negatif üretir. Boş/null olmaması "belge gerçekten oluştu" sinyali yeterlidir.
        Assert.assertNotNull(row.status, "dbsdocs.status null olmamalı. Satır: " + row);
        Assert.assertFalse(row.status.trim().isEmpty(), "dbsdocs.status boş olmamalı. Satır: " + row);

        log.info("[DTS-DB] {} için dbsdocs çapraz doğrulaması PASSED: factoringid={}, dbsamount={}, status={}",
                generatedInvoiceNo, row.factoringId, row.dbsAmount, row.status);
    }
}
