# Teklif Talebi İptal Akışı — UAT regresyon senaryosu
#
# Kök neden zinciri (kaynak: #5649 canlı reprosu, 19.08.2026, taze/organik veriyle doğrulandı):
#   - "Teklif Talebini İptal Et" (AuctionModel.rejectAuctionWithSession) invoice.remainingAmount'ı
#     auctionInvoiceGroup.getRequestedAmount() ile geri ekler.
#   - AuctionInvoiceGroupModel.getInvoiceTotalUsedAmountWithSession() içindeki
#     switch(auction.getStatus()) REJECTED durumunu 0 katkı ile sayar (Vaadin 24 migration'dan
#     beri mevcut, regresyon DEĞİL) — bu yüzden basit tek-fatura/tek-döngü senaryosunda
#     (yükle → teklif al → iptal → tekrar teklif al) "Temlik tutarı, kalan tutardan yüksek
#     olamaz." (AuctionInvoiceGroupModel.1) hatası OLUŞMAMALIDIR.
#   - Bu senaryo o pozitif davranışı kalıcı bir regresyon testine çevirir.
#
# ⚠️ Bilinen açık defect #5905 ("İptal Edilince Faturanın auctionCount Alanı Düşmüyor") bu
# senaryonun kapsamı DIŞINDA bırakılmıştır — remainingAmount doğrulanır, auctionCount SADECE
# loglanır/raporlanır (#5905 kapanınca sıkı assert eklenebilir, bkz. InvoiceDbAssertions).
#
# Test verisi: her koşuda TzfInvoiceExcelGenerator ile benzersiz fatura numaralı .xls üretilir;
# tüm doğrulamalar (grid + DB) bu numara üzerinden tam eşleşmeyle yapılır. Hedef kullanıcılar
# config'ten okunur (tzf.* anahtarları — TZF-001 ile aynı izole otomasyon firması/alıcı).
Feature: Teklif Talebi İptal Akışı UAT
  Tedarikçi oluşturduğu bir teklif talebini (bordro) iptal edebilmeli; iptal sonrası faturanın
  rezervasyonu (kalan tutar) DB'de doğru şekilde geri alınmalı ve aynı fatura için tekrar
  teklif alınabilmeli (Temlik tutarı hatası oluşmamalı).

  @ui @uat @teklif-iptal
  Scenario: Teklif talebi iptali sonrası rezervasyon geri alınır ve tekrar teklif alınabilir
    Given TZF senaryosu için 1 adet E-Fatura içeren Excel hazırlanır
    When admin TZF alıcı kullanıcısına geçiş yapar
    And alıcı ekranında hazırlanan Excel ile faturalar yüklenir
    Then faturaların başarıyla yüklendiği doğrulanır
    When admin TZF tedarikçi kullanıcısına geçiş yapar
    Then yüklenen faturalar tedarikçi listesinde görünmeli
    When yüklenen faturalardan biri için teklif alınır
    And teklif modalı onaylanır ve işlemdekiler sayfasına yönlenilir
    Then oluşan teklif talebi (bordro) işlemdekiler sayfasında iptal edilir
    And faturanın kalan tutarı DB'de orijinal ödenebilir tutara geri döndüğü doğrulanır
    When aynı fatura için Yüklenmişler ekranından tekrar teklif alınır
    Then tekrar teklif alma sırasında Temlik tutarı hatası oluşmadığı doğrulanır
