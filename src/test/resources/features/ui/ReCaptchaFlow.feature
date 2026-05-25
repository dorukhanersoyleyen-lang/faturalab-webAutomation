@ui @recaptcha
Feature: reCAPTCHA Entegrasyon Testleri
  # Login, Sifremi Unuttum ve Kayit Ol ekranlarindaki V2/V3 reCAPTCHA davranislarini dogrular.
  # Referans: TC-RCAP-01 — TC-RCAP-14

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-01
  # Login ekranina ilk giriste V3 rozeti gorunmeli; gecerli kimlikle giris yapilabilmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-01
  Scenario: TC-RCAP-01 - Login ekraninda V3 reCAPTCHA rozeti gorunmeli ve giris yapilabilmeli
    Given kullanici login ekranina gider
    Then V3 reCAPTCHA rozeti sag alt kosede gorunmeli
    When kullanici gecerli kimlik bilgileriyle giris yapar
    Then uygulama ana sayfasina yonlendirilmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-02
  # Yanlis sifre sonrasi V2 widget gorunmeli; V2 check edilip dogru sifre ile giris yapilabilmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-02
  Scenario: TC-RCAP-02 - Yanlis sifre sonrasi V2 widget gorunmeli ve giris yapilabilmeli
    Given kullanici login ekranina gider
    When kullanici yanlis sifre ile giris denemesi yapar
    Then V2 reCAPTCHA widget gorunmeli
    When kullanici V2 reCAPTCHA checkbox isaretler
    And kullanici dogru sifre ile giris yapar
    Then uygulama ana sayfasina yonlendirilmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-03
  # Login ekraninda F5 sonrasi V3 rozeti tekrar gorunmeli; giris yapilabilmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-03
  Scenario: TC-RCAP-03 - Login ekraninda F5 sonrasi V3 rozeti gorunmeli ve giris yapilabilmeli
    Given kullanici login ekranina gider
    When kullanici sayfayi yeniler
    Then V3 reCAPTCHA rozeti sag alt kosede gorunmeli
    When kullanici gecerli kimlik bilgileriyle giris yapar
    Then uygulama ana sayfasina yonlendirilmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-04
  # Sifremi Unuttum ekraninda V3 rozeti olmamali; V2 widget gorunmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-04
  Scenario: TC-RCAP-04 - Sifremi Unuttum ekraninda V3 yok V2 widget gorunmeli
    Given kullanici login ekranina gider
    When kullanici Sifremi Unuttum butonuna tiklar
    Then sifremi unuttum ekrani acilmali
    And V3 reCAPTCHA rozeti gorunmemeli
    And V2 reCAPTCHA widget gorunmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-05
  # Sifremi Unuttum'dan logo tiklaninca login ekranina donulmeli; V3 rozeti gorunmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-05
  Scenario: TC-RCAP-05 - Sifremi Unuttum logosuna tiklaninca login ekranina donulmeli ve V3 rozeti gorunmeli
    Given kullanici sifremi unuttum ekranindadir
    When kullanici FATURALAB logosuna tiklar
    Then login ekrani acilmali
    And V3 reCAPTCHA rozeti sag alt kosede gorunmeli
    When kullanici gecerli kimlik bilgileriyle giris yapar
    Then uygulama ana sayfasina yonlendirilmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-06
  # Sifremi Unuttum: V2 check + gecerli eposta + KURTAR -> basari mesaji + login'e yonlendirilmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-06
  Scenario: TC-RCAP-06 - Sifremi Unuttum V2 check ve gecerli eposta ile basari mesaji gorunmeli
    Given kullanici sifremi unuttum ekranindadir
    When kullanici V2 reCAPTCHA checkbox isaretler
    And kullanici gecerli eposta adresini girer
    And kullanici KURTAR butonuna tiklar
    Then basari mesaji gorunmeli
    And login ekranina yonlendirilmeli
    And V3 reCAPTCHA rozeti sag alt kosede gorunmeli
    When kullanici gecerli kimlik bilgileriyle giris yapar
    Then uygulama ana sayfasina yonlendirilmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-07
  # Sifremi Unuttum: V2 suresi dolunca "Gecersiz reCaptcha" hatasi gorunmeli.
  # Bu senaryo ~2.5 dakika bekler (@slow ile isaretlenmistir).
  # ─────────────────────────────────────────────────────────────────────────────
  @slow @tc-rcap-07
  Scenario: TC-RCAP-07 - Sifremi Unuttum V2 suresi dolunca gecersiz recaptcha hatasi gorunmeli
    Given kullanici sifremi unuttum ekranindadir
    When kullanici V2 reCAPTCHA checkbox isaretler
    And kullanici V2 reCAPTCHA suresinin dolmasini bekler
    When kullanici gecerli eposta adresini girer
    And kullanici KURTAR butonuna tiklar
    Then gecersiz recaptcha hata mesaji gorunmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-08
  # Kayit Ol ekraninda V3 rozeti olmamali; V2 widget gorunmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-08
  Scenario: TC-RCAP-08 - Kayit Ol ekraninda V3 yok V2 widget gorunmeli
    Given kullanici login ekranina gider
    When kullanici Kayit Ol butonuna tiklar
    Then kayit ol ekrani acilmali
    And V3 reCAPTCHA rozeti gorunmemeli
    And V2 reCAPTCHA widget gorunmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-09
  # Kayit Ol'dan logo tiklaninca login ekranina donulmeli; V3 rozeti gorunmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-09
  Scenario: TC-RCAP-09 - Kayit Ol logosuna tiklaninca login ekranina donulmeli ve V3 rozeti gorunmeli
    Given kullanici kayit ol ekranindadir
    When kullanici FATURALAB logosuna tiklar
    Then login ekrani acilmali
    And V3 reCAPTCHA rozeti sag alt kosede gorunmeli
    When kullanici gecerli kimlik bilgileriyle giris yapar
    Then uygulama ana sayfasina yonlendirilmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-10
  # Kayit Ol: V2 check + form doldurunca eposta dogrulama ekranina yonlendirilmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-10
  Scenario: TC-RCAP-10 - Kayit Ol V2 check ve form ile eposta dogrulama ekranina yonlendirilmeli
    Given kullanici kayit ol ekranindadir
    When kullanici V2 reCAPTCHA checkbox isaretler
    And kullanici kayit formunu doldurur
    And kullanici KAYIT OL butonuna tiklar
    Then eposta dogrulama ekranina yonlendirilmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-11
  # Kayit Ol: V2 suresi dolunca "Captcha dogrulama" hatasi gorunmeli.
  # Bu senaryo ~2.5 dakika bekler (@slow ile isaretlenmistir).
  # ─────────────────────────────────────────────────────────────────────────────
  @slow @tc-rcap-11
  Scenario: TC-RCAP-11 - Kayit Ol V2 suresi dolunca captcha dogrulama hatasi gorunmeli
    Given kullanici kayit ol ekranindadir
    When kullanici V2 reCAPTCHA checkbox isaretler
    And kullanici V2 reCAPTCHA suresinin dolmasini bekler
    When kullanici kayit formunu doldurur
    And kullanici KAYIT OL butonuna tiklar
    Then captcha dogrulama hata mesaji gorunmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-12
  # Login V2 fallback: V2 suresi dolunca dogru sifre ile giris yapinca "Gecersiz reCaptcha" hatasi.
  # Bu senaryo ~2.5 dakika bekler (@slow ile isaretlenmistir).
  # ─────────────────────────────────────────────────────────────────────────────
  @slow @tc-rcap-12
  Scenario: TC-RCAP-12 - Login V2 fallback suresi dolunca gecersiz recaptcha hatasi gorunmeli
    Given kullanici login ekranina gider
    When kullanici yanlis sifre ile giris denemesi yapar
    Then V2 reCAPTCHA widget gorunmeli
    When kullanici V2 reCAPTCHA checkbox isaretler
    And kullanici V2 reCAPTCHA suresinin dolmasini bekler
    When kullanici dogru sifre ile giris yapar
    Then gecersiz recaptcha hata mesaji gorunmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-13
  # Cikis sonrasi login ekranina donulmeli; V3 rozeti gorunmeli; tekrar giris yapilabilmeli.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-13
  Scenario: TC-RCAP-13 - Cikis sonrasi login ekraninda V3 rozeti ile tekrar giris yapilabilmeli
    Given kullanici basariyla giris yapmis
    When kullanici cikis yapar
    Then login ekranina yonlendirilmeli
    And V3 reCAPTCHA rozeti sag alt kosede gorunmeli
    When kullanici gecerli kimlik bilgileriyle giris yapar
    Then uygulama ana sayfasina yonlendirilmeli

  # ─────────────────────────────────────────────────────────────────────────────
  # TC-RCAP-14
  # Basarili giris sonrasi F5 ile sayfa yenilenince oturum devam etmeli; login'e atilmamali.
  # ─────────────────────────────────────────────────────────────────────────────
  @smoke @tc-rcap-14
  Scenario: TC-RCAP-14 - Basarili giris sonrasi F5 ile sayfa yenilenince oturum devam etmeli
    Given kullanici basariyla giris yapmis
    When kullanici sayfayi yeniler
    Then kullanici uygulamada kalmali
    And login ekranina yonlendirilmemeli
