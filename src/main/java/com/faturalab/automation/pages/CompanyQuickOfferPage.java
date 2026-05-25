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
