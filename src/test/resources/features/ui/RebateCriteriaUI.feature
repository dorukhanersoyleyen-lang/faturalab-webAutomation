@ui @dev2 @admin @rebate @regression
Feature: TC-REBATE-01-UI - Rebate Kriter Yonetimi UI

  Background:
    Given admin dorukhan roleyle dev2'ye giris yapildi

  # TC-REBATE-01-001
  @smoke @happy-path @critical
  Scenario: TC-REBATE-01-001 - Rebate kriterleri ekrani acilir ve grid gorunur
    When rebate kriterleri ekranina gidilir
    Then ekranda vaadin-grid gorunmeli

  # TC-REBATE-01-002
  @critical @regression
  Scenario: TC-REBATE-01-002 - rebateAmount sifir iken validasyon hatasi gozlemlenir
    Given rebate kriterleri ekranina gidilir
    When rebate miktari sifir girilirse
    And rebate formu kaydedilirse
    Then rebate dogrulama hatasi gorunmeli

  # TC-REBATE-01-003
  @smoke
  Scenario: TC-REBATE-01-003 - Kriter tablosuna yeni rebate satiri eklenir
    Given rebate kriterleri ekranina gidilir
    When rebate tablosuna yeni bir satir eklenir
    Then rebate listesi basariyla guncellenmeli

  # TC-REBATE-01-004
  @regression
  Scenario: TC-REBATE-01-004 - Mevcut rebate satiri duzenlenir
    Given rebate kriterleri ekranina gidilir
    When mevcut rebate satiri duzenlenirse
    And rebate formu kaydedilirse
    Then degisiklik rebate listesine yansiyor

  # TC-REBATE-01-005
  @regression
  Scenario: TC-REBATE-01-005 - Rebate satiri silinir
    Given rebate kriterleri ekranina gidilir
    When rebate satiri silinirse
    Then rebate satiri listeden kalkmali

  # TC-REBATE-01-006
  @negative @validation
  Scenario: TC-REBATE-01-006 - Hatali rebate orani girildiginde hata mesaji gorunur
    Given rebate kriterleri ekranina gidilir
    When hatali rebate orani girilirse
    And rebate formu kaydedilirse
    Then rebate hata mesaji gorunmeli

  # TC-REBATE-01-007
  @smoke
  Scenario: TC-REBATE-01-007 - Rebate orani hesaplama dogrulamasi
    Given rebate kriterleri ekranina gidilir
    When rebate orani "2.5" girilirse
    Then rebate hesaplama dogrulama yapilir

  # TC-REBATE-01-008
  @regression
  Scenario: TC-REBATE-01-008 - Kaydet sonrasi rebate listesi guncellenir
    Given rebate kriterleri ekranina gidilir
    When rebate tablosuna yeni bir satir eklenir
    And rebate formu kaydedilirse
    Then basari bildirimi gorunmeli
    And rebate listesi basariyla guncellenmeli

  # TC-REBATE-01-009
  @negative @validation @critical
  Scenario: TC-REBATE-01-009 - Negatif rebate orani kabul edilmez
    Given rebate kriterleri ekranina gidilir
    When rebate orani "-1" girilirse
    And rebate formu kaydedilirse
    Then rebate dogrulama hatasi gorunmeli

  # TC-REBATE-01-010
  @edge-case
  Scenario: TC-REBATE-01-010 - Cok yuksek rebate orani girilir (edge case)
    Given rebate kriterleri ekranina gidilir
    When rebate orani "100" girilirse
    And rebate formu kaydedilirse
    Then rebate hata mesaji gorunmeli

  # TC-REBATE-01-011
  @regression
  Scenario: TC-REBATE-01-011 - Bos kriter tablosunda liste gorunumu kontrol edilir
    Given rebate kriterleri ekranina gidilir
    Then rebate kriter tablosu bos veya dolu olarak gorunmeli
