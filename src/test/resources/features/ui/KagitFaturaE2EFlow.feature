@ui @dev2 @e2e @kritik @kfatura
Feature: SCN-KFATURA-E2E - Kağıt Fatura E2E Akışı (Limit ve Fiyat Kriterli)
  #
  # Akış:
  #   1. Admin    → Limit ve fiyat kriterleri (kriter tabloları) ayarlar
  #   2. Alıcı    → Kağıt fatura yükler (resim → form → Yükle)
  #   3. Tedarikçi → Onaylanan fatura için ihale oluşturur ve yayınlar
  #   4. Finansman → Kriterlere uygun faiz oranıyla teklif verir
  #   5. Tedarikçi → Teklifi kabul eder (bordro oluşur)
  #   6. Admin    → Yönetim panelinden bordroyu onaylar
  #
  # QA Hub: SCN-KFATURA-001
  # Bağlı TC: TC-COMP-01, TC-ADMIN-02, TC-FACT-03
  #

  @smoke @happy-path
  Scenario: SCN-KFATURA-001 - Limit ve fiyat kriterli tam E2E akış

    # ─── ASAMA 0: Tüm rol oturumları hazırlanır ────────────────────────────────
    Given tum rol oturumlari hazirlandir:
      | rol       | kullanici                         |
      | company   | EFG Gida A.S.                     |
      | admin     | dorukhan.ersoyleyen@faturalab.com |
      | buyer     | ALBC Marketler Zinciri A.S.       |
      | factoring | Akbank                            |

    # ─── ASAMA 1: Admin limit ve fiyat kriterleri ayarlar ─────────────────────
    # Kriter tabloları: finansmanın hangi fiyat aralığında teklif verebileceğini belirler.
    # Limit kontrolü otomatik teklifte devreye girer; manuel ihalede limit aşılabilir.
    When admin rolune gecilirse
    And limit ve fiyat yonetimi ekranina gidilirse
    And finansman limiti asagidaki kriterlerle ayarlanirsa:
      | kurumAdi | limitTutari | paraBirimi |
      | Akbank   | 1000000     | TRY        |
    And fiyat kriterleri asagidaki degerlerle yapılandırılırsa:
      | minFaizOrani | maxFaizOrani | azamiVadeSuresiGun | otomatikEsleme |
      | 0.5          | 5.0          | 90                 | true           |
    Then limit ve fiyat kriterleri basariyla kaydedilmeli

    # ─── ASAMA 2: Alıcı kağıt fatura yükler ──────────────────────────────────
    # Kağıt fatura süreci: herhangi bir resim/görsel seç → form doldur → Yükle
    When alici rolune gecilirse
    And alici kagit fatura yukleme ekranina gidilirse
    And bir gorsel dosya secilir
    And kagit fatura formu asagidaki bilgilerle doldurulur:
      | tedarikciVergiNo | faturaTutari | vadeSuresiGun |
      | 3960656675       | 50000        | 90            |
    And "Yukle" butonuna tiklanirsa
    Then kagit fatura basariyla yuklenmelidir

    # ─── ASAMA 3: Tedarikçi onaylanan fatura için ihale oluşturur ─────────────
    # İhale: finansman kurumlarının rekabetçi teklifler sunduğu açık artırma
    When tedarikci rolune gecilirse
    And tedarikci aktif faturalar listesine gidilirse
    And son eklenen fatura icin ihale olusturma baslatilirsa
    And ihale formu asagidaki parametrelerle doldurulur:
      | bitislerSuresiSaat | buyNowEsigiTRY |
      | 24                 | 60000          |
    And "Yayinla" butonuna tiklanirsa
    Then ihale basariyla olusturulmali
    And ihale durumu "WAITING" olmali

    # ─── ASAMA 4: Finansman teklif verir ─────────────────────────────────────
    # Faiz oranı min(%0.5) ile max(%5.0) arasında olmalı — limit 1.000.000 TRY
    When finansman rolune gecilirse
    And finansman aktif ihaleler listesine gidilirse
    And son aktif ihale icin teklif formu acilirsa
    And teklif asagidaki degerlerle girilir:
      | faizOrani | teklifTutari |
      | 2.5       | 52500        |
    And teklif "Kaydet" butonuna tiklanirsa
    Then teklif basariyla olusturulmali
    And teklif limit ve fiyat kriterlerine uygun olmali

    # ─── ASAMA 5: Tedarikçi teklifi kabul eder ───────────────────────────────
    # Kabul sonrası: ihale PENDINGBUYER durumuna geçer, bordro otomatik oluşur
    When tedarikci rolune gecilirse
    And tedarikci aktif ihaleleri goruntular
    And mevcut teklif icin "Kabul Et" butonuna tiklanirsa
    And teklif kabul onay dialogu onaylanirsa
    Then teklif kabul edilmeli
    And ihale durumu "PENDINGBUYER" olmali
    And bordro olusturulmali

    # ─── ASAMA 6: Admin yönetim panelinden bordroyu onaylar ──────────────────
    # Admin, tedarikçi-finansman arasında oluşan bordroyu nihai olarak onaylar
    When admin rolune gecilirse
    And admin bordro yonetimi ekranina gidilirse
    And olusturulan bordro icin "ONAYLA" butonuna tiklanirsa
    Then admin tarafindan bordro onay adimi tamamlandi
    And admin tarafindan bordro durumu kontrol edildi
