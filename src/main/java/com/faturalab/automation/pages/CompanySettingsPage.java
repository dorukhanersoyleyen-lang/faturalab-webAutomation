package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

/**
 * Tedarikçi — Ayarlar / görünüm sayısı (UAT FL-005).
 */
public class CompanySettingsPage extends BasePageObject {

    private static final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final CompanyInvoicePage invoiceNav;

    public CompanySettingsPage(WebDriver driver) {
        super(driver);
        this.invoiceNav = new CompanyInvoicePage(driver);
    }

    public void navigateToAyarlar() {
        tryOpenNavigationDrawer();
        invoiceNav.navigateToInvoiceList();
        waitForVaadinNavigation();
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-side-nav-item")),
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-grid"))));
        } catch (Exception e) {
            log.debug("Ana kabuk beklenirken: {}", e.getMessage());
        }
        tryOpenNavigationDrawer();
        if (!clickNavItemByText("ayarlar")) {
            clickNavItemByText("Ayarlar");
        }
        if (!clickNavItemByText("şirket")) {
            clickNavItemByText("Sirket");
        }
        try {
            WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//vaadin-button[contains(@class,'menu-button') and " +
                            "(contains(normalize-space(),'Ayarlar') or contains(normalize-space(),'Şirket') or " +
                            " contains(normalize-space(),'şirket'))]")));
            menu.click();
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Ayarlar menüsü: {}", e.getMessage());
        }
    }

    public void clickDuzenle() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase();" +
                    "  if (t.includes('düzenle') || t.includes('duzenle')) { b.click(); return; }" +
                    "}");
        } catch (Exception e) {
            log.warn("Düzenle: {}", e.getMessage());
        }
    }

    public void selectGorunumSayisi(String sayi) {
        try {
            WebElement select = driver.findElement(By.cssSelector("vaadin-select, vaadin-combo-box"));
            select.click();
            try {
                Thread.sleep(400);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            ((JavascriptExecutor) driver).executeScript(
                    "var items = document.querySelectorAll('vaadin-item, [role=\"option\"]');" +
                    "var target = arguments[0];" +
                    "for (var it of items) {" +
                    "  if ((it.textContent || '').trim() === target) { it.click(); return true; }" +
                    "}" +
                    "return false;",
                    sayi);
        } catch (Exception e) {
            log.warn("Görünüm sayısı: {}", e.getMessage());
        }
    }

    public void clickKaydet() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  if ((b.textContent || '').trim().toLowerCase() === 'kaydet') { b.click(); return; }" +
                    "}");
            Thread.sleep(1000);
        } catch (Exception e) {
            log.warn("Kaydet: {}", e.getMessage());
        }
    }

    /**
     * Ayarlar / görünüm sayısı formu hâlâ ekranda mı (toast sessiz kayıtta olabilir).
     */
    public boolean isLikelyOnAyarlarScreen() {
        try {
            if (!driver.findElements(By.cssSelector("vaadin-select, vaadin-combo-box")).isEmpty()) {
                return true;
            }
            return !driver.findElements(By.xpath(
                    "//vaadin-button[contains(normalize-space(),'Düzenle') or contains(normalize-space(),'düzenle')]"))
                    .isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuccessNotificationVisible() {
        try {
            waitForVisibility(SUCCESS_NOTIFICATION, 12);
            return true;
        } catch (Exception e) {
            log.debug("SUCCESS_NOTIFICATION: {}", e.getMessage());
        }
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(drv -> Boolean.TRUE.equals(((JavascriptExecutor) drv).executeScript(
                            "var s = document.querySelectorAll('[theme~=\"success\"], vaadin-notification-card, "
                                    + ".v-Notification.notification-success');" +
                            "for (var i = 0; i < s.length; i++) {" +
                            "  var r = s[i].getBoundingClientRect();" +
                            "  if (r.width > 2 && r.height > 2) return true;" +
                            "}" +
                            "return false;")));
            return true;
        } catch (Exception e2) {
            return false;
        }
    }
}
