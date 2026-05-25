# language: tr
@api @auction
Özellik: Auction Fatura Yükleme ve Reddetme Flow'u
  
  Bu özellik ALBC firma için auction fatura yükleme, status kontrolü, reddetme ve reddedilen faturanın tekrar kontrolü flow'unu test eder.
  
  @smoke @albc @auction @happy-path
  Senaryo: ALBC - Auction fatura yükleme, status kontrolü, reddetme ve status kontrol testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki geçerli auction fatura bilgileri ile fatura yüklerse
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | auctionType |
      | ALBC-AUC-2025001 | 1234567893    | 2500          | E_FATURA    | STANDARD    |
    O zaman auction fatura başarıyla yüklenmiş olmalı
    Ve auction fatura status'ü kontrol edilmeli
    Ve auction fatura status'ü "DRAFT" olmalı
    Eğer ki auction faturası reddedilirse
    O zaman auction fatura reddetme işlemi başarıyla tamamlanmış olmalı
    Ve reddedilen auction fatura status'ü kontrol edilmeli
    Ve auction fatura status'ü "REJECTED" olmalı
    Ve auction fatura reddetme nedeni görünmeli

  @negative @albc @auction
  Senaryo: ALBC - Boş parametrelerle auction fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki boş parametrelerle auction fatura yüklenmeye çalışılırsa
    O zaman hata mesajı alınmalı
    Ve auction fatura yüklenmemiş olmalı

  @validation @albc @auction
  Senaryo: ALBC - Geçersiz auction tip ile fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki geçersiz auction tip ile fatura yüklerse
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | auctionType |
      | ALBC-AUC-INV001  | 1234567893    | 1500          | E_FATURA    | INVALID_TYPE |
    O zaman hata mesajı alınmalı
    Ve auction fatura yüklenmemiş olmalı

  @edge-case @albc @auction
  Senaryo: ALBC - Var olan invoice numarası ile auction fatura yükleme testi
    Diyelim ki "dev.faturalab.buyer.albc" ortamı kullanılıyor
    Ve kullanıcı kimlik doğrulaması yapıldı
    Eğer ki zaten var olan invoice numarası ile auction fatura yüklerse
      | invoiceNo        | supplierTaxNo | invoiceAmount | invoiceType | auctionType |
      | ALBC-AUC-DUP001  | 1234567893    | 3000          | E_FATURA    | STANDARD    |
    O zaman duplicate invoice hatası alınmalı
    Ve auction fatura yüklenmemiş olmalı 