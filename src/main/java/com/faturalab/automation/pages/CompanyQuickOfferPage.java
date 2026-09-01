package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Tedarikçi — Hızlı teklif al / işlem bekleyenler (UAT FL-006, FL-007, E2E).
 */
public class CompanyQuickOfferPage extends BasePageObject {

    private static final By DIALOG = By.cssSelector("vaadin-dialog-overlay");
    private static final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final CompanyInvoicePage invoice;

    public CompanyQuickOfferPage(WebDriver driver) {
        super(driver);
        this.invoice = new CompanyInvoicePage(driver);
    }

    public void navigateToFaturalarim() {
        invoice.navigateToInvoiceList();
    }

    /**
     * Tedarikçi menüsünden gelen / alınan finansman teklifleri listesine gider (E2E FL-008).
     */
    public boolean navigateToGelenTeklifler() {
        String[] keywords = {
                "gelen teklif", "gelen teklifler", "alinan teklif", "alınan teklif",
                "teklifler", "gelen"};
        for (String kw : keywords) {
            if (clickNavItemByText(kw)) {
                waitForVaadinNavigation();
                try {
                    Thread.sleep(900);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return true;
            }
        }
        log.warn("Gelen teklifler navigasyonu: menü bulunamadı.");
        return false;
    }

    public void navigateToIslemBekleyenler() {
        invoice.dismissInvoiceUploadDialogIfOpen();
        tryOpenNavigationDrawer();
        // Tedarikçi sidebar'ında sayfa adı "İşlemdekiler" (2026-07-02 canlı DOM)
        if (clickNavItemByText("işlemdekiler") || clickNavItemByText("islemdekiler")) {
            waitForVaadinNavigation();
            return;
        }
        if (!clickNavItemByText("işlem bekleyen")) {
            clickNavItemByText("islem bekleyen");
        }
        if (!clickNavItemByText("bekleyen")) {
            try {
                WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//vaadin-button[contains(@class,'menu-button') and " +
                                "contains(translate(normalize-space(),'İIı','iiI'),'bekleyen')]")));
                menu.click();
                waitForVaadinNavigation();
            } catch (Exception e) {
                log.warn("İşlem bekleyenler: {}", e.getMessage());
            }
        }
    }

    /**
     * @param ignoredInvoiceNo boş ise ilk anlamlı satır seçilir
     */
    /**
     * Aktif grid'de ilk anlamlı fatura satırına tıklar (işlem bekleyenler / teklif öncesi).
     */
    public void selectFirstGridRowForOffer() {
        try {
            invoice.dismissInvoiceUploadDialogIfOpen();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-grid")));
            List<WebElement> cells = driver.findElements(By.cssSelector("vaadin-grid-cell-content"));
            for (WebElement c : cells) {
                String t = c.getText();
                if (t == null) {
                    continue;
                }
                String s = t.trim();
                if (s.length() > 5 && (s.matches(".*\\d.*") || s.contains("/") || s.contains("202"))) {
                    c.click();
                    try {
                        Thread.sleep(450);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return;
                }
            }
            if (!cells.isEmpty()) {
                cells.get(0).click();
                Thread.sleep(400);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("İlk grid satırı seçimi: {}", e.getMessage());
        }
    }

    /**
     * Teklif talebi dialogunda varsa "otomatik teklif" / "otomatik gönderim" benzeri onay kutusunu işaretler.
     */
    public void enableOtomatikTeklifSecenekleriIfPresent() {
        try {
            Boolean toggled = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function walk(node, d) {" +
                    "  if (!node || d > 16) return false;" +
                    "  if (node.shadowRoot && walk(node.shadowRoot, d + 1)) return true;" +
                    "  var tag = (node.tagName || '').toLowerCase();" +
                    "  if (tag === 'vaadin-checkbox' || tag === 'input') {" +
                    "    var lab = (node.getAttribute && (node.getAttribute('label') || node.getAttribute('aria-label'))) || '';" +
                    "    var txt = ((node.textContent || '') + ' ' + lab).toLowerCase();" +
                    "    if (txt.includes('otomatik') && (txt.includes('teklif') || txt.includes('gönder') || txt.includes('gonder') || txt.includes('fatura'))) {" +
                    "      if (tag === 'input' && node.type === 'checkbox' && !node.checked) { node.click(); return true; }" +
                    "      if (node.click && !node.checked) { node.click(); return true; }" +
                    "    }" +
                    "  }" +
                    "  var ch = node.children;" +
                    "  if (ch) for (var i = 0; i < ch.length; i++) if (walk(ch[i], d + 1)) return true;" +
                    "  return false;" +
                    "}" +
                    "return walk(document.querySelector('vaadin-dialog-overlay') || document.body, 0);");
            if (Boolean.TRUE.equals(toggled)) {
                Thread.sleep(300);
                log.info("Otomatik teklif / otomatik akış seçeneği etkinleştirildi (varsa).");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.debug("Otomatik teklif checkbox: {}", e.getMessage());
        }
    }

    /**
     * Metin eşleşen ilk tıklanabilir buton (gölge ağaç + light DOM uzantısı).
     */
    public boolean clickButtonMatchingInDom(String labelNeedle) {
        if (labelNeedle == null) {
            return false;
        }
        String needle = labelNeedle.toLowerCase().trim();
        try {
            Boolean ok = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var needle = arguments[0].toLowerCase();" +
                    "function matchBtn(t) {" +
                    "  if (!t) return false;" +
                    "  var x = t.toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (x.includes(needle)) return true;" +
                    "  if (needle.includes('kabul') && (x.includes('kabul') || x === 'evet')) return true;" +
                    "  return false;" +
                    "}" +
                    "function walk(node, d) {" +
                    "  if (!node || d > 18) return false;" +
                    "  if (node.shadowRoot && walk(node.shadowRoot, d + 1)) return true;" +
                    "  var btns = node.querySelectorAll ? node.querySelectorAll('vaadin-button, button, a[role=\"button\"]') : [];" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    var t = b.textContent || '';" +
                    "    if (matchBtn(t)) { b.click(); return true; }" +
                    "  }" +
                    "  var ch = node.children;" +
                    "  if (ch) for (var j = 0; j < ch.length; j++) if (walk(ch[j], d + 1)) return true;" +
                    "  return false;" +
                    "}" +
                    "return walk(document.body, 0);",
                    needle);
            if (Boolean.TRUE.equals(ok)) {
                Thread.sleep(600);
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Buton tıklama ({}): {}", labelNeedle, e.getMessage());
        }
        return false;
    }

    public boolean selectFatura(String ignoredInvoiceNo) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-grid-cell-content")));
            java.util.List<WebElement> cells = driver.findElements(By.cssSelector("vaadin-grid-cell-content"));
            for (WebElement c : cells) {
                String t = c.getText();
                if (t != null && t.length() > 3 && !t.equalsIgnoreCase("GÖZAT") && !t.contains("TEKLİF")) {
                    c.click();
                    return true;
                }
            }
            if (!cells.isEmpty()) {
                cells.get(0).click();
                return true;
            }
        } catch (Exception e) {
            log.warn("Fatura satırı seçimi: {}", e.getMessage());
        }
        return false;
    }

    public boolean clickHizliTeklifAl() {
        try {
            Boolean js = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function matches(txt) {" +
                    "  if (!txt) return false;" +
                    "  function tclean(s) { return (s || '').replace(/\\s+/g,' ').trim(); }" +
                    "  var t = txt.toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  var tr = tclean(txt);" +
                    "  if (t.includes('hızlı teklif') || t.includes('hizli teklif')) return true;" +
                    "  if (t.includes('teklif al') || t.includes('teklif talebi')) return true;" +
                    "  if (/tekl[iı]f/i.test(tr) && /h[iı]zl[iı]/i.test(tr)) return true;" +
                    "  if (tr.indexOf('TEKLİF AL') >= 0 || tr.indexOf('TEKLIF AL') >= 0) return true;" +
                    "  return false;" +
                    "}" +
                    "function btnText(b) {" +
                    "  var t = b.textContent || '';" +
                    "  if (!t.trim() && b.shadowRoot) t = b.shadowRoot.textContent || '';" +
                    "  return t;" +
                    "}" +
                    "function walk(node, depth) {" +
                    "  if (!node || depth > 16) return false;" +
                    "  if (node.shadowRoot && walk(node.shadowRoot, depth + 1)) return true;" +
                    "  var btns = node.querySelectorAll ? node.querySelectorAll('vaadin-button, button') : [];" +
                    "  for (var i = 0; i < btns.length; i++) {" +
                    "    if (matches(btnText(btns[i])) && !btns[i].disabled) { btns[i].click(); return true; }" +
                    "  }" +
                    "  var ch = node.children;" +
                    "  if (ch) for (var j = 0; j < ch.length; j++) if (walk(ch[j], depth + 1)) return true;" +
                    "  return false;" +
                    "}" +
                    "return walk(document.body, 0);");
            if (Boolean.TRUE.equals(js)) {
                Thread.sleep(800);
                return true;
            }
        } catch (Exception e) {
            log.warn("Hızlı teklif al: {}", e.getMessage());
        }
        return false;
    }

    public boolean isModalOpen() {
        try {
            Boolean js = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function overlayVisible(o) {" +
                    "  if (!o) return false;" +
                    "  var cs = window.getComputedStyle(o);" +
                    "  if (cs.display === 'none' || cs.visibility === 'hidden' || cs.opacity === '0') return false;" +
                    "  var r = o.getBoundingClientRect();" +
                    "  return r.width > 2 && r.height > 2;" +
                    "}" +
                    "var sel = 'vaadin-dialog-overlay, vaadin-confirm-dialog-overlay';" +
                    "var list = document.querySelectorAll(sel);" +
                    "for (var i = 0; i < list.length; i++) {" +
                    "  if (overlayVisible(list[i])) return true;" +
                    "}" +
                    "return false;");
            return Boolean.TRUE.equals(js);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean selectTeklifSuresi(String gun) {
        try {
            Boolean picked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var g = arguments[0];" +
                    "function walk(node, d) {" +
                    "  if (!node || d > 18) return false;" +
                    "  if (node.shadowRoot && walk(node.shadowRoot, d + 1)) return true;" +
                    "  var items = node.querySelectorAll ? node.querySelectorAll('vaadin-item, vaadin-radio-button, [role=\"option\"]') : [];" +
                    "  for (var it of items) {" +
                    "    if ((it.textContent || '').includes(g)) { it.click(); return true; }" +
                    "  }" +
                    "  var sels = node.querySelectorAll ? node.querySelectorAll('vaadin-select, vaadin-combo-box') : [];" +
                    "  for (var s of sels) { if (!s.disabled) { s.click(); return true; } }" +
                    "  var ch = node.children;" +
                    "  if (ch) for (var j = 0; j < ch.length; j++) if (walk(ch[j], d + 1)) return true;" +
                    "  return false;" +
                    "}" +
                    "var ov = document.querySelector('vaadin-dialog-overlay');" +
                    "if (walk(ov || document.body, 0)) return true;" +
                    "return walk(document.body, 0);",
                    gun);
            Thread.sleep(500);
            if (Boolean.TRUE.equals(picked)) {
                ((JavascriptExecutor) driver).executeScript(
                        "var g = arguments[0];" +
                        "var items = document.querySelectorAll('vaadin-item, [role=\"option\"]');" +
                        "for (var it of items) { if ((it.textContent || '').includes(g)) { it.click(); break; } }",
                        gun);
                Thread.sleep(400);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickGonder() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function btnText(b) {" +
                    "  var t = b.textContent || '';" +
                    "  if (!t.trim() && b.shadowRoot) t = b.shadowRoot.textContent || '';" +
                    "  return t.toLowerCase().trim();" +
                    "}" +
                    "function tryRoot(root) {" +
                    "  if (!root) return false;" +
                    "  var btns = root.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    var t = btnText(b);" +
                    "    if (b.disabled) continue;" +
                    "    if (t.includes('gönder') || t.includes('gonder') || t.includes('oluştur') || t.includes('olustur') " +
                    "     || t.includes('tamam') || (t.includes('kaydet') && t.length < 20)) {" +
                    "      b.click(); return true;" +
                    "    }" +
                    "  }" +
                    "  return false;" +
                    "}" +
                    "var ov = document.querySelector('vaadin-dialog-overlay');" +
                    "if (tryRoot(ov)) return true;" +
                    "if (ov && ov.shadowRoot && tryRoot(ov.shadowRoot)) return true;" +
                    "return tryRoot(document.body);");
            if (!Boolean.TRUE.equals(clicked)) {
                log.warn("Gönder/Kaydet butonu bulunamadı.");
            }
            Thread.sleep(1200);
        } catch (Exception e) {
            log.warn("Gönder: {}", e.getMessage());
        }
    }

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 8).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Toast için kısa kontrol (polling döngülerinde tekrarlı bekleme yapmaz). */
    public boolean isSuccessNotificationPresentQuick() {
        try {
            List<WebElement> found = driver.findElements(SUCCESS_NOTIFICATION);
            for (WebElement el : found) {
                try {
                    if (el.isDisplayed()) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.debug("peek toast: {}", e.getMessage());
        }
        return false;
    }

    // ─── TZF işlemi akışı ────────────────────────────────────────────────────

    /**
     * Fatura listesinde verilen fatura numarasının satırındaki "TEKLİF AL" butonuna tıklar.
     * Satır bazlı buton bulunamazsa sayfadaki ilk TEKLİF AL butonuna düşer.
     */
    public boolean clickTeklifAlForInvoice(String invoiceNo) {
        try {
            invoice.dismissInvoiceUploadDialogIfOpen();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-grid")));
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "var invoiceNo = arguments[0];" +
                    "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                    "var targetIdx = -1;" +
                    "for (var i = 0; i < cells.length; i++) {" +
                    "  if ((cells[i].textContent || '').includes(invoiceNo)) { targetIdx = i; break; }" +
                    "}" +
                    "function isTeklifAl(b) {" +
                    "  var t = (b.textContent || '').toUpperCase().replace(/\\s+/g,' ').trim();" +
                    "  if (b.disabled || (!t.includes('TEKLİF AL') && !t.includes('TEKLIF AL'))) return false;" +
                    "  var r = b.getBoundingClientRect();" + // virtual scroll cache butonlarını atla
                    "  return r.width > 2 && r.height > 2;" +
                    "}" +
                    "if (targetIdx >= 0) {" +
                    // Aynı satır: fatura no hücresinin komşu hücrelerinde TEKLİF AL ara
                    "  for (var j = targetIdx; j < Math.min(cells.length, targetIdx + 15); j++) {" +
                    "    var btns = cells[j].querySelectorAll('vaadin-button, button');" +
                    "    for (var b of btns) { if (isTeklifAl(b)) { b.click(); return 'row_match'; } }" +
                    "  }" +
                    "  for (var k = Math.max(0, targetIdx - 15); k < targetIdx; k++) {" +
                    "    var btns2 = cells[k].querySelectorAll('vaadin-button, button');" +
                    "    for (var b2 of btns2) { if (isTeklifAl(b2)) { b2.click(); return 'row_match_back'; } }" +
                    "  }" +
                    "}" +
                    "var all = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var a of all) { if (isTeklifAl(a)) { a.click(); return 'first_fallback'; } }" +
                    "return null;",
                    invoiceNo);
            log.info("TEKLİF AL tıklama sonucu ({}): {}", invoiceNo, result);
            if (result != null) {
                Thread.sleep(1000);
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("clickTeklifAlForInvoice ({}): {}", invoiceNo, e.getMessage());
        }
        return false;
    }

    /**
     * TEKLİF AL'a basar ve teklif modalı açılana kadar retry eder.
     * Grid yeniden render sırasında TEKLİF AL tıklaması bazen sunucuya işlemiyor
     * → modal açılmıyordu (flaky). Her denemede tıkla + modal açılışını poll et (#5798 fix).
     */
    public boolean clickTeklifAlAndWaitModal(String invoiceNo, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("TEKLİF AL denemesi {}/{} ({})", attempt, maxAttempts, invoiceNo);
                if (clickTeklifAlForInvoice(invoiceNo)) {
                    long deadline = System.currentTimeMillis() + 6000L;
                    while (System.currentTimeMillis() < deadline) {
                        if (isModalOpen()) {
                            log.info("Teklif modalı açıldı (deneme {}).", attempt);
                            return true;
                        }
                        Thread.sleep(500);
                    }
                }
                log.warn("Deneme {}: teklif modalı açılmadı, tekrar denenecek.", attempt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("clickTeklifAlAndWaitModal deneme {}: {}", attempt, e.getMessage());
            }
        }
        return false;
    }

    /**
     * Açılan teklif modalında hiçbir alanı değiştirmeden tekrar "Teklif Al" butonuna basar.
     */
    public boolean confirmTeklifAlInModal() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function visible(el) {" +
                    "  var r = el.getBoundingClientRect();" +
                    "  return r.width > 2 && r.height > 2;" +
                    "}" +
                    "var overlays = document.querySelectorAll('vaadin-dialog-overlay');" +
                    "for (var o of overlays) {" +
                    "  if (!visible(o)) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    var t = (b.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "    if (!b.disabled && (t.includes('teklif al') || t === 'teklif al')) { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(clicked)) {
                log.info("Modal içinde 'Teklif Al' onaylandı.");
                Thread.sleep(1200);
                acceptVaadinConfirmDialogIfPresent();
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("confirmTeklifAlInModal: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Teklif sonrası "İşlemdekiler" sayfasına otomatik yönlenmeyi bekler.
     * Yönlenme gelmezse menüden kendisi gider.
     */
    public boolean waitForIslemdekilerPage(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (isOnIslemdekilerPage()) {
                log.info("İşlemdekiler sayfasına yönlenildi.");
                return true;
            }
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        log.warn("İşlemdekiler yönlenmesi gelmedi — menüden gidiliyor.");
        navigateToIslemBekleyenler();
        waitForVaadinNavigation();
        return isOnIslemdekilerPage();
    }

    private boolean isOnIslemdekilerPage() {
        try {
            Boolean onPage = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var url = (window.location.href || '').toLowerCase();" +
                    "if (url.includes('islemdeki') || url.includes('bekleyen')) return true;" +
                    "var body = (document.body.innerText || '').toUpperCase();" +
                    "return body.includes('KABUL / İPTAL') || body.includes('KABUL/İPTAL') " +
                    "    || (body.includes('İŞLEMDEKİLER') && body.includes('KABUL'));");
            return Boolean.TRUE.equals(onPage);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * İşlemdekiler gridinde EN YENİ (en büyük numaralı) bordroyu döner.
     *
     * Bu, testin kendi az önce oluşturduğu teklif talebidir: bordro numaraları
     * artan sırada üretilir ve tedarikçinin listesinde yalnızca kendi talepleri
     * bulunur. "İlk satır" varsayımı YANLIŞ — grid sıralaması eski/teklifsiz
     * talepleri öne alabiliyor (CI'da TZF-001 flaky'sinin kök nedeni buydu).
     *
     * @return bordro no (ör. A2026_78207); bulunamazsa null
     */
    public String findLatestBordroInGrid() {
        try {
            Object r = ((JavascriptExecutor) driver).executeScript(
                    "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                    "var re = /([A-Z]\\d{4}_(\\d{2,}))/;" +
                    "var best = null, bestNum = -1;" +
                    "for (var c of cells) {" +
                    "  var rc = c.getBoundingClientRect(); if (rc.width < 2 || rc.height < 2) continue;" +
                    "  var m = (c.textContent || '').match(re);" +
                    "  if (!m) continue;" +
                    "  var n = parseInt(m[2], 10);" +
                    "  if (n > bestNum) { bestNum = n; best = m[1]; }" +
                    "}" +
                    "return best;");
            String bordro = r != null ? r.toString().trim() : null;
            log.info("İşlemdekiler'de en yeni bordro: {}", bordro);
            return bordro;
        } catch (Exception e) {
            log.warn("findLatestBordroInGrid: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verilen bordronun BULUNDUĞU SATIRDAKİ "Kabul / İptal" butonuna basar.
     * (Sadece "ilk satır"a basmak yanlış auction'ı açıyordu — #TZF CI flaky fix.)
     */
    public boolean clickKabulIptalForBordro(String bordroNo) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "var target = arguments[0];" +
                    "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                    "var idx = -1;" +
                    "for (var i = 0; i < cells.length; i++) {" +
                    "  var rc = cells[i].getBoundingClientRect(); if (rc.width < 2) continue;" +
                    "  if ((cells[i].textContent || '').indexOf(target) >= 0) { idx = i; break; }" +
                    "}" +
                    "if (idx < 0) return 'bordro_hucresi_yok';" +
                    "function isKabulIptal(b) {" +
                    "  if (b.disabled) return false;" +
                    "  var t = (b.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (!(t.indexOf('kabul') >= 0 && (t.indexOf('iptal') >= 0 || t.indexOf('/') >= 0))) return false;" +
                    "  var r = b.getBoundingClientRect(); return r.width > 2 && r.height > 2;" +
                    "}" +
                    // Aynı satır: bordro hücresinin komşu hücrelerinde ara (önce ileri, sonra geri)
                    "for (var j = idx; j < Math.min(cells.length, idx + 14); j++) {" +
                    "  var bs = cells[j].querySelectorAll('vaadin-button, button');" +
                    "  for (var b of bs) { if (isKabulIptal(b)) { b.click(); return 'satir_ileri'; } }" +
                    "}" +
                    "for (var k = Math.max(0, idx - 14); k < idx; k++) {" +
                    "  var bs2 = cells[k].querySelectorAll('vaadin-button, button');" +
                    "  for (var b2 of bs2) { if (isKabulIptal(b2)) { b2.click(); return 'satir_geri'; } }" +
                    "}" +
                    "return 'buton_yok';",
                    bordroNo);
            log.info("Kabul/İptal tıklama ({}): {}", bordroNo, result);
            if ("satir_ileri".equals(result) || "satir_geri".equals(result)) {
                Thread.sleep(1200);
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("clickKabulIptalForBordro ({}): {}", bordroNo, e.getMessage());
        }
        return false;
    }

    /** Görünür dialogu "Kapat"/"İptal" ile kapatır (tekrar denemeden önce temiz durum). */
    public void closeVisibleDialog() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var ov = ovs[ovs.length-1]; if(!ov) return false;" +
                    "var btns = ov.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent||'').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t === 'kapat' || t === 'iptal' || t === 'i̇ptal') { b.click(); return true; }" +
                    "}" +
                    "return false;");
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
    }

    /**
     * KENDİ bordromuzun teklifini kabul eder ve commit'i doğrular.
     *
     * Akış: bordronun satırındaki Kabul/İptal → "Kabul Et" → ABF onayı + Evet
     * + gerçek başarı toast'ı.
     *
     * ⚠️ OTOBİT SENKRONDUR (kaynak kod: AuctionModel.makeAutoBidsOfAuctionWithSession
     * startAuctionWithSession ile AYNI transaction'da; cron'da otobit job'ı YOK).
     * Teklif, talep oluşurken ya üretilir ya hiç üretilmez — BEKLEMEKLE GELMEZ.
     * Bu yüzden retry yalnızca UI render/JS-click kırılganlığı içindir (kısa aralık),
     * teklifin düşmesini beklemek için değil.
     *
     * @return commit doğrulandıysa bordro no; aksi halde null (neden loglanır)
     */
    public String acceptOfferForBordro(String bordroNo, int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Kabul denemesi {}/{} — bordro {}", attempt, maxAttempts, bordroNo);
                if (!clickKabulIptalForBordro(bordroNo)) {
                    log.warn("Deneme {}: bordro {} satırında Kabul/İptal açılamadı.", attempt, bordroNo);
                    Thread.sleep(2500);
                    continue;
                }
                if (acceptFirstOfferInModal() && checkAbfAndConfirmAccept()) {
                    log.info("Kabul COMMIT doğrulandı — bordro {} (deneme {})", bordroNo, attempt);
                    return bordroNo;
                }
                log.warn("Deneme {}: bordro {} kabul edilemedi.", attempt, bordroNo);
                closeVisibleDialog();
                Thread.sleep(2500);   // modal/grid yeniden render için — teklif beklemek için DEĞİL
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("acceptOfferForBordro deneme {}: {}", attempt, e.getMessage());
            }
        }
        log.error("KABUL EDİLEMEDİ — bordro {}. {}", bordroNo, noOfferDiagnostics());
        return null;
    }

    /**
     * "Kabul Et" bulunamadığında teşhis metni üretir.
     *
     * Kaynak kod (CompanyAuctionApprovalDialog:428) teklif satırını
     * {@code if (!offer.isOffered()) return new HorizontalLayout();} ile boş render eder —
     * yani Kabul Et'in yokluğu = o teklif için otobit ÜRETİLMEMİŞ (auctionoffer.offered=false,
     * amount=null). Otobit'in elenme nedenleri (AuctionModel.makeAutoBidsOfAuctionWithSession):
     * minParticipationAmountLimit altı tutar, bidRate null (barem/kriter tablosu satırı yok),
     * vade tatile denk gelmesi, limit yetersizliği, FK cut-off saati, GİB doğrulaması,
     * buyer.factoringPrioritizationType (ROUND_ROBIN'de her koşumda farklı FK seçilir).
     */
    public String noOfferDiagnostics() {
        try {
            Object dump = ((JavascriptExecutor) driver).executeScript(
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var ov = ovs[ovs.length-1];" +
                    "if(!ov) return 'modal_kapali';" +
                    "return Array.from(ov.querySelectorAll('vaadin-button, button'))" +
                    "  .map(function(b){return (b.textContent||'').replace(/\\s+/g,' ').trim();})" +
                    "  .filter(function(t){return t.length>0;}).join(' | ');");
            String s = String.valueOf(dump);
            boolean noOffer = s.contains("Teklif Talebini İptal Et") && !s.toLowerCase().contains("kabul et");
            return "Modal butonları: [" + s + "]"
                    + (noOffer
                       ? " → TEKLİF YOK: otobit bu talep için teklif üretmemiş (offered=false). "
                         + "Olası nedenler: minParticipationAmountLimit, bidRate/kriter tablosu satırı yok, "
                         + "vade tatil günü, limit yetersiz, FK cut-off, GİB doğrulaması, "
                         + "buyer.factoringPrioritizationType (ROUND_ROBIN). "
                         + "DB ile doğrula: SELECT offered, amount, factoringid FROM auctionoffer WHERE auctionid=..."
                       : " → Kabul Et render edilmiş ama tıklama/commit başarısız (UI kırılganlığı).");
        } catch (Exception e) {
            return "teşhis alınamadı: " + e.getMessage();
        }
    }

    /** İşlemdekiler listesinde ilk satırın "Kabul / İptal" butonuna basar. */
    public boolean clickKabulIptal() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (!b.disabled && t.includes('kabul') && (t.includes('iptal') || t.includes('/'))) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    // tek başına 'KABUL' etiketi taşıyan liste aksiyonu
                    "for (var b2 of btns) {" +
                    "  var t2 = (b2.textContent || '').toLowerCase().trim();" +
                    "  if (!b2.disabled && t2 === 'kabul') { b2.click(); return true; }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(clicked)) {
                log.info("'Kabul / İptal' butonuna tıklandı.");
                Thread.sleep(1200);
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("clickKabulIptal: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Kabul/İptal modalında aşağı kaydırıp "Teklifler" sekmesi altındaki
     * ilk teklifin "Kabul Et" butonuna basar.
     */
    public boolean acceptFirstOfferInModal() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            // Modal içeriğini en alta kaydır (Teklifler bölümü modalın altında)
            js.executeScript(
                    "var overlays = document.querySelectorAll('vaadin-dialog-overlay');" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect();" +
                    "  if (r.width < 2) continue;" +
                    "  var scrollables = [o].concat(Array.from(o.querySelectorAll('*')));" +
                    "  if (o.shadowRoot) {" +
                    "    var content = o.shadowRoot.querySelector('[part=\"content\"], [part=\"overlay\"]');" +
                    "    if (content) scrollables.unshift(content);" +
                    "  }" +
                    "  for (var s of scrollables) {" +
                    "    if (s.scrollHeight > s.clientHeight + 10) { s.scrollTop = s.scrollHeight; }" +
                    "  }" +
                    "}");
            Thread.sleep(700);

            // "Teklifler" sekmesi/başlığı varsa tıkla (görünür SON overlay'de — ilk overlay
            // bayat/görünmez olabilir; CI'da bu yüzden buton bulunamıyordu)
            js.executeScript(
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var root = ovs.length ? ovs[ovs.length-1] : document;" +
                    "var els = root.querySelectorAll('vaadin-tab, [role=\"tab\"], vaadin-button, h3, h4, span');" +
                    "for (var el of els) {" +
                    "  var t = (el.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t === 'teklifler' || t === 'gelen teklifler') { el.click(); return true; }" +
                    "}" +
                    "return false;");
            Thread.sleep(700);

            // "Kabul Et" butonu — yalnızca MODAL RENDER gecikmesi için poll (10 sn).
            // Otobit senkron olduğundan teklifin sonradan düşmesini beklemek anlamsız;
            // 10 sn içinde çıkmazsa teklif zaten üretilmemiştir (noOfferDiagnostics).
            long btnDeadline = System.currentTimeMillis() + 10000L;
            while (System.currentTimeMillis() < btnDeadline) {
                Boolean accepted = (Boolean) js.executeScript(
                        "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                        "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                        "var root = ovs.length ? ovs[ovs.length-1] : document;" +
                        "var btns = root.querySelectorAll('vaadin-button, button');" +
                        "for (var b of btns) {" +
                        "  var t = (b.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                        "  if (!b.disabled && (t === 'kabul et' || t.includes('kabul et'))) {" +
                        "    try { b.scrollIntoView({block:'center'}); } catch (e) {}" +
                        "    b.click(); return true;" +
                        "  }" +
                        "}" +
                        "return false;");
                if (Boolean.TRUE.equals(accepted)) {
                    log.info("Teklifler altındaki ilk 'Kabul Et' butonuna tıklandı.");
                    Thread.sleep(1000);
                    return true;
                }
                Thread.sleep(1500); // teklif henüz düşmemiş olabilir — bekle, tekrar bak
            }
            // Teşhis için modal içeriği dökümü
            Object dump = js.executeScript(
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var root = ovs.length ? ovs[ovs.length-1] : null;" +
                    "if(!root) return 'gorunur_overlay_yok';" +
                    "return Array.from(root.querySelectorAll('vaadin-button, button'))" +
                    "  .map(function(b){return (b.textContent||'').replace(/\\s+/g,' ').trim();})" +
                    "  .filter(function(t){return t.length>0;}).join(' | ');");
            log.warn("'Kabul Et' butonu 30sn içinde görünmedi. Modal butonları: {}", dump);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("acceptFirstOfferInModal: {}", e.getMessage());
        }
        return false;
    }

    /**
     * WP#5844 canlı doğrulaması — Kabul/İptal modalını (CompanyAuctionApprovalDialog) SADECE
     * OKUMAK için açar: modalı en alta kaydırır, "Teklifler" sekmesi varsa tıklar (salt görünüm
     * değişimi, YIKICI DEĞİL — "Kabul Et"e ASLA basılmaz), sonra görünür overlay'in TÜM metnini
     * döker. Otobit tutarı ve ağırlıklı ortalama vade bu dökümden regex/gözle okunur.
     *
     * @return görünür overlay'in ham metni (bulunamazsa boş string)
     */
    public String scrollAndDumpApprovalModalReadOnly() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                    "var overlays = document.querySelectorAll('vaadin-dialog-overlay');" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect();" +
                    "  if (r.width < 2) continue;" +
                    "  var scrollables = [o].concat(Array.from(o.querySelectorAll('*')));" +
                    "  if (o.shadowRoot) {" +
                    "    var content = o.shadowRoot.querySelector('[part=\"content\"], [part=\"overlay\"]');" +
                    "    if (content) scrollables.unshift(content);" +
                    "  }" +
                    "  for (var s of scrollables) {" +
                    "    if (s.scrollHeight > s.clientHeight + 10) { s.scrollTop = s.scrollHeight; }" +
                    "  }" +
                    "}");
            Thread.sleep(700);
            js.executeScript(
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var root = ovs.length ? ovs[ovs.length-1] : document;" +
                    "var els = root.querySelectorAll('vaadin-tab, [role=\"tab\"], vaadin-button, h3, h4, span');" +
                    "for (var el of els) {" +
                    "  var t = (el.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t === 'teklifler' || t === 'gelen teklifler') { el.click(); return true; }" +
                    "}" +
                    "return false;");
            Thread.sleep(700);
            Object dump = js.executeScript(
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var root = ovs.length ? ovs[ovs.length-1] : null;" +
                    "if (!root) return '';" +
                    "return (root.textContent || '').replace(/\\s+/g,' ').trim();");
            String text = String.valueOf(dump);
            log.info("[WP5844] Kabul/İptal modalı (salt-okunur) döküm uzunluğu: {} karakter", text.length());
            return text;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } catch (Exception e) {
            log.warn("[WP5844] scrollAndDumpApprovalModalReadOnly: {}", e.getMessage());
            return "";
        }
    }

    /** Kabul sonrası gelen "Evet" onay modalını onaylar. */
    public boolean confirmEvet() {
        acceptVaadinConfirmDialogIfPresent();
        return true;
    }

    /**
     * "Onay" dialogundaki (CompanyAuctionConfirmDialog) zorunlu
     * "ABF belgesini okudum onaylıyorum" checkbox'ını işaretler, sonra "Evet"e basar
     * ve commit'i doğrular.
     *
     * KÖK NEDEN (kaynak kod CompanyAuctionConfirmDialog:196): ABF gerekli + checkbox
     * işaretsizse "Evet" yalnızca "Lütfen ABF'yi onaylayınız" uyarısı verip commit ETMEZ
     * → auction WAITING kalır. Bu yüzden Evet'ten ÖNCE checkbox işaretlenmeli (#5798 fix).
     *
     * @return commit doğrulandıysa (Onay dialogu kapandı, ABF uyarısı gelmedi) true
     */
    public boolean checkAbfAndConfirmAccept() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            // 1. Görünür overlay'deki ABF checkbox'ını işaretle (varsa)
            Object checked = js.executeScript(
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var ov = ovs[ovs.length-1]; if(!ov) return 'no_overlay';" +
                    "var cbs = ov.querySelectorAll('vaadin-checkbox');" +
                    "for (var cb of cbs) {" +
                    "  var t = (cb.textContent||'').toLowerCase();" +
                    "  if (t.includes('abf') || t.includes('okudum') || t.includes('onayl')) {" +
                    "    if (!cb.checked) {" +
                    "      var inp = cb.querySelector('input') || (cb.shadowRoot && cb.shadowRoot.querySelector('input'));" +
                    "      if (inp) { inp.click(); } else { cb.click(); }" +
                    "    }" +
                    "    return 'checked:' + cb.checked;" +
                    "  }" +
                    "}" +
                    "return 'no_abf_checkbox';");
            log.info("ABF checkbox durumu: {}", checked);
            Thread.sleep(400);

            // 2. "Evet" butonuna bas (Onay dialogu içindeki acceptButton)
            Object evet = js.executeScript(
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var ov = ovs[ovs.length-1]; if(!ov) return false;" +
                    "var btns = ov.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent||'').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (!b.disabled && t === 'evet') { b.click(); return true; }" +
                    "}" +
                    "return false;");
            log.info("'Evet' tıklandı: {}", evet);
            Thread.sleep(1500);

            // 3. Commit doğrula — GERÇEK başarı toast'ı: "Teklif talebi başarıyla tamamlandı".
            //    "Evet" sonrası ara dialog (Cari Hesap seçimi / hizmet bedeli>0 ise ödeme)
            //    açılabilir; asıl acceptOffer commit'i o dialog tamamlanınca oluyor (kaynak kod
            //    CompanyAuctionConfirmDialog:280-320 success(CurrentAccount) callback). Ara dialogu
            //    genel onay butonuyla ilerlet, sonra gerçek başarı toast'ını bekle. Toast yoksa FAIL
            //    (auction WAITING kalır — DB-dürüst sinyal, #5798).
            long deadline = System.currentTimeMillis() + 15000L;
            while (System.currentTimeMillis() < deadline) {
                // Gerçek başarı toast'ı?
                Boolean success = (Boolean) js.executeScript(
                        "var cards = document.querySelectorAll('vaadin-notification-card');" +
                        "for (var c of cards){ var r=c.getBoundingClientRect(); if(r.width<2) continue;" +
                        "  var t=(c.textContent||'').toLowerCase();" +
                        "  if (t.includes('başarıyla tamamland') || t.includes('basariyla tamamland')" +
                        "      || t.includes('gerçekleş') || t.includes('gerceklesi')) return true; }" +
                        "return false;");
                if (Boolean.TRUE.equals(success)) {
                    log.info("Gerçek başarı toast'ı görüldü — kabul COMMIT oldu.");
                    return true;
                }
                // ABF uyarısı?
                Boolean warn = (Boolean) js.executeScript(
                        "var cards = document.querySelectorAll('vaadin-notification-card');" +
                        "for (var c of cards){ var t=(c.textContent||'').toLowerCase();" +
                        "  if (t.includes('abf') && t.includes('onayla')) return true; }" +
                        "return false;");
                if (Boolean.TRUE.equals(warn)) {
                    log.warn("ABF uyarısı çıktı — commit olmadı.");
                    return false;
                }
                // Ara dialog (cari hesap/ödeme) açıldıysa: genel onay/seç/devam butonuna bas.
                Object follow = js.executeScript(
                        "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                        "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                        "var ov = ovs[ovs.length-1]; if(!ov) return 'yok';" +
                        "var btns = ov.querySelectorAll('vaadin-button, button');" +
                        "for (var b of btns){ if(b.disabled) continue;" +
                        "  var t=(b.textContent||'').toLowerCase().replace(/\\s+/g,' ').trim();" +
                        "  if (t==='onayla'||t==='kaydet'||t==='devam'||t==='seç'||t==='sec'||t==='evet'||t==='tamam') {" +
                        "    b.click(); return 'follow:'+t; } }" +
                        "return 'buton_yok';");
                if (String.valueOf(follow).startsWith("follow:")) {
                    log.info("Ara dialog ilerletildi: {}", follow);
                }
                Thread.sleep(600);
            }
            log.warn("Gerçek başarı toast'ı gelmedi — kabul commit doğrulanamadı (WAITING kalmış olabilir).");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("checkAbfAndConfirmAccept: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Kabul onayı sonrası ekranda görünen bordro numarasını yakalar.
     * Bildirim toast'ı, açık dialog ve sayfa gövdesini "bordro" kelimesi
     * çevresindeki alfasayısal değer için tarar.
     *
     * @return bordro no; bulunamazsa null
     */
    public String captureBordroNo() {
        try {
            Object found = ((JavascriptExecutor) driver).executeScript(
                    // ⚠️ SADECE taze kaynaklar: kabul sonrası açılan bildirim toast'ı ve dialog.
                    // document.body.innerText KULLANILMAZ — sayfada duran ESKİ bir bordroyu
                    // yakalayıp kabul BAŞARISIZ olsa bile yanlış pozitif üretiyordu (#5798 bulgusu).
                    "var sources = [];" +
                    "var cards = document.querySelectorAll('vaadin-notification-card, vaadin-notification-container');" +
                    "for (var c of cards) { var r=c.getBoundingClientRect(); if (r.width>2 && r.height>2) sources.push(c.textContent || ''); }" +
                    "var overlays = document.querySelectorAll('vaadin-dialog-overlay, vaadin-confirm-dialog-overlay');" +
                    "for (var o of overlays) { var ro=o.getBoundingClientRect(); if (ro.width>2) sources.push(o.textContent || ''); }" +
                    // Bordro no formatı (canlıda doğrulandı): A2026_77768 — harf + yıl + '_' + sıra.
                    "var reStrict = /bordro\\s*(?:no|numaras[ıi])?\\s*[:#]?\\s*([A-Z]\\d{4}_\\d{2,})/i;" +
                    "var reAny = /([A-Z]\\d{4}_\\d{2,})/;" +
                    "for (var s of sources) { var m = s.match(reStrict); if (m && m[1]) return m[1]; }" +
                    // reAny sadece bu taze kaynaklar içinde (whole-body DEĞİL).
                    "for (var s2 of sources) { var m2 = s2.match(reAny); if (m2 && m2[1]) return m2[1]; }" +
                    "return null;");
            String bordroNo = found != null ? found.toString().trim() : null;
            log.info("Bordro no yakalama sonucu (taze kaynak): {}", bordroNo);
            return bordroNo;
        } catch (Exception e) {
            log.warn("captureBordroNo: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Commit toast'ı görüldükten SONRA bordro numarasını POLL eder. Backend commit
     * (bordro üretimi) UI başarı toast'ından biraz geç tamamlanabiliyor — özellikle
     * CI'da (headless, dev_ci lokalden yavaş) bu gecikme belirginleşiyor. Tek seferlik
     * okuma null dönerse HEMEN vazgeçmek yerine kısa aralıklı retry ile null→değer
     * geçişini yakalar (Jenkins build #42 flake fix).
     *
     * @return yakalanan bordro no; süre içinde hâlâ null ise null
     */
    private String pollForBordroNo(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        String bordro = captureBordroNo();
        while (bordro == null && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            bordro = captureBordroNo();
        }
        return bordro;
    }

    /**
     * Teklif kabulünü yapar ve taze bordro toast'ı görünene kadar retry eder.
     * Kabul akışı Vaadin timing nedeniyle bazen commit olmuyordu (auction WAITING kalıyor);
     * her denemede kabul + onay tekrarlanır, taze bordro yakalanınca döner (#5798 fix).
     *
     * @return yakalanan bordro no; hiçbir denemede taze bordro çıkmazsa null
     */
    public String acceptOfferWithRetryAndCapture(int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Teklif kabul denemesi {}/{}", attempt, maxAttempts);
                if (!clickKabulIptal()) {
                    log.warn("Deneme {}: Kabul/İptal açılamadı.", attempt);
                    continue;
                }
                // Kabul edilen auction'ın bordrosunu Kabul/İptal modalından yakala (bu run'a ait).
                String bordro = captureBordroNo();
                acceptFirstOfferInModal();          // "Kabul Et" → "Onay" dialogu açılır
                boolean committed = checkAbfAndConfirmAccept();  // ABF işaretle + Evet + commit doğrula
                if (committed) {
                    // Backend commit (bordro üretimi) UI başarı toast'ından BİRAZ GEÇ
                    // tamamlanabiliyor — CI'da (headless, dev_ci) bu gecikme daha belirgin
                    // (build #42: toast görüldü ama tek seferlik okuma null döndürdü).
                    // Toast görüldükten SONRA bordro null döndükçe kısa aralıklarla POLL et;
                    // hemen fail etme.
                    String after = pollForBordroNo(12000L);
                    if (after != null) bordro = after;
                    log.info("Kabul COMMIT doğrulandı — bordro: {} (deneme {})", bordro, attempt);
                    return bordro;
                }
                log.warn("Deneme {}: kabul commit olmadı, tekrar denenecek.", attempt);
            } catch (Exception e) {
                log.warn("acceptOfferWithRetryAndCapture deneme {}: {}", attempt, e.getMessage());
            }
        }
        return null;
    }

    // ─── WP#5649 — taze fatura ile organik iptal + tekrar teklif al ─────────

    /**
     * Verilen bordronun Kabul/İptal modalında "Teklif Talebini İptal Et" (rejectButton,
     * {@code CompanyAuctionApprovalDialog.17}) butonuna basar, "Onay" dialogunda (Evet/Hayır)
     * "Evet" ile onaylar ve gerçek başarı toast'ını ("Teklif talebi iptal edildi.",
     * {@code CompanyAuctionApprovalDialog.21}) bekler.
     *
     * Kaynak kod (AuctionModel.rejectAuctionWithSession): auction.status → REJECTED,
     * ilgili Offer'lar AUCTIONREJECT, invoice.remainingAmount geri eklenir.
     *
     * @return gerçek "iptal edildi" toast'ı görüldüyse true
     */
    public boolean cancelAuctionForBordro(String bordroNo) {
        try {
            if (!clickKabulIptalForBordro(bordroNo)) {
                log.warn("[WP5649] Bordro {} için Kabul/İptal modalı açılamadı.", bordroNo);
                return false;
            }
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String clickScript =
                    "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                    "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                    "var ov = ovs[ovs.length-1]; if(!ov) return false;" +
                    "var btns = ov.querySelectorAll('vaadin-button, button');" +
                    // ⚠️ 'İ'.toLowerCase() JS'de "i" + U+0307 (combining dot) üretir — düz
                    // 'iptal' string'i bu yüzden eşleşmez. NFKD + combining mark temizliği şart.
                    "function foldTr(s) {" +
                    "  return (s || '').normalize('NFKD').replace(/[\\u0300-\\u036f]/g,'')" +
                    "    .toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "}" +
                    "for (var b of btns) {" +
                    "  var t = foldTr(b.textContent);" +
                    "  if (!b.disabled && t.includes('teklif talebini') && t.includes('iptal')) {" +
                    "    try { b.scrollIntoView({block:'center'}); } catch (e) {}" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;";
            Boolean clicked = (Boolean) js.executeScript(clickScript);
            // Modal render/hydration gecikmesi için kısa poll ile tekrar dene (ilk denemede
            // buton henüz DOM'a eklenmemiş olabilir).
            long clickDeadline = System.currentTimeMillis() + 6000L;
            while (!Boolean.TRUE.equals(clicked) && System.currentTimeMillis() < clickDeadline) {
                Thread.sleep(600);
                clicked = (Boolean) js.executeScript(clickScript);
            }
            log.info("[WP5649] 'Teklif Talebini İptal Et' tıklandı mı: {}", clicked);
            if (!Boolean.TRUE.equals(clicked)) {
                Object dump = js.executeScript(
                        "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                        "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                        "var ov = ovs[ovs.length-1];" +
                        "if(!ov) return 'gorunur_overlay_yok';" +
                        "return Array.from(ov.querySelectorAll('vaadin-button, button'))" +
                        "  .map(function(b){return (b.textContent||'').replace(/\\s+/g,' ').trim() + (b.disabled?'[disabled]':'');})" +
                        "  .filter(function(t){return t.length>0;}).join(' | ');");
                log.warn("[WP5649] 'Teklif Talebini İptal Et' bulunamadı. Modal butonları: {}", dump);
                return false;
            }
            // "Onay" (Evet/Hayır) dialogu render gecikmesi için kısa poll ile bekle + "Evet"e bas.
            // ⚠️ Paylaşılan acceptVaadinConfirmDialogIfPresent() BİLİNÇLİ OLARAK kullanılmıyor:
            // o metod her tıklamadan sonra sabit waitForVaadinNavigation() (1500ms document.readyState
            // beklemesi + ekstra Thread.sleep(1500)) + kendi Thread.sleep(500)'ünü uyguluyor — diğer
            // 14 sayfa/akışta (admin güncelleme, DTS, kullanıcı ekleme vb.) bu doğru çünkü onlar sayfa
            // navigasyonu bekliyor. BU akışta ise "Evet" sonrası başarı toast'ı SADECE 2000ms ekranda
            // kalıyor (kaynak kod: Alert.info(title) → info(title, null, 2000), Alert.java:38-39).
            // Paylaşılan helper'ın ~2000ms'lik sabit post-click gecikmesi TEK BAŞINA toast'ın ömrünü
            // tüketiyordu; CI'daki (dev_ci) ekstra round-trip overhead'i eklenince toast tamamen
            // kaçırılıyordu (Jenkins build #100 — bkz. repro_teklif_iptal_toast_flaky raporu).
            // Bu yüzden "Evet" tıklaması ile toast poll'ünün BAŞLAMASI arasında SIFIR ek bekleme olmalı.
            boolean evetClicked = clickEvetFastNoWait();
            log.info("[WP5649] 'Evet' tıklandı mı: {}", evetClicked);
            if (!evetClicked) {
                log.warn("[WP5649] Onay dialogunda 'Evet' butonu bulunamadı — bordro {}.", bordroNo);
                return false;
            }

            // "Evet" tıklamasının HEMEN ardından, sıkı aralıklarla (200ms) toast'ı poll et.
            // Toplam pencere CI yavaşlığı için 12sn — sorun süre değil, poll'un GEÇ BAŞLAMASIYDI.
            long deadline = System.currentTimeMillis() + 12000L;
            while (System.currentTimeMillis() < deadline) {
                Boolean success = (Boolean) js.executeScript(
                        "var cards = document.querySelectorAll('vaadin-notification-card');" +
                        "for (var c of cards){ var r=c.getBoundingClientRect(); if(r.width<2) continue;" +
                        "  var t=(c.textContent||'').toLowerCase();" +
                        "  if (t.includes('iptal edildi')) return true; }" +
                        "return false;");
                if (Boolean.TRUE.equals(success)) {
                    log.info("[WP5649] Gerçek 'Teklif talebi iptal edildi.' toast'ı görüldü — bordro {}.", bordroNo);
                    return true;
                }
                // Hata toast'ı çıktıysa (backend reddetti/ABF vb.) hemen FAIL — modal kapanmış
                // olsa bile başarı sayma.
                Object errorToast = js.executeScript(
                        "var cards = document.querySelectorAll('vaadin-notification-card');" +
                        "for (var c of cards){ var r=c.getBoundingClientRect(); if(r.width<2) continue;" +
                        "  var t=(c.textContent||'').toLowerCase();" +
                        "  if (t.includes('hata') || t.includes('başarısız') || t.includes('basarisiz')) return t; }" +
                        "return null;");
                if (errorToast != null) {
                    log.warn("[WP5649] Hata toast'ı görüldü — bordro {} iptal edilemedi: {}", bordroNo, errorToast);
                    return false;
                }
                Thread.sleep(200);
            }

            // Fallback (toast'a güvenmek tek başına kırılgan — .claude/rules/web-automation.md
            // "CI'da E2E flaky" kalıbı): kaynak kod (CompanyAuctionApprovalDialog) rejectAuction()
            // başarılı OLMADAN exitSuccess(false)'u çağırmaz — yani "Onay"/Kabul-İptal modalının
            // gerçekten KAPANMIŞ olması, kısa ömürlü toast'tan bağımsız ikinci bir başarı sinyalidir.
            if (!isModalOpen()) {
                log.warn("[WP5649] Toast 12sn içinde görünmedi ama modal kapandı — fallback sinyaliyle "
                        + "başarı kabul ediliyor (bordro {}). Kaynak kod: rejectAuction() başarısız olsaydı "
                        + "exitSuccess çağrılmaz, dialog açık kalırdı.", bordroNo);
                return true;
            }
            log.warn("[WP5649] İptal toast'ı 12sn içinde görünmedi ve modal hâlâ açık — bordro {}.", bordroNo);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[WP5649] cancelAuctionForBordro ({}): {}", bordroNo, e.getMessage());
            return false;
        }
    }

    /**
     * "Onay" (Evet/Hayır) dialogunda "Evet" butonuna basar — ÇOK HAFİF, sabit bekleme yok.
     *
     * Paylaşılan {@link #acceptVaadinConfirmDialogIfPresent()}'ten FARKLI olarak tıklama sonrası
     * {@code waitForVaadinNavigation()} veya ek {@code Thread.sleep} ÇAĞIRMAZ — sadece dialogun
     * render gecikmesi ihtimaline karşı kısa aralıklı (150ms) bir bulma-poll'ü içerir. Bu, kısa
     * ömürlü (2000ms) başarı toast'larını hemen ardından yakalaması gereken akışlar için tasarlandı
     * (bkz. {@link #cancelAuctionForBordro(String)} javadoc'undaki kök neden açıklaması).
     *
     * @return "Evet" butonu bulunup tıklandıysa true
     */
    private boolean clickEvetFastNoWait() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script =
                "var ovs = Array.from(document.querySelectorAll(" +
                "  'vaadin-dialog-overlay, vaadin-confirm-dialog-overlay, vaadin-confirm-dialog'))" +
                "  .filter(function(o){var r=o.getBoundingClientRect(); return r.width>2 || r.height>2;});" +
                "for (var i = ovs.length - 1; i >= 0; i--) {" +
                "  var ov = ovs[i];" +
                "  var btns = ov.querySelectorAll('vaadin-button, button');" +
                "  for (var b of btns) {" +
                "    if (b.disabled) continue;" +
                "    var t = (b.textContent||'').toLowerCase().replace(/\\s+/g,' ').trim();" +
                "    if (t === 'evet' || t.indexOf('evet ') === 0) { b.click(); return true; }" +
                "  }" +
                "}" +
                "return false;";
        try {
            Boolean clicked = (Boolean) js.executeScript(script);
            long deadline = System.currentTimeMillis() + 6000L;
            while (!Boolean.TRUE.equals(clicked) && System.currentTimeMillis() < deadline) {
                Thread.sleep(150);
                clicked = (Boolean) js.executeScript(script);
            }
            return Boolean.TRUE.equals(clicked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[WP5649] clickEvetFastNoWait: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Fatura için TEKRAR "TEKLİF AL" tıklar ve sonucu gözlemler: teklif modalı mı açıldı
     * (AC#2 hatası YOK) yoksa "Temlik tutarı, kalan tutardan yüksek olamaz." (veya benzer)
     * hata toast'ı mı çıktı (AC#2 hatası VAR — {@code AuctionInvoiceGroupModel.1}).
     *
     * @return "MODAL_OPENED" | "ERROR_TOAST:<metin>" | "NEITHER"
     */
    public String retryTeklifAlAndObserve(String invoiceNo, int timeoutSeconds) {
        if (!clickTeklifAlForInvoice(invoiceNo)) {
            log.warn("[WP5649] Tekrar TEKLİF AL tıklanamadı: {}", invoiceNo);
            return "NEITHER";
        }
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (isModalOpen()) {
                    log.info("[WP5649] Tekrar TEKLİF AL sonrası teklif modalı açıldı (hata YOK).");
                    return "MODAL_OPENED";
                }
                Object toast = js.executeScript(
                        "var cards = document.querySelectorAll('vaadin-notification-card');" +
                        "for (var c of cards){ var r=c.getBoundingClientRect(); if(r.width<2) continue;" +
                        "  var t=(c.textContent||'').replace(/\\s+/g,' ').trim();" +
                        "  if (t.length>0) return t; }" +
                        "return null;");
                if (toast != null) {
                    String t = String.valueOf(toast);
                    log.warn("[WP5649] Tekrar TEKLİF AL sonrası toast görüldü: {}", t);
                    return "ERROR_TOAST:" + t;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "NEITHER";
            } catch (Exception ignored) {
            }
        }
        log.warn("[WP5649] {} sn içinde ne modal ne toast görüldü.", timeoutSeconds);
        return "NEITHER";
    }

    /** Fatura listesinde en az bir anlamlı hücre var mı (boş grid = false). */
    public boolean hasInvoiceGridRows() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-grid")));
            List<WebElement> cells = driver.findElements(By.cssSelector("vaadin-grid-cell-content"));
            for (WebElement c : cells) {
                String t = c.getText();
                if (t != null && t.trim().length() > 2) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("hasInvoiceGridRows: {}", e.getMessage());
        }
        return false;
    }
}
