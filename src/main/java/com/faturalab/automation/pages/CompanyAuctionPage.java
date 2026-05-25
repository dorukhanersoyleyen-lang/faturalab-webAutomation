package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tedarikçi (Company) — İhale Oluşturma ve Teklif Yönetimi.
 *
 * Akış:
 *  1. Tedarikçi onaylanmış fatura için ihale açar (DRAFT)
 *  2. İhaleyi yayınlar → WAITING (finansman tekliflerini bekler)
 *  3. Finansman tekliflerini görür, en avantajlısını seçer → PENDINGBUYER
 *  4. Bordro otomatik oluşur
 *
 * Ekranlar:
 *  - "Faturalarım" veya "Aktif Faturalar" → fatura listesi
 *  - "İhale Oluştur" butonu veya menüsü
 *  - "Aktif İhaleler" → teklif listesi, Kabul Et
 */
public class CompanyAuctionPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Company sidebar "Yüklenmişler" butonu (class: menu-button).
     * Fatura listesine gitmek için kullanılır.
     */
    private final By AKTIF_FATURALAR_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and contains(normalize-space(),'Yüklenmişler')] | " +
            "//vaadin-button[contains(@class,'menu-button') and contains(normalize-space(),'İskonto Edilenler')]");

    /**
     * Company sidebar "İhaleler" butonu (class: menu-button).
     */
    private final By AKTIF_IHALELER_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='İhaleler']");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By GRID = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS = By.cssSelector("vaadin-grid-cell-content");

    // ─── İhale Oluşturma ──────────────────────────────────────────────────────

    private final By IHALE_OLUSTUR_BTN = By.xpath(
            "//vaadin-button[contains(normalize-space(),'İhale') and contains(normalize-space(),'Oluştur')] | " +
            "//vaadin-button[contains(normalize-space(),'Ihale') and contains(normalize-space(),'Olustur')] | " +
            "//vaadin-button[normalize-space()='Yeni İhale'] | " +
            "//button[contains(normalize-space(),'İhale')]");

    private final By BITIS_TARIHI_INPUT = By.xpath(
            "//vaadin-date-picker[contains(@label,'Bitiş') or contains(@label,'Bitis')]//input | " +
            "//vaadin-date-time-picker//input | " +
            "//input[contains(@placeholder,'Bitiş') or contains(@placeholder,'Bitis') " +
            "  or contains(@name,'end') or contains(@id,'endDate')]");

    private final By BUYNOW_INPUT = By.xpath(
            "//vaadin-text-field[contains(@label,'BuyNow') or contains(@label,'Eşik') " +
            "  or contains(@label,'Esik') or contains(@label,'Anlık')]//input | " +
            "//vaadin-number-field[contains(@label,'BuyNow')]//input");

    private final By YAYINLA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yayınla'] | " +
            "//vaadin-button[normalize-space()='YAYINLA'] | " +
            "//vaadin-button[normalize-space()='Yayinla'] | " +
            "//vaadin-button[normalize-space()='Publish']");

    // ─── Teklif Kabul ─────────────────────────────────────────────────────────

    private final By KABUL_ET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Kabul Et'] | " +
            "//vaadin-button[normalize-space()='KABUL ET'] | " +
            "//vaadin-button[normalize-space()='Kabul'] | " +
            "//vaadin-button[normalize-space()='Accept']");

    private final By CONFIRM_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public CompanyAuctionPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Company sidebar "İhaleler" butonuna tıklar.
     */
    public void navigateToIhaleler() {
        try {
            WebElement menu = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(15))
                    .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(AKTIF_IHALELER_MENU));
            menu.click();
            log.info("Sidebar 'İhaleler' tıklandı.");
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("İhaleler navigasyonu başarısız: {}", e.getMessage());
        }
    }

    /**
     * Tedarikçi aktif/onaylı fatura listesine gider.
     */
    public void navigateToActiveInvoices() {
        try {
            try {
                WebElement menu = waitForElementToBeClickable(AKTIF_FATURALAR_MENU);
                menu.click();
                log.info("Aktif faturalar menüsüne tıklandı.");
            } catch (Exception ex) {
                boolean clicked = clickNavItemByText("fatura")
                        || clickNavItemByText("aktif")
                        || clickNavItemByText("onaylı");
                if (!clicked) log.warn("Aktif faturalar menüsü bulunamadı.");
            }
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Aktif faturalar navigasyonu: {}", e.getMessage());
        }
    }

    /**
     * Tedarikçi ihale/teklif listesine gider.
     */
    public void navigateToAuctionOffers() {
        try {
            try {
                WebElement menu = waitForElementToBeClickable(AKTIF_IHALELER_MENU);
                menu.click();
                log.info("Aktif ihaleler menüsüne tıklandı.");
            } catch (Exception ex) {
                boolean clicked = clickNavItemByText("ihale")
                        || clickNavItemByText("teklif");
                if (!clicked) log.warn("Aktif ihaleler menüsü bulunamadı.");
            }
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("İhale/teklif navigasyonu: {}", e.getMessage());
        }
    }

    // ─── İhale Oluşturma ──────────────────────────────────────────────────────

    /**
     * Son eklenen/onaylı fatura satırı için ihale oluşturma sürecini başlatır.
     * Önce "İhale Oluştur" butonuna tıklar; varsa listeden ilk uygun faturayı seçer.
     */
    public void startAuctionForLatestInvoice() {
        try {
            // Sayfadaki tüm ihale/oluştur butonlarını dene
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if (t.includes('ihale') || t.includes('auction') || " +
                "      t.includes('oluştur') || t.includes('olustur') || t.includes('create')) {" +
                "    if (!b.disabled && b.getAttribute('aria-disabled') !== 'true') {" +
                "      b.click(); return true;" +
                "    }" +
                "  }" +
                "}" +
                "return false;"
            );

            if (Boolean.TRUE.equals(clicked)) {
                log.info("İhale oluştur butonuna tıklandı.");
                waitForVaadinNavigation();
            } else {
                // Fallback: XPath ile dene
                WebElement btn = waitForElementToBeClickable(IHALE_OLUSTUR_BTN);
                btn.click();
                log.info("İhale oluştur butonuna XPath ile tıklandı.");
                waitForVaadinNavigation();
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("İhale oluşturma başlatılamadı: {}", e.getMessage());
        }
    }

    /**
     * İhale formunu doldurur.
     *
     * @param durationHours İhale süresi (saat) — bitiş tarihi bugün+N saat
     * @param buyNowThreshold BuyNow eşiği (TRY, 0 = kapalı)
     */
    public void fillAuctionForm(int durationHours, int buyNowThreshold) {
        try {
            // Bitiş tarihi
            LocalDate endDate = LocalDate.now().plusDays(Math.max(1, durationHours / 24));
            String endDateStr = endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            fillFieldByLocator(BITIS_TARIHI_INPUT, endDateStr, "Bitiş Tarihi");

            // BuyNow eşiği
            if (buyNowThreshold > 0) {
                fillFieldByLocator(BUYNOW_INPUT, String.valueOf(buyNowThreshold), "BuyNow Eşiği");
            }
            log.info("İhale formu dolduruldu: süre={}s, buyNow={}", durationHours, buyNowThreshold);
        } catch (Exception e) {
            log.warn("İhale formu doldurulamadı: {}", e.getMessage());
        }
    }

    /**
     * "Yayınla" butonuna tıklar.
     */
    public void clickYayinla() {
        try {
            WebElement btn = waitForElementToBeClickable(YAYINLA_BTN);
            btn.click();
            log.info("Yayınla butonuna tıklandı.");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = b.textContent.toLowerCase().trim();" +
                "  if (t === 'yayınla' || t === 'yayinla' || t === 'publish') { b.click(); return true; }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) log.error("Yayınla butonu bulunamadı.");
        }
    }

    // ─── Teklif Kabul ─────────────────────────────────────────────────────────

    /**
     * Mevcut teklifler listesinde "Kabul Et" butonuna tıklar.
     */
    public void clickKabulEt() {
        try {
            WebElement btn = waitForElementToBeClickable(KABUL_ET_BTN);
            btn.click();
            log.info("Kabul Et butonuna tıklandı.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if (t.includes('kabul') || t.includes('accept') || t.includes('al')) {" +
                "    if (!b.disabled) { b.click(); return true; }" +
                "  }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) log.error("Kabul Et butonu bulunamadı.");
        }
    }

    /**
     * Onay dialogu açılırsa "Evet" / "Onayla" / "Tamam" butonuna tıklar.
     */
    public void confirmDialog() {
        try {
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(CONFIRM_BTN));
            btn.click();
            log.info("Onay dialogu onaylandı.");
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // Dialog gelmedi, normal
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public boolean isAuctionCreated() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 8).isDisplayed();
        } catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("WAITING") || src.contains("başarı") || src.contains("oluşturuldu");
        }
    }

    public boolean isOfferAccepted() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 8).isDisplayed();
        } catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("PENDINGBUYER") || src.contains("kabul") || src.contains("başarı");
        }
    }

    /**
     * Sayfadaki grid'den ihale durumunu döner.
     */
    public String getAuctionStatus() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(GRID));
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            String[] statuses = {"WAITING", "PENDINGBUYER", "ACCEPTED", "REJECTED",
                                 "DRAFT", "CMB_APPROVAL", "CMB_APPROVED"};
            for (WebElement cell : cells) {
                String text = cell.getText();
                for (String s : statuses) {
                    if (text != null && text.contains(s)) return s;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    private void fillFieldByLocator(By locator, String value, String fieldName) {
        try {
            WebElement field = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(3))
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
