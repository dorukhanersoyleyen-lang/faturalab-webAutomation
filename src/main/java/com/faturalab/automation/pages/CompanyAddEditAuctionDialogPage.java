package com.faturalab.automation.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Tedarikçi — "Yeni Teklif Talebi Başlat / Taslak Teklif Talebi Bilgileri" diyaloğu
 * (kaynak: {@code CompanyAddEditAuctionDialog}, {@code origin/vaadin-24} READ-ONLY incelendi).
 *
 * ⚠️ Bu dialog {@link CompanyQuickOfferPage#clickTeklifAlForInvoice} ile AÇILAN AYNI dialogdur
 * (fatura satırındaki "TEKLİF AL" butonu bu dialogu açar — TZF/DFP zaten bu mekanizmayı kullanıyor).
 * DTF'e özel eklenen tek şey: {@code dtfButton} ("DTF Başlat"). Kaynak kod kanıtı: tedarikçinin
 * ÜST alıcıyla arasındaki {@code Supplier.requiredDtf=true} + DTF tarih aralığı içindeyse normal
 * "Başlat" ({@code startButton}) butonu DEVRE DIŞI bırakılır — ⚠️ CANLI KOŞUMLA DÜZELTİLDİ
 * (2026-08-13): bu butonun bu ekrandaki GERÇEK etiketi "Başlat" DEĞİL, **"Teklif Al"**'dır (dialog
 * "TEKLİF AL" satır butonuyla açıldığı için buton metni bağlama göre değişiyor) — canlı buton
 * dökümüyle kanıtlandı: `[Teklif Al,disabled=true]` (requireddtf=true iken), `[DTF Başlat,disabled=false]`.
 */
public class CompanyAddEditAuctionDialogPage extends BasePageObject {

    public CompanyAddEditAuctionDialogPage(WebDriver driver) {
        super(driver);
    }

    /**
     * "Teklif Talebine Dahil Edilecek Faturalar" gridinde verilen fatura numarasının "Seç"
     * checkbox'ını işaretler. ⚠️ 2026-08-13 canlı koşum kanıtı: bu dialog TEK fatura değil, aynı
     * alıcıya ait TÜM bekleyen faturaları listeler (aynı gün tekrar tekrar koşulunca birikir) —
     * "TEKLİF AL" satır bazlı tıklama dialogu açar ama hedef faturanın "Seç" checkbox'ı OTOMATİK
     * işaretlenmez; işaretlenmeden "DTF Başlat"/"Teklif Al" ikisi de DISABLED kalır (hiç seçim yok).
     * Checkbox hücresi, değer hücresinin HEMEN ÖNÜNDE ayrı hücrede (bkz. web-automation.md § Kolon
     * filtre dialogu — aynı Vaadin grid kalıbı).
     */
    public boolean selectInvoiceCheckbox(String invoiceNo) {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var invoiceNo = arguments[0];" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var cells = Array.from(o.querySelectorAll('vaadin-grid-cell-content'));" +
                    "  var targetIdx = -1;" +
                    "  for (var i = 0; i < cells.length; i++) {" +
                    "    if ((cells[i].textContent || '').includes(invoiceNo)) { targetIdx = i; break; }" +
                    "  }" +
                    "  if (targetIdx < 0) continue;" +
                    "  for (var k = Math.max(0, targetIdx - 4); k < targetIdx; k++) {" +
                    "    var cb = cells[k].querySelector('vaadin-checkbox');" +
                    "    if (cb) {" +
                    "      if (!cb.checked) { cb.click(); }" +
                    "      return true;" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return false;",
                    invoiceNo);
            log.info("[DTF] '{}' faturasının Seç checkbox'ı işaretlendi mi: {}", invoiceNo, clicked);
            if (Boolean.TRUE.equals(clicked)) {
                Thread.sleep(800);
            }
            return Boolean.TRUE.equals(clicked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTF] selectInvoiceCheckbox ({}): {}", invoiceNo, e.getMessage());
            return false;
        }
    }

    /** "Teklif Al" (CompanyAddEditAuctionDialog.startButton — bu ekrandaki gerçek etiket) ve
     *  "DTF Başlat" butonlarının enabled/disabled durumunu döker. */
    public String[] getStartAndDtfButtonState() {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "var startState = 'NOT_FOUND', dtfState = 'NOT_FOUND';" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    var t = fold(b.textContent);" +
                    "    if (t === 'teklif al') { startState = b.disabled ? 'DISABLED' : 'ENABLED'; }" +
                    "    if (t === 'dtf baslat') { dtfState = b.disabled ? 'DISABLED' : 'ENABLED'; }" +
                    "  }" +
                    "}" +
                    "return [startState, dtfState];");
            @SuppressWarnings("unchecked")
            java.util.List<Object> list = (java.util.List<Object>) result;
            String[] states = {String.valueOf(list.get(0)), String.valueOf(list.get(1))};
            log.info("[DTF] 'Teklif Al' durumu: {} | 'DTF Başlat' durumu: {}", states[0], states[1]);
            return states;
        } catch (Exception e) {
            log.warn("[DTF] getStartAndDtfButtonState: {}", e.getMessage());
            return new String[]{"ERROR", "ERROR"};
        }
    }

    /**
     * "Teklif Talebi Süresi" (auctionTimeSelect) alanından verilen süreyi seçer (ör. "1 Saat").
     * ⚠️ KÖK NEDEN (2026-08-13, canlı koşumla kanıtlandı — kullanıcı gözlemiyle bulundu): bu alan
     * varsayılan BOŞ gelir (auction.timeIndex=0); kaynak kod kanıtı
     * ({@code CompanyAddEditAuctionDialog.dtfButtonClickListener}): {@code if (auction.getTimeIndex()
     * == 0) { Alert.warning("DTF teklifi en az 1 saatlik başlatılmalıdır."); return; }} — süre
     * seçilmeden "DTF Başlat" tıklanınca uygulama SESSİZCE (dialog geçişi olmadan) bu uyarıyı
     * gösterip hiçbir şey yapmaz; otomasyon bunu bir timeout/hata olarak yorumlar. Fix: tıklamadan
     * ÖNCE bu alanı doldur. Vaadin {@code vaadin-select} açılınca DOM'a AYRI bir
     * {@code vaadin-select-overlay} eklenir (dialog overlay'inin İÇİNDE değil) — iki adımlı JS
     * (aç → poll et → öğeye tıkla) gerekir.
     */
    public boolean selectTeklifTalebiSuresi(String optionFoldText) {
        try {
            Boolean opened = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var selects = o.querySelectorAll('vaadin-select');" +
                    "  for (var s of selects) {" +
                    "    var lbl = fold(s.getAttribute('label') || (s.querySelector('label') ? s.querySelector('label').textContent : ''));" +
                    "    if (lbl.indexOf('teklif talebi suresi') >= 0) { s.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTF] 'Teklif Talebi Süresi' dropdown açıldı mı: {}", opened);
            if (!Boolean.TRUE.equals(opened)) {
                return false;
            }
            Thread.sleep(900);
            String target = optionFoldText.toLowerCase();
            Boolean picked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var target = arguments[0];" +
                    // ⚠️ Vaadin 24 Select overlay yapısı belirsiz (vaadin-select-overlay İÇİNDE
                    // vaadin-select-item / vaadin-item / [role=option] olabilir, sürüme göre
                    // değişebilir) — bu yüzden GENİŞ bir seçici seti + fallback deneniyor.
                    "var roots = Array.from(document.querySelectorAll(" +
                    "  'vaadin-select-overlay, vaadin-select-list-box, [popover]'));" +
                    "for (var root of roots) {" +
                    "  var candidates = root.querySelectorAll(" +
                    "      'vaadin-select-item, vaadin-item, [role=\"option\"], li');" +
                    "  for (var it of candidates) {" +
                    "    if (fold(it.textContent) === target) { it.click(); return true; }" +
                    "  }" +
                    "}" +
                    // Fallback: overlay/list-box adı belirsizse, sayfadaki TÜM elementler arasında
                    // tam metin eşleşmesi olan ve en 'derin' (en az çocuklu) elementi bul.
                    "var all = Array.from(document.querySelectorAll('*'));" +
                    "var best = null;" +
                    "for (var el of all) {" +
                    "  if (el.children.length > 0) continue;" +
                    "  if (fold(el.textContent) === target) { best = el; break; }" +
                    "}" +
                    "if (best) { best.click(); return true; }" +
                    "return false;",
                    target);
            log.info("[DTF] '{}' süresi seçildi mi: {}", optionFoldText, picked);
            if (!Boolean.TRUE.equals(picked)) {
                Object diag = ((JavascriptExecutor) driver).executeScript(
                        "var o = document.querySelector('vaadin-select-overlay');" +
                        "if (!o) return 'NO_OVERLAY';" +
                        "return (o.textContent || '').replace(/\\s+/g,' ').trim().substring(0, 500);");
                log.warn("[DTF] TEŞHİS — 'Teklif Talebi Süresi' overlay içeriği: {}", diag);
            }
            if (Boolean.TRUE.equals(picked)) {
                Thread.sleep(500);
            }
            return Boolean.TRUE.equals(picked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTF] selectTeklifTalebiSuresi: {}", e.getMessage());
            return false;
        }
    }

    /** "DTF Başlat" butonuna tıklar. */
    public boolean clickDtfBaslat() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    if (fold(b.textContent) === 'dtf baslat') { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTF] 'DTF Başlat' butonuna tıklandı mı: {}", clicked);
            if (Boolean.TRUE.equals(clicked)) {
                Thread.sleep(1500);
            }
            return Boolean.TRUE.equals(clicked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTF] clickDtfBaslat: {}", e.getMessage());
            return false;
        }
    }

    /** Görünür dialogdaki TÜM buton metinlerini + disabled durumlarını döker (teşhis amaçlı). */
    public String dumpAllButtons() {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "var out = [];" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    out.push('[' + (b.textContent||'').replace(/\\s+/g,' ').trim() + ',disabled=' + b.disabled + ']');" +
                    "  }" +
                    "}" +
                    "return out.join(' | ');");
            String s = String.valueOf(result);
            log.info("[DTF] TEŞHİS: tüm buton dökümü: {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTF] dumpAllButtons: {}", e.getMessage());
            return "";
        }
    }

    public String dumpVisibleText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  return (o.textContent || '').replace(/\\s+/g,' ').trim().substring(0, 1000);" +
                    "}" +
                    "return '';");
            String s = String.valueOf(text);
            log.info("[DTF] Dialog metni (Teklif Talebi): {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTF] dumpVisibleText: {}", e.getMessage());
            return "";
        }
    }
}
