# language: tr
@api @faktoring @bordro
Özellik: Finansman Bordro (Teklif Talebi) Yönetimi Akışı

  Bu özellik finansman kurumunun bordro (teklif talebi) onaylama ve iptal etme
  işlemlerini test eder.
  Bordro; tedarikçi kazanan teklifi kabul ettiğinde otomatik oluşur.
  Finansman, tek tek fatura onaylamaz — BORDRO onaylar/iptal eder.
  Ekran: Teklif Talebi Yönetimi (Finansman Paneli)
  Test Case Referansları: TC-FACT-03-001 ... TC-FACT-03-006
  QA Doküman: olustur/qa/test-cases/TC-FACT-03-bordro-yonetimi.md

  # TC-FACT-03-001
  @smoke @happy-path @faktoring @kritik
  Senaryo: TC-FACT-03-001 - Bordro onaylama (mutlu yol)
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Ve tedarikçi kazanan teklifi kabul etmiş ve bir bordro oluşmuş
      | bordroNo      | ticariIsletme  | alici                    |
      | A2025_73387   | EFG Gıda A.Ş. | ALBC Marketler Zinciri A.Ş. |
    Eğer ki finansman teklif talebi listesinde bordroyu onaylarsa
    O zaman bordro durumu onaylandı olarak güncellenmeli
    Ve bordro listesinde durum değişikliği görünmeli

  # TC-FACT-03-002
  @smoke @faktoring
  Senaryo: TC-FACT-03-002 - Bordro iptali
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Ve onay bekleyen bir bordro mevcut
      | bordroNo    |
      | A2025_73388 |
    Eğer ki finansman teklif talebini iptal ederse
    O zaman bordro durumu iptal olarak güncellenmeli
    Ve iptal edilen bordro "Günlük Teklif İptal Talebi" sekmesinde görünmeli

  # TC-FACT-03-003
  @smoke @faktoring
  Senaryo: TC-FACT-03-003 - Tüm bordroları iptal etme
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Ve birden fazla onay bekleyen bordro mevcut
    Eğer ki finansman "Tümünü İptal Et" aksiyonunu kullanırsa
    O zaman listedeki tüm bekleyen bordrolar iptal durumuna geçmeli

  # TC-FACT-03-004
  @smoke @faktoring
  Senaryo: TC-FACT-03-004 - Bordro detay görüntüleme (GÖZAT)
    Diyelim ki "dev.faturalab.bank.bien" ortamı kullanılıyor
    Ve finansman kullanıcısı ile kimlik doğrulaması yapıldı
    Ve bir bordro mevcut
      | bordroNo    |
      | A2025_73387 |
    Eğer ki finansman bordroya "GÖZAT" ile bakarsa
    O zaman bordro detay ekranı açılmalı
    Ve bordro bilgileri doğru gösterilmeli
