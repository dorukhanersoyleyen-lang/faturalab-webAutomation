# FL-005: Fatura Görünüm Sayısı Ayarı (Tedarikçi Ayarlar)
# FL-006: Hızlı Teklif Al Modal (Tedarikçi)
# FL-007: Teklif Talebi Süresi Değiştirme
Feature: Tedarikçi Teklif Alma ve Ayarlar UAT
  Tedarikçi kullanıcısı ayarlar, hızlı teklif al modalı ve teklif talebi süresi işlemlerini gerçekleştirebilmeli.

  Background:
    Given tedarikci olarak giriş yapılır

  @ui @uat @smoke @happy-path @fl-005
  Scenario: FL-005 - Tedarikçi fatura görünüm sayısını değiştirebilmeli
    When tedarikci ayarlar ekranına gidilir
    And fatura görünüm sayısı dropdown'ı açılır
    And görünüm sayısı "25" olarak seçilir
    And ayarlar kaydedilir
    Then fatura görünüm sayısı "25" olarak kaydedilmiş olmalı
    And faturalar listesinde sayfa başına "25" satır görünmeli

  @ui @uat @smoke @happy-path @fl-006
  Scenario: FL-006 - Hızlı Teklif Al modalı açılabilmeli ve kullanılabilir olmalı
    When faturalarım ekranına gidilir
    And fatura listesinden bir fatura satırı seçilir
    And "Hızlı Teklif Al" butonuna tıklanır
    Then hızlı teklif al modalı açılmalı
    And modal içeriği görünmeli
    And modal boyutu ekrana uygun olmalı

  @ui @uat @smoke @happy-path @fl-006
  Scenario: FL-006b - Hızlı Teklif Al modalı tüm alanları içermeli
    When faturalarım ekranına gidilir
    And fatura listesinden bir fatura satırı seçilir
    And "Hızlı Teklif Al" butonuna tıklanır
    Then hızlı teklif al modalı açılmalı
    And modal içinde alıcı bilgisi alanı görünmeli
    And modal içinde tutar alanı görünmeli
    And modal içinde süre alanı görünmeli

  @ui @uat @smoke @happy-path @fl-007
  Scenario: FL-007 - Teklif talebi süresi değiştirilebilmeli (1 gün)
    When işlem bekleyenler ekranına gidilir
    And bekleyen fatura satırında "Teklif Al" butonuna tıklanır
    And teklif talebi süresi "1" gün olarak seçilir
    And teklif talebi gönderilir
    Then teklif talebi başarıyla oluşturulmuş olmalı
    And teklif talebi süresinin "1" gün olarak ayarlandığı görünmeli

  @ui @uat @smoke @happy-path @fl-007
  Scenario: FL-007b - Teklif talebi süresi 3 gün olarak ayarlanabilmeli
    When işlem bekleyenler ekranına gidilir
    And bekleyen fatura satırında "Teklif Al" butonuna tıklanır
    And teklif talebi süresi "3" gün olarak seçilir
    And teklif talebi gönderilir
    Then teklif talebi başarıyla oluşturulmuş olmalı
    And teklif talebi süresinin "3" gün olarak ayarlandığı görünmeli
