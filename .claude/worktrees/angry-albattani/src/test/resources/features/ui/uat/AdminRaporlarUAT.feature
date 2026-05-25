# FL-001: Teklif Talepleri Filtreleme (FAILED - high priority)
# FL-002: Rapor İndirme
Feature: Admin Raporlar UAT
  Admin kullanıcısı raporlar modülünü test eder.

  Background:
    Given admin olarak giriş yapılır

  @ui @uat @smoke @happy-path @fl-001
  Scenario: FL-001 - Teklif Talepleri raporu filtreler doğru çalışmalı
    When admin raporlar menüsüne gidilir
    And teklif talepleri ekranına gidilir
    And tarih filtresi uygulanır
    And durum filtresi "PENDING" olarak uygulanır
    Then filtreli teklif talepleri listesi görünmeli

  @ui @uat @smoke @happy-path @fl-001
  Scenario: FL-001b - FK filtresi ile teklif talepleri filtrelenebilmeli
    When admin raporlar menüsüne gidilir
    And teklif talepleri ekranına gidilir
    And teklif talepleri grid'i görünmeli
    Then filtreli teklif talepleri listesi görünmeli

  @ui @uat @smoke @happy-path @fl-002
  Scenario: FL-002 - Admin rapor indirebilmeli
    When admin raporlar menüsüne gidilir
    And raporlar listesinden bir rapor tipi seçilir
    And indir butonuna tıklanır
    Then rapor başarıyla indirilmeli veya indirme başlamalı
