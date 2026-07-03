# TZF-001: TZF İşlemi Uçtan Uca Akışı
# Alıcı adına Excel ile fatura yükleme → tedarikçi teklif alma ve kabul →
# admin Günlük İşlemler raporunda bordro doğrulaması.
#
# Test verisi: her koşuda TzfInvoiceExcelGenerator ile benzersiz fatura numaralı
# .xls üretilir; tüm doğrulamalar bu numaralar üzerinden tam eşleşmeyle yapılır.
# Hedef kullanıcılar config'ten okunur (tzf.* anahtarları).
Feature: TZF İşlemi UAT
  Alıcı, tedarikçi adına Excel ile fatura yükleyebilmeli; tedarikçi bu faturalar
  için teklif alıp kabul edebilmeli ve işlem admin günlük işlemlerde görünmeli.

  @ui @uat @tzf @e2e @happy-path @tzf-001
  Scenario: TZF-001 - Alıcı Excel fatura yükleme, tedarikçi teklif kabul ve günlük işlem doğrulama
    Given TZF senaryosu için 3 adet E-Fatura içeren Excel hazırlanır
    When admin TZF alıcı kullanıcısına geçiş yapar
    And alıcı ekranında hazırlanan Excel ile faturalar yüklenir
    Then faturaların başarıyla yüklendiği doğrulanır
    When admin TZF tedarikçi kullanıcısına geçiş yapar
    Then yüklenen faturalar tedarikçi listesinde görünmeli
    When yüklenen faturalardan biri için teklif alınır
    And teklif modalı onaylanır ve işlemdekiler sayfasına yönlenilir
    And işlemdekiler sayfasında ilk teklif kabul edilir ve onaylanır
    Then bordro numarası yakalanır
    When admin olarak günlük işlemler raporuna gidilir
    Then oluşturulan işlem günlük işlemler listesinde görünmeli
