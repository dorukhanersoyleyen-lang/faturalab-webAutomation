package com.faturalab.automation.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Kayıt Ol ekranı page object.
 * Login ekranından "KAYIT OL" butonuyla ulaşılan ekran.
 */
public class RegisterPage extends BasePageObject {

    private final By KAYIT_OL_BTN = By.xpath(
            "//vaadin-button[normalize-space()='KAYIT OL'] | " +
            "//vaadin-button[contains(normalize-space(),'KAYIT OL')]");
    private final By LOGO         = By.cssSelector(
            "[class*='login_logo'], [class*='logo'], img[alt*='aturalab']");
    private final By NOTIFICATION = By.cssSelector("vaadin-notification-container");

    // Form alanları — placeholder eşleşmesi yoksa index ile fallback
    private final By AD_INPUT       = By.cssSelector("vaadin-text-field[placeholder='Ad'] input");
    private final By SOYAD_INPUT    = By.cssSelector("vaadin-text-field[placeholder='Soyad'] input");
    private final By EMAIL_INPUT    = By.cssSelector(
            "vaadin-text-field[placeholder='E-posta'] input, vaadin-text-field[placeholder='E-posta adresi'] input");
    private final By VERGI_INPUT    = By.cssSelector(
            "vaadin-text-field[placeholder='Vergi Numarası'] input, " +
            "vaadin-text-field[placeholder='VKN'] input, " +
            "vaadin-text-field[placeholder='Vergi No'] input");
    private final By SIFRE_INPUT    = By.cssSelector(
            "vaadin-password-field[placeholder='Şifre'] input");
    private final By SIFRE2_INPUT   = By.cssSelector(
            "vaadin-password-field[placeholder='Şifre Tekrar'] input, " +
            "vaadin-password-field[placeholder='Şifre Onay'] input");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    // ─── Sayfa Doğrulama ─────────────────────────────────────────────────────

    public boolean isRegisterPage() {
        try {
            sleep(1000);
            boolean hasKayitBtn = !driver.findElements(KAYIT_OL_BTN).isEmpty();
            // Login sayfasında da KAYIT OL butonu olabilir; burada şifre alanlarını da kontrol et
            boolean hasForm = !driver.findElements(By.cssSelector("vaadin-form-layout, vaadin-text-field")).isEmpty();
            log.info("RegisterPage kontrolü: kayitBtn={}, form={}", hasKayitBtn, hasForm);
            return hasKayitBtn && hasForm;
        } catch (Exception e) {
            log.warn("RegisterPage kontrolü başarısız: {}", e.getMessage());
            return false;
        }
    }

    public boolean isEmailVerificationPage() {
        try {
            sleep(2000);
            String url = driver.getCurrentUrl().toLowerCase();
            boolean urlMatch = url.contains("verify") || url.contains("dogrula") || url.contains("onay");
            if (urlMatch) {
                log.info("E-posta doğrulama sayfası URL: {}", driver.getCurrentUrl());
                return true;
            }
            // İçerik kontrolü
            String src = driver.getPageSource().toLowerCase();
            boolean contentMatch = src.contains("e-posta") && (src.contains("doğrula") || src.contains("verify"))
                    || src.contains("aktivasyon");
            log.info("E-posta doğrulama sayfası içerik eşleşmesi: {}", contentMatch);
            return contentMatch;
        } catch (Exception e) {
            log.warn("EmailVerification sayfası kontrolü başarısız: {}", e.getMessage());
            return false;
        }
    }

    // ─── Form Doldurma ────────────────────────────────────────────────────────

    /**
     * Kayıt formunu test verisiyle doldurur.
     * Zaman damgalı e-posta ile unique kayıt oluşturulur.
     */
    public void fillRegistrationForm() {
        long ts = System.currentTimeMillis();
        String email = "autotest_" + ts + "@test-otomasyon.com";
        fillField(AD_INPUT,     "Test");
        fillField(SOYAD_INPUT,  "Otomasyon");
        fillField(EMAIL_INPUT,  email);
        fillField(VERGI_INPUT,  "1234567890");
        fillField(SIFRE_INPUT,  "TestAuto.1");
        fillField(SIFRE2_INPUT, "TestAuto.1");
        log.info("Kayıt formu dolduruldu. E-posta: {}", email);
    }

    private void fillField(By locator, String value) {
        try {
            WebElement el = new WebDriverWait(driver, Duration.ofSeconds(6))
                    .until(ExpectedConditions.elementToBeClickable(locator));
            el.clear();
            el.sendKeys(value);
        } catch (Exception e) {
            log.debug("Alan doldurulamadı (locator={}): {}", locator, e.getMessage());
        }
    }

    public void clickKayitOl() {
        removeRecaptchaOverlay();
        try {
            waitForElementToBeClickable(KAYIT_OL_BTN).click();
        } catch (Exception e) {
            log.warn("KAYIT OL butonu normal tıklanamadı, shadow JS click: {}", e.getMessage());
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(8))
                        .until(d -> {
                            java.util.List<WebElement> btns = d.findElements(KAYIT_OL_BTN);
                            return btns.isEmpty() ? null : btns.get(0);
                        });
                ((JavascriptExecutor) driver).executeScript(
                        "var sr = arguments[0].shadowRoot;" +
                        "(sr ? sr.querySelector('button') : arguments[0]).click();", btn);
            } catch (Exception ex) {
                log.error("JS KAYIT OL click başarısız: {}", ex.getMessage());
            }
        }
        log.info("KAYIT OL butonuna tıklandı.");
        sleep(2500);
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

    public void clickLogo() {
        try {
            WebElement logo = new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(LOGO));
            logo.click();
        } catch (Exception e) {
            log.warn("Logo CSS bulunamadı, JS fallback: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript(
                    "var c = document.querySelectorAll('[class*=\"logo\"], img, svg');" +
                    "for (var i = 0; i < c.length; i++) {" +
                    "  var r = c[i].getBoundingClientRect();" +
                    "  if (r.top < 150 && r.left < 400 && r.width > 0) { c[i].click(); break; }" +
                    "}");
        }
        sleep(2000);
        log.info("Logo tıklandı.");
    }

    // ─── Bildirim / Hata ─────────────────────────────────────────────────────

    public boolean isCaptchaErrorVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.visibilityOfElementLocated(NOTIFICATION));
            String text = driver.findElement(NOTIFICATION).getText().toLowerCase();
            boolean error = text.contains("captcha") || text.contains("doğrula");
            log.info("Captcha hatası görünürlük: {}, metin: {}", error, text);
            return error;
        } catch (Exception e) {
            String src = driver.getPageSource().toLowerCase();
            boolean found = src.contains("captcha") && (src.contains("doğrula") || src.contains("lütfen"));
            log.info("Captcha hatası sayfa kaynağında: {}", found);
            return found;
        }
    }

    // ─── reCAPTCHA — ortak mantık ─────────────────────────────────────────────

    public boolean isV3BadgePresent() {
        try {
            sleep(1000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object r = js.executeScript(
                    "var b = document.querySelector('.grecaptcha-badge');" +
                    "if (!b) return false;" +
                    "var s = window.getComputedStyle(b);" +
                    "return s.display !== 'none' && s.visibility !== 'hidden';");
            if (Boolean.TRUE.equals(r)) return true;
            r = js.executeScript(
                    "return document.querySelectorAll('iframe[src*=\"recaptcha/api2/anchor\"]').length > 0;");
            return Boolean.TRUE.equals(r);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isV2WidgetVisible() {
        try {
            sleep(1000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object r = js.executeScript(
                    "var fs = document.querySelectorAll('iframe[src*=\"recaptcha\"]');" +
                    "for (var i = 0; i < fs.length; i++) {" +
                    "  var rect = fs[i].getBoundingClientRect();" +
                    "  if (rect.width > 200 && rect.height > 50) return true;" +
                    "}" +
                    "return document.querySelector('.g-recaptcha, div[data-sitekey]') !== null;");
            return Boolean.TRUE.equals(r);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean clickV2Checkbox() {
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
            WebElement frame = w.until(d -> {
                for (WebElement f : d.findElements(
                        By.cssSelector("iframe[src*='recaptcha/api2/anchor'], iframe[title='reCAPTCHA']"))) {
                    try {
                        Rectangle r = f.getRect();
                        if (r.getWidth() > 200 && r.getHeight() > 50) return f;
                    } catch (Exception ignored) {}
                }
                return null;
            });
            driver.switchTo().frame(frame);
            sleep(800);
            w.until(ExpectedConditions.elementToBeClickable(By.id("recaptcha-anchor"))).click();
            sleep(2500);
            driver.switchTo().defaultContent();
            log.info("RegisterPage V2 checkbox tıklandı.");
            return true;
        } catch (Exception e) {
            log.warn("RegisterPage V2 checkbox başarısız: {}", e.getMessage());
            try { driver.switchTo().defaultContent(); } catch (Exception ignored) {}
            return false;
        }
    }

    public void waitForV2Expiry() {
        int sec = 155;
        log.info("V2 süre dolumu bekleniyor ({} sn)...", sec);
        sleep(sec * 1000L);
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
