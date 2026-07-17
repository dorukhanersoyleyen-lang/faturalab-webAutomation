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

            // "Teklifler" sekmesi/başlığı varsa tıkla
            js.executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "var root = overlay || document;" +
                    "var els = root.querySelectorAll('vaadin-tab, [role=\"tab\"], vaadin-button, h3, h4, span');" +
                    "for (var el of els) {" +
                    "  var t = (el.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t === 'teklifler' || t === 'gelen teklifler') { el.click(); return true; }" +
                    "}" +
                    "return false;");
            Thread.sleep(700);

            // İlk sıradaki "Kabul Et" butonu
            Boolean accepted = (Boolean) js.executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "var root = overlay || document;" +
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
            log.warn("'Kabul Et' butonu modalda bulunamadı.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("acceptFirstOfferInModal: {}", e.getMessage());
        }
        return false;
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

            // 3. Commit doğrula: ABF uyarısı çıkmadı + Onay dialogu kapandı
            long deadline = System.currentTimeMillis() + 8000L;
            while (System.currentTimeMillis() < deadline) {
                Boolean warn = (Boolean) js.executeScript(
                        "var cards = document.querySelectorAll('vaadin-notification-card');" +
                        "for (var c of cards){ var t=(c.textContent||'').toLowerCase();" +
                        "  if (t.includes('abf') && t.includes('onayla')) return true; }" +
                        "return false;");
                if (Boolean.TRUE.equals(warn)) {
                    log.warn("ABF uyarısı çıktı — commit olmadı (checkbox işaretlenememiş olabilir).");
                    return false;
                }
                Boolean onayOpen = (Boolean) js.executeScript(
                        "var ovs = Array.from(document.querySelectorAll('vaadin-dialog-overlay'))" +
                        "  .filter(function(o){return o.getBoundingClientRect().width>2;});" +
                        "for (var o of ovs){ var t=(o.textContent||'').toLowerCase();" +
                        "  if (t.includes('abf belgesini okudum') || t.includes(\"abf'yi\")) return true; }" +
                        "return false;");
                if (!Boolean.TRUE.equals(onayOpen)) {
                    log.info("Onay dialogu kapandı — kabul commit oldu.");
                    return true;
                }
                Thread.sleep(500);
            }
            log.warn("Onay dialogu kapanmadı — commit doğrulanamadı.");
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
                    // Commit sonrası bordro hâlâ yakalanabiliyorsa güncelle
                    String after = captureBordroNo();
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
