@ui @login @kritik
Feature: Login ve Kimlik Dogrulama
  # TC-LOGIN-01 test case serisini kapsar.
  # LoginView ekrani uzerinden yapilan giris, hata ve 2FA senaryolarini icerir.
  # Referans: TC-LOGIN-01-giris.md

  Background:
    # Test URL: https://dev.faturalab.com/app
    # Her senaryo oncesinde tarayici LoginView ekraninda baslar (UIHooks tarafindan saglanir)

  # ───────────────────────────────────────────────────────────────────────────
  # TC-LOGIN-01-001 — Gecerli kimlik bilgileriyle basarili giris (Mutlu Yol)
  # Oncelik: Kritik | Tip: Pozitif | Bagli US: US-LOGIN-01.1
  # ───────────────────────────────────────────────────────────────────────────
  @smoke @happy-path
  Scenario: TC-LOGIN-01-001 - Gecerli kimlik bilgileriyle giris
    # Kullanici: EFG Gida (Company) — test@testggg.com / Dorukhan.1
    # UserState = ACTIVE, reCAPTCHA servisi eriselebilir
    Given tedarikci EFG roleyle dev2'ye giris yapildi
    Then tedarikci ana sayfasina yonlendirilmeli
    # TODO: implement step def — "tedarikci ana sayfasina yonlendirilmeli"

  # ───────────────────────────────────────────────────────────────────────────
  # TC-LOGIN-01-003 — Hatali sifre ile giris denemesi
  # Oncelik: Kritik | Tip: Negatif | Bagli US: US-LOGIN-01.1
  # ───────────────────────────────────────────────────────────────────────────
  @smoke
  Scenario: TC-LOGIN-01-003 - Hatali sifre ile giris denemesi
    # Gecerli email, yanlis sifre kullanilir
    # Beklenen: "E-posta veya sifre hatali" mesaji gosterilir, kullanici LoginView'da kalir
    Given LoginView ekrani acik
    When kullanici gecerli email ve hatali sifre ile giris yaparsa
    Then hata mesaji gorunmeli
    And kullanici LoginView'da kalmali
    # TODO: implement step def — "LoginView ekrani acik"
    # TODO: implement step def — "kullanici gecerli email ve hatali sifre ile giris yaparsa"
    # TODO: implement step def — "kullanici LoginView'da kalmali"

  # ───────────────────────────────────────────────────────────────────────────
  # TC-LOGIN-01-005 / TC-LOGIN-01-009 — Art arda hatali deneme ile hesap kilitlenme
  # Oncelik: Yuksek | Tip: Negatif | Bagli US: US-LOGIN-01.5
  # Not: Dokumanda TC-LOGIN-01-005 format hatasi (gecersiz email), hesap kilitleme TC-009'da
  # Burada senaryo tanimi "3 hatali deneme -> LOCKED" olarak yorumlanmistir
  # ───────────────────────────────────────────────────────────────────────────
  @smoke
  Scenario: TC-LOGIN-01-005 - Hesap kilitlenme (art arda hatali deneme)
    # Kilit esigi asildiginda UserState = LOCKED durumuna gecer
    # Beklenen: "Hesabiniz kilitlenmistir" mesaji, ek deneme yapilamaz
    Given LoginView ekrani acik
    When kullanici kilit esigi kadar yanlis sifre ile giris yaparsa
    Then hesap kilitlenme mesaji gorunmeli
    And kullanici sisteme giris yapamaz olmali
    # TODO: implement step def — "LoginView ekrani acik"
    # TODO: implement step def — "kullanici kilit esigi kadar yanlis sifre ile giris yaparsa"
    # TODO: implement step def — "hesap kilitlenme mesaji gorunmeli"
    # TODO: implement step def — "kullanici sisteme giris yapamaz olmali"

  # ───────────────────────────────────────────────────────────────────────────
  # TC-LOGIN-01-007 — 2FA aktif hesapla basarili giris (TOTP)
  # Oncelik: Kritik | Tip: Pozitif | Bagli US: US-LOGIN-01.2
  # ───────────────────────────────────────────────────────────────────────────
  @smoke @2fa
  Scenario: TC-LOGIN-01-007 - 2FA aktif hesapla basarili giris
    # Hesabinda 2FA aktif olan kullanici email+sifre girer
    # OneTimePasswordDialog acar, gecerli TOTP kodu girilir
    # Beklenen: Dogrulama basarili, ana sayfaya yonlendirilir
    Given LoginView ekrani acik
    When 2FA aktif kullanici email ve sifresiyle giris yaparsa
    And OneTimePasswordDialog uzerinde gecerli TOTP kodu girilirse
    And "Dogrula" butonuna tiklanirsa
    Then kullanici basariyla ana sayfaya yonlendirilmeli
    # TODO: implement step def — "LoginView ekrani acik"
    # TODO: implement step def — "2FA aktif kullanici email ve sifresiyle giris yaparsa"
    # TODO: implement step def — "OneTimePasswordDialog uzerinde gecerli TOTP kodu girilirse"
    # TODO: implement step def — "kullanici basariyla ana sayfaya yonlendirilmeli"

  # ───────────────────────────────────────────────────────────────────────────
  # TC-LOGIN-01-008 — 2FA hatali OTP kodu
  # Oncelik: Yuksek | Tip: Negatif | Bagli US: US-LOGIN-01.2
  # ───────────────────────────────────────────────────────────────────────────
  @smoke @2fa
  Scenario: TC-LOGIN-01-008 - 2FA hatali OTP kodu
    # Email+sifre girisi basarili, OneTimePasswordDialog acik
    # Gecersiz 6 haneli kod (000000) girilir
    # Beklenen: "Gecersiz dogrulama kodu" mesaji, dialog kapanmaz
    Given LoginView ekrani acik
    And 2FA aktif kullanici email ve sifresiyle giris yapilmis ve OTP dialog acik
    When OneTimePasswordDialog uzerinde gecersiz OTP kodu girilirse
    And "Dogrula" butonuna tiklanirsa
    Then OTP hata mesaji gorunmeli
    And OneTimePasswordDialog kapanmamali
    # TODO: implement step def — "LoginView ekrani acik"
    # TODO: implement step def — "2FA aktif kullanici email ve sifresiyle giris yapilmis ve OTP dialog acik"
    # TODO: implement step def — "OneTimePasswordDialog uzerinde gecersiz OTP kodu girilirse"
    # TODO: implement step def — "OTP hata mesaji gorunmeli"
    # TODO: implement step def — "OneTimePasswordDialog kapanmamali"
