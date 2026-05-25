@ui @dev2 @admin @price
Feature: TC-PRICE-01-UI - Fiyat Yonetimi UI

  Background:
    Given admin dorukhan roleyle dev2'ye giris yapildi

  # TC-PRICE-01-001
  @smoke @happy-path
  Scenario: TC-PRICE-01-001 - Fiyat yonetimi ekrani acilir ve kriter tablosu gorunur
    When fiyat yonetimi ekranina gidilir
    Then ekranda vaadin-grid gorunmeli

  # TC-PRICE-01-002
  @smoke
  Scenario: TC-PRICE-01-002 - Kriter tablosu baslik sutunlari dogru gorunur
    Given fiyat yonetimi ekranina gidilir
    When fiyat ekrani kriter tablosu goruntulenir
    Then kriter tablosu baslik sutunlari dogru gorunmeli

  # TC-PRICE-01-003
  @smoke
  Scenario: TC-PRICE-01-003 - Gecerli fiyat aralik degerleri ile guncelleme yapilir
    Given fiyat yonetimi ekranina gidilir
    When fiyat aralik degerleri girilirse:
      | minFiyat | maxFiyat |
      | 100      | 5000     |
    And fiyat guncelleme formu kaydedilirse
    Then fiyat guncelleme basariyla tamamlanmali

  # TC-PRICE-01-004
  @negative @validation
  Scenario: TC-PRICE-01-004 - Min fiyat max fiyattan buyuk girildiginde hata gorunur
    Given fiyat yonetimi ekranina gidilir
    When gecersiz fiyat aralik degeri girilirse
    And fiyat guncelleme formu kaydedilirse
    Then fiyat aralik validasyon hatasi gorunmeli

  # TC-PRICE-01-005
  @regression
  Scenario: TC-PRICE-01-005 - Fiyat guncelleme akisi tamamlanir
    Given fiyat yonetimi ekranina gidilir
    When fiyat aralik degerleri girilirse:
      | minFiyat | maxFiyat |
      | 500      | 10000    |
    And fiyat guncelleme formu kaydedilirse
    Then basari bildirimi gorunmeli
    And tum fiyat araliklari listede gorunmeli

  # TC-PRICE-01-006
  @negative @edge-case
  Scenario: TC-PRICE-01-006 - Sifir fiyat araligi girildiginde validasyon calısır
    Given fiyat yonetimi ekranina gidilir
    When fiyat aralik degerleri girilirse:
      | minFiyat | maxFiyat |
      | 0        | 0        |
    And fiyat guncelleme formu kaydedilirse
    Then fiyat aralik validasyon hatasi gorunmeli

  # TC-PRICE-01-007
  @regression
  Scenario: TC-PRICE-01-007 - Birden fazla fiyat aralik satiri listelenir
    Given fiyat yonetimi ekranina gidilir
    Then tum fiyat araliklari listede gorunmeli

  # TC-PRICE-01-008
  @negative
  Scenario: TC-PRICE-01-008 - Bos fiyat alani ile kaydet yapildiginda form bloklanir
    Given fiyat yonetimi ekranina gidilir
    When fiyat alanları bos birakılarak kaydet tiklanirsa
    Then fiyat aralik validasyon hatasi gorunmeli
