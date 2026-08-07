package com.faturalab.automation.pages;

import com.faturalab.automation.utils.VaadinGridFilterHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Tedarikçi (Company) — DTS (Doğrudan Tahsilat Sistemi) liste ekranı (FAZ 1).
 *
 * Kaynak kod eşlemesi (bkz. output/reports/dts_source_code_ui_map_20260805.md):
 *  - Sidebar butonu: "Doğrudan Tahsilat Sistemi" (CompanyDashboardMenuView.30)
 *  - Grid: CompanyDtsView — kolonlar DTS No / Dönem / İşlem Durumu / Toplam Tutar
 *
 * ⚠️ FAZ 1 kapsamı SADECE navigasyon + liste görünürlüğü + kayıt doğrulama.
 * Ayar dialogu (CompanyDtsSettingsDialog), hesaplama (CompanyDetailDtsSettingsDialog),
 * onay (CompanyDtsAllocationDialog) SONRAKİ fazlarda eklenecek.
 */
public class CompanyDtsListPage extends BasePageObject {

    private static final By GRID = By.cssSelector("vaadin-grid");

    public CompanyDtsListPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Sidebar'daki "Doğrudan Tahsilat Sistemi" menüsüne tıklar.
     * TZF/DFP ile aynı kalıp: clickNavItemByText (shadow-DOM + light-DOM fallback).
     */
    public boolean navigateToDts() {
        tryOpenNavigationDrawer();
        // Şirkete özel sidebar (privilege'e göre dinamik üretilir) impersonation sonrası
        // hemen render olmayabilir — ilk denemede sadece footer linkleri (Üyelik Sözleşmesi/
        // KVKK) görünmesi "sidebar henüz yüklenmedi" belirtisi (2026-08-05 canlı gözlem).
        // Gerçek menü öğeleri (>4 link) görünene kadar kısa poll.
        waitForSidebarPopulated(8);
        if (clickNavItemByText("doğrudan tahsilat sistemi") || clickNavItemByText("dogrudan tahsilat sistemi")) {
            waitForVaadinNavigation();
            return true;
        }
        // Kısaltılmış anahtar kelime fallback (menü metni "Doğrudan Tahsilat Sistemi" ile
        // eşleşmezse — örn. terminoloji sapması "...Yönetimi" ihtimaline karşı)
        if (clickNavItemByText("doğrudan tahsilat") || clickNavItemByText("dogrudan tahsilat")) {
            waitForVaadinNavigation();
            return true;
        }
        // Sidebar buton tabanlıysa (Admin tarzı vaadin-button) da dene — Company sidebar'ının
        // bu hesap için hangi bileşeni kullandığı canlı doğrulanana kadar iki yol da denenir.
        if (clickSidebarButtonByText("doğrudan tahsilat sistemi")
                || clickSidebarButtonByText("dogrudan tahsilat sistemi")) {
            waitForVaadinNavigation();
            return true;
        }
        log.warn("[DTS] 'Doğrudan Tahsilat Sistemi' menüsü bulunamadı.");
        return false;
    }

    /** Sidebar'da (a[href], vaadin-side-nav-item, vaadin-tab, vaadin-button) anlamlı sayıda öğe görünene kadar poll eder. */
    private void waitForSidebarPopulated(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                Object count = ((JavascriptExecutor) driver).executeScript(
                        "var sel = 'vaadin-side-nav-item, vaadin-tab, a[href], vaadin-button';" +
                        "return document.querySelectorAll(sel).length;");
                long n = count instanceof Number ? ((Number) count).longValue() : 0;
                if (n > 4) {
                    log.info("[DTS] Sidebar dolu görünüyor ({} öğe).", n);
                    return;
                }
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ignored) {
            }
        }
        log.warn("[DTS] Sidebar {} sn içinde dolmadı — mevcut haliyle denenecek.", timeoutSeconds);
    }

    /** Admin-tarzı vaadin-button sidebar öğelerinde metin eşleşmesiyle tıklar. */
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
            log.debug("[DTS] clickSidebarButtonByText '{}': {}", keyword, e.getMessage());
            return false;
        }
    }

    /** DTS listesi (CompanyDtsView) grid'i görünür mü. */
    public boolean isGridVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(22))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            return !driver.findElements(GRID).isEmpty();
        } catch (Exception e) {
            log.warn("[DTS] Grid görünürlüğü doğrulanamadı: {}", e.getMessage());
            return !driver.findElements(GRID).isEmpty();
        }
    }

    /**
     * Gridde verilen DTS referans numarasının (ör. DTS_10) görünür bir hücrede
     * geçip geçmediğini kontrol eder. Virtual scroll cache hücrelerini
     * ({@code getBoundingClientRect} < 2px) elemek için görünürlük şartı konur.
     */
    public boolean isReferenceVisibleInGrid(String referenceNo) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            Thread.sleep(1500); // Vaadin grid asenkron satır render'ı
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[DTS] isReferenceVisibleInGrid grid beklemesi: {}", e.getMessage());
        }
        try {
            Boolean found = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var needle = arguments[0].toLowerCase();" +
                    "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                    "for (var c of cells) {" +
                    "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if ((c.textContent || '').toLowerCase().includes(needle)) return true;" +
                    "}" +
                    "return false;",
                    referenceNo);
            log.info("[DTS] Grid araması '{}': {}", referenceNo, found);
            return Boolean.TRUE.equals(found);
        } catch (Exception e) {
            log.warn("[DTS] isReferenceVisibleInGrid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gridde en az bir DTS kaydı (DTS_&lt;sayı&gt; formatında "DTS No" hücresi) görünür mü.
     *
     * ⚠️ 2026-08-05 canlı gözlem: DTS listesi varsayılan olarak "Başlama Tarihi Son 7 gün"
     * filtre çipiyle açılır — bu yüzden eski kayıtlar (ör. DTS_10) ilk sayfada GÖRÜNMEZ,
     * sadece son 7 güne ait olanlar (DTS_60+) görünür. FAZ 1 kapsamında filtre kaldırma/
     * genişletme henüz yazılmadı; bu metod "liste dolu ve gerçek veri render ediyor" genel
     * doğrulaması için kullanılır (belirli bir referenceno değil).
     */
    public boolean hasAnyDtsRowVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[DTS] hasAnyDtsRowVisible grid beklemesi: {}", e.getMessage());
        }
        try {
            Boolean found = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var re = /DTS_\\d+/;" +
                    "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                    "for (var c of cells) {" +
                    "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (re.test(c.textContent || '')) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTS] Gridde en az bir DTS_ kaydı görünür mü: {}", found);
            return Boolean.TRUE.equals(found);
        } catch (Exception e) {
            log.warn("[DTS] hasAnyDtsRowVisible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * FAZ 2 tanı aracı: görünür grid satırlarını hücreleri satır-y-konumuna göre gruplayarak
     * ("header hücreleri" {@code .filter-header-cell} içerenler hariç) döker. Her string bir
     * satırın hücre metinlerini {@code " || "} ile birleştirir. Header/veri ayrımı olmadan
     * kolon varsayımına dayanmadan çalışır (virtual-scroll'a dayanıklı — sadece görünür rect).
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> dumpVisibleRowsGrouped() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            Thread.sleep(1500);
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
            java.util.List<String> rows = (java.util.List<String>) result;
            log.info("[DTS] Görünür satır dökümü ({} satır):", rows.size());
            for (String r : rows) {
                log.info("[DTS]   {}", r);
            }
            return rows;
        } catch (Exception e) {
            log.warn("[DTS] dumpVisibleRowsGrouped: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Gridde durumu (ASCII-fold) verilen anahtar kelimeyi içeren bir satır arar ve o satırdaki
     * (görünür) butona tıklar. Satır grubu {@link #dumpVisibleRowsGrouped()} ile aynı y-bazlı
     * gruplama mantığını kullanır (kolon sırasına bağlı değil).
     *
     * @param statusFoldKeyword ör. "taslak" (DRAFT) veya "onay bekliyor" (PENDING)
     * @return buton bulunup tıklandıysa true
     */
    public boolean clickActionButtonForRowWithStatus(String statusFoldKeyword) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var target = fold(arguments[0]);" +
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
                    "for (var i = 0; i < keys.length; i++) {" +
                    "  var rowCells = rows[keys[i]];" +
                    "  var matched = rowCells.some(function(c) { return fold(c.textContent) === target; });" +
                    "  if (!matched) continue;" +
                    "  for (var j = 0; j < rowCells.length; j++) {" +
                    "    var btn = rowCells[j].querySelector('vaadin-button, button');" +
                    "    if (btn && !btn.disabled) { btn.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;", statusFoldKeyword);
            log.info("[DTS] clickActionButtonForRowWithStatus('{}'): {}", statusFoldKeyword, clicked);
            return Boolean.TRUE.equals(clicked);
        } catch (Exception e) {
            log.warn("[DTS] clickActionButtonForRowWithStatus: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Dashboard üst filtre kutusundaki ("Filtre seç" placeholder'lı {@code vaadin-select})
     * boş seçimi seçerek varsayılan tarih filtresini (ör. "Son 7 gün") kaldırmayı dener.
     * Canlı DOM'da doğrulanmamış — FAZ 2 keşif amaçlı, başarısız olursa false döner ve
     * çağıran taraf mevcut (filtreli) görünümle devam eder.
     */
    public boolean tryClearDateFilter() {
        try {
            Boolean opened = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var sels = Array.from(document.querySelectorAll('vaadin-select'));" +
                    "for (var s of sels) {" +
                    "  var r = s.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var ph = (s.placeholder || s.getAttribute('placeholder') || '');" +
                    "  if (ph.toLowerCase().indexOf('filtre') >= 0) { s.click(); return true; }" +
                    "}" +
                    "return false;");
            log.info("[DTS] tryClearDateFilter - filtre select acildi mi: {}", opened);
            if (!Boolean.TRUE.equals(opened)) {
                return false;
            }
            Thread.sleep(800);
            Boolean cleared = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-select-overlay');" +
                    "if (!overlay) return false;" +
                    "var items = Array.from(overlay.querySelectorAll('vaadin-item, vaadin-select-item'));" +
                    "for (var it of items) {" +
                    "  var t = (it.textContent || '').replace(/\\s+/g,' ').trim();" +
                    "  if (t.length === 0 || t.toLowerCase().indexOf('filtre se') >= 0) { it.click(); return true; }" +
                    "}" +
                    "return false;");
            log.info("[DTS] tryClearDateFilter - bos secim tiklandi mi: {}", cleared);
            if (Boolean.TRUE.equals(cleared)) {
                Thread.sleep(1500);
            }
            return Boolean.TRUE.equals(cleared);
        } catch (Exception e) {
            log.warn("[DTS] tryClearDateFilter: {}", e.getMessage());
            return false;
        }
    }

    /**
     * FAZ 3 — kaynak kod kanıtlı çözüm (CompanyAuctionHeaderFilterView.java): varsayılan
     * "Başlama Tarihi Son 7 gün" filtresi sunucu tarafında {@code getFilteredAllDts(filter,...)}
     * ile uygulanıyor (grid'in kendi TableFilterManager kolon filtreleri DEĞİL — o client-side'dır
     * ve sunucudan hiç gelmeyen eski kayıtları göstermez). Aktif filtre bir "filter-item-box" chip'i
     * olarak render edilir; içindeki ikon-only (LUMO_TERTIARY, DELETE_BLACK) buton'a
     * {@code removeButton.addClickListener} ile {@code currentFilter}'dan tarih aralığını kaldırıp
     * {@code refresh(true)} çağırır. JS {@code .click()} ile DOM'u gizlemek bu sunucu-state
     * güncellemesini TETİKLEMEZ — bu yüzden butonu JS ile bulup {@code data-dts-remove-target}
     * ile işaretleyip GERÇEK Selenium {@code WebElement.click()} ile tıklıyoruz.
     */
    public boolean removeDefaultDateFilterChip() {
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-dts-remove-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dts-remove-target');});" +
                    "var boxes = document.querySelectorAll('.filter-item-box');" +
                    "for (var b of boxes) {" +
                    "  var r = b.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = fold(b.textContent);" +
                    "  if (t.indexOf('baslama tarihi') >= 0 || t.indexOf('son 7') >= 0) {" +
                    "    var btn = b.querySelector('vaadin-button, button');" +
                    "    if (btn) { btn.setAttribute('data-dts-remove-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS] removeDefaultDateFilterChip - hedef buton isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement removeBtn = driver.findElement(By.cssSelector("[data-dts-remove-target='1']"));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", removeBtn);
            Thread.sleep(300);
            removeBtn.click(); // gerçek Selenium click — Vaadin removeButton click listener'ını tetikler
            log.info("[DTS] Tarih filtre çipi gerçek Selenium click ile kaldırıldı.");
            Thread.sleep(2000); // Vaadin round-trip + grid yeniden yükleme
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS] removeDefaultDateFilterChip: {}", e.getMessage());
            return false;
        }
    }

    /**
     * FAZ 3 canlı gözlem: "DTS No" kolon filtresiyle grid tam olarak 1 kayda indirildiğinde
     * {@link #clickActionButtonForRowWithStatus(String)}'in y-bazlı satır gruplama toleransı
     * (üç pikselli yuvarlama) buton hücresini metin hücrelerinden AYRI bir gruba düşürebiliyor
     * (buton bileşeninin render yüksekliği text hücrelerinden birkaç piksel farklı) — filtre
     * tam 1 satıra indiğinde bu ayrım anlamsız, gridde görünür TEK satır aksiyon butonuna
     * (Devam Et / Gözat) doğrudan basılır.
     */
    public boolean clickOnlySingleRowActionButton() {
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
                    "document.querySelectorAll('[data-dts-single-row-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dts-single-row-target');});" +
                    "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                    "var found = null;" +
                    "for (var c of cells) {" +
                    "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (c.querySelector('.filter-header-cell')) continue;" +
                    "  var btn = c.querySelector('vaadin-button, button');" +
                    "  if (btn && !btn.disabled) { found = btn; break; }" +
                    "}" +
                    "if (!found) return false;" +
                    "found.setAttribute('data-dts-single-row-target', '1');" +
                    "return true;");
            log.info("[DTS] clickOnlySingleRowActionButton - buton isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement btn = driver.findElement(By.cssSelector("[data-dts-single-row-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            btn.click();
            log.info("[DTS] clickOnlySingleRowActionButton: gerçek Selenium click uygulandı.");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS] clickOnlySingleRowActionButton: {}", e.getMessage());
            return false;
        }
    }

    /**
     * FAZ 3 orkestrasyonu: (1) varsayılan tarih çipini kaldır, (2) grid'in kendi
     * TableFilterManager kolon filtresini ("DTS No") {@link VaadinGridFilterHelper} ile
     * verilen referans numarasına uygula. İkinci adım BİRİNCİ adım olmadan işe yaramaz —
     * kolon filtresi sadece sunucudan zaten gelmiş (client-side) satırlar üzerinde çalışır.
     */
    public boolean findDraftDtsByReferenceNo(String referenceNo) {
        boolean chipRemoved = removeDefaultDateFilterChip();
        log.info("[DTS] findDraftDtsByReferenceNo - tarih cipi kaldirildi mi: {}", chipRemoved);
        if (!chipRemoved) {
            log.warn("[DTS] Tarih filtre çipi kaldırılamadı — {} muhtemelen görünmeyecek.", referenceNo);
        }
        boolean filtered = VaadinGridFilterHelper.applyOnlyValuesWithRetry(
                driver, "DTS No", java.util.Collections.singletonList(referenceNo), 3);
        log.info("[DTS] findDraftDtsByReferenceNo - '{}' kolon filtresi uygulandi mi: {}", referenceNo, filtered);
        return filtered;
    }

    /**
     * Gridde (fold'lu) verilen statü anahtar kelimesinin görünür bir hücrede belirmesini
     * poll eder — "Başlat" sonrası statü Hesaplanıyor'a (CALCULATING) geçişini doğrulamak için.
     * Sabit sleep DEĞİL; TZF'deki pollForBordroNo kalıbıyla aynı mantık.
     */
    public boolean waitForStatusFold(String statusFoldKeyword, int timeoutSeconds) {
        String escaped = statusFoldKeyword.replace("'", "\\'");
        return VaadinGridFilterHelper.waitForJs(driver, timeoutSeconds,
                "function fold(s){return (s||'')" +
                ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                "var target = fold('" + escaped + "');" +
                "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                "for (var c of cells) {" +
                "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                "  if (fold(c.textContent||'').indexOf(target) >= 0) return true;" +
                "}" +
                "return false;");
    }
}
