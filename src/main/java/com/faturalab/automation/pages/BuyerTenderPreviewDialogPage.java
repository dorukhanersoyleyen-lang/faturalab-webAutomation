package com.faturalab.automation.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * "İhale Başlat" tıklandıktan sonra açılan "İhale Ön İzleme" onay diyaloğu
 * (kaynak: {@code BuyerTenderPreviewDialog}, {@code origin/vaadin-24} READ-ONLY incelendi).
 *
 * ⚠️ Kaynak kod kanıtı: bu dialog bir GRID (Grid.addColumn ile "İhale Tutarı" / "Katılım Tutarı
 * Toplamı" / "İhaleye Katılabilecek Tedarikçi Sayısı" (t.getTotalSupplierCount()) / "Teklife
 * Dahil Edilebilecek Bordro Sayısı" / "Teklife Dahil Edilebilecek Fatura Sayısı" kolonlarıyla) —
 * basit etiket-değer çiftleri değil. "Evet" butonu ({@code BuyerTenderPreviewDialog.8}) tıklanınca
 * {@code TenderModel.startDtfAuctionAndDtfTender(...)} çalışır ve DTF Tender+Auction PERSIST olur
 * (bu adım GERİ ALINAMAZ).
 */
public class BuyerTenderPreviewDialogPage extends BasePageObject {

    public BuyerTenderPreviewDialogPage(WebDriver driver) {
        super(driver);
    }

    /** {@link #isOpened()}'ı verilen süre boyunca poll eder. */
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

    /** "İhale başlatmak istediğinizden emin misiniz?" onay metni görünüyorsa dialog açık demektir. */
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
                    "  if (t.indexOf('ihale baslatmak istediginizden emin misiniz') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTF] BuyerTenderPreviewDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTF] isOpened (BuyerTenderPreviewDialog): {}", e.getMessage());
            return false;
        }
    }

    public String dumpVisibleText() {
        try {
            // ⚠️ 2026-08-13 canlı koşumla DÜZELTİLDİ: bu noktada DOM'da AYNI ANDA 3 overlay birden
            // görünür olabilir (eski Teklif Talebi dialogu + BuyerStartDtfTenderDialog + bu dialog —
            // hiçbiri geçişte kapanmıyor). "İlk görünür overlay" mantığı yanlış (eski) dialogu
            // yakalıyordu — içerik filtresi (onay metni) ile doğru overlay'i hedefle.
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (fold(o.textContent).indexOf('ihale baslatmak istediginizden emin misiniz') < 0) continue;" +
                    "  return (o.textContent || '').replace(/\\s+/g,' ').trim().substring(0, 1500);" +
                    "}" +
                    "return '';");
            String s = String.valueOf(text);
            log.info("[DTF] Dialog metni (BuyerTenderPreviewDialog): {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTF] dumpVisibleText (BuyerTenderPreviewDialog): {}", e.getMessage());
            return "";
        }
    }

    /**
     * "İhaleye Katılabilecek Tedarikçi Sayısı" grid hücresinin metnini okur.
     * ⚠️ 2026-08-13 canlı koşumla DÜZELTİLDİ: "header'dan sonraki İLK sayısal hücre" sezgisi YANLIŞ
     * sonuç veriyordu — canlı grid'de 6 header hücresi ardından 6 değer hücresi (AYNI sırada) geliyor
     * (İhale Tutarı | Katılım Tutarı Toplamı | ...Ortalama Vade | İhaleye Katılabilecek Tedarikçi
     * Sayısı | Bordro Sayısı | Fatura Sayısı → değerler: "6,691.00 TL" | "6,691.00 TL" | "32" | "2" |
     * "1" | "1"). Hedef header 4. sırada ama "sonraki ilk sayısal hücre" olan "32" (Ortalama Vade
     * DEĞERİ, gün sayısı) yanlışlıkla eşleşiyordu — asıl "2" bir hücre sonra. Doğru yaklaşım: header
     * satırının toplam hücre sayısını (ilk rakam içeren hücreye kadar) bul, hedef header'ın header
     * satırındaki pozisyonunu AYNI OFSET ile değer satırına uygula (kolon hizalaması).
     */
    public String getKatilimciSayisiText() {
        try {
            Object val = ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (fold(o.textContent).indexOf('ihale baslatmak istediginizden emin misiniz') < 0) continue;" +
                    "  var cells = Array.from(o.querySelectorAll('vaadin-grid-cell-content'));" +
                    "  var headerIdx = -1;" +
                    "  for (var i = 0; i < cells.length; i++) {" +
                    "    if (fold(cells[i].textContent).indexOf('katilabilecek tedarikci sayisi') >= 0) { headerIdx = i; break; }" +
                    "  }" +
                    "  if (headerIdx < 0) continue;" +
                    "  var headerCount = -1;" +
                    "  for (var k = 0; k < cells.length; k++) {" +
                    "    if (/\\d/.test((cells[k].textContent || ''))) { headerCount = k; break; }" +
                    "  }" +
                    "  if (headerCount < 0 || headerIdx >= headerCount) continue;" +
                    "  var valueIdx = headerCount + headerIdx;" +
                    "  if (valueIdx < cells.length) return (cells[valueIdx].textContent || '').trim();" +
                    "}" +
                    "return 'NOT_FOUND';");
            String s = String.valueOf(val);
            log.info("[DTF] 'İhaleye Katılabilecek Tedarikçi Sayısı' değeri: {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTF] getKatilimciSayisiText: {}", e.getMessage());
            return "ERROR";
        }
    }

    /** "Evet" butonuna tıklar — ⚠️ GERİ ALINAMAZ, DTF Tender+Auction gerçekten başlatılır. */
    public boolean clickEvet() {
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
                    "  if (t.indexOf('ihale baslatmak istediginizden emin misiniz') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    if (fold(b.textContent) === 'evet') { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTF] 'Evet' butonuna tıklandı mı: {}", clicked);
            if (Boolean.TRUE.equals(clicked)) {
                Thread.sleep(2000);
            }
            return Boolean.TRUE.equals(clicked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTF] clickEvet: {}", e.getMessage());
            return false;
        }
    }
}
