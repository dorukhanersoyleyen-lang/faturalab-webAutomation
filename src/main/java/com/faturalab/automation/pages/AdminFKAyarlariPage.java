package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Admin — Yönetim Paneli → "Limit ve Fiyat Yönetimi" altındaki FK / banka ayarları (UAT FL-012).
 * <p>Burada hedeflenen iş: ilgili finansman kurumunu (combo) seçmek ve <strong>cut-off saati</strong>
 * gibi FK parametrelerini kaydetmektir; boşta bekleme genelde Vaadin combo/grid yüklenirken
 * veya banka listesinde sabit metin (örn. Denizbank) bulunamadığında oluşur.</p>
 */
public class AdminFKAyarlariPage extends BasePageObject {

    private static final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final AdminPanelPage adminPanel;

    public AdminFKAyarlariPage(WebDriver driver) {
        super(driver);
        this.adminPanel = new AdminPanelPage(driver);
    }

    public void navigateToAyarlar() {
        waitForVaadinNavigation();
    }

    public void navigateToFKAyarlari() {
        Exception last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                adminPanel.navigateToYonetimPanelItem("Limit ve Fiyat Yönetimi");
                waitForVaadinNavigation();
                if (isFKAyarlariVisible()) {
                    return;
                }
            } catch (Exception e) {
                last = e;
                log.warn("Limit ve Fiyat Yönetimi denemesi {} başarısız: {}", attempt + 1, e.getMessage());
            }
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            Boolean js = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase();" +
                    "  if (t.includes('limit') && t.includes('fiyat')) { b.click(); return true; }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(js)) {
                waitForVaadinNavigation();
                return;
            }
        } catch (Exception e) {
            last = e;
        }
        log.warn("FK ayarları navigasyonu tamamlanamadı: {}", last != null ? last.getMessage() : "");
        clickNavItemByText("limit");
        waitForVaadinNavigation();
    }

    public void selectBanka(String bankaAdi) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var combos = document.querySelectorAll('vaadin-combo-box, vaadin-select');" +
                    "if (combos.length) combos[0].click();");
            Thread.sleep(450);
            Boolean picked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var kw = arguments[0].toLowerCase();" +
                    "var items = document.querySelectorAll('vaadin-item, vaadin-combo-box-item, [role=\"option\"]');" +
                    "for (var el of items) {" +
                    "  if ((el.textContent || '').toLowerCase().includes(kw)) { el.click(); return true; }" +
                    "}" +
                    "return false;",
                    bankaAdi);
            if (!Boolean.TRUE.equals(picked)) {
                Boolean any = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "var items = document.querySelectorAll('vaadin-item, [role=\"option\"]');" +
                        "for (var el of items) {" +
                        "  var t = (el.textContent || '').trim();" +
                        "  if (t.length > 1 && t.length < 80) { el.click(); return true; }" +
                        "}" +
                        "return false;");
                if (Boolean.TRUE.equals(any)) {
                    log.info("Banka listesinde '{}' yok — ilk uygun seçenek seçildi.", bankaAdi);
                } else {
                    log.warn("Banka listesinde '{}' bulunamadı — adım atlanıyor.", bankaAdi);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Banka seçimi: {}", e.getMessage());
        }
    }

    public void setCutoffSaat(String saat) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
        try {
            WebElement input = shortWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//vaadin-text-field[contains(@label,'Cut') or contains(@label,'cut') "
                            + "or contains(@label,'Saat') or contains(@label,'saat') "
                            + "or contains(@label,'Kesim') or contains(@label,'kesim') "
                            + "or contains(@label,'Bitiş') or contains(@label,'bitiş') "
                            + "or contains(@label,'Bitis') or contains(@label,'bitis')]//input | "
                            + "//vaadin-time-picker//input | "
                            + "//vaadin-integer-field//input | "
                            + "//vaadin-number-field//input")));
            input.clear();
            input.sendKeys(saat);
        } catch (Exception e) {
            log.warn("Cut-off saati alanı (XPath) bulunamadı, Vaadin host JS deneniyor: {}", e.getMessage());
            try {
                Boolean js = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "var v = arguments[0];" +
                        "function labOf(host) {" +
                        "  return ((host.getAttribute('label') || '') + ' ' + "
                        + "(host.getAttribute('placeholder') || '')).toLowerCase();" +
                        "}" +
                        "function walk(node, depth) {" +
                        "  if (!node || depth > 26) return false;" +
                        "  if (node.shadowRoot && walk(node.shadowRoot, depth + 1)) return true;" +
                        "  var tag = (node.tagName || '').toLowerCase();" +
                        "  if (tag === 'vaadin-text-field' || tag === 'vaadin-integer-field' || "
                        + "tag === 'vaadin-number-field' || tag === 'vaadin-time-picker') {" +
                        "    var lab = labOf(node);" +
                        "    if (lab.includes('cut') || lab.includes('kesim') || lab.includes('saat') || "
                        + "lab.includes('bitiş') || lab.includes('bitis') || lab.includes('time')) {" +
                        "      try { node.focus(); } catch (e1) {}" +
                        "      node.value = v;" +
                        "      var inp = node.querySelector('input');" +
                        "      if (inp) { inp.value = v; try { inp.dispatchEvent(new Event('input',{bubbles:true})); } catch (e2) {} }" +
                        "      try { node.dispatchEvent(new Event('input', {bubbles:true, composed:true})); } catch (e3) {}" +
                        "      try { node.dispatchEvent(new CustomEvent('value-changed', {bubbles:true, composed:true, detail:{value:v}})); } catch (e4) {}" +
                        "      return true;" +
                        "    }" +
                        "  }" +
                        "  var ch = node.children;" +
                        "  if (ch) for (var i = 0; i < ch.length; i++) if (walk(ch[i], depth + 1)) return true;" +
                        "  return false;" +
                        "}" +
                        "return walk(document.body, 0);",
                        saat);
                if (!Boolean.TRUE.equals(js)) {
                    List<WebElement> nums = driver.findElements(By.cssSelector(
                            "vaadin-integer-field input, vaadin-number-field input, vaadin-text-field input, "
                                    + "vaadin-time-picker input"));
                    if (!nums.isEmpty()) {
                        nums.get(0).clear();
                        nums.get(0).sendKeys(saat);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void clickKaydet() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').trim().toLowerCase();" +
                    "  if (t === 'kaydet' || t === 'kaydet ' || t === 'save') { b.click(); return true; }" +
                    "}" +
                    "return false;");
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Kaydet: {}", e.getMessage());
        }
        acceptVaadinConfirmDialogIfPresent();
    }

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 6).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isFKAyarlariVisible() {
        return driver.findElements(By.cssSelector("vaadin-grid, vaadin-form-layout, form")).size() > 0;
    }
}
