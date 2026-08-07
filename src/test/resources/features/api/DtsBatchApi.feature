# DTS_API-001: DTS (Doğrudan Tahsilat Sistemi) — Ticari İşletme Entegrasyon REST API'si
# Base path: /app/api/integration/company/v0 (FaturalabAPI'nin buyer/v0 kalıbının aynısı).
# Akış: authenticate -> batch/start -> batch/upload -> batch/end.
# Kimlik: Test Otomasyon Sadece Tedarikçi (company.id=998, izole entegrasyon test firması,
# dev.properties > dts.api.*). Her koşumda benzersiz dtsReferenceNo/batchNo/invoiceNo üretilir
# (statik fixture ikinci koşuda EXIST_BATCH_NO / mükerrer kayıt hatası verir).
# 2026-08-06 canlı doğrulama (Python prob): dts.id=121 -> status=DRAFT, batch.id=180, invoice.id=713226.
Feature: DTS Batch Entegrasyon API

  @api @dts @regression @dts-api-001
  Scenario: DTS_API-001 - DTS batch akışı authenticate/start/upload/end ile başarıyla tamamlanmalı
    Given "TEST" entegrasyon kullanıcısı ile DTS API'sinde kimlik doğrulanır
    When yeni bir DTS referans numarası ile DTS batch işlemi başlatılır
    And 1 fatura içeren batch DTS'e yüklenir
    And DTS batch işlemi bitirilir
    Then DTS batch akışının her adımı başarılı mesajla sonuçlanmalı ve DTS durumu Taslak (DRAFT) olmalı
