# Faturalab API Test Automation 🚀

Bu proje ALBC firması için Faturalab API'larının otomatik test edilmesi amacıyla geliştirilmiştir.

## 📋 Test Kategorileri

- **@smoke** - Temel fatura upload/delete testleri
- **@negative** - Boş parametreler ve hata senaryoları  
- **@validation** - Geçersiz değerler ve input validasyon
- **@invoiceTypes** - E-Fatura, E-Arşiv farklı fatura türleri

## 🏃‍♂️ Test Çalıştırma

### Basit Yöntem (Önerilen)
```bash
# Windows
run-tests.bat

# Mac/Linux  
./run-tests.sh
```

### Maven ile Çalıştırma
```bash
# Tüm enabled testleri çalıştır
mvn clean test

# Sadece smoke testleri
mvn test -Dtest.name="ALBC-Smoke-Tests"

# Sadece negative testleri  
mvn test -Dtest.name="ALBC-Negative-Tests"
```

## ⚙️ Test Yönetimi

Testler `testng.xml` dosyasından yönetilir:

```xml
<!-- Test grubu çalıştırmak için -->
<test name="ALBC-Smoke-Tests" enabled="true">

<!-- Test grubu atlamak için -->  
<test name="ALBC-Validation-Tests" enabled="false">
```

## 📊 Raporlar

Test çalıştıktan sonra raporlar şurada oluşur:

- **TestNG Report**: `target/surefire-reports/index.html`
- **Cucumber Report**: `target/cucumber-reports/basic-html/index.html` 
- **JSON Report**: `target/cucumber-reports/cucumber-test-report.json`

## 🌐 Environment

ALBC environment configuration:
```
Host: https://dev.faturalab.com/app/api/integration/buyer/v0
Environment: ALBC Marketler
```

## 📁 Proje Yapısı

```
├── src/test/java/
│   ├── runners/ALBCTestRunner.java       # Cucumber TestNG Runner
│   ├── stepdefinitions/FaturaAPISteps.java # Test step implementations  
│   └── hooks/CucumberHooks.java          # Test lifecycle hooks
├── src/test/resources/
│   ├── features/FaturaUploadFlow.feature # BDD test scenarios
│   └── config/                           # Environment configurations
├── testng.xml                            # TestNG test suite configuration
├── run-tests.bat                         # Windows test runner
└── run-tests.sh                          # Mac/Linux test runner
```

## 🔧 Hızlı Authentication Test

```bash
java -cp target/test-classes:target/classes com.faturalab.automation.ALBCAuthTest
```

## 📝 API Test Detayları

Her test çalıştığında şu bilgiler raporlanır:

- ✅ **Request detayları** (endpoint, headers, body)
- ✅ **Response detayları** (status, headers, JSON response)  
- ✅ **Test adımları** ve sonuçları
- ✅ **Hata durumları** ve debug bilgileri

## 🎯 Test Senaryoları

1. **Basit Fatura Yükleme** - Authentication → Upload → History → Delete
2. **Boş Parametreler** - Hata handling testleri
3. **Geçersiz Değerler** - Validation testleri  
4. **E-Arşiv Fatura** - Farklı fatura türü testleri

Tüm testler **ALBC environment** üzerinde çalışır ve **gerçek API endpoint'leri** kullanır. 