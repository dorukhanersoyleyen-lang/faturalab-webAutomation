# FL-008: Teklif Alma Akışı E2E (Critical)
# FL-018: TZF Temel Akışlar (Critical)
# FL-019: DFP Test Senaryoları
Feature: E2E Akışlar UAT
  Sistemin uçtan uca iş akışları eksiksiz çalışmalı.

  @ui @uat @smoke @happy-path @kritik @fl-008
  Scenario: FL-008 - Teklif alma tam akışı sorunsuz çalışmalı (E2E)
    Given tedarikci olarak giriş yapılır
    When faturalarım ekranına gidilir
    And yeni fatura yükleme ekranına gidilir
    And fatura yükle butonuna tıklanır
    And geçerli bir fatura dosyası seçilir
    And fatura kaydedilir
    And işlem bekleyenler ekranına gidilir
    And bekleyen fatura satırında teklif al butonuna tıklanır
    And teklif talebi oluşturulur ve gönderilir
    Then teklif talebi başarıyla oluşturulmuş olmalı
    Given faktoring olarak giriş yapılır
    When finansman teklif talepleri ekranına gidilir
    And teklif talebi listesinden ilgili talep satırı seçilir
    And teklif formu doldurulur
    And teklif gönderilir
    Then finansman teklifi başarıyla iletilmiş olmalı
    Given tedarikci olarak giriş yapılır
    When gelen teklifler ekranına gidilir
    And iletilen teklif satırında "Kabul Et" butonuna tıklanır
    And teklif kabul onayı verilir
    Then teklif kabul edilmiş olmalı
    Given faktoring olarak giriş yapılır
    When finansman teklif talepleri ekranına gidilir
    And kabul edilen teklif için bordro oluşturma başlatılır
    And bordro parametreleri onaylanır
    Then bordro başarıyla oluşturulmuş olmalı
    And tüm E2E akışı sorunsuz tamamlanmalı

  @ui @uat @smoke @happy-path @kritik @fl-018
  Scenario: FL-018 - TZF temel akışı çalışmalı
    Given tedarikci olarak giriş yapılır
    When TZF ekranına gidilir
    Then TZF ekranı başarıyla yüklenmiş olmalı
    And TZF fatura listesi görünmeli
    When TZF fatura listesinden bir fatura satırı seçilir
    And TZF teklif alma akışı başlatılır
    And TZF teklif talebi parametreleri girilir
    And TZF teklif talebi gönderilir
    Then TZF teklif talebi başarıyla oluşturulmuş olmalı
    And TZF akışı sorunsuz çalışmalı

  @ui @uat @smoke @happy-path @fl-019
  Scenario: FL-019 - DFP modülü temel özellikleri çalışmalı
    Given tedarikci olarak giriş yapılır
    When DFP ekranına gidilir
    Then DFP ekranı başarıyla yüklenmiş olmalı
    And DFP fatura listesi veya yükleme alanı görünmeli
    When DFP fatura yükleme akışı başlatılır
    And DFP fatura bilgileri girilir
    And DFP fatura kaydedilir
    Then DFP modülü erişilebilir olmalı
    And DFP işlemi başarıyla gerçekleşmiş olmalı
