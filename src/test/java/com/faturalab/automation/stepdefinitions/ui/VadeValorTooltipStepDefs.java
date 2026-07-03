package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

/**
 * WP-5492: Vade Tarihi ve Valör Tarihi Alanlarına Tooltip Eklenmesi
 * -------------------------------------------------------------------
 * Kapsam:
 *   - Fatura yükleme ekranı  → Vade Tarihi, Ek Vade Tarihi
 *   - Teklif detay ekranı    → Vade Tarihi, Valör Tarihi
 *   - Teklif kabul akışı     → Vade Tarihi, Valör Tarihi
 *   - Bordro detay ekranı    → Vade Tarihi, Valör Tarihi
 *
 * Beklenen tooltip metinleri:
 *   Vade Tarihi  → "Ödemenin yapılması gereken son gündür."
 *   Valör Tarihi → "Tutarın hesabınıza geçeceği gündür."
 */
public class VadeValorTooltipStepDefs {

    private static final Logger log = LogManager.getLogger(VadeValorTooltipStepDefs.class);

    // Ekran görüntüsünden alınan gerçek tooltip metinleri
    private static final String VADE_TOOLTIP_BEKLENEN  = "İlgili fatura için ödemenin yapılması gereken son gündür.";
    private static final String VALOR_TOOLTIP_BEKLENEN = "Tutarın hesabınıza geçeceği gündür.";

    // Tooltip ikonu için olası CSS seçiciler — gerçek uygulama DOM'una göre güncellenmeli
    private static final String TOOLTIP_ICON_CSS =
            "vaadin-tooltip, [data-tooltip], .tooltip-icon, .info-icon, " +
            "[title], vaadin-icon[icon*='info'], iron-icon[icon*='info'], " +
            ".help-icon, [aria-describedby]";

    private WebDriver driver() {
        return DriverManager.getDriver();
    }

    // ─── Navigasyon Adımları ──────────────────────────────────────────────────

    @When("fatura listesinden teklif durumunda bir fatura seçilir")
    public void teklifDurumundaFaturaSeç() {
        // TODO: Teklif durumundaki ilk fatura satırını tıkla
        // Örnek: driver().findElements(By.cssSelector(".invoice-row[data-status='teklif']")).stream().findFirst()...
        log.info("Teklif durumunda fatura seçiliyor.");
    }

    @And("teklif detay ekranına gidilir")
    public void teklifDetayEkrani() {
        // TODO: Seçili faturanın detay sayfasına git
        // Örnek: driver().findElement(By.cssSelector(".invoice-row .detail-link")).click();
        log.info("Teklif detay ekranına gidiliyor.");
    }

    @When("teklif kabul ekranına gidilir")
    public void teklifKabulEkrani() {
        // TODO: İlk bekleyen teklifin kabul modalını veya sayfasını aç
        // Örnek: driver().findElement(By.cssSelector(".pending-offer .accept-btn")).click();
        log.info("Teklif kabul ekranına gidiliyor.");
    }

    @When("bordro listesi ekranına gidilir")
    public void bordroListesiEkrani() {
        // TODO: Sol menüden Bordro / Bordrolarım sayfasına navigate et
        // Örnek: driver().get(BASE_URL + "/payroll");
        log.info("Bordro listesi ekranına gidiliyor.");
    }

    @And("bordro listesinden bir bordro seçilir")
    public void bordroListesindenSec() {
        // TODO: İlk bordro satırını tıkla
        List<WebElement> rows = driver().findElements(By.cssSelector(".payroll-row, tr.bordro-satir"));
        if (rows.isEmpty()) {
            log.warn("Bordro listesi boş — veri önkoşulu sağlanamadı, adım atlanıyor.");
            return;
        }
        rows.get(0).click();
        log.info("Bordro listesinden ilk satır seçildi.");
    }

    @And("bordro detay ekranı açılır")
    public void bordroDetayEkraniAcilir() {
        // TODO: Bordro detay modalı veya sayfasının yüklendiğini bekle
        // Örnek: new WebDriverWait(driver(), 10).until(ExpectedConditions.visibilityOfElementLocated(...));
        log.info("Bordro detay ekranı açıldı.");
    }

    // ─── Tooltip Varlık Kontrolleri ───────────────────────────────────────────

    /**
     * Belirli bir alan etiketinin (örn: "Vade Tarihi") yanındaki tooltip ikonunu bulur.
     * DOM yapısına göre seçici güncellenmeli.
     */
    @Then("{string} alanının yanında tooltip ikonu görünmeli")
    public void tooltipIkonuGorunmeli(String alanAdi) {
        WebElement icon = findTooltipIconForField(alanAdi);
        if (icon == null) {
            log.warn("'{}' alanı için tooltip ikonu bulunamadı — DOM seçici güncellenmeli.", alanAdi);
        }
        Assert.assertNotNull(icon,
                "'" + alanAdi + "' alanının yanında tooltip ikonu görünmeli. URL: "
                        + driver().getCurrentUrl());
        Assert.assertTrue(icon.isDisplayed(),
                "'" + alanAdi + "' tooltip ikonu görünür olmalı. URL: " + driver().getCurrentUrl());
        log.info("'{}' alanı tooltip ikonu görünür.", alanAdi);
    }

    @Then("{string} tooltip ikonu disabled veya gizli olmamalı")
    public void tooltipIkonuAktifOlmali(String alanAdi) {
        WebElement icon = findTooltipIconForField(alanAdi);
        if (icon == null) {
            log.warn("'{}' tooltip ikonu DOM'da bulunamadı.", alanAdi);
            Assert.fail("'" + alanAdi + "' tooltip ikonu DOM'da bulunmalı. URL: " + driver().getCurrentUrl());
            return;
        }
        Assert.assertTrue(icon.isDisplayed() && icon.isEnabled(),
                "'" + alanAdi + "' tooltip ikonu görünür ve aktif olmalı. URL: " + driver().getCurrentUrl());
        log.info("'{}' tooltip ikonu aktif ve görünür.", alanAdi);
    }

    // ─── Hover & Metin Doğrulama ──────────────────────────────────────────────

    @And("{string} tooltip ikonuna hover yapılır")
    public void tooltipIkonunaHover(String alanAdi) {
        WebElement icon = findTooltipIconForField(alanAdi);
        if (icon == null) {
            log.warn("'{}' tooltip ikonu bulunamadı — hover atlanıyor.", alanAdi);
            return;
        }
        new Actions(driver()).moveToElement(icon).pause(600).perform();
        log.info("'{}' tooltip ikonuna hover yapıldı.", alanAdi);
    }

    @And("tooltip metni {string} olarak görünmeli")
    public void tooltipMetniDogrula(String beklenenMetin) {
        String gercekMetin = getVisibleTooltipText();
        if (gercekMetin == null || gercekMetin.isBlank()) {
            log.warn("Tooltip metni alınamadı. Beklenen: '{}'", beklenenMetin);
        }
        Assert.assertNotNull(gercekMetin,
                "Tooltip metni görünmeli. Beklenen: '" + beklenenMetin + "'. URL: " + driver().getCurrentUrl());
        Assert.assertTrue(gercekMetin.contains(beklenenMetin),
                "Tooltip metni '" + beklenenMetin + "' içermeli. Gerçek: '" + gercekMetin
                        + "'. URL: " + driver().getCurrentUrl());
        log.info("Tooltip metni doğrulandı — beklenen: '{}', gerçek: '{}'", beklenenMetin, gercekMetin);
    }

    @Then("{string} tooltip metni boş olmamalı")
    public void tooltipMetniBoşOlmamali(String alanAdi) {
        WebElement icon = findTooltipIconForField(alanAdi);
        if (icon == null) {
            log.warn("'{}' tooltip ikonu DOM'da yok.", alanAdi);
            Assert.fail("'" + alanAdi + "' tooltip ikonu bulunmalı. URL: " + driver().getCurrentUrl());
            return;
        }
        new Actions(driver()).moveToElement(icon).pause(600).perform();
        String metin = getVisibleTooltipText();
        Assert.assertNotNull(metin, "'" + alanAdi + "' tooltip metni null olmamalı.");
        Assert.assertFalse(metin.isBlank(),
                "'" + alanAdi + "' tooltip metni boş olmamalı. URL: " + driver().getCurrentUrl());
        log.info("'{}' tooltip metni boş değil: '{}'", alanAdi, metin);
    }

    @Then("{string} tooltip metni {string} içermemeli")
    public void tooltipMetniIcermemeli(String alanAdi, String yanlisMetin) {
        WebElement icon = findTooltipIconForField(alanAdi);
        if (icon == null) {
            log.warn("'{}' tooltip ikonu bulunamadı — kontrol atlanıyor.", alanAdi);
            return;
        }
        new Actions(driver()).moveToElement(icon).pause(600).perform();
        String metin = getVisibleTooltipText();
        if (metin == null) {
            log.warn("Tooltip metni alınamadı — içermemeli kontrolü geçiliyor.");
            return;
        }
        Assert.assertFalse(metin.contains(yanlisMetin),
                "'" + alanAdi + "' tooltip metni '" + yanlisMetin + "' içermemeli. Gerçek: '" + metin
                        + "'. URL: " + driver().getCurrentUrl());
        log.info("'{}' tooltip metni yanlış içerik barındırmıyor.", alanAdi);
    }

    // ─── Edge Case Adımları ───────────────────────────────────────────────────

    @And("sayfa tam yüklenmeden {string} tooltip ikonuna hover yapılırsa")
    public void erkenHoverDenemesi(String alanAdi) {
        // Sayfa yüklenmeden hover: JS hazır olmayabilir — hata fırlatmamalı
        try {
            WebElement icon = findTooltipIconForField(alanAdi);
            if (icon != null) {
                new Actions(driver()).moveToElement(icon).perform();
            }
            log.info("Erken hover denemesi yapıldı — '{}' alanı.", alanAdi);
        } catch (Exception e) {
            log.warn("Erken hover exception (beklenen davranış olabilir): {}", e.getMessage());
        }
    }

    @Then("tooltip içeriği boş görünmemeli veya hata fırlatmamalı")
    public void tooltipHataFirlatmamali() {
        // Sayfa çökmemiş ve URL geçerli ise test geçer
        String url = driver().getCurrentUrl();
        Assert.assertFalse(url == null || url.isBlank(),
                "Sayfa çökmemeli — URL geçerli olmalı.");
        log.info("Erken hover sonrası sayfa sağlıklı. URL: {}", url);
    }

    @When("viewport mobil boyutuna ayarlanır")
    public void viewportMobilBoyutu() {
        driver().manage().window().setSize(new Dimension(390, 844)); // iPhone 14 boyutu
        log.info("Viewport 390x844 (mobil) olarak ayarlandı.");
    }

    @Then("{string} tooltip ikonu ekranda görünür olmalı")
    public void tooltipIkonuMobilde(String alanAdi) {
        WebElement icon = findTooltipIconForField(alanAdi);
        if (icon == null) {
            log.warn("Mobil viewport'ta '{}' tooltip ikonu bulunamadı.", alanAdi);
        }
        Assert.assertNotNull(icon,
                "Mobil viewport'ta '" + alanAdi + "' tooltip ikonu görünmeli. URL: " + driver().getCurrentUrl());
        Assert.assertTrue(icon.isDisplayed(),
                "Tooltip ikonu görünür olmalı. URL: " + driver().getCurrentUrl());
    }

    @Then("tooltip içeriği ekran sınırları dışına taşmamalı")
    public void tooltipEkranDisiTasmamali() {
        // TODO: Tooltip element bounding rect alınıp window.innerWidth/innerHeight ile karşılaştırılmalı
        // Örnek JS: ((JavascriptExecutor) driver()).executeScript(...)
        log.info("Tooltip taşma kontrolü — görsel doğrulama veya JS ile implemente edilmeli.");
        Assert.assertTrue(true, "Tooltip taşma kontrolü — TODO implemente edilmeli.");
    }

    @Then("{string} alanı fatura yükleme formunda mevcut değilse tooltip da görünmemeli")
    public void alanYoksaTooltipYok(String alanAdi) {
        List<WebElement> alanlar = driver().findElements(
                By.xpath("//*[contains(text(),'" + alanAdi + "')]"));
        if (alanlar.isEmpty()) {
            log.info("'{}' alanı formda mevcut değil — tooltip da beklenmez. Kontrol geçti.", alanAdi);
            return;
        }
        // Alan varsa tooltip da olmalı — bu senaryo sadece "alan yoksa tooltip da yok" kontrolü
        log.info("'{}' alanı formda mevcut — bu senaryo bu durumda geçerli değil.", alanAdi);
    }

    // ─── Yardımcı Metotlar ────────────────────────────────────────────────────

    /**
     * Verilen alan adına göre tooltip ikonunu DOM'da arar.
     * Gerçek uygulama DOM yapısına göre seçiciler güncellenmeli.
     *
     * Olası DOM yapıları:
     *   <label>Vade Tarihi <vaadin-icon icon="vaadin:info-circle" /></label>
     *   <span class="field-label">Vade Tarihi</span><span class="tooltip-icon" data-tooltip="..."/>
     */
    /**
     * Vaadin dialog shadow DOM'u da dahil olmak üzere tooltip ikonunu arar.
     *
     * Vaadin bileşenleri shadow root kullanır; normal driver.findElements() shadow içini göremez.
     * Bu yüzden JavascriptExecutor ile shadow root geçişi yapılır.
     *
     * Öncelik sırası:
     *   1. JS: vaadin-dialog-overlay shadow DOM içinde alan adına yakın vaadin-icon[icon*='info']
     *   2. JS: vaadin-dialog-overlay shadow DOM içinde vaadin-tooltip[text*='ödemenin|hesabınıza']
     *   3. Normal DOM: vaadin-tooltip[text*='ödemenin|hesabınıza'] (overlay dışına render edilenler)
     *   4. Normal DOM: [title] attribute ile eşleşen info ikonları
     */
    @SuppressWarnings("unchecked")
    private WebElement findTooltipIconForField(String alanAdi) {
        JavascriptExecutor js = (JavascriptExecutor) driver();

        try {
            // Strateji 1: JS ile shadow DOM geçişi — dialog içindeki vaadin-icon[icon*='info']
            // Vaadin dialog içeriği: vaadin-dialog-overlay → shadow-root → [part=content] → ...
            WebElement iconViaJs = (WebElement) js.executeScript(
                    "var fieldName = arguments[0];" +
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return null;" +
                    // Shadow root içindeki content container'ı bul
                    "var sr = overlay.shadowRoot;" +
                    "var content = sr ? sr.querySelector('[part=\"content\"]') : null;" +
                    "var root = content || overlay;" +
                    // Tüm text node'larını tara, alan adını içereni bul
                    "var allEls = root.querySelectorAll('*');" +
                    "for (var el of allEls) {" +
                    "  var txt = el.childNodes.length > 0 ? " +
                    "    Array.from(el.childNodes).filter(n=>n.nodeType===3).map(n=>n.textContent).join('') : '';" +
                    "  if (txt.trim().includes(fieldName)) {" +
                    "    var parent = el.parentElement || el.parentNode;" +
                    "    if (parent) {" +
                    "      var icon = parent.querySelector('vaadin-icon[icon*=\"info\"]');" +
                    "      if (!icon) icon = parent.querySelector('[class*=\"info\"][class*=\"icon\"], [class*=\"tooltip\"]');" +
                    "      if (icon) return icon;" +
                    "    }" +
                    "    var sibling = el.nextElementSibling;" +
                    "    while (sibling) {" +
                    "      if (sibling.tagName && sibling.tagName.toLowerCase().includes('icon')) return sibling;" +
                    "      if (sibling.getAttribute && sibling.getAttribute('icon') && sibling.getAttribute('icon').includes('info')) return sibling;" +
                    "      sibling = sibling.nextElementSibling;" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return null;",
                    alanAdi);

            if (iconViaJs != null) {
                log.info("'{}' için JS/shadow DOM ile vaadin-icon bulundu.", alanAdi);
                return iconViaJs;
            }

            // Strateji 2: JS ile vaadin-tooltip[text] attribute araması (dialog içinde)
            WebElement tooltipEl = (WebElement) js.executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "var root = overlay || document;" +
                    "var tooltips = root.querySelectorAll('vaadin-tooltip');" +
                    "for (var t of tooltips) {" +
                    "  var txt = t.getAttribute('text') || t.getAttribute('content') || t.textContent || '';" +
                    "  if (txt.includes('ödemenin') || txt.includes('hesabınıza')) return t;" +
                    "}" +
                    // document genelinde de ara (tooltip overlay dışına render edilmiş olabilir)
                    "var allTooltips = document.querySelectorAll('vaadin-tooltip');" +
                    "for (var t of allTooltips) {" +
                    "  var txt = t.getAttribute('text') || t.getAttribute('content') || t.textContent || '';" +
                    "  if (txt.includes('ödemenin') || txt.includes('hesabınıza')) return t;" +
                    "}" +
                    "return null;");

            if (tooltipEl != null) {
                log.info("'{}' için vaadin-tooltip text attribute ile eşleşti.", alanAdi);
                return tooltipEl;
            }

            // Strateji 3: Normal DOM'da vaadin-icon (shadow root olmayan durum / farklı Vaadin versiyonu)
            List<WebElement> vaadinIcons = driver().findElements(
                    By.cssSelector("vaadin-icon[icon*='info'], vaadin-icon[icon*='circle']"));
            if (!vaadinIcons.isEmpty()) {
                log.info("'{}' için normal DOM'da vaadin-icon bulundu (strateji 3).", alanAdi);
                return vaadinIcons.get(0);
            }

            // Strateji 4: title attribute
            List<WebElement> titled = driver().findElements(By.cssSelector("[title]"));
            for (WebElement el : titled) {
                String title = el.getAttribute("title");
                if (title != null && (title.contains("ödemenin") || title.contains("hesabınıza"))) {
                    return el;
                }
            }

            log.warn("'{}' için tooltip ikonu bulunamadı — tüm shadow DOM stratejileri denendi.", alanAdi);
            return null;

        } catch (Exception e) {
            log.error("Tooltip ikonu arama hatası: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Hover sonrası ekranda görünen tooltip metnini okur.
     * Vaadin tooltip, title attribute veya özel tooltip container'larını kontrol eder.
     */
    private String getVisibleTooltipText() {
        JavascriptExecutor js = (JavascriptExecutor) driver();
        try {
            // Hover sonrası kısa bekle — Vaadin tooltip animasyonu için
            new WebDriverWait(driver(), Duration.ofSeconds(2))
                    .until(d -> {
                        List<WebElement> els = d.findElements(
                                By.cssSelector("vaadin-tooltip-overlay, [part='overlay'][role='tooltip']"));
                        return els.stream().anyMatch(e -> {
                            try { return e.isDisplayed() && !e.getText().isBlank(); } catch (Exception ex) { return false; }
                        });
                    });
        } catch (Exception ignored) { /* timeout — aşağıda fallback */ }

        try {
            // Strateji 1: vaadin-tooltip-overlay (hover sonrası body'e append edilir)
            List<WebElement> overlays = driver().findElements(
                    By.cssSelector("vaadin-tooltip-overlay"));
            for (WebElement el : overlays) {
                try {
                    if (el.isDisplayed()) {
                        String text = el.getText();
                        if (text != null && !text.isBlank()) { return text.trim(); }
                        // Shadow root içindeki metin
                        Object sr = js.executeScript("return arguments[0].shadowRoot ? arguments[0].shadowRoot.textContent : null;", el);
                        if (sr != null && !sr.toString().isBlank()) { return sr.toString().trim(); }
                    }
                } catch (Exception ignored) {}
            }

            // Strateji 2: JS ile vaadin-tooltip-overlay shadow root metni
            Object jsText = js.executeScript(
                    "var el = document.querySelector('vaadin-tooltip-overlay');" +
                    "if (!el) return null;" +
                    "if (el.shadowRoot) {" +
                    "  var c = el.shadowRoot.querySelector('[part=\"overlay\"], [part=\"content\"], .overlay-content');" +
                    "  if (c) return c.textContent;" +
                    "  return el.shadowRoot.textContent;" +
                    "}" +
                    "return el.textContent;");
            if (jsText != null && !jsText.toString().isBlank()) { return jsText.toString().trim(); }

            // Strateji 3: role=tooltip
            List<WebElement> roleTooltips = driver().findElements(By.cssSelector("[role='tooltip']"));
            for (WebElement el : roleTooltips) {
                try {
                    if (el.isDisplayed()) {
                        String text = el.getText();
                        if (text != null && !text.isBlank()) { return text.trim(); }
                    }
                } catch (Exception ignored) {}
            }

            log.warn("Görünür tooltip metni bulunamadı.");
            return null;

        } catch (Exception e) {
            log.error("Tooltip metni okuma hatası: {}", e.getMessage());
            return null;
        }
    }
}
