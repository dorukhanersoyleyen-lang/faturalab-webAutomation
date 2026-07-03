package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Admin — Raporlar / Teklif Talepleri listesi (UAT FL-001, FL-002).
 */
public class AdminReportsPage extends BasePageObject {

    private static final By GRID = By.cssSelector("vaadin-grid");
    private static final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final AdminPanelPage adminPanel;

    public AdminReportsPage(WebDriver driver) {
        super(driver);
        this.adminPanel = new AdminPanelPage(driver);
    }

    public void navigateToRaporlar() {
        try {
            adminPanel.clickSidebarItem("Raporlar");
        } catch (Exception e) {
            clickNavItemByText("rapor");
        }
        waitForVaadinNavigation();
    }

    public void navigateToTeklifTalepleri() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var sel = 'vaadin-button, button, a, vaadin-item, [role=\"button\"], span.link, .menu-button';" +
                    "var els = document.querySelectorAll(sel);" +
                    "for (var el of els) {" +
                    "  var t = (el.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t.includes('teklif talep') || t.includes('teklif talepleri') || " +
                    "      t === 'teklif talepleri' || (t.includes('teklif') && t.includes('talep'))) {" +
                    "    el.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(clicked)) {
                waitForVaadinNavigation();
                return;
            }
        } catch (Exception e) {
            log.warn("Teklif talepleri (JS): {}", e.getMessage());
        }
        if (!clickNavItemByText("teklif talep")) {
            clickNavItemByText("Teklif");
        }
        waitForVaadinNavigation();
        acceptVaadinConfirmDialogIfPresent();
    }

    /** Raporlar altındaki "Günlük İşlemler" sayfasına gider (TZF işlemi doğrulaması). */
    public void navigateToGunlukIslemler() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var sel = 'vaadin-button, button, a, vaadin-item, vaadin-side-nav-item, [role=\"button\"], .menu-button';" +
                    "var els = document.querySelectorAll(sel);" +
                    "for (var el of els) {" +
                    "  var t = (el.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t.length < 40 && (t.includes('günlük işlem') || t.includes('gunluk islem'))) {" +
                    "    el.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(clicked)) {
                waitForVaadinNavigation();
                return;
            }
        } catch (Exception e) {
            log.warn("Günlük işlemler (JS): {}", e.getMessage());
        }
        if (!clickNavItemByText("günlük işlem")) {
            clickNavItemByText("gunluk");
        }
        waitForVaadinNavigation();
    }

    /**
     * Grid hücrelerinde verilen metni arar (bordro no / tedarikçi adı doğrulaması).
     * Grid'de yoksa sayfa gövdesine de bakar.
     */
    public boolean isTextVisibleInGrid(String needle) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            Thread.sleep(1500); // Vaadin grid asenkron satır render'ı
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("isTextVisibleInGrid grid beklemesi: {}", e.getMessage());
        }
        try {
            Boolean found = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var needle = arguments[0].toLowerCase();" +
                    "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                    "for (var c of cells) {" +
                    "  if ((c.textContent || '').toLowerCase().includes(needle)) return true;" +
                    "}" +
                    "return (document.body.innerText || '').toLowerCase().includes(needle);",
                    needle);
            log.info("Günlük işlemler grid araması '{}': {}", needle, found);
            return Boolean.TRUE.equals(found);
        } catch (Exception e) {
            log.warn("isTextVisibleInGrid: {}", e.getMessage());
            return false;
        }
    }

    public void applyTarihFilter(String baslangic, String bitis) {
        try {
            List<WebElement> inputs = driver.findElements(By.cssSelector(
                    "vaadin-date-picker input, vaadin-date-time-picker input, input[type='date']"));
            if (inputs.size() >= 2) {
                inputs.get(0).clear();
                inputs.get(0).sendKeys(baslangic);
                inputs.get(1).clear();
                inputs.get(1).sendKeys(bitis);
            }
        } catch (Exception e) {
            log.warn("Tarih filtresi: {}", e.getMessage());
        }
        acceptVaadinConfirmDialogIfPresent();
    }

    public void applyDurumFilter(String durum) {
        try {
            Boolean applied = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var kw = (arguments[0] || '').toUpperCase();" +
                    "var combos = document.querySelectorAll('vaadin-combo-box, vaadin-select, vaadin-multi-select-combo-box');" +
                    "if (combos.length > 0) { combos[0].click(); }" +
                    "var items = document.querySelectorAll('vaadin-item, vaadin-combo-box-item, [role=\"option\"]');" +
                    "for (var i = 0; i < items.length; i++) {" +
                    "  var t = (items[i].textContent || '').toUpperCase().replace(/\\s+/g, ' ').trim();" +
                    "  if (!t) continue;" +
                    "  if (t.includes(kw)) { items[i].click(); return true; }" +
                    "}" +
                    "return false;",
                    durum);
            if (!Boolean.TRUE.equals(applied)) {
                ((JavascriptExecutor) driver).executeScript(
                        "var kw = arguments[0].toUpperCase();" +
                        "var els = document.querySelectorAll('vaadin-select, vaadin-combo-box, vaadin-list-box vaadin-item');" +
                        "for (var el of els) {" +
                        "  var t = (el.textContent || '').toUpperCase();" +
                        "  if (t.includes(kw)) { el.click(); return; }" +
                        "}",
                        durum);
            }
        } catch (Exception e) {
            log.warn("Durum filtresi: {}", e.getMessage());
        }
        acceptVaadinConfirmDialogIfPresent();
    }

    public boolean isGridVisible() {
        try {
            WebDriverWait gridWait = new WebDriverWait(driver, Duration.ofSeconds(22));
            gridWait.until(ExpectedConditions.visibilityOfElementLocated(GRID));
            return !driver.findElements(GRID).isEmpty();
        } catch (Exception e) {
            return !driver.findElements(GRID).isEmpty();
        }
    }

    public void selectRaporTipi(String tipKeyword) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var kw = arguments[0].toLowerCase();" +
                    "var cells = document.querySelectorAll('vaadin-grid-cell-content, td');" +
                    "for (var c of cells) {" +
                    "  if ((c.textContent || '').toLowerCase().includes(kw)) { c.click(); return true; }" +
                    "}" +
                    "return false;",
                    tipKeyword);
        } catch (Exception e) {
            log.warn("Rapor tipi seçimi: {}", e.getMessage());
        }
    }

    public void clickIndirButonu() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if (t.includes('indir') || t.includes('download') || t.includes('export')) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;");
        } catch (Exception e) {
            log.warn("İndir butonu: {}", e.getMessage());
        }
    }

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
