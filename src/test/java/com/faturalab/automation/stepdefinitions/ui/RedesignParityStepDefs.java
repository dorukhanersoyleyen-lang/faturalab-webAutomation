package com.faturalab.automation.stepdefinitions.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.utils.parity.AxeCheck;
import com.faturalab.automation.utils.parity.ParityAssert;
import com.faturalab.automation.utils.parity.ParityFinding;
import com.faturalab.automation.utils.parity.UiFingerprint;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Redesign parity (K1 fonksiyonel parmak izi + K4 erişilebilirlik) adımları.
 *
 * <p>Eski tasarım baseline'ı {@code testdata/parity/<ekran>.json} içinde dondurulmuştur (kaynak: dev.rakbank.faturalab.ae,
 * Dasein geçişinden önceki jar). Yeni ekran canlıda ölçülür; eksik alan/buton/kural ve klavye erişimi kaybı FAIL'dir.
 * Rapor: {@code target/parity/<ekran>_rapor.md}. Mimari: v2_QA/output/reports/fe_pixel_perfection_mimari_20261015.md
 */
public class RedesignParityStepDefs {

    private static final Logger log = LogManager.getLogger(RedesignParityStepDefs.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path OUT_DIR = Paths.get("target", "parity");
    private static final String DEFAULT_OLD_URL = "https://dev.rakbank.faturalab.ae";
    private static final long PAGE_TIMEOUT_MS = 30_000;
    private static final long STABLE_TIMEOUT_MS = 15_000;
    private static final long POLL_MS = 500;
    /** Eski ve yeni giriş ekranında var, kayıt/şifremi unuttum ekranlarında yok → yükleme ve geçiş sinyali. */
    private static final String LOGIN_MARKER = "Beni hatırla";

    /** Giriş öncesi ekranlara ulaşmak için giriş ekranında tıklanacak metin (boş = giriş ekranının kendisi). */
    private static final Map<String, String> SCREEN_ENTRY = Map.of(
            "giris", "",
            "kayit_adim1", "Kayıt Ol",
            "sifremi_unuttum", "Şifremi Unuttum");

    /** Shadow DOM dahil, TR-duyarsız; vaadin-button veya tıklanabilir yaprak öğe. */
    private static final String CLICK_BY_TEXT_JS =
            "const fold=s=>(s||'').toLocaleLowerCase('tr').replace(/\\s+/g,' ').trim();"
            + "const walk=(n,a)=>{for(const e of n.querySelectorAll('*')){a.push(e);if(e.shadowRoot)walk(e.shadowRoot,a)}return a};"
            + "const el=walk(document,[]).find(e=>(e.tagName==='VAADIN-BUTTON'||(e.children.length===0&&getComputedStyle(e).cursor==='pointer'))"
            + "&&fold(e.textContent)===fold(arguments[0])&&e.getBoundingClientRect().width>1);"
            + "if(!el)return false;el.click();return true;";

    private Scenario scenario;
    private String screen;
    private String env;
    private JsonNode fingerprint;
    private ObjectNode axeSummary;
    private boolean reportStarted;
    private final List<ParityFinding> findings = new ArrayList<>();

    @Before("@parity or @parity-capture")
    public void bindScenario(Scenario sc) {
        this.scenario = sc;
    }

    @Given("{string} ekranı {string} tasarım ortamında açılır")
    public void ekranAcilir(String ekran, String ortam) {
        Assert.assertTrue("eski".equals(ortam) || "yeni".equals(ortam), "Ortam 'eski' veya 'yeni' olmalı: '" + ortam + "'");
        String entry = SCREEN_ENTRY.get(ekran);
        Assert.assertNotNull(entry, "Tanımsız parity ekranı: " + ekran + " (SCREEN_ENTRY'ye ekleyin)");
        this.screen = ekran;
        this.env = ortam;
        WebDriver driver = DriverManager.getDriver();
        String url = "eski".equals(ortam)
                ? ConfigReader.getProperty("parity.old.url", DEFAULT_OLD_URL)
                : ConfigReader.getProperty("base.url");
        log.info("[PARITY] {} ekranı açılıyor ({} → {})", ekran, ortam, url);
        driver.get(url);
        Assert.assertTrue(waitForText(driver, LOGIN_MARKER, true),
                "Giriş ekranı " + PAGE_TIMEOUT_MS + " ms içinde yüklenmedi: " + url);
        if (!entry.isEmpty()) {
            Assert.assertTrue(clickByText(driver, entry), "'" + entry + "' öğesi bulunamadı/tıklanamadı (" + ortam + ")");
            // Geçiş tamamlanmadan ölçüm alınırsa giriş ekranı ölçülür → giriş ekranına özgü işaret kaybolana kadar bekle
            Assert.assertTrue(waitForText(driver, LOGIN_MARKER, false),
                    "'" + entry + "' tıklandı ama ekran " + PAGE_TIMEOUT_MS + " ms içinde değişmedi (" + ortam + ")");
        }
    }

    @When("ekranın UI parmak izi alınır")
    public void parmakIziAlinir() throws IOException {
        Assert.assertNotNull(screen, "Önce \"<ekran> ekranı <ortam> tasarım ortamında açılır\" adımı çalışmalı");
        fingerprint = UiFingerprint.captureStable(DriverManager.getDriver(), STABLE_TIMEOUT_MS);
        write(env + "/" + screen + "_parmak_izi.json", pretty(fingerprint));
    }

    @When("ekranın erişilebilirlik taraması yapılır")
    public void axeTaramasi() throws IOException {
        Assert.assertNotNull(screen, "Önce \"<ekran> ekranı <ortam> tasarım ortamında açılır\" adımı çalışmalı");
        axeSummary = AxeCheck.scan(DriverManager.getDriver());
        write(env + "/" + screen + "_axe.json", pretty(axeSummary));
        log.info("[PARITY] axe ({}/{}): {}", env, screen, axeSummary.path("byImpact"));
    }

    // Karşılaştırma adımları bulguları TOPLAR, senaryoyu düşürmez — böylece parmak izi FAIL olsa da axe değerlendirilir.
    // Hüküm son adımda verilir ("parity bulgularında FAIL yoktur").
    @Then("parmak izi {string} baseline'ı ile karşılaştırılır")
    public void baselineIleKarsilastir(String ekran) throws IOException {
        Assert.assertNotNull(fingerprint, "Önce \"ekranın UI parmak izi alınır\" adımı çalışmalı");
        recordFindings(ekran, "Fonksiyonel parity", ParityAssert.compare(loadSpec(ekran), fingerprint));
    }

    @Then("erişilebilirlik ihlalleri {string} baseline'ı ile karşılaştırılır")
    public void axeKarsilastir(String ekran) throws IOException {
        Assert.assertNotNull(axeSummary, "Önce \"ekranın erişilebilirlik taraması yapılır\" adımı çalışmalı");
        JsonNode baselineRules = loadSpec(ekran).path("rules").path("axeBaseline");
        Assert.assertTrue(baselineRules.isArray(), "Spec'te rules.axeBaseline (eski tasarım ihlal listesi) yok: " + ekran
                + " — eski ekranda ihlal yoksa boş liste [] yazın");
        recordFindings(ekran, "Erişilebilirlik (axe)", AxeCheck.compare(axeSummary, baselineRules));
    }

    @Then("parity bulgularında FAIL yoktur")
    public void failYok() {
        String fails = findings.stream().filter(f -> f.getSeverity() == ParityFinding.Severity.FAIL)
                .map(ParityFinding::toString).collect(Collectors.joining("\n  - "));
        Assert.assertTrue(fails.isEmpty(), screen + " ekranında redesign parity FAIL:\n  - " + fails);
    }

    /** Baseline yakalama modu: eski tasarımdan alınan ölçüm, testdata/parity'ye elle dondurulmak üzere kaydedilir. */
    @Then("ölçüm baseline adayı olarak kaydedilir")
    public void baselineAdayi() throws IOException {
        Assert.assertNotNull(fingerprint, "Önce \"ekranın UI parmak izi alınır\" adımı çalışmalı");
        ObjectNode cand = MAPPER.createObjectNode();
        cand.put("screen", screen);
        cand.put("source", DriverManager.getDriver().getCurrentUrl());
        cand.put("capturedAt", OffsetDateTime.now().toString());
        cand.set("baseline", fingerprint);
        cand.set("axe", axeSummary);
        String json = pretty(cand);
        write("captured/" + screen + ".json", json);
        attach(json, "application/json", "baseline adayı: " + screen);
    }

    // ------------------------------------------------------------------ yardımcılar
    private void recordFindings(String ekran, String title, List<ParityFinding> found) throws IOException {
        findings.addAll(found);
        StringBuilder md = new StringBuilder("## ").append(title).append(" — ").append(ekran).append('\n');
        if (found.isEmpty()) {
            md.append("- ✅ Fark yok\n");
        }
        for (ParityFinding f : found) {
            md.append("- **").append(f.getSeverity()).append("** [").append(f.getCategory()).append("] ")
                    .append(f.getMessage()).append('\n');
        }
        String report = md.toString();
        Files.createDirectories(OUT_DIR);
        // Senaryodaki ilk yazım dosyayı sıfırlar (önceki koşumların bulguları karışmasın), sonrakiler ekler
        StandardOpenOption mode = reportStarted ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
        reportStarted = true;
        Files.write(OUT_DIR.resolve(ekran + "_rapor.md"), report.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, mode);
        attach(report, "text/plain", title + " — " + ekran);
        found.forEach(f -> log.info("[PARITY] {}", f));
    }

    private void attach(String content, String mediaType, String name) {
        if (scenario != null) {
            scenario.attach(content, mediaType, name);
        }
    }

    private static JsonNode loadSpec(String ekran) throws IOException {
        String res = "testdata/parity/" + ekran + ".json";
        try (InputStream in = RedesignParityStepDefs.class.getClassLoader().getResourceAsStream(res)) {
            Assert.assertNotNull(in, "Parity spec bulunamadı: " + res);
            return MAPPER.readTree(in);
        }
    }

    private static String pretty(JsonNode node) throws IOException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    private static void write(String rel, String content) throws IOException {
        Path p = OUT_DIR.resolve(rel);
        Files.createDirectories(p.getParent());
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean clickByText(WebDriver driver, String text) {
        long deadline = System.currentTimeMillis() + PAGE_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(CLICK_BY_TEXT_JS, text))) {
                return true;
            }
            if (!sleep()) {
                return false;
            }
        }
        return false;
    }

    /**
     * Metnin sayfada görünmesini ({@code present=true}) veya kaybolmasını bekler. innerText CSS text-transform
     * uyguladığı için ("ŞİFREMİ UNUTTUM") iki taraf da TR-fold'dan geçirilir.
     */
    private static boolean waitForText(WebDriver driver, String text, boolean present) {
        String needle = ParityAssert.norm(text);
        long deadline = System.currentTimeMillis() + PAGE_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            Object body = ((JavascriptExecutor) driver).executeScript("return document.body ? document.body.innerText : ''");
            if (ParityAssert.norm(String.valueOf(body)).contains(needle) == present) {
                return true;
            }
            if (!sleep()) {
                return false;
            }
        }
        return false;
    }

    /** @return false: thread kesildi, bekleme döngüsü bırakılmalı */
    private static boolean sleep() {
        try {
            Thread.sleep(POLL_MS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
