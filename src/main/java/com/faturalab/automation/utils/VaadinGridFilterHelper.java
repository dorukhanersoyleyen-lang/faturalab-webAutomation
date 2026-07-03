package com.faturalab.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * FaturaLab Vaadin grid'lerinin ortak kolon filtre dialogunu yönetir.
 *
 * Canlı DOM'da doğrulanmış yapı (2026-07-02, Kullanıcılar ve Yüklenmişler gridleri aynı):
 *  - Kolon başlığı: vaadin-grid-cell-content > .filter-header-cell > span.filter-icon + span.filter-text
 *  - Dialog: vaadin-dialog-overlay.table-filter-dialog
 *  - Arama: dialog içinde vaadin-text-field[label="Ara"] input
 *  - Liste: vaadin-grid.check-table — checkbox hücresi, değer hücresinin HEMEN ÖNÜNDE
 *  - Varsayılan: tüm değerler seçili (= filtre yok); "(Tümünü seç)" kaldırılıp hedefler işaretlenir
 *  - Onay: 'Tamam'
 */
public final class VaadinGridFilterHelper {

    private static final Logger log = LogManager.getLogger(VaadinGridFilterHelper.class);

    private VaadinGridFilterHelper() {
    }

    /**
     * Verilen kolonda yalnızca istenen değerler seçili kalacak şekilde filtre uygular
     * ve ilk değerin grid'de GÖRÜNÜR olmasını bekler.
     */
    public static boolean applyOnlyValues(WebDriver driver, String columnKeyword, List<String> values) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try {
            // 1. Kolon başlığındaki filtre ikonuna tıkla
            Object headerHit = js.executeScript(
                "var kw = arguments[0].toLowerCase();" +
                "var headers = document.querySelectorAll('vaadin-grid-cell-content .filter-header-cell');" +
                "for (var h of headers) {" +
                "  var txtEl = h.querySelector('span.filter-text');" +
                "  var t = ((txtEl ? txtEl.textContent : h.textContent) || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                "  if (t === kw || t.includes(kw)) {" +
                "    var icon = h.querySelector('span.filter-icon');" +
                "    (icon || h).click();" +
                "    return t;" +
                "  }" +
                "}" +
                "return null;", columnKeyword.toLowerCase(java.util.Locale.ROOT));
            if (headerHit == null) {
                log.warn("Filtre kolonu bulunamadı: {}", columnKeyword);
                return false;
            }
            log.info("Filtre ikonuna tıklandı: '{}'", headerHit);

            // 2. Dialog'u bekle
            if (!waitForJs(driver, 8,
                    "return !!document.querySelector('vaadin-dialog-overlay.table-filter-dialog');")) {
                log.warn("table-filter-dialog açılmadı ({}).", columnKeyword);
                return false;
            }

            // 3. "(Tümünü seç)" kaldır → tüm seçimler temizlenir
            Object unselectAll = js.executeScript(
                "var dlg = document.querySelector('vaadin-dialog-overlay.table-filter-dialog');" +
                "var cells = Array.from(dlg.querySelectorAll('vaadin-grid.check-table vaadin-grid-cell-content'));" +
                "var idx = cells.findIndex(function(c) { return (c.textContent||'').indexOf('münü seç') >= 0; });" +
                "if (idx < 1) return 'tumunu_sec_yok';" +
                "var cb = cells[idx - 1].querySelector('vaadin-checkbox, input[type=checkbox]');" +
                "if (!cb) return 'checkbox_yok';" +
                "if (cb.checked === true) { cb.click(); return 'temizlendi'; }" +
                "return 'zaten_bos';");
            log.info("Tümünü seç: {}", unselectAll);
            sleep(1200);

            // 4. Her değer için: Ara'ya yaz → eşleşen checkbox'ı işaretle
            for (String value : values) {
                Boolean typed = (Boolean) js.executeScript(
                    "var target = arguments[0];" +
                    "var dlg = document.querySelector('vaadin-dialog-overlay.table-filter-dialog');" +
                    "var inp = dlg.querySelector('vaadin-text-field[label=\"Ara\"] input');" +
                    "if (!inp) {" +
                    "  var inputs = Array.from(dlg.querySelectorAll('input[type=\"text\"], input:not([type])'));" +
                    "  inp = inputs.length ? inputs[inputs.length - 1] : null;" +
                    "}" +
                    "if (!inp) return false;" +
                    "inp.focus();" +
                    "inp.value = target;" +
                    "inp.dispatchEvent(new Event('input', {bubbles: true}));" +
                    "inp.dispatchEvent(new Event('change', {bubbles: true}));" +
                    "return true;", value);
                if (!Boolean.TRUE.equals(typed)) {
                    log.warn("Filtre 'Ara' kutusu bulunamadı.");
                    return false;
                }
                sleep(1500);

                Object checked = js.executeScript(
                    "var target = arguments[0].toLowerCase();" +
                    "var dlg = document.querySelector('vaadin-dialog-overlay.table-filter-dialog');" +
                    "var cells = Array.from(dlg.querySelectorAll('vaadin-grid.check-table vaadin-grid-cell-content'));" +
                    "var idx = cells.findIndex(function(c) {" +
                    "  var t = (c.textContent||'').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  return t.length > 0 && t.indexOf('münü seç') < 0 && t.includes(target);" +
                    "});" +
                    "if (idx < 1) return null;" +
                    "var cb = cells[idx - 1].querySelector('vaadin-checkbox, input[type=checkbox]');" +
                    "if (!cb) return null;" +
                    "if (cb.checked !== true) { cb.click(); return 'isaretlendi'; }" +
                    "return 'zaten_isaretli';", value);
                if (checked == null) {
                    log.warn("Filtre listesinde değer bulunamadı: {}", value);
                    return false;
                }
                log.info("Filtre değeri {}: {}", value, checked);
                sleep(700);
            }

            // 5. Tamam
            Boolean confirmed = (Boolean) js.executeScript(
                "var dlg = document.querySelector('vaadin-dialog-overlay.table-filter-dialog');" +
                "var btns = dlg.querySelectorAll('vaadin-button, button');" +
                "for (var b of btns) {" +
                "  if ((b.textContent || '').trim() === 'Tamam') { b.click(); return true; }" +
                "}" +
                "return false;");
            if (!Boolean.TRUE.equals(confirmed)) {
                log.warn("Filtre dialogu 'Tamam' bulunamadı.");
                return false;
            }

            // 6. İlk değerin görünür satırda belirmesini bekle
            String first = values.get(0).toLowerCase(java.util.Locale.ROOT).replace("'", "\\'");
            boolean visible = waitForJs(driver, 12,
                "var target = '" + first + "';" +
                "var cells = document.querySelectorAll('vaadin-grid vaadin-grid-cell-content');" +
                "for (var c of cells) {" +
                "  var r = c.getBoundingClientRect();" +
                "  if (r.width < 2 || r.height < 2) continue;" +
                "  if ((c.textContent||'').toLowerCase().includes(target)) return true;" +
                "}" +
                "return false;");
            log.info("Filtre uygulandı ({}), ilk değer görünür: {}", columnKeyword, visible);
            return visible;
        } catch (Exception e) {
            log.warn("applyOnlyValues ({}): {}", columnKeyword, e.getMessage());
            return false;
        }
    }

    /** Verilen JS koşulu true dönene kadar poll eder (500 ms aralıkla). */
    public static boolean waitForJs(WebDriver driver, int timeoutSeconds, String jsReturningBoolean) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                Object r = js.executeScript(jsReturningBoolean);
                if (Boolean.TRUE.equals(r)) {
                    return true;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
