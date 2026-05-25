# FaturaLab Web Automation — Cursor IDE Context

## Project Overview
Selenium + Cucumber BDD + TestNG automation project for **dev2.faturalab.com** (Vaadin 24 / Spring Boot).

## Tech Stack
- **Java 17**, Maven
- Selenium 4.16.1, Cucumber 7.15.0, TestNG 7.9.0
- Allure reports + masterthought cucumber reports
- Target env: `https://dev2.faturalab.com` (Vaadin 24 SPA)

## Test Roles & Credentials (dev2.properties)
| Role | Email | Password |
|------|-------|----------|
| Admin | dorukhan.ersoyleyen@faturalab.com | Dorukhan.1 |
| Company (Tedarikçi / EFG Gıda) | test@testggg.com | Dorukhan.1 |
| Buyer (Alıcı / ALBC) | berkay.aytekin@albc.faturalab.com | Dorukhan.1 |
| Factoring (Finansman / Akbank) | kadir.ak@akbank.faturalab.com | Dorukhan.1 |

## Running Tests
```bash
mvn clean test -Denv=dev2 -DsuiteXmlFile=testng-ui.xml
# or with tag filter:
mvn test -Denv=dev2 -DsuiteXmlFile=testng-ui.xml -Dcucumber.filter.tags="@smoke"
```

## Package Structure

### Key Packages
| Package | Purpose |
|---------|---------|
| `com.faturalab.automation.stepdefinitions.ui` | **All UI step defs** — isolated package for UITestRunner |
| `com.faturalab.automation.context` | `RoleSessionManager` — cookie-based multi-role session |
| `com.faturalab.automation.pages` | Page objects (BasePageObject + 4 role pages) |
| `com.faturalab.automation.config` | `ConfigReader` reads `src/test/resources/config/{env}.properties` |
| `com.faturalab.automation.driver` | `DriverManager` — ThreadLocal WebDriver |

### UI Step Definition Files (`stepdefinitions/ui/`)
- `UIHooks.java` — `@Before/@After/@AfterStep` for `@ui` tag
- `LoginRoleStepDefs.java` — login Given steps for each role
- `CommonUIStepDefs.java` — shared `@Then("basari bildirimi gorunmeli")`
- `CompanyInvoiceUIStepDefs.java` — company invoice upload steps
- `AdminInvoiceUIStepDefs.java` — admin invoice approval steps
- `FactoringUIStepDefs.java` — factoring offer management steps
- `BuyerAuctionUIStepDefs.java` — buyer auction approval steps

### Test Runner
- `UITestRunner.java` — glue ONLY `com.faturalab.automation.stepdefinitions.ui`
- `TestRunner.java` — runs old API/integration tests (non-UI)

## Feature Files
All UI features in `src/test/resources/features/ui/`:
- `FaturaYuklemeUI.feature` — `@ui @dev2 @company`
- `AdminFaturaOnayUI.feature` — `@ui @dev2 @admin`
- `AliciIhaleOnayUI.feature` — `@ui @dev2 @buyer`
- `FactoringTeklifUI.feature` — `@ui @dev2 @factoring`
- `UcTanUcaAkis.feature` — `@ui @dev2 @e2e`

**Language:** English Gherkin (NO `# language: tr`)
**Tags:** `@ui and @dev2 and not @disabled`

## Critical Architecture Decisions

### 1. Isolated UI Package
UITestRunner.glue = `"com.faturalab.automation.stepdefinitions.ui"` ONLY.
Old packages (`stepdefinitions.admin`, `.invoice`, `.factoring`, `.company`) are NOT in UITestRunner.
This prevents DuplicateStepDefinition errors.

### 2. pom.xml — No cucumber.glue System Property
The `<cucumber.glue>` system property was REMOVED from Surefire plugin.
It was overriding UITestRunner's @CucumberOptions.
The `<argLine>-Dfile.encoding=UTF-8</argLine>` is kept.

### 3. Turkish Characters
Old step def files using `io.cucumber.java.tr.*` were deleted (encoding conflict on Windows-1252).
All new step defs use English (`io.cucumber.java.en.*`) only.
Turkish text appears only in log messages and comments, NOT in @Given/@When/@Then annotations.

### 4. Vaadin 24 Login Fix (`RoleSessionManager.performLogin()`)
- Vaadin takes 5-15 seconds to render inputs after page load
- Fix: `wait.until(!input elements.isEmpty())` before form filling
- `isDisplayed()` check removed from `findWithFallback()` (too strict for Vaadin)
- `waitForDashboard()` waits until URL no longer contains `/login`

### 5. Vaadin 24 Navigation Fix (`BasePageObject`)
- `vaadin-side-nav-item` may expose text via shadow DOM slots
- XPath `normalize-space()` sometimes fails on Vaadin shadow DOM
- Added `clickNavItemByText(String keyword)` — JavaScript textContent search
- All page object `navigate*()` methods: XPath first → JS fallback
- Added `waitForVaadinNavigation()` instead of `waitForPageLoad()` for SPA nav

### 6. Cookie-Based Multi-Role Sessions
`RoleSessionManager` stores cookie sets per role. `switchToRole()` clears all cookies, loads stored set, refreshes. `clearAllSessions()` called in `UIHooks.@After`.

## Known Issues / TODO
- Menu item text must match actual dev2 sidebar text — if navigation still fails after JS fallback, open browser DevTools, inspect `vaadin-side-nav-item` elements, note actual `.textContent` and update keywords in page objects
- `vaadin-grid` virtualization: `vaadin-grid-cell-content` only renders visible rows — scroll may be needed for full grid traversal
- `testng-ui.xml` must exist at project root; if missing, create it (see below)

## testng-ui.xml (create at project root if missing)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="UI Test Suite" verbose="2">
  <test name="UI Tests">
    <classes>
      <class name="com.faturalab.automation.runner.UITestRunner"/>
    </classes>
  </test>
</suite>
```

## Deleted Files (do NOT recreate)
These 5 files were deleted because they used `io.cucumber.java.tr.*` which caused compile errors on Windows due to encoding:
- `stepdefinitions/admin/InvoiceApprovalStepDefs.java`
- `stepdefinitions/company/IhaleManagementStepDefs.java`
- `stepdefinitions/factoring/BordroYonetimiStepDefs.java`
- `stepdefinitions/factoring/FactoringLimitStepDefs.java`
- `stepdefinitions/invoice/InvoiceUploadStepDefs.java`

Their functionality is replaced by the `stepdefinitions/ui/` package files.
