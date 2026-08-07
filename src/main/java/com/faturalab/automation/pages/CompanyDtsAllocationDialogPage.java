package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Tedarikçi (Company) — DTS satırının statüsü DRAFT dışına geçtikten sonra listedeki "Gözat"
 * ({@code CompanyTendersView.7}) butonuyla açılan sonuç/onay diyaloğu
 * (kaynak: {@code CompanyDtsAllocationDialog}, başlık "Doğrudan Tahsilat Bilgileri" —
 * {@code CompanyDtsAllocationDialog.0}, Company **ve** Admin {@code AdminDtsDialog} aynı başlığı
 * paylaşır).
 *
 * Kaynak kod kanıtı ({@code CompanyDtsAllocationDialog.buildCalculateButton()}):
 * "Onayla" ({@code .25}) butonu SADECE statü CALCULATING veya PENDING iken eklenir
 * ({@code addStartButton}) — tıklanınca "Onay" başlıklı ConfirmDialog ({@code .26}=
 * "DTS işlemini onaylamak istediğinize emin misiniz?") açılır; "Evet" ile
 * {@code DtsModel.approvedDts()} çağrılır ve statü **SEND** (Gönderiliyor) olur.
 */
public class CompanyDtsAllocationDialogPage extends BasePageObject {

    public CompanyDtsAllocationDialogPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Diyaloğun açık olup olmadığını görünür metinle doğrular: "doğrudan tahsilat bilgileri"
     * başlığı VE "özet bilgileri" accordion'u aynı anda görünür olmalı (Company/Admin ortak).
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
                    "  if (t.indexOf('dogrudan tahsilat bilgileri') >= 0 && t.indexOf('ozet bilgileri') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTS] CompanyDtsAllocationDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTS] isOpened: {}", e.getMessage());
            return false;
        }
    }

    /** Diyalog içeriğinin ilk 800 karakterini debug için loglar (locator kanıtı). */
    public String dumpVisibleText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  return (o.textContent || '').replace(/\\s+/g,' ').trim().substring(0, 800);" +
                    "}" +
                    "return '';");
            String s = String.valueOf(text);
            log.info("[DTS] CompanyDtsAllocationDialog metni: {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTS] dumpVisibleText: {}", e.getMessage());
            return "";
        }
    }

    /**
     * "Onayla" butonuna basar ve açılan "Onay" ConfirmDialog'unu ("DTS işlemini onaylamak
     * istediğinize emin misiniz?", Evet/Hayır) onaylar. Geri alınamaz — statü SEND'e (Gönderiliyor)
     * geçer.
     *
     * ⚠️ Aynı tuzak {@link CompanyDtsSettingsDialogPage#clickContinue()} ile aynı: bu dialogun KENDİ
     * "Onayla" butonu "onayla" pos-keyword'üyle eşleşiyor, üstteki ConfirmDialog'daki "Evet"i genel
     * {@code acceptVaadinConfirmDialogIfPresent()} ile aramak yanlışlıkla "Onayla"yı tekrar tıklayabilir.
     * Fix: "Evet" araması SADECE "ozet bilgileri" metnini İÇERMEYEN (= ana dialog olmayan) bir overlay
     * içinde yapılır.
     */
    public boolean clickApprove() {
        log.info("[DTS] clickApprove oncesi dialog metni: {}", dumpVisibleText());
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-dts-approve-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dts-approve-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var full = fold(o.textContent);" +
                    "  if (full.indexOf('dogrudan tahsilat bilgileri') < 0 || full.indexOf('ozet bilgileri') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    var t = fold(b.textContent);" +
                    "    if (t === 'onayla') { b.setAttribute('data-dts-approve-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS] CompanyDtsAllocationDialog 'Onayla' butonu isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                log.warn("[DTS] 'Onayla' butonu bulunamadi — statu CALCULATING/PENDING disinda olabilir. Guncel metin: {}",
                        dumpVisibleText());
                return false;
            }
            WebElement btn = driver.findElement(By.cssSelector("[data-dts-approve-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            btn.click();
            log.info("[DTS] 'Onayla' tiklandi. Onay ('Evet') bekleniyor...");
            Thread.sleep(700);

            boolean confirmed = clickEvetInOverlayExcluding("ozet bilgileri", 8);
            log.info("[DTS] 'Onay' ConfirmDialog Evet ile onaylandi mi (scoped): {}", confirmed);
            if (!confirmed) {
                log.warn("[DTS] 'Onayla' sonrasi 'ozet bilgileri' icermeyen bir Onay dialogu bulunamadi. Guncel metin: {}",
                        dumpVisibleText());
                return false;
            }
            Thread.sleep(1500);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS] clickApprove: {}", e.getMessage());
            return false;
        }
    }

    /**
     * "excludeFoldSubstring" metnini İÇERMEYEN (yani ana dialog OLMAYAN) görünür bir
     * {@code vaadin-dialog-overlay} içinde fold-eşleşmesi tam "evet" olan butona GERÇEK
     * Selenium click() ile basar.
     */
    private boolean clickEvetInOverlayExcluding(String excludeFoldSubstring, int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        String escapedExclude = excludeFoldSubstring.replace("'", "\\'");
        while (System.currentTimeMillis() < deadline) {
            try {
                Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "function fold(s){return (s||'')" +
                        ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                        ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                        ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                        "document.querySelectorAll('[data-dts-evet-target]').forEach(function(e){" +
                        "  e.removeAttribute('data-dts-evet-target');});" +
                        "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                        "for (var o of overlays) {" +
                        "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                        "  var full = fold(o.textContent);" +
                        "  if (full.indexOf('" + escapedExclude + "') >= 0) continue;" +
                        "  var btns = o.querySelectorAll('vaadin-button, button');" +
                        "  for (var b of btns) {" +
                        "    if (b.disabled) continue;" +
                        "    var t = fold(b.textContent);" +
                        "    if (t === 'evet') { b.setAttribute('data-dts-evet-target', '1'); return true; }" +
                        "  }" +
                        "}" +
                        "return false;");
                if (Boolean.TRUE.equals(marked)) {
                    WebElement btn = driver.findElement(By.cssSelector("[data-dts-evet-target='1']"));
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'});", btn);
                    Thread.sleep(200);
                    btn.click();
                    log.info("[DTS] Scoped 'Evet' butonuna gerçek Selenium click uygulandı.");
                    return true;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                log.debug("[DTS] clickEvetInOverlayExcluding: {}", e.getMessage());
            }
        }
        return false;
    }
}
