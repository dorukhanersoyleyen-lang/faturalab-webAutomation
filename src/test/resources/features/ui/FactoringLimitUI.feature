@ui @dev2 @factoring @limit
Feature: TC-FACT-02-UI - Finansman Limit Yonetimi UI

  # TC-FACT-02-UI-001
  @smoke @happy-path
  Scenario: TC-FACT-02-UI-001 - Admin faktoring kurumu icin gecerli limit kaydeder
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When faktoring limit formu asagidaki degerlerle doldurulursa:
      | kurumAdi    | limitTutari | paraBirimi |
      | OPR Bankasi | 5000000     | TRY        |
    Then limit basariyla kaydedilmeli

  # TC-FACT-02-UI-002
  @negative @validation
  Scenario: TC-FACT-02-UI-002 - Limit sifir girildiginde validasyon hatasi gozlemlenir
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When limit alani sifir olarak girilirse
    Then limit sifir validasyon mesaji veya engel gorunmeli

  # TC-FACT-02-UI-003
  @negative @validation
  Scenario: TC-FACT-02-UI-003 - Bos limit ile kaydet yapildiginda form bloklanir
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When limit alani bos birakılarak kaydet tiklanirsa
    Then limit form kaydet butonunun etkisiz oldugu veya hata gorunmeli

  # TC-FACT-02-UI-004
  @smoke
  Scenario: TC-FACT-02-UI-004 - Mevcut limit guncellenir
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When mevcut faktoring limiti "8000000" olarak guncellenir
    Then limit guncelleme basariyla tamamlanmali

  # TC-FACT-02-UI-005
  @regression
  Scenario: TC-FACT-02-UI-005 - Alici bazli limit ayarlamasi yapilir
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When alici bazli limit tablosuna gidilir
    Then alici bazli limit grid gorunmeli

  # TC-FACT-02-UI-006
  @smoke
  Scenario: TC-FACT-02-UI-006 - Finansman kullanicisi kendi limitini goruntular
    Given finansman OPR rolüyle dev2'ye giris yapildi
    When finansman kullanicisi kendi limit ekranini acar
    Then faktoring limit bilgisi ekranda gorunmeli

  # TC-FACT-02-UI-007
  @negative @kritik
  Scenario: TC-FACT-02-UI-007 - Limit asimi durumunda fatura teklifi bloklanir
    Given finansman OPR rolüyle dev2'ye giris yapildi
    When finansman limitini asan teklif vermeye calisir
    Then fatura teklif bloklanmali veya hata mesaji gorunmeli

  # TC-FACT-02-UI-008
  @regression
  Scenario: TC-FACT-02-UI-008 - Para birimi secimi limit formunda calisir
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When para birimi "USD" secilerek limit kaydedilirse
    Then limit basariyla kaydedilmeli

  # TC-FACT-02-UI-009
  @regression
  Scenario: TC-FACT-02-UI-009 - Limit guncelleme sonrasi grid guncellenir
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When mevcut faktoring limiti "3000000" olarak guncellenir
    Then limit grid satirinda yeni deger gorunmeli

  # TC-FACT-02-UI-010
  @edge-case
  Scenario: TC-FACT-02-UI-010 - Cok buyuk limit degeri girilir (edge case)
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When faktoring limit formu asagidaki degerlerle doldurulursa:
      | kurumAdi    | limitTutari | paraBirimi |
      | OPR Bankasi | 999999999   | TRY        |
    Then limit basariyla kaydedilmeli

  # TC-FACT-02-UI-011
  @negative
  Scenario: TC-FACT-02-UI-011 - Yetkisiz kullanici limit ekranina erisemez
    Given alici ALBC roleyle dev2'ye giris yapildi
    When limit yonetim sayfasina erisim denenir
    Then limit ekranina erisim engellenmeli veya menu gorunmemeli

  # TC-FACT-02-UI-012
  @smoke
  Scenario Outline: TC-FACT-02-UI-012 - Farkli tutar degerlerinde limit kaydi
    Given admin dorukhan roleyle dev2'ye giris yapildi
    And limit ve fiyat yonetimi ekranina gidilirse
    When faktoring limiti "<limitTutari>" TRY olarak girilirse
    Then limit kayit sonucu "<beklenenSonuc>" olmali

    Examples:
      | limitTutari | beklenenSonuc |
      | 100000      | BASARILI      |
      | 0           | HATA          |
      | 50000000    | BASARILI      |
