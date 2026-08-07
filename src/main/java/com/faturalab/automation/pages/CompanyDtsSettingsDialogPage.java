package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Tedarikçi (Company) — DTS "Devam Et" (Taslak satırı) sonrası açılan ayar diyaloğu
 * (kaynak: {@code CompanyDtsSettingsDialog}, başlık "Doğrudan Tahsilat Ayarı"/
 * "Yeni Doğrudan Tahsilat Ayarı" — CompanyDtsSettingsDialog.0/.1, messages_tr.properties).
 *
 * FAZ 2 kapsamı: sadece diyaloğun AÇILDIĞINI doğrulama (başlık + algoritma/bordo alanları).
 * "Devam Et" (dialog içi, DtsSettings kaydı) ve sonraki CompanyDetailDtsSettingsDialog
 * ("Başlat") SONRAKİ bir adımda ele alınabilir.
 */
public class CompanyDtsSettingsDialogPage extends BasePageObject {

    public CompanyDtsSettingsDialogPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Diyaloğun açık olup olmadığını görünür metinle doğrular: tam olarak
     * "doğrudan tahsilat ayarı" (ASCII-fold) başlığı VE "algoritma" kelimesi
     * (dropdown label'ları) aynı anda görünür olmalı — sayfa üstündeki
     * "Doğrudan Tahsilat Ayarları" (çoğul, ayarlar sayfası) ile karışmasın.
     */
    public boolean isOpened() {
        try {
            Boolean opened = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll(" +
                    "  'vaadin-dialog-overlay, [role=\"dialog\"]'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = fold(o.textContent);" +
                    "  if (t.indexOf('dogrudan tahsilat ayari') >= 0 && t.indexOf('algoritma') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTS] CompanyDtsSettingsDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTS] isOpened: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Dialog içindeki (kaynak: {@code CompanyDtsSettingsDialog.buildFormLayout()}) "Devam Et"
     * ana butonuna basar (varsayılan FK/algoritma seçimleri korunur — kaynak kod kanıtı:
     * {@code continueButton} click handler SADECE {@code defaultAlgorithmComboBox} ve
     * {@code overLimitAlgorithmComboBox}'ı validate eder — ikisi de zaten dolu değerle
     * gelir; FK checkbox seçimi validate edilmiyor, opsiyonel). Ardından açılan "Onay"
     * ConfirmDialog'unu ({@code CompanyDtsSettingsDialog.5}="Devam etmek istediğinizden emin
     * misiniz?", Evet/Hayır) kapsamlı (scoped) şekilde onaylar.
     *
     * ⚠️ 2026-08-06 canlı bulgu: genel {@link BasePageObject#acceptVaadinConfirmDialogIfPresent()}
     * BURADA KULLANILAMAZ — o metodun pozitif-kelime listesi ("devam" dahil) bu dialogun KENDİ
     * "Devam Et" butonuyla da eşleşiyor. Vaadin, üstteki Onay ConfirmDialog'u AÇARKEN alttaki
     * Ayar dialogunu DOM'dan kaldırmıyor (ikisi de aynı anda {@code vaadin-dialog-overlay} olarak
     * ölçülebilir rect'e sahip) — genel metod DOM sırasına göre önce alttaki dialogu tarayıp
     * "Devam Et"i TEKRAR tıklıyor, gerçek "Evet"e hiç basmıyor (5 tur boyunca "Devam Et"i
     * defalarca yeniden tetikleyip yığılmış Onay dialogları bırakıyor, hiçbiri kapanmıyor).
     * Fix: "Evet" tıklaması SADECE "algoritma" metnini İÇERMEYEN (= Ayar dialogu olmayan) bir
     * overlay içinde aranır.
     */
    public boolean clickContinue() {
        log.info("[DTS] clickContinue oncesi dialog metni: {}", dumpVisibleText());
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-dts-continue-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dts-continue-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var full = fold(o.textContent);" +
                    "  if (full.indexOf('dogrudan tahsilat ayari') < 0 || full.indexOf('algoritma') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    var t = fold(b.textContent);" +
                    "    if (t === 'devam et') { b.setAttribute('data-dts-continue-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS] CompanyDtsSettingsDialog 'Devam Et' butonu isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement btn = driver.findElement(By.cssSelector("[data-dts-continue-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            btn.click();
            log.info("[DTS] 'Devam Et' tiklandi (Ayar dialogu). Onay ('Evet') bekleniyor...");
            Thread.sleep(700);

            String toastAfterClick = readVisibleNotificationText();
            if (toastAfterClick != null) {
                log.warn("[DTS] 'Devam Et' sonrasi görünür toast: {}", toastAfterClick);
            }

            boolean confirmed = clickEvetInOverlayExcluding("algoritma", 8);
            log.info("[DTS] 'Onay' ConfirmDialog Evet ile onaylandi mi (scoped): {}", confirmed);
            if (!confirmed) {
                log.warn("[DTS] 'Devam Et' sonrasi 'algoritma' icermeyen bir Onay dialogu bulunamadi — "
                        + "validasyon sessizce basarisiz olmus olabilir. Guncel dialog metni: {}", dumpVisibleText());
                return false;
            }
            Thread.sleep(1500);

            String toastAfterConfirm = readVisibleNotificationText();
            if (toastAfterConfirm != null) {
                log.warn("[DTS] Onay sonrasi görünür toast: {}", toastAfterConfirm);
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS] clickContinue: {}", e.getMessage());
            return false;
        }
    }

    /**
     * "algoritma" metnini İÇERMEYEN (yani Ayar dialogu OLMAYAN) görünür bir
     * {@code vaadin-dialog-overlay} içinde fold-eşleşmesi tam "evet" olan butona GERÇEK
     * Selenium click() ile basar. Kısa poll (dialog render gecikmesine karşı).
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

    /** O an görünür vaadin-notification-card / toast metnini döner, yoksa null. */
    private String readVisibleNotificationText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "var cards = document.querySelectorAll(" +
                    "  'vaadin-notification-card, vaadin-notification-container > *, .v-Notification');" +
                    "for (var c of cards) {" +
                    "  var r = c.getBoundingClientRect();" +
                    "  if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = (c.textContent || '').trim();" +
                    "  if (t.length > 0) return t;" +
                    "}" +
                    "return null;");
            return text != null ? text.toString() : null;
        } catch (Exception e) {
            log.debug("[DTS] readVisibleNotificationText: {}", e.getMessage());
            return null;
        }
    }

    /** Diyalog içeriğinin ilk 500 karakterini debug için loglar (locator kanıtı). */
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
            log.info("[DTS] Dialog metni: {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTS] dumpVisibleText: {}", e.getMessage());
            return "";
        }
    }
}
