@ui @auction @alici @reddi
Feature: Alici Reddi ve Yeniden Ihale
  # SCN-02 senaryosunu kapsar.
  # PENDINGBUYER asamasinda alicinin ihaleyı reddetmesi ve tedarikci'nin
  # ayni faturalarla yeniden ihale acabildigini dogrular.
  # SCN-01'in negatif/kurtarma varyantidir.
  # Referans: SCN-02-alici-reddi-yeniden-ihale.md
  # Bagli TC: TC-002-001, TC-002-002, TC-002-003, TC-002-004

  # ───────────────────────────────────────────────────────────────────────────
  # SCN-02 — Alici ihaleyı reddeder, tedarikci yeniden ihale acar
  # Oncelik: Yuksek | Roller: Company, Buyer, Factoring, Admin
  # Durum gecisleri:
  #   Ihale-1: DRAFT -> WAITING -> PENDINGBUYER -> REJECTED
  #   Fatura:  APPROVED (red sonrasi kilitli kalmamali, yeniden kullanilabilmeli)
  #   Ihale-2: DRAFT -> WAITING -> PENDINGBUYER -> ACCEPTED
  # ───────────────────────────────────────────────────────────────────────────
  @smoke
  Scenario: SCN-02 - Alici ihaleyı reddeder, tedarikci yeniden ihale acar
    Given tum rol oturumlari hazirlandir:
      | rol        | kullanici                         |
      | company    | EFG Gida A.S.                     |
      | admin      | dorukhan.ersoyleyen@faturalab.com |
      | buyer      | ALBC Marketler Zinciri A.S.       |
      | factoring  | Akbank                            |

    # ASAMA 1: Tedarikci fatura yukler
    When tedarikci rolune gecilirse
    And "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And gecerli bir XML fatura dosyasi secilirse
    And "Kaydet" butonuna tiklanirsa
    Then fatura basariyla yuklenmeli
    And fatura durumu "PENDING_APPROVAL" olmali

    # ASAMA 2: Alici fatura onaylar — PENDING_APPROVAL -> APPROVED
    When alici rolune gecilirse
    And alici bekleyen fatura listesine gidilirse
    And fatura "ONAYLA" butonuna tiklanirsa
    Then fatura basariyla onaylanmali
    And fatura durumu "APPROVED" olmali
    # TODO: implement step def — "alici bekleyen fatura listesine gidilirse"
    # TODO: implement step def — "fatura 'ONAYLA' butonuna tiklanirsa"

    # ASAMA 3: Tedarikci ihale olusturur ve yayinlar — DRAFT -> WAITING
    When tedarikci rolune gecilirse
    And ihale olusturma akisi baslatilirsa
    And onaylanan fatura secilirse
    And ihale formu doldurulursa
    And "Yayinla" butonuna tiklanirsa
    Then ihale durumu "WAITING" olmali
    # TODO: implement step def — "ihale olusturma akisi baslatilirsa"
    # TODO: implement step def — "onaylanan fatura secilirse"
    # TODO: implement step def — "ihale formu doldurulursa"
    # TODO: implement step def — "ihale durumu 'WAITING' olmali"

    # ASAMA 4: Finansman teklif verir
    When finansman rolune gecilirse
    And aktif ihaleler listesinde ilgili ihale bulunursa
    And teklif formu acilir ve teklif miktari girilirse
    And teklif kaydedilirse
    Then basari bildirimi gorunmeli
    # TODO: implement step def — "aktif ihaleler listesinde ilgili ihale bulunursa"
    # TODO: implement step def — "teklif formu acilir ve teklif miktari girilirse"
    # TODO: implement step def — "teklif kaydedilirse"

    # ASAMA 5: Tedarikci teklifi kabul eder — WAITING -> PENDINGBUYER
    When tedarikci rolune gecilirse
    And aktif ihaleler ekraninda ilgili ihale secilirse
    And CompanyAuctionOffersView acilirsa
    And teklif secilir ve "Kabul Et" butonuna tiklanirsa
    And onay dialogu onaylanirsa
    Then ihale durumu "PENDINGBUYER" olmali
    And kazanan teklifin OfferState "WONAUCTION" olmali
    And finansman sisteminde bordro olusturulmali
    # TODO: implement step def — "aktif ihaleler ekraninda ilgili ihale secilirse"
    # TODO: implement step def — "CompanyAuctionOffersView acilirsa"
    # TODO: implement step def — "teklif secilir ve 'Kabul Et' butonuna tiklanirsa"
    # TODO: implement step def — "onay dialogu onaylanirsa"
    # TODO: implement step def — "ihale durumu 'PENDINGBUYER' olmali"
    # TODO: implement step def — "kazanan teklifin OfferState {string} olmali"

    # ASAMA 6: Alici ihaleyı REDDEDER — PENDINGBUYER -> REJECTED (Ana Test Noktasi)
    When alici rolune gecilirse
    And ihale listesi ekranina gidilirse
    And bekleyen ihale icin "REDDET" butonuna tiklanirsa
    And red nedeni girilirse
    Then ihale reddedilmeli
    And ihale durumu "REJECTED" olmali
    # TODO: implement step def — "ihale durumu 'REJECTED' olmali"

    # ASAMA 7: Finansman red bildirimi dogrulamasi
    When finansman rolune gecilirse
    And teklif verilen ihaleler listesi incelenirse
    Then teklifin OfferState "AUCTIONREJECT" olmali
    # TODO: implement step def — "teklif verilen ihaleler listesi incelenirse"
    # TODO: implement step def — "teklifin OfferState {string} olmali"

    # ASAMA 8: Tedarikci faturaların kilidinin acildigini dogrular (Kritik Kontrol)
    When tedarikci rolune gecilirse
    And "Faturalarim" ekranina gidilirse
    Then reddedilen ihaledeki faturalar "APPROVED" durumunda olmali
    And faturalar yeni ihale icin kullanilabilir olmali
    # TODO: implement step def — "reddedilen ihaledeki faturalar {string} durumunda olmali"
    # TODO: implement step def — "faturalar yeni ihale icin kullanilabilir olmali"

    # ASAMA 9: Tedarikci yeniden ihale acar — yeni WAITING
    When ihale olusturma akisi baslatilirsa
    And kilitlenmis olmayan onaylanan fatura secilirse
    And ihale formu doldurulursa
    And "Yayinla" butonuna tiklanirsa
    Then yeni ihale "WAITING" olarak olusturulmali
    And eski ihale "REJECTED" ve yeni ihale "WAITING" olarak listede ayri gorunmeli
    # TODO: implement step def — "kilitlenmis olmayan onaylanan fatura secilirse"
    # TODO: implement step def — "yeni ihale 'WAITING' olarak olusturulmali"
    # TODO: implement step def — "eski ihale 'REJECTED' ve yeni ihale 'WAITING' olarak listede ayri gorunmeli"

    # ASAMA 10-11: Finansman 2. ihalede teklif verir; tedarikci teklifi secer
    When finansman rolune gecilirse
    And aktif ihaleler listesinde yeni ihale bulunursa
    And teklif formu acilir ve teklif miktari girilirse
    And teklif kaydedilirse
    Then basari bildirimi gorunmeli
    When tedarikci rolune gecilirse
    And aktif ihaleler ekraninda yeni ihale secilirse
    And CompanyAuctionOffersView acilirsa
    And teklif secilir ve "Kabul Et" butonuna tiklanirsa
    And onay dialogu onaylanirsa
    Then ihale durumu "PENDINGBUYER" olmali
    # TODO: implement step def — "aktif ihaleler listesinde yeni ihale bulunursa"
    # TODO: implement step def — "aktif ihaleler ekraninda yeni ihale secilirse"

    # ASAMA 12: Alici 2. ihaleyı onaylar — PENDINGBUYER -> ACCEPTED
    When alici rolune gecilirse
    And ihale listesi ekranina gidilirse
    And bekleyen ihale icin "ONAYLA" butonuna tiklanirsa
    Then ihale basariyla onaylanmali
    And ihale durumu "ACCEPTED" olmali
    # TODO: implement step def — "ihale durumu 'ACCEPTED' olmali"
