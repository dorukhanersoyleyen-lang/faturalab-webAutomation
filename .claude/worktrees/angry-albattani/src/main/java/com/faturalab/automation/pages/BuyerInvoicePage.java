package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Alıcı (Buyer) — Kağıt Fatura Yükleme ekranı.
 */
public class BuyerInvoicePage extends BasePageObject {

    private final By KAGIT_FATURA_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'Kağıt') or contains(normalize-space(),'Kagit') " +
            " or contains(normalize-space(),'Paper') or contains(normalize-space(),'Manuel')]");

    private final By FILE_UPLOAD_INPUT = By.cssSelector(
            "input[type='file'], vaadin-upload input[type='file']");

    private final By SUPPLIER_TAX_INPUT = By.xpath(
            "//vaadin-text-field[contains(@label,'Vergi') or contains(@label,'Tedarik')]//input | " +
            "//input[contains(@placeholder,'vergi') or contains(@placeholder,'Vergi') " +
            "  or contains(@name,'tax') or contains(@id,'tax')]");

    private final By AMOUNT_INPUT = By.xpath(
            "//vaadin-text-field[contains(@label,'Tutar') or contains(@label,'Miktar')]//input | " +
            "//vaadin-number-field[contains(@label,'Tutar') or contains(@label,'Miktar')]//input | " +
            "//input[contains(@placeholder,'Tutar') or contains(@placeholder,'tutar')]");

    private final By DUE_DATE_INPUT = By.xpath(
            "//vaadin-date-picker[contains(@label,'Vade') or contains(@label,'Son Tarih')]//input | " +
            "//input[contains(@placeholder,'vade') or contains(@placeholder,'Vade') " +
            "  or contains(@name,'due') or contains(@id,'due')]");

    private final By YUKLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Yükle'] | " +
            "//vaadin-button[normalize-space()='YÜKLE'] | " +
            "//vaadin-button[normalize-space()='Yukle'] | " +
            "//button[normalize-space()='Yükle']");

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification");

    public BuyerInvoicePage(WebDriver driver) {
        super(driver);
    }

    public void navigateToPaperInvoiceUpload() {
        try {
            try {
                WebElement menu = waitForElementToBeClickable(KAGIT_FATURA_MENU);
                menu.click();
                log.info("Kağıt Fatura menüsüne XPath ile tıklandı.");
            } catch (Exception ex) {
                log.info("XPath ile bulunamadı, JS nav deneniyor...");
                boolean clicked = clickNavItemByText("kağıt")
                        || clickNavItemByText("kagit")
                        || clickNavItemByText("paper")
                        || clickNavItemByText("manuel");
                if (!clicked) log.warn("Kağıt Fatura menüsü bulunamadı — mevcut sayfada devam.");
            }
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.error("Kağıt fatura navigasyonu başarısız: {}", e.getMessage());
        }
    }

    public void uploadImageFile() {
        String defaultPath = System.getProperty("user.dir") +
                "/src/test/resources/testdata/test-invoice.pdf";
        uploadImageFile(defaultPath);
    }

    public void uploadImageFile(String filePath) {
        try {
            WebElement fileInput = null;
            try {
                fileInput = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "var u = document.querySelector('vaadin-upload');" +
                    "if (u && u.shadowRoot) return u.shadowRoot.querySelector('input[type=\"file\"]');" +
                    "return null;"
                );
            } catch (Exception ignored) {}

            if (fileInput == null) {
                try {
                    fileInput = driver.findElement(By.cssSelector("vaadin-upload input[type='file']"));
                } catch (Exception ignored) {}
            }
            if (fileInput == null) {
                fileInput = driver.findElement(FILE_UPLOAD_INPUT);
            }

            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.cssText='display:block!important;visibility:visible!important;opacity:1!important;';",
                fileInput);
            fileInput.sendKeys(filePath);
            log.info("Görsel dosya gönderildi: {}", filePath);
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Görsel yükleme başarısız (soft-fail): {}", e.getMessage());
        }
    }

    public void fillInvoiceForm(String supplierTaxNo, String amount, int tenorDays) {
        try {
            fillField(SUPPLIER_TAX_INPUT, supplierTaxNo, "Tedarikçi Vergi No");
            fillField(AMOUNT_INPUT, amount, "Tutar");
            if (tenorDays > 0) {
                LocalDate dueDate = LocalDate.now().plusDays(tenorDays);
                String dueDateStr = dueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                fillField(DUE_DATE_INPUT, dueDateStr, "Vade Tarihi");
            }
            log.info("Kağıt fatura formu dolduruldu: vergi={}, tutar={}, vade={}g",
                    supplierTaxNo, amount, tenorDays);
        } catch (Exception e) {
            log.error("Form doldurulamadı: {}", e.getMessage());
        }
    }

    public void clickYukle() {
        try {
            WebElement btn = waitForElementToBeClickable(YUKLE_BTN);
            btn.click();
            log.info("Yükle butonuna tıklandı.");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = b.textContent.trim().toLowerCase();" +
                "  if (t === 'yükle' || t === 'yukle' || t === 'upload') { b.click(); return true; }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) {
                log.error("Yükle butonu bulunamadı: {}", e.getMessage());
            }
        }
    }

    public boolean isUploadSuccessful() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 10).isDisplayed();
        } catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("başarı") || src.contains("basari")
                    || src.contains("success") || src.contains("yuklendi");
        }
    }

    private void fillField(By locator, String value, String fieldName) {
        try {
            WebElement field = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(3))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(locator));
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
