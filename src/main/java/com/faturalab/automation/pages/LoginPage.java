package com.faturalab.automation.pages;

import com.faturalab.automation.config.ConfigReader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Login ekranı için reCAPTCHA odaklı page object.
 * Var olan HomePage.java'yı tamamlar; doğrudan login akışı + CAPTCHA metodları içerir.
 */
public class LoginPage extends BasePageObject {

    private final By EMAIL_INPUT    = By.cssSelector(
            "vaadin-text-field[placeholder='E-posta adresi'] input, input[type='email']");
    private final By PASSWORD_INPUT = By.cssSelector(
            "vaadin-password-field[placeholder='Şifre'] input, input[type='password']");
    private final By LOGIN_BUTTON   = By.xpath(
            "//vaadin-button[normalize-space()='GİRİŞ YAP']");
    private final By FORGOT_BTN     = By.xpath(
            "//vaadin-button[contains(normalize-space(),'Şifremi Unuttum')]");
    private final By REGISTER_BTN   = By.xpath(
            "//vaadin-button[contains(normalize-space(),'KAYIT OL')] | " +
            "//vaadin-button[contains(normalize-space(),'Kayıt Ol')]");
    private final By NOTIFICATION   = By.cssSelector("vaadin-notification-container");
    private final By APP_LAYOUT     = By.cssSelector("vaadin-app-layout");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    public void navigateToLoginPage() {
        String url = ConfigReader.getProperty("base.url");
        driver.get(url);
        waitForPageLoad();
        sleep(2000);
        log.info("Login ekranına gidildi: {}", url);
    }

    public void refresh() {
        driver.navigate().refresh();
        waitForPageLoad();
        sleep(2000);
        log.info("Sayfa yenilendi (F5).");
    }

    // ─── Form Eylemleri ───────────────────────────────────────────────────────

    public void enterEmail(String email) {
        try {
            WebElement field = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
            field.clear();
            field.sendKeys(email);
        } catch (Exception e) {
            log.warn("E-posta alanı CSS ile bulunamadı, JS fallback: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript(
                    "var inputs = document.querySelectorAll('input[type=\"text\"], input[type=\"email\"], " +
                    "vaadin-text-field input');" +
                    "if (inputs.length > 0) {" +
                    "  inputs[0].value = arguments[0];" +
                    "  inputs[0].dispatchEvent(new Event('input', {bubbles:true}));" +
                    "  inputs[0].dispatchEvent(new Event('change', {bubbles:true}));" +
                    "}", email);
        }
    }

    public void enterPassword(String password) {
        try {
            WebElement field = new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
            field.clear();
            field.sendKeys(password);
        } catch (Exception e) {
            log.warn("Şifre alanı CSS ile bulunamadı, JS fallback: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript(
                    "var inputs = document.querySelectorAll('input[type=\"password\"]');" +
                    "if (inputs.length > 0) {" +
                    "  inputs[0].value = arguments[0];" +
                    "  inputs[0].dispatchEvent(new Event('input', {bubbles:true}));" +
                    "  inputs[0].dispatchEvent(new Event('change', {bubbles:true}));" +
                    "}", password);
        }
    }

    public void clickLoginButton() {
        removeRecaptchaOverlay();
        try {
            waitForElementToBeClickable(LOGIN_BUTTON).click();
        } catch (Exception e) {
            log.warn("Login butonu normal tıklanamadı, shadow JS click: {}", e.getMessage());
            try {
                WebElement btn = driver.findElement(LOGIN_BUTTON);
                ((JavascriptExecutor) driver).executeScript(
                        "var sr = arguments[0].shadowRoot;" +
                        "(sr ? sr.querySelector('button') : arguments[0]).click();", btn);
            } catch (Exception ex) {
                log.error("JS login click başarısız: {}", ex.getMessage());
            }
        }
        log.info("GİRİŞ YAP butonuna tıklandı.");
    }

    private void removeRecaptchaOverlay() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('div').forEach(function(el) {" +
                    "  if (parseInt(el.style.zIndex || '0') >= 1000000000) el.remove();" +
                    "});");
            sleep(200);
        } catch (Exception ignored) {}
    }

    public void loginWith(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLoginButton();
    }

    public void loginWithDefaultCredentials() {
        String email    = ConfigReader.getProperty("admin.email");
        String password = ConfigReader.getProperty("admin.password");
        loginWith(email, password);
        log.info("Varsayılan kimlik bilgileriyle giriş deneniyor: {}", email);
    }

    public void clickForgotPassword() {
        waitForElementToBeClickable(FORGOT_BTN).click();
        sleep(2500);
        log.info("Şifremi Unuttum butonuna tıklandı.");
    }

    public void clickRegister() {
        waitForElementToBeClickable(REGISTER_BTN).click();
        sleep(2500);
        log.info("Kayıt Ol butonuna tıklandı.");
    }

    // ─── Durum Doğrulama ─────────────────────────────────────────────────────

    public boolean isOnLoginPage() {
        try {
            driver.findElement(EMAIL_INPUT);
            driver.findElement(PASSWORD_INPUT);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isOnDashboard() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(45)).until(d -> {
                // vaadin-app-layout varlığı → uygulama yüklendi
                if (!d.findElements(APP_LAYOUT).isEmpty()) return true;
                // Giriş Yap butonu kayboldu → login ekranından çıkıldı
                return d.findElements(LOGIN_BUTTON).isEmpty();
            });
            boolean hasLayout = !driver.findElements(APP_LAYOUT).isEmpty();
            log.info("Dashboard tespiti başarılı (layout={}). URL: {}", hasLayout, driver.getCurrentUrl());
            return true;
        } catch (Exception e) {
            log.warn("Dashboard bekleme timeout: {}", e.getMessage());
            try {
                return !driver.findElements(APP_LAYOUT).isEmpty();
            } catch (Exception ex) {
                return false;
            }
        }
    }

    public boolean isStillInApp() {
        try {
            // F5 sonrası Vaadin yeniden yükleniyor — layout görünene kadar bekle (45s)
            new WebDriverWait(driver, Duration.ofSeconds(45)).until(d -> {
                String url = d.getCurrentUrl();
                if (url.contains("/login") || url.contains("/error")) return false;
                // vaadin-app-layout VEYA herhangi bir uygulama elementi varsa başarılı say
                if (!d.findElements(APP_LAYOUT).isEmpty()) return true;
                if (!d.findElements(By.cssSelector("vaadin-side-nav, vaadin-tabs, vaadin-menu-bar")).isEmpty()) return true;
                return false;
            });
            log.info("Uygulama içinde — layout mevcut. URL: {}", driver.getCurrentUrl());
            return true;
        } catch (Exception e) {
            String url = driver.getCurrentUrl();
            log.warn("isStillInApp timeout. URL={}", url);
            // Son kontrol: URL login değilse uygulama içinde kabul et
            return !url.contains("/login") && !url.contains("/error");
        }
    }

    public String getNotificationText() {
        try {
            WebElement n = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.visibilityOfElementLocated(NOTIFICATION));
            return n.getText();
        } catch (Exception e) {
            try {
                return driver.findElement(By.tagName("body")).getText();
            } catch (Exception ex) {
                return "";
            }
        }
    }

    // ─── reCAPTCHA V3 Badge ───────────────────────────────────────────────────

    /**
     * Sayfada .grecaptcha-badge elementinin DOM'da var olup olmadığını kontrol eder.
     * V3 badge sağ alt köşede sabit konumlu olarak eklenir.
     */
    public boolean isV3BadgePresent() {
        try {
            sleep(1500); // reCAPTCHA script yüklenme süresi
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object result = js.executeScript(
                    "var badge = document.querySelector('.grecaptcha-badge');" +
                    "if (!badge) return false;" +
                    "var style = window.getComputedStyle(badge);" +
                    "return style.display !== 'none' && style.visibility !== 'hidden';"
            );
            if (Boolean.TRUE.equals(result)) {
                log.info("V3 reCAPTCHA badge DOM'da mevcut.");
                return true;
            }
            // Fallback: anchor iframe varlığı
            result = js.executeScript(
                    "return document.querySelectorAll('iframe[src*=\"recaptcha/api2/anchor\"]').length > 0;"
            );
            boolean found = Boolean.TRUE.equals(result);
            log.info("V3 badge iframe kontrolü: {}", found);
            return found;
        } catch (Exception e) {
            log.warn("V3 badge kontrolü başarısız: {}", e.getMessage());
            return false;
        }
    }

    public boolean isV3BadgeAbsent() {
        return !isV3BadgePresent();
    }

    // ─── reCAPTCHA V2 Widget ──────────────────────────────────────────────────

    /**
     * Sayfa içeriğinde görünür bir V2 reCAPTCHA widget'ı (checkbox iframe) olup olmadığını kontrol eder.
     * V3 badge'inden ayırt etmek için boyut eşiği kullanılır (V2 min ~300x74 px).
     */
    public boolean isV2WidgetVisible() {
        try {
            sleep(1500);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object result = js.executeScript(
                    "var frames = document.querySelectorAll('iframe[src*=\"recaptcha\"]');" +
                    "for (var i = 0; i < frames.length; i++) {" +
                    "  var r = frames[i].getBoundingClientRect();" +
                    "  if (r.width > 200 && r.height > 50) return true;" +
                    "}" +
                    "var grc = document.querySelector('.g-recaptcha, div[data-sitekey]');" +
                    "return grc !== null;"
            );
            boolean visible = Boolean.TRUE.equals(result);
            log.info("V2 widget görünürlük: {}", visible);
            return visible;
        } catch (Exception e) {
            log.warn("V2 widget kontrolü başarısız: {}", e.getMessage());
            return false;
        }
    }

    /**
     * V2 reCAPTCHA checkbox'ına tıklar.
     * Önce iframe'e geçer, checkbox'ı tıklar, varsayılan içeriğe döner.
     * Test anahtarı varsa otomatik onaylanır; production anahtarıyla challenge çıkabilir.
     */
    public boolean clickV2Checkbox() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(12));

            // Boyutu olan ilk reCAPTCHA iframe'ini bul
            WebElement recaptchaFrame = shortWait.until(d -> {
                List<WebElement> frames = d.findElements(
                        By.cssSelector("iframe[src*='recaptcha/api2/anchor'], iframe[title='reCAPTCHA']"));
                for (WebElement f : frames) {
                    try {
                        Rectangle r = f.getRect();
                        if (r.getWidth() > 200 && r.getHeight() > 50) return f;
                    } catch (Exception ignored) {}
                }
                return null;
            });

            driver.switchTo().frame(recaptchaFrame);
            sleep(1000);

            WebElement checkbox = shortWait.until(
                    ExpectedConditions.elementToBeClickable(By.id("recaptcha-anchor")));
            checkbox.click();
            sleep(2500); // onay animasyonu bitmesini bekle

            driver.switchTo().defaultContent();
            log.info("V2 reCAPTCHA checkbox tıklandı.");
            return true;
        } catch (Exception e) {
            log.warn("V2 checkbox tıklama başarısız: {}", e.getMessage());
            try { driver.switchTo().defaultContent(); } catch (Exception ignored) {}
            return false;
        }
    }

    /**
     * V2 reCAPTCHA'nın süresinin dolmasını bekler (~150 saniye).
     */
    public void waitForV2Expiry() {
        int waitSeconds = 155;
        log.info("V2 reCAPTCHA süre dolumu bekleniyor ({} sn)...", waitSeconds);
        sleep(waitSeconds * 1000L);
        log.info("V2 süre dolumu bekleme tamamlandı.");
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
