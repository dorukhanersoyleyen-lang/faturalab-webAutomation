# language: tr
@api @ihale @company @low-priority
Özellik: Tedarikçi İhale Oluşturma Akışı

  Bu özellik tedarikçinin ihale oluşturma, yayınlama ve kazanan teklif
  seçme işlemlerini test eder.
  NOT: Bu özellik %70 hedeflidir — yeni/geliştirilmekte olan bir yapıdır.
  Teklif hesaplama ve yetki kontrolleri kritik, diğerleri düşük önceliklidir.
  Test Case Referansları: TC-COMP-02-001 ... TC-COMP-02-010
  QA Doküman: olustur/qa/test-cases/TC-COMP-02-ihale-olusturma.md

  Arka Plan:
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Ve sistemde "APPROVED" durumunda en az bir fatura mevcut

  # TC-COMP-02-001
  @smoke @happy-path
  Senaryo: TC-COMP-02-001 - Onaylı fatura ile ihale oluşturma ve yayınlama
    Eğer ki tedarikçi onaylı faturayı seçip ihale parametrelerini girerse
      | auctionDuration | participantType | valorDate   |
      | 24_HOURS        | ALL             | +7          |
    Ve ihaleyi yayınlarsa
    O zaman ihale "WAITING" durumunda oluşmuş olmalı
    Ve ihale aktif ihaleler listesinde görünmeli

  # TC-COMP-02-002
  @smoke
  Senaryo: TC-COMP-02-002 - İhaleyi taslak olarak kaydetme
    Eğer ki tedarikçi onaylı faturayı seçip ihale parametrelerini girerse
      | auctionDuration | participantType | valorDate   |
      | 24_HOURS        | ALL             | +7          |
    Ve ihaleyi taslak olarak kaydederse
    O zaman ihale "DRAFT" durumunda oluşmuş olmalı
    Ve taslak ihaleler listesinde görünmeli

  # TC-COMP-02-003
  @negative @kritik
  Senaryo: TC-COMP-02-003 - Onaysız (PENDING_APPROVAL) fatura ile ihale açma denemesi
    Eğer ki tedarikçi "PENDING_APPROVAL" durumundaki bir faturayı ihale için seçmeye çalışırsa
    O zaman fatura seçilememeli
    Ve uyarı veya kısıt mesajı görünmeli

  # TC-COMP-02-004
  @negative @kritik
  Senaryo: TC-COMP-02-004 - Aktif ihalede kullanılan faturayı tekrar seçme denemesi
    Diyelim ki bir fatura aktif "WAITING" durumundaki ihalede kullanılıyor
    Eğer ki tedarikçi aynı faturayı yeni bir ihalede seçmeye çalışırsa
    O zaman fatura seçilememeli

  # TC-COMP-02-005
  @negative @validation
  Senaryo: TC-COMP-02-005 - Zorunlu alan boş bırakılarak ihale yayınlama denemesi
    Eğer ki tedarikçi valör tarihi boş bırakıp ihale yayınlamaya çalışırsa
    O zaman "Bu alan zorunludur" validasyon hatası görünmeli
    Ve ihale oluşturulmamış olmalı

  # TC-COMP-02-006
  @edge-case @validation
  Senaryo: TC-COMP-02-006 - Geçmiş tarihli valör tarihi ile ihale oluşturma
    Eğer ki tedarikçi valör tarihi olarak geçmiş bir tarih girerse
    O zaman geçersiz tarih hatası görünmeli
    Ve ihale oluşturulmamış olmalı

  # TC-COMP-02-007
  @smoke @kritik @happy-path
  Senaryo: TC-COMP-02-007 - Kazanan teklifi seçme
    Diyelim ki "WAITING" durumunda bir ihale mevcut
    Ve finansman bu ihalede teklif vermiş
      | offerAmount |
      | 50000       |
    Eğer ki tedarikçi kazanan teklifi seçip onaylarsa
    O zaman ihale "PENDINGBUYER" durumuna geçmeli
    Ve kazanan teklif "WONAUCTION" durumunda olmalı

  # TC-COMP-02-008
  @negative
  Senaryo: TC-COMP-02-008 - Hiç teklif yokken kazananı seçme denemesi
    Diyelim ki "WAITING" durumunda teklif almamış bir ihale mevcut
    Eğer ki tedarikçi kazanan seç aksiyonunu almaya çalışırsa
    O zaman aksiyon butonu mevcut olmamalı veya devre dışı olmalı

  # TC-COMP-02-009
  @smoke
  Senaryo: TC-COMP-02-009 - Taslak ihalenin silinmesi
    Diyelim ki "DRAFT" durumunda bir ihale mevcut
    Eğer ki tedarikçi taslak ihaleyi silerse
    O zaman ihale listeden kaldırılmış olmalı
    Ve ihaledeki faturalar serbest kalmalı

  # TC-COMP-02-010
  @negative @kritik
  Senaryo: TC-COMP-02-010 - Yayınlanmış ihalenin düzenlenmesi denemesi
    Diyelim ki "WAITING" durumunda bir ihale mevcut
    Eğer ki tedarikçi ihaleyi düzenlemeye çalışırsa
    O zaman düzenleme seçeneği mevcut olmamalı veya devre dışı olmalı
