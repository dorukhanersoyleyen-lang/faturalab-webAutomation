package com.faturalab.automation.context;

import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.driver.DriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
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

    /**
     * JS'e enjekte edilen Türkçe→ASCII katlama (fold) fonksiyonu.
     * Config'te ASCII yazılan hedef ("EFG Gida") ile ekrandaki Türkçe değeri ("EFG Gıda A.Ş.")
     * eşleştirmek için kullanılır. Her filtre/eşleşme JS bloğunun başına eklenir.
     */
    private static final String TR_FOLD_JS =
            "function fold(s){return (s||'')" +
            ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
            ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
            ".replace(/[âÂ]/g,'a').toLowerCase().replace(/\\s+/g,' ').trim();}";

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
        // Tüm roller için admin kimliği kullanılır.
        // Admin login sonrası Kullanıcılar ekranından hedef kullanıcıya geçiş yapılır.
        try {
            String adminEmail    = ConfigReader.getProperty("admin.email");
            String adminPassword = ConfigReader.getProperty("admin.password");

            defaultCredentials.put(Role.ADMIN,     new String[]{ adminEmail, adminPassword });
            defaultCredentials.put(Role.COMPANY,   new String[]{ adminEmail, adminPassword });
            defaultCredentials.put(Role.BUYER,     new String[]{ adminEmail, adminPassword });
            defaultCredentials.put(Role.FACTORING, new String[]{ adminEmail, adminPassword });
        } catch (Exception e) {
            log.warn("Kimlik bilgileri properties'ten yüklenemedi: {}", e.getMessage());
        }
    }

    // ─── Login & Cookie Kaydet ────────────────────────────────────────────────

    /**
     * Verilen role ile giriş yapar ve oturumu cookie'ye kaydeder.
     *
     * Akış:
     *   1. Admin olarak login ol (dorukhan.ersoyleyen@faturalab.com)
     *   2. Hedef rol ADMIN ise burada dur — admin oturumu yeterli.
     *   3. Hedef rol COMPANY/BUYER/FACTORING ise:
     *      Yönetim Paneli → Kullanıcılar → hedef kullanıcıyı bul → "Olarak Giriş Yap" tıkla
     */
    public static void loginAs(WebDriver driver, Role role, String email, String password) {
        String baseUrl = ConfigReader.getProperty("base.url");
        String adminEmail    = ConfigReader.getProperty("admin.email");
        String adminPassword = ConfigReader.getProperty("admin.password");

        log.info("[{}] Admin ile login başlatılıyor: {} → {}", role.getDisplayName(), adminEmail, baseUrl);

        try { driver.manage().deleteAllCookies(); } catch (Exception ignored) {}

        try {
            driver.get(baseUrl);
        } catch (Exception e) {
            try { driver.navigate().to(baseUrl); }
            catch (Exception e2) { throw new RuntimeException("Login navigasyonu başarısız: " + e2.getMessage(), e2); }
        }

        // 1. Admin olarak login
        performLogin(driver, adminEmail, adminPassword);
        waitForDashboard(driver);
        log.info("[{}] Admin login tamamlandı.", role.getDisplayName());

        // 2. Hedef rol admin ise oturumu kaydet ve çık
        if (role == Role.ADMIN) {
            Set<Cookie> cookies = new HashSet<>(driver.manage().getCookies());
            sessionCookies.put(role, cookies);
            lastUrls.put(role, driver.getCurrentUrl());
            log.info("[{}] Oturum kaydedildi: {} cookie", role.getDisplayName(), cookies.size());
            return;
        }

        // 3. Hedef kullanıcıya geç: email parametresi burada impersonate identifier'dır
        //    (dev2.properties'teki company/buyer/factoring.impersonate.identifier)
        impersonateUser(driver, role, email);

        // 3b. Geçişi doğrula: hâlâ admin Kullanıcılar ekranındaysak yanlış oturumla
        //     devam etmek tüm senaryoyu sessizce bozar — açık hata ver.
        boolean switched = waitForJsCondition(driver, 15,
                "return (document.body.innerText || '').indexOf('Yeni Admin Ekle') < 0;");
        if (!switched) {
            throw new IllegalStateException("[" + role.getDisplayName()
                    + "] impersonation doğrulanamadı — sayfa hâlâ admin Kullanıcılar ekranında. Hedef: " + email);
        }

        // Oturumu kaydet
        Set<Cookie> cookies = new HashSet<>(driver.manage().getCookies());
        sessionCookies.put(role, cookies);
        lastUrls.put(role, driver.getCurrentUrl());
        log.info("[{}] İmpersonate oturumu kaydedildi: {} cookie", role.getDisplayName(), cookies.size());
    }

    /**
     * Admin panelinde Kullanıcılar ekranına gidip hedef firma/email'e göre
     * kullanıcı satırını bulur ve "Olarak Giriş Yap" / login butonuna tıklar.
     *
     * Admin sidebar butonları vaadin-button kullandığı için AdminPanelPage.clickSidebarItem() kullanılır.
     */
    private static void impersonateUser(WebDriver driver, Role role, String targetIdentifier) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        log.info("[{}] Kullanıcıya geçiş başlatılıyor: {}", role.getDisplayName(), targetIdentifier);

        try {
            // 1. Admin sidebar'dan "Kullanıcılar"a tıkla
            //    AdminPanelPage.clickSidebarItem() kullanır: //vaadin-button[normalize-space()='Kullanıcılar']
            new com.faturalab.automation.pages.AdminPanelPage(driver).clickSidebarItem("Kullanıcılar");
            waitForVaadinStatic(driver);
            log.info("[{}] Kullanıcılar ekranına gidildi.", role.getDisplayName());

            // 2. Sayfadaki tüm vaadin-button ve button'ları logla (debug)
            Object btns = js.executeScript(
                "return Array.from(document.querySelectorAll('vaadin-button,button'))" +
                ".map(b => b.textContent.trim().substring(0,40)).filter(t=>t).join(' | ')");
            log.info("[{}] Kullanıcılar sayfasındaki butonlar: {}", role.getDisplayName(), btns);

            // 3. Sekme geçişi: Rolle eşleşen sekmeye tıkla (Ticari İşletme / Finansal Kurum / Alıcı)
            String tabName = roleToTabName(role);
            js.executeScript(
                "var kw = arguments[0].toLowerCase();" +
                "var tabs = document.querySelectorAll('vaadin-button');" +
                "for (var t of tabs) {" +
                "  var txt = (t.textContent || '').toLowerCase().trim();" +
                "  if (txt === kw) { t.click(); return true; }" +
                "}" +
                "return false;", tabName);
            waitForVaadinStatic(driver);
            log.info("[{}] Sekmeye tıklandı: {}", role.getDisplayName(), tabName);

            // 4. Grid yüklenene kadar bekle — Vaadin grid asenkron render
            waitForVaadinStatic(driver);
            waitForVaadinStatic(driver); // 2x 1.5sn = 3sn toplam

            // Sayfadaki tüm butonları logla (debug)
            Object allBtns2 = js.executeScript(
                "return Array.from(document.querySelectorAll('vaadin-button,button'))" +
                ".map(function(b){return (b.textContent||'').trim().substring(0,30);})" +
                ".filter(function(t){return t.length>0;}).join(' | ')");
            log.info("[{}] Sekme sonrasi butonlar: {}", role.getDisplayName(), allBtns2);

            // 5. Önce kolon filtresiyle hedefli geçiş dene:
            //    filtre popup → hedefi ara → checkbox seç → Tamam → GİT → "Evet" onayı.
            //    (git_first_fallback'in yanlış kullanıcıya girme riskini ortadan kaldırır)
            if (impersonateViaColumnFilter(driver, role, targetIdentifier)) {
                acceptGitConfirmModal(driver);
                waitForDashboard(driver);
                log.info("[{}] Filtreli GİT ile kullanıcıya geçildi: {} — URL: {}",
                        role.getDisplayName(), targetIdentifier, driver.getCurrentUrl());
                return;
            }
            log.warn("[{}] Filtreli geçiş başarısız — eski hücre eşleşme yöntemi deneniyor.",
                    role.getDisplayName());

            // 5b. Hedef kullanıcıyı bul: önce grid cells'de ara, yoksa ilk GİT'e tıkla
            Object switched = js.executeScript(
                "var target = arguments[0].toLowerCase();" +
                "var gitBtns = Array.from(document.querySelectorAll('vaadin-button')).filter(function(b) {" +
                "  var t = (b.textContent||'').trim();" +
                "  return t === 'GİT' || t.toUpperCase() === 'GIT';" +
                "});" +
                "if (gitBtns.length === 0) return 'no_git_btn';" +
                "var cells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                "for (var i = 0; i < cells.length; i++) {" +
                "  if ((cells[i].textContent||'').toLowerCase().includes(target)) {" +
                "    gitBtns[0].click(); return 'git_by_cell_match';" +
                "  }" +
                "}" +
                "gitBtns[0].click(); return 'git_first_fallback';" ,
                targetIdentifier);

            String result = switched != null ? switched.toString() : "null";
            log.info("[{}] impersonate JS sonuç: {}", role.getDisplayName(), result);

            if (result.startsWith("git") || result.startsWith("clicked")) {
                // GİT tıklandı — "oturum açmak istiyor musunuz" modalı varsa onayla
                waitForVaadinStatic(driver);
                acceptGitConfirmModal(driver);
                waitForVaadinStatic(driver);
                String urlAfterGit = driver.getCurrentUrl();
                Object bodyAfterGit = js.executeScript(
                    "return Array.from(document.querySelectorAll('vaadin-button,button'))" +
                    ".map(function(b){return (b.textContent||'').trim().substring(0,30);})" +
                    ".filter(function(t){return t.length>0;}).slice(0,20).join(' | ')");
                log.info("[{}] GIT sonrasi URL: {}", role.getDisplayName(), urlAfterGit);
                log.info("[{}] GIT sonrasi butonlar: {}", role.getDisplayName(), bodyAfterGit);
                waitForDashboard(driver);
                log.info("[{}] Dashboard sonrasi URL: {}", role.getDisplayName(), driver.getCurrentUrl());
            } else {
                log.warn("[{}] GIT butonu bulunamadi ({}), shadow DOM fallback deneniyor.", role.getDisplayName(), result);
                impersonateFallbackViaGrid(driver, role, targetIdentifier);
            }

        } catch (Exception e) {
            log.error("[{}] impersonateUser hatasi: {}", role.getDisplayName(), e.getMessage());
        }
    }

    private static String roleToTabName(Role role) {
        switch (role) {
            case COMPANY:   return "Ticari İşletme";
            case BUYER:     return "Alıcı";
            case FACTORING: return "Finansal Kurum";
            default:        return "Admin";
        }
    }

    /**
     * Kullanıcılar grid'inde rol kolonunun filtre dialogu üzerinden hedefli geçiş.
     *
     * Canlı DOM'da doğrulanmış yapı (2026-07-02):
     *  - Kolon başlığı: vaadin-grid-cell-content > .filter-header-cell > span.filter-icon + span.filter-text
     *  - Filtre dialogu: vaadin-dialog-overlay.table-filter-dialog
     *  - Arama kutusu: dialog içinde vaadin-text-field[label="Ara"] input
     *  - Liste: vaadin-grid.check-table — checkbox hücresi, firma adı hücresinin HEMEN ÖNÜNDE ayrı hücrededir
     *  - Varsayılan durumda TÜM checkbox'lar seçilidir (= filtre yok); önce "(Tümünü seç)" kaldırılır
     *  - Onay: 'Tamam' / 'Vazgeç' butonları
     *
     * Filtre sonrası yalnızca GÖRÜNÜR (rect > 0) GİT butonuna tıklanır — Vaadin grid'in
     * virtual scroll cache'i DOM'da eski hücreleri tutar, sayaç/ilk-buton güvenilmezdir.
     * Hedef satır görünür değilse tıklama YAPILMAZ (yanlış kullanıcıya geçiş engellenir).
     *
     * @return GİT'e kadar tüm adımlar başarılıysa true
     */
    private static boolean impersonateViaColumnFilter(WebDriver driver, Role role, String target) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String columnKeyword = roleToTabName(role); // kolon başlığı = sekme adıyla aynı

        try {
            // 1. Kolon başlığındaki filtre ikonuna tıkla → table-filter-dialog açılır
            Object headerHit = js.executeScript(
                "var kw = arguments[0].toLowerCase();" +
                "var headers = document.querySelectorAll('vaadin-grid-cell-content .filter-header-cell');" +
                "for (var h of headers) {" +
                "  var txtEl = h.querySelector('span.filter-text');" +
                "  var t = ((txtEl ? txtEl.textContent : h.textContent) || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                "  if (t === kw || t.includes(kw)) {" +
                "    var icon = h.querySelector('span.filter-icon');" +
                "    (icon || h).click();" +
                "    return t;" +
                "  }" +
                "}" +
                "return null;", columnKeyword.toLowerCase(Locale.ROOT));
            if (headerHit == null) {
                log.warn("[{}] Filtre kolonu başlığı bulunamadı: {}", role.getDisplayName(), columnKeyword);
                return false;
            }
            log.info("[{}] Filtre ikonuna tıklandı: '{}'", role.getDisplayName(), headerHit);

            // 2. table-filter-dialog'un açılmasını bekle (poll)
            if (!waitForJsCondition(driver, 8,
                    "return !!document.querySelector('vaadin-dialog-overlay.table-filter-dialog[opened], " +
                    "vaadin-dialog-overlay.table-filter-dialog');")) {
                log.warn("[{}] table-filter-dialog açılmadı.", role.getDisplayName());
                return false;
            }

            // 3. "(Tümünü seç)" işaretini kaldır → tüm seçimler temizlenir
            Object unselectAll = js.executeScript(
                "var dlg = document.querySelector('vaadin-dialog-overlay.table-filter-dialog');" +
                "var cells = Array.from(dlg.querySelectorAll('vaadin-grid.check-table vaadin-grid-cell-content'));" +
                "var idx = cells.findIndex(function(c) { return (c.textContent||'').indexOf('münü seç') >= 0; });" +
                "if (idx < 1) return 'tumunu_sec_yok';" +
                "var cb = cells[idx - 1].querySelector('vaadin-checkbox, input[type=checkbox]');" +
                "if (!cb) return 'checkbox_yok';" +
                "if (cb.checked === true) { cb.click(); return 'temizlendi'; }" +
                "return 'zaten_bos';");
            log.info("[{}] Tümünü seç: {}", role.getDisplayName(), unselectAll);
            waitForVaadinStatic(driver);

            // 4. "Ara" kutusuna hedefin ASCII-güvenli EN UZUN prefix'ini yaz.
            //    İlk kelime (ör. "Test") büyük listelerde (Ticari İşletme) yetersiz daraltır;
            //    hedef satır virtual scroll'da render edilmeyince checkbox bulunamaz.
            //    İlk Türkçe karaktere kadarki prefix hem ASCII-güvenli hem maksimum ayırt edici:
            //    "Test Otomasyon Sadece Tedarikçi" -> "Test Otomasyon Sadece Tedarik"
            //    "EFG Gıda" -> "EFG G"  |  "ALBC" -> "ALBC"
            Boolean typed = (Boolean) js.executeScript(
                "var target = arguments[0];" +
                "var m = target.match(/^[^çğıöşüÇĞİÖŞÜ]+/);" +
                "var searchStr = (m && m[0].trim().length >= 3) ? m[0].trim() : (target.split(/\\s+/)[0] || target);" +
                "var dlg = document.querySelector('vaadin-dialog-overlay.table-filter-dialog');" +
                "var inp = dlg.querySelector('vaadin-text-field[label=\"Ara\"] input');" +
                "if (!inp) {" +
                "  var inputs = Array.from(dlg.querySelectorAll('input[type=\"text\"], input:not([type])'));" +
                "  inp = inputs.length ? inputs[inputs.length - 1] : null;" +
                "}" +
                "if (!inp) return false;" +
                "inp.focus();" +
                "inp.value = searchStr;" +
                "inp.dispatchEvent(new Event('input', {bubbles: true}));" +
                "inp.dispatchEvent(new Event('change', {bubbles: true}));" +
                "return true;", target);
            if (!Boolean.TRUE.equals(typed)) {
                log.warn("[{}] Filtre 'Ara' kutusu bulunamadı.", role.getDisplayName());
                return false;
            }
            log.info("[{}] Filtre aramasına yazıldı (ilk kelime): {}", role.getDisplayName(), target);
            waitForVaadinStatic(driver);

            // 5. Hedef firmanın checkbox'ını işaretle (checkbox = ad hücresinin önceki hücresi).
            //    Eşleşme Türkçe-DUYARSIZ (ASCII-fold): config'te "EFG Gida" ↔ ekranda "EFG Gıda".
            Object checked = js.executeScript(
                TR_FOLD_JS +
                "var target = fold(arguments[0]);" +
                "var dlg = document.querySelector('vaadin-dialog-overlay.table-filter-dialog');" +
                "var cells = Array.from(dlg.querySelectorAll('vaadin-grid.check-table vaadin-grid-cell-content'));" +
                "var idx = cells.findIndex(function(c) {" +
                "  var raw = (c.textContent||'');" +
                "  if (raw.indexOf('münü seç') >= 0) return false;" +
                "  var t = fold(raw);" +
                "  return t.length > 0 && t.includes(target);" +
                "});" +
                "if (idx < 1) return null;" +
                "var cb = cells[idx - 1].querySelector('vaadin-checkbox, input[type=checkbox]');" +
                "if (!cb) return null;" +
                "if (cb.checked !== true) { cb.click(); return 'isaretlendi'; }" +
                "return 'zaten_isaretli';", target);
            if (checked == null) {
                log.warn("[{}] Filtre listesinde hedef bulunamadı: {}", role.getDisplayName(), target);
                return false;
            }
            log.info("[{}] Hedef checkbox: {}", role.getDisplayName(), checked);
            waitForVaadinStatic(driver);

            // 6. Tamam'a bas
            Boolean confirmed = (Boolean) js.executeScript(
                "var dlg = document.querySelector('vaadin-dialog-overlay.table-filter-dialog');" +
                "var btns = dlg.querySelectorAll('vaadin-button, button');" +
                "for (var b of btns) {" +
                "  if ((b.textContent || '').trim() === 'Tamam') { b.click(); return true; }" +
                "}" +
                "return false;");
            if (!Boolean.TRUE.equals(confirmed)) {
                log.warn("[{}] Filtre dialogu 'Tamam' butonu bulunamadı.", role.getDisplayName());
                return false;
            }

            // 7. Grid'in filtrelenmesini bekle: hedef satır GÖRÜNÜR olana kadar poll (ASCII-fold)
            boolean rowVisible = waitForJsCondition(driver, 10,
                TR_FOLD_JS +
                "var target = fold('" + target.replace("'", "\\'") + "');" +
                "var cells = document.querySelectorAll('vaadin-grid vaadin-grid-cell-content');" +
                "for (var c of cells) {" +
                "  var r = c.getBoundingClientRect();" +
                "  if (r.width < 2 || r.height < 2) continue;" + // virtual scroll cache hücrelerini atla
                "  if (fold(c.textContent||'').includes(target)) return true;" +
                "}" +
                "return false;");
            if (!rowVisible) {
                log.warn("[{}] Filtre sonrası hedef satır görünmedi: {}", role.getDisplayName(), target);
                return false;
            }

            // 8. GÖRÜNÜR GİT'e tıkla; onay modalı gelmezse yeniden dene.
            //    Tamam sonrası grid yeniden render olduğundan ilk tıklama bayat
            //    elemana denk gelebiliyor — modal gelene kadar en fazla 3 deneme.
            waitForVaadinStatic(driver); // grid render'ının oturması
            for (int attempt = 1; attempt <= 3; attempt++) {
                Boolean gitClicked = (Boolean) js.executeScript(
                    "var gitBtns = Array.from(document.querySelectorAll('vaadin-button')).filter(function(b) {" +
                    "  var t = (b.textContent || '').trim();" +
                    "  if (t !== 'GİT' && t.toUpperCase() !== 'GIT') return false;" +
                    "  var r = b.getBoundingClientRect();" +
                    "  return r.width > 2 && r.height > 2;" +
                    "});" +
                    "if (gitBtns.length === 0) return false;" +
                    "gitBtns[0].click();" +
                    "return true;");
                if (!Boolean.TRUE.equals(gitClicked)) {
                    log.warn("[{}] Görünür GİT butonu bulunamadı (deneme {}).", role.getDisplayName(), attempt);
                    return false;
                }
                log.info("[{}] Filtrelenmiş satırda GİT tıklandı (deneme {}).", role.getDisplayName(), attempt);

                // Onay modalı ("... oturum açmak istediğinizden emin misiniz?" / Hayır-Evet) bekle
                boolean modalVisible = waitForJsCondition(driver, 6,
                    "var hosts = document.querySelectorAll('vaadin-dialog-overlay');" +
                    "for (var h of hosts) {" +
                    "  var r = h.getBoundingClientRect();" +
                    "  if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = (h.textContent || '').toLowerCase();" +
                    "  if (t.indexOf('oturum açmak') >= 0 || t.indexOf('emin misiniz') >= 0) return true;" +
                    "}" +
                    "return false;");
                if (modalVisible) {
                    return true;
                }
                // Bazı akışlarda modal olmayabilir — sayfa zaten değiştiyse başarı say
                Boolean stillOnAdmin = (Boolean) js.executeScript(
                    "return (document.body.innerText || '').indexOf('Yeni Admin Ekle') >= 0;");
                if (!Boolean.TRUE.equals(stillOnAdmin)) {
                    log.info("[{}] Onay modalı yok ama admin ekranından çıkılmış — geçiş sayılıyor.", role.getDisplayName());
                    return true;
                }
                log.warn("[{}] GİT sonrası onay modalı gelmedi, yeniden denenecek (deneme {}).",
                        role.getDisplayName(), attempt);
                waitForVaadinStatic(driver);
            }
            return false;
        } catch (Exception e) {
            log.warn("[{}] impersonateViaColumnFilter hatası: {}", role.getDisplayName(), e.getMessage());
            return false;
        }
    }

    /**
     * GİT sonrası çıkan "... kullanıcısı ile oturum açmak istediğinizden emin misiniz?"
     * modalında (vaadin-dialog-overlay.confirm-dialog, butonlar: Hayır/Evet) "Evet"e basar.
     * Modal asenkron açıldığı için ~12 sn poll eder; hiç gelmezse sessizce geçer.
     */
    private static void acceptGitConfirmModal(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long deadline = System.currentTimeMillis() + 12_000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                Boolean clicked = (Boolean) js.executeScript(
                    "function visible(el) {" +
                    "  var r = el.getBoundingClientRect();" +
                    "  return r.width > 2 && r.height > 2;" +
                    "}" +
                    "var hosts = document.querySelectorAll(" +
                    "  'vaadin-dialog-overlay.confirm-dialog, vaadin-confirm-dialog-overlay, vaadin-dialog-overlay');" +
                    "for (var h of hosts) {" +
                    "  if (!visible(h)) continue;" +
                    "  var txt = (h.textContent || '').toLowerCase();" +
                    "  var isConfirm = (h.className || '').toString().indexOf('confirm') >= 0" +
                    "      || txt.indexOf('oturum açmak') >= 0 || txt.indexOf('emin misiniz') >= 0;" +
                    "  if (!isConfirm) continue;" +
                    "  var btns = h.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    var t = (b.textContent || '').trim().toLowerCase();" +
                    "    if (t === 'evet' || t === 'yes') { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("GİT onay modalı 'Evet' ile onaylandı.");
                    waitForVaadinStatic(driver);
                    return;
                }
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.debug("acceptGitConfirmModal: {}", e.getMessage());
                return;
            }
        }
        log.info("GİT onay modalı görünmedi — devam ediliyor (modal gelmemiş olabilir).");
    }

    /** Verilen JS koşulu true dönene kadar poll eder (500 ms aralıkla). */
    private static boolean waitForJsCondition(WebDriver driver, int timeoutSeconds, String jsReturningBoolean) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                Object r = js.executeScript(jsReturningBoolean);
                if (Boolean.TRUE.equals(r)) {
                    return true;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * vaadin-grid shadow DOM içinde hedef kullanıcıyı arar.
     * Vaadin grid satırları shadow root içinde olabileceğinden JS ile derin tarama yapar.
     */
    private static void impersonateFallbackViaGrid(WebDriver driver, Role role, String targetIdentifier) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try {
            Boolean ok = (Boolean) js.executeScript(
                "var target = arguments[0].toLowerCase();" +
                "function findInShadow(root, depth) {" +
                "  if (!root || depth > 15) return false;" +
                "  var txt = (root.textContent || '').toLowerCase();" +
                "  if (txt.includes(target)) {" +
                "    var btns = root.querySelectorAll('vaadin-button,button,[role=\"button\"]');" +
                "    for (var b of btns) {" +
                "      var label = (b.textContent||b.getAttribute('title')||'').toLowerCase();" +
                "      if (label.includes('giriş')||label.includes('login')) { b.click(); return true; }" +
                "    }" +
                "  }" +
                "  var children = root.children || [];" +
                "  for (var c of children) {" +
                "    if (c.shadowRoot && findInShadow(c.shadowRoot, depth+1)) return true;" +
                "    if (findInShadow(c, depth+1)) return true;" +
                "  }" +
                "  return false;" +
                "}" +
                "return findInShadow(document.body, 0);",
                targetIdentifier);

            if (Boolean.TRUE.equals(ok)) {
                log.info("[{}] Shadow DOM fallback ile kullanıcıya geçildi.", role.getDisplayName());
                waitForDashboard(driver);
            } else {
                log.warn("[{}] Shadow DOM fallback da başarısız — kullanıcı bulunamadı: {}",
                        role.getDisplayName(), targetIdentifier);
            }
        } catch (Exception e) {
            log.warn("[{}] impersonateFallback hatası: {}", role.getDisplayName(), e.getMessage());
        }
    }

    /** BasePageObject dışında kullanılabilen statik nav helper */
    private static void clickNavItemByTextStatic(WebDriver driver, String keyword) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript(
                "var kw = arguments[0].toLowerCase();" +
                "var sel = 'vaadin-side-nav-item, vaadin-tab, [role=\"menuitem\"]';" +
                "var els = Array.from(document.querySelectorAll(sel));" +
                "for (var e of els) {" +
                "  var txt = (e.textContent||'').toLowerCase().trim();" +
                "  if (txt.length <= 60 && txt.includes(kw)) { e.click(); return true; }" +
                "}" +
                "return false;", keyword);
        } catch (Exception e) {
            log.warn("clickNavItemByTextStatic '{}': {}", keyword, e.getMessage());
        }
    }

    /** waitForVaadinNavigation statik varyantı */
    private static void waitForVaadinStatic(WebDriver driver) {
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    /**
     * Varsayılan kimlik bilgileriyle (dev2.properties) login yapar.
     * Kimlik bilgileri static block'ta yüklenememişse (env=dev iken yüklenip sonra
     * env=dev2'ye geçilmişse) ConfigReader'dan yeniden okur.
     */
    public static void loginAsDefault(WebDriver driver, Role role) {
        // Admin credentials (her rol için ortak)
        String adminEmail    = ConfigReader.getProperty("admin.email");
        String adminPassword = ConfigReader.getProperty("admin.password");

        if (adminEmail == null || adminEmail.isEmpty()) {
            throw new IllegalStateException("admin.email properties'te tanımlı değil.");
        }

        // Role-specific impersonate identifier (şirket adı veya email kısmı)
        // ADMIN rolü için identifier gerekmez
        String keyPrefix   = role.name().toLowerCase(Locale.ROOT);
        String identifier  = ConfigReader.getProperty(keyPrefix + ".impersonate.identifier");

        // Admin rolü için identifier olmayabilir — doğrudan login
        if (role == Role.ADMIN || identifier == null || identifier.isEmpty()) {
            loginAs(driver, role, adminEmail, adminPassword);
        } else {
            // Diğer roller: admin login → Kullanıcılar → identifier ile geç
            loginAs(driver, role, identifier, adminPassword);
        }
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
        try { driver.get(baseUrl); } catch (Exception e) {
            log.warn("[{}] switchToRole navigasyon başarısız: {}", role.getDisplayName(), e.getMessage());
        }
        try { driver.manage().deleteAllCookies(); } catch (Exception e) {
            log.warn("[{}] Cookie temizleme başarısız: {}", role.getDisplayName(), e.getMessage());
        }

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
     * Vaadin 24 login sayfası için CSS seçiciler (dev.faturalab.com/app uyumlu).
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
     *
     * FaturaLab SPA'da login ve dashboard aynı URL'i (/app/) kullanır.
     * Bu yüzden sadece URL kontrolü yeterli değil.
     *
     * Strateji 1 (kesin): Sayfada "GİRİŞ YAP" butonu kaybolana kadar bekle.
     *   → Login sayfasında var, dashboard'da yok.
     * Strateji 2 (fallback): URL /login içermiyorsa + sayfa içeriği 500 karakterden uzunsa.
     * Strateji 3 (fallback): 60 saniye geçtiyse devam et.
     */
    private static void waitForDashboard(WebDriver driver) {
        org.openqa.selenium.support.ui.WebDriverWait wait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(60));
        try {
            wait.until(d -> {
                try {
                    // Sayfa body metnini JS ile al
                    Object bodyText = ((JavascriptExecutor) d).executeScript(
                            "return document.body ? (document.body.innerText || document.body.textContent || '') : '';");
                    String text = bodyText != null ? bodyText.toString() : "";

                    // "GİRİŞ YAP" veya "GİRİŞ" butonu varsa hâlâ login ekranındayız
                    boolean onLoginPage = text.contains("GİRİŞ YAP")
                            || text.contains("SSO İLE GİRİŞ")
                            || text.contains("Şifremi Unuttum");

                    // Dashboard'a geçildiyse sidebar veya kullanıcı menüsü görünür
                    boolean dashboardLoaded = !onLoginPage && text.length() > 300;

                    if (onLoginPage) {
                        log.debug("waitForDashboard: hâlâ login ekranı — bekleniyor...");
                    }
                    return dashboardLoaded;
                } catch (Exception ex) {
                    return false;
                }
            });
            log.info("waitForDashboard: Dashboard yüklendi, Vaadin hydration için 2 sn bekleniyor.");
            Thread.sleep(2000); // Vaadin component tree için ek bekleme
        } catch (Exception e) {
            log.warn("Dashboard bekleme timeout — devam ediliyor: {}", e.getMessage());
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
                if (el != null) return el; // isDisplayed() kontrolü kaldırıldı
            } catch (Exception ignored) {}
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Element bulunamadı. Denenen selektörler: " + String.join(", ", selectors));
    }
}
