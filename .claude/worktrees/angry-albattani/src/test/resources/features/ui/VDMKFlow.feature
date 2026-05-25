@ui @auction @vdmk @cmb
Feature: VDMK CMB Onayli Ihale Akisi
  # SCN-08 ve TC-COMP-03 senaryolarini kapsar.
  # VDMK (Varliga Dayali Menkul Kiyman) ihalesi olusturuldigunda sistem CMB_APPROVAL
  # durumuna gecer. Admin CMB onayini islemeden once finansman teklif verememeli.
  # Admin onayinin ardindan ihale CMB_APPROVED -> WAITING seklinde ilerler
  # ve normal SCN-01 benzeri akis devam eder.
  # AuctionStatusType: CMB_APPROVAL ve CMB_APPROVED enum'lari bu senaryonun
  # temel kontrol noktasidir.
  # Referans: SCN-08-vdmk-cmb-onayli-ihale.md, TC-COMP-03-vdmk-ihale.md
  # Bagli TC: TC-COMP-03-001, TC-008-001 ~ TC-008-006

  # ───────────────────────────────────────────────────────────────────────────
  # SCN-08 — VDMK ihalesi olustur, CMB onayi bekle, onay sonrasi ihale baslar
  # Oncelik: Orta | Roller: Company (VDMK yetkili), Admin, Factoring, Buyer
  # Durum gecis zinciri:
  #   CMB_APPROVAL -> CMB_APPROVED -> WAITING -> PENDINGBUYER -> ACCEPTED
  # ───────────────────────────────────────────────────────────────────────────
  @smoke
  Scenario: SCN-08 - VDMK ihalesi olustur, CMB onayi bekle, onay sonrasi ihale baslar
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

    # ASAMA 3: Tedarikci VDMK ihalesi olusturur — CompanyVDMKFormView
    # Beklenen: Standart WAITING degil, CMB_APPROVAL durumu olusur (Ana Test Noktasi 1)
    When tedarikci rolune gecilirse
    And VDMK ihale olusturma ekranina gidilirse
    And onaylanan fatura secilirse
    And VDMK formundaki ozgul alanlar doldurulursa
    And VDMK formu gonderilirse
    Then VDMK ihalesi "CMB_APPROVAL" durumunda olusturulmali
    And ihale standart "WAITING" durumuna gecmemeli
    # TODO: implement step def — "VDMK ihale olusturma ekranina gidilirse"
    # TODO: implement step def — "onaylanan fatura secilirse"
    # TODO: implement step def — "VDMK formundaki ozgul alanlar doldurulursa"
    # TODO: implement step def — "VDMK formu gonderilirse"
    # TODO: implement step def — "VDMK ihalesi {string} durumunda olusturulmali"
    # TODO: implement step def — "ihale standart 'WAITING' durumuna gecmemeli"

    # ASAMA 4: CMB_APPROVAL durumunda finansman teklif vermeye calisir (engel kontrolu)
    When finansman rolune gecilirse
    And aktif ihaleler listesi incelenirse
    Then CMB_APPROVAL durumundaki VDMK ihalesi icin teklif butonu aktif olmamali
    # TODO: implement step def — "aktif ihaleler listesi incelenirse"
    # TODO: implement step def — "CMB_APPROVAL durumundaki VDMK ihalesi icin teklif butonu aktif olmamali"

    # ASAMA 5: Admin CMB onayini isler — CMB_APPROVAL -> CMB_APPROVED (Ana Test Noktasi 2)
    When admin rolune gecilirse
    And admin fatura yonetimi ekranina gidilirse
    And CMB onay bekleyen ihaleler listesine gidilirse
    And VDMK ihalesi icin "CMB Onayla" butonuna tiklanirsa
    And onay dialogu onaylanirsa
    Then VDMK ihalesi "CMB_APPROVED" durumuna gecmeli
    And ihale "WAITING" durumuna ilerlemelidir
    # TODO: implement step def — "CMB onay bekleyen ihaleler listesine gidilirse"
    # TODO: implement step def — "VDMK ihalesi icin 'CMB Onayla' butonuna tiklanirsa"
    # TODO: implement step def — "onay dialogu onaylanirsa"
    # TODO: implement step def — "VDMK ihalesi {string} durumuna gecmeli"
    # TODO: implement step def — "ihale 'WAITING' durumuna ilerlemelidir"
    # NOT: CMB_APPROVED -> WAITING gecisinin otomatik mi yoksa ayri admin aksiyonu
    # gerektirip gerektirmedigini dogrulanmasi gerekiyor

    # ASAMA 6: Tedarikci WAITING durumunu dogrular
    When tedarikci rolune gecilirse
    And VDMK ihaleler listesi yenilenirse
    Then VDMK ihalesi "WAITING" durumuna gecmeli
    # TODO: implement step def — "VDMK ihaleler listesi yenilenirse"

    # ASAMA 7: Finansman WAITING durumundaki VDMK ihalesi icin teklif verir
    When finansman rolune gecilirse
    And aktif ihaleler listesinde VDMK ihalesi gorunmeli
    And VDMK ihalesi detayi acilirsa
    And teklif formu acilir ve teklif miktari girilirse
    And teklif kaydedilirse
    Then basari bildirimi gorunmeli
    # TODO: implement step def — "aktif ihaleler listesinde VDMK ihalesi gorunmeli"
    # TODO: implement step def — "VDMK ihalesi detayi acilirsa"
    # TODO: implement step def — "teklif formu acilir ve teklif miktari girilirse"
    # TODO: implement step def — "teklif kaydedilirse"

    # ASAMA 8: Tedarikci kazanan teklifi secer — WAITING -> PENDINGBUYER
    When tedarikci rolune gecilirse
    And aktif ihaleler ekraninda VDMK ihalesi secilirse
    And CompanyAuctionOffersView acilirsa
    And teklif secilir ve "Kabul Et" butonuna tiklanirsa
    And onay dialogu onaylanirsa
    Then ihale durumu "PENDINGBUYER" olmali
    And kazanan teklifin OfferState "WONAUCTION" olmali
    And finansman sisteminde bordro olusturulmali
    # TODO: implement step def — "aktif ihaleler ekraninda VDMK ihalesi secilirse"
    # TODO: implement step def — "CompanyAuctionOffersView acilirsa"
    # TODO: implement step def — "teklif secilir ve 'Kabul Et' butonuna tiklanirsa"
    # TODO: implement step def — "onay dialogu onaylanirsa"
    # TODO: implement step def — "ihale durumu 'PENDINGBUYER' olmali"
    # TODO: implement step def — "kazanan teklifin OfferState {string} olmali"

    # ASAMA 9: Finansman bordroyu onaylar
    When finansman rolune gecilirse
    And "Teklif Talebi Yonetimi" ekranina gidilirse
    And ilgili bordro satirinda "ONAYLA" butonuna tiklanirsa
    Then bordro basariyla onaylanmali

    # ASAMA 10: Alici VDMK ihalesini onaylar — PENDINGBUYER -> ACCEPTED
    When alici rolune gecilirse
    And ihale listesi ekranina gidilirse
    And bekleyen ihale icin "ONAYLA" butonuna tiklanirsa
    Then ihale basariyla onaylanmali
    And ihale durumu "ACCEPTED" olmali
    # TODO: implement step def — "ihale durumu 'ACCEPTED' olmali"

    # ASAMA 11: Admin tam durum gecis zincirini dogrular
    When admin rolune gecilirse
    And admin fatura yonetimi ekranina gidilirse
    And VDMK ihalesi admin listesinde "ACCEPTED" olarak gorunmeli
    Then VDMK ihalesi durum gecis zinciri dogrulansın:
      """
      CMB_APPROVAL -> CMB_APPROVED -> WAITING -> PENDINGBUYER -> ACCEPTED
      """
    # TODO: implement step def — "VDMK ihalesi admin listesinde {string} olarak gorunmeli"
    # TODO: implement step def — "VDMK ihalesi durum gecis zinciri dogrulansin:"
