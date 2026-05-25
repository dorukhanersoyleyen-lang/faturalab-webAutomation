# UI Otomasyon — Devam Notu
_Son güncelleme: 2026-03-16_

## Hedef
`https://dev2.faturalab.com` üzerinde Selenium tabanlı çok-rol UI otomasyonu.
Mevcut otomasyon projesi: `C:\Users\dorukhane\Desktop\faturalab-webAutomation`

---

## Yapılan İşler

### 1. RoleSessionManager.java ✅
**Konum:** `src/main/java/com/faturalab/automation/context/RoleSessionManager.java`

Cookie tabanlı çok-rol oturum yöneticisi. Strateji:
- Her rol için bir kez `loginAs(driver, Role.X, email, pass)` → cookie'ler hafızada tutulur
- `switchToRole(driver, Role.X)` → cookie'leri yükle + sayfayı yenile (yeniden login YOK)
- Senaryo sonunda `clearAllSessions()` çağrılır

Roller: `Role.COMPANY`, `Role.BUYER`, `Role.FACTORING`, `Role.ADMIN`

### 2. dev2.properties ✅
**Konum:** `src/test/resources/config/dev2.properties`

- `base.url=https://dev2.faturalab.com`
- Admin bilgileri dolu: `dorukhan.ersoyleyen@faturalab.com / Dorukhan.1`
- **TODO:** Diğer 3 rolün e-posta/şifresini admin > kullanıcılar panelinden bul ve doldur:
  - `company.email/password` → EFG Gıda A.Ş. kullanıcısı
  - `buyer.email/password` → ALBC Marketler Zinciri A.Ş. kullanıcısı
  - `factoring.email/password` → OPR Bankası kullanıcısı

---

## Yapılacaklar (Sırasıyla)

### 3. Page Objects (Vaadin 24 uyumlu)
`src/main/java/com/faturalab/automation/pages/` altına şunlar eklenecek:

#### CompanyInvoicePage.java
- Faturalarım ekranı → `navigateToInvoiceList()`
- Fatura Yükle dialog → `openUploadDialog()` / `uploadInvoiceFile(path)`
- Liste doğrulama → `getInvoiceStatus(invoiceNo)`
- Seçiciler (Vaadin 24): `vaadin-button`, XPath text bazlı

#### AdminPanelPage.java
- Onay bekleyen faturalar listesi
- `approveInvoice(invoiceNo)` — ONAYLA butonuna tıkla
- `searchInvoice(invoiceNo)` — Fatura no ile arama

#### FactoringDashboardPage.java
- Teklif Talebi Yönetimi ekranı (screenshot: A2025_73387 satırı)
- Tab: Günlük Teklif Talebi
- `approveOffer(bordroNo)` — ONAYLA butonu
- `cancelOffer(bordroNo)` — İPTAL butonu
- `viewOffer(bordroNo)` — GÖZAT butonu

#### BuyerAuctionPage.java
- İhale listesi → `navigateToPendingAuctions()`
- `approveAuction(auctionId)` — İhaleyi onayla
- `rejectAuction(auctionId, reason)` — İhaleyi reddet

### 4. Feature Files
`src/test/resources/features/` altına:
- `FaturaYuklemeUI.feature` — TC-COMP-01 (11 case)
- `AdminFaturaOnayUI.feature` — TC-ADMIN-02 (7 case)
- `FactoringTeklifUI.feature` — TC-FACT-01 (8 case)
- `AliciIhaleOnayUI.feature` — TC-BUYER-02 (7 case)
- `UcTanUcaAkis.feature` — SCN-01 (full E2E, 4 rol)

Dil: Turkish Gherkin (`# language: tr`)
Taglar: `@ui @dev2` + rol spesifik (`@company`, `@admin`, `@factoring`, `@buyer`)

### 5. Step Definitions
`src/test/java/com/faturalab/automation/stepdefinitions/` altına:
- `LoginRoleStepDefs.java` — Shared login/rol geçiş adımları (RoleSessionManager kullanır)
- `CompanyInvoiceUIStepDefs.java`
- `AdminInvoiceUIStepDefs.java`
- `FactoringUIStepDefs.java`
- `BuyerAuctionUIStepDefs.java`

### 6. UITestRunner.java
`src/test/java/com/faturalab/automation/runners/`
- Tags: `@ui and @dev2`
- Glue: tüm yeni step def paketleri
- `-Denv=dev2` ile çalıştırılır

---

## Kritik Teknik Notlar

### Vaadin 24 Selektör Stratejisi
FaturaLab dev2 Vaadin 24 kullanıyor. Temel kurallar:
- Butonlar: `vaadin-button` web component → text XPath ile: `//vaadin-button[normalize-space()='ONAYLA']`
- Grid: `vaadin-grid` → satır seçimi için `vaadin-grid-cell-content`
- Text field: `vaadin-text-field input` (shadow DOM input'u)
- Dialog: `vaadin-dialog-overlay`
- Notification: `vaadin-notification-container`
- Menü: `vaadin-side-nav-item` veya XPath text ile

### Multi-Rol Cookie Stratejisi
```gherkin
Arka Plan:
  Diyelim ki tedarikçi EFG oturumu hazırlandı
  Ve admin dorukhan oturumu hazırlandı
  Ve alıcı ALBC oturumu hazırlandı
  Ve finansman OPR oturumu hazırlandı

Senaryo: Uçtan uca ihale akışı
  Eğer ki tedarikçi rolüne geçilir
  Ve fatura yüklenir
  Ve admin rolüne geçilir
  Ve fatura onaylanır
  ...
```

### Properties Yükleme
`-Denv=dev2` ile `dev2.properties` yüklenir (ConfigReader otomatik yapar)

### Mevcut HomePage.java Login Selektörleri
```
email: input[type='text'].v-login-textfield-component
password: input[type='password'].v-login-textfield-component
loginBtn: div.login-button[role='button']
```
Bu selektörler dev2.faturalab.com login ekranında çalışıyorsa kullan, çalışmıyorsa Vaadin 24 alternatiflerine geç.

---

## API Endpoint Referansı (gerekirse hibrit test için)
```
Company: https://dev2.faturalab.com/app/api/integration/company/v0/
Buyer:   https://dev2.faturalab.com/app/api/integration/buyer/v0/
Factoring: https://dev2.faturalab.com/app/api/integration/factoring/v0/
Auth endpoint (her rol): POST .../authenticate/  (param: authenticateParam)
```

---

## Test Kullanıcısı Tespiti
Admin panelinden (`dorukhan.ersoyleyen@faturalab.com / Dorukhan.1`) şu firmaların kullanıcılarını bul:
- EFG Gıda A.Ş. → tedarikçi email + şifre
- ALBC Marketler Zinciri A.Ş. → alıcı email + şifre
- OPR Bankası → finansman email + şifre

Bulunca `dev2.properties` içindeki TODO alanlarını doldur.
