# FL-014: Excel ile Yükleme (Critical)
# FL-016: CSV ile Yükleme (Critical)
# FL-017: Manuel Yükleme
Feature: Tedarikçi Fatura Yükleme Yöntemleri UAT
  Tedarikçi kullanıcısı Excel, CSV ve manuel yöntemlerle fatura yükleyebilmeli.

  Background:
    Given tedarikci olarak giriş yapılır

  @ui @uat @smoke @happy-path @kritik @fl-014
  Scenario: FL-014 - Tedarikçi Excel ile fatura yükleyebilmeli
    When fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    And "Excel ile Yükle" sekmesine tıklanır
    And şablon indir butonu görünür ve tıklanabilir durumdadır
    And hazırlanan Excel dosyası seçilir
    And yükle butonuna tıklanır
    Then Excel ile fatura yükleme akışı başarıyla tamamlanmalı
    And yüklenen faturalar listede görünmeli

  @ui @uat @smoke @happy-path @kritik @fl-014
  Scenario: FL-014b - Excel yükleme dialogu açılabilmeli
    When fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    Then fatura yükleme dialogu görünmeli
    And "Excel ile Yükle" sekmesi seçilebilir olmalı
    And Excel sekmesi içeriği görüntülenmeli

  @ui @uat @smoke @happy-path @kritik @fl-016
  Scenario: FL-016 - Tedarikçi CSV ile fatura yükleyebilmeli
    When fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    And "CSV ile Yükle" sekmesine tıklanır
    Then CSV ile yükle sekmesi içeriği görünmeli
    And CSV şablon indir butonu görünmeli
    And CSV dosya seçme alanı aktif olmalı

  @ui @uat @smoke @happy-path @fl-017
  Scenario: FL-017 - Tedarikçi manuel fatura yükleyebilmeli
    When fatura yükle ekranına gidilir
    And fatura yükleme dialogu açılır
    And "Manuel" sekmesine tıklanır
    And alıcı listesinden bir alıcı seçilir
    And fatura tutarı girilir
    And vade tarihi girilir
    And kaydet butonuna tıklanır
    Then manuel fatura başarıyla yüklenmiş olmalı
    And fatura listesinde yeni fatura satırı görünmeli
