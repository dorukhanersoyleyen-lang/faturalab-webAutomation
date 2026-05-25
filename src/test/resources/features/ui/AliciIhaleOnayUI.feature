@ui @dev2 @buyer
Feature: TC-BUYER-02 - Alici Ihale Onay/Ret UI

  Background:
    Given alici ALBC roleyle dev2'ye giris yapildi

  # TC-BUYER-02-001
  @smoke @happy-path @kritik
  Scenario: TC-BUYER-02-001 - Alici bekleyen ihaleyi onaylar
    Given sistemde aliciya yonlendirilmis bekleyen bir ihale mevcut
    When ihale listesi ekranina gidilirse
    And ilgili ihale satirinda "ONAYLA" butonuna tiklanirsa
    Then basari bildirimi gorunmeli
    And ihale durumu "APPROVED" olmali
    And finansman sisteminde bordro olusturulmali

  # TC-BUYER-02-002
  @smoke
  Scenario: TC-BUYER-02-002 - Alici ihaleyi reddeder
    Given sistemde bekleyen bir ihale mevcut
    When ihale listesi ekranina gidilirse
    And ilgili ihale satirinda "REDDET" butonuna tiklanirsa
    And red nedeni olarak "Fiyat uygun degil" girilirse
    Then basari bildirimi gorunmeli
    And ihale durumu "REJECTED" veya "Reddedildi" olmali

  # TC-BUYER-02-003
  @smoke
  Scenario: TC-BUYER-02-003 - Alici ihale detayini goruntular
    Given sistemde bir ihale mevcut
    When ihale listesi ekranina gidilirse
    And ihale detay butonuna tiklanirsa
    Then ihale detaylari gorunmeli
    And fatura bilgileri, tutar, tedarikci adi dogru gosterilmeli

  # TC-BUYER-02-004
  @smoke
  Scenario: TC-BUYER-02-004 - Ihale listesi ekrani yuklenir
    When ihale listesi ekranina gidilirse
    Then ekranda ihale grid'i gorunmeli
    And grid sutunlari dogru goruntulenmeli

  # TC-BUYER-02-005
  @negative @validasyon
  Scenario: TC-BUYER-02-005 - Red nedeni girilmeden ihale reddedilemez
    Given sistemde bekleyen bir ihale mevcut
    When ihale listesi ekranina gidilirse
    And ilgili ihale satirinda "REDDET" butonuna tiklanirsa
    And red nedeni alani bos birakilirsa
    And "Gonder" butonuna tiklanirsa
    Then "Red nedeni zorunludur" validasyon uyarisi gorunmeli
    And ihale hala reddedilmemis olmali

  # TC-BUYER-02-006
  @negative
  Scenario: TC-BUYER-02-006 - Zaten onaylanmis ihale tekrar onaylanamaz
    Given daha once onaylanmis bir ihale mevcut
    When ihale listesi ekranina gidilirse
    And onaylanmis ihale satiri incelenirse
    Then "ONAYLA" butonu gorunmemeli veya pasif olmali

  # TC-BUYER-02-007
  @entegrasyon @kritik
  Scenario: TC-BUYER-02-007 - Alici onayi sonrasi finansman bordro gorur
    Given alici bir ihaleyi onayladi
    And finansman OPR rolune gecildi
    When "Teklif Talebi Yonetimi" ekranina gidilirse
    Then yeni bir bordro satiri listede gorunmeli
    And bordro durumu "PENDING_APPROVAL" veya "Onay Bekliyor" olmali
