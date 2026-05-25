@ui @auction @buynow
Feature: BuyNow Anlik Kabul
  # SCN-04 senaryosunu kapsar.
  # BuyNow esigi belirlendiginde: esik alti teklif ihaleyı kapatmaz (NORMAL),
  # esigi asan teklif ise ihaleyı aninda sonlandirir (BUY_NOW).
  # OfferType: BUY_NOW kontrolu bu senaryonun ana dogrulama noktasidir.
  # Referans: SCN-04-buynow-anlik-kabul.md
  # Bagli TC: TC-004-001, TC-004-002, TC-004-003, TC-004-004, TC-004-005

  Background:
    # Ortak on hazirlik: 4 rol oturumu acilir, tedarikci fatura yukler, alici onaylar
    Given tum rol oturumlari hazirlandir:
      | rol        | kullanici                         |
      | company    | EFG Gida A.S.                     |
      | admin      | dorukhan.ersoyleyen@faturalab.com |
      | buyer      | ALBC Marketler Zinciri A.S.       |
      | factoring  | Akbank                            |
    And tedarikci rolune gecilirse
    And "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And gecerli bir XML fatura dosyasi secilirse
    And "Kaydet" butonuna tiklanirsa
    And fatura basariyla yuklenmeli
    And alici rolune gecilirse
    And alici bekleyen fatura listesine gidilirse
    And fatura "ONAYLA" butonuna tiklanirsa
    And fatura durumu "APPROVED" olmali
    # TODO: implement step def — "alici bekleyen fatura listesine gidilirse"
    # TODO: implement step def — "fatura 'ONAYLA' butonuna tiklanirsa"

  # ───────────────────────────────────────────────────────────────────────────
  # SCN-04-001 — Esik alti teklif: ihale kapanmaz
  # BuyNow esigi: 95.000 TRY | Finansman-A teklifi: 90.000 TRY (esik alti)
  # Beklenen: OfferType=NORMAL, ihale WAITING durumunda kalmaya devam eder
  # Oncelik: Yuksek | Bagli TC: TC-004-002
  # ───────────────────────────────────────────────────────────────────────────
  @smoke
  Scenario: SCN-04-001 - Esik alti teklif: ihale kapanmaz
    # ASAMA 3: Tedarikci buyNow esikiyle ihale olusturur
    When tedarikci rolune gecilirse
    And ihale olusturma akisi baslatilirsa
    And onaylanan fatura secilirse
    And buyNow esigi "95000" TRY olarak girilirse
    And ihale formu doldurulursa
    And "Yayinla" butonuna tiklanirsa
    Then ihale durumu "WAITING" olmali
    # TODO: implement step def — "ihale olusturma akisi baslatilirsa"
    # TODO: implement step def — "onaylanan fatura secilirse"
    # TODO: implement step def — "buyNow esigi {string} TRY olarak girilirse"
    # TODO: implement step def — "ihale formu doldurulursa"
    # TODO: implement step def — "ihale durumu 'WAITING' olmali"

    # ASAMA 4: Finansman-A esik alti teklif verir (90.000 TRY < 95.000 TRY)
    When finansman rolune gecilirse
    And aktif ihaleler listesinde ilgili ihale bulunursa
    And teklif tutari "90000" TRY olarak girilirse
    And teklif kaydedilirse
    Then teklif olusturulmali ve OfferType "NORMAL" olmali
    And ihale hala "WAITING" durumunda olmali
    # TODO: implement step def — "aktif ihaleler listesinde ilgili ihale bulunursa"
    # TODO: implement step def — "teklif tutari {string} TRY olarak girilirse"
    # TODO: implement step def — "teklif kaydedilirse"
    # TODO: implement step def — "teklif olusturulmali ve OfferType {string} olmali"
    # TODO: implement step def — "ihale hala {string} durumunda olmali"

  # ───────────────────────────────────────────────────────────────────────────
  # SCN-04-002 — Esik ustu teklif: ihale otomatik kapanir (BUY_NOW)
  # BuyNow esigi: 95.000 TRY | Finansman-B teklifi: 96.000 TRY (esigi aser)
  # Beklenen: OfferType=BUY_NOW, ihale WAITING -> PENDINGBUYER gecisi otomatik
  # Tedarikci manuel teklif secimi yapmaz; sistem kapatirir
  # Oncelik: Yuksek | Bagli TC: TC-004-003, TC-004-004
  # ───────────────────────────────────────────────────────────────────────────
  @smoke @happy-path
  Scenario: SCN-04-002 - Esik ustu teklif: ihale otomatik kapanir (BUY_NOW)
    # ASAMA 3: Tedarikci buyNow esikiyle ihale olusturur
    When tedarikci rolune gecilirse
    And ihale olusturma akisi baslatilirsa
    And onaylanan fatura secilirse
    And buyNow esigi "95000" TRY olarak girilirse
    And ihale formu doldurulursa
    And "Yayinla" butonuna tiklanirsa
    Then ihale durumu "WAITING" olmali
    # TODO: implement step def — "ihale olusturma akisi baslatilirsa"
    # TODO: implement step def — "onaylanan fatura secilirse"
    # TODO: implement step def — "buyNow esigi {string} TRY olarak girilirse"
    # TODO: implement step def — "ihale formu doldurulursa"
    # TODO: implement step def — "ihale durumu 'WAITING' olmali"

    # ASAMA 5: Finansman-B buyNow esigini asan teklif verir (96.000 TRY > 95.000 TRY)
    When finansman rolune gecilirse
    And aktif ihaleler listesinde ilgili ihale bulunursa
    And teklif tutari "96000" TRY olarak girilirse
    And teklif kaydedilirse
    Then teklif OfferType "BUY_NOW" olmali
    And ihale otomatik olarak "PENDINGBUYER" durumuna gecmeli
    # TODO: implement step def — "aktif ihaleler listesinde ilgili ihale bulunursa"
    # TODO: implement step def — "teklif tutari {string} TRY olarak girilirse"
    # TODO: implement step def — "teklif kaydedilirse"
    # TODO: implement step def — "teklif OfferType {string} olmali"
    # TODO: implement step def — "ihale otomatik olarak {string} durumuna gecmeli"

    # ASAMA 6: Tedarikci BuyNow akisini dogrular
    When tedarikci rolune gecilirse
    And CompanyAuctionOffersView acilirsa
    Then kazanan teklifin OfferState "WONAUCTION" olmali
    And kaybeden tekliflerin OfferState "LOSTAUCTION" olmali
    And tedarikci tarafindan manuel teklif secimi yapilmamali
    And bordro olusturulmali
    # TODO: implement step def — "CompanyAuctionOffersView acilirsa"
    # TODO: implement step def — "kazanan teklifin OfferState {string} olmali"
    # TODO: implement step def — "kaybeden tekliflerin OfferState {string} olmali"
    # TODO: implement step def — "tedarikci tarafindan manuel teklif secimi yapilmamali"
    # TODO: implement step def — "bordro olusturulmali"

    # ASAMA 7-8: Finansman-B bordroyu onaylar; alici ihaleyı onaylar
    When finansman rolune gecilirse
    And "Teklif Talebi Yonetimi" ekranina gidilirse
    And ilgili bordro satirinda "ONAYLA" butonuna tiklanirsa
    Then bordro basariyla onaylanmali
    When alici rolune gecilirse
    And ihale listesi ekranina gidilirse
    And bekleyen ihale icin "ONAYLA" butonuna tiklanirsa
    Then ihale basariyla onaylanmali
    And ihale durumu "ACCEPTED" olmali
    # TODO: implement step def — "ihale durumu 'ACCEPTED' olmali"
