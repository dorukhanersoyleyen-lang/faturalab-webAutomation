# FL-013: FK Teklif Verme Akışı
Feature: Finansman Kurumu Teklif Verme UAT
  Finansman kurumu teklif taleplerini görebilmeli ve teklif verebilmeli.

  Background:
    Given faktoring olarak giriş yapılır
    And sistemde bir bordro mevcut

  @ui @uat @smoke @happy-path @kritik @fl-013
  Scenario: FL-013 - FK teklif taleplerini görebilmeli
    When finansman teklif talepleri ekranına gidilir
    And teklif talebi listesi grid'i görünmeli
    Then teklif talepleri başarıyla listelenmeli

  @ui @uat @smoke @happy-path @kritik @fl-013
  Scenario: FL-013b - FK teklif verebilmeli
    When finansman teklif talepleri ekranına gidilir
    And "Gunluk Teklif Talebi" sekmesine gecilirse
    And ilgili bordro satirinda "GOZAT" butonuna tiklanirsa
    Then bordro detay sayfasi veya dialogu acilmali
