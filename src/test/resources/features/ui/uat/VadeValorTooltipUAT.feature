# WP-5492: PROD | UX | Tedarikçi | Vade Tarihi ve Valör Tarihi Alanlarına Tooltip Eklenmesi
# Kapsam: Fatura Yükleme, Teklif Detay, Teklif Kabul, Bordro Detay ekranlarındaki tooltip içerikleri
# Tooltip metinleri:
#   Vade Tarihi  → "Ödemenin yapılması gereken son gündür."
#   Valör Tarihi → "Tutarın hesabınıza geçeceği gündür."
Feature: Vade Tarihi ve Valör Tarihi Tooltip (WP-5492) UAT
  Tedarikçi kullanıcısı vade tarihi ve valör tarihi alanlarının yanında
  açıklayıcı tooltip/bilgilendirme metni görebilmeli.

  Background:
    Given tedarikci olarak giriş yapılır

  # ─── FATURA YÜKLEME EKRANI ────────────────────────────────────────────────────

  @ui @uat @smoke @happy-path @wp-5492 @tooltip @fatura-yukleme
  Scenario: WP-5492-T01 - Fatura yükleme ekranında Vade Tarihi alanında tooltip görünmeli
    When fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    And örnek XML veya PDF fatura dosyası seçilir
    Then "Vade Tarihi" alanının yanında tooltip ikonu görünmeli
    And "Vade Tarihi" tooltip ikonuna hover yapılır
    And tooltip metni "İlgili fatura için ödemenin yapılması gereken son gündür." olarak görünmeli

  @ui @uat @smoke @happy-path @wp-5492 @tooltip @fatura-yukleme
  Scenario: WP-5492-T02 - Fatura yükleme ekranında Ek Vade Tarihi alanında tooltip görünmeli
    When fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    And örnek XML veya PDF fatura dosyası seçilir
    Then "Ek Vade Tarihi" alanının yanında tooltip ikonu görünmeli
    And "Ek Vade Tarihi" tooltip ikonuna hover yapılır
    And tooltip metni "İlgili fatura için ödemenin yapılması gereken son gündür." olarak görünmeli

  @ui @uat @negatif @wp-5492 @tooltip @fatura-yukleme
  Scenario: WP-5492-T03 - Fatura yükleme ekranında Valör Tarihi için tooltip ikonu bulunmamalı (alan yoksa)
    When fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    And örnek XML veya PDF fatura dosyası seçilir
    Then "Valör Tarihi" alanı fatura yükleme formunda mevcut değilse tooltip da görünmemeli

  # ─── TEKLİF DETAY EKRANI ──────────────────────────────────────────────────────

  @ui @uat @smoke @happy-path @wp-5492 @tooltip @teklif-detay @disabled
  Scenario: WP-5492-T04 - Teklif detay ekranında Vade Tarihi tooltip görünmeli
    When faturalarım ekranına gidilir
    And fatura listesinden teklif durumunda bir fatura seçilir
    And teklif detay ekranına gidilir
    Then "Vade Tarihi" alanının yanında tooltip ikonu görünmeli
    And "Vade Tarihi" tooltip ikonuna hover yapılır
    And tooltip metni "Ödemenin yapılması gereken son gündür." olarak görünmeli

  @ui @uat @smoke @happy-path @wp-5492 @tooltip @teklif-detay @disabled
  Scenario: WP-5492-T05 - Teklif detay ekranında Valör Tarihi tooltip görünmeli
    When faturalarım ekranına gidilir
    And fatura listesinden teklif durumunda bir fatura seçilir
    And teklif detay ekranına gidilir
    Then "Valör Tarihi" alanının yanında tooltip ikonu görünmeli
    And "Valör Tarihi" tooltip ikonuna hover yapılır
    And tooltip metni "Tutarın hesabınıza geçeceği gündür." olarak görünmeli

  @ui @uat @negatif @wp-5492 @tooltip @teklif-detay @disabled
  Scenario: WP-5492-T06 - Teklif detay ekranında tooltip metni boş veya eksik olmamalı
    When faturalarım ekranına gidilir
    And fatura listesinden teklif durumunda bir fatura seçilir
    And teklif detay ekranına gidilir
    Then "Vade Tarihi" tooltip metni boş olmamalı
    And "Valör Tarihi" tooltip metni boş olmamalı

  # ─── TEKLİF KABUL AKIŞI ───────────────────────────────────────────────────────

  @ui @uat @smoke @happy-path @wp-5492 @tooltip @teklif-kabul @disabled
  Scenario: WP-5492-T07 - Teklif kabul akışında Vade Tarihi tooltip görünmeli
    When işlem bekleyenler ekranına gidilir
    And teklif kabul ekranına gidilir
    Then "Vade Tarihi" alanının yanında tooltip ikonu görünmeli
    And "Vade Tarihi" tooltip ikonuna hover yapılır
    And tooltip metni "Ödemenin yapılması gereken son gündür." olarak görünmeli

  @ui @uat @smoke @happy-path @wp-5492 @tooltip @teklif-kabul @disabled
  Scenario: WP-5492-T08 - Teklif kabul akışında Valör Tarihi tooltip görünmeli
    When işlem bekleyenler ekranına gidilir
    And teklif kabul ekranına gidilir
    Then "Valör Tarihi" alanının yanında tooltip ikonu görünmeli
    And "Valör Tarihi" tooltip ikonuna hover yapılır
    And tooltip metni "Tutarın hesabınıza geçeceği gündür." olarak görünmeli

  @ui @uat @negatif @wp-5492 @tooltip @teklif-kabul @disabled
  Scenario: WP-5492-T09 - Teklif kabul akışında tooltip ikonu tıklanabilir/hover edilebilir olmalı
    When işlem bekleyenler ekranına gidilir
    And teklif kabul ekranına gidilir
    Then "Vade Tarihi" tooltip ikonu disabled veya gizli olmamalı
    And "Valör Tarihi" tooltip ikonu disabled veya gizli olmamalı

  # ─── BORDRO DETAY EKRANI ──────────────────────────────────────────────────────

  @ui @uat @smoke @happy-path @wp-5492 @tooltip @bordro-detay @disabled
  Scenario: WP-5492-T10 - Bordro detay ekranında Vade Tarihi tooltip görünmeli
    When bordro listesi ekranına gidilir
    And bordro listesinden bir bordro seçilir
    And bordro detay ekranı açılır
    Then "Vade Tarihi" alanının yanında tooltip ikonu görünmeli
    And "Vade Tarihi" tooltip ikonuna hover yapılır
    And tooltip metni "Ödemenin yapılması gereken son gündür." olarak görünmeli

  @ui @uat @smoke @happy-path @wp-5492 @tooltip @bordro-detay @disabled
  Scenario: WP-5492-T11 - Bordro detay ekranında Valör Tarihi tooltip görünmeli
    When bordro listesi ekranına gidilir
    And bordro listesinden bir bordro seçilir
    And bordro detay ekranı açılır
    Then "Valör Tarihi" alanının yanında tooltip ikonu görünmeli
    And "Valör Tarihi" tooltip ikonuna hover yapılır
    And tooltip metni "Tutarın hesabınıza geçeceği gündür." olarak görünmeli

  @ui @uat @negatif @wp-5492 @tooltip @bordro-detay @disabled
  Scenario: WP-5492-T12 - Bordro detay ekranında tooltip metni yanlış içerik göstermemeli
    When bordro listesi ekranına gidilir
    And bordro listesinden bir bordro seçilir
    And bordro detay ekranı açılır
    Then "Vade Tarihi" tooltip metni "Tutarın hesabınıza geçeceği gündür." içermemeli
    And "Valör Tarihi" tooltip metni "Ödemenin yapılması gereken son gündür." içermemeli

  # ─── ÇAPRAZ EKRAN / EDGE CASE ─────────────────────────────────────────────────

  @ui @uat @edge-case @wp-5492 @tooltip @disabled
  Scenario: WP-5492-T13 - Tüm ekranlarda tooltip ikonu sayfada hazır olmadan önce hover edilirse içerik görünmemeli
    When fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    And "Manuel" sekmesine tıklanır
    And sayfa tam yüklenmeden "Vade Tarihi" tooltip ikonuna hover yapılırsa
    Then tooltip içeriği boş görünmemeli veya hata fırlatmamalı

  @ui @uat @edge-case @wp-5492 @tooltip @disabled
  Scenario: WP-5492-T14 - Tooltip, mobil viewport boyutunda da görünür olmalı
    When viewport mobil boyutuna ayarlanır
    And fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    And "Manuel" sekmesine tıklanır
    Then "Vade Tarihi" tooltip ikonu ekranda görünür olmalı
    And tooltip içeriği ekran sınırları dışına taşmamalı
