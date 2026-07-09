# Tedarikçi tarafı farklı dosya tipleriyle fatura yükleme.
# Canlı DOM + kaynak kod (CompanyAddInvoiceDialog): "FATURA YÜKLE / TALEP ET" dialogu
# "Fatura Türü" radyosu ile ayrılır:
#   Kağıt Fatura -> jpg, jpeg, png, gif, bmp, pdf, doc, docx  (İMZASIZ = PAPER, deterministik)
#   E-Fatura/... -> xls, xlsx, xml, zip                        (xml/zip imza ister -> kapsam dışı)
# Kağıt fatura görsel/PDF akışı imza gerektirmediğinden gerçek yükleme test edilebilir.
# Reddedilen uzantı mesajı (CompanyAddInvoiceDialog.107):
#   "Only jpg, jpeg, png, gif, bmp, pdf, doc and docx files can be uploaded."
#
# Hedef tedarikçi: EFG Gıda (adres/posta kodu verisi DOLU) — "tedarikci olarak giriş yapılır".
# NOT: Test Otomasyon Sadece Tedarikçi ile bu akış defect #5813 yüzünden ÇALIŞMAZ
# (adres verisi eksik firmada CompanyPaperInvoiceDialog NPE). EFG'de akışın tamamı:
# görsel yüklenir -> fatura DETAY MODALI açılır -> zorunlu alanlar doldurulur -> Kaydet.
Feature: Tedarikçi Farklı Dosya Tipleriyle Fatura Yükleme UAT
  Tedarikçi, kağıt fatura türünde desteklenen görsel/PDF formatlarıyla fatura
  yükleyebilmeli; desteklenmeyen uzantılar reddedilmeli.

  @ui @uat @regression @tedarikci-dosya-tipi @happy-path @dosya-png
  Scenario: TD-001 - Tedarikçi kağıt fatura görseli (.png) yükleyebilmeli
    Given tedarikçi kağıt fatura için "PNG" dosyası hazırlanır
    Given tedarikci olarak giriş yapılır
    And tedarikçi kağıt fatura türüyle hazırlanan dosya yüklenir
    And kağıt fatura detay modalında zorunlu alanlar doldurulur ve kaydedilir
    Then tedarikçi dosyası başarıyla yüklenmiş olmalı

  @ui @uat @regression @tedarikci-dosya-tipi @happy-path @dosya-pdf
  Scenario: TD-002 - Tedarikçi kağıt fatura belgesi (.pdf) yükleyebilmeli
    Given tedarikçi kağıt fatura için "PDF" dosyası hazırlanır
    Given tedarikci olarak giriş yapılır
    And tedarikçi kağıt fatura türüyle hazırlanan dosya yüklenir
    And kağıt fatura detay modalında zorunlu alanlar doldurulur ve kaydedilir
    Then tedarikçi dosyası başarıyla yüklenmiş olmalı

  @ui @uat @regression @tedarikci-dosya-tipi @negatif @dosya-gecersiz
  Scenario: TD-003 - Tedarikçi kağıt faturada desteklenmeyen uzantı (.txt) reddedilmeli
    Given tedarikçi kağıt fatura için "GECERSIZ" dosyası hazırlanır
    Given tedarikci olarak giriş yapılır
    And tedarikçi kağıt fatura türüyle hazırlanan dosya yüklenir
    Then tedarikçi dosya tipi reddedildiği bildirimi gösterilmeli

  @ui @uat @regression @tedarikci-dosya-tipi @happy-path @dosya-xml
  Scenario: TD-004 - Tedarikçi dummy imzalı tekil XML fatura yükleyebilmeli
    Given tedarikçi için 1 adet dummy imzalı XML fatura hazırlanır
    Given tedarikci olarak giriş yapılır
    And tedarikçi E-Fatura türüyle hazırlanan dosya yüklenir
    Then tedarikçi dosyası başarıyla yüklenmiş olmalı

# TD-005 (Tedarikçi XML'li ZIP) KAPSAM DIŞI — kaldırıldı:
#   Tedarikçi E-Fatura yolunda saf-XML ZIP yüklendiğinde dialog yalnızca "Kapat"
#   butonu gösteriyor; "Yükle"/"Kaydet" akışı tetiklenmiyor (canlı koşumda doğrulandı,
#   2026-07-09). Yani bu ZIP tipi tedarikçi E-Fatura radyosuyla işlenmiyor.
#   Tedarikçi XML kabiliyeti TD-004 (tekil dummy imzalı XML) ile kanıtlı ve yeşil.
#   XML'li ZIP asıl olarak ALICI "Fatura Yükle" (isXmlZipFile) akışına aittir; ancak
#   şablon tarafları (EFG/ALBC) dev'de alıcı firma eşleşmesi gerektirir → ayrı iş.
