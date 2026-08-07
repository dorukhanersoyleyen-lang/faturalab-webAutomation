package com.faturalab.automation.pages;

import com.faturalab.automation.utils.VaadinGridFilterHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Tedarikçi (Company) — "Bayi Limitleri" liste ekranı.
 *
 * Kaynak kod eşlemesi (origin/vaadin-24, READ-ONLY incelendi):
 *  - Sidebar butonu: "Bayi Limitleri" — {@link CompanyDisplayFactoringsPage}'teki "Doğrudan Tahsilat"
 *    üst grup başlığının ARKASINDA lazy render ediliyor (aynı navigasyon kalıbı).
 *  - Grid: CompanyDisplayDealerLimitsView — kolonlar: (Düzenle) / Bayi Ünvanı / Finansal Kurum /
 *    Sözleşme Tarihi / Güncelleme Tarihi / Etkin / FK Durumu / DBS Limit Tutarı / Kalan DBS Limit
 *    Tutarı / Vadeli Yüklenen Tutar / Kredilenen Tutar / Ödenmemiş Tutar / İskonto Limiti /
 *    Aktif İskonto Tutarı / Ek İskonto Limiti / Açıklama.
 *  - ⚠️ Kaynak kod kanıtı: bu ekranda "Ekle" butonu YOKTUR (sadece {@code buildTableLayout()},
 *    hiçbir "Bayi Limiti Ekle" call-to-action yok) — SADECE Read+Update (satır "Düzenle" →
 *    CompanyEditDealerLimitDialog). REST tarafında da (CompanyService./dealer/limit) yazma
 *    endpoint'i YOK (Api.dealerLimit() sadece sorgu) — bu KASITLI bir kısıtlamadır.
 */
public class CompanyDisplayDealerLimitsPage extends BasePageObject {

    private static final By GRID = By.cssSelector("vaadin-grid");

    public CompanyDisplayDealerLimitsPage(WebDriver driver) {
        super(driver);
    }

    /** Sidebar'daki "Bayi Limitleri" butonuna tıklar ({@link CompanyDisplayFactoringsPage#navigateToFactorings} ile aynı kalıp). */
    public boolean navigateToDealerLimits() {
        tryOpenNavigationDrawer();
        waitForSidebarPopulated(8);
        if (clickSidebarButtonByText("bayi limitleri")) {
            waitForVaadinNavigation();
            return true;
        }
        boolean groupOpened = clickSidebarButtonByExactFold("dogrudan tahsilat");
        log.info("[DTS-MD] 'Doğrudan Tahsilat' üst grup başlığı tıklandı mı: {}", groupOpened);
        if (groupOpened) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (clickSidebarButtonByText("bayi limitleri")) {
                waitForVaadinNavigation();
                return true;
            }
        }
        if (clickNavItemByText("bayi limitleri")) {
            waitForVaadinNavigation();
            return true;
        }
        log.warn("[DTS-MD] 'Bayi Limitleri' menüsü bulunamadı.");
        return false;
    }

    private boolean clickSidebarButtonByExactFold(String exactFoldKeyword) {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var target = arguments[0];" +
                    "var btns = document.querySelectorAll('vaadin-button');" +
                    "for (var b of btns) {" +
                    "  var r = b.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (fold(b.textContent) === target) { b.click(); return true; }" +
                    "}" +
                    "return false;", exactFoldKeyword);
            return Boolean.TRUE.equals(clicked);
        } catch (Exception e) {
            log.debug("[DTS-MD] clickSidebarButtonByExactFold '{}': {}", exactFoldKeyword, e.getMessage());
            return false;
        }
    }

    private void waitForSidebarPopulated(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                Object count = ((JavascriptExecutor) driver).executeScript(
                        "var sel = 'vaadin-side-nav-item, vaadin-tab, a[href], vaadin-button';" +
                        "return document.querySelectorAll(sel).length;");
                long n = count instanceof Number ? ((Number) count).longValue() : 0;
                if (n > 4) {
                    return;
                }
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ignored) {
            }
        }
        log.warn("[DTS-MD] Sidebar {} sn içinde dolmadı — mevcut haliyle denenecek.", timeoutSeconds);
    }

    private boolean clickSidebarButtonByText(String keyword) {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var kw = arguments[0].toLowerCase();" +
                    "var btns = document.querySelectorAll('vaadin-button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent||'').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t.length > 0 && t.length <= 60 && t.includes(kw)) { b.click(); return true; }" +
                    "}" +
                    "return false;", keyword);
            return Boolean.TRUE.equals(clicked);
        } catch (Exception e) {
            log.debug("[DTS-MD] clickSidebarButtonByText '{}': {}", keyword, e.getMessage());
            return false;
        }
    }

    public boolean isGridVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            return !driver.findElements(GRID).isEmpty();
        } catch (Exception e) {
            log.warn("[DTS-MD] Grid görünürlüğü doğrulanamadı: {}", e.getMessage());
            return !driver.findElements(GRID).isEmpty();
        }
    }

    /** "Bayi Ünvanı" kolonu üzerinden kolon filtresi uygular (VaadinGridFilterHelper — Bayi Kodu ile aynı kalıp). */
    public boolean findDealerLimitRowByDealerName(String dealerName) {
        return VaadinGridFilterHelper.applyOnlyValuesWithRetry(
                driver, "Bayi Ünvanı", Collections.singletonList(dealerName), 3);
    }

    /** Görünür grid satırlarını (y-bazlı gruplama) döker — tanı/loglama amaçlı. */
    @SuppressWarnings("unchecked")
    public List<String> dumpVisibleRowsGrouped() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                    "var body = cells.filter(function(c) {" +
                    "  var r = c.getBoundingClientRect();" +
                    "  if (r.width < 2 || r.height < 2) return false;" +
                    "  if (c.querySelector('.filter-header-cell')) return false;" +
                    "  return true;" +
                    "});" +
                    "var rows = {};" +
                    "body.forEach(function(c) {" +
                    "  var top = Math.round(c.getBoundingClientRect().top / 3) * 3;" +
                    "  if (!rows[top]) rows[top] = [];" +
                    "  rows[top].push(c);" +
                    "});" +
                    "var keys = Object.keys(rows).map(Number).sort(function(a,b){return a-b;});" +
                    "return keys.map(function(k) {" +
                    "  return rows[k].map(function(c) {" +
                    "    return (c.textContent || '').replace(/\\s+/g,' ').trim();" +
                    "  }).join(' || ');" +
                    "});");
            List<String> rows = (List<String>) result;
            log.info("[DTS-MD] Görünür satır dökümü ({} satır):", rows.size());
            for (String r : rows) {
                log.info("[DTS-MD]   {}", r);
            }
            return rows;
        } catch (Exception e) {
            log.warn("[DTS-MD] dumpVisibleRowsGrouped: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Filtre tam TEK satıra indirildiğinde gridde görünen TEK "Düzenle" butonuna gerçek Selenium
     * click() ile basar ({@link CompanyDisplayFactoringsPage#clickOnlySingleRowEditButton} ile aynı gerekçe).
     */
    public boolean clickOnlySingleRowEditButton() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('[data-dealerlimit-edit-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dealerlimit-edit-target');});" +
                    "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                    "var found = null;" +
                    "for (var c of cells) {" +
                    "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (c.querySelector('.filter-header-cell')) continue;" +
                    "  var btn = c.querySelector('vaadin-button, button');" +
                    "  if (btn && !btn.disabled) { found = btn; break; }" +
                    "}" +
                    "if (!found) return false;" +
                    "found.setAttribute('data-dealerlimit-edit-target', '1');" +
                    "return true;");
            log.info("[DTS-MD] clickOnlySingleRowEditButton (Bayi Limiti) - buton isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement btn = driver.findElement(By.cssSelector("[data-dealerlimit-edit-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            btn.click();
            log.info("[DTS-MD] clickOnlySingleRowEditButton (Bayi Limiti): gerçek Selenium click uygulandı.");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS-MD] clickOnlySingleRowEditButton (Bayi Limiti): {}", e.getMessage());
            return false;
        }
    }
}
