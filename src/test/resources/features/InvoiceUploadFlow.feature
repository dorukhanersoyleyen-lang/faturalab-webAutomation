# language: tr
@api @fatura @company
Özellik: Tedarikçi Fatura Yükleme Akışı

  Bu özellik tedarikçi tarafında fatura yükleme işlemlerini test eder.
  Test Case Referansları: TC-COMP-01-001 ... TC-COMP-01-011
  QA Doküman: olustur/qa/test-cases/TC-COMP-01-fatura-yukleme.md

  Arka Plan:
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı

  # TC-COMP-01-001
  @smoke @happy-path @company
  Senaryo: TC-COMP-01-001 - Geçerli XML fatura yükleme
    Eğer ki tedarikçi geçerli XML fatura bilgileri ile fatura yüklerse
      | invoiceNo          | supplierTaxNo | invoiceAmount | invoiceType |
      | COMP-XML-001       | 1234567893    | 5000          | E_FATURA    |
    O zaman fatura başarıyla yüklenmiş olmalı
    Ve fatura durumu "PENDING_APPROVAL" olmalı
    Ve fatura listesinde faturası görünmeli

  # TC-COMP-01-002
  @negative @company
  Senaryo: TC-COMP-01-002 - Geçersiz XML fatura yükleme
    Eğer ki tedarikçi bozuk XML dosyası ile fatura yüklemeye çalışırsa
    O zaman fatura yükleme hata mesajı alınmalı
    Ve fatura yüklenmemiş olmalı

  # TC-COMP-01-003
  @negative @company
  Senaryo: TC-COMP-01-003 - Sistemde tanımsız alıcıya ait fatura yükleme
    Eğer ki tedarikçi sistemde tanımlı olmayan alıcı VKN ile fatura yüklerse
      | invoiceNo          | supplierTaxNo | invoiceAmount | invoiceType |
      | COMP-NOBUYER-001   | 9999999999    | 5000          | E_FATURA    |
    O zaman alıcı bulunamadı uyarısı görünmeli

  # TC-COMP-01-004
  @negative @security @company
  Senaryo: TC-COMP-01-004 - ADD_INVOICE yetkisi olmayan kullanıcı fatura yükleme denemesi
    Eğer ki ADD_INVOICE yetkisi olmayan kullanıcı fatura yüklemeye çalışırsa
    O zaman yetki hatası alınmalı
    Ve fatura yüklenmemiş olmalı

  # TC-COMP-01-005
  @negative @security @company
  Senaryo: TC-COMP-01-005 - Support kullanıcı fatura kaydetme denemesi
    Diyelim ki support rolündeki kullanıcı ile kimlik doğrulaması yapıldı
    Eğer ki support kullanıcı fatura kaydetmeye çalışırsa
      | invoiceNo          | supplierTaxNo | invoiceAmount | invoiceType |
      | COMP-SUPPORT-001   | 1234567893    | 1000          | E_FATURA    |
    O zaman "Support kullanıcıları fatura kaydedemez" uyarısı alınmalı
    Ve fatura yüklenmemiş olmalı

  # TC-COMP-01-007
  @smoke @excel @company
  Senaryo: TC-COMP-01-007 - Geçerli Excel ile toplu fatura yükleme
    Eğer ki tedarikçi geçerli Excel dosyası ile toplu fatura yüklerse
      | rowCount | validRows | invalidRows |
      | 3        | 3         | 0           |
    O zaman toplu yükleme sonuç ekranı gösterilmeli
    Ve "3 fatura başarıyla yüklendi" mesajı görünmeli
    Ve "0 hatalı satır" görünmeli
    Ve yüklenen 3 fatura "PENDING_APPROVAL" durumunda listelenmeli

  # TC-COMP-01-008
  @edge-case @excel @company
  Senaryo: TC-COMP-01-008 - Kısmen geçerli Excel yükleme (bazı satırlar hatalı)
    Eğer ki tedarikçi 3 geçerli 2 hatalı satır içeren Excel ile toplu fatura yüklerse
      | rowCount | validRows | invalidRows |
      | 5        | 3         | 2           |
    O zaman toplu yükleme sonuç ekranı gösterilmeli
    Ve "3 fatura başarıyla yüklendi" mesajı görünmeli
    Ve "2 hatalı satır" görünmeli
    Ve geçerli 3 fatura sisteme eklenmiş olmalı
    Ve hatalı 2 satır sisteme eklenmemiş olmalı

  # TC-COMP-01-009
  @edge-case @company
  Senaryo: TC-COMP-01-009 - Aynı fatura numarası ile mükerrer yükleme denemesi
    Diyelim ki daha önce yüklenmiş bir fatura numarası mevcut
      | existingInvoiceNo |
      | COMP-DUP-001      |
    Eğer ki aynı fatura numarası ile tekrar yükleme yapılırsa
    O zaman mükerrer fatura hatası alınmalı
    Ve sisteme ikinci kayıt oluşturulmamış olmalı

  # TC-COMP-01-010
  @smoke @upload-request @company
  Senaryo: TC-COMP-01-010 - Tedarikçi bazlı yükleme talebi oluşturma
    Eğer ki tedarikçi alıcıya yükleme talebi gönderirse
      | buyerTaxNo  | requestedAmount | currency |
      | 1234567893  | 10000           | TRY      |
    O zaman yükleme talebi başarıyla oluşturulmuş olmalı
    Ve talep "REQUESTED" durumunda listelenmeli

  # TC-COMP-01-011
  @smoke @company
  Senaryo: TC-COMP-01-011 - Yüklenen faturanın listede görünmesi ve detay kontrolü
    Eğer ki tedarikçi geçerli XML fatura bilgileri ile fatura yüklerse
      | invoiceNo          | supplierTaxNo | invoiceAmount | invoiceType |
      | COMP-DETAIL-001    | 1234567893    | 7500          | E_FATURA    |
    O zaman fatura başarıyla yüklenmiş olmalı
    Ve fatura listesinde "PENDING_APPROVAL" durumunda görünmeli
    Ve fatura detayında fatura no doğru görünmeli
    Ve fatura detayında tutar doğru görünmeli
