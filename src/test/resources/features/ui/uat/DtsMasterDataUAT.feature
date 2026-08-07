# DTS Master Data — Bayi / Finansal Kurum İlişkisi (companyfactoring) / Bayi Limitleri CRUD
# Tedarikçi: Test Otomasyon Sadece Tedarikçi (company.id=998) — dts.supplier.impersonate.identifier.
# Bu dosya DTSIslemUAT.feature'a (DTS-001, işlem akışı) DOKUNMAZ — ayrı, master-data odaklı bir dosyadır.
#
# Kapsam (bu görevde @dts-fk-crud VE @dts-bayi-crud implemente edildi — @dts-limit-crud @wip olarak
# işaretlenip aşağıda yorum satırı olarak planlandı, sonraki görevde tamamlanacak):
#
# @dts-fk-crud (İMPLEMENTE EDİLDİ): Finansal Kurumlar ekranında (CompanyDisplayFactoringsView)
#   "Anlaşmalı Finansal Kurum Ekle" (CompanyAddEditFactoringDialog, editMode=false) ile
#   "Türkiye İş Bankası A.Ş." (factoring.id=68 — mevcut 4 FK'yla çakışmaz: Akbank/57, MNO/53,
#   Garanti/58, Denizbank/62) eklenir → listede doğrulanır → "Düzenle" (editMode=true) ile
#   açılır → "DBS Maliyet (%)" alanı değiştirilir → listede doğrulanır.
#   ⚠️ Kaynak kod kanıtı (origin/vaadin-24, CompanyAddEditFactoringDialog.buildFormLayout()):
#   CREATE modunda formda SADECE "Para Birimi" ve "Finansal Kurum" alanları vardır — "Çalışma
#   Modeli"/DBS Maliyet/First Loss/Fiyat alanları SADECE editMode==true iken forma eklenir.
#   Dialogda HİÇBİR ZAMAN genel bir "Limit" (tutar) alanı yoktur — sadece "Limit Üstü Gönderim"
#   (allowOverLimit) checkbox'ı vardır. Bu yüzden senaryo CREATE'te sadece FK seçimiyle sınırlı,
#   EDIT'te ise gerçekten var olan "DBS Maliyet (%)" alanı değiştirilip doğrulanıyor.
#   ⚠️ AmountField (DBS Maliyet gibi tutar/yüzde alanları) yazarken KRİTİK sıralama gerektiriyor
#   (odaklanma yarışı + blur olmadan sunucu senkronu olmaması) — bkz. web-automation.md "AmountField"
#   maddesi ve CompanyAddEditFactoringDialogPage#setDbsCost Javadoc'u; @dts-bayi-crud'daki "Teminat
#   Limiti" AYNI bileşen/AYNI kalıpla yazılıyor.
#
# @dts-bayi-crud (İMPLEMENTE EDİLDİ): Bayiler ekranında (CompanyDisplayDealerView) her koşumda
#   BENZERSİZ (timestamp'li) ad/kod/VKN ile "Bayi Ekle" (CompanyAddEditDealerDialog, editMode=false)
#   → listede doğrulanır → "Düzenle" (editMode=true) ile açılır → "Teminat Limiti" (AmountField)
#   değiştirilir → listede doğrulanır. "Otomasyon Bayi" (dealerlimit.id=88, @dts-limit-crud'un hedefi)
#   kaydına HİÇ dokunmaz — TAM YENİ, izole bir bayi oluşturur (TZF/DFP'deki "her koşumda benzersiz
#   veri" kuralıyla UYUMLU — @dts-fk-crud'daki sabit-hedef+idempotentlik-guard istisnasından FARKLI).
#   ⚠️ Kaynak kod kanıtı (origin/vaadin-24, CompanyAddEditDealerDialog.buildFormLayout()): editMode'da
#   "Bayi Ünvanı" (dealerNameField.setEnabled(false)) ve "Vergi Kimlik No" (taxtNumberTextField.
#   setEnabled(false)) DISABLED'dır — "telefon" alanı diye bir alan YOKTUR. Düzenlenebilir gerçek
#   alanlar: "Bayi Kodu", "Teminat Limiti", "Para Birimi" (CurrencyType: TL/EUR/USD/AED/GBP — "TRY"
#   DEĞİL, kod "TL" kullanır), "Durumu" (Aktif/Pasif radio).
#   ⚠️ Grid gösterimi kod-yolu olarak DBS Maliyet'ten FARKLI (Teminat Limiti `InvoiceHelper.
#   getAmountLabelForTable`→`Convert.toHumanReadableAmountString`, DBS Maliyet plain
#   `BigDecimal.toString()`) ama CANLI SONUÇ AYNI: 2026-08-07 koşumunda dev'de İKİSİ DE nokta-ondalık
#   gösteriyor ("500.00 TL" — `AppSettingsManager.getLocale()` dev'de TR değil çözülüyor). Yazma
#   virgüllü (AmountField normalize ediyor), grid doğrulaması NOKTALI — DBS Maliyet'teki yazma≠gösterim
#   ayrımıyla AYNI kalıp; ayrıca EDIT'te alan ÖNCEDEN DOLU geldiği için odaklanma-yarışının geç gelen
#   sunucu push'u riski CREATE'e göre daha yüksek — yaz-ve-doğrula-yeniden-dene döngüsü kullanılıyor
#   (bkz. CompanyAddEditDealerDialogPage#setGuaranteeLimit).
#
# @dts-limit-crud (İMPLEMENTE EDİLDİ): Bayi Limitleri ekranı (CompanyDisplayDealerLimitsView) SADECE
#   Read+Update destekler — UI'da "Ekle" butonu YOK, REST tarafında da (CompanyService./dealer/limit)
#   yazma/oluşturma endpoint'i YOK (Api.dealerLimit() sadece sorgu). Bu KASITLI bir kısıtlamadır,
#   gizlenmez: Create adımı senaryoda YOKTUR. Mevcut dealerlimit.id=88 (Otomasyon Bayi ↔ Akbank,
#   creditedAmount=0, DBS Limit Tutarı=200000.00 TL) kaydı "Bayi Ünvanı" filtresiyle bulunur →
#   "Düzenle" → "DBS Limit Tutarı" GEÇİCİ bir test değerine değiştirilir → DOĞRULANIR → SONRA
#   ORİJİNAL değerine (200000) GERİ DÖNDÜRÜLÜR → doğrulanır.
#   ⚠️ Restore adımı KASITLI eklendi: bu SHARED bir fixture kaydı (DTS-001/DTS_API-001 senaryoları da
#   "Otomasyon Bayi" + "Test Otomasyon Sadece Tedarikçi" kurulumunu kullanıyor) — kalıcı olarak
#   200000'den küçük bir değerde bırakmak diğer senaryoların yeterlilik kontrollerini (limit >=
#   kullanılan tutar) sessizce bozabilir. TZF/DFP/diğer CRUD senaryolarının "her koşumda benzersiz
#   veri" kuralından FARKLI, KASITLI bir istisna: bu senaryo veriyi bulduğu haliyle BIRAKMALI.
#   ⚠️ Doğrulama GRİD METİN TARAMASI YERİNE DB'den (`DtsDbAssertions.findDealerLimitDbsAmount`)
#   yapılıyor: DBS Limit Tutarı da (Teminat Limiti/DBS Maliyet gibi) `InvoiceHelper.
#   getAmountLabelForTable` ile formatlanıyor, ama 200000 büyüklüğünde bir değerde olası binlik-ayraç
#   biçiminin (nokta mı virgül mü) belirsizliği metin aramasını kırılgan kılardı — DB'den BigDecimal
#   okumak formatlamadan tamamen bağımsız, kesin bir doğrulama sağlıyor (DTS-001'in dbsdocs DB
#   doğrulamasıyla AYNI gerekçe).
#   ⚠️ AmountField yazımı "DBS Limit Tutarı" alanında da CompanyAddEditDealerDialogPage#setGuaranteeLimit
#   ile AYNI kanıtlanmış (clear() DEĞİL, Ctrl+A) kalıbı kullanıyor (bkz. CompanyEditDealerLimitDialogPage).
Feature: DTS Master Data (Bayi / Finansal Kurum / Bayi Limitleri) CRUD

  @ui @uat @regression @dts @dts-fk-crud
  Scenario: DTS-MD-002 - Yeni Finansal Kurum ilişkisi (Türkiye İş Bankası A.Ş.) eklenip düzenlenmeli
    Given dts tedarikcisi olarak giriş yapılır
    When tedarikçi "Finansal Kurumlar" menüsüne gider
    And "Anlaşmalı Finansal Kurum Ekle" butonuna tıklanır
    Then "Anlaşmalı Finansal Kurum" ekleme diyaloğu açılmalı
    When "Türkiye İş Bankası A.Ş." finansal kurumu seçilip kaydedilir
    Then Finansal Kurumlar listesinde "Türkiye İş Bankası A.Ş." satırı görünmeli
    When "Türkiye İş Bankası A.Ş." satırı için "Düzenle" butonuna tıklanır
    Then "Anlaşmalı Finansal Kurum" düzenleme diyaloğu açılmalı
    When "DBS Maliyet (%)" alanı yeni bir değere güncellenip kaydedilir
    Then Finansal Kurumlar listesinde "Türkiye İş Bankası A.Ş." satırındaki DBS Maliyet güncellenmiş olmalı

  @ui @uat @regression @dts @dts-bayi-crud
  Scenario: DTS-MD-003 - Yeni bayi eklenip Teminat Limiti düzenlenmeli
    Given dts tedarikcisi olarak giriş yapılır
    When tedarikçi "Bayiler" menüsüne gider
    And "Bayi Ekle" butonuna tıklanır
    Then "Bayi Ekle" ekleme diyaloğu açılmalı
    When benzersiz bayi bilgileri girilip kaydedilir
    Then Bayiler listesinde yeni bayi satırı görünmeli
    When yeni bayi satırı için "Düzenle" butonuna tıklanır
    Then bayi düzenleme diyaloğu açılmalı
    When "Teminat Limiti" alanı yeni bir değere güncellenip kaydedilir
    Then Bayiler listesinde güncellenen Teminat Limiti değeri görünmeli

  @ui @uat @regression @dts @dts-limit-crud
  Scenario: DTS-MD-004 - Otomasyon Bayi'nin DBS Limit Tutarı düzenlenip orijinal değerine geri döndürülmeli
    Given dts tedarikcisi olarak giriş yapılır
    When tedarikçi "Bayi Limitleri" menüsüne gider
    And "Otomasyon Bayi" satırı için "Düzenle" butonuna tıklanır
    Then bayi limiti düzenleme diyaloğu açılmalı
    When "DBS Limit Tutarı" alanı geçici bir test değerine güncellenip kaydedilir
    Then dealerlimit tablosunda DBS Limit Tutarı geçici test değerine güncellenmiş olmalı
    When "Otomasyon Bayi" satırı için "Düzenle" butonuna tıklanır
    And "DBS Limit Tutarı" alanı orijinal değerine geri güncellenip kaydedilir
    Then dealerlimit tablosunda DBS Limit Tutarı orijinal değerine geri dönmüş olmalı
