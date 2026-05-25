# language: tr
@api @fatura @v2
Özellik: Fatura Yükleme Flow'u V2
  
  Bu özellik ALBC firma için V2 API üzerinde fatura yükleme, listeleme ve silme flow'unu test eder.
  
  @smoke @albc @buyer @v2
  Senaryo: ALBC V2 - Basit fatura yükleme testi
    Diyelim ki "dev.faturalab.v2.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki geçerli fatura bilgileri ile fatura yüklerse
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | ALBC-V2-000001   | 1234567893    | 1000          | E_FATURA    |
    O zaman fatura başarıyla yüklenmiş olmalı
    Ve fatura geçmişinde faturası görünmeli
    Eğer ki faturası silinirse
    O zaman fatura başarıyla silinmiş olmalı
    Ve fatura geçmişinde faturası görünmemeli
  
  @negative @albc @buyer @v2
  Senaryo: ALBC V2 - Boş parametrelerle fatura yükleme testi
    Diyelim ki "dev.faturalab.v2.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki boş parametrelerle fatura yüklenmeye çalışılırsa
    O zaman hata mesajı alınmalı
    Ve fatura yüklenmemiş olmalı

  @validation @albc @buyer @v2
  Senaryo: ALBC V2 - Geçersiz miktarlarla fatura yükleme testi
    Diyelim ki "dev.faturalab.v2.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki geçersiz miktarla fatura yüklerse
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType |
      | ALBC-V2-INV-001  | 1234567893    | 0             | E_FATURA    |
    O zaman hata mesajı alınmalı
    Ve fatura yüklenmemiş olmalı

  @invoiceTypes @albc @buyer @v2
  Senaryo: ALBC V2 - E-Arşiv fatura yükleme testi
    Diyelim ki "dev.faturalab.v2.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki E-Arşiv fatura bilgileri ile fatura yüklerse
      | invoiceNo      | supplierTaxNo | invoiceAmount | invoiceType |
      | ALBC-V2-ARSIV  | 1234567893    | 1000          | E_ARSIV     |
    O zaman fatura başarıyla yüklenmiş olmalı
