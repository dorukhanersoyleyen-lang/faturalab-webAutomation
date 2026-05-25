# Faturalab API Test Automation

Bu proje Faturalab API'leri için oluşturulmuş otomasyon test framework'üdür.

## 🚀 Özellikler

- **Environment Manager**: JSON environment dosyalarını otomatik okur
- **Parametrik Testler**: Birden fazla environment ile testleri çalıştırır
- **BDD Yaklaşımı**: Cucumber ile Türkçe feature dosyaları
- **Comprehensive Coverage**: Tüm fatura flow'unu kapsar
- **Data Validation**: Boş parametreler, geçersiz değerler test edilir
- **Real API Integration**: Gerçek Faturalab API endpoint'leri kullanır

## 📁 Proje Yapısı

```
src/
├── main/java/com/faturalab/automation/
│   ├── api/
│   │   └── FaturalabAPI.java              # Ana API sınıfı
│   ├── config/
│   │   └── EnvironmentManager.java        # Environment yönetimi
│   ├── models/                            # Request/Response modelleri
│   │   ├── UploadInvoiceRequest.java
│   │   ├── InvoiceHistoryRequest.java
│   │   ├── DeleteInvoiceRequest.java
│   │   ├── AuthenticateRequest.java
│   │   └── ApiResponse.java
│   └── utils/
│       └── InvoiceTestDataGenerator.java  # Test data üretici
│
├── test/java/com/faturalab/automation/
│   ├── runners/
│   │   └── FaturaAPITestRunner.java       # Test çalıştırıcısı
│   └── stepdefinitions/
│       └── FaturaAPISteps.java            # Cucumber step definitions
│
└── test/resources/
    ├── features/
    │   └── FaturaUploadFlow.feature       # Türkçe BDD senaryoları
    └── config/                            # Environment JSON dosyaları
        ├── dev.faturalab.buyer.migros.postman_environment.json
        ├── dev.faturalab.buyer.a101.postman_environment.json
        └── ...
```

## 🎯 Test Edilen Flow

### Ana Flow: Fatura Yükleme ve Silme
1. **Authentication** - Kullanıcı kimlik doğrulaması
2. **Upload Invoice** - Fatura yükleme
3. **Invoice History** - Faturanın listede olduğunu doğrulama
4. **Delete Invoice** - Faturayı silme
5. **Invoice History** - Silindiğini doğrulama

### Validasyon Testleri
- Boş parametreler testi
- Geçersiz miktarlar (0, negatif)
- Geçersiz tarihler (geçmiş, tatil günleri)
- Fatura tipi validasyonları:
  - E-Fatura: hashCode zorunlu
  - E-Arşiv: taxExclusiveAmount zorunlu
  - Paper: İlave gereksinim yok

## 🔧 Nasıl Çalıştırılır

### 1. Tek Environment ile Test
```bash
# Migros environment ile testleri çalıştır
mvn clean test -Dtest=FaturaAPITestRunner -Dcucumber.filter.tags="@api and @fatura"
```

### 2. Tüm API Testlerini Çalıştır
```bash
# Faturalab API testNG suite ile
mvn clean test -DsuiteXmlFile=faturalab-api-tests.xml
```

### 3. Belirli Tag ile Testler
```bash
# Sadece negative testler
mvn clean test -Dtest=FaturaAPITestRunner -Dcucumber.filter.tags="@negative"

# Sadece validation testleri
mvn clean test -Dtest=FaturaAPITestRunner -Dcucumber.filter.tags="@validation"

# Invoice type testleri
mvn clean test -Dtest=FaturaAPITestRunner -Dcucumber.filter.tags="@invoiceTypes"
```

### 4. Belirli Environment ile
Environment'lar feature dosyasında parametre olarak geçiliyor:
- `dev.faturalab.buyer.migros`
- `dev.faturalab.buyer.a101`
- `dev.faturalab.buyer.carrefoursa`
- `dev.faturalab.buyer.hepsiburada`

## 🧪 Test Senaryoları

### ✅ Pozitif Testler
```gherkin
Senaryo Taslağı: Fatura yükleme ve silme flow'u - <environment>
  Diyelim ki "dev.faturalab.buyer.migros" ortamı kullanılıyor
  Ve kullanıcı kimlik doğrulaması yapıldı
  Eğer ki geçerli fatura bilgileri ile fatura yüklerse
  O zaman fatura başarıyla yüklenmiş olmalı
  Ve fatura geçmişinde faturası görünmeli
  Eğer ki faturası silinirse
  O zaman fatura başarıyla silinmiş olmalı
```

### ❌ Negatif Testler
- Boş parametrelerle fatura yükleme
- Sıfır ve negatif miktarlar
- Geçersiz tarihler
- Zorunlu alanların eksik olması

## 📊 Raporlama

Testler çalıştıktan sonra raporlar şurada oluşur:

### Cucumber Reports
```bash
target/cucumber-reports/fatura-api-tests.html
```

### Allure Reports
```bash
mvn allure:serve
```

## 🔍 Environment Dosyaları

Her environment dosyası şu parametreleri içerir:
```json
{
  "values": [
    {"key": "host", "value": "https://dev.faturalab.com/api/migros"},
    {"key": "apiKey", "value": "API_KEY"},
    {"key": "alias", "value": "MIGROS"},
    {"key": "password", "value": "PASSWORD"},
    {"key": "taxNumber", "value": "TAX_NUMBER"},
    {"key": "userEmail", "value": "EMAIL"},
    {"key": "sessionId", "value": ""}
  ]
}
```

## 🛠️ Geliştirme

### Yeni Environment Ekleme
1. `src/test/resources/config/` altına yeni JSON dosyası ekle
2. `EnvironmentManager.getBuyerEnvironments()` metoduna environment adını ekle
3. Feature dosyasında Examples tablosuna yeni satır ekle

### Yeni Test Senaryosu Ekleme
1. `FaturaUploadFlow.feature` dosyasına yeni senaryo ekle
2. `FaturaAPISteps.java` dosyasına step definition'ları ekle
3. Gerekirse yeni model sınıfları oluştur

### Yeni Endpoint Ekleme
1. `FaturalabAPI.java` dosyasına yeni method ekle
2. Request/Response model sınıfları oluştur
3. Step definition'ları implement et

## 🐛 Troubleshooting

### Authentication Hatası
- Environment dosyasındaki credentials'ları kontrol et
- API endpoint'in doğru olduğunu kontrol et

### Network Hatası
- VPN bağlantını kontrol et
- Base URL'nin erişilebilir olduğunu kontrol et

### Test Data Hatası
- Invoice numarasının unique olduğunu kontrol et
- Tarih formatlarının doğru olduğunu kontrol et

## 📝 Log'lar

Detaylı log'lar için:
```bash
tail -f target/logs/faturalab-api-tests.log
```

Her API çağrısı, request/response detayları ve hata durumları log'lanır.

---

## 🎉 Örnek Test Çıktısı

```
[INFO] Running FaturaAPITestRunner
[INFO] Initializing environment: dev.faturalab.buyer.migros
[INFO] Authentication successful. Session ID: abc123...
[INFO] Uploading invoice: TEST2025000001_1234567890 with amount: 1000.0
[INFO] Invoice uploaded successfully: TEST2025000001_1234567890
[INFO] Checking invoice history for: TEST2025000001_1234567890
[INFO] Invoice found in history: TEST2025000001_1234567890
[INFO] Deleting invoice: TEST2025000001_1234567890
[INFO] Invoice deleted successfully: TEST2025000001_1234567890
[INFO] Invoice successfully removed from history
```

Bu framework ile tüm buyer environment'ları için fatura yükleme flow'unu otomatik test edebilirsiniz! 🚀 