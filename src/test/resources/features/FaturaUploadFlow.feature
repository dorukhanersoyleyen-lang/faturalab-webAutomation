# language: tr
@api @fatura
Özellik: Fatura Yükleme Flow'u
  
  Bu özellik ALBC firma için fatura yükleme, listeleme ve silme flow'unu test eder.
  
  @smoke @albc @buyer
  Senaryo: ALBC - Basit fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki geçerli fatura bilgileri ile fatura yüklerse
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | ALBC-2025-000001 | 1234567893    | 1000          | E_FATURA    |
    O zaman fatura başarıyla yüklenmiş olmalı
    Ve fatura geçmişinde faturası görünmeli
    Eğer ki faturası silinirse
    O zaman fatura başarıyla silinmiş olmalı
    Ve fatura geçmişinde faturası görünmemeli
  
  @negative @albc @buyer
  Senaryo: ALBC - Boş parametrelerle fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki boş parametrelerle fatura yüklenmeye çalışılırsa
    O zaman hata mesajı alınmalı
    Ve fatura yüklenmemiş olmalı

  @validation @albc @buyer
  Senaryo: ALBC - Geçersiz miktarlarla fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki geçersiz miktarla fatura yüklerse
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | ALBC-INVALID-001 | 1234567893    | 0             | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve fatura yüklenmemiş olmalı

  @invoiceTypes @albc @buyer
  Senaryo: ALBC - E-Arşiv fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki E-Arşiv fatura bilgileri ile fatura yüklerse
      | invoiceNo      | supplierTaxNo | invoiceAmount | invoiceType |
      | ALBC-ARSIV-001 | 1234567893    | 1000          | E_ARSIV     |
    O zaman fatura başarıyla yüklenmiş olmalı