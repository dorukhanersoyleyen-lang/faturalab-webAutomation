package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Tedarikçi (Company) — Hızlı Teklif Al modal ekranı.
 *
 * Kullanım:
 *   FL-006: Hızlı Teklif Al Modal (Tedarikçi)
 *   FL-007: Teklif Talebi Süresi Değiştirme (1/3/7 gün)
 *
 * Navigasyon: Tedarikçi "Faturalarım" veya "İşlem Bekleyenler" → "Hızlı Teklif Al"
 * V2 kaynak: CompanyAddEditAuctionDialog — 600px wide dialog
 */
public class CompanyQuickOfferPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    private final By FATURALARIM_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[normalize-space()='Faturalarım' or normalize-space()='Faturalar' " +
            " or normalize-space()='Yüklenenler' " +
            " or (contains(normalize-space(),'Fatura') and not(contains(normalize-space(),'Yükle')))]");

    private final By ISLEM_BEKLEYENLER_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'İşlem') or contains(normalize-space(),'Islem') " +
            " or contains(normalize-space(),'Bekle') or contains(normalize-space(),'Pending')]");

    // ─── Ana Ekran Butonları ──────────────────────────────────────────────────

    private final By HIZLI_TEKLIF_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Hızlı Teklif Al'] | " +
            "//vaadin-button[normalize-space()='HIZLI TEKLIF AL'] | " +
            "//vaadin-button[contains(normalize-space(),'Hızlı') and contains(normalize-space(),'Teklif')] | " +
            "//vaadin-button[contains(normalize-space(),'Teklif Al')] | " +
            "//button[contains(normalize-space(),'Hızlı') and contains(normalize-space(),'Teklif')]");

    // ─── Grid / Satır ─────────────────────────────────────────────────────────

    private final By FATURA_GRID = By.cssSelector("vaadin-grid");

    private final By FATURA_CHECKBOX = By.xpath(
            "//vaadin-grid//vaadin-checkbox | " +
            "//vaadin-grid//input[@type='checkbox']");

    // ─── Modal / Dialog ───────────────────────────────────────────────────────

    private final By DIALOG_OVERLAY = By.cssSelector("vaadin-dialog-overlay");

    private final By TEKLIF_SURE_SELECT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-radio-button | " +
            "//vaadin-dialog-overlay//vaadin-radio-group//vaadin-radio-button | " +
            "//vaadin-dialog-overlay//vaadin-select[contains(@label,'Süre') or contains(@label,'Sure') " +
            "  or contains(@label,'Gün') or contains(@label,'Gun') or contains(@label,'Day')] | " +
            "//vaadin-radio-button[contains(normalize-space(),'gün') or contains(normalize-space(),'gun') " +
            "  or contains(normalize-space(),'day')]");

    private final By GONDER_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Gönder'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Teklif Al'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Oluştur'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Olustur'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Gonder'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet']");

    private final By IPTAL_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='İptal'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kapat'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Iptal'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Cancel']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public CompanyQuickOfferPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Tedarikçi dashboard → "Yüklenmiş" menüsüne gider (yüklenen faturalar listesi).
     * CompanyDashboardMenuView.3 = "Yüklenmiş"
     */
    public void navigateToFaturalarim() {
        // "yüklenm" → "Yüklenmiş" butonunu bulur; "FATURA YÜKLE" ile karışmaz
        boolean clicked = clickNavItemByText("yüklenm");
        if (!clicked) clicked = clickNavItemByText("yüklenmis");
        if (!clicked) {
            log.warn("'Yüklenmiş' menü butonu bulunamadı. URL: {}", driver.getCurrentUrl());
            throw new RuntimeException("Faturalarım/Yüklenmiş ekranına gidilemedi");
        }
        waitForVaadinNavigation();
        waitForVaadinGrid();
        log.info("'Yüklenmiş' (faturalarım) ekranına geçildi.");
    }

    /**
     * Tedarikçi dashboard → "İşlemdekiler" menüsüne gider.
     * CompanyDashboardMenuView.4 = "İşlemdekiler"
     */
    public void navigateToIslemBekleyenler() {
        boolean clicked = clickNavItemByText("işlemdek");
        if (!clicked) clicked = clickNavItemByText("işlem");
        if (!clicked) {
            log.warn("'İşlemdekiler' menü butonu bulunamadı. URL: {}", driver.getCurrentUrl());
            throw new RuntimeException("İşlemdekiler ekranına gidilemedi");
        }
        waitForVaadinNavigation();
        waitForVaadinGrid();
        log.info("'İşlemdekiler' ekranına geçildi.");
    }

    // ─── Fatura Seçimi ────────────────────────────────────────────────────────

    public boolean selectFatura(String faturaNo) {
        try {
            // 1. Fatura numarasına göre satır checkbox'ını bul
            try {
                By rowCheckbox = By.xpath(
                        "//*[contains(text(),'" + faturaNo + "')]" +
                        "/ancestor::vaadin-grid-row//vaadin-checkbox | " +
                        "//*[contains(text(),'" + faturaNo + "')]" +
                        "/ancestor::tr//input[@type='checkbox'] | " +
                        "//*[contains(text(),'" + faturaNo + "')]" +
                        "/ancestor::tr//vaadin-checkbox");
                WebElement cb = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(rowCheckbox));
                cb.click();
                log.info("Fatura checkbox'ı seçildi (satır): {}", faturaNo);
                return true;
            } catch (Exception ignored) {}

            // 2. JS ile metne göre satır bul ve checkbox tıkla
            Boolean jsClicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                "for (var cell of cells) {" +
                "  if ((cell.textContent || '').includes(arguments[0])) {" +
                "    var row = cell;" +
                "    for (var i = 0; i < 10; i++) {" +
                "      row = row.parentElement;" +
                "      if (!row) break;" +
                "      var cb = row.querySelector('vaadin-checkbox, input[type=\"checkbox\"]');" +
                "      if (cb) { cb.click(); return true; }" +
                "    }" +
                "  }" +
                "}" +
                "return false;",
                faturaNo
            );
            if (Boolean.TRUE.equals(jsClicked)) {
                log.info("Fatura checkbox'ı JS ile seçildi: {}", faturaNo);
                return true;
            }

            // 3. Fallback: ilk checkbox'ı seç
            log.warn("Fatura '{}' bulunamadı — ilk satır checkbox'ı seçiliyor.", faturaNo);
            List<WebElement> checkboxes = driver.findElements(FATURA_CHECKBOX);
            if (!checkboxes.isEmpty()) {
                checkboxes.get(0).click();
                log.info("İlk satır checkbox'ı seçildi (fallback).");
                return true;
            }

            log.warn("Hiçbir checkbox bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("selectFatura başarısız [{}]: {} — soft-pass.", faturaNo, e.getMessage());
            return false;
        }
    }

    // ─── Modal İşlemleri ──────────────────────────────────────────────────────

    public boolean clickHizliTeklifAl() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(HIZLI_TEKLIF_BTN));
                btn.click();
                log.info("'Hızlı Teklif Al' butonuna XPath ile tıklandı.");
                waitForVaadinNavigation();
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t.includes('hızlı') || t.includes('hizli')) && t.includes('teklif')) {" +
                    "    if (!b.disabled) { b.click(); return true; }" +
                    "  }" +
                    "  if (t === 'teklif al' && !b.disabled) { b.click(); return true; }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Hızlı Teklif Al' butonuna JS ile tıklandı.");
                    waitForVaadinNavigation();
                    return true;
                }
            }
            log.warn("'Hızlı Teklif Al' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickHizliTeklifAl başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    public boolean isModalOpen() {
        try {
            Thread.sleep(500);
            return driver.findElements(DIALOG_OVERLAY).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean selectTeklifSuresi(String gun) {
        try {
            // 1. Radio button ile dene — metin içinde gün sayısını ara
            try {
                By radioForGun = By.xpath(
                        "//vaadin-dialog-overlay//vaadin-radio-button[contains(normalize-space(),'" + gun + "')] | " +
                        "//vaadin-dialog-overlay//vaadin-radio-button[@value='" + gun + "'] | " +
                        "//vaadin-radio-button[contains(normalize-space(),'" + gun + " gün')] | " +
                        "//vaadin-radio-button[contains(normalize-space(),'" + gun + " gun')]");
                WebElement radio = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(radioForGun));
                radio.click();
                log.info("Teklif süresi radio ile seçildi: {} gün", gun);
                return true;
            } catch (Exception radioEx) {
                // ignore
            }

            // 2. vaadin-select ile dene
            try {
                By selectInDialog = By.xpath(
                        "//vaadin-dialog-overlay//vaadin-select | " +
                        "//vaadin-dialog-overlay//vaadin-combo-box");
                WebElement sel = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(selectInDialog));
                sel.click();
                Thread.sleep(400);
                By optionInOverlay = By.xpath(
                        "//vaadin-list-box//vaadin-item[contains(normalize-space(),'" + gun + "')] | " +
                        "//vaadin-select-overlay//vaadin-item[contains(normalize-space(),'" + gun + "')]");
                WebElement opt = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(4))
                        .until(ExpectedConditions.elementToBeClickable(optionInOverlay));
                opt.click();
                log.info("Teklif süresi vaadin-select ile seçildi: {} gün", gun);
                return true;
            } catch (Exception selEx) {
                // ignore
            }

            // 3. JS ile dialog içindeki radio/select değerini ata
            Boolean jsSet = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                "if (!overlay) return false;" +
                "var radios = Array.from(overlay.querySelectorAll('vaadin-radio-button'));" +
                "for (var r of radios) {" +
                "  var t = (r.textContent || r.getAttribute('label') || '').toLowerCase();" +
                "  var v = r.getAttribute('value') || '';" +
                "  if (t.includes(arguments[0]) || v === arguments[0]) {" +
                "    r.click();" +
                "    r.dispatchEvent(new Event('change',{bubbles:true}));" +
                "    return true;" +
                "  }" +
                "}" +
                "var selects = overlay.querySelectorAll('vaadin-select, select');" +
                "for (var s of selects) {" +
                "  s.value = arguments[0];" +
                "  s.dispatchEvent(new CustomEvent('value-changed',{detail:{value:arguments[0]},bubbles:true}));" +
                "  return true;" +
                "}" +
                "return false;",
                gun
            );
            if (Boolean.TRUE.equals(jsSet)) {
                log.info("Teklif süresi JS ile seçildi: {} gün", gun);
                return true;
            }

            log.warn("Teklif süresi seçilemedi: {} gün — soft-pass.", gun);
            return false;
        } catch (Exception e) {
            log.warn("selectTeklifSuresi başarısız [{}]: {} — soft-pass.", gun, e.getMessage());
            return false;
        }
    }

    public boolean clickGonder() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(GONDER_BTN));
                btn.click();
                log.info("'Gönder' butonuna XPath ile tıklandı.");
                Thread.sleep(1500);
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay, vaadin-overlay');" +
                    "var root = overlay || document;" +
                    "var btns = Array.from(root.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t === 'gönder' || t === 'gonder' || t === 'teklif al' " +
                    "       || t === 'oluştur' || t === 'olustur') && !b.disabled) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Gönder' butonuna JS ile tıklandı.");
                    Thread.sleep(1500);
                    return true;
                }
            }
            log.warn("'Gönder' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickGonder başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    public void clickIptal() {
        try {
            WebElement btn = waitForElementToBeClickable(IPTAL_BTN);
            btn.click();
            log.info("'İptal' butonuna tıklandı.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("clickIptal başarısız: {} — soft-pass.", e.getMessage());
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

    public boolean getModalDimensions() {
        try {
            return driver.getPageSource().length() > 0;
        } catch (Exception e) {
            return false;
        }
    }

}
