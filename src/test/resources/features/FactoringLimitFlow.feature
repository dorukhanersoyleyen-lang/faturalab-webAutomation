# language: tr
@api @faktoring @limit
Özellik: Finansman Teklif Limit Kontrol Akışı

  Bu özellik finansman kuruluşunun teklif verirken uygulanan limit
  kontrollerini test eder.
  Test Case Referansları: TC-FACT-02-001 ... TC-FACT-02-005

  # TC-FACT-02-001
  @smoke @happy-path @faktoring
  Senaryo: TC-FACT-02-001 - Limit dahilinde otomatik teklif verme
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Ve finansmanın mevcut limiti yeterli
    Eğer ki finansman limit dahilinde bir tutarda teklif verirse
      | offerAmount | currency |
      | 10000       | TRY      |
    O zaman teklif başarıyla kaydedilmiş olmalı

  # TC-FACT-02-002
  @negative @kritik @faktoring
  Senaryo: TC-FACT-02-002 - Otomatik teklif sisteminde limit aşan teklif bloklanmalı
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Ve finansmanın kalan limiti 0 veya yetersiz
    Eğer ki finansman limitini aşan bir tutarda teklif vermeye çalışırsa
      | offerAmount | currency |
      | 99999999    | TRY      |
    O zaman teklif bloklanmış olmalı
    Ve yetersiz limit hata mesajı alınmalı

  # TC-FACT-02-003
  @edge-case @faktoring
  Senaryo: TC-FACT-02-003 - Manuel ihalede limit aşımı (uyarısız geçme kontrolü)
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Ve manuel ihale aktif durumda
    Eğer ki finansman manuel ihalede limitini aşan tutarda teklif verirse
      | offerAmount | currency |
      | 99999999    | TRY      |
    O zaman teklif işlemi tamamlanmış olmalı
    Ve herhangi bir blok mesajı görünmemeli

  # TC-FACT-02-004
  @edge-case @faktoring
  Senaryo: TC-FACT-02-004 - Limiti sıfır olan finansmanın teklif verme denemesi
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve limiti sıfır olan finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Eğer ki bu finansman herhangi bir tutarda teklif vermeye çalışırsa
    O zaman teklif işlemi reddedilmeli
    Ve "Limitiniz bulunmuyor" benzeri mesaj görünmeli

  # TC-FACT-02-005
  @smoke @faktoring
  Senaryo taslağı: TC-FACT-02-005 - Farklı limit senaryolarında teklif sonuçları
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Eğer ki finansman <offerAmount> tutarında teklif verirse
    O zaman sonuç <beklenenSonuc> olmalı

    Örnekler:
      | offerAmount | beklenenSonuc |
      | 1000        | BASARILI      |
      | 0           | HATA          |
      | -100        | HATA          |
