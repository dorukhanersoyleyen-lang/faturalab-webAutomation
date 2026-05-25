package com.faturalab.automation.context;

import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cookie tabanlı çok-rol oturum yöneticisi.
 *
 * Strateji:
 *  1. Her rol için ilk kez login yap → cookie'leri kaydet.
 *  2. Rol geçişinde → tüm cookie'leri sil, kayıtlı cookie'leri yükle, sayfayı yenile.
 *  3. Senaryo bitişinde clearAllSessions() çağır.
 *
 * Kullanım örneği:
 *  RoleSessionManager.loginAs(driver, Role.COMPANY,   "efg@test.com",  "pass");
 *  RoleSessionManager.loginAs(driver, Role.BUYER,     "albc@test.com", "pass");
 *  RoleSessionManager.switchToRole(driver, Role.COMPANY);
 *  // ... company işlemleri ...
 *  RoleSessionManager.switchToRole(driver, Role.BUYER);
 *  // ... buyer işlemleri ...
 */
public class RoleSessionManager {

    private static final Logger log = LogManager.getLogger(RoleSessionManager.class);

    // ─── Rol Tanımları ────────────────────────────────────────────────────────

    public enum Role {
        COMPANY("Tedarikçi"),
        BUYER("Alıcı"),
        FACTORING("Finansman"),
        ADMIN("Admin");

        private final String displayName;
        Role(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // ─── Oturum Deposu ────────────────────────────────────────────────────────

    /** Rol → cookie seti */
    private static final Map<Role, Set<Cookie>> sessionCookies = new ConcurrentHashMap<>();

    /** Rol → son aktif URL (cookie domain uyumu için) */
    private static final Map<Role, String> lastUrls = new ConcurrentHashMap<>();

    // ─── Kimlik Bilgileri (properties'ten yüklenir) ───────────────────────────

    private static final Map<Role, String[]> defaultCredentials = new HashMap<>();

    static {
        // ConfigReader üzerinden dev2.properties'teki değerleri yükle
        try {
            defaultCredentials.put(Role.ADMIN, new String[]{
                    ConfigReader.getProperty("admin.email"),
                    ConfigReader.getProperty("admin.password")
            });
            defaultCredentials.put(Role.COMPANY, new String[]{
                    ConfigReader.getProperty("company.email"),
                    ConfigReader.getProperty("company.password")
            });
            defaultCredentials.put(Role.BUYER, new String[]{
                    ConfigReader.getProperty("buyer.email"),
                    ConfigReader.getProperty("buyer.password")
            });
            defaultCredentials.put(Role.FACTORING, new String[]{
                    ConfigReader.getProperty("factoring.email"),
                    ConfigReader.getProperty("factoring.password")
            });
        } catch (Exception e) {
            log.warn("Kimlik bilgileri properties'ten yüklenemedi: {}", e.getMessage());
        }
    }

    // ─── Login & Cookie Kaydet ────────────────────────────────────────────────

    /**
     * Verilen role ile giriş yapar ve oturumu cookie'ye kaydeder.
     * Login sayfası URL'si base.url'den alınır.
     */
    public static void loginAs(WebDriver driver, Role role, String email, String password) {
        String baseUrl = ConfigReader.getProperty("base.url");
        log.info("[{}] Login başlatılıyor: {} → {}", role.getDisplayName(), email, baseUrl);

        driver.manage().deleteAllCookies();
        driver.get(baseUrl);

        // Login form doldur
        performLogin(driver, email, password);

        // Dashboard yüklenene kadar bekle
        waitForDashboard(driver);

        // Cookie'leri kaydet
        Set<Cookie> cookies = new HashSet<>(driver.manage().getCookies());
        sessionCookies.put(role, cookies);
        lastUrls.put(role, driver.getCurrentUrl());

        log.info("[{}] Oturum kaydedildi: {} cookie", role.getDisplayName(), cookies.size());
    }

    /**
     * Varsayılan kimlik bilgileriyle (dev2.properties) login yapar.
     */
    public static void loginAsDefault(WebDriver driver, Role role) {
        String[] creds = defaultCredentials.get(role);
        if (creds == null || creds[0] == null || creds[0].isEmpty()) {
            throw new IllegalStateException(
                    role.getDisplayName() + " için properties'te kimlik bilgisi tanımlı değil. " +
                    "dev2.properties'teki " + role.name().toLowerCase() + ".email ve .password alanlarını doldurun.");
        }
        loginAs(driver, role, creds[0], creds[1]);
    }

    // ─── Rol Geçişi ───────────────────────────────────────────────────────────

    /**
     * Tarayıcıyı istenen role geçirir.
     * Cookie'leri yükler ve sayfayı yeniler.
     */
    public static void switchToRole(WebDriver driver, Role role) {
        Set<Cookie> cookies = sessionCookies.get(role);
        if (cookies == null || cookies.isEmpty()) {
            throw new IllegalStateException(
                    "[" + role.getDisplayName() + "] için kayıtlı oturum yok. " +
                    "Önce loginAs() veya loginAsDefault() çağırın.");
        }

        String baseUrl = ConfigReader.getProperty("base.url");

        // Mevcut cookie'leri temizle ve yeni oturumu yükle
        driver.get(baseUrl);
        driver.manage().deleteAllCookies();

        for (Cookie cookie : cookies) {
            try {
                driver.manage().addCookie(cookie);
            } catch (Exception e) {
                log.debug("Cookie eklenemedi ({}) : {}", cookie.getName(), e.getMessage());
            }
        }

        driver.navigate().refresh();
        waitForDashboard(driver);

        log.info("[{}] Rol geçişi tamamlandı.", role.getDisplayName());
    }

    // ─── Oturum Durumu ────────────────────────────────────────────────────────

    public static boolean hasSession(Role role) {
        return sessionCookies.containsKey(role) && !sessionCookies.get(role).isEmpty();
    }

    /**
     * Tüm oturumları temizler (senaryo sonunda çağrılır).
     */
    public static void clearAllSessions() {
        sessionCookies.clear();
        lastUrls.clear();
        log.info("Tüm oturumlar temizlendi.");
    }

    public static void clearSession(Role role) {
        sessionCookies.remove(role);
        lastUrls.remove(role);
        log.info("[{}] Oturum temizlendi.", role.getDisplayName());
    }

    // ─── Yardımcı Metotlar ────────────────────────────────────────────────────

    /**
     * Login formunu doldurur ve giriş yapar.
     * Vaadin 24 login sayfası için CSS seçiciler (dev2.faturalab.com uyumlu).
     */
    private static void performLogin(WebDriver driver, String email, String password) {
        org.openqa.selenium.support.ui.WebDriverWait wait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;

        try {
            // Sayfanın tam yüklenmesini bekle (Vaadin için 5-15sn gerekebilir)
            wait.until(d -> "complete".equals(js.executeScript("return document.readyState")));
            // Input elementlerinin DOM'a eklenmesini bekle
            wait.until(d -> !d.findElements(org.openqa.selenium.By.cssSelector("input")).isEmpty());
            Thread.sleep(1000); // Vaadin hydration için ek bekleme

            // 1. Önce standart CSS selektörlerle dene
            org.openqa.selenium.WebElement emailField = null;
            org.openqa.selenium.WebElement passwordField = null;
            org.openqa.selenium.WebElement loginBtn = null;

            String[] emailSelectors = {
                "input[type='text'].v-login-textfield-component",
                "input[name='username']",
                "input[type='email']",
                "input[type='text']",
                "#username"
            };
            String[] passwordSelectors = {
                "input[type='password'].v-login-textfield-component",
                "input[name='password']",
                "input[type='password']",
                "#password"
            };
            String[] buttonSelectors = {
                "div.login-button[role='button']",
                "vaadin-button.login-button",
                "button[type='submit']",
                ".v-button.login-button",
                "input[type='submit']",
                "button"
            };

            try { emailField = findWithFallback(driver, emailSelectors); } catch (Exception ignored) {}
            try { passwordField = findWithFallback(driver, passwordSelectors); } catch (Exception ignored) {}
            try { loginBtn = findWithFallback(driver, buttonSelectors); } catch (Exception ignored) {}

            // 2. Vaadin shadow DOM JS fallback
            if (emailField == null) {
                log.warn("CSS selektörle email field bulunamadı, JS ile deneniyor...");
                js.executeScript(
                    "var inputs = document.querySelectorAll('input[type=\"text\"], input[type=\"email\"], input:not([type=\"password\"])'); " +
                    "if(inputs.length > 0) { inputs[0].value = arguments[0]; inputs[0].dispatchEvent(new Event('input',{bubbles:true})); inputs[0].dispatchEvent(new Event('change',{bubbles:true})); }",
                    email);
                Thread.sleep(500);
                try { passwordField = findWithFallback(driver, passwordSelectors); } catch (Exception ignored) {}
                try { loginBtn = findWithFallback(driver, buttonSelectors); } catch (Exception ignored) {}
            } else {
                emailField.clear();
                emailField.sendKeys(email);
            }

            if (passwordField == null) {
                log.warn("CSS selektörle password field bulunamadı, JS ile deneniyor...");
                js.executeScript(
                    "var inputs = document.querySelectorAll('input[type=\"password\"]'); " +
                    "if(inputs.length > 0) { inputs[0].value = arguments[0]; inputs[0].dispatchEvent(new Event('input',{bubbles:true})); inputs[0].dispatchEvent(new Event('change',{bubbles:true})); }",
                    password);
            } else {
                passwordField.clear();
                passwordField.sendKeys(password);
            }

            Thread.sleep(500);

            if (loginBtn == null) {
                log.warn("Login butonu bulunamadı, Enter ile deneniyor...");
                if (passwordField != null) {
                    passwordField.sendKeys(org.openqa.selenium.Keys.ENTER);
                } else {
                    js.executeScript(
                        "var inputs = document.querySelectorAll('input[type=\"password\"]'); " +
                        "if(inputs.length > 0) { inputs[0].dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',bubbles:true})); }");
                }
            } else {
                loginBtn.click();
            }

            log.info("Login formu gönderildi: {}", email);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException("Login formu doldurulamadı: " + e.getMessage(), e);
        }
    }

    /**
     * Dashboard yüklenene kadar bekler.
     */
    private static void waitForDashboard(WebDriver driver) {
        org.openqa.selenium.support.ui.WebDriverWait wait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30));
        try {
            wait.until(d -> {
                String url = d.getCurrentUrl();
                // Login sayfasından çıktıysak başarılı
                return !url.contains("/login") && !url.contains("/error");
            });
            Thread.sleep(1000); // Vaadin hydration için kısa bekleme
        } catch (Exception e) {
            log.warn("Dashboard bekleme timeout: {}", e.getMessage());
        }
    }

    /**
     * Birden fazla CSS selektör dener, ilk bulunanı döner.
     */
    private static org.openqa.selenium.WebElement findWithFallback(WebDriver driver, String... selectors) {
        for (String selector : selectors) {
            try {
                org.openqa.selenium.WebElement el = driver.findElement(
                        org.openqa.selenium.By.cssSelector(selector));
                if (el != null) return el;
            } catch (Exception ignored) {}
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Element bulunamadı. Denenen selektörler: " + String.join(", ", selectors));
    }
}
