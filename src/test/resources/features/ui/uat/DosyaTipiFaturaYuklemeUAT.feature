# Farklı dosya tipleriyle fatura yükleme (ALICI tarafı).
# Kabul edilen uzantılar kaynak koddan alındı (Convert.java / AddInvoiceListDialog):
#   xls, xlsx, csv, xml, zip  -> kabul
#   diğer (ör. txt)           -> red: "Only files with xls, xlsx, csv, xml, zip extensions can be uploaded."
# Test verisi her koşumda runtime üretilir (TzfInvoiceExcelGenerator):
#   XLS/XLSX -> E-Fatura liste (benzersiz no + hash), imza gerektirmez -> deterministik.
#
# KAPSAM DIŞI (imza engeli):
#  - Tekil geçerli XML: E-Fatura/E-Arşiv imza doğrulaması ister (InvoiceValidation).
#  - ZIP: Alıcı dialog'u ZIP'i isXmlZipFile=TRUE ile açar (AddInvoiceListDialog:332),
#    yani içindeki her dosyayı imzalı XML olarak parse eder — görsel/PAPER ZIP akışı
#    ALICIDA YOK, tedarikçi tarafındadır (CompanyMatchInvoiceListDialog). İmzalı e-fatura
#    test verimiz olmadığından alıcıda XML ve ZIP başarı testi yazılamaz; format-kabul
#    matrisinde reddedilmeyen taraftalar, negatif taraf .txt ile doğrulanır.
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
