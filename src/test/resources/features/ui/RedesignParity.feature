# Redesign Parity — Dasein yeni tasarım geçişi (Sprint 130 "V2 | Ortak": #5979 #5980 #5981)
#
# K1 Fonksiyonel parmak izi: eski tasarımın (dev.rakbank.faturalab.ae, #5979 öncesi jar) dondurulmuş envanteri
#    testdata/parity/<ekran>.json'da; yeni ekranda eksik alan/buton, kaybolan zorunluluk/maxlength/password tipi
#    ve "buton → klavyeyle erişilemeyen metin" dönüşümü FAIL'dir. Tenant farkları (logo, destek maili) allowlist'te.
# K4 Erişilebilirlik: axe-core WCAG 2.2 AA; referans = eski tasarımın ihlal listesi (kural bazında —
#    yeni critical/serious kural veya aynı kuralda artan öğe sayısı FAIL).
#
# Koşum (yeni):  mvn test -Dtest=UITestRunner -Denv=dev "-Dcucumber.filter.tags=@parity" -Dfaturalab.open.reports=false
# Baseline al:   mvn test -Dtest=UITestRunner -Denv=dev "-Dcucumber.filter.tags=@parity-capture" -Dfaturalab.open.reports=false
#                → target/parity/captured/<ekran>.json gözden geçirilip testdata/parity/<ekran>.json > baseline'a dondurulur.
# Rapor:         target/parity/<ekran>_rapor.md + cucumber raporuna ek.
# Not: Hiçbir adım form GÖNDERMEZ (kayıt/şifre sıfırlama/giriş yapılmaz) — sadece ekran envanteri ölçülür.

@ui @redesign
Feature: Redesign parity — giriş öncesi ekranlar eski tasarımın işlevini korur

  @parity
  Scenario Outline: "<ekran>" yeni tasarımda eski tasarımın alan, aksiyon ve erişim kurallarını korur
    Given "<ekran>" ekranı "yeni" tasarım ortamında açılır
    When ekranın UI parmak izi alınır
    And ekranın erişilebilirlik taraması yapılır
    Then parmak izi "<ekran>" baseline'ı ile karşılaştırılır
    And erişilebilirlik ihlalleri "<ekran>" baseline'ı ile karşılaştırılır
    And parity bulgularında FAIL yoktur

    Examples:
      | ekran           |
      | giris           |
      | kayit_adim1     |
      | sifremi_unuttum |

  @parity-capture
  Scenario Outline: "<ekran>" eski tasarım baseline adayı yakalanır
    Given "<ekran>" ekranı "eski" tasarım ortamında açılır
    When ekranın UI parmak izi alınır
    And ekranın erişilebilirlik taraması yapılır
    Then ölçüm baseline adayı olarak kaydedilir

    Examples:
      | ekran           |
      | giris           |
      | kayit_adim1     |
      | sifremi_unuttum |
