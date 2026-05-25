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
     * Navigates to a sidebar/nav item using JavaScript textContent search.
     * Works with Vaadin 24 shadow DOM where XPath text() queries may miss slotted content.
     *
     * @param textKeyword ASCII keyword to match (case-insensitive)
     * @return true if a navigation item was found and clicked
     */
    protected boolean clickNavItemByText(String textKeyword) {
        // Önce mevcut nav item listesini logla (debug)
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object navItems = js.executeScript(
                "var sel = 'vaadin-side-nav-item, vaadin-tab, a[href]';" +
                "return Array.from(document.querySelectorAll(sel))" +
                ".map(e => e.textContent.trim().replace(/\\s+/g,' ')).filter(t=>t.length>0).join(' | ');"
            );
            log.info("Nav items (keyword='{}') : {}", textKeyword, navItems);
        } catch (Exception ignored) {}

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Boolean shadowHit = (Boolean) js.executeScript(
                    "var kw = arguments[0].toLowerCase();" +
                    "function walk(node, depth) {" +
                    "  if (!node || depth > 30) return false;" +
                    "  if (node.shadowRoot && walk(node.shadowRoot, depth + 1)) return true;" +
                    "  var tag = (node.tagName || '').toLowerCase();" +
                    "  if (tag === 'vaadin-side-nav-item' || tag === 'a' || tag === 'vaadin-tab') {" +
                    "    var txt = (node.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "    if (txt.length && txt.length < 400 && txt.includes(kw)) {" +
                    "      try { node.click(); return true; } catch (e) {}" +
                    "    }" +
                    "  }" +
                    "  var ch = node.children;" +
                    "  if (ch) for (var i = 0; i < ch.length; i++) {" +
                    "    if (walk(ch[i], depth + 1)) return true;" +
                    "  }" +
                    "  return false;" +
                    "}" +
                    "return walk(document.body, 0);",
                    textKeyword);
            if (Boolean.TRUE.equals(shadowHit)) {
                log.info("Shadow/depth-first nav clicked for keyword: '{}'", textKeyword);
                return true;
            }
        } catch (Exception e) {
            log.debug("Shadow nav attempt failed: {}", e.getMessage());
        }

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Boolean found = (Boolean) js.executeScript(
                "var kw = arguments[0].toLowerCase();" +
                "var sel = 'vaadin-side-nav-item, vaadin-tab, a[href], [role=\"menuitem\"], [role=\"option\"]';" +
                "var els = Array.from(document.querySelectorAll(sel));" +
                "for (var i = 0; i < els.length; i++) {" +
                "  var txt = (els[i].textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                "  if (txt.includes(kw)) { els[i].click(); return true; }" +
                "}" +
                "return false;",
                textKeyword
            );
            if (Boolean.TRUE.equals(found)) {
                log.info("JS nav clicked for keyword: '{}'", textKeyword);
                return true;
            }
        } catch (Exception e) {
            log.debug("JS nav attempt failed: {}", e.getMessage());
        }
        // Debug: mevcut URL ve a[href] linklerini logla (nav bulunamadı)
        try {
            log.info("Nav bulunamadi (keyword='{}'). URL: {}", textKeyword, driver.getCurrentUrl());
            Object links = ((JavascriptExecutor) driver).executeScript(
                "return Array.from(document.querySelectorAll('a[href]'))" +
                ".map(a => '[' + a.textContent.trim().substring(0,25) + ']=' + a.href)" +
                ".filter(s=>s.length>5).slice(0,15).join(' || ');"
            );
            log.info("Sayfadaki linkler: {}", links);
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Dar görünümde yan menü kapalıysa drawer / navbar toggle ile açmayı dener.
     */
    protected void tryOpenNavigationDrawer() {
        try {
            Boolean toggled = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function tryClick(list) {" +
                    "  for (var i = 0; i < list.length; i++) {" +
                    "    var e = list[i];" +
                    "    var r = e.getBoundingClientRect();" +
                    "    if (r.width < 2 || r.height < 2) continue;" +
                    "    try { e.click(); return true; } catch (ex) {}" +
                    "  }" +
                    "  return false;" +
                    "}" +
                    "var a = document.querySelectorAll('vaadin-drawer-toggle, [slot=\\\"navbar\\\"] button, " +
                    "vaadin-button[aria-label*=\"menu\" i]');" +
                    "if (tryClick(a)) return true;" +
                    "var b = document.querySelectorAll('[part=\\\"navbar\\\"] vaadin-button, header vaadin-button');" +
                    "if (tryClick(b)) return true;" +
                    "return false;");
            if (Boolean.TRUE.equals(toggled)) {
                Thread.sleep(900);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.debug("tryOpenNavigationDrawer: {}", e.getMessage());
        }
    }

    /**
     * Waits for Vaadin client-side navigation to settle.
     */
    protected void waitForVaadinNavigation() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            wait.until(d -> "complete".equals(js.executeScript("return document.readyState")));
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.debug("waitForVaadinNavigation: {}", e.getMessage());
        }
    }

    /**
     * Açıksa Vaadin onay diyalogunda (ör. "güncellemek istediğinizden emin misiniz") olumlu yanıtı tıklar.
     * Üst üste birden fazla onay penceresi varsa birkaç kez dener.
     */
    public void acceptVaadinConfirmDialogIfPresent() {
        for (int round = 0; round < 5; round++) {
            try {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "var neg = ['iptal','hayır','hayir','vazgeç','vazgec','cancel','no'];" +
                        "var pos = ['evet','tamam','onayla','devam','yes','ok','confirm'];" +
                        "function visible(el) {" +
                        "  if (!el) return false;" +
                        "  var tag0 = (el.tagName || '').toLowerCase();" +
                        "  if (tag0 === 'vaadin-confirm-dialog') return true;" +
                        "  var cs = window.getComputedStyle(el);" +
                        "  if (cs.display === 'none' || cs.visibility === 'hidden') return false;" +
                        "  var r = el.getBoundingClientRect();" +
                        "  return r.width > 2 || r.height > 2;" +
                        "}" +
                        "function skipLabel(t) {" +
                        "  t = (t || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                        "  for (var i = 0; i < neg.length; i++) {" +
                        "    if (t === neg[i] || t.indexOf(neg[i] + ' ') === 0) return true;" +
                        "  }" +
                        "  return false;" +
                        "}" +
                        "function matchPos(t) {" +
                        "  t = (t || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                        "  for (var j = 0; j < pos.length; j++) {" +
                        "    if (t === pos[j] || t.indexOf(pos[j] + ' ') === 0) return true;" +
                        "  }" +
                        "  return false;" +
                        "}" +
                        "function themePrimary(n) {" +
                        "  var th = (n.getAttribute && n.getAttribute('theme')) || '';" +
                        "  th = th.toLowerCase();" +
                        "  return th.indexOf('primary') >= 0 && th.indexOf('tertiary') < 0 && th.indexOf('contrast') < 0;" +
                        "}" +
                        "function clickInSubtree(root) {" +
                        "  if (!root || !visible(root)) return false;" +
                        "  var dlgText = (root.textContent || '').toLowerCase();" +
                        "  var needPrimary = dlgText.indexOf('emin') >= 0 || dlgText.indexOf('güncelle') >= 0 || " +
                        "      dlgText.indexOf('guencelle') >= 0 || dlgText.indexOf('kullanıcı') >= 0 || " +
                        "      dlgText.indexOf('kullanici') >= 0;" +
                        "  var stack = [root];" +
                        "  while (stack.length) {" +
                        "    var n = stack.pop();" +
                        "    if (!n || n.nodeType !== 1) continue;" +
                        "    var tag = (n.tagName || '').toLowerCase();" +
                        "    if ((tag === 'vaadin-button' || tag === 'button') && !n.disabled) {" +
                        "      var t = (n.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                        "      if (!t && n.shadowRoot) {" +
                        "        t = (n.shadowRoot.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                        "      }" +
                        "      if (t && !skipLabel(t) && matchPos(t)) { n.click(); return true; }" +
                        "    }" +
                        "    if (n.shadowRoot) stack.push(n.shadowRoot);" +
                        "    var ch = n.children;" +
                        "    if (ch) for (var k = ch.length - 1; k >= 0; k--) stack.push(ch[k]);" +
                        "  }" +
                        "  if (!needPrimary) return false;" +
                        "  stack = [root];" +
                        "  while (stack.length) {" +
                        "    var n2 = stack.pop();" +
                        "    if (!n2 || n2.nodeType !== 1) continue;" +
                        "    var tag2 = (n2.tagName || '').toLowerCase();" +
                        "    if (tag2 === 'vaadin-button' && !n2.disabled && themePrimary(n2)) {" +
                        "      var tx = n2.textContent || '';" +
                        "      if (!tx && n2.shadowRoot) tx = n2.shadowRoot.textContent || '';" +
                        "      if (!skipLabel(tx)) { n2.click(); return true; }" +
                        "    }" +
                        "    if (n2.shadowRoot) stack.push(n2.shadowRoot);" +
                        "    var ch2 = n2.children;" +
                        "    if (ch2) for (var k2 = ch2.length - 1; k2 >= 0; k2--) stack.push(ch2[k2]);" +
                        "  }" +
                        "  return false;" +
                        "}" +
                        "var hostSel = 'vaadin-dialog-overlay, vaadin-confirm-dialog-overlay, vaadin-confirm-dialog';" +
                        "var hosts = document.querySelectorAll(hostSel);" +
                        "for (var h = 0; h < hosts.length; h++) {" +
                        "  if (clickInSubtree(hosts[h])) return true;" +
                        "}" +
                        "var cds = document.querySelectorAll('vaadin-confirm-dialog[opened]');" +
                        "for (var c = 0; c < cds.length; c++) {" +
                        "  if (clickInSubtree(cds[c])) return true;" +
                        "}" +
                        "return false;");
                if (!Boolean.TRUE.equals(clicked)) {
                    return;
                }
                log.info("Vaadin onay diyalogu onaylandi (tur {}).", round + 1);
                waitForVaadinNavigation();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.debug("acceptVaadinConfirmDialogIfPresent: {}", e.getMessage());
                return;
            }
        }
    }
}
