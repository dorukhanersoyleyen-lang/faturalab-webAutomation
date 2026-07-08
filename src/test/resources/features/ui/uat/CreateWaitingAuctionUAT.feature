# CREATE-WAITING-AUCTION: Bağımsız WAITING durumunda auction oluşturma
# Reproduce testleri için tekrar koşulabilir yardımcı senaryo. TZF-001'in
# SADECE ilk yarısını kapsar: alıcı Excel ile fatura yükler → tedarikçi
# "TEKLİF AL" + modal onayı ile auction WAITING durumunda oluşturulur.
#
# ⚠️ Kasıtlı olarak "Kabul / İptal" ve sonrası adımlar ÇALIŞTIRILMAZ —
# auction WAITING kalmalı ki /offer/ deep-link ile reproduce testi yapılabilsin.
#
# Test verisi: her koşuda TzfInvoiceExcelGenerator ile benzersiz fatura numaralı
# .xls üretilir. Hedef kullanıcılar config'ten okunur (tzf.* anahtarları) —
# TZF-001 ile aynı tedarikçi/alıcı çifti kullanılır.
Feature: Bağımsız WAITING Auction Oluşturma
  Reproduce testi için alıcı Excel ile fatura yükler, tedarikçi bu fatura için
  teklif alır ve modalı onaylar; auction WAITING durumunda açık bırakılır.

  @ui @uat @regression @create-waiting-auction
  Scenario: WAITING-001 - Alıcı Excel fatura yükleme ve tedarikçi teklif alma ile WAITING auction oluşturma
    Given TZF senaryosu için 1 adet E-Fatura içeren Excel hazırlanır
    When admin TZF alıcı kullanıcısına geçiş yapar
    And alıcı ekranında hazırlanan Excel ile faturalar yüklenir
    Then faturaların başarıyla yüklendiği doğrulanır
    When admin TZF tedarikçi kullanıcısına geçiş yapar
    Then yüklenen faturalar tedarikçi listesinde görünmeli
    When yüklenen faturalardan biri için teklif alınır
    And teklif modalı onaylanır ve işlemdekiler sayfasına yönlenilir
    Then auction WAITING durumunda oluşturulduğu için işlem burada durur
