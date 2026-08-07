package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tedarikçi (Company) — "Bayi Limitleri" satırı "Düzenle" diyaloğu
 * (kaynak: {@code CompanyEditDealerLimitDialog}, {@code origin/vaadin-24} READ-ONLY incelendi).
 *
 * ⚠️ Kaynak kod kanıtı: bu dialogun SADECE EDIT modu vardır (CREATE YOK — bkz.
 * {@link CompanyDisplayDealerLimitsPage} Javadoc'u). Düzenlenebilir gerçek alanlar: "DBS Limit
 * Tutarı" (dealerLimitAmountField, {@code AmountField}), "Ek İskonto Limiti"
 * (additionalDiscountLimitAmountField), "Açıklama", "FK Durumu", "Sözleşme Tarihi", "Etkin"
 * checkbox'ı, "Para Birimi". "Vadeli Yüklenen Tutar"/"Kredilenen Tutar"/"Ödenmemiş Tutar"/
 * "İskonto Limiti"/"Aktif İskonto Tutarı" ile "Bayi Ünvanı"/"Finansal Kurum" (dealerNameField/
 * factoringTextField) HEP DISABLED'dır (salt-görüntüleme). Bu senaryo sadece "DBS Limit Tutarı"nı
 * değiştirir — kaynak kod: {@code if (newRemainingLimit = inputLimit - creditedAmount < 0) → save
 * edilmez, sadece uyarı}. Save butonu HER ZAMAN "Güncelle" (CompanyAddEditDealerDialog.2).
 */
public class CompanyEditDealerLimitDialogPage extends BasePageObject {

    public CompanyEditDealerLimitDialogPage(WebDriver driver) {
        super(driver);
    }

    /** "Bayi Ünvanı" VE "DBS Limit Tutarı" etiketleri aynı anda görünüyorsa bu dialog açık demektir
     *  (CompanyAddEditDealerDialog'da "DBS Limit Tutarı" YOKTUR — ayırt edici kombinasyon). */
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
                    "  if (t.indexOf('bayi unvani') >= 0 && t.indexOf('dbs limit tutari') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] CompanyEditDealerLimitDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTS-MD] isOpened (DealerLimit): {}", e.getMessage());
            return false;
        }
    }

    public String dumpVisibleText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  return (o.textContent || '').replace(/\\s+/g,' ').trim().substring(0, 900);" +
                    "}" +
                    "return '';");
            String s = String.valueOf(text);
            log.info("[DTS-MD] Dialog metni (Bayi Limiti): {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTS-MD] dumpVisibleText (DealerLimit): {}", e.getMessage());
            return "";
        }
    }

    /**
     * "DBS Limit Tutarı" alanına ({@code AmountField}) yeni değer yazar.
     *
     * {@link CompanyAddEditDealerDialogPage#setGuaranteeLimit} ile AYNI, kanıtlanmış (3/3 PASSED)
     * kalıp: {@code clear()} YERİNE tek focus + Ctrl+A (tümünü seç) + üzerine yaz — {@code clear()}'ın
     * HER ÇAĞRIDA odaklanmayı yeniden tetikleyip odaklanma-yarışını sonsuza kadar beslediği
     * (2026-08-07 canlı koşumla kanıtlanmış) kök nedeni tekrarlamaz.
     */
    public boolean setDbsLimitAmount(String value) {
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-dealerlimit-amount-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dealerlimit-amount-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var fields = o.querySelectorAll('vaadin-text-field, vaadin-number-field');" +
                    "  for (var f of fields) {" +
                    "    var lbl = fold(f.getAttribute('label') || (f.querySelector('label')||{}).textContent || '');" +
                    "    if (lbl.indexOf('dbs limit tutari') >= 0) { f.setAttribute('data-dealerlimit-amount-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] 'DBS Limit Tutarı' alanı bulundu mu: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                log.warn("[DTS-MD] 'DBS Limit Tutarı' alanı bulunamadı. Güncel metin: {}", dumpVisibleText());
                return false;
            }
            WebElement field = driver.findElement(By.cssSelector("[data-dealerlimit-amount-target='1']"));
            WebElement input = field.findElement(By.cssSelector("input"));
            input.click();
            waitForStableValue(input, 6, 300);
            boolean typedOk = false;
            for (int attempt = 1; attempt <= 3 && !typedOk; attempt++) {
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(value);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", input);
                String immediate = input.getAttribute("value");
                try { Thread.sleep(900); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                String settled = input.getAttribute("value");
                typedOk = matchesNumeric(immediate, value) && matchesNumeric(settled, value);
                log.info("[DTS-MD] 'DBS Limit Tutarı' yazma denemesi {}/3 (Ctrl+A): hedef='{}' anlık='{}' 900ms-sonra='{}' başarılı={}",
                        attempt, value, immediate, settled, typedOk);
            }
            // ⚠️ KÖK NEDEN (2026-08-07, canlı koşumla kanıtlandı): Keys.TAB + sentetik blur/change
            // dispatch, DİĞER AmountField'larda (DBS Maliyet, Teminat Limiti) sorunsuz çalışırken bu
            // SPESİFİK alanda (dealerLimitAmountField — addValueChangeListener'ı recalculateDiscountLimitAmount()'ı
            // tetikleyen TEK AmountField, ek bir sunucu round-trip'i var) hem INNER hem OUTER .value'yu
            // TAMAMEN BOŞALTIYORDU (AmountField.formatTextValue()'nun catch dalı — setPlainValue(null) —
            // tetikleniyor gibi görünüyor). Fix: TAB yerine dialogun NÖTR bir alanına (overlay'in
            // kendisi, herhangi bir input değil) GERÇEK bir Selenium click ile blur tetikle — sentetik
            // event dispatch'i tamamen kaldırıldı, tarayıcının native focus-out'una güveniliyor. 3/3
            // ardışık canlı koşumda DB'den kanıtlandı.
            WebElement dialogOverlay = driver.findElement(By.cssSelector("vaadin-dialog-overlay"));
            new Actions(driver).moveToElement(dialogOverlay, 5, 5).click().perform();
            try { Thread.sleep(600); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            @SuppressWarnings("unchecked")
            List<Object> outerReadback = (List<Object>) ((JavascriptExecutor) driver).executeScript(
                    "var f = document.querySelector('[data-dealerlimit-amount-target=\"1\"]');" +
                    "var i = f ? f.querySelector('input') : null;" +
                    "return [f ? String(f.value) : 'NULL_OUTER', i ? String(i.value) : 'NULL_INNER'];");
            typedOk = matchesNumeric(String.valueOf(outerReadback.get(1)), value);
            log.info("[DTS-MD] 'DBS Limit Tutarı' alanına '{}' yazıldı, nötr alana tıklanarak blur tetiklendi "
                    + "(outer='{}' inner='{}' typedOk={}).", value, outerReadback.get(0), outerReadback.get(1), typedOk);
            return typedOk;
        } catch (Exception e) {
            log.warn("[DTS-MD] setDbsLimitAmount: {}", e.getMessage());
            return false;
        }
    }

    private void waitForStableValue(WebElement input, int maxPolls, int intervalMs) {
        String previous = null;
        for (int i = 0; i < maxPolls; i++) {
            String current;
            try {
                current = input.getAttribute("value");
            } catch (Exception e) {
                return;
            }
            if (previous != null && previous.equals(current)) {
                return;
            }
            previous = current;
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * İki sayısal metni, ayraç STİLİNDEN (TR virgül-ondalık "205555,00" ↔ EN binlik-virgül+nokta-ondalık
     * "205,555.00") bağımsız olarak, GERÇEK SAYISAL DEĞER bazında karşılaştırır.
     *
     * ⚠️ KÖK NEDEN (2026-08-07, canlı koşumla kanıtlandı): "." ve ","yı körü körüne SİLEN önceki
     * yaklaşım ("12,34"→"1234" gibi <1000 değerlerde) küçük tutarlarda tesadüfen doğru sonuç
     * veriyordu, ama "200,000.00" (binlik ayraçlı, 1000+) gibi bir DEĞERDE "20000000" (8 hane) üretip
     * "200000" (6 hane, ayraçsız hedef) ile YANLIŞ eşleşmiyor çıkarıyordu — kayıt aslında doğru
     * kaydedilmişken sahte bir "başarısız" sonucu doğurdu. Fix: hangi karakterin ONDALIK ayracı
     * olduğunu (metinde EN SONA gelen "," veya "." ) tespit edip GERÇEK BigDecimal'a çevirip
     * {@code compareTo} ile karşılaştır.
     */
    private boolean matchesNumeric(String actual, String expected) {
        BigDecimal a = parseFlexibleAmount(actual);
        BigDecimal e = parseFlexibleAmount(expected);
        return a != null && e != null && a.compareTo(e) == 0;
    }

    private BigDecimal parseFlexibleAmount(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        int lastComma = s.lastIndexOf(',');
        int lastDot = s.lastIndexOf('.');
        String normalized;
        if (lastComma > lastDot) {
            // Virgül ondalık ayracı, nokta(lar) binlik ayracı ("205.555,00" veya "205555,00")
            normalized = s.replace(".", "").replace(",", ".");
        } else if (lastDot > lastComma) {
            // Nokta ondalık ayracı, virgül(ler) binlik ayracı ("205,555.00" veya "205555.00")
            normalized = s.replace(",", "");
        } else {
            // Hiç ayraç yok
            normalized = s;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e2) {
            return null;
        }
    }

    /** "Güncelle" butonuna basıp açılan "Onay" ConfirmDialog'unu onaylar (bu dialogda SADECE EDIT modu vardır). */
    public boolean clickUpdateAndConfirm() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var full = fold(o.textContent);" +
                    "  if (full.indexOf('dbs limit tutari') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    if (fold(b.textContent) === 'guncelle') { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] 'guncelle' butonu tıklandı mı (Bayi Limiti): {}", clicked);
            if (!Boolean.TRUE.equals(clicked)) {
                log.warn("[DTS-MD] 'guncelle' butonu bulunamadı (Bayi Limiti). Güncel metin: {}", dumpVisibleText());
                return false;
            }
            Thread.sleep(800);
            acceptVaadinConfirmDialogIfPresent();
            Thread.sleep(1200);
            // Kalıcı bekçi: validateAll() "zorunlu alanları doldurunuz" uyarısı sessizce save'i iptal
            // edebiliyor (2026-08-07'de dealerLimitAmountField'ın boşalması yüzünden yaşandı, artık
            // düzeltildi) — bir daha olursa BURADA görünür/loglanır hale getiriliyor, sessizce geçilmiyor.
            Object toastText = ((JavascriptExecutor) driver).executeScript(
                    "var cards = Array.from(document.querySelectorAll('vaadin-notification-card'));" +
                    "return cards.map(function(c){ return (c.textContent||'').trim(); }).filter(Boolean).join(' || ');");
            if (toastText != null && !String.valueOf(toastText).isEmpty()) {
                log.warn("[DTS-MD] 'Güncelle' onayı sonrası bildirim göründü (beklenmedik olabilir): '{}'", toastText);
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS-MD] clickUpdateAndConfirm (DealerLimit): {}", e.getMessage());
            return false;
        }
    }
}
