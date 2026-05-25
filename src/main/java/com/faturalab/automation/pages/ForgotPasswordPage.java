package com.faturalab.automation.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Şifremi Unuttum ekranı page object.
 * Login ekranından "Şifremi Unuttum" butonuyla ulaşılan ekran.
 */
public class ForgotPasswordPage extends BasePageObject {

    private final By EMAIL_INPUT  = By.cssSelector(
            "vaadin-text-field input, input[type='email'], input[type='text']");
    private final By KURTAR_BTN   = By.xpath(
            "//vaadin-button[normalize-space()='KURTAR'] | " +
            "//vaadin-button[contains(normalize-space(),'KURTAR')] | " +
            "//vaadin-button[contains(normalize-space(),'Gönder')]");
    private final By LOGO         = By.cssSelector(
            "[class*='login_logo'], [class*='logo'], img[alt*='aturalab'], img[alt*='aturalab']");
    private final By NOTIFICATION = By.cssSelector("vaadin-notification-container");
    private final By SUCCESS_MSG  = By.cssSelector(
            "vaadin-notification-container, [class*='success'], [role='alert']");

    public ForgotPasswordPage(WebDriver driver) {
        super(driver);
    }

    // ─── Sayfa Doğrulama ─────────────────────────────────────────────────────

    /**
     * Şifremi Unuttum ekranında olup olmadığını kontrol eder.
     * Şifre girişi yok, yalnızca e-posta alanı + KURTAR butonu beklenir.
     */
    public boolean isForgotPasswordPage() {
        try {
            sleep(1000);
            // Şifre alanı yoksa ve e-posta alanı varsa bu ekranız
            boolean hasEmail    = !driver.findElements(EMAIL_INPUT).isEmpty();
            boolean noPassword  = driver.findElements(
                    By.cssSelector("input[type='password']")).isEmpty();
            boolean hasKurtar   = !driver.findElements(KURTAR_BTN).isEmpty();
            log.info("ForgotPassword ekran kontrolü: email={}, noPassword={}, kurtar={}", hasEmail, noPassword, hasKurtar);
            return hasEmail && (noPassword || hasKurtar);
        } catch (Exception e) {
            log.warn("ForgotPassword ekran kontrolü başarısız: {}", e.getMessage());
            return false;
        }
    }

    // ─── Form Eylemleri ──────────────────────────────────────────────────────

    public void enterEmail(String email) {
        WebElement field = waitForElementToBeClickable(EMAIL_INPUT);
        field.clear();
        field.sendKeys(email);
        log.info("E-posta girildi: {}", email);
    }

    public void clickKurtar() {
        removeRecaptchaOverlay();
        try {
            waitForElementToBeClickable(KURTAR_BTN).click();
        } catch (Exception e) {
            log.warn("KURTAR butonu normal tıklanamadı, shadow JS click: {}", e.getMessage());
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(8))
                        .until(d -> {
                            java.util.List<WebElement> btns = d.findElements(KURTAR_BTN);
                            return btns.isEmpty() ? null : btns.get(0);
                        });
                ((JavascriptExecutor) driver).executeScript(
                        "var sr = arguments[0].shadowRoot;" +
                        "(sr ? sr.querySelector('button') : arguments[0]).click();", btn);
            } catch (Exception ex) {
                log.error("JS KURTAR click başarısız: {}", ex.getMessage());
            }
        }
        log.info("KURTAR butonuna tıklandı.");
        sleep(2000);
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
            // JS fallback: sayfanın sol üstündeki ilk tıklanabilir görseli bul
            log.warn("Logo CSS ile bulunamadı, JS fallback deneniyor: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript(
                    "var candidates = document.querySelectorAll('[class*=\"logo\"], img, svg');" +
                    "for (var i = 0; i < candidates.length; i++) {" +
                    "  var r = candidates[i].getBoundingClientRect();" +
                    "  if (r.top < 150 && r.left < 400 && r.width > 0) { candidates[i].click(); break; }" +
                    "}");
        }
        sleep(2000);
        log.info("Logo tıklandı.");
    }

    // ─── Bildirim / Mesaj ─────────────────────────────────────────────────────

    public boolean isSuccessMessageVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(NOTIFICATION));
            String text = driver.findElement(NOTIFICATION).getText().toLowerCase();
            boolean success = text.contains("gönderildi") || text.contains("mail")
                    || text.contains("e-posta") || text.contains("başarı");
            log.info("Başarı mesajı görünürlük: {}, metin: {}", success, text);
            return success;
        } catch (Exception e) {
            // Bazen bildirim kaybolabilir; sayfa kaynağında ara
            String src = driver.getPageSource().toLowerCase();
            boolean found = src.contains("gönderildi") || src.contains("mail gönderildi")
                    || src.contains("e-posta gönderildi");
            log.info("Başarı mesajı sayfa kaynağında: {}", found);
            return found;
        }
    }

    public boolean isInvalidCaptchaErrorVisible() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions.visibilityOfElementLocated(NOTIFICATION));
            String text = driver.findElement(NOTIFICATION).getText().toLowerCase();
            boolean error = text.contains("captcha") || text.contains("recaptcha")
                    || text.contains("geçersiz") || text.contains("doğrula");
            log.info("Geçersiz reCaptcha hatası görünürlük: {}, metin: {}", error, text);
            return error;
        } catch (Exception e) {
            String src = driver.getPageSource().toLowerCase();
            boolean found = src.contains("geçersiz recaptcha") || src.contains("invalid recaptcha")
                    || src.contains("captcha") && src.contains("geçersiz");
            log.info("Geçersiz reCaptcha sayfa kaynağında: {}", found);
            return found;
        }
    }

    // ─── reCAPTCHA — LoginPage ile aynı mantık ───────────────────────────────

    public boolean isV3BadgePresent() {
        try {
            sleep(1000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Object r1 = js.executeScript(
                    "var b = document.querySelector('.grecaptcha-badge');" +
                    "if (!b) return false;" +
                    "var s = window.getComputedStyle(b);" +
                    "return s.display !== 'none' && s.visibility !== 'hidden';");
            if (Boolean.TRUE.equals(r1)) return true;
            Object r2 = js.executeScript(
                    "return document.querySelectorAll('iframe[src*=\"recaptcha/api2/anchor\"]').length > 0;");
            return Boolean.TRUE.equals(r2);
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
            log.info("ForgotPassword V2 checkbox tıklandı.");
            return true;
        } catch (Exception e) {
            log.warn("ForgotPassword V2 checkbox başarısız: {}", e.getMessage());
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
