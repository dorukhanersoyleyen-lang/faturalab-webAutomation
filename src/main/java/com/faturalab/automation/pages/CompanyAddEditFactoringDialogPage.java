package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Tedarikçi (Company) — "Anlaşmalı Finansal Kurum Ekle/Düzenle" diyaloğu
 * (kaynak: {@code CompanyAddEditFactoringDialog}, {@code origin/vaadin-24} READ-ONLY incelendi).
 *
 * ⚠️ Kaynak kod kanıtı ({@code buildFormLayout()}): CREATE modunda (companyFactoring==null) forma
 * SADECE "Para Birimi" (currencyComboBox) ve "Finansal Kurum" (factoringComboBox) eklenir —
 * "Çalışma Modeli" / "DBS Maliyet (%)" / "First Loss (%)" / "Fiyat (%)" / diğer alanlar SADECE
 * {@code editMode==true} iken formLayout'a eklenir (kaynak: {@code if (editMode) { formLayout.add(...) } }).
 * Dialogda HİÇBİR ZAMAN genel bir "Limit" (tutar) alanı YOKTUR — sadece "Limit Üstü Gönderim"
 * ({@code allowOverLimitCheckBox}, messages key {@code CompanyAddEditFactoringDialog.19}) checkbox'ı
 * vardır. Bu yüzden CREATE adımı sadece Finansal Kurum seçimiyle sınırlı; EDIT'te gerçekten var olan
 * "DBS Maliyet (%)" sayısal alanı değiştirilip doğrulanır.
 */
public class CompanyAddEditFactoringDialogPage extends BasePageObject {

    public CompanyAddEditFactoringDialogPage(WebDriver driver) {
        super(driver);
    }

    /** Add veya Edit modunda diyalog açık mı — accordion başlığı "Anlaşmalı Finansal Kurum" +
     *  "Finansal Kurum" combobox etiketi aynı anda görünür olmalı (ikisi de her iki modda ortak). */
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
                    "  if (t.indexOf('anlasmali finansal kurum') >= 0 && t.indexOf('finansal kurum') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] CompanyAddEditFactoringDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTS-MD] isOpened: {}", e.getMessage());
            return false;
        }
    }

    /** Sadece EDIT modunda forma eklenen "Çalışma Modeli" alanı görünür mü (add/edit ayrımı). */
    public boolean isEditModeOpened() {
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
                    "  if (t.indexOf('calisma modeli') >= 0) return true;" +
                    "}" +
                    "return false;");
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTS-MD] isEditModeOpened: {}", e.getMessage());
            return false;
        }
    }

    /** Diyalog içeriğinin ilk 900 karakterini debug için loglar (locator kanıtı). */
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
            log.info("[DTS-MD] Dialog metni: {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTS-MD] dumpVisibleText: {}", e.getMessage());
            return "";
        }
    }

    /**
     * "Finansal Kurum" combobox'ını açıp verilen ismi içeren öğeyi seçer (CREATE + EDIT ortak alan).
     *
     * ⚠️ 2026-08-06 canlı gözlem (5 deneme, tanı dökümüyle kanıtlandı): dialog'u sadece açıp overlay'i
     * taramak YETMEZ — {@code vaadin-combo-box-overlay} VİRTUALİZE bir listedir (18 kayıt render edilip
     * alfabetik "Akbank"tan "Garanti"ye kadar geliyor, "Türkiye İş Bankası A.Ş." (T) viewport'a hiç
     * girmiyor — web-automation.md'deki grid virtual-scroll tuzağının combo-box eşdeğeri). Çözüm:
     * combo-box'ın gerçek {@code input} alanına (light-DOM) Selenium sendKeys ile filtre metni
     * yazmak ({@code SupplierManagementPage#selectProductType} ile aynı kalıp) — bu overlay'i
     * TEK eşleşen satıra indirir, virtualization sorunu ortadan kalkar.
     */
    public boolean selectFactoring(String factoringNameKeyword) {
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-fk-combo-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-fk-combo-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var combos = o.querySelectorAll('vaadin-combo-box');" +
                    "  for (var c of combos) {" +
                    "    var lbl = fold(c.getAttribute('label') || (c.querySelector('label')||{}).textContent || '');" +
                    "    if (lbl === 'finansal kurum') {" +
                    "      var inp = c.querySelector('input');" +
                    "      if (!inp) return false;" +
                    "      inp.setAttribute('data-fk-combo-target', '1');" +
                    "      return true;" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] 'Finansal Kurum' combobox input'u işaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement input = driver.findElement(By.cssSelector("[data-fk-combo-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", input);
            input.click();
            Thread.sleep(300);
            input.sendKeys(factoringNameKeyword);
            Thread.sleep(900);

            // ⚠️ Düz toLowerCase() Türkçe "İ"yi (U+0130) "i" + BİRLEŞTİRİCİ NOKTA (U+0307) yapar
            // (web-automation.md kural #8 ile aynı tuzak) — bu yüzden seçim ASCII-fold ile yapılır.
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
                    "return false;", factoringNameKeyword);
            log.info("[DTS-MD] Finansal kurum '{}' (filtrelenmiş) seçildi mi: {}", factoringNameKeyword, picked);
            return Boolean.TRUE.equals(picked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS-MD] selectFactoring: {}", e.getMessage());
            return false;
        }
    }

    /** "DBS Maliyet (%)" alanına (sadece EDIT modunda görünür) yeni değer yazar. */
    public boolean setDbsCost(String value) {
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-fk-dbscost-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-fk-dbscost-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var fields = o.querySelectorAll('vaadin-text-field, vaadin-number-field');" +
                    "  for (var f of fields) {" +
                    "    var lbl = fold(f.getAttribute('label') || (f.querySelector('label')||{}).textContent || '');" +
                    "    if (lbl.indexOf('dbs maliyet') >= 0) { f.setAttribute('data-fk-dbscost-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS-MD] 'DBS Maliyet (%)' alanı bulundu mu: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                log.warn("[DTS-MD] 'DBS Maliyet (%)' alanı bulunamadı. Güncel metin: {}", dumpVisibleText());
                return false;
            }
            WebElement field = driver.findElement(By.cssSelector("[data-fk-dbscost-target='1']"));
            WebElement input = field.findElement(By.cssSelector("input"));
            // ⚠️ KÖK NEDEN #1 (2026-08-07, canlı DOM keşfiyle kanıtlandı): AmountField'ın
            // addFocusListener'ı odaklanınca sunucu tarafında ASENKRON olarak setPlainValue(plainValue)
            // (ham/eski değeri, ör. "0") client'a geri pushluyor. Bu push'u asıl TETİKLEYEN
            // input.clear()'ın KENDİSİ (native focus+select+delete). Push gecikmeli (network/VPN
            // round-trip) geldiği için clear()'dan hemen sonraki ilk keystroke'ların arasına düşüp o
            // ana kadar yazılanı eski değerle eziyor (kanıt: karakter-karakter yazımda "1" yazınca "0"
            // oluyor, "12" yazınca "02" oluyor — ilk karakter kayboluyor). Fix: clear()'dan SONRA
            // push'un oturması için bekle, SONRA tüm değeri TEK SEFERDE yaz (karakter karakter değil —
            // aradaki her ek round-trip yeni bir çarpışma riski taşır).
            input.click();
            try { Thread.sleep(400); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            input.clear();
            try { Thread.sleep(600); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            input.sendKeys(value);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                    "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", input);
            // ⚠️ KÖK NEDEN #2 (2026-08-07): AmountField virgül→nokta dönüşümünü ve sunucu-taraflı
            // plainValue senkronizasyonunu SADECE addBlurListener (blur event'i) içinde yapıyor.
            // Sentetik 'input'/'change' event'leri DOM'da görünen değeri günceller ama gerçek blur
            // olmadan sunucudaki plainValue eskisi kalır → "Güncelle" eski değeri kaydeder.
            // Gerçek Selenium TAB tuşu ile odağı taşıyıp native blur'u tetikliyoruz.
            input.sendKeys(Keys.TAB);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('blur',{bubbles:true}));" +
                    "arguments[0].dispatchEvent(new CustomEvent('change',{bubbles:true}));", input);
            try { Thread.sleep(400); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            log.info("[DTS-MD] 'DBS Maliyet (%)' alanına '{}' yazıldı ve blur tetiklendi.", value);
            return true;
        } catch (Exception e) {
            log.warn("[DTS-MD] setDbsCost: {}", e.getMessage());
            return false;
        }
    }

    /**
     * "Kaydet" (add) veya "Güncelle" (edit) butonuna basıp açılan "Onay" (Evet/Hayır) ConfirmDialog'unu
     * onaylar.
     *
     * ⚠️ Bu dialogun kendi buton metinleri "Kaydet"/"Güncelle"dir — {@link BasePageObject#acceptVaadinConfirmDialogIfPresent()}
     * pozitif-kelime listesiyle (evet/tamam/onayla/devam/yes/ok/confirm) ÇAKIŞMAZ. CompanyDtsSettingsDialog/
     * CompanyDtsAllocationDialog'daki "Devam Et"/"Onayla" çakışma tuzağı (excludeSubstring gerektiren) BURADA
     * YOK — genel metod güvenle kullanılabilir (2026-08-06 kod incelemesiyle doğrulandı).
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
                    "  if (full.indexOf('anlasmali finansal kurum') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    if (fold(b.textContent) === target) { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;", label);
            log.info("[DTS-MD] '{}' butonu tıklandı mı: {}", label, clicked);
            if (!Boolean.TRUE.equals(clicked)) {
                log.warn("[DTS-MD] '{}' butonu bulunamadı. Güncel metin: {}", label, dumpVisibleText());
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
            log.warn("[DTS-MD] clickSaveAndConfirm: {}", e.getMessage());
            return false;
        }
    }
}
