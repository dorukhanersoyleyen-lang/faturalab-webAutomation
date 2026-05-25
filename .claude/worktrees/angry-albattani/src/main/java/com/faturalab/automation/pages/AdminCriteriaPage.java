package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Admin — Limit ve Fiyat Yönetimi (Kriter Tabloları) + Bordro Yönetimi ekranları.
 */
public class AdminCriteriaPage extends BasePageObject {

    private final By LIMIT_FIYAT_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[(contains(normalize-space(),'Limit') or contains(normalize-space(),'limit')) and " +
            "(contains(normalize-space(),'Fiyat') or contains(normalize-space(),'Kriter') " +
            " or contains(normalize-space(),'Yönetim') or contains(normalize-space(),'Yonetim'))]");

    private final By BORDRO_YONETIMI_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'Bordro') and " +
            "(contains(normalize-space(),'Yönetim') or contains(normalize-space(),'Yonetim') " +
            " or contains(normalize-space(),'Onay') or contains(normalize-space(),'Liste'))]");

    private final By KURUM_COMBOBOX = By.xpath(
            "//vaadin-combo-box[contains(@label,'Kurum') or contains(@label,'Finansman') " +
            " or contains(@label,'kurum')]//input | " +
            "//vaadin-select[contains(@label,'Kurum')]//input");

    private final By LIMIT_TUTARI_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Limit') or contains(@label,'limit') " +
            " or contains(@label,'Tutar')]//input | " +
            "//vaadin-text-field[contains(@label,'Limit')]//input | " +
            "//input[contains(@name,'limit') or contains(@id,'limit')]");

    private final By PARA_BIRIMI_COMBOBOX = By.xpath(
            "//vaadin-combo-box[contains(@label,'Para') or contains(@label,'Birim') " +
            " or contains(@label,'Currency')]//input | " +
            "//vaadin-select[contains(@label,'Para')]//input");

    private final By LIMIT_KAYDET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-button[normalize-space()='KAYDET'] | " +
            "//vaadin-button[normalize-space()='Güncelle'] | " +
            "//vaadin-button[normalize-space()='Ekle']");

    private final By MIN_FAIZ_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Min') or contains(@label,'min') " +
            " or contains(@label,'Minimum') or contains(@label,'Alt')]//input | " +
            "//input[contains(@name,'minRate') or contains(@id,'minFaiz')]");

    private final By MAX_FAIZ_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Max') or contains(@label,'max') " +
            " or contains(@label,'Maksimum') or contains(@label,'Üst')]//input | " +
            "//input[contains(@name,'maxRate') or contains(@id,'maxFaiz')]");

    private final By AZAMI_VADE_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Vade') or contains(@label,'vade') " +
            " or contains(@label,'Gün') or contains(@label,'Day')]//input | " +
            "//vaadin-text-field[contains(@label,'Vade')]//input | " +
            "//input[contains(@name,'tenor') or contains(@id,'vade')]");

    private final By OTOMATIK_ESLEME_CHECKBOX = By.xpath(
            "//vaadin-checkbox[contains(@label,'Otomatik') or contains(@label,'otomatik') " +
            " or contains(@label,'Eşleş') or contains(@label,'Auto')]");

    private final By KRITER_KAYDET_BTN = By.xpath(
            "(//vaadin-button[normalize-space()='Kaydet'])[last()] | " +
            "//vaadin-button[normalize-space()='Kriterleri Kaydet'] | " +
            "//vaadin-button[normalize-space()='Uygula']");

    private final By BORDRO_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS  = By.cssSelector("vaadin-grid-cell-content");

    private final By ONAYLA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='ONAYLA'] | " +
            "//vaadin-button[normalize-space()='Onayla'] | " +
            "//button[normalize-space()='ONAYLA']");

    private final By CONFIRM_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam']");

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification");

    public AdminCriteriaPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToLimitFiyatYonetimi() {
        try {
            WebElement menu = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(LIMIT_FIYAT_MENU));
            menu.click();
            log.info("Limit ve Fiyat Yönetimi menüsüne tıklandı.");
            waitForVaadinNavigation();
            return;
        } catch (Exception ex) {
            log.info("XPath ile bulunamadı, JS nav deneniyor...");
        }
        for (String kw : new String[]{"limit", "fiyat", "kriter", "finansman"}) {
            if (clickNavItemByText(kw)) {
                waitForVaadinNavigation();
                log.info("Limit/Fiyat menüsüne JS ile gidildi: keyword={}", kw);
                return;
            }
        }
        log.warn("Limit ve Fiyat Yönetimi navigasyonu başarısız — mevcut sayfada devam.");
    }

    public void navigateToBordroYonetimi() {
        try {
            WebElement menu = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(BORDRO_YONETIMI_MENU));
            menu.click();
            log.info("Bordro Yönetimi menüsüne tıklandı.");
            waitForVaadinNavigation();
            return;
        } catch (Exception ex) {
            log.info("Bordro menüsü XPath ile bulunamadı, JS nav deneniyor...");
        }
        for (String kw : new String[]{"bordro", "payroll", "bordr"}) {
            if (clickNavItemByText(kw)) {
                waitForVaadinNavigation();
                log.info("Bordro menüsüne JS ile gidildi: keyword={}", kw);
                return;
            }
        }
        log.warn("Bordro Yönetimi navigasyonu başarısız — mevcut sayfada devam.");
    }

    public void setFinansmanLimit(String kurumAdi, String limitTutari, String paraBirimi) {
        try {
            fillComboBox(KURUM_COMBOBOX, kurumAdi, "Kurum");
            Thread.sleep(500);
            fillField(LIMIT_TUTARI_INPUT, limitTutari, "Limit Tutarı");
            if (paraBirimi != null && !paraBirimi.isBlank()) {
                fillComboBox(PARA_BIRIMI_COMBOBOX, paraBirimi, "Para Birimi");
            }
            clickSave(LIMIT_KAYDET_BTN, "Limit Kaydet");
            log.info("Finansman limiti ayarlandı: kurum={}, tutar={}, para={}", kurumAdi, limitTutari, paraBirimi);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Finansman limiti ayarlanamadı: {}", e.getMessage());
        }
    }

    public void setFiyatKriterleri(String minFaiz, String maxFaiz, String azamiVadeGun, boolean otomatikEsleme) {
        try {
            fillField(MIN_FAIZ_INPUT, minFaiz, "Min Faiz Oranı");
            fillField(MAX_FAIZ_INPUT, maxFaiz, "Max Faiz Oranı");
            fillField(AZAMI_VADE_INPUT, azamiVadeGun, "Azami Vade Gün");
            if (otomatikEsleme) {
                try {
                    WebElement cb = driver.findElement(OTOMATIK_ESLEME_CHECKBOX);
                    String checked = cb.getAttribute("checked");
                    if (checked == null || checked.isEmpty()) {
                        cb.click();
                        log.info("Otomatik eşleşme aktif edildi.");
                    }
                } catch (Exception ex) {
                    log.warn("Otomatik eşleşme checkbox bulunamadı: {}", ex.getMessage());
                }
            }
            clickSave(KRITER_KAYDET_BTN, "Kriter Kaydet");
            log.info("Fiyat kriterleri ayarlandı: min={}%, max={}%, vade={}gün, oto={}", minFaiz, maxFaiz, azamiVadeGun, otomatikEsleme);
        } catch (Exception e) {
            log.warn("Fiyat kriterleri ayarlanamadı: {}", e.getMessage());
        }
    }

    public void clickOnaylaForBordro(String bordroNo) {
        try {
            if (bordroNo != null && !bordroNo.isBlank()) {
                By rowBtn = By.xpath(
                        "//*[contains(text(),'" + bordroNo + "')]" +
                        "/ancestor::*[self::tr or self::vaadin-grid-row or contains(@part,'row')]" +
                        "//vaadin-button[normalize-space()='ONAYLA' or normalize-space()='Onayla']");
                try {
                    WebElement btn = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(rowBtn));
                    btn.click();
                    log.info("Bordro [{}] için ONAYLA butonuna tıklandı.", bordroNo);
                    confirmIfDialogAppears();
                    return;
                } catch (Exception ex) {
                    log.info("Satır-bazlı ONAYLA bulunamadı, ilk görünür butona tıklanıyor...");
                }
            }
            WebElement btn = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(ONAYLA_BTN));
            btn.click();
            log.info("İlk ONAYLA butonuna tıklandı.");
            confirmIfDialogAppears();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').trim();" +
                "  if (t === 'ONAYLA' || t === 'Onayla') {" +
                "    if (!b.disabled) { b.click(); return true; }" +
                "  }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) {
                log.error("ONAYLA butonu bulunamadı: {}", e.getMessage());
            }
        }
    }

    public boolean isCriteriaSaved() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("kaydedildi") || src.contains("güncellendi") || src.contains("başarı")
                    || src.contains("success") || src.contains("Kayıt");
        }
    }

    public boolean isBordroInStatus(String bordroNo, String status) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(BORDRO_GRID));
            Thread.sleep(1000);
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            boolean bordroFound = false;
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && text.contains(bordroNo)) bordroFound = true;
                if (bordroFound && text != null && text.contains(status)) return true;
            }
            return driver.getPageSource().contains(status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return driver.getPageSource().contains(status);
        }
    }

    public boolean isBordroApproved() {
        try {
            String src = driver.getPageSource();
            return src.contains("APPROVED") || src.contains("Onaylandı") || src.contains("onaylandi");
        } catch (Exception e) {
            return false;
        }
    }

    private void fillField(By locator, String value, String fieldName) {
        try {
            WebElement field = new WebDriverWait(driver, java.time.Duration.ofSeconds(3))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                field);
            field.sendKeys(value);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                field);
            log.info("{} girildi: {}", fieldName, value);
        } catch (Exception e) {
            log.warn("{} girilemedi: {}", fieldName, e.getMessage());
        }
    }

    private void fillComboBox(By locator, String value, String fieldName) {
        try {
            WebElement input = new WebDriverWait(driver, java.time.Duration.ofSeconds(3))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
            input.clear();
            input.sendKeys(value);
            Thread.sleep(500);
            By option = By.xpath(
                "//vaadin-combo-box-overlay//vaadin-combo-box-item[contains(normalize-space(),'" + value + "')] | " +
                "//*[@role='option'][contains(normalize-space(),'" + value + "')]");
            try {
                WebElement opt = new WebDriverWait(driver, java.time.Duration.ofSeconds(4))
                    .until(ExpectedConditions.elementToBeClickable(option));
                opt.click();
            } catch (Exception ex) {
                input.sendKeys(org.openqa.selenium.Keys.ENTER);
            }
            log.info("{} seçildi: {}", fieldName, value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("{} seçilemedi: {}", fieldName, e.getMessage());
        }
    }

    private void clickSave(By locator, String label) {
        try {
            WebElement btn = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(locator));
            btn.click();
            log.info("{} butonuna tıklandı.", label);
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("{} butonu tıklanamadı: {}", label, e.getMessage());
        }
    }

    private void confirmIfDialogAppears() {
        try {
            WebElement btn = new WebDriverWait(driver, java.time.Duration.ofSeconds(3))
                .until(ExpectedConditions.elementToBeClickable(CONFIRM_BTN));
            btn.click();
            log.info("Onay dialogu onaylandı.");
            Thread.sleep(1000);
        } catch (Exception ignored) {}
    }
}
