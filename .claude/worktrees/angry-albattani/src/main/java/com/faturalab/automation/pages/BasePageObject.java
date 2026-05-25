package com.faturalab.automation.pages;

import com.faturalab.automation.utils.WaitHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public abstract class BasePageObject {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WaitHelper waitHelper;
    protected static final Logger log = LogManager.getLogger(BasePageObject.class);
    
    public BasePageObject(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.waitHelper = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
    }
    
    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        element.click();
        log.info("Clicked on element: {}", element);
    }
    
    protected void sendKeys(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
        log.info("Entered text '{}' into element: {}", text, element);
    }
    
    protected String getText(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        String text = element.getText();
        log.info("Retrieved text '{}' from element: {}", text, element);
        return text;
    }
    
    protected boolean isDisplayed(WebElement element) {
        try {
            wait.until(ExpectedConditions.visibilityOf(element));
            return element.isDisplayed();
        } catch (Exception e) {
            log.error("Element is not displayed: {}", e.getMessage());
            return false;
        }
    }
    
    protected void scrollToElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        log.info("Scrolled to element: {}", element);
    }
    
    protected void waitForPageLoad() {
        waitHelper.waitForPageLoad();
    }
    
    /**
     * Finds an element using By locator
     * @param locator By locator
     * @return WebElement
     */
    protected WebElement findElement(By locator) {
        WebElement element = driver.findElement(locator);
        log.debug("Found element using locator: {}", locator);
        return element;
    }
    
    /**
     * Waits for element to be clickable
     * @param locator By locator
     * @return WebElement that is clickable
     */
    protected WebElement waitForElementToBeClickable(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        log.debug("Element is clickable: {}", locator);
        return element;
    }
    
    /**
     * Waits for element visibility with custom timeout
     * @param locator By locator
     * @param timeoutInSeconds Custom timeout in seconds
     * @return WebElement that is visible
     */
    protected WebElement waitForVisibility(By locator, int timeoutInSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        WebElement element = customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        log.debug("Element is visible: {}", locator);
        return element;
    }

    /**
     * Vaadin SPA navigasyonu için 2 saniyelik bekleme.
     * Tüm page object'lerde ortak kullanılır.
     */
    protected void waitForVaadinNavigation() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Vaadin side-nav veya menü öğesine anahtar kelime ile tıklar.
     * Admin/Company/Buyer/Factoring dashboard'larındaki .button-menu butonlarını da kapsar.
     * @param keyword tıklanacak menü öğesinde aranacak metin (küçük harf)
     * @return tıklama başarılıysa true
     */
    protected boolean clickNavItemByText(String keyword) {
        // Try up to 3 times with a brief wait between attempts (page may still be loading)
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                if (attempt > 0) {
                    Thread.sleep(1500);
                }
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var kw = arguments[0].toLowerCase();" +
                    // Priority 1: .button-menu class (V2 dashboard nav)
                    "var priority = document.querySelectorAll('vaadin-button.button-menu, button.button-menu');" +
                    "for (var el of priority) {" +
                    "  var txt = (el.textContent || el.innerText || '').toLowerCase().trim();" +
                    "  if (txt.includes(kw) && txt.length < 60) { el.click(); return true; }" +
                    "}" +
                    // Priority 2: all vaadin-buttons and standard nav elements
                    "var els = document.querySelectorAll(" +
                    "  'vaadin-button, button, vaadin-side-nav-item, a[href], [role=\"menuitem\"]," +
                    "   vaadin-app-layout a, vaadin-tab, [slot=\"navbar\"] a, vaadin-menu-item');" +
                    "for (var el of els) {" +
                    "  var txt = (el.textContent || el.innerText || el.getAttribute('aria-label') || '').toLowerCase().trim();" +
                    "  if (txt.includes(kw) && txt.length < 60 && !el.disabled) { el.click(); return true; }" +
                    "}" +
                    "return false;",
                    keyword
                );
                if (Boolean.TRUE.equals(clicked)) {
                    return true;
                }
            } catch (Exception e) {
                log.debug("clickNavItemByText('{}') attempt {} exception: {}", keyword, attempt, e.getMessage());
            }
        }
        return false;
    }

    /**
     * Vaadin bileşeninin label'ına göre text-field veya text-area değeri atar.
     * Shadow DOM piercing için component.value property'sini kullanır.
     * @param labelText Vaadin bileşeninin label metni (case-insensitive, kısmi eşleşme)
     * @param value     Atanacak değer
     * @return başarılıysa true
     */
    protected boolean setVaadinFieldValue(String labelText, String value) {
        try {
            Boolean result = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var lbl = arguments[0].toLowerCase();" +
                "var val = arguments[1];" +
                "var fields = document.querySelectorAll('vaadin-text-field, vaadin-text-area');" +
                "for (var f of fields) {" +
                "  var fLbl = (f.getAttribute('label') || '').toLowerCase();" +
                "  if (fLbl.includes(lbl)) {" +
                "    f.value = val;" +
                "    f.dispatchEvent(new CustomEvent('value-changed',{bubbles:true,detail:{value:val}}));" +
                "    var inp = null;" +
                "    if (f.shadowRoot) inp = f.shadowRoot.querySelector('input, textarea');" +
                "    if (!inp) inp = f.querySelector('input, textarea');" +
                "    if (inp) {" +
                "        inp.focus();" +
                "        inp.value = val;" +
                "        inp.dispatchEvent(new Event('focus',{bubbles:true}));" +
                "        inp.dispatchEvent(new Event('input',{bubbles:true}));" +
                "        inp.dispatchEvent(new Event('change',{bubbles:true}));" +
                "        inp.dispatchEvent(new Event('blur',{bubbles:true}));" +
                "    }" +
                "    return true;" +
                "  }" +
                "}" +
                "return false;",
                labelText, value
            );
            if (Boolean.TRUE.equals(result)) {
                log.info("Vaadin field '{}' değeri '{}' olarak atandı", labelText, value);
                return true;
            }
            log.warn("Vaadin field '{}' bulunamadı", labelText);
            return false;
        } catch (Exception e) {
            log.debug("setVaadinFieldValue exception: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Herhangi bir vaadin-button veya button'a tam metniyle tıklar (case-insensitive).
     * @param text Tam buton metni
     * @return başarılıysa true
     */
    protected boolean clickButtonByText(String text) {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var kw = arguments[0].trim().toLowerCase();" +
                "var els = document.querySelectorAll('vaadin-button, button');" +
                "for (var el of els) {" +
                "  var txt = (el.textContent || el.innerText || '').trim().toLowerCase();" +
                "  if (txt === kw) { el.click(); return true; }" +
                "}" +
                "return false;",
                text
            );
            if (Boolean.TRUE.equals(clicked)) {
                log.info("Buton tıklandı: '{}'", text);
                return true;
            }
            log.warn("Buton bulunamadı: '{}'", text);
            return false;
        } catch (Exception e) {
            log.debug("clickButtonByText('{}') exception: {}", text, e.getMessage());
            return false;
        }
    }

    /**
     * Mevcut sayfada vaadin-grid görünür mü kontrolü.
     */
    protected boolean isVaadinGridVisible() {
        try {
            List<WebElement> grids = driver.findElements(By.cssSelector("vaadin-grid"));
            return !grids.isEmpty() && grids.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * vaadin-grid görünene kadar bekler (15 sn timeout).
     */
    protected void waitForVaadinGrid() {
        try {
            waitForVisibility(By.cssSelector("vaadin-grid"), 15);
        } catch (Exception e) {
            log.warn("vaadin-grid bekleme timeout: {}", e.getMessage());
        }
    }

    /**
     * Herhangi bir Vaadin notification açık mı kontrol eder.
     */
    protected boolean isAnyNotificationVisible() {
        try {
            Boolean result = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var notifs = document.querySelectorAll('vaadin-notification');" +
                "for (var n of notifs) { if (n.opened) return true; }" +
                "var c = document.querySelector('vaadin-notification-container');" +
                "if (c && c.children.length > 0) return true;" +
                "return false;"
            );
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * vaadin-dialog-overlay açık mı kontrolü.
     * Sadece [opened] attribute'u veya JS .opened property'si ile kontrol edilir.
     * vaadin-dialog-overlay DOM'da kalır (closed olduğunda bile) — isDisplayed() güvenilmez.
     */
    protected boolean isDialogOpen() {
        try {
            // Primary: check for [opened] attribute — most reliable
            List<WebElement> dialogs = driver.findElements(
                    By.cssSelector("vaadin-dialog-overlay[opened]"));
            if (!dialogs.isEmpty()) return true;
            // Secondary: check .opened JS property (Vaadin sometimes uses property not attribute)
            Boolean jsOpen = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var overlays = document.querySelectorAll('vaadin-dialog-overlay');" +
                "for (var o of overlays) { if (o.opened === true) return true; }" +
                "return false;"
            );
            return Boolean.TRUE.equals(jsOpen);
        } catch (Exception e) {
            return false;
        }
    }
}