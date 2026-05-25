@ui @auction @faktoring @kritik
Feature: Coklu Finansman Teklif Yarisi
  # SCN-03 senaryosunu kapsar.
  # 3 farkli finansman kurulusunun ayni ihalede yarismasi ve tedarikci'nin
  # en avantajli (en dusuk faiz oranli) teklifi secmesini test eder.
  # WONAUCTION ve LOSTAUCTION durum gecisleri bu senaryonun ana dogrulama noktasidir.
  # Referans: SCN-03-coklu-finansman-en-iyi-teklif.md
  # Bagli TC: TC-003-001, TC-003-002, TC-003-003, TC-003-004, TC-003-005

  # ───────────────────────────────────────────────────────────────────────────
  # SCN-03 — 3 finansman teklif verir, tedarikci en iyisini secer
  # Oncelik: Yuksek | Roller: Company, Buyer, Faktoring-A, Faktoring-B, Faktoring-C, Admin
  # Finansman-A: %1.5 | Finansman-B: %1.2 (KAZANAN) | Finansman-C: %1.8
  # ───────────────────────────────────────────────────────────────────────────
  @smoke @happy-path
  Scenario: SCN-03 - 3 finansman teklif verir, tedarikci en iyisini secer
    # ASAMA 1-2: Fatura yukleme ve alici onayi
    Given tum rol oturumlari hazirlandir:
      | rol        | kullanici                         |
      | company    | EFG Gida A.S.                     |
      | admin      | dorukhan.ersoyleyen@faturalab.com |
      | buyer      | ALBC Marketler Zinciri A.S.       |
      | factoring  | Akbank                            |
    When tedarikci rolune gecilirse
    And "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And gecerli bir XML fatura dosyasi secilirse
    And "Kaydet" butonuna tiklanirsa
    Then fatura basariyla yuklenmeli
    And fatura durumu "PENDING_APPROVAL" olmali

    # ASAMA 2: Alici fatura onayi — PENDING_APPROVAL -> APPROVED
    When alici rolune gecilirse
    And alici bekleyen fatura listesine gidilirse
    And fatura "ONAYLA" butonuna tiklanirsa
    Then fatura basariyla onaylanmali
    And fatura durumu "APPROVED" olmali
    # TODO: implement step def — "alici bekleyen fatura listesine gidilirse"
    # TODO: implement step def — "fatura 'ONAYLA' butonuna tiklanirsa"

    # ASAMA 3: Tedarikci ihale olusturma ve yayinlama — DRAFT -> WAITING
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

    # ASAMA 4: Finansman-A teklif verme — Oran: %1.5
    When finansman rolune gecilirse
    And aktif ihaleler listesinde ilgili ihale bulunursa
    And teklif formu acilir ve faiz orani "%1.5" olarak girilirse
    And teklif kaydedilirse
    Then teklif olusturulmali ve OfferType "NORMAL" olmali
    # TODO: implement step def — "aktif ihaleler listesinde ilgili ihale bulunursa"
    # TODO: implement step def — "teklif formu acilir ve faiz orani {string} olarak girilirse"
    # TODO: implement step def — "teklif kaydedilirse"
    # TODO: implement step def — "teklif olusturulmali ve OfferType {string} olmali"

    # ASAMA 5: Finansman-B teklif verme — Oran: %1.2 (en iyi)
    # NOT: Dev2 ortaminda tek finansman hesabi (Akbank) mevcut; bu adim ayni oturum uzerinden simule edilir
    When finansman rolune gecilirse
    And aktif ihaleler listesinde ilgili ihale bulunursa
    And teklif formu acilir ve faiz orani "%1.2" olarak girilirse
    And teklif kaydedilirse
    Then teklif olusturulmali ve OfferType "NORMAL" olmali

    # ASAMA 6: Finansman-C teklif verme — Oran: %1.8
    When finansman rolune gecilirse
    And aktif ihaleler listesinde ilgili ihale bulunursa
    And teklif formu acilir ve faiz orani "%1.8" olarak girilirse
    And teklif kaydedilirse
    Then teklif olusturulmali ve OfferType "NORMAL" olmali

    # ASAMA 7: Tedarikci CompanyAuctionOffersView ekraninda teklifleri karsilastirir
    When tedarikci rolune gecilirse
    And aktif ihaleler ekraninda ilgili ihale secilirse
    And CompanyAuctionOffersView acilirsa
    Then 3 teklifin listede gorunmesi gerekir
    And teklifler faiz oranina gore sirali olmali
    And en dusuk oranli teklif en ustte gosterilmeli
    # TODO: implement step def — "aktif ihaleler ekraninda ilgili ihale secilirse"
    # TODO: implement step def — "CompanyAuctionOffersView acilirsa"
    # TODO: implement step def — "3 teklifin listede gorunmesi gerekir"
    # TODO: implement step def — "teklifler faiz oranina gore sirali olmali"
    # TODO: implement step def — "en dusuk oranli teklif en ustte gosterilmeli"

    # ASAMA 8: Tedarikci en avantajli teklifi (Finansman-B, %1.2) secer
    When Finansman-B'nin teklifinde "Kabul Et" butonuna tiklanirsa
    And onay dialogu onaylanirsa
    Then ihale durumu "PENDINGBUYER" olmali
    And kazanan teklifin OfferState "WONAUCTION" olmali
    And kaybeden tekliflerin OfferState "LOSTAUCTION" olmali
    And bordro olusturulmali
    # TODO: implement step def — "Finansman-B'nin teklifinde 'Kabul Et' butonuna tiklanirsa"
    # TODO: implement step def — "onay dialogu onaylanirsa"
    # TODO: implement step def — "ihale durumu 'PENDINGBUYER' olmali"
    # TODO: implement step def — "kazanan teklifin OfferState {string} olmali"
    # TODO: implement step def — "kaybeden tekliflerin OfferState {string} olmali"
    # TODO: implement step def — "bordro olusturulmali"

    # ASAMA 9-10: Finansman-A ve C LOSTAUCTION dogrulama; Finansman-B bordro onaylama
    When finansman rolune gecilirse
    And "Teklif Talebi Yonetimi" ekranina gidilirse
    And ilgili bordro satirinda "ONAYLA" butonuna tiklanirsa
    Then bordro basariyla onaylanmali

    # ASAMA 11: Alici ihaleyi onaylar — PENDINGBUYER -> ACCEPTED
    When alici rolune gecilirse
    And ihale listesi ekranina gidilirse
    And bekleyen ihale icin "ONAYLA" butonuna tiklanirsa
    Then ihale basariyla onaylanmali
    And ihale durumu "ACCEPTED" olmali
    # TODO: implement step def — "ihale durumu 'ACCEPTED' olmali"
