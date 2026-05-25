# FL-009: Hızlı Teklif Al Modal (Alıcı)
# FL-010: İhale Başlatma (Critical)
# FL-011: Alıcı Profili Oluştur
Feature: Alıcı İhale Yönetimi UAT
  Alıcı kullanıcısı ihale modalı, ihale başlatma ve profil oluşturma işlemlerini gerçekleştirebilmeli.

  Background:
    Given alici olarak giriş yapılır

  @ui @uat @smoke @happy-path @fl-009
  Scenario: FL-009 - Alıcı hızlı teklif al modalını görebilmeli
    When ihaleler ekranına gidilir
    And ihale listesinde bekleyen bir ihale satırı görüntülenir
    And ilgili ihale satırında "Hızlı Teklif Al" butonuna tıklanır
    Then hızlı teklif al modalı açılmalı
    And modal içinde ihale detay bilgileri görünmeli

  @ui @uat @smoke @happy-path @kritik @fl-010
  Scenario: FL-010 - Alıcı ihale başlatabilmeli
    When ihaleler ekranına gidilir
    And "İhale Başlat" butonuna tıklanır
    And ihale parametreleri girilir
    And tedarikçi seçilir
    And "Başlat" butonuna tıklanır
    Then ihale başarıyla başlatılmış olmalı
    And ihale durumu "WAITING" veya "Bekliyor" olmalı
    And ihale listesinde yeni ihale satırı görünmeli

  @ui @uat @smoke @happy-path @kritik @fl-010
  Scenario: FL-010b - İhale listesi ekranı erişilebilir olmalı
    When ihaleler ekranına gidilir
    Then ihale listesi grid'i görünmeli
    And grid'de ihale satırları listelenmiş olmalı
    And grid sütun başlıkları doğru görüntülenmeli

  @ui @uat @smoke @happy-path @fl-011
  Scenario: FL-011 - Alıcı profili oluşturulabilmeli
    When profil yönetimi ekranına gidilir
    And "Profil Oluştur" butonuna tıklanır
    And profil adı girilir
    And profil bilgileri doldurulur
    And kaydet butonuna tıklanır
    Then profil başarıyla oluşturulmuş olmalı
    And yeni profil listede görünmeli
