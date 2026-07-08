# Tedarikçi tarafı farklı dosya tipleriyle fatura yükleme.
# Canlı DOM + kaynak kod (CompanyAddInvoiceDialog): "FATURA YÜKLE / TALEP ET" dialogu
# "Fatura Türü" radyosu ile ayrılır:
#   Kağıt Fatura -> jpg, jpeg, png, gif, bmp, pdf, doc, docx  (İMZASIZ = PAPER, deterministik)
#   E-Fatura/... -> xls, xlsx, xml, zip                        (xml/zip imza ister -> kapsam dışı)
# Kağıt fatura görsel/PDF akışı imza gerektirmediğinden gerçek yükleme test edilebilir.
# Reddedilen uzantı mesajı (CompanyAddInvoiceDialog.107):
#   "Only jpg, jpeg, png, gif, bmp, pdf, doc and docx files can be uploaded."
#
# ⛔ BLOKE — Defect #5813 (https://pm5471.faturalab.com/work_packages/5813):
# Adres/posta kodu/faks verisi eksik firmalarda (Test Otomasyon Sadece Tedarikçi dahil)
# görsel yüklenince CompanyPaperInvoiceDialog NPE veriyor, detay modalı hiç açılmıyor.
# Bu senaryolar @regression scope'unda fix-doğrulama testi olarak bekler; fix gelince:
#  1) Detay modalı adımları eklenecek (görsel sonrası: fatura no/tutar gir + Kaydet)
#  2) @regression kaldırılıp günlük UAT kapsamına terfi edecek.
Feature: Tedarikçi Farklı Dosya Tipleriyle Fatura Yükleme UAT
  Tedarikçi, kağıt fatura türünde desteklenen görsel/PDF formatlarıyla fatura
  yükleyebilmeli; desteklenmeyen uzantılar reddedilmeli.

  @ui @uat @regression @tedarikci-dosya-tipi @happy-path @dosya-png
  Scenario: TD-001 - Tedarikçi kağıt fatura görseli (.png) yükleyebilmeli
    Given tedarikçi kağıt fatura için "PNG" dosyası hazırlanır
    When admin TZF tedarikçi kullanıcısına geçiş yapar
    And tedarikçi kağıt fatura türüyle hazırlanan dosya yüklenir
    Then tedarikçi dosyası başarıyla yüklenmiş olmalı

  @ui @uat @regression @tedarikci-dosya-tipi @happy-path @dosya-pdf
  Scenario: TD-002 - Tedarikçi kağıt fatura belgesi (.pdf) yükleyebilmeli
    Given tedarikçi kağıt fatura için "PDF" dosyası hazırlanır
    When admin TZF tedarikçi kullanıcısına geçiş yapar
    And tedarikçi kağıt fatura türüyle hazırlanan dosya yüklenir
    Then tedarikçi dosyası başarıyla yüklenmiş olmalı

  @ui @uat @regression @tedarikci-dosya-tipi @negatif @dosya-gecersiz
  Scenario: TD-003 - Tedarikçi kağıt faturada desteklenmeyen uzantı (.txt) reddedilmeli
    Given tedarikçi kağıt fatura için "GECERSIZ" dosyası hazırlanır
    When admin TZF tedarikçi kullanıcısına geçiş yapar
    And tedarikçi kağıt fatura türüyle hazırlanan dosya yüklenir
    Then tedarikçi dosya tipi reddedildiği bildirimi gösterilmeli
