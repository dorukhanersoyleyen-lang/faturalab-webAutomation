# Farklı dosya tipleriyle fatura yükleme (ALICI tarafı).
# Kabul edilen uzantılar kaynak koddan alındı (Convert.java / AddInvoiceListDialog):
#   xls, xlsx, csv, xml, zip  -> kabul
#   diğer (ör. txt)           -> red: "Only files with xls, xlsx, csv, xml, zip extensions can be uploaded."
# Test verisi her koşumda runtime üretilir (TzfInvoiceExcelGenerator):
#   XLS/XLSX -> E-Fatura liste (benzersiz no + hash), imza gerektirmez -> deterministik.
#
# XML/ZIP (dummy imza ile — kullanıcı onayı: dev ortamı dummy imzayı kabul eder):
#  - Şablon: testdata/test-invoice.xml (dev'in kabul ettiği KANITLI: TC-COMP-01-001 geçiyor).
#  - Şablon tarafları SABİT: tedarikçi EFG (3960656675), alıcı ALBC (3456789010) —
#    bu yüzden XML/ZIP senaryoları ALBC alıcısıyla koşar ("alici olarak giriş yapılır"),
#    TZF alıcısıyla DEĞİL (buyer TIN eşleşmezse AddInvoiceListDialog.106 hatası).
# NOT: XML ve XML'li ZIP başarı testleri ALICI tarafında değil TEDARİKÇİ tarafında
#   (TedarikciDosyaTipiUploadUAT — E-Fatura radyosu): dev, tedarikçi XML upload'ında
#   test-invoice.xml şablonunu (EFG tedarikçili) kabul ediyor. Alıcı XML upload farklı
#   davrandığı ve dev'de firma eşleşmesi gerektirdiği için burada kapsanmaz.
Feature: Farklı Dosya Tipleriyle Fatura Yükleme UAT
  Alıcı, desteklenen Excel formatlarıyla (.xls / .xlsx) fatura listesi yükleyebilmeli;
  desteklenmeyen uzantılar reddedilmeli.

  @ui @uat @dosya-tipi @happy-path @dosya-xls
  Scenario: DT-001 - Alıcı .xls Excel listesiyle fatura yükleyebilmeli
    Given TZF senaryosu için 2 adet fatura içeren "XLS" dosyası hazırlanır
    When admin TZF alıcı kullanıcısına geçiş yapar
    And alıcı ekranında hazırlanan dosya yüklenir
    Then dosya başarıyla yüklenmiş olmalı

  @ui @uat @dosya-tipi @happy-path @dosya-xlsx
  Scenario: DT-002 - Alıcı .xlsx Excel listesiyle fatura yükleyebilmeli
    Given TZF senaryosu için 2 adet fatura içeren "XLSX" dosyası hazırlanır
    When admin TZF alıcı kullanıcısına geçiş yapar
    And alıcı ekranında hazırlanan dosya yüklenir
    Then dosya başarıyla yüklenmiş olmalı

  @ui @uat @dosya-tipi @negatif @dosya-gecersiz
  Scenario: DT-004 - Desteklenmeyen uzantı (.txt) reddedilmeli
    Given TZF senaryosu için 2 adet fatura içeren "GECERSIZ" dosyası hazırlanır
    When admin TZF alıcı kullanıcısına geçiş yapar
    And alıcı ekranında hazırlanan dosya yüklenir
    Then dosya tipi reddedildiği bildirimi gösterilmeli
