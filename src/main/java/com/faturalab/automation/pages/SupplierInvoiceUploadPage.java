package com.faturalab.automation.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Locale;

/**
 * Tedarikçi "FATURA YÜKLE / TALEP ET" dialogu — farklı dosya tipleriyle yükleme.
 *
 * Canlı DOM'da doğrulanmış yapı (2026-07-08):
 *  - Sidebar butonu: "FATURA YÜKLE/ TALEP ET"
 *  - Dialog başlığı: "FATURA YÜKLE / TALEP ET", sekmeler: Fatura Yükle / Fatura Sil / Fatura Yükleme Talebi
 *  - Fatura Türü radyoları (vaadin-radio-button):
 *      "E-Fatura/E-Arşiv/E-Müstahsil" -> xls, xlsx, xml, zip
 *      "Kağıt Fatura"                 -> jpg, jpeg, png, gif, bmp, pdf, doc, docx (İMZASIZ = PAPER)
 *  - vaadin-upload (auto-upload); dosya seçilince otomatik yüklenir.
 *
 * Dialog içi upload/notification işlemleri {@link CompanyInvoicePage}'e delege edilir.
 */
public class SupplierInvoiceUploadPage extends BasePageObject {

    private final CompanyInvoicePage dialogOps;

    public SupplierInvoiceUploadPage(WebDriver driver) {
        super(driver);
        this.dialogOps = new CompanyInvoicePage(driver);
    }

    /** Tedarikçi ekranında "FATURA YÜKLE/ TALEP ET" dialogunu açar. */
    public boolean openUploadDialog() {
        tryOpenNavigationDrawer();
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toUpperCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t.includes('FATURA YÜKLE') || t.includes('FATURA YUKLE')) { b.click(); return true; }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(clicked)) {
                log.info("Tedarikçi 'FATURA YÜKLE/ TALEP ET' butonuna tıklandı.");
                waitForVaadinNavigation();
            } else {
                log.warn("Tedarikçi fatura yükleme butonu bulunamadı.");
                return false;
            }
        } catch (Exception e) {
            log.warn("openUploadDialog: {}", e.getMessage());
            return false;
        }
        return dialogOps.isUploadDialogOpen();
    }

    /** "Kağıt Fatura" fatura türü radyosunu seçer (jpg/png/pdf/... görsel = PAPER). */
    public void selectKagitFatura() {
        selectFaturaTuru("kağıt");
    }

    /** "E-Fatura/E-Arşiv/E-Müstahsil" fatura türü radyosunu seçer (xls/xlsx/xml/zip). */
    public void selectEFatura() {
        selectFaturaTuru("e-fatura");
    }

    private void selectFaturaTuru(String needleLower) {
        try {
            Boolean ok = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var needle = arguments[0];" +
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "var root = overlay || document;" +
                    "var radios = root.querySelectorAll('vaadin-radio-button');" +
                    "for (var r of radios) {" +
                    "  var t = (r.textContent || '').toLowerCase();" +
                    "  if (t.includes(needle)) {" +
                    "    if (!(r.checked || r.hasAttribute('checked'))) r.click();" +
                    "    return true;" +
                    "  }" +
                    "}" +
                    "return false;", needleLower);
            log.info("Fatura türü '{}' seçimi: {}", needleLower, ok);
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("selectFaturaTuru '{}': {}", needleLower, e.getMessage());
        }
    }

    /** Görsel/PDF/belge dosyasını vaadin-upload'a gönderir (auto-upload tetiklenir). */
    public void uploadFile(String absoluteFilePath) {
        dialogOps.uploadFile(absoluteFilePath);
    }

    /**
     * E-Fatura (XML/ZIP) akışında dosya seçildikten sonra "Yükle" butonuna basar.
     * TC-COMP-01 kanıtı: tedarikçi XML upload uploadFile + clickSave ile çalışır.
     */
    public void submitUpload() {
        dialogOps.clickSave();
    }

    /**
     * Yükleme başarısını bekler. Kağıt fatura görseli auto-upload olduğundan
     * başarı toast'ı kısa süre görünür — hızlı poll edilir.
     */
    public boolean waitForUploadSuccess(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            String toast = readVisibleNotificationText();
            if (toast != null) {
                String lower = toast.toLowerCase(Locale.ROOT);
                if (lower.contains("hata") || lower.contains("başarısız") || lower.contains("basarisiz")
                        || lower.contains("only ") || lower.contains("uploaded")) {
                    // "...can be uploaded" red mesajı da içerir; başarı kelimesi yoksa hata say
                    if (!(lower.contains("başarı") || lower.contains("basari")
                            || lower.contains("successfully") || lower.contains("yüklendi") || lower.contains("yuklendi"))) {
                        log.warn("Yükleme hata/red bildirimi: {}", toast);
                        return false;
                    }
                }
                if (lower.contains("başarı") || lower.contains("basari") || lower.contains("successfully")
                        || lower.contains("yüklendi") || lower.contains("yuklendi")) {
                    log.info("Yükleme başarı bildirimi: {}", toast);
                    return true;
                }
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // Toast kaçırıldıysa: dialog KAPANDIYSA başarı say. Kağıt fatura detay modalı
        // validasyon fail'de AÇIK kalır; kapanması = Kaydet kabul etti = başarı.
        // isErrorNotificationVisible() KULLANMA — vaadin-notification-container hep DOM'da,
        // yanlış pozitif verir (kural #3). Onun yerine GÖRÜNÜR hata toast metnine bak.
        boolean dialogClosed = !dialogOps.isUploadDialogOpen();
        String lastToast = readVisibleNotificationText();
        boolean visibleError = lastToast != null
                && (lastToast.toLowerCase(Locale.ROOT).contains("hata")
                    || lastToast.toLowerCase(Locale.ROOT).contains("beklenmeyen")
                    || lastToast.toLowerCase(Locale.ROOT).contains("error")
                    || lastToast.toLowerCase(Locale.ROOT).contains("başarısız"));
        log.info("Başarı toast'ı yakalanamadı — dialog kapalı: {}, görünür hata: {} ({})",
                dialogClosed, visibleError, lastToast);
        return dialogClosed && !visibleError;
    }

    /**
     * Geçersiz dosya tipi reddini bekler. Kağıt fatura türünde desteklenmeyen uzantı
     * "Only jpg, jpeg, png, gif, bmp, pdf, doc and docx files can be uploaded" (veya TR karşılığı) verir.
     *
     * @return red bildirimi metni; görülmezse null
     */
    public String waitForRejectionMessage(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            String toast = readVisibleNotificationText();
            if (toast != null) {
                String lower = toast.toLowerCase(Locale.ROOT);
                boolean isRejection = lower.contains("only ") || lower.contains("can be uploaded")
                        || lower.contains("uzant") || lower.contains("yüklenebilir")
                        || lower.contains("yuklenebilir") || lower.contains("desteklen");
                if (isRejection) {
                    log.info("Dosya tipi reddi bildirimi: {}", toast);
                    return toast;
                }
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    /**
     * Kağıt fatura türünde desteklenmeyen uzantı reddini doğrular.
     * Dev'de red iki biçimde olur: (a) görünür red toast'ı, VEYA
     * (b) dosya sessizce işlenmez → kağıt fatura DETAY modalı hiç açılmaz.
     * İkisinden biri gerçekleşir + başarı toast'ı YOKSA reddedilmiş sayılır.
     *
     * @return red gerekçesi (toast metni veya "detay modalı açılmadı"); reddedilmediyse null
     */
    public String waitForRejectionOrNoProgress(int timeoutSeconds) {
        // 1) Önce klasik red toast'ı ara
        String toast = waitForRejectionMessage(timeoutSeconds);
        if (toast != null) {
            return toast;
        }
        // 2) Toast yoksa: detay modalı açıldı mı? Açıldıysa dosya KABUL edilmiş → red DEĞİL.
        boolean detailOpened = isPaperDetailModalOpen();
        // 3) Yanlışlıkla başarı toast'ı geldiyse red sayma
        String last = readVisibleNotificationText();
        boolean success = last != null && (last.toLowerCase(Locale.ROOT).contains("başarı")
                || last.toLowerCase(Locale.ROOT).contains("basari")
                || last.toLowerCase(Locale.ROOT).contains("successfully"));
        if (!detailOpened && !success) {
            log.info("Desteklenmeyen uzantı işlenmedi (detay modalı açılmadı) → red kabul edildi.");
            return "Detay modalı açılmadı — dosya kabul edilmedi";
        }
        log.warn("Red doğrulanamadı: detayModalı={}, başarıToast={}", detailOpened, success);
        return null;
    }

    /**
     * Bug #5746 doğrulaması: yükleme sırasında "Bilinmeyen bir hata oluştu"
     * (unknown error) bildirimi çıkıp çıkmadığını izler.
     *
     * @return unknown-error bildirimi görüldüyse metni; süre içinde çıkmazsa null
     */
    public String waitForUnknownError(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            String toast = readVisibleNotificationText();
            if (toast != null) {
                String lower = toast.toLowerCase(Locale.ROOT);
                if (lower.contains("bilinmeyen") || lower.contains("beklenmeyen")
                        || lower.contains("unknown error") || lower.contains("unexpected error")) {
                    log.warn("Bug #5746 — bilinmeyen hata bildirimi görüldü: {}", toast);
                    return toast;
                }
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    /** Kağıt fatura detay modalı (CompanyPaperInvoiceDialog) görünür mü? */
    private boolean isPaperDetailModalOpen() {
        try {
            Object r = ((JavascriptExecutor) driver).executeScript(
                    "var os = document.querySelectorAll('vaadin-dialog-overlay');" +
                    "for (var o of os) {" +
                    "  var rc = o.getBoundingClientRect();" +
                    "  if (rc.width < 2) continue;" +
                    "  var h = o.querySelector('h2, h3, [class*=\"title\"]');" +
                    "  var t = ((h ? h.textContent : '') || o.textContent || '').toLowerCase();" +
                    "  if (t.includes('fatura tutarı') || t.includes('fatura tutari')) return true;" +
                    "}" +
                    "return false;");
            return Boolean.TRUE.equals(r);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Görsel yüklendikten sonra açılan kağıt fatura DETAY modalını
     * (CompanyPaperInvoiceDialog, başlık "Fatura Yükleme") doldurup kaydeder.
     *
     * İlk iterasyon stratejisi: modaldaki zorunlu (required) boş alanlar akıllı
     * varsayılanlarla doldurulur; zorunlu combobox'larda ilk seçenek seçilir.
     * Her alan loglanır — fail durumunda koşum logundan ince ayar yapılır.
     *
     * @param invoiceNo fatura no alanına yazılacak benzersiz numara
     */
    public boolean fillPaperInvoiceDetailsAndSave(String invoiceNo) {
        try {
            // 1. Detay modalının açılmasını bekle (upload sonrası asenkron)
            boolean opened = com.faturalab.automation.utils.VaadinGridFilterHelper.waitForJs(driver, 15,
                    "var os = document.querySelectorAll('vaadin-dialog-overlay');" +
                    "for (var o of os) {" +
                    "  var r = o.getBoundingClientRect();" +
                    "  if (r.width < 2) continue;" +
                    "  var h = o.querySelector('h2, h3, [class*=\"title\"]');" +
                    "  var t = ((h ? h.textContent : '') || o.textContent || '').toLowerCase();" +
                    "  if (t.includes('fatura yükleme') || t.includes('fatura tutarı') || t.includes('fatura tutari')) return true;" +
                    "}" +
                    "return false;");
            if (!opened) {
                log.warn("Kağıt fatura detay modalı açılmadı.");
                return false;
            }
            log.info("Kağıt fatura detay modalı açıldı.");

            // 2. Modal alan dökümü (teşhis için). Vaadin'de label bir PROPERTY —
            //    getAttribute('label') boş döner, f.label okunmalı.
            Object dump = ((JavascriptExecutor) driver).executeScript(
                    "var ov = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;}).pop();" +
                    "if (!ov) return 'ov yok';" +
                    "var out = []; var i = 0;" +
                    "var fields = ov.querySelectorAll('vaadin-text-field, vaadin-number-field, vaadin-integer-field, " +
                    "vaadin-date-picker, vaadin-combo-box, vaadin-select, vaadin-text-area');" +
                    "for (var f of fields) {" +
                    "  var r = f.getBoundingClientRect();" +
                    "  if (r.width < 2) continue;" +
                    "  var lab = f.label || f.getAttribute('label') || f.getAttribute('placeholder') || '';" +
                    "  var req = f.required || f.hasAttribute('required');" +
                    "  var inp = f.querySelector('input, textarea');" +
                    "  var val = inp ? (inp.value || '') : '';" +
                    "  out.push('[' + (i++) + '] ' + f.tagName.toLowerCase() + ' L=\"' + lab + '\" req=' + req +" +
                    "    ' x=' + Math.round(r.x) + ' val=' + val.substring(0,16));" +
                    "}" +
                    "return out.join('  ||  ');");
            log.info("Detay modal alanları: {}", dump);

            // 3. Zorunlu boş text/number/date alanlarını label PROPERTY'sine göre doldur
            Object filled = ((JavascriptExecutor) driver).executeScript(
                    "var invoiceNo = arguments[0];" +
                    "var ov = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;}).pop();" +
                    "var report = [];" +
                    "var today = new Date();" +
                    "var due = new Date(); due.setDate(due.getDate()+90);" +
                    "function fmt(d){var m=('0'+(d.getMonth()+1)).slice(-2);var g=('0'+d.getDate()).slice(-2);return g+'.'+m+'.'+d.getFullYear();}" +
                    "var fields = ov.querySelectorAll('vaadin-text-field, vaadin-number-field, vaadin-integer-field, vaadin-date-picker, vaadin-text-area');" +
                    "for (var f of fields) {" +
                    "  var r = f.getBoundingClientRect(); if (r.width < 2) continue;" +
                    "  var inp = f.querySelector('input, textarea'); if (!inp) continue;" +
                    "  if ((inp.value || '').trim() !== '') continue;" +
                    "  var isReq = f.required || f.hasAttribute('required'); if (!isReq) continue;" +
                    "  var lab = (f.label || f.getAttribute('label') || '').toLowerCase();" +
                    "  var tag = f.tagName.toLowerCase();" +
                    "  var val = null; var isoVal = null;" +
                    "  if (tag === 'vaadin-date-picker') { var d = lab.includes('vade') ? due : today;" +
                    "    val = fmt(d);" +
                    "    isoVal = d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2); }" +
                    "  else if (lab.includes('tutar') || lab.includes('amount')) val = '1000';" +
                    "  else if (lab.includes('fatura no') || lab.includes('fatura numar') || lab === 'no') val = invoiceNo;" +
                    "  else if (lab.includes('vergi') && lab.includes('no')) val = '3960656675';" +
                    "  else if (lab.includes('posta')) val = '06000';" +
                    "  else if (lab.includes('telefon')) val = '05001234567';" +
                    "  else if (lab.includes('adres')) val = 'Otomasyon Test Adres No:1';" +
                    "  else if (lab.includes('ünvan') || lab.includes('unvan') || lab.includes('ad')) val = 'Otomasyon Test';" +
                    "  else val = 'Otomasyon';" +
                    // Vaadin: input.value binding'i tetiklemez — ÖNCE component.value (date-picker ISO ister)
                    "  try { f.value = (isoVal !== null ? isoVal : val); } catch(e){}" +
                    "  if (inp) { inp.focus(); inp.value = val;" +
                    "    inp.dispatchEvent(new Event('input', {bubbles:true}));" +
                    "    inp.dispatchEvent(new Event('change', {bubbles:true}));" +
                    "    inp.dispatchEvent(new Event('blur', {bubbles:true})); }" +
                    "  report.push('[' + lab + ']=' + val);" +
                    "}" +
                    "return report.join(', ');", invoiceNo);
            log.info("Doldurulan alanlar: {}", filled);
            Thread.sleep(800);

            // 4a. Combo envanteri (teşhis): required combo'ları label/x/tag ile logla
            Object comboInv = ((JavascriptExecutor) driver).executeScript(
                    "var ov = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;}).pop();" +
                    "var out=[]; var i=0;" +
                    "var combos = ov.querySelectorAll('vaadin-combo-box, vaadin-select');" +
                    "for (var cb of combos) {" +
                    "  var r = cb.getBoundingClientRect(); if (r.width<2) continue;" +
                    "  var inp = cb.querySelector('input'); var cur = inp?(inp.value||''):(cb.value||'');" +
                    "  out.push('['+(i++)+'] '+cb.tagName.toLowerCase()+' L=\"'+(cb.label||'')+'\" req='+(cb.required||cb.hasAttribute('required'))+' x='+Math.round(r.x)+' val='+cur.substring(0,12));" +
                    "}" +
                    "return out.join('  ||  ');");
            log.info("Combo envanteri: {}", comboInv);

            // 4b. İl-önce, İlçe-sonra sırayla her boş required combo'yu aç + ilk item'ı seç.
            //     Türkçe fold ile label eşleştir; item overlay'de açılır.
            String[] order = {"il", "ilce", "diger"};
            for (String pass : order) {
                for (int c = 0; c < 4; c++) {
                    Object comboLabel = ((JavascriptExecutor) driver).executeScript(
                            "var pass = arguments[0];" +
                            "function fold(s){return (s||'').replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s')" +
                            "  .replace(/[ğĞ]/g,'g').replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c').toLowerCase();}" +
                            "var ov = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                            "  .filter(function(o){return o.getBoundingClientRect().width>2;}).pop();" +
                            "var combos = Array.prototype.slice.call(ov.querySelectorAll('vaadin-combo-box, vaadin-select'));" +
                            "for (var k=0;k<combos.length;k++) { var cb=combos[k];" +
                            "  var r = cb.getBoundingClientRect(); if (r.width<2) continue;" +
                            "  if (cb.hasAttribute('data-auto-done')) continue;" +
                            "  var inp = cb.querySelector('input'); var cur = inp?(inp.value||''):(cb.value||'');" +
                            "  if ((cur||'').trim() !== '') { cb.setAttribute('data-auto-done','1'); continue; }" +
                            "  var lab = fold(cb.label);" +
                            "  var isIlce = lab.indexOf('ilce')>=0;" +
                            "  var isIl = (lab==='il' || (lab.indexOf('il')>=0 && !isIlce));" +
                            "  var match = (pass==='il'&&isIl) || (pass==='ilce'&&isIlce) || (pass==='diger'&&!isIl&&!isIlce);" +
                            "  if (!match) continue;" +
                            "  cb.setAttribute('data-auto-done','1');" +
                            "  var tag = cb.tagName.toLowerCase();" +
                            "  if (tag==='vaadin-select') { var b=cb.querySelector('vaadin-select-value-button,[role=button]'); (b||cb).click(); }" +
                            "  else { cb.click(); if(inp){inp.focus();inp.click();} }" +
                            "  return (cb.label||'?')+'|'+tag+'|x='+Math.round(r.x);" +
                            "}" +
                            "return null;", pass);
                    if (comboLabel == null) break;
                    Thread.sleep(1000);
                    Object picked = ((JavascriptExecutor) driver).executeScript(
                            "var items = document.querySelectorAll(" +
                            "  'vaadin-select-overlay vaadin-item, vaadin-combo-box-overlay vaadin-combo-box-item, " +
                            "   vaadin-combo-box-item, vaadin-item, [role=\"option\"]');" +
                            "for (var it of items) {" +
                            "  var r = it.getBoundingClientRect(); if (r.width<2||r.height<2) continue;" +
                            "  var t = (it.textContent||'').trim();" +
                            "  if (t.length>0 && t.toLowerCase().indexOf('seçiniz')<0) { it.click(); return t.substring(0,22); }" +
                            "}" +
                            "return 'ITEM-YOK';");
                    log.info("Combo '{}' → seçilen: {}", comboLabel, picked);
                    Thread.sleep(700);
                }
            }

            // 4c. Fatura Tutarı: JS value binding'i bu özel alanda tutmuyor (screenshot: kırmızı/boş
            //     kalıyor). Selenium sendKeys ile gerçek klavye girişi yap — Vaadin kesin bind eder.
            //     Ayrıca label'ı boş olan 2. date-picker = Vade Tarihi; bugün yerine +90 iş günü yaz
            //     (bugün vade INVOICE_EXPIRED verir).
            ((JavascriptExecutor) driver).executeScript(
                    "var ov = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;}).pop();" +
                    "var fields = ov.querySelectorAll('vaadin-text-field, vaadin-number-field');" +
                    "for (var f of fields) {" +
                    "  var lab = (f.label||'').toLowerCase();" +
                    "  if (lab.indexOf('tutar') >= 0) { var i=f.querySelector('input'); if(i) i.setAttribute('data-auto-tutar','1'); }" +
                    "}" +
                    // vade: fatura bilgileri sütunundaki (x<600) label'sız date-picker
                    "var dps = ov.querySelectorAll('vaadin-date-picker');" +
                    "for (var d of dps) {" +
                    "  var r=d.getBoundingClientRect(); var l=(d.label||'').toLowerCase();" +
                    "  if (r.x < 600 && (l==='' || l.indexOf('vade')>=0)) { var di=d.querySelector('input'); if(di) di.setAttribute('data-auto-vade','1'); }" +
                    "}");
            try {
                // JS doldurma date-picker'lara focus atınca takvim açılmış olabilir —
                // ÖNCE tümünü kapat (kullanıcı gözlemi: açık takvim Fatura Tutarı'nı bloke ediyor).
                closeAllDatePickerOverlays();

                // 1) Fatura Tutarı — takvim kapalıyken Selenium sendKeys (JS binding tutmuyor)
                java.util.List<org.openqa.selenium.WebElement> tutarInputs =
                        driver.findElements(org.openqa.selenium.By.cssSelector("input[data-auto-tutar]"));
                log.info("data-auto-tutar input sayısı: {}", tutarInputs.size());
                for (org.openqa.selenium.WebElement ti : tutarInputs) {
                    // Koşulsuz: value dolu görünse de (getAttribute eski/format değer dönebilir)
                    // ekranda boş — her zaman temizle ve yaz.
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", ti);
                    ti.click();
                    ti.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
                    ti.sendKeys(org.openqa.selenium.Keys.DELETE);
                    ti.sendKeys("1000");
                    ti.sendKeys(org.openqa.selenium.Keys.TAB);
                    log.info("Fatura Tutarı sendKeys → value={}", ti.getAttribute("value"));
                }
                closeAllDatePickerOverlays(); // tutar TAB'ı bir sonraki date-picker'ı açmış olabilir

                // 2) Vade Tarihi — sendKeys + ESCAPE (ENTER YOK; ENTER formu erken tetikliyordu)
                java.util.List<org.openqa.selenium.WebElement> vadeInputs =
                        driver.findElements(org.openqa.selenium.By.cssSelector("input[data-auto-vade]"));
                log.info("data-auto-vade input sayısı: {}", vadeInputs.size());
                java.time.LocalDate v = java.time.LocalDate.now().plusDays(90);
                while (v.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                        || v.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                    v = v.plusDays(1);
                }
                String vadeStr = v.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                for (org.openqa.selenium.WebElement vi : vadeInputs) {
                    vi.click();
                    vi.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"));
                    vi.sendKeys(vadeStr);
                    // ESCAPE GÖNDERME: dialog'a propagate edip TÜM modalı kapatıyor (closeOnEsc).
                    // Takvimi JS ile kapatacağız (closeAllDatePickerOverlays) — modalı etkilemez.
                    log.info("Vade Tarihi sendKeys → {}", vadeStr);
                }
                closeAllDatePickerOverlays(); // Kaydet öncesi takvim kapat (modalı KAPATMADAN)
                Thread.sleep(500);
            } catch (Exception e) {
                log.warn("Tutar/vade sendKeys: {}", e.getMessage());
            }

            // 5. Kaydet — butonu işaretle + görünür alana kaydır (ekran dışında kalabilir),
            //    sonra GERÇEK Selenium click ile bas (JS click sessizce işlemeyebilir).
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var ov = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;}).pop();" +
                    "if(!ov) return false;" +
                    "var btns = ov.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  if ((b.textContent || '').trim() === 'Kaydet' && !b.disabled) {" +
                    "    b.setAttribute('data-auto-kaydet','1');" +
                    "    b.scrollIntoView({block:'center', inline:'center'});" +
                    "    return true;" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("Kaydet butonu bulundu/işaretlendi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                log.warn("Kaydet butonu bulunamadı (aktif değil veya modal alanların dışında)");
                return false;
            }
            Thread.sleep(300);
            boolean clicked = false;
            try {
                org.openqa.selenium.WebElement kaydetBtn =
                        driver.findElement(org.openqa.selenium.By.cssSelector("[data-auto-kaydet='1']"));
                kaydetBtn.click(); // görünür değilse ElementNotInteractable fırlatır → yanlış pozitif önlenir
                clicked = true;
                log.info("Kaydet'e gerçek Selenium click yapıldı");
            } catch (Exception clickEx) {
                log.warn("Kaydet Selenium click başarısız ({}), JS click deneniyor", clickEx.getMessage());
                Object jsClick = ((JavascriptExecutor) driver).executeScript(
                        "var b=document.querySelector(\"[data-auto-kaydet='1']\"); if(b){b.click();return true;} return false;");
                clicked = Boolean.TRUE.equals(jsClick);
            }
            if (!clicked) {
                return false;
            }
            Thread.sleep(1200);
            acceptVaadinConfirmDialogIfPresent(); // vade/tatil onayı gelirse
            // DOĞRULAMA: modal Kaydet ile kapandı mı? (ESCAPE artık gönderilmiyor → kapanma = Kaydet başarılı)
            boolean modalClosed = !dialogOps.isUploadDialogOpen();
            log.info("Kaydet sonrası modal kapandı mı: {}", modalClosed);
            return modalClosed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("fillPaperInvoiceDetailsAndSave: {}", e.getMessage());
            return false;
        }
    }

    /** Açık kalan tüm vaadin-date-picker takvim overlay'lerini kapatır (üstteki alanları bloke ederler). */
    private void closeAllDatePickerOverlays() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('vaadin-date-picker').forEach(function(d){ try { d.opened=false; } catch(e){} });" +
                    "document.querySelectorAll('vaadin-date-picker-overlay, vaadin-date-picker-overlay-content')" +
                    "  .forEach(function(o){ try { o.remove(); } catch(e){} });");
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
    }

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
            return null;
        }
    }
}
