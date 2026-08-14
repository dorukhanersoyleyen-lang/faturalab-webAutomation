package com.faturalab.automation.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * "DTF Başlat" tıklandıktan sonra açılan "İhale Ayrıntıları" (DTF Tender) diyaloğu
 * (kaynak: {@code BuyerStartDtfTenderDialog}, {@code origin/vaadin-24} READ-ONLY incelendi).
 *
 * ⚠️ Kaynak kod kanıtı: {@code dtfAuctionAndBuyerComboBox}'un değer-değişim dinleyicisi Auction
 * ÖNCEDEN SEÇİLİ geldiğinde (bizim akışımızda hep öyle — auction zaten CompanyAddEditAuctionDialog'dan
 * seçilerek geliyor) TÜM alanları (DTF Oranı, İhale Tutarı, Bitiş Tarihi, Valör Tarihi, Saat) OTOMATİK
 * doldurur. "Katılımcılar" alanı varsayılan "Tedarikçiler" + "Tüm Tedarikçiler" checkbox'ı varsayılan
 * İŞARETLİ (selectedSuppliers boş başlar) — hiçbir alana dokunmadan doğrudan "İhale Başlat" (save
 * butonu, {@code BuyerStartTenderDialog.18}) tıklanabilir.
 */
public class BuyerStartDtfTenderDialogPage extends BasePageObject {

    public BuyerStartDtfTenderDialogPage(WebDriver driver) {
        super(driver);
    }

    /** {@link #isOpened()}'ı verilen süre boyunca poll eder (dialog render/sunucu round-trip gecikmesi için). */
    public boolean waitUntilOpened(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (isOpened()) {
                return true;
            }
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * "Katılımcılar" VE "İhale Bilgileri" etiketleri aynı anda görünüyorsa bu dialog açık demektir.
     * ⚠️ 2026-08-13 canlı koşumla DÜZELTİLDİ: gerçek dialog başlığı/alan adı "Bordro Bitiş Tarihi"
     * DEĞİL, sade "Bitiş Tarihi" — canlı buton/metin dökümüyle kanıtlandı ("İhale Bilgileri | DTF
     * İhale Alıcısı | ... | İhale Süresi | Periyot | Bitiş Tarihi | ... | Katılımcılar | Tüm
     * Tedarikçiler | Kapat | İhale Başlat | İhale Ayrıntıları").
     */
    public boolean isOpened() {
        try {
            Boolean opened = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = fold(o.textContent);" +
                    "  if (t.indexOf('katilimcilar') >= 0 && t.indexOf('ihale bilgileri') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTF] BuyerStartDtfTenderDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTF] isOpened (BuyerStartDtfTenderDialog): {}", e.getMessage());
            return false;
        }
    }

    public String dumpVisibleText() {
        try {
            // ⚠️ 2026-08-13 canlı koşumla DÜZELTİLDİ: "DTF Başlat" tıklandığında ALTTAKİ
            // CompanyAddEditAuctionDialog (Teklif Talebi) KAPANMIYOR, üstüne BuyerStartDtfTenderDialog
            // açılıyor — DOM'da İKİ overlay birden görünür (width/height>2). Sadece görünürlüğe bakan
            // "ilk eşleşen overlay" mantığı ESKİ dialogu yakalıyordu. Fix: içerik olarak "ihale
            // bilgileri" geçen overlay'i hedefle.
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (fold(o.textContent).indexOf('ihale bilgileri') < 0) continue;" +
                    "  return (o.textContent || '').replace(/\\s+/g,' ').trim().substring(0, 1200);" +
                    "}" +
                    "return '';");
            String s = String.valueOf(text);
            log.info("[DTF] Dialog metni (BuyerStartDtfTenderDialog): {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTF] dumpVisibleText (BuyerStartDtfTenderDialog): {}", e.getMessage());
            return "";
        }
    }

    /** "İhale Tutarı" alanının (tenderAmountField) o anki DOM değerini okur (0/boş ise auto-fill henüz oturmamış olabilir). */
    public String getTenderAmountFieldValue() {
        try {
            Object val = ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (fold(o.textContent).indexOf('ihale bilgileri') < 0) continue;" +
                    // ⚠️ 2026-08-13 canlı koşumla DÜZELTİLDİ: kaynak kod kanıtı — tenderAmountField
                    // (AmountField, TextField alt sınıfı) burada `setLabel(null)` ile AÇIKÇA label'sız
                    // bırakılmış (kaynak: BuyerStartDtfTenderDialog.java:515) — attribute/label eşleşmesi
                    // asla tutmaz. Tek ayırt edici iz: `setWidth("120px")` (satır 517) → inline
                    // style="width:120px" olarak DOM'a yansır (`f.width` DEĞİL, `f.style.width`).
                    "  var fields = o.querySelectorAll('vaadin-text-field, vaadin-number-field');" +
                    "  for (var f of fields) {" +
                    "    var lbl = fold(f.getAttribute('label') || '');" +
                    "    if (lbl.indexOf('ihale tutari') >= 0 || (lbl === '' && f.style && f.style.width === '120px')) {" +
                    "      return String(f.value);" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return 'NOT_FOUND';");
            return String.valueOf(val);
        } catch (Exception e) {
            log.warn("[DTF] getTenderAmountFieldValue: {}", e.getMessage());
            return "ERROR";
        }
    }

    /** "İhale Başlat" (save) butonuna tıklar. */
    public boolean clickIhaleBaslat() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = fold(o.textContent);" +
                    "  if (t.indexOf('katilimcilar') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    if (fold(b.textContent) === 'ihale baslat') { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTF] 'İhale Başlat' butonuna tıklandı mı: {}", clicked);
            if (Boolean.TRUE.equals(clicked)) {
                Thread.sleep(1500);
            } else {
                log.warn("[DTF] 'İhale Başlat' bulunamadı. Dialog metni: {}", dumpVisibleText());
            }
            return Boolean.TRUE.equals(clicked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTF] clickIhaleBaslat: {}", e.getMessage());
            return false;
        }
    }
}
