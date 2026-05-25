# FaturaLab UI Otomasyon — Claude Agent Talimatları

## Proje Özeti
FaturaLab v2 (`https://dev.faturalab.com/app`) için Selenium + Cucumber BDD + Java tabanlı çok-rol UI otomasyon projesi.

| | |
|---|---|
| **Proje** | faturalab-webAutomation |
| **GroupId** | org.faturalabAutomation |
| **Java** | 11+ |
| **Selenium** | 4.16.1 |
| **Cucumber** | 7.15.0 |
| **TestNG** | 7.9.0 |
| **Raporlama** | Allure 2.25 + Cucumber HTML/JSON + CucumberExtendedReportGenerator |
| **CI** | Jenkins (Jenkinsfile mevcut) |

### Teknoloji Stack'i
- **UI Otomasyon**: Selenium WebDriver 4 + WebDriverManager 5.6.3
- **BDD**: Cucumber-Java + Cucumber-TestNG + PicoContainer
- **Test Çerçevesi**: TestNG 7
- **Raporlama**: Allure Reports, Cucumber HTML, özel CucumberExtendedReportGenerator
- **Logging**: Log4j2
- **Konfigürasyon**: `.properties` dosyaları (env bazlı: `dev.properties`, `dev2.properties`)

---

## Mevcut Durum (Mayıs 2026)

### Tamamlanan Bileşenler ✅

**Page Objects** (`src/main/java/com/faturalab/automation/pages/`):
- `BasePageObject.java` — Vaadin 24 yardımcı metodlar, shadow DOM nav, confirm dialog handler
- `LoginPage.java`, `HomePage.java`, `DashboardPage.java`, `RegisterPage.java`, `ForgotPasswordPage.java`
- `CompanyInvoicePage.java`, `CompanyAuctionPage.java`, `CompanyBulkUploadPage.java`, `CompanyQuickOfferPage.java`, `CompanySettingsPage.java`
- `AdminPanelPage.java`, `AdminCriteriaPage.java`, `AdminFirmaPage.java`, `AdminKullaniciPage.java`, `AdminFKAyarlariPage.java`, `AdminReportsPage.java`
- `FactoringDashboardPage.java`, `FactoringAuctionPage.java`, `FactoringLimitPage.java`
- `BuyerAuctionPage.java`, `BuyerAuctionManagePage.java`, `BuyerInvoicePage.java`
- `PriceManagementPage.java`, `CriteriaTablePage.java`, `SupplierManagementPage.java`, `VDMKAuctionPage.java`

**Step Definitions** (`src/test/java/com/faturalab/automation/stepdefinitions/ui/`):
- `UIHooks.java` — @Before/@After/@AfterStep lifecycle, screenshot on failure, QAHubReporter
- `LoginRoleStepDefs.java`, `CommonUIStepDefs.java`
- `CompanyInvoiceUIStepDefs.java`, `CompanyAuctionUIStepDefs.java`
- `AdminInvoiceUIStepDefs.java`, `AdminCriteriaUIStepDefs.java`
- `FactoringUIStepDefs.java`, `FactoringOfferUIStepDefs.java`, `FactoringLimitUIStepDefs.java`
- `BuyerAuctionUIStepDefs.java`, `BuyerInvoiceUIStepDefs.java`
- `PriceManagementUIStepDefs.java`, `RebateCriteriaUIStepDefs.java`, `VDMKFlowUIStepDefs.java`
- `BaremliAkisUIStepDefs.java`, `ReCaptchaFlowStepDefs.java`
- UAT step defs: `TedarikciFaturaYuklemeUATStepDefs.java`, `TedarikciTeklifAlmaUATStepDefs.java`, `AliciIhaleUATStepDefs.java`, `FinansmanTeklifVermeUATStepDefs.java`, `AdminFirmaKullaniciUATStepDefs.java`, `AdminAyarlarUATStepDefs.java`, `AdminRaporlarUATStepDefs.java`, `E2EAkislarUATStepDefs.java`
- E2E: `SCN02StepDefs.java`, `SCN03StepDefs.java`, `SCN04StepDefs.java`

**Feature Files** (`src/test/resources/features/ui/`):
- `FaturaYuklemeUI.feature` (TC-COMP-01), `AdminFaturaOnayUI.feature`, `FactoringTeklifUI.feature`, `FactoringLimitUI.feature`, `AliciIhaleOnayUI.feature`, `AliciReddiYenidenIhale.feature`
- `UcTanUcaAkis.feature` (E2E, 4 rol), `BaremliAliciE2EAkisi.feature`, `BaremsizFiyatAkisi.feature`, `BuyNowFlow.feature`, `CokluFinansmanTeklif.feature`
- `LoginFlow.feature`, `PriceManagementUI.feature`, `RebateCriteriaUI.feature`, `VDMKFlow.feature`, `KagitFaturaE2EFlow.feature`, `ReCaptchaFlow.feature`
- UAT suite (`features/ui/uat/`): `TedarikciFaturaYuklemeUAT.feature`, `TedarikciTeklifAlmaUAT.feature`, `AliciIhaleUAT.feature`, `FinansmanTeklifVermeUAT.feature`, `AdminFirmaKullaniciUAT.feature`, `AdminAyarlarUAT.feature`, `AdminRaporlarUAT.feature`, `E2EAkislarUAT.feature`

**Test Runners** (`src/test/java/com/faturalab/automation/runner/`):
- `UITestRunner.java` — tags: `@smoke and @happy-path and @kritik and not @disabled`, features: `ui/`
- `UATTestRunner.java` — tags: `@uat and not @disabled`, features: `ui/uat/`
- `TestRunner.java`, `ReCaptchaTestRunner.java`

**Altyapı**:
- `RoleSessionManager.java` — Cookie tabanlı çok-rol oturum, 4 rol: COMPANY, BUYER, FACTORING, ADMIN
- `DriverFactory.java`, `DriverManager.java` — WebDriver yönetimi
- `ConfigReader.java`, `EnvironmentManager.java` — `-Denv=dev2` ile properties yükleme
- `WaitHelper.java`, `VaadinFormFieldSnapshot.java`, `QAHubReporter.java`
- `CucumberExtendedReportGenerator.java` — özel HTML rapor üretici
- Credentiallar `dev2.properties` içinde **dolu**: admin, company (EFG Gıda), buyer (ALBC / admin), factoring (Akbank)

---

## Proje Yapısı (Güncel)

```
src/
├── main/java/com/faturalab/automation/
│   ├── api/                           ✅ FaturalabAPI, AuctionAPI
│   ├── config/                        ✅ ConfigReader, EnvironmentManager
│   ├── context/
│   │   └── RoleSessionManager.java   ✅ Cookie tabanlı çok-rol oturum yöneticisi
│   ├── driver/                        ✅ DriverFactory, DriverManager
│   ├── models/                        ✅ auth/, invoice/, auction/, common/
│   ├── pages/                         ✅ 20+ page object (Vaadin 24 uyumlu)
│   ├── reporting/                     ✅ CucumberExtendedReportGenerator
│   └── utils/                         ✅ WaitHelper, QAHubReporter, VaadinFormFieldSnapshot
└── test/
    ├── java/com/faturalab/automation/
    │   ├── runner/                    ✅ UITestRunner, UATTestRunner, TestRunner
    │   └── stepdefinitions/
    │       └── ui/                   ✅ 25+ step definition sınıfı
    └── resources/
        ├── features/ui/              ✅ 17 feature file
        │   └── uat/                  ✅ 8 UAT feature file
        ├── config/
        │   ├── dev.properties        ✅
        │   └── dev2.properties       ✅ Tüm roller dolu
        └── testdata/                 ✅ test-invoice.xml, broken-invoice.xml, PDF
```

---

## Kritik Kurallar

### Vaadin 24 Selector Stratejisi (ZORUNLU)
```java
// ✅ Doğru — Vaadin bileşenlerine özel
driver.findElement(By.cssSelector("vaadin-button[theme~='primary']"))
driver.findElement(By.cssSelector("vaadin-grid"))
driver.findElement(By.cssSelector("vaadin-text-field[label='Fatura No']"))
driver.findElement(By.xpath("//vaadin-button[normalize-space()='ONAYLA']"))

// BasePageObject'teki yardımcı metodlar kullan:
clickNavItemByText("Faturalarım")        // shadow DOM dahil derinlemesine arama
acceptVaadinConfirmDialogIfPresent()     // confirm dialog otomatik kapama
waitForVaadinNavigation()               // Vaadin client-side nav bekleme

// ❌ Yanlış — Vaadin Shadow DOM'u bypass edemez
driver.findElement(By.className("v-button-caption"))
driver.findElement(By.id("someVaadin24Id"))
```

### RoleSessionManager Kullanımı
```java
// Varsayılan kimlik bilgileriyle (dev2.properties) login
RoleSessionManager.loginAsDefault(driver, Role.ADMIN);
RoleSessionManager.loginAsDefault(driver, Role.COMPANY);

// Veya explicit kimlik bilgileriyle
RoleSessionManager.loginAs(driver, Role.ADMIN, adminEmail, adminPassword);

// Rol geçişi — yeniden login YOK, sadece cookie swap
RoleSessionManager.switchToRole(driver, Role.COMPANY);

// Oturum var mı kontrol
RoleSessionManager.hasSession(Role.BUYER);

// Test sonunda (UIHooks @After içinde otomatik çağrılır)
RoleSessionManager.clearAllSessions();
```

### Cucumber Feature Formatı
```gherkin
@ui @dev2 @company @smoke
Feature: TC-COMP-01 - Tedarikci Fatura Yukleme UI

  Background:
    Given tedarikci EFG roleyle dev2'ye giris yapildi

  @happy-path @kritik
  Scenario: Gecerli XML fatura basariyla yuklenir
    When "Faturalarim" ekranina gidilirse
    And "Fatura Yukle" butonuna tiklanirsa
    And gecerli bir XML fatura dosyasi secilirse
    And "Kaydet" butonuna tiklanirsa
    Then basari bildirimi gorunmeli
```

### Taglar
- `@ui` — tüm UI testleri (UIHooks trigger)
- `@dev2` — dev2/dev ortamı
- `@uat` — UAT suite (UATTestRunner)
- `@company`, `@admin`, `@factoring`, `@buyer` — rol bazlı
- `@smoke`, `@happy-path`, `@kritik` — UITestRunner varsayılan filtresi
- `@e2e` — uçtan uca senaryolar
- `@negative`, `@validasyon` — negatif test senaryoları
- `@disabled` — geçici olarak devre dışı bırakılmış (runner'dan hariç tutulur)
- `@fl-001` … `@fl-019` — UAT case ID'leri

---

## Ortam Konfigürasyonu

### dev2.properties (src/test/resources/config/)
```properties
base.url=https://dev.faturalab.com/app    # Dikkat: URL bu, dev2.faturalab.com DEĞİL
browser=chrome
headless=true
implicit.wait=10
page.load.timeout=30
explicit.wait=30

admin.email=dorukhan.ersoyleyen@faturalab.com
admin.password=Dorukhan.1

company.email=test@testggg.com
company.password=Dorukhan.1
company.name=EFG Gida A.S.

buyer.email=dorukhan.ersoyleyen@faturalab.com   # Admin kredisi kullanılıyor
buyer.password=Dorukhan.1
buyer.name=ALBC Marketler Zinciri A.S.

factoring.email=kadir.ak@akbank.faturalab.com   # OPR değil, Akbank
factoring.password=Dorukhan.1
factoring.name=Akbank

test.matched.company.taxno=3960656675
test.matched.buyer.taxno=3456789010
```

### Jenkins
- Poll SCM: her 2 dakikada kontrol
- `Jenkinsfile` mevcut → `mvn clean test`
- Allure raporu Jenkins'e yüklenir

---

## Komutlar

```bash
# UITestRunner — smoke/happy-path/kritik testler (varsayılan)
mvn clean test -Denv=dev2 -DsuiteXmlFile=testng-ui.xml

# UITestRunner — etiket filtresiyle
mvn clean test -Denv=dev2 -DsuiteXmlFile=testng-ui.xml -Dcucumber.filter.tags="@ui and @dev2"

# UATTestRunner — tüm UAT senaryoları
mvn clean test -Denv=dev2 -DsuiteXmlFile=testng-uat.xml

# UATTestRunner — tek UAT case
mvn clean test -Denv=dev2 -DsuiteXmlFile=testng-uat.xml -Dcucumber.filter.tags="@fl-001"

# UATTestRunner — kritik UAT
mvn clean test -Denv=dev2 -DsuiteXmlFile=testng-uat.xml -Dcucumber.filter.tags="@uat and @kritik"

# Smoke testleri
mvn clean test -Denv=dev2 -Dcucumber.filter.tags="@smoke"

# Raporu otomatik açmadan çalıştır
mvn clean test -Denv=dev2 -DsuiteXmlFile=testng-uat.xml -Dfaturalab.open.reports=false

# Allure raporu görüntüle
allure serve allure-results/

# Jenkins build
mvn clean test -Pjenkins
```

---

## Önemli Dosyalar

| Dosya | Açıklama |
|-------|----------|
| `UI_OTOMASYON_DEVAM.md` | Devam notu — nerede kaldık (kısmen eski) |
| `CURSOR_CONTEXT.md` | Cursor AI bağlam dosyası |
| `FATURA_API_TESTS_README.md` | API test notları |
| `WINDOWS_SETUP_GUIDE.md` | Windows kurulum rehberi |
| `testng-ui.xml` | UI smoke/kritik test suite |
| `testng-uat.xml` | UAT test suite |
| `testng.xml` | Genel TestNG suite |
| `allure-results/` | Son test sonuçları |
| `src/main/java/.../pages/BasePageObject.java` | Tüm page object'lerin base class'ı — Vaadin yardımcı metodlar burada |
| `src/main/java/.../context/RoleSessionManager.java` | Multi-rol cookie session manager |
