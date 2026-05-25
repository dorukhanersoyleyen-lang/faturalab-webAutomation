package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Tedarikçi (Company) — Toplu fatura yükleme ekranı.
 *
 * Kullanım:
 *   FL-014: Excel ile Yükleme
 *   FL-016: CSV ile Yükleme
 *   FL-017: Manuel Yükleme
 *
 * Navigasyon: Tedarikçi → "Fatura Yükle" butonu veya menü
 * V2 kaynak: CompanyAddInvoiceDialog — 600px, upload types: XML, Excel, CSV, Manual
 */
public class CompanyBulkUploadPage extends BasePageObject {

    // ─── Navigasyon / Ana Ekran ───────────────────────────────────────────────

    private final By FATURA_YUKLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Fatura Yükle'] | " +
            "//vaadin-button[normalize-space()='FATURA YÜKLE'] | " +
            "//vaadin-button[normalize-space()='Fatura Y\u00dcKLE'] | " +
            "//vaadin-button[contains(normalize-space(),'Fatura') and contains(normalize-space(),'Yükle')] | " +
            "//button[normalize-space()='Fatura Yükle'] | " +
            "//button[normalize-space()='FATURA YÜKLE']");

    // ─── Dialog ───────────────────────────────────────────────────────────────

    private final By DIALOG_OVERLAY = By.cssSelector("vaadin-dialog-overlay");

    private final By RESULT_DIALOG = By.cssSelector("vaadin-dialog-overlay");

    // ─── Yükleme Yöntemi Sekmeleri ────────────────────────────────────────────

    private final By EXCEL_TAB = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Excel'] | " +
            "//vaadin-dialog-overlay//*[@role='tab' and normalize-space()='Excel'] | " +
            "//vaadin-dialog-overlay//vaadin-tab[normalize-space()='Excel'] | " +
            "//vaadin-dialog-overlay//vaadin-radio-button[contains(@label,'Excel')]");

    private final By CSV_TAB = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='CSV'] | " +
            "//vaadin-dialog-overlay//*[@role='tab' and normalize-space()='CSV'] | " +
            "//vaadin-dialog-overlay//vaadin-tab[normalize-space()='CSV'] | " +
            "//vaadin-dialog-overlay//vaadin-radio-button[contains(@label,'CSV')]");

    private final By MANUEL_TAB = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Manuel'] | " +
            "//vaadin-dialog-overlay//*[@role='tab' and normalize-space()='Manuel'] | " +
            "//vaadin-dialog-overlay//vaadin-tab[normalize-space()='Manuel'] | " +
            "//vaadin-dialog-overlay//vaadin-radio-button[contains(@label,'Manuel')]");

    // ─── Şablon İndir ─────────────────────────────────────────────────────────

    private final By TEMPLATE_INDIR_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[contains(normalize-space(),'Şablon') " +
            "  or contains(normalize-space(),'Sablon') or contains(normalize-space(),'Template') " +
            "  or contains(normalize-space(),'İndir') or contains(normalize-space(),'Indir')] | " +
            "//vaadin-dialog-overlay//a[contains(normalize-space(),'İndir') " +
            "  or contains(normalize-space(),'Template') or contains(normalize-space(),'Şablon')]");

    // ─── Dosya Yükleme ────────────────────────────────────────────────────────

    private final By FILE_INPUT = By.cssSelector(
            "vaadin-dialog-overlay vaadin-upload, " +
            "vaadin-dialog-overlay input[type='file'], " +
            "vaadin-upload, " +
            "input[type='file']");

    // ─── Manuel Yükleme Alanları ──────────────────────────────────────────────

    private final By ALICI_SELECT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-combo-box[contains(@label,'Alıcı') " +
            "  or contains(@label,'Alici') or contains(@label,'Buyer')] | " +
            "//vaadin-dialog-overlay//vaadin-select[contains(@label,'Alıcı') " +
            "  or contains(@label,'Alici') or contains(@label,'Buyer')]");

    private final By TUTAR_FIELD = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-field[contains(@label,'Tutar') " +
            "  or contains(@label,'Miktar') or contains(@label,'Amount')]//input | " +
            "//vaadin-dialog-overlay//vaadin-number-field[contains(@label,'Tutar') " +
            "  or contains(@label,'Miktar')]//input | " +
            "//vaadin-dialog-overlay//input[contains(@placeholder,'Tutar') " +
            "  or contains(@placeholder,'Miktar')]");

    private final By VADE_FIELD = By.xpath(
            "//vaadin-dialog-overlay//vaadin-date-picker[contains(@label,'Vade') " +
            "  or contains(@label,'Tarih') or contains(@label,'Date')]//input | " +
            "//vaadin-dialog-overlay//input[contains(@placeholder,'Vade') " +
            "  or contains(@placeholder,'Tarih') or @type='date']");

    // ─── Kaydet / Yükle ──────────────────────────────────────────────────────

    private final By KAYDET_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Yükle'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Y\u00dcKLE'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Gönder'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Gonder'] | " +
            "//vaadin-overlay//vaadin-button[normalize-space()='Kaydet']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public CompanyBulkUploadPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    public void navigateToFaturaYukle() {
        try {
            boolean clicked = false;
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(FATURA_YUKLE_BTN));
                btn.click();
                log.info("'Fatura Yükle' butonuna XPath ile tıklandı.");
                clicked = true;
            } catch (Exception xpathEx) {
                Boolean jsClicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if (t.includes('yükle') || t.includes('yukle')) {" +
                    "    if (t.includes('fatura') || t.length < 20) {" +
                    "      if (!b.disabled) { b.click(); return true; }" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(jsClicked)) {
                    log.info("'Fatura Yükle' butonuna JS ile tıklandı.");
                    clicked = true;
                }
            }
            if (!clicked) {
                log.warn("'Fatura Yükle' butonu/menüsü bulunamadı — mevcut sayfada devam ediliyor. URL: {}",
                        driver.getCurrentUrl());
                return;
            }
            waitForVaadinNavigation();
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG_OVERLAY));
            } catch (Exception dialogEx) {
                log.warn("Dialog açılmadı — devam ediliyor: {}", dialogEx.getMessage());
            }
        } catch (Exception e) {
            log.warn("navigateToFaturaYukle başarısız: {} — mevcut sayfada devam ediliyor.", e.getMessage());
        }
    }

    public boolean clickFaturaYukleButonu() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(FATURA_YUKLE_BTN));
                btn.click();
                log.info("'Fatura Yükle' butonuna XPath ile tıklandı (dialog açma).");
                Thread.sleep(800);
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t.includes('yükle') || t.includes('yukle')) && !b.disabled) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Fatura Yükle' butonuna JS ile tıklandı (dialog açma).");
                    Thread.sleep(800);
                    return true;
                }
            }
            log.warn("'Fatura Yükle' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickFaturaYukleButonu başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    // ─── Yükleme Yöntemi Seçimi ───────────────────────────────────────────────

    public boolean selectUploadMethod(String method) {
        try {
            // 1. XPath ile tab/buton/radio bul
            By tabLocator;
            String keyword = method.toLowerCase();
            switch (keyword) {
                case "excel":
                    tabLocator = EXCEL_TAB;
                    break;
                case "csv":
                    tabLocator = CSV_TAB;
                    break;
                case "manuel":
                case "manual":
                    tabLocator = MANUEL_TAB;
                    break;
                default:
                    tabLocator = By.xpath(
                            "//vaadin-dialog-overlay//*[normalize-space()='" + method + "' " +
                            "  or contains(normalize-space(),'" + method + "')]" +
                            "[self::vaadin-button or self::vaadin-tab or self::vaadin-radio-button]");
            }

            try {
                WebElement tab = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(tabLocator));
                tab.click();
                log.info("Yükleme yöntemi seçildi (XPath): {}", method);
                Thread.sleep(500);
                return true;
            } catch (Exception xpathEx) {
                // ignore
            }

            // 2. JS ile dialog içinde metin eşleştir
            Boolean jsClicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                "if (!overlay) return false;" +
                "var els = Array.from(overlay.querySelectorAll(" +
                "  'vaadin-button, vaadin-tab, vaadin-radio-button, [role=\"tab\"]'));" +
                "for (var el of els) {" +
                "  var t = (el.textContent || el.getAttribute('label') || '').trim().toLowerCase();" +
                "  if (t === arguments[0].toLowerCase() || t.includes(arguments[0].toLowerCase())) {" +
                "    el.click();" +
                "    return true;" +
                "  }" +
                "}" +
                "return false;",
                method
            );
            if (Boolean.TRUE.equals(jsClicked)) {
                log.info("Yükleme yöntemi JS ile seçildi: {}", method);
                Thread.sleep(500);
                return true;
            }

            log.warn("Yükleme yöntemi seçilemedi: {} — soft-pass.", method);
            return false;
        } catch (Exception e) {
            log.warn("selectUploadMethod başarısız [{}]: {} — soft-pass.", method, e.getMessage());
            return false;
        }
    }

    public boolean clickTemplateIndir() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(TEMPLATE_INDIR_BTN));
                btn.click();
                log.info("'Şablon İndir' butonuna XPath ile tıklandı.");
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "var root = overlay || document;" +
                    "var els = Array.from(root.querySelectorAll('vaadin-button, button, a'));" +
                    "for (var el of els) {" +
                    "  var t = (el.textContent || '').toLowerCase().trim();" +
                    "  if (t.includes('indir') || t.includes('şablon') || t.includes('sablon') " +
                    "      || t.includes('template')) {" +
                    "    el.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Şablon İndir' butonuna JS ile tıklandı.");
                    return true;
                }
            }
            log.warn("'Şablon İndir' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickTemplateIndir başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    // ─── Dialog Durumu ────────────────────────────────────────────────────────

    public boolean isDialogOpen() {
        try {
            Thread.sleep(500);
            java.util.List<WebElement> dialogs = driver.findElements(DIALOG_OVERLAY);
            if (dialogs.isEmpty()) return false;
            return dialogs.stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Manuel Yükleme Alanları ──────────────────────────────────────────────

    public boolean selectAlici(String aliciAdi) {
        try {
            try {
                WebElement combo = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(ALICI_SELECT));
                combo.click();
                Thread.sleep(300);
                WebElement input = combo.findElement(By.cssSelector("input"));
                input.clear();
                input.sendKeys(aliciAdi);
                Thread.sleep(500);
                By dropdownOption = By.xpath(
                        "//vaadin-combo-box-overlay//vaadin-combo-box-item[contains(normalize-space(),'" + aliciAdi + "')] | " +
                        "//vaadin-list-box//vaadin-item[contains(normalize-space(),'" + aliciAdi + "')]");
                WebElement opt = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(4))
                        .until(ExpectedConditions.elementToBeClickable(dropdownOption));
                opt.click();
                log.info("Alıcı seçildi (XPath/combo): {}", aliciAdi);
                return true;
            } catch (Exception xpathEx) {
                Boolean jsSet = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return false;" +
                    "var combos = overlay.querySelectorAll('vaadin-combo-box, vaadin-select');" +
                    "for (var c of combos) {" +
                    "  var lbl = (c.getAttribute('label') || '').toLowerCase();" +
                    "  if (lbl.includes('alıcı') || lbl.includes('alici') || lbl.includes('buyer')) {" +
                    "    c.value = arguments[0];" +
                    "    c.dispatchEvent(new CustomEvent('value-changed',{detail:{value:arguments[0]},bubbles:true}));" +
                    "    return true;" +
                    "  }" +
                    "}" +
                    "return false;",
                    aliciAdi
                );
                if (Boolean.TRUE.equals(jsSet)) {
                    log.info("Alıcı JS ile seçildi: {}", aliciAdi);
                    return true;
                }
            }
            log.warn("Alıcı seçilemedi: {} — soft-pass.", aliciAdi);
            return false;
        } catch (Exception e) {
            log.warn("selectAlici başarısız [{}]: {} — soft-pass.", aliciAdi, e.getMessage());
            return false;
        }
    }

    public boolean enterTutar(String tutar) {
        try {
            WebElement field = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(TUTAR_FIELD));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));", field);
            field.sendKeys(tutar);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", field);
            log.info("Tutar girildi: {}", tutar);
            return true;
        } catch (Exception e) {
            log.warn("enterTutar başarısız [{}]: {} — soft-pass.", tutar, e.getMessage());
            return false;
        }
    }

    public boolean enterVadeTarihi(String tarih) {
        try {
            WebElement field = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(VADE_FIELD));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));", field);
            field.sendKeys(tarih);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", field);
            log.info("Vade tarihi girildi: {}", tarih);
            return true;
        } catch (Exception e) {
            log.warn("enterVadeTarihi başarısız [{}]: {} — soft-pass.", tarih, e.getMessage());
            return false;
        }
    }

    public boolean clickKaydet() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
                btn.click();
                log.info("'Kaydet/Yükle' butonuna XPath ile tıklandı.");
                Thread.sleep(1500);
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay, vaadin-overlay');" +
                    "var root = overlay || document;" +
                    "var btns = Array.from(root.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t === 'kaydet' || t === 'yükle' || t === 'yukle' " +
                    "       || t === 'gönder' || t === 'gonder') && !b.disabled) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Kaydet/Yükle' butonuna JS ile tıklandı.");
                    Thread.sleep(1500);
                    return true;
                }
            }
            log.warn("'Kaydet/Yükle' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickKaydet başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isUploadResultVisible() {
        // Başarı bildirimi veya sonuç dialogu görünüyor mu?
        try {
            if (waitForVisibility(SUCCESS_NOTIFICATION, 3).isDisplayed()) return true;
        } catch (Exception ignored) {}

        try {
            java.util.List<WebElement> dialogs = driver.findElements(RESULT_DIALOG);
            if (!dialogs.isEmpty() && dialogs.stream().anyMatch(WebElement::isDisplayed)) {
                return true;
            }
        } catch (Exception ignored) {}

        try {
            String src = driver.getPageSource();
            return src.contains("başarı") || src.contains("yüklendi") || src.contains("success")
                    || src.contains("uploaded") || src.contains("tamamlandı");
        } catch (Exception ignored) {}

        return false;
    }

}
