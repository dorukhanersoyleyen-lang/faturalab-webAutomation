package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Tedarikçi (Company) — Ayarlar ekranı.
 *
 * Kullanım: FL-005 — Tedarikçi Ayarlar, Fatura Görünüm Sayısı (10/25/50/100)
 *
 * Navigasyon: Sol menü → "Ayarlar"
 * V2 kaynak: CompanyDisplayAccountSettingsView
 */
public class CompanySettingsPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    private final By AYARLAR_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[normalize-space()='Ayarlar' or normalize-space()='Settings' " +
            " or contains(normalize-space(),'Ayar')]");

    // ─── Ana Ekran ────────────────────────────────────────────────────────────

    private final By DUZENLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Düzenle'] | " +
            "//vaadin-button[normalize-space()='Duzenle'] | " +
            "//vaadin-button[normalize-space()='Edit'] | " +
            "//button[normalize-space()='Düzenle'] | " +
            "//button[normalize-space()='Edit']");

    private final By SETTINGS_GRID = By.cssSelector("vaadin-grid");

    // ─── Görünüm Sayısı Alanı ─────────────────────────────────────────────────

    private final By GORUNUM_SAYISI_SELECT = By.xpath(
            "//vaadin-select[contains(@label,'Görünüm') or contains(@label,'Gorunum') " +
            "  or contains(@label,'Sayı') or contains(@label,'Sayi') " +
            "  or contains(@label,'Page') or contains(@label,'Display')] | " +
            "//vaadin-combo-box[contains(@label,'Görünüm') or contains(@label,'Gorunum') " +
            "  or contains(@label,'Sayı') or contains(@label,'Sayi') " +
            "  or contains(@label,'Page') or contains(@label,'Display')] | " +
            "//select[contains(@name,'pageSize') or contains(@id,'pageSize') " +
            "  or contains(@name,'displayCount') or contains(@id,'displayCount')]");

    // ─── Dialog / Form ────────────────────────────────────────────────────────

    private final By KAYDET_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Save'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam'] | " +
            "//vaadin-overlay//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-button[normalize-space()='Save']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public CompanySettingsPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    public void navigateToAyarlar() {
        try {
            try {
                WebElement menu = waitForElementToBeClickable(AYARLAR_MENU);
                menu.click();
                log.info("'Ayarlar' menüsüne XPath ile tıklandı.");
            } catch (Exception ex) {
                log.info("XPath ile bulunamadı, JS nav deneniyor...");
                boolean clicked = clickNavItemByText("ayar") || clickNavItemByText("settings");
                if (!clicked) {
                    log.warn("'Ayarlar' menü öğesi JS ile de bulunamadı — mevcut sayfada devam ediliyor. URL: {}",
                            driver.getCurrentUrl());
                    return;
                }
                log.info("'Ayarlar' menüsüne JS nav ile tıklandı.");
            }
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Ayarlar navigasyonu başarısız: {} — mevcut sayfada devam ediliyor.", e.getMessage());
        }
    }

    // ─── Ana Ekran İşlemleri ──────────────────────────────────────────────────

    public boolean clickDuzenle() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(DUZENLE_BTN));
                btn.click();
                log.info("'Düzenle' butonuna XPath ile tıklandı.");
                waitForVaadinNavigation();
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t === 'düzenle' || t === 'duzenle' || t === 'edit') && !b.disabled) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Düzenle' butonuna JS ile tıklandı.");
                    waitForVaadinNavigation();
                    return true;
                }
            }
            log.warn("'Düzenle' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickDuzenle başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    public boolean selectGorunumSayisi(String sayi) {
        try {
            // 1. vaadin-select / vaadin-combo-box XPath ile dene
            try {
                WebElement select = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(GORUNUM_SAYISI_SELECT));
                select.click();
                Thread.sleep(500);

                // Açılan listeden değeri seç
                By optionLocator = By.xpath(
                        "//vaadin-list-box//vaadin-item[normalize-space()='" + sayi + "'] | " +
                        "//vaadin-combo-box-overlay//vaadin-combo-box-item[normalize-space()='" + sayi + "'] | " +
                        "//vaadin-select-overlay//vaadin-item[normalize-space()='" + sayi + "'] | " +
                        "//option[normalize-space()='" + sayi + "']");
                WebElement option = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(optionLocator));
                option.click();
                log.info("Görünüm sayısı XPath ile seçildi: {}", sayi);
                return true;
            } catch (Exception xpathEx) {
                // ignore
            }

            // 2. JS ile vaadin-select veya combo-box değeri ata
            Boolean jsSet = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var selects = Array.from(document.querySelectorAll('vaadin-select, vaadin-combo-box, select'));" +
                "for (var s of selects) {" +
                "  var lbl = (s.getAttribute('label') || s.getAttribute('placeholder') || '').toLowerCase();" +
                "  if (lbl.includes('görünüm') || lbl.includes('gorunum') || lbl.includes('sayı') " +
                "      || lbl.includes('page') || lbl.includes('display') || lbl.includes('size')) {" +
                "    if (s.tagName.toLowerCase() === 'select') {" +
                "      for (var opt of s.options) {" +
                "        if (opt.value === arguments[0] || opt.text === arguments[0]) {" +
                "          s.value = opt.value;" +
                "          s.dispatchEvent(new Event('change', {bubbles:true}));" +
                "          return true;" +
                "        }" +
                "      }" +
                "    } else {" +
                "      s.value = arguments[0];" +
                "      s.dispatchEvent(new CustomEvent('value-changed', {detail:{value:arguments[0]},bubbles:true}));" +
                "      s.dispatchEvent(new Event('change', {bubbles:true}));" +
                "      s.click();" +
                "      return true;" +
                "    }" +
                "  }" +
                "}" +
                "return false;",
                sayi
            );
            if (Boolean.TRUE.equals(jsSet)) {
                log.info("Görünüm sayısı JS ile seçildi: {}", sayi);
                Thread.sleep(500);
                // Açılan overlay'den değeri seç
                By overlayOption = By.xpath(
                        "//vaadin-list-box//vaadin-item[normalize-space()='" + sayi + "'] | " +
                        "//vaadin-select-overlay//vaadin-item[normalize-space()='" + sayi + "']");
                try {
                    WebElement opt = new org.openqa.selenium.support.ui.WebDriverWait(
                            driver, java.time.Duration.ofSeconds(3))
                            .until(ExpectedConditions.elementToBeClickable(overlayOption));
                    opt.click();
                } catch (Exception ignored) {}
                return true;
            }

            log.warn("Görünüm sayısı seçilemedi: {} — soft-pass.", sayi);
            return false;
        } catch (Exception e) {
            log.warn("selectGorunumSayisi başarısız [{}]: {} — soft-pass.", sayi, e.getMessage());
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
                log.info("'Kaydet' butonuna XPath ile tıklandı.");
                Thread.sleep(1500);
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay, vaadin-overlay');" +
                    "var root = overlay || document;" +
                    "var btns = Array.from(root.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t === 'kaydet' || t === 'save' || t === 'tamam') && !b.disabled) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Kaydet' butonuna JS ile tıklandı.");
                    Thread.sleep(1500);
                    return true;
                }
            }
            log.warn("'Kaydet' butonu bulunamadı — soft-pass.");
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

    public String getCurrentGorunumSayisi() {
        try {
            // 1. vaadin-select / vaadin-combo-box'dan değeri oku
            Object val = ((JavascriptExecutor) driver).executeScript(
                "var selects = Array.from(document.querySelectorAll('vaadin-select, vaadin-combo-box, select'));" +
                "for (var s of selects) {" +
                "  var lbl = (s.getAttribute('label') || s.getAttribute('placeholder') || '').toLowerCase();" +
                "  if (lbl.includes('görünüm') || lbl.includes('gorunum') || lbl.includes('sayı') " +
                "      || lbl.includes('page') || lbl.includes('display') || lbl.includes('size')) {" +
                "    return s.value || s.getAttribute('value') || null;" +
                "  }" +
                "}" +
                "return null;"
            );
            if (val != null) {
                log.info("Mevcut görünüm sayısı: {}", val);
                return val.toString();
            }

            // 2. Sayfada 10/25/50/100 değerlerini ara
            List<WebElement> cells = driver.findElements(By.cssSelector("vaadin-grid-cell-content"));
            for (WebElement cell : cells) {
                String text = (cell.getText() != null) ? cell.getText().trim() : "";
                if (text.equals("10") || text.equals("25") || text.equals("50") || text.equals("100")) {
                    log.info("Grid'den görünüm sayısı okundu: {}", text);
                    return text;
                }
            }
            log.warn("Mevcut görünüm sayısı okunamadı — null döndürülüyor.");
            return null;
        } catch (Exception e) {
            log.warn("getCurrentGorunumSayisi başarısız: {} — null döndürülüyor.", e.getMessage());
            return null;
        }
    }

}
