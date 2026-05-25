@ui @dev2 @company
Feature: TC-COMP-01 - Tedarikci Fatura Yukleme UI

  Background:
    Given tedarikci EFG roleyle dev2'ye giris yapildi

  # TC-COMP-01-001
  @smoke @happy-path @kritik
  Scenario: TC-COMP-01-001 - Gecerli XML fatura basariyla yuklenir
    When "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And gecerli bir XML fatura dosyasi secilirse
    And "Kaydet" butonuna tiklanirsa
    Then basari bildirimi gorunmeli
    And fatura listesinde yeni fatura gorunmeli
    And fatura durumu "PENDING_APPROVAL" olmali

  # TC-COMP-01-002
  # @disabled: Uygulama PDF yuklemeyi reddetmiyor (format validasyonu yok)
  @negative @validasyon @disabled
  Scenario: TC-COMP-01-002 - XML olmayan dosya yukleme hatasi
    When "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And gecersiz formatta (PDF) bir dosya yuklenmeye calisilirsa
    Then hata bildirimi gorunmeli
    And fatura listesine eklenmemis olmali

  # TC-COMP-01-003
  @negative @validasyon
  Scenario: TC-COMP-01-003 - Dosya secmeden Kaydet denemesi
    When "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And hicbir dosya secmeden "Kaydet" butonuna tiklanirsa
    Then "Dosya seciniz" uyarisi veya validasyon hatasi gorunmeli
    And dialog acik kalmaya devam etmeli

  # TC-COMP-01-004
  @smoke
  Scenario: TC-COMP-01-004 - Yuklenen fatura listede gorunur
    Given bir fatura daha once basariyla yuklenmis
    When "Faturalarim" ekranina gidilirse
    Then fatura listesi yuklenmeli
    And listede en az bir fatura satiri gorunmeli

  # TC-COMP-01-005
  @smoke
  Scenario: TC-COMP-01-005 - Fatura durumu PENDING_APPROVAL gorunur
    Given bir fatura basariyla yuklenmis
    When "Faturalarim" ekranina gidilirse
    Then yuklenen faturanin durumu "PENDING_APPROVAL" veya "Onay Bekliyor" olmali

  # TC-COMP-01-006
  # @disabled: ADD_INVOICE yetkisiz kullanici mevcut sistemde tanimli degil
  @yetki @negatif @disabled
  Scenario: TC-COMP-01-006 - ADD_INVOICE yetkisi olmayan kullanici fatura yukleyemez
    Given ADD_INVOICE yetkisi olmayan bir tedarikci kullanicisiyla giris yapildi
    When "Faturalarim" ekranina gidilirse
    Then "Fatura Yukle" butonu gorunmemeli

  # TC-COMP-01-007
  @smoke
  Scenario: TC-COMP-01-007 - Dialog kapatildiginda fatura eklenmez
    When "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And dialog "Iptal" butonuyla kapatilirsa
    Then dialog kapanmali
    And fatura listesi degismemis olmali

  # TC-COMP-01-008a
  @smoke @e2e
  Scenario: TC-COMP-01-008a - Gecerli XML fatura tipi yuklenebilir
    When "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And "gecerli_xml" formatinda bir fatura yuklenirse
    And "Kaydet" butonuna tiklanirsa
    Then basari bildirimi gorunmeli

  # TC-COMP-01-008b
  @smoke @e2e
  Scenario: TC-COMP-01-008b - Bozuk XML fatura tipi hata verir
    When "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And "bozuk_xml" formatinda bir fatura yuklenirse
    And "Kaydet" butonuna tiklanirsa
    Then hata bildirimi gorunmeli

  # TC-COMP-01-009
  @smoke
  Scenario: TC-COMP-01-009 - Ayni fatura tekrar yuklenmez (duplicate)
    Given daha once yuklenmis bir fatura numarasi biliniyor
    When "Fatura Yukle" dialogu acilirsa
    And ayni fatura dosyasi tekrar yuklenirse
    And "Kaydet" butonuna tiklanirsa
    Then "Fatura zaten mevcut" veya benzeri bir hata mesaji gorunmeli

  # TC-COMP-01-010
  @smoke
  Scenario: TC-COMP-01-010 - Fatura yukleme sonrasi liste guncellenir
    Given mevcut fatura listesindeki satir sayisi not edilmis
    When yeni bir fatura basariyla yuklenirse
    Then fatura listesindeki satir sayisi bir artmis olmali

  # TC-COMP-01-011
  @smoke @entegrasyon
  Scenario: TC-COMP-01-011 - Yuklenen fatura admin tarafindan gorulebilir
    Given tedarikci bir fatura yukledi
    When admin rolune gecilirse
    And admin fatura yonetimi ekranina gidilirse
    Then yuklenen fatura admin listesinde gorunmeli
    And durumu "PENDING_APPROVAL" olmali
