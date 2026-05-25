# FL-012: Cut-off Süresi Ayarlama
# "Limit ve Fiyat Yönetimi" ekranında yapılan iş: Admin, seçilen finansman kurumu (banka) için
# FK cut-off saatini (işlem kesim saati) girip kaydeder — limit tablolarını doldurmak değil.
Feature: Admin FK Ayarları UAT
  Admin finansman kurumu cut-off saatini ayarlayabilmeli.

  Background:
    Given admin olarak giriş yapılır

  @ui @uat @smoke @happy-path @fl-012
  Scenario: FL-012 - Admin FK cut-off saatini ayarlayabilmeli
    When admin FK ayarları ekranına gidilir
    And banka seçilir
    And cut-off saati "16" olarak girilir
    And cut-off ayarları kaydedilir
    Then cut-off ayarı başarıyla kaydedilmiş olmalı

  @ui @uat @smoke @happy-path @fl-012
  Scenario: FL-012b - FK Ayarları ekranı erişilebilir olmalı
    When admin FK ayarları ekranına gidilir
    Then FK ayarları ekranı görüntülenmeli
