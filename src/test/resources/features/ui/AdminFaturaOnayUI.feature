@ui @dev2 @admin
Feature: TC-ADMIN-02 - Admin Fatura Onay UI

  Background:
    Given admin dorukhan roleyle dev2'ye giris yapildi

  # TC-ADMIN-02-001
  @smoke @happy-path @kritik
  Scenario: TC-ADMIN-02-001 - Admin PENDING_APPROVAL faturay onaylar
    Given sistemde "PENDING_APPROVAL" durumunda bir fatura mevcut
    When admin fatura yonetimi ekranina gidilirse
    And o fatura icin "ONAYLA" butonuna tiklanirsa
    Then basari bildirimi gorunmeli
    And fatura durumu "APPROVED" olmali
    And fatura ihale secim listesinde gorunmeli

  # TC-ADMIN-02-002
  @disabled
  Scenario: TC-ADMIN-02-002 - DEVRE DISI - Admin fatura engelleme
    Given bu senaryo devre disi

  # TC-ADMIN-02-003
  @disabled
  Scenario: TC-ADMIN-02-003 - DEVRE DISI - BLOCKED fatura ihalede
    Given bu senaryo devre disi

  # TC-ADMIN-02-004
  @smoke
  Scenario: TC-ADMIN-02-004 - Fatura numarasiyla admin arama
    Given sistemde bilinen bir fatura numarasi mevcut
    When admin fatura yonetimi ekranina gidilirse
    And arama alanina bilinen fatura numarasi girilirse
    Then ilgili fatura listede gorunmeli
    And fatura bilgileri dogru gosterilmeli

  # TC-ADMIN-02-005
  @negative
  Scenario: TC-ADMIN-02-005 - Yanlis fatura numarasiyla admin arama
    When admin fatura yonetimi ekranina gidilirse
    And arama alanina "OLMAYAN-FATURA-99999" girilirse
    Then "Fatura bulunamadi" uyarisi veya bos liste gorunmeli

  # TC-ADMIN-02-006
  @negative @validasyon
  Scenario: TC-ADMIN-02-006 - Bos fatura numarasiyla arama denemesi
    When admin fatura yonetimi ekranina gidilirse
    And arama alani bos birakilip arama baslatilirsa
    Then validasyon uyarisi gorunmeli veya tum faturalar listelenmeli

  # TC-ADMIN-02-007
  @smoke @entegrasyon @kritik
  Scenario: TC-ADMIN-02-007 - Onaylanan fatura ihale secim listesinde gorunur
    Given bir fatura "PENDING_APPROVAL" durumundayken admin tarafindan onaylanmis
      | invoiceNo       |
      | ADMIN-APPR-001  |
    And tedarikci EFG rolune gecildi
    When tedarikci ihale olusturma ekraninda fatura secim listesini acarsa
    Then onaylanan fatura listede "APPROVED" olarak gorunmeli
    And fatura secilebilir durumda olmali
