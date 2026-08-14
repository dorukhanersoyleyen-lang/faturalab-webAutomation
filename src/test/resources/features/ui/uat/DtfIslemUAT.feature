# DTF (Dikey Ticaret Finansmanı) İşlemi UAT — izole otomasyon zinciri.
#
# Zincir (bkz. .claude memory'de project_dtf_feature_overview.md, v2_QA reposu):
#   Ana Firma (buyer.id=145 "Test Otomasyon Sadece Alıcı", TZF ile AYNI buyer, additif kullanım)
#     └── Ara Tedarikçi (company.id=998 "Test Otomasyon Sadece Tedarikçi", isbuyer=true,
#         kendi bare buyer.id=151'i var — requireddtf=true, dtfrate=10)
#           └── Alt Tedarikçiler (company 401 "KT Test Tedarikçi", company 243 "Baran Madencilik A.Ş.")
#
# ⚠️ Kaynak kod kanıtı (origin/vaadin-24, READ-ONLY incelendi):
#   - `CompanyAddEditAuctionDialog`: supplier.requireddtf=true + tarih aralığı içindeyse normal
#     "Başlat" DEVRE DIŞI kalır, sadece "DTF Başlat" (dtfButton) aktiftir.
#   - "DTF Başlat" → isDtfForAuction=true → `BuyerStartDtfTenderDialog(auction, ...)` açılır.
#     Bu dialog buyer'ı `BuyerModel.getBuyerByTaxNumber(company.getTaxNumber())` ile bulur —
#     company 998'in taxnumber'ı (4050604050) buyer.id=151 ile EŞLEŞİYOR (kasıtlı kurulum).
#   - "Katılımcılar" alanı varsayılan "Tedarikçiler" + "Tüm Tedarikçiler" checkbox'ı varsayılan
#     İŞARETLİ → `SupplierModel.getActiveSuppliersByBuyerId(buyer.getId())` ile buyer.id=151'in
#     TÜM aktif tedarikçileri (KT Test Tedarikçi + Baran Madencilik) katılımcı olur — HİÇBİR
#     SupplierGroup/SubSupplier kurulumu gerekmez.
#   - Auction seçiliyken dialog TÜM alanları (DTF Oranı, İhale Tutarı, Bitiş Tarihi, Valör Tarihi)
#     OTOMATİK DOLDURUR (dtfAuctionAndBuyerComboBox'ın value-change listener'ı) — hiçbir alana
#     dokunmadan doğrudan "İhale Başlat" tıklanabilir.
#   - "İhale Başlat" → `BuyerTenderPreviewDialog` ("İhale Ön İzleme") açılır → "Evet" →
#     `startDtfAuctionAndDtfTender` çalışır → `tender.tendertype='DTF'`, bağlı `auction.productType
#     ='DEEP_TIER_FINANCING'`, `tender.status='WAITING'`.
#
# ⚠️ Gerçek TÜBİTAK/Roketsan zincirinde AYNI "Katılımcılar" adımı aktif bir bug'a çarpıyor
#   (OP#5892) — bu senaryo İZOLE, temiz veri kullandığı için o bug'ı TETİKLEMEZ; bu senaryonun
#   PASSED olması "mekanizma prensipte doğru çalışıyor" tezini kanıtlar (bkz. memory dosyası).
Feature: DTF İşlemi UAT

  @ui @uat @regression @dtf @dtf-001
  Scenario: DTF-001 - Ara Tedarikçi Ana Firma faturasıyla DTF başlatır, Katılımcılar listesi doğru gösterilmeli
    Given DTF senaryosu için 1 adet E-Fatura içeren Excel hazırlanır
    When admin DTF ana firma kullanıcısına geçiş yapar
    And ana firma ekranında hazırlanan Excel ile faturalar yüklenir
    Then faturaların başarıyla yüklendiği doğrulanır
    When admin DTF ara tedarikçi kullanıcısına geçiş yapar
    And yüklenen fatura için "Yeni Teklif Talebi Başlat" ekranı açılır
    Then "DTF Başlat" butonu aktif olmalı ve "Teklif Al" butonu pasif olmalı
    When "DTF Başlat" butonuna tıklanır
    Then "İhale Ayrıntıları" (DTF Tender) diyaloğu açılmalı ve alanlar otomatik dolmuş olmalı
    When DTF için "İhale Başlat" butonuna tıklanır
    Then "İhale Ön İzleme" diyaloğunda katılımcı sayısı 2 olmalı
    When "Evet" butonuna tıklanır
    Then DTF Tender ve bağlı Auction veritabanında doğru statüyle oluşmuş olmalı
