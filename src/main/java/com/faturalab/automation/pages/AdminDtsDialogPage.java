package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Admin — {@code AdminDtsDialog} ("Doğrudan Tahsilat Bilgileri" — aynı başlık {@code
 * CompanyDtsAllocationDialog.0}'ı paylaşır). {@code DisplayDtsView} grid satırındaki "Gözat"
 * ({@code CompanyTendersView.7}) ile açılır.
 *
 * Kaynak kod kanıtı ({@code AdminDtsDialog.buildAcceptedButton()}): "Onayla" ({@code
 * CompanyDtsAllocationDialog.25}) butonu SADECE statü **SEND** (Gönderiliyor) iken VE admin
 * {@code PrivilegeAdminType.DTS_ACCEPTED} yetkisine sahipken eklenir. Tıklanınca "Onay" başlıklı
 * ConfirmDialog (metin hardcoded "DTS işlemini göndermek istediğinize emin misiniz?") açılır;
 * "Evet" ile statü **COMPLETED** (Tamamlandı) — nihai onay, GERİ ALINAMAZ.
 */
public class AdminDtsDialogPage extends BasePageObject {

    public AdminDtsDialogPage(WebDriver driver) {
        super(driver);
    }

    /** Diyaloğun açık olup olmadığını görünür metinle doğrular ("doğrudan tahsilat bilgileri"). */
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
                    "  if (t.indexOf('dogrudan tahsilat bilgileri') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTS] AdminDtsDialog acik mi: {}", opened);
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
            log.info("[DTS] AdminDtsDialog metni: {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTS] dumpVisibleText: {}", e.getMessage());
            return "";
        }
    }

    /**
     * "Onayla" butonuna basar ve "Onay" ConfirmDialog'unu ("DTS işlemini göndermek
     * istediğinize emin misiniz?", Evet/Hayır) onaylar. GERİ ALINAMAZ — statü COMPLETED'a
     * (Tamamlandı) geçer.
     *
     * ⚠️ Aynı çift-"Onayla" tuzağı: "Evet" araması SADECE "finansal kurum bilgileri" metnini
     * İÇERMEYEN (= ana AdminDtsDialog olmayan) bir overlay içinde yapılır.
     */
    public boolean clickApprove() {
        log.info("[DTS] clickApprove oncesi dialog metni: {}", dumpVisibleText());
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-admindts-approve-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-admindts-approve-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var full = fold(o.textContent);" +
                    "  if (full.indexOf('dogrudan tahsilat bilgileri') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    var t = fold(b.textContent);" +
                    "    if (t === 'onayla') { b.setAttribute('data-admindts-approve-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS] AdminDtsDialog 'Onayla' butonu isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                log.warn("[DTS] 'Onayla' butonu bulunamadi — statu SEND disinda veya yetki eksik olabilir. Guncel metin: {}",
                        dumpVisibleText());
                return false;
            }
            WebElement btn = driver.findElement(By.cssSelector("[data-admindts-approve-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            btn.click();
            log.info("[DTS] AdminDtsDialog 'Onayla' tiklandi. Onay ('Evet') bekleniyor...");
            Thread.sleep(700);

            boolean confirmed = clickEvetInOverlayExcluding("finansal kurum bilgileri", 8);
            log.info("[DTS] AdminDtsDialog 'Onay' ConfirmDialog Evet ile onaylandi mi (scoped): {}", confirmed);
            if (!confirmed) {
                log.warn("[DTS] Admin 'Onayla' sonrasi 'finansal kurum bilgileri' icermeyen bir Onay dialogu bulunamadi. Guncel metin: {}",
                        dumpVisibleText());
                return false;
            }
            Thread.sleep(1500);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS] AdminDtsDialog clickApprove: {}", e.getMessage());
            return false;
        }
    }

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
                        "document.querySelectorAll('[data-admindts-evet-target]').forEach(function(e){" +
                        "  e.removeAttribute('data-admindts-evet-target');});" +
                        "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                        "for (var o of overlays) {" +
                        "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                        "  var full = fold(o.textContent);" +
                        "  if (full.indexOf('" + escapedExclude + "') >= 0) continue;" +
                        "  var btns = o.querySelectorAll('vaadin-button, button');" +
                        "  for (var b of btns) {" +
                        "    if (b.disabled) continue;" +
                        "    var t = fold(b.textContent);" +
                        "    if (t === 'evet') { b.setAttribute('data-admindts-evet-target', '1'); return true; }" +
                        "  }" +
                        "}" +
                        "return false;");
                if (Boolean.TRUE.equals(marked)) {
                    WebElement btn = driver.findElement(By.cssSelector("[data-admindts-evet-target='1']"));
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'});", btn);
                    Thread.sleep(200);
                    btn.click();
                    log.info("[DTS] AdminDtsDialog scoped 'Evet' butonuna gerçek Selenium click uygulandı.");
                    return true;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception e) {
                log.debug("[DTS] AdminDtsDialog clickEvetInOverlayExcluding: {}", e.getMessage());
            }
        }
        return false;
    }
}
