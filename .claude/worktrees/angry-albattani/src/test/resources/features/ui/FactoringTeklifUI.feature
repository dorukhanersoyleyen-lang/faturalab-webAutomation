@ui @dev2 @factoring
Feature: TC-FACT-01 - Finansman Teklif ve Bordro Yonetimi UI

  Background:
    Given finansman OPR rolüyle dev2'ye giris yapildi

  # TC-FACT-01-001
  @smoke @happy-path @kritik
  Scenario: TC-FACT-01-001 - Finansman bekleyen bordroyu onaylar
    Given sistemde onay bekleyen bir bordro mevcut
    When "Teklif Talebi Yonetimi" ekranina gidilirse
    And "Gunluk Teklif Talebi" sekmesine gecilirse
    And ilgili bordro satirinda "ONAYLA" butonuna tiklanirsa
    Then basari bildirimi gorunmeli
    And bordro durumu "APPROVED" veya "Onaylandi" olmali

  # TC-FACT-01-002
  @smoke
  Scenario: TC-FACT-01-002 - Finansman bordroyu iptal eder
    Given sistemde aktif bir bordro mevcut
    When "Teklif Talebi Yonetimi" ekranina gidilirse
    And "Gunluk Teklif Talebi" sekmesine gecilirse
    And ilgili bordro satirinda "IPTAL" butonuna tiklanirsa
    Then basari bildirimi gorunmeli
    And bordro durumu "CANCELLED" veya "Iptal" olmali

  # TC-FACT-01-003
  @smoke
  Scenario: TC-FACT-01-003 - Finansman bordro detayini goruntular (GOZAT)
    Given sistemde bir bordro mevcut
    When "Teklif Talebi Yonetimi" ekranina gidilirse
    And "Gunluk Teklif Talebi" sekmesine gecilirse
    And ilgili bordro satirinda "GOZAT" butonuna tiklanirsa
    Then bordro detay sayfasi veya dialogu acilmali
    And bordro bilgileri dogru gosterilmeli

  # TC-FACT-01-004
  @smoke
  Scenario: TC-FACT-01-004 - Teklif Talebi Yonetimi grid yuklenir
    When "Teklif Talebi Yonetimi" ekranina gidilirse
    Then ekranda vaadin-grid gorunmeli
    And "Gunluk Teklif Talebi", "Iptal Talebi" tablari gorunmeli

  # TC-FACT-01-005
  @smoke
  Scenario: TC-FACT-01-005 - Iptal Talebi sekmesine gecis
    When "Teklif Talebi Yonetimi" ekranina gidilirse
    And "Iptal Talebi" sekmesine tiklanirsa
    Then iptal talepleri listesi yuklenmeli

  # TC-FACT-01-006
  @negative
  Scenario: TC-FACT-01-006 - Onaylanmis bordroyu tekrar onaylama denemesi
    Given daha once onaylanmis bir bordro mevcut
    When "Teklif Talebi Yonetimi" ekranina gidilirse
    And onaylanmis bordroda aksiyon butonu aranirsa
    Then "ONAYLA" butonu gorunmemeli veya pasif olmali

  # TC-FACT-01-007
  @entegrasyon @kritik
  Scenario: TC-FACT-01-007 - Onaylanan bordro tedarikcie yansiyor
    Given finansman bir bordroyu onayladi
    And tedarikci EFG rolune gecildi
    When tedarikci bordro veya fatura takip ekranina gidilirse
    Then ilgili fatura veya bordroda onaylandi bilgisi gorunmeli

  # TC-FACT-01-008
  @entegrasyon
  Scenario: TC-FACT-01-008 - Bordro satir bilgileri dogru gorunur
    When "Teklif Talebi Yonetimi" ekranina gidilirse
    And "Gunluk Teklif Talebi" sekmesindeki ilk bordro satiri incelenirse
    Then "Bordro No", "Ticari Isletme", "Alici", "Finansal Kurum" sutunlari dolu olmali
