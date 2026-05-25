# FL-003: Firma Düzenleme
# FL-004: Kullanıcı Düzenleme
Feature: Admin Firma ve Kullanıcı Yönetimi UAT
  Admin firmaları ve kullanıcıları düzenleyebilmeli.

  Background:
    Given admin olarak giriş yapılır

  @ui @uat @smoke @happy-path @fl-003
  Scenario: FL-003 - Admin firma bilgilerini düzenleyebilmeli
    When admin firma yönetimi ekranına gidilir
    And firma listesi görüntülenir
    And ilk firma için düzenle butonuna tıklanır
    And firma bilgileri güncellenir
    And kaydet butonuna tıklanır
    Then firma bilgileri başarıyla güncellenmiş olmalı

  @ui @uat @smoke @happy-path @fl-004
  Scenario: FL-004 - Admin kullanıcı bilgilerini düzenleyebilmeli
    When admin kullanıcı yönetimi ekranına gidilir
    And kullanıcı listesi görüntülenir
    And ilk kullanıcı satırına tıklanır
    And kullanıcı düzenle işlemi yapılır
    And kaydet butonuna tıklanır
    Then kullanıcı bilgileri başarıyla güncellenmiş olmalı
