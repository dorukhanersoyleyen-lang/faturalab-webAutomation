package com.faturalab.automation.utils.parity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Ekranın fonksiyonel parmak izini (alan/buton/link/checkbox/klavye erişimi/bileşen envanteri) çıkarır.
 *
 * <p>JS kaynağı: {@code src/main/resources/parity/ui_fingerprint.js} — shadow DOM dahil gezer.
 * Eski ↔ yeni tasarım karşılaştırmasında piksel yerine bu envanter kullanılır (tenant/logo farkından etkilenmez).
 */
public final class UiFingerprint {

    private static final Logger log = LogManager.getLogger(UiFingerprint.class);
    private static final String JS_RESOURCE = "parity/ui_fingerprint.js";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long STABLE_POLL_MS = 700;
    private static String jsSource;

    private UiFingerprint() {
    }

    /** Tek seferlik parmak izi. */
    public static JsonNode capture(WebDriver driver) {
        Object raw = ((JavascriptExecutor) driver).executeScript(
                script() + "\nreturn JSON.stringify(uiFingerprint(arguments[0]));", (Object) null);
        try {
            return MAPPER.readTree(String.valueOf(raw));
        } catch (IOException e) {
            throw new IllegalStateException("Parmak izi JSON'u okunamadı: " + e.getMessage(), e);
        }
    }

    /**
     * Vaadin render'ı oturana kadar bekler: ardışık iki ölçümde alan+buton+checkbox envanteri aynı
     * ve en az bir alan/buton var ise o ölçümü döner. Zaman aşımında son ölçüm döner (uyarı loglanır).
     */
    public static JsonNode captureStable(WebDriver driver, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        JsonNode prev = null;
        JsonNode cur = capture(driver);
        while (System.currentTimeMillis() < deadline) {
            if (prev != null && isNonEmpty(cur) && signature(prev).equals(signature(cur))) {
                return cur;
            }
            sleep(STABLE_POLL_MS);
            prev = cur;
            cur = capture(driver);
        }
        log.warn("[PARITY] Parmak izi {} ms içinde stabil olmadı; son ölçüm kullanılıyor", timeoutMs);
        return cur;
    }

    private static boolean isNonEmpty(JsonNode fp) {
        return fp.path("fields").size() + fp.path("buttons").size() > 0;
    }

    private static String signature(JsonNode fp) {
        return fp.path("fields").toString() + fp.path("buttons") + fp.path("checkboxes") + fp.path("pseudoClickables");
    }

    private static synchronized String script() {
        if (jsSource == null) {
            try (InputStream in = UiFingerprint.class.getClassLoader().getResourceAsStream(JS_RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException("Classpath'te bulunamadı: " + JS_RESOURCE);
                }
                jsSource = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException("Parmak izi JS'i okunamadı: " + e.getMessage(), e);
            }
        }
        return jsSource;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
