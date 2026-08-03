# DFP-001: DFP (MARKET_PLACE / E-İskonto) İşlemi Uçtan Uca
# Tedarikçi: İşbank Tedarikçi 1 (MP) — company 575, VKN 2490025653.
#   - İşbank (68) MP limiti: 50.000 TL (kanıt: 96 başarılı offered=true, son 11.03.2026)
#   - ALBC ilişkisi DELETED → ALBC lehtarlı fatura otomatik MARKET_PLACE olur
#     (AuctionModel.java:3488-3495: aktif tedarikçi değilse → MARKET_PLACE)
# Tutar politikası: HER işlem 1,00 TL (limit tüketimini minimumda tutmak için).
# Kapsam: fatura yükle → Teklif Al → teklif talebinin oluşması (İşlemdekiler).
#   İşbank teklifinin dönmesi (offered=true) banka/kBroker zamanlamasına bağlı
#   olduğundan senaryo talep-oluşumunda biter; teklif dönüşü koşum sonrası DB'den
#   doğrulanır (auction.producttype='MARKET_PLACE' + auctionoffer.offered).
Feature: DFP İşlemi UAT
  Tedarikçi, aktif tedarikçisi olmadığı bir lehtara kestiği faturayla
  DFP (MARKET_PLACE) teklif talebi başlatabilmeli.

  @ui @uat @regression @dfp @dfp-001
  Scenario: DFP-001 - İşbank MP tedarikçisi 1 TL faturayla DFP teklif talebi başlatabilmeli
    Given DFP için 1 TL tutarında dummy imzalı XML fatura hazırlanır
    Given dfp tedarikcisi olarak giriş yapılır
    And DFP dosyası yüklenir ve kaydedilir
    Then yüklenen faturalar tedarikçi listesinde görünmeli
    When yüklenen faturalardan biri için teklif alınır
    And teklif modalı onaylanır ve işlemdekiler sayfasına yönlenilir
    Then DFP teklif talebi işlemdekiler listesinde görünmeli
