package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Admin — Firma listesi / düzenleme (UAT FL-003).
 */
public class AdminFirmaPage extends BasePageObject {

    private static final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final AdminPanelPage adminPanel;

    public AdminFirmaPage(WebDriver driver) {
        super(driver);
        this.adminPanel = new AdminPanelPage(driver);
    }

    public void navigateToFirmaYonetimi() {
        try {
            adminPanel.clickSidebarItem("Firma Listesi");
        } catch (Exception e) {
            clickNavItemByText("firma");
        }
        waitForVaadinNavigation();
    }

    public boolean isFirmaGridVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-grid")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * İlk firma satırındaki Düzenle’yi hedefler (tüm sayfadaki ilk düzenle değil — yanlış diyalog).
     */
    public void clickFirstDuzenleButton() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-grid")));
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function clickDzenIn(scope) {" +
                    "  var btns = scope.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    var t = (b.textContent || '').toLowerCase().trim();" +
                    "    if (t.includes('düzenle') || t.includes('duzenle') || t === 'edit') {" +
                    "      try { b.scrollIntoView({block:'center', inline:'nearest'}); } catch (e) {}" +
                    "      b.click(); return true;" +
                    "    }" +
                    "  }" +
                    "  return false;" +
                    "}" +
                    "var grid = document.querySelector('vaadin-grid');" +
                    "if (grid && clickDzenIn(grid)) return true;" +
                    "return clickDzenIn(document.body);");
            if (!Boolean.TRUE.equals(clicked)) {
                WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//vaadin-grid//vaadin-button[contains(normalize-space(.),'Düzenle') "
                                + "or contains(normalize-space(.),'düzenle') or contains(normalize-space(.),'zenle')]")));
                btn.click();
            }
        } catch (Exception e) {
            log.warn("Düzenle butonu: {}", e.getMessage());
        }
        acceptVaadinConfirmDialogIfPresent();
    }

    public boolean isFirmaDialogOpen() {
        try {
            Boolean open = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlays = document.querySelectorAll('vaadin-dialog-overlay');" +
                    "for (var o of overlays) {" +
                    "  if (!o.hasAttribute('opened')) continue;" +
                    "  var cs = window.getComputedStyle(o);" +
                    "  if (cs.display === 'none' || cs.visibility === 'hidden') continue;" +
                    "  var r = o.getBoundingClientRect();" +
                    "  if (r.width > 2 && r.height > 2) return true;" +
                    "}" +
                    "return false;");
            return Boolean.TRUE.equals(open);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Firma düzenleme diyaloğunun açılması için kısa süre bekler.
     */
    public boolean waitForFirmaDialogOpen(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (isFirmaDialogOpen()) {
                return true;
            }
            try {
                Thread.sleep(350);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return isFirmaDialogOpen();
            }
        }
        return isFirmaDialogOpen();
    }

    /**
     * Açık firma düzenleme diyaloğundaki Adres alanının mevcut değerini okur.
     */
    public String readFirmaAdres() {
        try {
            Object v = ((JavascriptExecutor) driver).executeScript(
                    "var kw = 'adres';" +
                    "var roots = [document.querySelector('vaadin-dialog-overlay'), document.body];" +
                    "for (var r of roots) {" +
                    "  if (!r) continue;" +
                    "  var fields = r.querySelectorAll('vaadin-text-field, vaadin-text-area');" +
                    "  for (var tf of fields) {" +
                    "    var lab = (tf.getAttribute('label') || '').toLowerCase();" +
                    "    if (!lab.includes(kw)) continue;" +
                    "    var val = tf.value != null ? tf.value : '';" +
                    "    if (val) return val;" +
                    "    var inp = tf.querySelector('input, textarea');" +
                    "    return inp ? (inp.value || '') : '';" +
                    "  }" +
                    "}" +
                    "return null;");
            if (v != null && !"null".equals(String.valueOf(v))) {
                return String.valueOf(v);
            }
            WebElement field = driver.findElement(By.xpath(
                    "//vaadin-text-field[contains(@label,'Adres') or contains(@label,'adres')]//input | " +
                            "//span[normalize-space()='Adres:']/following-sibling::vaadin-text-field//input"));
            String a = field.getAttribute("value");
            return a != null ? a : "";
        } catch (Exception e) {
            log.warn("Firma adres okuma: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Firma düzenleme diyaloğunda boş kalan alanları doldurur; adres alanına {@code yeniAdres} yazar.
     * Zorunlu validasyonlarda takılmamak için metin, sayı, e-posta ve (mümkünse) boş combobox seçimleri işlenir.
     */
    public void fillFirmaEditDialogForSave(String yeniAdres) {
        try {
            Object filled = ((JavascriptExecutor) driver).executeScript(
                    "var adresVal = arguments[0] || 'UAT Adres';" +
                    "function setHostValue(host, text) {" +
                    "  if (!host || host.disabled || host.hasAttribute('disabled')) return false;" +
                    "  try { host.focus(); } catch (e) {}" +
                    "  host.value = text;" +
                    "  var inp = host.querySelector('input, textarea');" +
                    "  if (inp) { inp.value = text; try { inp.dispatchEvent(new Event('input', {bubbles:true})); } catch(e2) {} }" +
                    "  try { host.dispatchEvent(new Event('input', {bubbles:true, composed:true})); } catch (e3) {}" +
                    "  try { host.dispatchEvent(new CustomEvent('value-changed', {bubbles:true, composed:true," +
                    "    detail:{value:text}})); } catch (e4) {}" +
                    "  return true;" +
                    "}" +
                    "function currentText(host) {" +
                    "  var v = host.value; if (v != null && String(v).trim() !== '') return String(v).trim();" +
                    "  var inp = host.querySelector('input, textarea');" +
                    "  if (inp && inp.value) return String(inp.value).trim();" +
                    "  return '';" +
                    "}" +
                    "function pickVal(lab) {" +
                    "  lab = (lab || '').toLowerCase();" +
                    "  if (lab.includes('adres') || lab.includes('address')) return adresVal;" +
                    "  if (lab.includes('e-posta') || lab.includes('eposta') || lab.includes('email') || lab.includes('mail'))" +
                    "    return 'uat.firma@faturalab.com';" +
                    "  if (lab.includes('telefon') || lab.includes('phone') || lab.includes('gsm') || lab.includes('fax') || lab.includes('cep'))" +
                    "    return '02121234567';" +
                    "  if (lab.includes('vergi') && lab.includes('daire')) return 'İstanbul';" +
                    "  if (lab.includes('vergi') || lab.includes('vkn') || lab.includes('kimlik') || lab.includes('tax no'))" +
                    "    return '3960656675';" +
                    "  if (lab.includes('ticari') || lab.includes('unvan') || lab.includes('şirket') || lab.includes('sirket') || lab.includes('firma ad'))" +
                    "    return 'UAT Ticari Unvan A.Ş.';" +
                    "  if (lab.includes('şehir') || lab.includes('sehir') || lab.includes('il ') || lab === 'il')" +
                    "    return 'İstanbul';" +
                    "  if (lab.includes('ilçe') || lab.includes('ilce') || lab.includes('sem')) return 'Kadıköy';" +
                    "  if (lab.includes('posta') && lab.includes('kod')) return '34710';" +
                    "  if (lab.includes('ülke') || lab.includes('ulke') || lab.includes('country')) return 'Türkiye';" +
                    "  if (lab.includes('web') || lab.includes('site') || lab.includes('url')) return 'https://faturalab.com';" +
                    "  if (lab.includes('yetkili') || lab.includes('iletişim') || lab.includes('iletisim') || lab.includes('contact'))" +
                    "    return 'UAT Yetkili';" +
                    "  if (lab.includes('iban')) return 'TR330006100519786457841326';" +
                    "  if (lab.includes('banka')) return 'UAT Bank';" +
                    "  if (lab.includes('not') || lab.includes('açıklama') || lab.includes('aciklama') || lab.includes('description'))" +
                    "    return 'UAT otomasyon doldurma';" +
                    "  if (lab.includes('sermaye') || lab.includes('capital')) return '100000';" +
                    "  if (lab.includes('sicil') || lab.includes('mersis')) return '0123456789012345';" +
                    "  if (lab.includes('tarih') || lab.includes('date')) return '01.01.2025';" +
                    "  return 'UAT';" +
                    "}" +
                    "var overlay = document.querySelector('vaadin-dialog-overlay[opened]');" +
                    "if (!overlay) overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return 0;" +
                    "var count = 0;" +
                    "var tagSel = 'vaadin-text-field, vaadin-text-area, vaadin-email-field, vaadin-integer-field, vaadin-number-field';" +
                    "overlay.querySelectorAll(tagSel).forEach(function(host) {" +
                    "  if (host.disabled || host.hasAttribute('disabled') || host.hasAttribute('readonly')) return;" +
                    "  var lab = host.getAttribute('label') || '';" +
                    "  var cur = currentText(host);" +
                    "  var isAdres = lab.toLowerCase().includes('adres') || lab.toLowerCase().includes('address');" +
                    "  var val = isAdres ? adresVal : (cur.length > 0 ? cur : pickVal(lab));" +
                    "  if (isAdres || cur.length === 0) { if (setHostValue(host, String(val))) count++; }" +
                    "});" +
                    "overlay.querySelectorAll('vaadin-date-picker').forEach(function(dp) {" +
                    "  if (dp.disabled || dp.hasAttribute('disabled')) return;" +
                    "  var curd = (dp.value || '').trim();" +
                    "  if (curd.length > 0) return;" +
                    "  if (setHostValue(dp, '15.06.2025')) count++;" +
                    "});" +
                    "return count;",
                    yeniAdres == null ? "" : yeniAdres);
            log.info("Firma diyaloğu alan doldurma (Vaadin): yaklaşık {} alan işlendi", filled);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            pickFirstOptionsForEmptySelectsInFirmaDialog();
        } catch (Exception e) {
            log.warn("fillFirmaEditDialogForSave: {}", e.getMessage());
        }
        updateFirmaAdres(yeniAdres);
    }

    /**
     * Diyalog içindeki boş vaadin-select / combo-box için ilk görünen seçeneği tıklar.
     */
    private void pickFirstOptionsForEmptySelectsInFirmaDialog() {
        for (int round = 0; round < 4; round++) {
            try {
                List<WebElement> overlays = driver.findElements(By.cssSelector("vaadin-dialog-overlay[opened]"));
                if (overlays.isEmpty()) {
                    return;
                }
                WebElement overlay = overlays.get(0);
                List<WebElement> boxes = overlay.findElements(
                        By.cssSelector("vaadin-select, vaadin-combo-box, vaadin-multi-select-combo-box"));
                boolean anyClicked = false;
                for (WebElement box : boxes) {
                    try {
                        if (!box.isDisplayed() || Boolean.parseBoolean(box.getAttribute("disabled"))) {
                            continue;
                        }
                        String val = box.getAttribute("value");
                        if (val != null && !val.isBlank()) {
                            continue;
                        }
                        box.click();
                        try {
                            Thread.sleep(550);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        List<WebElement> items = driver.findElements(By.cssSelector("vaadin-item"));
                        for (WebElement it : items) {
                            try {
                                if (it.isDisplayed() && !it.getText().isBlank()) {
                                    it.click();
                                    anyClicked = true;
                                    try {
                                        Thread.sleep(350);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                    break;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (!anyClicked) {
                    return;
                }
            } catch (Exception e) {
                log.debug("pickFirstOptionsForEmptySelectsInFirmaDialog: {}", e.getMessage());
                return;
            }
        }
    }

    public void updateFirmaAdres(String adres) {
        try {
            Boolean js = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var kw = 'adres';" +
                    "var roots = [document.querySelector('vaadin-dialog-overlay'), document.body];" +
                    "for (var r of roots) {" +
                    "  if (!r) continue;" +
                    "  var fields = r.querySelectorAll('vaadin-text-field, vaadin-text-area');" +
                    "  for (var tf of fields) {" +
                    "    var lab = (tf.getAttribute('label') || '').toLowerCase();" +
                    "    if (!lab.includes(kw)) continue;" +
                    "    tf.focus();" +
                    "    tf.value = arguments[0];" +
                    "    tf.dispatchEvent(new Event('input', { bubbles: true, composed: true }));" +
                    "    tf.dispatchEvent(new CustomEvent('value-changed', { bubbles: true, composed: true }));" +
                    "    return true;" +
                    "  }" +
                    "}" +
                    "return false;",
                    adres);
            if (Boolean.TRUE.equals(js)) {
                return;
            }
            WebElement field = driver.findElement(By.xpath(
                    "//vaadin-text-field[contains(@label,'Adres') or contains(@label,'adres')]//input | " +
                            "//span[normalize-space()='Adres:']/following-sibling::vaadin-text-field//input"));
            field.clear();
            field.sendKeys(adres);
        } catch (Exception e) {
            log.warn("Firma adres alanı: {}", e.getMessage());
        }
    }

    public void clickKaydetInDialog() {
        try {
            WebElement dlgBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet'] | " +
                            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='KAYDET']")));
            dlgBtn.click();
            Thread.sleep(800);
        } catch (Exception e) {
            clickKaydetJs();
            try {
                Thread.sleep(800);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        acceptVaadinConfirmDialogIfPresent();
    }

    private void clickKaydetJs() {
        ((JavascriptExecutor) driver).executeScript(
                "var o = document.querySelector('vaadin-dialog-overlay');" +
                "if (!o) return;" +
                "var btns = o.querySelectorAll('vaadin-button, button');" +
                "for (var b of btns) {" +
                "  if ((b.textContent || '').toLowerCase().includes('kaydet')) { b.click(); break; }" +
                "}");
    }

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 6).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
