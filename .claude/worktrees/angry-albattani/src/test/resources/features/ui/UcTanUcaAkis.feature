@ui @dev2 @e2e @kritik
Feature: SCN-01 - Uctan Uca Ihale Akisi (4 Rol)

  # SCN-01-001
  @smoke @happy-path
  Scenario: SCN-01-001 - Tam is akisi - Fatura yukleme Admin onay Ihale Alici onay Bordro
    Given tum rol oturumlari hazirlandir:
      | rol        | kullanici                         |
      | company    | EFG Gida A.S.                     |
      | admin      | dorukhan.ersoyleyen@faturalab.com |
      | buyer      | ALBC Marketler Zinciri A.S.       |
      | factoring  | Akbank                            |
    When tedarikci rolune gecilirse
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
    When alici rolune gecilirse
    And ihale listesi ekranina gidilirse
    And bekleyen ihale icin "ONAYLA" butonuna tiklanirsa
    Then ihale basariyla onaylanmali
    And finansman sisteminde bordro olusturulmali
    When finansman rolune gecilirse
    And "Teklif Talebi Yonetimi" ekranina gidilirse
    And ilgili bordro satirinda "ONAYLA" butonuna tiklanirsa
    Then bordro basariyla onaylanmali
    And bordro durumu "APPROVED" olmali

  # SCN-01-002
  @smoke
  Scenario: SCN-01-002 - Alici reddi akisi
    Given tum rol oturumlari hazirlandir:
      | rol        | kullanici                         |
      | company    | EFG Gida A.S.                     |
      | admin      | dorukhan.ersoyleyen@faturalab.com |
      | buyer      | ALBC Marketler Zinciri A.S.       |
    When tedarikci bir fatura yukleyip admin tarafindan onaylatirsa
    And tedarikci rolune gecilip ihale olusturulursa
    And alici rolune gecilirse
    And ihale listesi ekranina gidilirse
    And bekleyen ihale icin "REDDET" butonuna tiklanirsa
    And red nedeni girilirse
    Then ihale reddedilmeli
    And finansman sisteminde bordro olusturulmali

  # SCN-01-003
  @smoke
  Scenario: SCN-01-003 - Finansman iptali akisi
    Given tam akis tamamlandi ve bordro olustur
    When finansman rolune gecilirse
    And "Teklif Talebi Yonetimi" ekranina gidilirse
    And ilgili bordro satirinda "IPTAL" butonuna tiklanirsa
    Then bordro iptal edilmeli
    And bordro durumu "CANCELLED" veya "Iptal" olmali
