# DTS-001: DTS (Doğrudan Tahsilat Sistemi) — konsolide uçtan-uca senaryo
# Tedarikçi: Test Otomasyon Sadece Tedarikçi (company.id=998, VKN 4050604050) — izole
#   entegrasyon test firması (Petek A.Ş./company.id=815 keşif dönemi artık kullanılmıyor).
# ⚠️ Terminoloji: OP task'larda "Doğrudan Tahsilat Yönetimi" geçse de gerçek UI/kod adı
#   "Doğrudan Tahsilat Sistemi"dir (CompanyDashboardMenuView.30).
# Test verisi statik/tükenmiş bir referans numarasına (eski DTS_10 / DTS_110000) bağımlı
#   DEĞİL — her koşumda CompanyBatchApi REST entegrasyonu (authenticate → batch/start →
#   batch/upload → batch/end) ile taze bir DTS taslak (DRAFT) kaydı üretilir, ardından o
#   kayıt UI'da tedarikçi tarafından bulunup ayar → hesaplama → "Başlat" ile ilerletilir.
# Kapsam: navigasyon + liste doğrulama + CompanyDtsSettingsDialog ("Devam Et") +
#   CompanyDetailDtsSettingsDialog ("Başlat") + statü CALCULATING/PENDING geçişi +
#   Company onayı (CompanyDtsAllocationDialog "Onayla" → SEND/Gönderiliyor) +
#   Admin nihai onayı (AdminDtsDialog "Onayla" → COMPLETED/Tamamlandı) + salt-okunur DB çapraz
#   doğrulama (dbsdocs kaydının doğru finansal kuruma/doğru tutara oluştuğu, DtsDbAssertions ile).
#   "Başlat" / Company "Onayla" / Admin "Onayla" hepsi GERİ ALINAMAZ durum geçişleri
#   (kasıtlı, her koşumda taze API verisi üzerinde koşulur).
Feature: DTS İşlemi UAT

  @ui @uat @regression @dts @dts-001
  Scenario: DTS-001 - API ile üretilen taslak DTS kaydı Başlat → Company onayı → Admin nihai onayı ile Tamamlandı'ya ilerlemeli
    Given API ile taze bir DTS taslak kaydı üretilir
    And dts tedarikcisi olarak giriş yapılır
    When tedarikçi "Doğrudan Tahsilat Sistemi" menüsüne gider
    Then üretilen DTS kaydı Taslak durumunda listede görünmeli
    When filtrelenmiş taslak DTS kaydı için "Devam Et" butonuna tıklanır
    Then "Doğrudan Tahsilat Ayarı" diyaloğu açılmalı
    When Doğrudan Tahsilat Ayarı diyaloğunda "Devam Et" ile ilerlenir
    Then "Yeni Doğrudan Tahsilat Bilgileri" diyaloğu açılmalı
    When "Başlat" butonuna tıklanıp hesaplama onaylanır
    Then üretilen DTS kaydının statüsü Hesaplanıyor'a geçmeli
    When tedarikçi filtrelenmiş DTS kaydı için "Gözat" butonuna tıklar
    Then "Doğrudan Tahsilat Bilgileri" diyaloğu açılmalı
    When tedarikçi "Onayla" butonuna tıklayıp DTS kaydını onaylar
    Then üretilen DTS kaydının statüsü Gönderiliyor'a geçmeli
    When admin olarak giriş yapılır
    And admin Raporlar "TAHSİLAT İŞLEMLERİ" sekmesine gider
    And admin filtrelenmiş DTS kaydı için "Gözat" butonuna tıklar
    Then admin "Doğrudan Tahsilat Bilgileri" diyaloğunu görmeli
    When admin "Onayla" butonuna tıklayıp DTS kaydını nihai onaylar
    Then üretilen DTS kaydının statüsü Tamamlandı'ya geçmeli
    Then üretilen faturanın DBS belgesi doğru finansal kuruma doğru tutarla oluşmalı
