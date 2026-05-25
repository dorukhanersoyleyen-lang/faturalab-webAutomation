@ui @dev2 @buyer @supplier @e2e @scn09a
Feature: SCN-09A - Baremsiz Fiyat Akisi (Duz Faiz Hesaplama)

  # SCN-09A-001
  @smoke @happy-path
  Scenario: SCN-09A-001 - ALBC fatura yukler, faktoring baremsiz teklif verir ve kabul eder, admin onaylar, tutar dogrulanir
    Given tum rol oturumlari hazirlandir:
      | rol       | kullanici                         |
      | admin     | dorukhan.ersoyleyen@faturalab.com |
      | buyer     | ALBC Marketler Zinciri A.S.       |
      | factoring | Akbank                            |
    When alici rolune gecilirse
    And "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And gecerli bir XML fatura dosyasi secilirse
    And "Kaydet" butonuna tiklanirsa
    Then fatura basariyla yuklenmeli
    And fatura durumu "PENDING_APPROVAL" olmali
    When admin rolune gecilirse
    And admin fatura yonetimi ekranina gidilirse
    And o fatura icin "ONAYLA" butonuna tiklanirsa
    Then fatura basariyla onaylanmali
    And fatura durumu "APPROVED" olmali
    When finansman rolune gecilirse
    And finansman aktif ihaleler listesine gidilirse
    And son aktif ihale icin teklif formu acilirsa
    And baremsiz teklif asagidaki degerlerle girilir:
      | faiz_orani | vade_gun |
      | 1.50       | 30       |
    And teklif "Gonder" butonuna tiklanirsa
    Then teklif basariyla olusturulmali
    And finansman kendi teklifini kabul eder
    When admin rolune gecilirse
    And admin bordro yonetimi ekranina gidilirse
    And olusturulan bordro icin "ONAYLA" butonuna tiklanirsa
    Then admin tarafindan bordro onay adimi tamamlandi
    And baremsiz hesaplama ile teklif tutari dogrulanir

  # SCN-09A-002
  @negative
  Scenario: SCN-09A-002 - Baremsiz akista fatura yuklenmeden teklif verilemez
    Given tum rol oturumlari hazirlandir:
      | rol       | kullanici                         |
      | admin     | dorukhan.ersoyleyen@faturalab.com |
      | buyer     | ALBC Marketler Zinciri A.S.       |
      | factoring | Akbank                            |
    When finansman rolune gecilirse
    And finansman aktif ihaleler listesine gidilirse
    Then teklif limit ve fiyat kriterlerine uygun olmali
