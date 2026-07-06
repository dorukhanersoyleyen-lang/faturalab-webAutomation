# language: tr
@api @fatura @onay @admin @regression
Özellik: Admin ve Finansal Kurum Fatura Onaylama Akışı

  Bu özellik admin ve finansal kurum tarafından yapılan fatura onaylama,
  engelleme ve arama işlemlerini test eder.
  Fatura onayını BUYER değil, ADMIN veya FACTORING yapar.
  Test Case Referansları: TC-ADMIN-02-001 ... TC-ADMIN-02-007
  QA Doküman: olustur/qa/test-cases/TC-ADMIN-02-fatura-onay.md

  # TC-ADMIN-02-001
  @smoke @happy-path @admin @kritik
  Senaryo: TC-ADMIN-02-001 - Admin fatura onaylama (mutlu yol)
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve admin kullanıcısı ile kimlik doğrulaması yapıldı
    Ve sistemde "PENDING_APPROVAL" durumunda bir fatura mevcut
      | invoiceNo         | supplierTaxNo | invoiceAmount | invoiceType |
      | ADMIN-APPR-001    | 4050604050    | 3000          | E_FATURA    |
    Eğer ki admin faturayı onaylarsa
    O zaman fatura durumu "APPROVED" olmalı
    Ve tedarikçi faturayı "APPROVED" olarak görmeli
    Ve fatura ihale seçim listesinde görünmeli

  # TC-ADMIN-02-002 — DEVRE DIŞI (Q7: fatura engelleme özelliği yok)
  @disabled
  Senaryo: TC-ADMIN-02-002 - [DEVRE DIŞI] Admin fatura engelleme (BLOCKED)
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve admin kullanıcısı ile kimlik doğrulaması yapıldı

  # TC-ADMIN-02-003 — DEVRE DIŞI (Q7: BLOCKED özelliği yok)
  @disabled
  Senaryo: TC-ADMIN-02-003 - [DEVRE DIŞI] BLOCKED faturanın ihaleye eklenmesi denemesi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve admin kullanıcısı ile kimlik doğrulaması yapıldı

  # TC-ADMIN-02-004
  @smoke @admin
  Senaryo: TC-ADMIN-02-004 - Fatura numarası ile admin arama
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve admin kullanıcısı ile kimlik doğrulaması yapıldı
    Ve sistemde bilinen bir fatura mevcut
      | invoiceNo       |
      | ADMIN-APPR-001  |
    Eğer ki admin fatura numarasını arama alanına girip sorgularsa
    O zaman fatura detay ekranı açılmalı
    Ve fatura bilgileri doğru gösterilmeli

  # TC-ADMIN-02-005
  @negative @admin
  Senaryo: TC-ADMIN-02-005 - Yanlış fatura numarası ile admin arama
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve admin kullanıcısı ile kimlik doğrulaması yapıldı
    Eğer ki admin sistemde olmayan bir fatura numarasını sorgularsa
    O zaman "Fatura bulunamadı" uyarısı görünmeli

  # TC-ADMIN-02-006
  @negative @validation @admin
  Senaryo: TC-ADMIN-02-006 - Boş fatura numarası ile arama denemesi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve admin kullanıcısı ile kimlik doğrulaması yapıldı
    Eğer ki admin fatura numarası alanını boş bırakıp sorgularsa
    O zaman "Bu alan zorunludur" validasyon hatası görünmeli

  # TC-ADMIN-02-007
  @smoke @entegrasyon @admin @kritik
  Senaryo: TC-ADMIN-02-007 - Onaylanan faturanın ihale seçim listesinde görünmesi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve admin kullanıcısı ile kimlik doğrulaması yapıldı
    Ve bir fatura "PENDING_APPROVAL" durumundayken admin tarafından onaylanmış
      | invoiceNo       |
      | ADMIN-APPR-001  |
    Eğer ki tedarikçi ihale oluşturma ekranında fatura seçim listesini açarsa
    O zaman onaylanan fatura listede "APPROVED" olarak görünmeli
    Ve fatura seçilebilir durumda olmalı
