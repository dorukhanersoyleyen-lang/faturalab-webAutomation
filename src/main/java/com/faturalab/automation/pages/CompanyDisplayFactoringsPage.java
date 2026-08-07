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
 * Tedarikçi (Company) — "Finansal Kurumlar" (Anlaşmalı Finansal Kurum İlişkisi) liste ekranı.
 *
 * Kaynak kod eşlemesi (origin/vaadin-24, READ-ONLY incelendi):
 *  - Sidebar butonu: "Finansal Kurumlar" (CompanyDealerMenuView.2) — DÜZ vaadin-button;
 *    {@code MainView.buildLeftMenu()} companyDealerMenuView'ı CompanyDashboardMenuView'ın
 *    YANINA doğrudan sidebar'a ekler — ayrı bir "Doğrudan / Tahsilat" alt-menüsü/submenu YOK.
 *  - Grid: CompanyDisplayFactoringsView — kolonlar: (boş, Düzenle) / Finansal Kurum / Para Birimi /
 *    Çalışma Modeli / Durum / İskonto Finansal Kurum / DBS Maliyet (%) / First Loss (%) /
 *    Fiyat (%) / Otomatik Teklif Formülü / Ek Vade Tipi / Fatura Vade Limiti / Maliyet Alıcıdan /
 *    Limit Üstü Gönderim / Güncelleme Tarihi / Güncelleyen Kullanıcı.
 *  - "Anlaşmalı Finansal Kurum Ekle" (CompanyDisplayFactoringsView.7) → CompanyAddEditFactoringDialog(null)
 *  - Satır "Düzenle" (DisplayIntegrationUrlView.7) → CompanyAddEditFactoringDialog(companyFactoring)
 */
public class CompanyDisplayFactoringsPage extends BasePageObject {

    private static final By GRID = By.cssSelector("vaadin-grid");

    public CompanyDisplayFactoringsPage(WebDriver driver) {
        super(driver);
    }

    /** Sidebar'daki "Finansal Kurumlar" butonuna tıklar (DTS listesindeki navigateToDts ile aynı kalıp). */
    public boolean navigateToFactorings() {
        tryOpenNavigationDrawer();
        waitForSidebarPopulated(8);
        dumpAllSidebarButtonTexts();
        if (clickSidebarButtonByText("finansal kurumlar")) {
            waitForVaadinNavigation();
            return true;
        }
        // ⚠️ 2026-08-06 canlı gözlem: "Finansal Kurumlar" DOM'da hazır DEĞİL — CompanyDealerMenuView
        // grubu üst-nav'da tek bir toplayıcı "Doğrudan Tahsilat" (SADECE bu metin, "...Sistemi"
        // İLE KARIŞTIRILMAMALI — o ayrı bir buton) butonunun ARKASINDA lazy render ediliyor.
        // Önce grup başlığına EXACT (fold) eşleşmeyle tıklanıp alt-menü açılmalı.
        boolean groupOpened = clickSidebarButtonByExactFold("dogrudan tahsilat");
        log.info("[DTS-MD] 'Doğrudan Tahsilat' üst grup başlığı tıklandı mı: {}", groupOpened);
        if (groupOpened) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            dumpAllSidebarButtonTexts();
            if (clickSidebarButtonByText("finansal kurumlar")) {
                waitForVaadinNavigation();
                return true;
            }
        }
        if (clickNavItemByText("finansal kurumlar")) {
            waitForVaadinNavigation();
            return true;
        }
        log.warn("[DTS-MD] 'Finansal Kurumlar' menüsü bulunamadı.");
        return false;
    }

    /** Fold-EXACT eşleşen (ör. "doğrudan tahsilat" ≠ "doğrudan tahsilat sistemi") görünür vaadin-button'a tıklar. */
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

    /** Tanı amaçlı: mevcut TÜM görünür vaadin-button metinlerini loglar (privilege/label doğrulama). */
    private void dumpAllSidebarButtonTexts() {
        try {
            Object texts = ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button');" +
                    "return Array.from(btns).map(function(b){" +
                    "  var r = b.getBoundingClientRect();" +
                    "  return (r.width>2&&r.height>2?'[V]':'[H]') + (b.textContent||'').replace(/\\s+/g,' ').trim();" +
                    "}).join(' || ');");
            log.info("[DTS-MD] Mevcut TÜM vaadin-button metinleri: {}", texts);
        } catch (Exception e) {
            log.debug("[DTS-MD] dumpAllSidebarButtonTexts: {}", e.getMessage());
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
                    log.info("[DTS-MD] Sidebar dolu görünüyor ({} öğe).", n);
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

    /** "Anlaşmalı Finansal Kurum Ekle" (CompanyDisplayFactoringsView.7) butonuna basar. */
    public boolean clickAddFactoringButton() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
        } catch (Exception ignored) {
        }
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var btns = document.querySelectorAll('vaadin-button');" +
                    "for (var b of btns) {" +
                    "  var r = b.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (fold(b.textContent) === 'anlasmali finansal kurum ekle') { b.click(); return true; }" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] 'Anlaşmalı Finansal Kurum Ekle' tıklandı mı: {}", clicked);
            return Boolean.TRUE.equals(clicked);
        } catch (Exception e) {
            log.warn("[DTS-MD] clickAddFactoringButton: {}", e.getMessage());
            return false;
        }
    }

    /** "Finansal Kurum" kolonu üzerinden kolon filtresi uygular (VaadinGridFilterHelper — DTS No ile aynı kalıp). */
    public boolean findFactoringRowByName(String factoringName) {
        return VaadinGridFilterHelper.applyOnlyValuesWithRetry(
                driver, "Finansal Kurum", Collections.singletonList(factoringName), 3);
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
     * click() ile basar (CompanyDtsListPage#clickOnlySingleRowActionButton ile aynı gerekçe:
     * y-bazlı satır gruplama toleransı, buton hücresini ayrı bir gruba düşürebiliyor).
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
                    "document.querySelectorAll('[data-fk-edit-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-fk-edit-target');});" +
                    "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                    "var found = null;" +
                    "for (var c of cells) {" +
                    "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (c.querySelector('.filter-header-cell')) continue;" +
                    "  var btn = c.querySelector('vaadin-button, button');" +
                    "  if (btn && !btn.disabled) { found = btn; break; }" +
                    "}" +
                    "if (!found) return false;" +
                    "found.setAttribute('data-fk-edit-target', '1');" +
                    "return true;");
            log.info("[DTS-MD] clickOnlySingleRowEditButton - buton isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement btn = driver.findElement(By.cssSelector("[data-fk-edit-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            btn.click();
            log.info("[DTS-MD] clickOnlySingleRowEditButton: gerçek Selenium click uygulandı.");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS-MD] clickOnlySingleRowEditButton: {}", e.getMessage());
            return false;
        }
    }

    /** Gridde (fold) verilen satır anahtar kelimesini içeren satırda, aynı zamanda değer anahtar
     *  kelimesinin de göründüğü anı poll eder (ör. "türkiye iş bankası" satırında "5,00" DBS Maliyet). */
    public boolean waitForRowContainingFold(String rowFoldKeyword, String valueFoldKeyword, int timeoutSeconds) {
        String escapedRow = rowFoldKeyword.replace("'", "\\'");
        String escapedVal = valueFoldKeyword.replace("'", "\\'");
        return VaadinGridFilterHelper.waitForJs(driver, timeoutSeconds,
                "function fold(s){return (s||'')" +
                ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                "var rowTarget = fold('" + escapedRow + "');" +
                "var valTarget = fold('" + escapedVal + "');" +
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
                "var keys = Object.keys(rows).map(Number);" +
                "for (var i = 0; i < keys.length; i++) {" +
                "  var rowCells = rows[keys[i]];" +
                "  var joined = fold(rowCells.map(function(c){return c.textContent||'';}).join(' '));" +
                "  if (joined.indexOf(rowTarget) < 0) continue;" +
                "  if (joined.indexOf(valTarget) >= 0) return true;" +
                "}" +
                "return false;");
    }

    /**
     * Grid ZATEN bir kolon filtresiyle tek satıra indirgenmişken (ör. "Finansal Kurum" filtresi ile
     * hedef kuruma daraltılmış), o satırdaki HERHANGİ bir hücrenin (fold) verilen değeri içerip
     * içermediğini poll eder — satır-satır gruplama YAPMAZ.
     *
     * ⚠️ KÖK NEDEN (2026-08-07, canlı koşumla kanıtlandı): {@link #waitForRowContainingFold} aynı
     * satırdaki hücreleri y-koordinatına göre (3px bucket) gruplayıp hem satır adının hem değerin
     * AYNI bucket'ta olmasını arıyor. Grid, "Güncelle" sonrası arka planda yeniden render olurken
     * (Vaadin virtual-scroll) bucket'lar geçici olarak tutarsızlaşabiliyor — 40 saniyelik pollingin
     * TAMAMI boyunca sürekli false dönüp, poll bitip AYRI bir {@link #dumpVisibleRowsGrouped()}
     * çağrısı yapılınca (kendi başına, tek seferlik, tazelenmiş DOM ile) satırın doğru göründüğü
     * görüldü — yani veri ZATEN doğruydu, sadece bucket'lama kırılgandı. Filtre zaten tek satıra
     * indirdiği için satır-gruplama gereksiz risk taşıyor; bu metod onu atlayıp güvenilir çalışıyor.
     */
    public boolean waitForAnyVisibleCellFold(String valueFoldKeyword, int timeoutSeconds) {
        String escapedVal = valueFoldKeyword.replace("'", "\\'");
        return VaadinGridFilterHelper.waitForJs(driver, timeoutSeconds,
                "function fold(s){return (s||'')" +
                ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                "var valTarget = fold('" + escapedVal + "');" +
                "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                "for (var c of cells) {" +
                "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                "  if (c.querySelector('.filter-header-cell')) continue;" +
                "  if (fold(c.textContent).indexOf(valTarget) >= 0) return true;" +
                "}" +
                "return false;");
    }
}
