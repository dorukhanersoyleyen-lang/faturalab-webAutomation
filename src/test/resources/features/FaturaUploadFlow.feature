# language: tr
@api @fatura
Özellik: Fatura Yükleme Flow'u
  
  Bu özellik ALBC ve diğer firmalar için fatura yükleme, listeleme ve silme flow'unu test eder.
  
  @smoke @albc @buyer
  Senaryo: ALBC - Basit fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki geçerli fatura bilgileri ile fatura yüklerse
      | invoiceNo     | ALBC-2025-000001 |
      | supplierTaxNo | 1234567893       |
      | invoiceAmount | 1000.00          |
      | invoiceType   | E_FATURA         |
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
      | invoiceNo     | ALBC-INVALID-001 |
      | supplierTaxNo | 1234567893       |
      | invoiceAmount | 0.00             |
      | invoiceType   | E_FATURA         |
    O zaman hata mesajı alınmalı
    Ve fatura yüklenmemiş olmalı

  @invoiceTypes @albc @buyer
  Senaryo: ALBC - E-Arşiv fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki E-Arşiv fatura bilgileri ile fatura yüklerse
      | invoiceNo     | ALBC-ARSIV-001 |
      | supplierTaxNo | 1234567893     |
      | invoiceAmount | 1000.00        |
      | invoiceType   | E_ARSIV        |
    O zaman fatura başarıyla yüklenmiş olmalı

  @smoke @bank @bien
  Senaryo: BIEN - Basit fatura yükleme testi
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki geçerli fatura bilgileri ile fatura yüklerse
      | invoiceNo     | BIEN2025000001 |
      | supplierTaxNo | 1234567890     |
      | invoiceAmount | 1000.00        |
      | invoiceType   | E_FATURA       |
    O zaman fatura başarıyla yüklenmiş olmalı
  
  @negative @bank @bien
  Senaryo: BIEN - Boş parametrelerle fatura yükleme testi
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki boş parametrelerle fatura yüklenmeye çalışılırsa
    O zaman hata mesajı alınmalı
    Ve fatura yüklenmemiş olmalı