package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.context.RoleSessionManager;
import com.faturalab.automation.context.RoleSessionManager.Role;
import com.faturalab.automation.db.CaptchaDbAssertions;
import com.faturalab.automation.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

/**
 * RECAPTCHA-LOGIN-001 — reCAPTCHA aktifken admin login doğrulaması (OP#5909, OP#5255).
 *
 * ⚠️ {@code appsettings.CAPTCHA_ENABLED} GLOBAL bir ayardır — açık kalırsa diğer tüm günlük UAT
 * senaryolarının (TZF/DTS/DTF) her login'i reCAPTCHA token beklemesiyle yavaşlar. Bu yüzden
 * {@link DtfIslemUATStepDefs}'teki {@code requireddtf} @Before/@After toggle deseniyle AYNI
 * güvenlik ilkesi uygulanır: sadece bu senaryonun süresi boyunca OPEN, PASS/FAIL fark etmeden
 * @After'da tekrar CLOSED'a döner. {@code parallel=false} olduğu için (Vaadin SPA kısıtı, bkz.
 * web-automation.md) diğer senaryolarla eş zamanlı çalışma riski yoktur.
 */
public class RecaptchaLoginUATStepDefs {

    private static final Logger log = LogManager.getLogger(RecaptchaLoginUATStepDefs.class);

    @Before("@recaptcha-login")
    public void captchaGeciciAcilir() {
        CaptchaDbAssertions.setCaptchaEnabled(true);
        log.info("[RECAPTCHA-LOGIN] appsettings.CAPTCHA_ENABLED=OPEN (senaryo süresince, @After'da geri alınacak)");
    }

    @After("@recaptcha-login")
    public void captchaGeriKapatilir() {
        CaptchaDbAssertions.setCaptchaEnabled(false);
        log.info("[RECAPTCHA-LOGIN] appsettings.CAPTCHA_ENABLED=CLOSED'a geri alındı — diğer günlük UAT "
                + "senaryolarının (TZF/DTS/DTF) normal hızlı login akışı korunuyor.");
    }

    @When("admin reCAPTCHA aktifken login olur")
    public void adminRecaptchaAktifkenLoginOlur() {
        // RoleSessionManager.loginAs, role==ADMIN olduğunda sadece admin login'i yapar
        // (performLogin -> reCAPTCHA v3 token bekleme/retry/V2-fallback teşhisi dahil) ve
        // oturumu kaydedip döner -- impersonation adımına geçmez. Bu, tam olarak bu senaryonun
        // test etmek istediği yüzeydir.
        WebDriver driver = DriverManager.getDriver();
        RoleSessionManager.loginAs(driver, Role.ADMIN, null, null);
    }

    @Then("admin dashboard'a başarıyla ulaşır")
    public void adminDashboardaBasariylaUlasir() {
        WebDriver driver = DriverManager.getDriver();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Pozitif doğrulama (fix-verification.md ilkesi): "hata yok" yetmez -- gerçekten
        // login ekranından çıkıldığını VE reCAPTCHA'nın (varsa) çözüldüğünü kanıtla.
        Boolean onLoginPage = (Boolean) js.executeScript(
                "var body = document.body.innerText || '';"
                        + "return body.indexOf('GİRİŞ YAP') >= 0;");
        Assert.assertFalse(Boolean.TRUE.equals(onLoginPage),
                "Login sonrası hâlâ 'GİRİŞ YAP' ekranındayız -- reCAPTCHA login akışı başarısız oldu.");

        Boolean v2StillVisible = (Boolean) js.executeScript(
                "var el = document.getElementById('recaptcha');"
                        + "return !!(el && el.innerHTML && el.innerHTML.trim() !== '');");
        Assert.assertFalse(Boolean.TRUE.equals(v2StillVisible),
                "V2 reCAPTCHA checkbox widget'i hâlâ görünür -- login reCAPTCHA'da tıkanmış, "
                        + "Selenium bunu çözemez.");

        log.info("[RECAPTCHA-LOGIN] Admin login doğrulandı: login ekranından çıkıldı, V2 fallback'e "
                + "sıkışılmadı.");
    }
}
