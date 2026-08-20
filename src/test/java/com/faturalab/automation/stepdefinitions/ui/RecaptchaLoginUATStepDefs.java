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

        Boolean onLoginPage = (Boolean) js.executeScript(
                "var body = document.body.innerText || '';"
                        + "return body.indexOf('GİRİŞ YAP') >= 0;");

        if (!Boolean.TRUE.equals(onLoginPage)) {
            log.info("[RECAPTCHA-LOGIN] Admin login doğrulandı: login ekranından çıkıldı (gerçek başarı).");
            return;
        }

        // ⚠️ BİLİNEN, KABUL EDİLMİŞ SINIR (2026-08-20, build #91'de canlı kanıtlandı): Google
        // reCAPTCHA v3, headless/otomasyon Chrome oturumlarını düşük skorlu/şüpheli buluyor ve
        // V2 görünür checkbox fallback'ine düşürüyor -- bu Selenium'un ÇÖZEBİLECEĞİ bir şey DEĞİL
        // (insan etkileşimi gerektirir). Bu durumda senaryoyu FAIL ETMİYORUZ -- kod/toggle
        // mekanizması doğru çalıştığını (token bekleme + V2 tespiti) zaten kanıtladı, geri kalanı
        // reCAPTCHA'nın kasıtlı bot-tespit davranışı. Bunu her gün "regresyon" gibi raporlayıp
        // gürültü üretmek yerine bilinen limit olarak logluyoruz (OP#5909 kullanıcı kararı).
        //
        // GERÇEK bir regresyonu (fix'in kendisi bozulursa, örn. token hiç gelmezse VE V2 de
        // render olmazsa -- yani RoleSessionManager.performLogin'in kendisi bir şekilde
        // tamamen sessiz kalırsa) yine de yakalamak için ÜÇÜNCÜ bir durumu kontrol ediyoruz.
        Boolean v2Rendered = (Boolean) js.executeScript(
                "var el = document.getElementById('recaptcha');"
                        + "return !!(el && el.innerHTML && el.innerHTML.trim() !== '');");

        if (Boolean.TRUE.equals(v2Rendered)) {
            log.warn("[RECAPTCHA-LOGIN] BİLİNEN SINIR: reCAPTCHA v3, bu CI oturumunu şüpheli bulup "
                    + "V2 görünür checkbox'a düşürdü -- Selenium bunu çözemez (insan etkileşimi "
                    + "gerektirir). Bu bir regresyon DEĞİL; token bekleme + V2 tespiti (kod tarafı) "
                    + "doğru çalıştı. Senaryo bu bilinen sınır nedeniyle PASS sayılıyor.");
            return;
        }

        // Ne gerçek başarı ne V2 render -- token hiç gelmedi VE fallback da tetiklenmedi.
        // Bu GERÇEK bir regresyon sinyali (örn. v3 script hiç yüklenmedi, appsettings okunamadı
        // vb.) -- burada FAIL etmek doğru.
        Assert.fail("Login sonrası hâlâ 'GİRİŞ YAP' ekranındayız AMA V2 fallback de tetiklenmedi -- "
                + "ne bilinen 'v3 bot-tespiti' sınırı ne gerçek başarı. Bu GERÇEK bir regresyon "
                + "olabilir (RoleSessionManager.performLogin veya reCAPTCHA script yükleme "
                + "zincirinde inceleme gerekir).");
    }
}
