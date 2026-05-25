@ui @dev2 @buyer @supplier @e2e @scn09b
Feature: SCN-09B - Baremli Alici E2E Akisi (Kademeli Faiz Hesaplama)

  # SCN-09B-001
  @smoke @happy-path
  Scenario: SCN-09B-001 - Admin baremli ayar yapar, ALBC fatura yukler, faktoring baremli teklif verir, tutar dogrulanir ve admin onaylar
    Given tum rol oturumlari hazirlandir:
      | rol       | kullanici                         |
      | admin     | dorukhan.ersoyleyen@faturalab.com |
      | buyer     | ALBC Marketler Zinciri A.S.       |
      | factoring | Akbank                            |
    When admin rolune gecilirse
    And admin tedarikci yonetimi ekranina gider
    And Akbank tedarikci icin "Baremli Fiyat" ayari aktif edilir
    And barem tablosu asagidaki degerlerle doldurulur:
      | vade_baslangic | vade_bitis | faiz_orani |
      | 1              | 30         | 1.20       |
      | 31             | 60         | 1.50       |
      | 61             | 90         | 1.80       |
    Then barem tablosu basariyla kaydedilmeli
    When alici rolune gecilirse
    And "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And gecerli bir XML fatura dosyasi secilirse
    And "Kaydet" butonuna tiklanirsa
    Then fatura basariyla yuklenmeli
    When admin rolune gecilirse
    And admin fatura yonetimi ekranina gidilirse
    And o fatura icin "ONAYLA" butonuna tiklanirsa
    Then fatura basariyla onaylanmali
    When finansman rolune gecilirse
    And finansman aktif ihaleler listesine gidilirse
    And son aktif ihale icin teklif formu acilirsa
    And baremli teklif formu doldurulur:
      | vade_gun |
      | 45       |
    And teklif "Gonder" butonuna tiklanirsa
    Then teklif basariyla olusturulmali
    And baremli hesaplama ile teklif tutari dogrulanir
    And baremli ve baremsiz teklif tutarlari birbirinden farkli olmali
    And finansman baremli teklifini kabul eder
    When admin rolune gecilirse
    And admin bordro yonetimi ekranina gidilirse
    And olusturulan bordro icin "ONAYLA" butonuna tiklanirsa
    Then admin tarafindan bordro onay adimi tamamlandi

  # SCN-09B-002
  @entegrasyon
  Scenario: SCN-09B-002 - Farkli vadeler icin barem tierleri dogru uygulanir
    Given tum rol oturumlari hazirlandir:
      | rol       | kullanici                         |
      | admin     | dorukhan.ersoyleyen@faturalab.com |
      | factoring | Akbank                            |
    When admin rolune gecilirse
    And admin tedarikci yonetimi ekranina gider
    And Akbank tedarikci icin "Baremli Fiyat" ayari aktif edilir
    And barem tablosu asagidaki degerlerle doldurulur:
      | vade_baslangic | vade_bitis | faiz_orani |
      | 1              | 30         | 1.00       |
      | 31             | 60         | 1.75       |
      | 61             | 90         | 2.50       |
    Then barem tablosu basariyla kaydedilmeli
    And baremli ve baremsiz teklif tutarlari birbirinden farkli olmali
