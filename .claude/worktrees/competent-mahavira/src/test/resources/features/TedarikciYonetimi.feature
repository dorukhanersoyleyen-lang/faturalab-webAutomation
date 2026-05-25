# language: en
@web @tedarikci @ui
Feature: Tedarikçi Yönetimi
  Bir yönetici olarak, tedarikçileri ekleyebilmeli, güncelleyebilmeli ve ürün tipi atayabilmeliyim

  Background:
    Given kullanıcı ana sayfaya gider
    And kullanıcı geçerli kimlik bilgileri ile giriş yapar
    And kullanıcı dashboard sayfasında olduğunu doğrular

  @tedarikci-ekleme @pozitif @urun-tipi
  Scenario: Ürün Tipi ile Tedarikçi Ekleme
    Given kullanıcı tedarikçi yönetimi sayfasına gider
    And kullanıcı "Yeni Tedarikçi Ekle" butonuna tıklar
    When kullanıcı tedarikçi bilgilerini girer:
      | Alan          | Değer                        |
      | Firma Adı     | QA-TEST Tedarikçi Şirketi 001 |
      | VKN           | 12345678901                  |
      | Kullanıcı Adı | QA-TEST Admin User           |
      | E-posta       | qa-test-001@faturalab.comtest |
    And kullanıcı ürün tipini "Tedarikçi Finansmanı" olarak seçer
    And kullanıcı "Kaydet" butonuna tıklar
    Then tedarikçi başarıyla kaydedilmelidir
    And tedarikçi listesinde "QA-TEST Tedarikçi Şirketi 001" görünmelidir
    And tedarikçinin ürün tipi "Tedarikçi Finansmanı" olarak görünmelidir

  @tedarikci-ekleme @pozitif @urun-tipi-yok
  Scenario: Ürün Tipi Olmadan Tedarikçi Ekleme
    Given kullanıcı tedarikçi yönetimi sayfasına gider
    And kullanıcı "Yeni Tedarikçi Ekle" butonuna tıklar
    When kullanıcı tedarikçi bilgilerini girer:
      | Alan          | Değer                        |
      | Firma Adı     | QA-TEST Tedarikçi Şirketi 002 |
      | VKN           | 98765432109                  |
      | Kullanıcı Adı | QA-TEST Admin User 2         |
      | E-posta       | qa-test-002@faturalab.comtest |
    And kullanıcı ürün tipini seçmez
    And kullanıcı "Kaydet" butonuna tıklar
    Then tedarikçi başarıyla kaydedilmelidir
    And tedarikçi listesinde "QA-TEST Tedarikçi Şirketi 002" görünmelidir
    And tedarikçinin ürün tipi boş olarak görünmelidir

  @tedarikci-guncelleme @pozitif @urun-tipi-ekleme
  Scenario: Mevcut Tedarikçinin Ürün Tipini Güncelleme
    Given "QA-TEST Tedarikçi Şirketi 002" adlı tedarikçi sistemde mevcut
    And bu tedarikçinin ürün tipi boş
    When kullanıcı tedarikçi yönetimi sayfasına gider
    And kullanıcı "QA-TEST Tedarikçi Şirketi 002" tedarikçisini bulur
    And kullanıcı tedarikçi üzerinde "Düzenle" butonuna tıklar
    And kullanıcı ürün tipini "Tedarikçi Finansmanı" olarak günceller
    And kullanıcı "Kaydet" butonuna tıklar
    Then tedarikçi başarıyla güncellenmelidir
    And tedarikçi listesinde güncellenen bilgiler görünmelidir
    And tedarikçinin ürün tipi "Tedarikçi Finansmanı" olarak görünmelidir

  @tedarikci-arama @pozitif
  Scenario: Tedarikçi Arama ve Filtreleme
    Given sistemde birden fazla tedarikçi mevcut
    When kullanıcı tedarikçi yönetimi sayfasına gider
    And kullanıcı arama kutusuna "QA-TEST" yazar
    Then sadece "QA-TEST" içeren tedarikçiler listelenmelidir
    And arama sonuçları doğru şekilde filtrelenmelidir

  @tedarikci-validasyon @negatif
  Scenario: Geçersiz Verilerle Tedarikçi Ekleme - Boş Firma Adı
    Given kullanıcı tedarikçi yönetimi sayfasına gider
    And kullanıcı "Yeni Tedarikçi Ekle" butonuna tıklar
    When kullanıcı geçersiz tedarikçi bilgilerini girer:
      | Alan          | Değer      | Beklenen Hata                    |
      | Firma Adı     | <boş>      | Firma adı zorunludur             |
    And kullanıcı "Kaydet" butonuna tıklar
    Then uygun hata mesajları görünmelidir
    And tedarikçi kaydedilmemelidir

  @tedarikci-validasyon @negatif
  Scenario: Geçersiz Verilerle Tedarikçi Ekleme - Geçersiz VKN
    Given kullanıcı tedarikçi yönetimi sayfasına gider
    And kullanıcı "Yeni Tedarikçi Ekle" butonuna tıklar
    When kullanıcı geçersiz tedarikçi bilgilerini girer:
      | Alan          | Değer      | Beklenen Hata                    |
      | VKN           | 123        | Geçersiz VKN formatı             |
    And kullanıcı "Kaydet" butonuna tıklar
    Then uygun hata mesajları görünmelidir
    And tedarikçi kaydedilmemelidir

  @tedarikci-validasyon @negatif
  Scenario: Geçersiz Verilerle Tedarikçi Ekleme - Geçersiz E-posta
    Given kullanıcı tedarikçi yönetimi sayfasına gider
    And kullanıcı "Yeni Tedarikçi Ekle" butonuna tıklar
    When kullanıcı geçersiz tedarikçi bilgilerini girer:
      | Alan          | Değer      | Beklenen Hata                    |
      | E-posta       | geçersiz   | Geçersiz e-posta formatı         |
    And kullanıcı "Kaydet" butonuna tıklar
    Then uygun hata mesajları görünmelidir
    And tedarikçi kaydedilmemelidir

  @tedarikci-silme @pozitif
  Scenario: Tedarikçi Silme
    Given "Test Silinecek Tedarikçi" adlı tedarikçi sistemde mevcut
    When kullanıcı tedarikçi yönetimi sayfasına gider
    And kullanıcı "Test Silinecek Tedarikçi" tedarikçisini bulur
    And kullanıcı tedarikçi üzerinde "Sil" butonuna tıklar
    And kullanıcı silme işlemini onaylar
    Then tedarikçi başarıyla silinmelidir
    And tedarikçi listesinde artık görünmemelidir