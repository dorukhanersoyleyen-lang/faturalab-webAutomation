package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Admin — Kullanıcı yönetimi (UAT FL-004).
 */
public class AdminKullaniciPage extends BasePageObject {

    private static final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final AdminPanelPage adminPanel;

    public AdminKullaniciPage(WebDriver driver) {
        super(driver);
        this.adminPanel = new AdminPanelPage(driver);
    }

    public void navigateToKullaniciYonetimi() {
        try {
            adminPanel.clickSidebarItem("Kullanıcılar");
        } catch (Exception e) {
            clickNavItemByText("kullanıcı");
        }
        waitForVaadinNavigation();
    }

    public boolean isKullaniciGridVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("vaadin-grid")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickFirstUserRow() {
        try {
            WebElement cell = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("vaadin-grid-cell-content")));
            cell.click();
        } catch (Exception e) {
            log.warn("İlk kullanıcı satırı: {}", e.getMessage());
        }
    }

    public void clickDuzenle() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase();" +
                    "  if (t.includes('düzenle') || t.includes('duzenle')) { b.click(); return true; }" +
                    "}" +
                    "return false;");
        } catch (Exception e) {
            log.warn("Düzenle: {}", e.getMessage());
        }
        acceptVaadinConfirmDialogIfPresent();
    }

    public String readKullaniciAdres() {
        try {
            WebElement field = driver.findElement(By.xpath(
                    "//vaadin-text-field[contains(@label,'Adres') or contains(@label,'adres')]//input"));
            String a = field.getAttribute("value");
            return a != null ? a : "";
        } catch (Exception e) {
            log.warn("Kullanıcı adres okuma: {}", e.getMessage());
            return "";
        }
    }

    public void updateKullaniciAdres(String adres) {
        try {
            Boolean js = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var kw = 'adres';" +
                    "var roots = [document.querySelector('vaadin-dialog-overlay[opened]')," +
                    "  document.querySelector('vaadin-dialog-overlay')," +
                    "  document.querySelector('vaadin-form-layout'), document.body];" +
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
                    "//vaadin-text-field[contains(@label,'Adres') or contains(@label,'adres')]//input"));
            field.clear();
            field.sendKeys(adres);
        } catch (Exception e) {
            log.warn("Kullanıcı adres: {}", e.getMessage());
        }
    }

    public void clickKaydet() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var btns = document.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').trim().toLowerCase();" +
                    "  if (t === 'kaydet') { b.click(); return true; }" +
                    "}" +
                    "return false;");
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Kaydet: {}", e.getMessage());
        }
        acceptVaadinConfirmDialogIfPresent();
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
}
