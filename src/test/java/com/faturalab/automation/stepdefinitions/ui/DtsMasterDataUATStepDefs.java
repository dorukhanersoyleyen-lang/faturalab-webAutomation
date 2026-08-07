package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.db.DtsDbAssertions;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.CompanyAddEditDealerDialogPage;
import com.faturalab.automation.pages.CompanyAddEditFactoringDialogPage;
import com.faturalab.automation.pages.CompanyDisplayDealerLimitsPage;
import com.faturalab.automation.pages.CompanyDisplayDealerPage;
import com.faturalab.automation.pages.CompanyDisplayFactoringsPage;
import com.faturalab.automation.pages.CompanyEditDealerLimitDialogPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * DTS Master Data — "Finansal Kurumlar" (companyfactoring) CRUD UAT senaryosu (@dts-fk-crud).
 *
 * ⚠️ Bu sınıf {@link DtsIslemUATStepDefs}'ten (DTS-001, işlem akışı) TAMAMEN AYRIDIR — ona
 * DOKUNULMADI. "dts tedarikcisi olarak giriş yapılır" adımı {@link DtsIslemUATStepDefs} içinde
 * zaten tanımlı olduğu için (Cucumber step tanımları classpath genelinde tekildir) burada
 * TEKRAR tanımlanmıyor — DtsMasterDataUAT.feature bu mevcut step'i doğrudan kullanır.
 *
 * Test Otomasyon Sadece Tedarikçi (company.id=998) — dts.supplier.impersonate.identifier.
 * Hedef finansal kurum: "Türkiye İş Bankası A.Ş." (factoring.id=68) — company.id=998'in mevcut
 * 4 FK ilişkisiyle (Akbank/57, MNO/53, Garanti/58, Denizbank/62) çakışmayan, henüz eklenmemiş
 * bir kurum (bkz. feature dosyası başlığı).
 *
 * ⚠️ Kaynak kod kanıtı (origin/vaadin-24, CompanyAddEditFactoringDialog.buildFormLayout(), READ-ONLY
 * incelendi): CREATE modunda (companyFactoring==null) forma SADECE "Para Birimi" ve "Finansal Kurum"
 * eklenir; "Çalışma Modeli"/"DBS Maliyet (%)"/"First Loss (%)"/"Fiyat (%)" SADECE editMode==true
 * iken forma eklenir. Dialogda HİÇBİR ZAMAN genel bir "Limit" (tutar) alanı yoktur (sadece "Limit
 * Üstü Gönderim" checkbox'ı vardır). Bu yüzden CREATE adımı sadece FK seçimiyle sınırlı; EDIT'te
 * gerçekten var olan "DBS Maliyet (%)" alanı değiştirilip doğrulanır.
 *
 * ⚠️ İdempotentlik notu: bu senaryo TEKRAR koşulursa "Türkiye İş Bankası A.Ş." artık company'nin
 * mevcut FK listesinde olacağı için CompanyAddEditFactoringDialog'un factoringComboBox'ında
 * (getNewFactorings zaten-bağlı kurumları eler) görünmeyecek ve CREATE adımı başarısız olur — bu
 * TZF/DFP'deki "her koşumda benzersiz veri" kuralından FARKLI, KASITLI bir istisnadır (tek bir
 * sabit FK ilişkisi test edildiği için); ilk koşumda sorun yoktur.
 */
public class DtsMasterDataUATStepDefs {

    private static final Logger log = LogManager.getLogger(DtsMasterDataUATStepDefs.class);

    private static final String TARGET_FACTORING = "Türkiye İş Bankası A.Ş.";
    // Yazarken Türkçe (virgül-ondalık) format kullanılır — AmountField (tr.com.globit.vaadin.
    // components.AmountField) girişte "," karakterini "." ile normalize edip BigDecimal'a çeviriyor.
    // ⚠️ Grid'de gösterim FARKLI: CompanyDisplayFactoringsView satırı `getDbsCost().toString() + '%'`
    // ile üretiyor — BigDecimal.toString() locale-bağımsızdır ve HER ZAMAN nokta kullanır (kanıt:
    // 2026-08-07 canlı koşum — "12,34" yazılıp kaydedildi, grid'de "12.34%" göründü). Bu yüzden grid
    // doğrulaması NOKTALI biçimle yapılır; kaydetme adımı VİRGÜLLÜ biçimle yapılır — ikisi kasıtlı
    // olarak farklıdır, hata değildir.
    private static final String NEW_DBS_COST = "12,34";
    private static final String NEW_DBS_COST_GRID_DISPLAY = "12.34";

    // ---- @dts-bayi-crud sabitleri ----
    // TZF/DFP kuralıyla UYUMLU: her koşumda BENZERSİZ (timestamp'li) veri — @dts-fk-crud'daki sabit
    // hedef + idempotentlik-guard istisnasının AKSİNE, burada "Otomasyon Bayi" (dealerlimit.id=88,
    // @dts-limit-crud'un hedefi) kaydına HİÇ dokunulmuyor, her koşum kendi TAM YENİ bayisini açıyor.
    private final String uniqueSuffix = String.valueOf(System.currentTimeMillis() % 1_000_000L);
    private final String newDealerName = "Otomasyon CRUD " + uniqueSuffix;
    private final String newDealerCode = "OTMCRUD" + uniqueSuffix;
    private final String newDealerTaxNumber = "9" + uniqueSuffix + "000";
    private static final String INITIAL_GUARANTEE_LIMIT = "500";
    // ⚠️ 2026-08-07 canlı koşumla DÜZELTİLDİ: dev.faturalab.com'da Teminat Limiti grid gösterimi de
    // (DBS Maliyet'teki gibi) NOKTA-ondalık çıkıyor ("500.00 TL") — `AppSettingsManager.getLocale()`
    // Türkçe değil, dev ortamında dot-decimal bir locale'e çözülüyor. Yazma yine virgüllü (AmountField
    // "," → "." normalize ediyor); grid doğrulaması NOKTALI biçimle yapılır — DBS Maliyet'teki
    // yazma≠gösterim ayrımıyla AYNI kalıp.
    private static final String NEW_GUARANTEE_LIMIT = "750,25";
    private static final String NEW_GUARANTEE_LIMIT_GRID_DISPLAY = "750.25";

    // ---- @dts-limit-crud sabitleri ----
    // ⚠️ SHARED fixture (dealerlimit.id=88, "Otomasyon Bayi" ↔ Akbank) — DTS-001/DTS_API-001 de
    // aynı kurulumu kullanıyor. Bu yüzden TZF/DFP'nin "her koşumda benzersiz veri" kuralı YERİNE
    // "değiştir → doğrula → orijinaline geri döndür → doğrula" kalıbı uygulanıyor (bkz. feature
    // dosyası başlığı). Grid metin taraması YERİNE DB doğrulaması (DtsDbAssertions) kullanılıyor —
    // büyük tutarlarda binlik-ayraç format belirsizliği metin aramasını kırılgan kılardı.
    private static final long TARGET_DEALER_LIMIT_ID = 88L;
    private static final String TARGET_DEALER_NAME = "Otomasyon Bayi";
    private static final String TEMP_DBS_LIMIT = "205555,00";
    private BigDecimal originalDbsLimitAmount;

    private CompanyDisplayFactoringsPage factoringsPage;
    private CompanyAddEditFactoringDialogPage factoringDialogPage;
    private CompanyDisplayDealerPage dealerPage;
    private CompanyAddEditDealerDialogPage dealerDialogPage;
    private CompanyDisplayDealerLimitsPage dealerLimitsPage;
    private CompanyEditDealerLimitDialogPage dealerLimitDialogPage;
    // ⚠️ İdempotentlik: bu senaryo TEKRAR koşulursa TARGET_FACTORING artık company'nin FK
    // listesinde olacağı için CREATE combobox'ında görünmez (getNewFactorings zaten-bağlı
    // kurumları eler) — bu senaryo dosyasının başındaki yorumda ZATEN belgelenmiş, KASITLI bir
    // durum. İlk koşumda create gerçekleşir; sonraki koşumlarda create adımları no-op geçilir,
    // senaryo doğrudan (zaten var olan kayıt üzerinde) EDIT akışına devam eder — böylece senaryo
    // sonsuza kadar tekrar koşulabilir kalır.
    private boolean targetFactoringAlreadyExisted;

    /** TR karakter + büyük/küçük harf duyarsız karşılaştırma (VaadinGridFilterHelper'daki
     *  TR_FOLD_JS'in Java karşılığı — kural #8, web-automation.md). */
    private static String foldTr(String s) {
        if (s == null) return "";
        return s.replace('İ', 'i').replace('I', 'ı').replace('ı', 'i')
                .replace('Ş', 's').replace('ş', 's')
                .replace('Ğ', 'g').replace('ğ', 'g')
                .replace('Ü', 'u').replace('ü', 'u')
                .replace('Ö', 'o').replace('ö', 'o')
                .replace('Ç', 'c').replace('ç', 'c')
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("\\s+", " ").trim();
    }

    private CompanyDisplayFactoringsPage getFactoringsPage() {
        if (factoringsPage == null) {
            factoringsPage = new CompanyDisplayFactoringsPage(DriverManager.getDriver());
        }
        return factoringsPage;
    }

    private CompanyAddEditFactoringDialogPage getFactoringDialogPage() {
        if (factoringDialogPage == null) {
            factoringDialogPage = new CompanyAddEditFactoringDialogPage(DriverManager.getDriver());
        }
        return factoringDialogPage;
    }

    private CompanyDisplayDealerPage getDealerPage() {
        if (dealerPage == null) {
            dealerPage = new CompanyDisplayDealerPage(DriverManager.getDriver());
        }
        return dealerPage;
    }

    private CompanyAddEditDealerDialogPage getDealerDialogPage() {
        if (dealerDialogPage == null) {
            dealerDialogPage = new CompanyAddEditDealerDialogPage(DriverManager.getDriver());
        }
        return dealerDialogPage;
    }

    private CompanyDisplayDealerLimitsPage getDealerLimitsPage() {
        if (dealerLimitsPage == null) {
            dealerLimitsPage = new CompanyDisplayDealerLimitsPage(DriverManager.getDriver());
        }
        return dealerLimitsPage;
    }

    private CompanyEditDealerLimitDialogPage getDealerLimitDialogPage() {
        if (dealerLimitDialogPage == null) {
            dealerLimitDialogPage = new CompanyEditDealerLimitDialogPage(DriverManager.getDriver());
        }
        return dealerLimitDialogPage;
    }

    // ---- Navigasyon ----

    @When("tedarikçi \"Finansal Kurumlar\" menüsüne gider")
    public void tedarikciFinansalKurumlarMenusuneGider() {
        Assert.assertTrue(getFactoringsPage().navigateToFactorings(),
                "'Finansal Kurumlar' menüsü bulunamadı/tıklanamadı");
        Assert.assertTrue(getFactoringsPage().isGridVisible(), "CompanyDisplayFactoringsView grid görünür değil");
        targetFactoringAlreadyExisted = getFactoringsPage().findFactoringRowByName(TARGET_FACTORING);
        if (targetFactoringAlreadyExisted) {
            log.info("[DTS-MD] '{}' zaten company'nin FK listesinde (önceki koşumdan kalma) — "
                    + "CREATE adımları no-op geçilecek, senaryo doğrudan EDIT ile devam edecek.",
                    TARGET_FACTORING);
        }
    }

    // ---- Create ----

    @And("\"Anlaşmalı Finansal Kurum Ekle\" butonuna tıklanır")
    public void anlasmaliFinansalKurumEkleButonunaTiklanir() {
        if (targetFactoringAlreadyExisted) {
            log.info("[DTS-MD] CREATE no-op ('{}' zaten var).", TARGET_FACTORING);
            return;
        }
        boolean clicked = getFactoringsPage().clickAddFactoringButton();
        Assert.assertTrue(clicked, "'Anlaşmalı Finansal Kurum Ekle' butonu bulunamadı/tıklanamadı");
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("\"Anlaşmalı Finansal Kurum\" ekleme diyaloğu açılmalı")
    public void anlasmaliFinansalKurumEklemeDiyaloguAcilmali() {
        if (targetFactoringAlreadyExisted) {
            log.info("[DTS-MD] CREATE dialog kontrolü no-op ('{}' zaten var).", TARGET_FACTORING);
            return;
        }
        CompanyAddEditFactoringDialogPage dialog = getFactoringDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(),
                "'Anlaşmalı Finansal Kurum Ekle' tıklamasından sonra CompanyAddEditFactoringDialog açılmadı");
        Assert.assertFalse(dialog.isEditModeOpened(),
                "CREATE modunda 'Çalışma Modeli' alanı görünüyor — kaynak kod kanıtına göre CREATE'te bu alan görünmemeli");
        log.info("[DTS-MD] CompanyAddEditFactoringDialog (CREATE modu) doğrulandı.");
    }

    @When("\"Türkiye İş Bankası A.Ş.\" finansal kurumu seçilip kaydedilir")
    public void turkiyeIsBankasiFinansalKurumuSecilipKaydedilir() {
        if (targetFactoringAlreadyExisted) {
            log.info("[DTS-MD] CREATE seçim/kaydetme no-op ('{}' zaten var).", TARGET_FACTORING);
            return;
        }
        CompanyAddEditFactoringDialogPage dialog = getFactoringDialogPage();
        boolean selected = dialog.selectFactoring("iş bankası");
        Assert.assertTrue(selected, "'" + TARGET_FACTORING + "' finansal kurum listesinde bulunamadı/seçilemedi "
                + "(zaten eklenmiş olabilir — ilk koşumda beklenmez). Dialog metni: " + dialog.dumpVisibleText());
        boolean saved = dialog.clickSaveAndConfirm(false);
        Assert.assertTrue(saved, "CREATE 'Kaydet' butonu tıklanamadı veya onay dialogu kapanmadı");
        log.info("[DTS-MD] '{}' finansal kurumu seçilip kaydedildi.", TARGET_FACTORING);
    }

    @Then("Finansal Kurumlar listesinde \"Türkiye İş Bankası A.Ş.\" satırı görünmeli")
    public void finansalKurumlarListesindeTurkiyeIsBankasiSatiriGorunmeli() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean found = getFactoringsPage().findFactoringRowByName(TARGET_FACTORING);
        Assert.assertTrue(found, "'" + TARGET_FACTORING + "' kaydı 'Finansal Kurum' kolon filtresiyle bulunamadı.");
        List<String> rows = getFactoringsPage().dumpVisibleRowsGrouped();
        // ⚠️ Grid satırları TÜM BÜYÜK HARF render ediliyor ("TÜRKİYE İŞ BANKASI A.Ş."), TARGET_FACTORING
        // ise normal harfli — düz .contains() Türkçe "İ/I" kuralları yüzünden de kırılabileceğinden
        // (web-automation.md kural #8, TR_FOLD) fold'lu karşılaştırma kullanılır.
        boolean rowMatches = rows.stream().anyMatch(r -> foldTr(r).contains(foldTr(TARGET_FACTORING)));
        Assert.assertTrue(rowMatches, "'" + TARGET_FACTORING + "' filtrelendi ama satırda görünmüyor: " + rows);
        log.info("[DTS-MD] '{}' satırı listede doğrulandı (CREATE).", TARGET_FACTORING);
    }

    // ---- Edit ----

    @When("\"Türkiye İş Bankası A.Ş.\" satırı için \"Düzenle\" butonuna tıklanır")
    public void turkiyeIsBankasiSatiriIcinDuzenleButonunaTiklanir() {
        // Önceki adımda filtre zaten "Türkiye İş Bankası A.Ş." tek satırına indirildi.
        boolean clicked = getFactoringsPage().clickOnlySingleRowEditButton();
        Assert.assertTrue(clicked, "Filtrelenmiş satırdaki 'Düzenle' butonu bulunamadı/tıklanamadı");
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("\"Anlaşmalı Finansal Kurum\" düzenleme diyaloğu açılmalı")
    public void anlasmaliFinansalKurumDuzenlemeDiyaloguAcilmali() {
        CompanyAddEditFactoringDialogPage dialog = getFactoringDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(),
                "'Düzenle' tıklamasından sonra CompanyAddEditFactoringDialog açılmadı");
        Assert.assertTrue(dialog.isEditModeOpened(),
                "EDIT modunda olması gereken 'Çalışma Modeli' alanı görünmüyor — kaynak kod kanıtına göre "
                        + "bu alan sadece editMode=true iken forma eklenir. Dialog metni: " + dialog.dumpVisibleText());
        log.info("[DTS-MD] CompanyAddEditFactoringDialog (EDIT modu) doğrulandı.");
    }

    @When("\"DBS Maliyet \\(%\\)\" alanı yeni bir değere güncellenip kaydedilir")
    public void dbsMaliyetAlaniYeniBirDegereGuncellenipKaydedilir() {
        CompanyAddEditFactoringDialogPage dialog = getFactoringDialogPage();
        boolean typed = dialog.setDbsCost(NEW_DBS_COST);
        Assert.assertTrue(typed, "'DBS Maliyet (%)' alanı bulunamadı/doldurulamadı");
        boolean saved = dialog.clickSaveAndConfirm(true);
        Assert.assertTrue(saved, "EDIT 'Güncelle' butonu tıklanamadı veya onay dialogu kapanmadı");
        log.info("[DTS-MD] 'DBS Maliyet (%)' alanı '{}' olarak güncellendi.", NEW_DBS_COST);
    }

    @Then("Finansal Kurumlar listesinde \"Türkiye İş Bankası A.Ş.\" satırındaki DBS Maliyet güncellenmiş olmalı")
    public void finansalKurumlarListesindeDbsMaliyetGuncellenmisOlmali() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean found = getFactoringsPage().findFactoringRowByName(TARGET_FACTORING);
        Assert.assertTrue(found, "'" + TARGET_FACTORING + "' kaydı EDIT sonrası kolon filtresiyle bulunamadı.");
        // ⚠️ Grid zaten "Finansal Kurum" kolon filtresiyle TEK satıra (Türkiye İş Bankası) indirgendi
        // (findFactoringRowByName). Satır-satır y-koordinat gruplaması (waitForRowContainingFold)
        // canlı koşumda kırılgan çıktı (Güncelle sonrası grid yeniden render olurken bucket'lar geçici
        // tutarsızlaşıyor) — filtre zaten tekilleştirdiği için satır gruplamaya gerek yok, doğrudan
        // görünür hücrelerde değer arıyoruz (bkz. waitForAnyVisibleCellFold Javadoc'u).
        boolean updated = getFactoringsPage().waitForAnyVisibleCellFold(NEW_DBS_COST_GRID_DISPLAY, 40);
        List<String> rows = getFactoringsPage().dumpVisibleRowsGrouped();
        Assert.assertTrue(updated, "'" + TARGET_FACTORING + "' satırında güncellenen DBS Maliyet değeri ('"
                + NEW_DBS_COST_GRID_DISPLAY + "') görünmüyor. Güncel satırlar: " + rows);
        log.info("[DTS-MD] '{}' satırında DBS Maliyet güncellemesi ('{}') doğrulandı.", TARGET_FACTORING, NEW_DBS_COST_GRID_DISPLAY);
    }

    // ==================== @dts-bayi-crud ====================

    @When("tedarikçi \"Bayiler\" menüsüne gider")
    public void tedarikciBayilerMenusuneGider() {
        Assert.assertTrue(getDealerPage().navigateToDealers(), "'Bayiler' menüsü bulunamadı/tıklanamadı");
        Assert.assertTrue(getDealerPage().isGridVisible(), "CompanyDisplayDealerView grid görünür değil");
    }

    @And("\"Bayi Ekle\" butonuna tıklanır")
    public void bayiEkleButonunaTiklanir() {
        boolean clicked = getDealerPage().clickAddDealerButton();
        Assert.assertTrue(clicked, "'Bayi Ekle' butonu bulunamadı/tıklanamadı");
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("\"Bayi Ekle\" ekleme diyaloğu açılmalı")
    public void bayiEkleEklemeDiyaloguAcilmali() {
        CompanyAddEditDealerDialogPage dialog = getDealerDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(), "'Bayi Ekle' tıklamasından sonra CompanyAddEditDealerDialog açılmadı");
        Assert.assertFalse(dialog.isEditModeOpened(),
                "CREATE modunda 'Bayi Ünvanı' alanı DISABLED görünüyor — kaynak kod kanıtına göre CREATE'te enabled olmalı");
        log.info("[DTS-MD] CompanyAddEditDealerDialog (CREATE modu) doğrulandı. Yeni bayi: ad='{}' kod='{}' vkn='{}'",
                newDealerName, newDealerCode, newDealerTaxNumber);
    }

    @When("benzersiz bayi bilgileri girilip kaydedilir")
    public void benzersizBayiBilgileriGirilipKaydedilir() {
        CompanyAddEditDealerDialogPage dialog = getDealerDialogPage();
        Assert.assertTrue(dialog.setDealerName(newDealerName), "'Bayi Ünvanı' alanı doldurulamadı");
        Assert.assertTrue(dialog.setDealerCode(newDealerCode), "'Bayi Kodu' alanı doldurulamadı");
        Assert.assertTrue(dialog.setTaxNumber(newDealerTaxNumber), "'Vergi Kimlik No' alanı doldurulamadı");
        Assert.assertTrue(dialog.setGuaranteeLimit(INITIAL_GUARANTEE_LIMIT), "'Teminat Limiti' alanı doldurulamadı");
        Assert.assertTrue(dialog.selectCurrency("TL"), "'Para Birimi' alanında 'TL' seçilemedi");
        Assert.assertTrue(dialog.selectStatusActive(), "'Durumu' → 'Aktif' seçilemedi");
        boolean saved = dialog.clickSaveAndConfirm(false);
        Assert.assertTrue(saved, "CREATE 'Kaydet' butonu tıklanamadı veya onay dialogu kapanmadı");
        log.info("[DTS-MD] Yeni bayi '{}' (kod: {}) bilgileri girilip kaydedildi.", newDealerName, newDealerCode);
    }

    @Then("Bayiler listesinde yeni bayi satırı görünmeli")
    public void bayilerListesindeYeniBayiSatiriGorunmeli() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean found = getDealerPage().findDealerRowByCode(newDealerCode);
        Assert.assertTrue(found, "'" + newDealerCode + "' kaydı 'Bayi Kodu' kolon filtresiyle bulunamadı.");
        List<String> rows = getDealerPage().dumpVisibleRowsGrouped();
        boolean rowMatches = rows.stream().anyMatch(r -> foldTr(r).contains(foldTr(newDealerCode)));
        Assert.assertTrue(rowMatches, "'" + newDealerCode + "' filtrelendi ama satırda görünmüyor: " + rows);
        log.info("[DTS-MD] Yeni bayi satırı ('{}') listede doğrulandı (CREATE).", newDealerCode);
    }

    @When("yeni bayi satırı için \"Düzenle\" butonuna tıklanır")
    public void yeniBayiSatiriIcinDuzenleButonunaTiklanir() {
        // Önceki adımda filtre zaten newDealerCode tek satırına indirildi.
        boolean clicked = getDealerPage().clickOnlySingleRowEditButton();
        Assert.assertTrue(clicked, "Filtrelenmiş bayi satırındaki 'Düzenle' butonu bulunamadı/tıklanamadı");
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("bayi düzenleme diyaloğu açılmalı")
    public void bayiDuzenlemeDiyaloguAcilmali() {
        CompanyAddEditDealerDialogPage dialog = getDealerDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(), "'Düzenle' tıklamasından sonra CompanyAddEditDealerDialog açılmadı");
        Assert.assertTrue(dialog.isEditModeOpened(),
                "EDIT modunda 'Bayi Ünvanı' alanı DISABLED olmalı — kaynak kod kanıtına göre editMode'da "
                        + "bu alan devre dışı bırakılır. Dialog metni: " + dialog.dumpVisibleText());
        log.info("[DTS-MD] CompanyAddEditDealerDialog (EDIT modu) doğrulandı.");
    }

    @When("\"Teminat Limiti\" alanı yeni bir değere güncellenip kaydedilir")
    public void teminatLimitiAlaniYeniBirDegereGuncellenipKaydedilir() {
        CompanyAddEditDealerDialogPage dialog = getDealerDialogPage();
        boolean typed = dialog.setGuaranteeLimit(NEW_GUARANTEE_LIMIT);
        Assert.assertTrue(typed, "'Teminat Limiti' alanı bulunamadı/doldurulamadı");
        boolean saved = dialog.clickSaveAndConfirm(true);
        Assert.assertTrue(saved, "EDIT 'Güncelle' butonu tıklanamadı veya onay dialogu kapanmadı");
        log.info("[DTS-MD] 'Teminat Limiti' alanı '{}' olarak güncellendi.", NEW_GUARANTEE_LIMIT);
    }

    @Then("Bayiler listesinde güncellenen Teminat Limiti değeri görünmeli")
    public void bayilerListesindeGuncellenenTeminatLimitiDegeriGorunmeli() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        boolean found = getDealerPage().findDealerRowByCode(newDealerCode);
        Assert.assertTrue(found, "'" + newDealerCode + "' kaydı EDIT sonrası kolon filtresiyle bulunamadı.");
        // ⚠️ @dts-fk-crud'daki KANITLANMIŞ kök nedenle aynı (bkz. finansalKurumlarListesindeDbsMaliyetGuncellenmisOlmali):
        // satır-satır y-koordinat gruplaması "Güncelle" sonrası grid yeniden render olurken kırılgan —
        // filtre zaten tek satıra indirdiği için satır gruplamaya gerek yok.
        boolean updated = getDealerPage().waitForAnyVisibleCellFold(NEW_GUARANTEE_LIMIT_GRID_DISPLAY, 40);
        List<String> rows = getDealerPage().dumpVisibleRowsGrouped();
        Assert.assertTrue(updated, "'" + newDealerCode + "' satırında güncellenen Teminat Limiti değeri ('"
                + NEW_GUARANTEE_LIMIT_GRID_DISPLAY + "') görünmüyor. Güncel satırlar: " + rows);
        log.info("[DTS-MD] '{}' satırında Teminat Limiti güncellemesi ('{}') doğrulandı.", newDealerCode, NEW_GUARANTEE_LIMIT_GRID_DISPLAY);
    }

    // ==================== @dts-limit-crud ====================

    @When("tedarikçi \"Bayi Limitleri\" menüsüne gider")
    public void tedarikciBayiLimitleriMenusuneGider() {
        Assert.assertTrue(getDealerLimitsPage().navigateToDealerLimits(), "'Bayi Limitleri' menüsü bulunamadı/tıklanamadı");
        Assert.assertTrue(getDealerLimitsPage().isGridVisible(), "CompanyDisplayDealerLimitsView grid görünür değil");
        // Restore adımının doğru orijinal değere dönebilmesi için mevcut DB değeri BAŞTA okunuyor
        // (UI'dan değil — bağımsız, kesin bir ground truth).
        Optional<BigDecimal> current = DtsDbAssertions.findDealerLimitDbsAmount(TARGET_DEALER_LIMIT_ID);
        Assert.assertTrue(current.isPresent(), "dealerlimit.id=" + TARGET_DEALER_LIMIT_ID + " DB'de bulunamadı");
        originalDbsLimitAmount = current.get();
        log.info("[DTS-MD] dealerlimit.id={} orijinal DBS Limit Tutarı (DB'den okundu): {}",
                TARGET_DEALER_LIMIT_ID, originalDbsLimitAmount);
    }

    @And("\"Otomasyon Bayi\" satırı için \"Düzenle\" butonuna tıklanır")
    public void otomasyonBayiSatiriIcinDuzenleButonunaTiklanir() {
        // ⚠️ KÖK NEDEN (2026-08-07, kaynak kod + canlı koşumla kanıtlandı): CompanyDisplayDealerLimitsView'da
        // "Bayi Ünvanı" (ve "Finansal Kurum") kolonlarında .setKey(...) YOK (addColumn(lambda).setHeader(...)
        // — key hiç set edilmiyor). TableFilterManager bu kolonlar için filtre İKONUNU gösterip dialogu
        // açabiliyor ("Tümünü seç" bile bulunuyor) ama değer listesi index hizalaması bozuk çıkıyor —
        // "Otomasyon Bayi" gridde AÇIKÇA görünürken (canlı dump'ta doğrulandı) filtre değeri bulamıyor.
        // Bu genel bir test-kodu hatası DEĞİL, ekranın kaynak kodundaki gerçek bir sınırlama. company.id=998
        // için TEK bir dealerlimit kaydı olduğu DB'den kanıtlı (SELECT ... WHERE d.companyid=998 → 1 satır) —
        // filtreye hiç gerek yok, gridde zaten TEK satır var, doğrudan o satırın "Düzenle" butonuna basılır.
        List<String> rows = getDealerLimitsPage().dumpVisibleRowsGrouped();
        boolean targetVisible = rows.stream().anyMatch(r -> foldTr(r).contains(foldTr(TARGET_DEALER_NAME)));
        Assert.assertTrue(targetVisible, "'" + TARGET_DEALER_NAME + "' satırı gridde hiç görünmüyor: " + rows);
        boolean clicked = getDealerLimitsPage().clickOnlySingleRowEditButton();
        Assert.assertTrue(clicked, "Filtrelenmiş bayi limiti satırındaki 'Düzenle' butonu bulunamadı/tıklanamadı");
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("bayi limiti düzenleme diyaloğu açılmalı")
    public void bayiLimitiDuzenlemeDiyaloguAcilmali() {
        CompanyEditDealerLimitDialogPage dialog = getDealerLimitDialogPage();
        dialog.dumpVisibleText();
        Assert.assertTrue(dialog.isOpened(), "'Düzenle' tıklamasından sonra CompanyEditDealerLimitDialog açılmadı");
        log.info("[DTS-MD] CompanyEditDealerLimitDialog doğrulandı.");
    }

    @When("\"DBS Limit Tutarı\" alanı geçici bir test değerine güncellenip kaydedilir")
    public void dbsLimitTutariAlaniGeciciBirTestDegerineGuncellenipKaydedilir() {
        CompanyEditDealerLimitDialogPage dialog = getDealerLimitDialogPage();
        boolean typed = dialog.setDbsLimitAmount(TEMP_DBS_LIMIT);
        Assert.assertTrue(typed, "'DBS Limit Tutarı' alanı bulunamadı/doldurulamadı (geçici değer)");
        boolean saved = dialog.clickUpdateAndConfirm();
        Assert.assertTrue(saved, "'Güncelle' butonu tıklanamadı veya onay dialogu kapanmadı (geçici değer)");
        log.info("[DTS-MD] 'DBS Limit Tutarı' geçici test değerine ('{}') güncellendi.", TEMP_DBS_LIMIT);
    }

    @Then("dealerlimit tablosunda DBS Limit Tutarı geçici test değerine güncellenmiş olmalı")
    public void dealerlimitTablosundaDbsLimitTutariGeciciTestDegerineGuncellenmisOlmali() {
        BigDecimal expected = new BigDecimal(TEMP_DBS_LIMIT.replace(",", "."));
        BigDecimal actual = pollDealerLimitUntil(expected, 40);
        Assert.assertEquals(actual != null ? actual.stripTrailingZeros() : null, expected.stripTrailingZeros(),
                "dealerlimit.id=" + TARGET_DEALER_LIMIT_ID + " DBS Limit Tutarı geçici değere güncellenmedi. DB'deki son değer: " + actual);
        log.info("[DTS-MD] dealerlimit.id={} DBS Limit Tutarı geçici değere ('{}') güncellendiği DB'den doğrulandı.",
                TARGET_DEALER_LIMIT_ID, expected);
    }

    @And("\"DBS Limit Tutarı\" alanı orijinal değerine geri güncellenip kaydedilir")
    public void dbsLimitTutariAlaniOrijinalDegerineGeriGuncellenipKaydedilir() {
        CompanyEditDealerLimitDialogPage dialog = getDealerLimitDialogPage();
        Assert.assertTrue(dialog.isOpened(), "Restore adımı öncesi CompanyEditDealerLimitDialog açık değil");
        String restoreValue = originalDbsLimitAmount.stripTrailingZeros().toPlainString();
        boolean typed = dialog.setDbsLimitAmount(restoreValue);
        Assert.assertTrue(typed, "'DBS Limit Tutarı' alanı bulunamadı/doldurulamadı (restore)");
        boolean saved = dialog.clickUpdateAndConfirm();
        Assert.assertTrue(saved, "'Güncelle' butonu tıklanamadı veya onay dialogu kapanmadı (restore)");
        log.info("[DTS-MD] 'DBS Limit Tutarı' orijinal değerine ('{}') geri güncellendi.", restoreValue);
    }

    @Then("dealerlimit tablosunda DBS Limit Tutarı orijinal değerine geri dönmüş olmalı")
    public void dealerlimitTablosundaDbsLimitTutariOrijinalDegerineGeriDonmusOlmali() {
        BigDecimal actual = pollDealerLimitUntil(originalDbsLimitAmount, 40);
        Assert.assertEquals(actual != null ? actual.stripTrailingZeros() : null, originalDbsLimitAmount.stripTrailingZeros(),
                "dealerlimit.id=" + TARGET_DEALER_LIMIT_ID + " DBS Limit Tutarı orijinal değerine geri dönmedi (SHARED fixture — "
                        + "diğer DTS senaryolarını etkileyebilir!). DB'deki son değer: " + actual);
        log.info("[DTS-MD] dealerlimit.id={} DBS Limit Tutarı orijinal değerine ('{}') geri döndüğü DB'den doğrulandı.",
                TARGET_DEALER_LIMIT_ID, originalDbsLimitAmount);
    }

    /** dealerlimit.id={@value #TARGET_DEALER_LIMIT_ID} kaydının dbslimitamount'u beklenen değere
     *  ulaşana kadar (ya da timeout'a kadar) DB'yi poll eder — grid'in yeniden render tuzağıyla AYNI
     *  gerekçeyle (bkz. waitForAnyVisibleCellFold Javadoc'u) tek okuma yerine poll tercih edildi. */
    private BigDecimal pollDealerLimitUntil(BigDecimal expected, int timeoutSeconds) {
        BigDecimal expectedNormalized = expected.stripTrailingZeros();
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        BigDecimal last = null;
        while (System.currentTimeMillis() < deadline) {
            Optional<BigDecimal> current = DtsDbAssertions.findDealerLimitDbsAmount(TARGET_DEALER_LIMIT_ID);
            if (current.isPresent()) {
                last = current.get();
                if (last.stripTrailingZeros().equals(expectedNormalized)) {
                    return last;
                }
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return last;
            }
        }
        return last;
    }
}
