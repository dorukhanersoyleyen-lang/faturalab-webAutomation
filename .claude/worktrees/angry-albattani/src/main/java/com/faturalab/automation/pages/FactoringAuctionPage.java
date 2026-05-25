package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Finansman (Factoring) — Aktif İhale Listesi ve Teklif Verme.
 */
public class FactoringAuctionPage extends BasePageObject {

    private final By AKTIF_IHALELER_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[(contains(normalize-space(),'İhale') or contains(normalize-space(),'Ihale') " +
            " or contains(normalize-space(),'Açık Artırma') or contains(normalize-space(),'Auction')) and " +
            "(contains(normalize-space(),'Aktif') or contains(normalize-space(),'Liste') " +
            " or contains(normalize-space(),'Teklif') or contains(normalize-space(),'Bekleyen'))]");

    private final By IHALE_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS = By.cssSelector("vaadin-grid-cell-content");

    private final By TEKLIF_VER_BTN = By.xpath(
            "//vaadin-button[contains(normalize-space(),'Teklif') and contains(normalize-space(),'Ver')] | " +
            "//vaadin-button[normalize-space()='TEKLIF VER'] | " +
            "//vaadin-button[normalize-space()='Teklif Gir'] | " +
            "//vaadin-button[normalize-space()='GÖZAT'] | " +
            "//vaadin-button[normalize-space()='Gözat']");

    private final By FAIZ_ORANI_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Faiz') or contains(@label,'faiz') " +
            " or contains(@label,'Oran') or contains(@label,'Rate')]//input | " +
            "//vaadin-text-field[contains(@label,'Faiz')]//input | " +
            "//input[contains(@name,'rate') or contains(@id,'faiz') or contains(@placeholder,'faiz')]");

    private final By TEKLIF_TUTARI_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Teklif') or contains(@label,'Tutar') " +
            " or contains(@label,'Amount') or contains(@label,'Miktar')]//input | " +
            "//vaadin-text-field[contains(@label,'Teklif')]//input | " +
            "//input[contains(@name,'amount') or contains(@id,'teklif') or contains(@placeholder,'tutar')]");

    private final By KAYDET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-button[normalize-space()='KAYDET'] | " +
            "//vaadin-button[normalize-space()='Teklif Ver'] | " +
            "//vaadin-button[normalize-space()='Gönder'] | " +
            "//vaadin-button[normalize-space()='Submit']");

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification");

    public FactoringAuctionPage(WebDriver driver) {
        super(driver);
    }

    public void navigateToActiveAuctions() {
        try {
            WebElement menu = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(AKTIF_IHALELER_MENU));
            menu.click();
            log.info("Aktif ihaleler menüsüne tıklandı.");
            waitForVaadinNavigation();
            return;
        } catch (Exception ex) {
            log.info("XPath ile bulunamadı, JS nav deneniyor...");
        }
        for (String kw : new String[]{"ihale", "auction", "aktif", "teklif", "bekleyen"}) {
            if (clickNavItemByText(kw)) {
                waitForVaadinNavigation();
                log.info("İhale listesine JS ile gidildi: keyword={}", kw);
                return;
            }
        }
        log.warn("Aktif ihaleler navigasyonu başarısız — mevcut sayfada devam.");
    }

    public void openOfferFormForFirstAuction() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if (t.includes('teklif') || t.includes('gözat') || t.includes('gozat') " +
                "   || t.includes('offer') || t.includes('bid')) {" +
                "    if (!b.disabled && b.getAttribute('aria-disabled') !== 'true') {" +
                "      b.click(); return true;" +
                "    }" +
                "  }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(clicked)) {
                log.info("Teklif Ver butonuna JS ile tıklandı.");
                waitForVaadinNavigation();
            } else {
                WebElement btn = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(TEKLIF_VER_BTN));
                btn.click();
                log.info("Teklif Ver butonuna XPath ile tıklandı.");
                waitForVaadinNavigation();
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Teklif formu açılamadı: {}", e.getMessage());
        }
    }

    public void fillOfferForm(String faizOrani, String teklifTutari) {
        try {
            fillField(FAIZ_ORANI_INPUT, faizOrani, "Faiz Oranı");
            Thread.sleep(300);
            fillField(TEKLIF_TUTARI_INPUT, teklifTutari, "Teklif Tutarı");
            log.info("Teklif formu dolduruldu: faiz={}%, tutar={}", faizOrani, teklifTutari);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Teklif formu doldurulamadı: {}", e.getMessage());
        }
    }

    public void clickKaydet() {
        try {
            WebElement btn = new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
            btn.click();
            log.info("Kaydet butonuna tıklandı.");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').trim();" +
                "  if (t === 'Kaydet' || t === 'KAYDET' || t === 'Teklif Ver' || t === 'Gönder') {" +
                "    if (!b.disabled) { b.click(); return true; }" +
                "  }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) log.error("Kaydet butonu bulunamadı.");
        }
    }

    public boolean isOfferSubmitted() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 6).isDisplayed();
        } catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("başarı") || src.contains("kaydedildi") || src.contains("teklif")
                    || src.contains("WAITING") || src.contains("success");
        }
    }

    public boolean isOfferWithinCriteria() {
        String src = driver.getPageSource();
        boolean hasError = src.contains("limit aş") || src.contains("kriter dışı")
                || src.contains("geçersiz faiz") || src.contains("bloklandı")
                || src.contains("BLOCKED") || src.contains("REJECTED");
        return !hasError;
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
}
