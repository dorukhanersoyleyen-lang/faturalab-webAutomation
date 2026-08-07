package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Tedarikçi (Company) — "Bayi Ekle/Düzenle" diyaloğu
 * (kaynak: {@code CompanyAddEditDealerDialog}, {@code origin/vaadin-24} READ-ONLY incelendi).
 *
 * ⚠️ Kaynak kod kanıtı ({@code buildFormLayout()}): EDIT modunda (dealer!=null) "Bayi Ünvanı"
 * ({@code dealerNameField.setEnabled(false)}) ve "Vergi Kimlik No" ({@code taxtNumberTextField.setEnabled(false)})
 * alanları DISABLED'dır — "telefon" diye bir alan YOKTUR. Düzenlenebilir gerçek alanlar: "Bayi Kodu",
 * "Teminat Limiti" ({@link com.faturalab.automation.pages.CompanyAddEditFactoringDialogPage}'teki
 * "DBS Maliyet (%)" ile AYNI {@code AmountField} bileşeni — aynı odaklanma-yarışı/blur tuzağı geçerli,
 * bkz. {@link #setGuaranteeLimit}), "Para Birimi" (currencyComboBox), "Durumu" (Aktif/Pasif radio).
 * Save butonu CREATE'te "Kaydet" (AddEditRoleDialog.5), EDIT'te "Güncelle" (CompanyAddEditDealerDialog.2).
 */
public class CompanyAddEditDealerDialogPage extends BasePageObject {

    public CompanyAddEditDealerDialogPage(WebDriver driver) {
        super(driver);
    }

    /** Add veya Edit modunda diyalog açık mı — "Bayi Kodu" VE "Teminat Limiti" etiketleri her iki modda da ortak. */
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
                    "  if (t.indexOf('bayi kodu') >= 0 && t.indexOf('teminat limiti') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] CompanyAddEditDealerDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTS-MD] isOpened (Dealer): {}", e.getMessage());
            return false;
        }
    }

    /** EDIT modunda "Bayi Ünvanı" alanı DISABLED olur (kaynak kod kanıtı) — CREATE/EDIT ayrımı. */
    public boolean isEditModeOpened() {
        try {
            Boolean disabled = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var fields = o.querySelectorAll('vaadin-text-field');" +
                    "  for (var f of fields) {" +
                    "    var lbl = fold(f.getAttribute('label') || (f.querySelector('label')||{}).textContent || '');" +
                    "    if (lbl.indexOf('bayi unvani') >= 0) return f.hasAttribute('disabled') || f.disabled === true;" +
                    "  }" +
                    "}" +
                    "return false;");
            return Boolean.TRUE.equals(disabled);
        } catch (Exception e) {
            log.warn("[DTS-MD] isEditModeOpened (Dealer): {}", e.getMessage());
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
            log.info("[DTS-MD] Dialog metni (Bayi): {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTS-MD] dumpVisibleText (Dealer): {}", e.getMessage());
            return "";
        }
    }

    private boolean fillTextFieldByLabel(String labelFoldKeyword, String value) {
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-dealer-field-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dealer-field-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var fields = o.querySelectorAll('vaadin-text-field');" +
                    "  for (var f of fields) {" +
                    "    var lbl = fold(f.getAttribute('label') || (f.querySelector('label')||{}).textContent || '');" +
                    "    if (lbl.indexOf('" + labelFoldKeyword + "') >= 0) { f.setAttribute('data-dealer-field-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            if (!Boolean.TRUE.equals(marked)) {
                log.warn("[DTS-MD] '{}' alanı bulunamadı. Güncel metin: {}", labelFoldKeyword, dumpVisibleText());
                return false;
            }
            WebElement field = driver.findElement(By.cssSelector("[data-dealer-field-target='1']"));
            WebElement input = field.findElement(By.cssSelector("input"));
            input.click();
            Thread.sleep(200);
            input.clear();
            input.sendKeys(value);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                    "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", input);
            input.sendKeys(Keys.TAB);
            Thread.sleep(300);
            log.info("[DTS-MD] '{}' alanına '{}' yazıldı.", labelFoldKeyword, value);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS-MD] fillTextFieldByLabel '{}': {}", labelFoldKeyword, e.getMessage());
            return false;
        }
    }

    /** "Bayi Ünvanı" alanı — SADECE CREATE modunda enabled. */
    public boolean setDealerName(String value) {
        return fillTextFieldByLabel("bayi unvani", value);
    }

    /** "Bayi Kodu" alanı — CREATE ve EDIT'te ortak, ikisinde de enabled. */
    public boolean setDealerCode(String value) {
        return fillTextFieldByLabel("bayi kodu", value);
    }

    /** "Vergi Kimlik No" alanı — SADECE CREATE modunda enabled. */
    public boolean setTaxNumber(String value) {
        return fillTextFieldByLabel("vergi kimlik no", value);
    }

    /**
     * "Teminat Limiti" alanı — {@code AmountField} ({@link CompanyAddEditFactoringDialogPage#setDbsCost}
     * ile AYNI bileşen/AYNI tuzaklar): odaklanma-yarışı (clear()'ın kendisi focus tetikler, sunucu
     * gecikmeli olarak eski değeri geri pushlar) ve blur olmadan sunucu senkronizasyonu olmaması.
     * Aynı ispatlanmış sıralama uygulanıyor: click → bekle → clear() → bekle → TEK SEFERDE yaz →
     * TAB ile gerçek blur.
     */
    public boolean setGuaranteeLimit(String value) {
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-dealer-limit-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dealer-limit-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var fields = o.querySelectorAll('vaadin-text-field, vaadin-number-field');" +
                    "  for (var f of fields) {" +
                    "    var lbl = fold(f.getAttribute('label') || (f.querySelector('label')||{}).textContent || '');" +
                    "    if (lbl.indexOf('teminat limiti') >= 0) { f.setAttribute('data-dealer-limit-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] 'Teminat Limiti' alanı bulundu mu: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                log.warn("[DTS-MD] 'Teminat Limiti' alanı bulunamadı. Güncel metin: {}", dumpVisibleText());
                return false;
            }
            WebElement field = driver.findElement(By.cssSelector("[data-dealer-limit-target='1']"));
            WebElement input = field.findElement(By.cssSelector("input"));
            input.click();
            // ⚠️ KÖK NEDEN — EDİT'e ÖZGÜ AĞIRLAŞMA (2026-08-07, canlı koşumla kanıtlandı — bir önceki
            // "clear()+retry" fix'i YETERSİZ/KÖTÜLEŞTİRİCİ çıktı): AmountField'ın addFocusListener'ı
            // odaklanınca sunucu tarafında ASENKRON olarak eski değeri (ör. "500") client'a geri
            // pushluyor. Kritik ek bulgu: Selenium `input.clear()`'ın KENDİSİ HER ÇAĞRIDA odaklanmayı
            // YENİDEN tetikliyor — yani "başarısız olursa tekrar clear()+yaz" retry deseni her denemede
            // YENİ bir push'u TEKRAR tetikleyip yarışı sonsuza kadar besliyor (4 denemenin 4'ü de "500"
            // ile başarısız oldu — retry arttıkça durum İYİLEŞMEDİ). Fix: `clear()` YERİNE tek bir
            // gerçek focus'tan sonra klavye ile TÜMÜNÜ SEÇ (Ctrl+A) + üzerine yaz — seçim işlemi
            // ODAKLANMAYI YENİDEN TETİKLEMEZ (element zaten focused), bu yüzden race sadece İLK click()
            // sırasında BİR KEZ oluşur ve stabilite poll'uyla güvenle atlatılır.
            waitForStableValue(input, 6, 300);
            boolean typedOk = false;
            for (int attempt = 1; attempt <= 3 && !typedOk; attempt++) {
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(value);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", input);
                // Yazdıktan hemen sonraki değer + kısa bir süre sonraki değer İKİSİ de hedefle eşleşmeli —
                // sadece anlık okuma geç gelen bir ezilmeyi kaçırabilir.
                String immediate = input.getAttribute("value");
                try { Thread.sleep(900); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                String settled = input.getAttribute("value");
                boolean immediateOk = matchesNumeric(immediate, value);
                boolean settledOk = matchesNumeric(settled, value);
                typedOk = immediateOk && settledOk;
                log.info("[DTS-MD] 'Teminat Limiti' yazma denemesi {}/3 (Ctrl+A, clear() DEĞİL): hedef='{}' anlık='{}' 900ms-sonra='{}' başarılı={}",
                        attempt, value, immediate, settled, typedOk);
            }
            input.sendKeys(Keys.TAB);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('blur',{bubbles:true}));" +
                    "arguments[0].dispatchEvent(new CustomEvent('change',{bubbles:true}));", input);
            try { Thread.sleep(400); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            log.info("[DTS-MD] 'Teminat Limiti' alanına '{}' yazıldı ve blur tetiklendi (typedOk={}).", value, typedOk);
            return typedOk;
        } catch (Exception e) {
            log.warn("[DTS-MD] setGuaranteeLimit: {}", e.getMessage());
            return false;
        }
    }

    /** Bir input'un değeri PEŞPEŞE İKİ okumada değişmeden aynı kalana kadar poll eder (odaklanma-yarışı
     *  push'unun oturmasını beklemek için) — {@link #setGuaranteeLimit} tarafından kullanılır. */
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

    /** İki sayısal metni ayraç (","/".") farkını yok sayarak karşılaştırır. */
    private boolean matchesNumeric(String actual, String expected) {
        if (actual == null) return false;
        return actual.replace(".", "").replace(",", "").equals(expected.replace(".", "").replace(",", ""));
    }

    /**
     * "Para Birimi" combobox'ını açıp verilen değeri (ör. "TRY") seçer. CurrencyType kısa bir enum
     * listesi olduğu için virtualization riski düşük, ama {@code selectFactoring} ile AYNI güvenli
     * filtre-yaz-sonra-seç kalıbı kullanılıyor (tutarlılık + olası gelecekteki liste büyümesine karşı).
     */
    public boolean selectCurrency(String currencyKeyword) {
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-dealer-currency-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dealer-currency-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var combos = o.querySelectorAll('vaadin-combo-box');" +
                    "  for (var c of combos) {" +
                    "    var lbl = fold(c.getAttribute('label') || (c.querySelector('label')||{}).textContent || '');" +
                    "    if (lbl === 'para birimi') {" +
                    "      var inp = c.querySelector('input');" +
                    "      if (!inp) return false;" +
                    "      inp.setAttribute('data-dealer-currency-target', '1');" +
                    "      return true;" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] 'Para Birimi' combobox input'u işaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement input = driver.findElement(By.cssSelector("[data-dealer-currency-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", input);
            input.click();
            Thread.sleep(300);
            input.sendKeys(currencyKeyword);
            Thread.sleep(900);
            Boolean picked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var kw = fold(arguments[0]);" +
                    "var items = document.querySelectorAll('vaadin-combo-box-item, vaadin-item, [role=\"option\"]');" +
                    "for (var el of items) {" +
                    "  var r = el.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (fold(el.textContent).includes(kw)) { el.click(); return true; }" +
                    "}" +
                    "return false;", currencyKeyword);
            log.info("[DTS-MD] Para birimi '{}' (filtrelenmiş) seçildi mi: {}", currencyKeyword, picked);
            return Boolean.TRUE.equals(picked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS-MD] selectCurrency: {}", e.getMessage());
            return false;
        }
    }

    /** "Durumu" radio grubunda "Aktif" seçeneğini işaretler (mevcut kodda kanıtlı XPath kalıbı — bkz. CompanyInvoicePage). */
    public boolean selectStatusActive() {
        try {
            WebElement radio = driver.findElement(By.xpath(
                    "//vaadin-dialog-overlay//vaadin-radio-button[contains(normalize-space(),'Aktif')]"));
            radio.click();
            log.info("[DTS-MD] 'Durumu' → 'Aktif' seçildi.");
            return true;
        } catch (Exception e) {
            log.warn("[DTS-MD] selectStatusActive: {}", e.getMessage());
            return false;
        }
    }

    /**
     * "Kaydet" (add) veya "Güncelle" (edit) butonuna basıp açılan "Onay" ConfirmDialog'unu onaylar
     * ({@link CompanyAddEditFactoringDialogPage#clickSaveAndConfirm} ile aynı kalıp — overlay anchor
     * "bayi kodu"dur, çünkü CREATE'te caption "Bayi Ekle", EDIT'te sadece "Düzenle" — ikinci çok
     * genel olduğu için ayırt edici olmayan caption yerine ortak alan etiketi kullanılıyor).
     */
    public boolean clickSaveAndConfirm(boolean editMode) {
        String label = editMode ? "guncelle" : "kaydet";
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var target = arguments[0];" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var full = fold(o.textContent);" +
                    "  if (full.indexOf('bayi kodu') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    if (fold(b.textContent) === target) { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;", label);
            log.info("[DTS-MD] '{}' butonu tıklandı mı (Bayi): {}", label, clicked);
            if (!Boolean.TRUE.equals(clicked)) {
                log.warn("[DTS-MD] '{}' butonu bulunamadı (Bayi). Güncel metin: {}", label, dumpVisibleText());
                return false;
            }
            Thread.sleep(800);
            acceptVaadinConfirmDialogIfPresent();
            Thread.sleep(1200);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS-MD] clickSaveAndConfirm (Bayi): {}", e.getMessage());
            return false;
        }
    }
}
