package com.faturalab.automation.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * QA Hub entegrasyonu: Her UI otomasyon koşusu için bir TestRun oluşturur
 * ve her senaryo bitiminde sonucu http://localhost:3030 API'sine gönderir.
 *
 * Senaryo adı formatı: "TC-COMP-01-001 - ..." → FL-COMP-01-001 olarak eşlenir.
 * Servis erişilemez ise sessizce devre dışı kalır (testleri engellemez).
 */
public class QAHubReporter {

    private static final Logger log = LogManager.getLogger(QAHubReporter.class);

    private static final String BASE_URL = "http://localhost:3030";
    private static final int PROJECT_ID = 1;
    private static final int SUITE_ID   = 1;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private static final ObjectMapper JSON = new ObjectMapper();

    // runId: o otomasyon koşusuna ait QA Hub run ID'si
    private static volatile Integer runId = null;
    // tcId → QA Hub caseId haritası (TC-COMP-01-001 ve FL-COMP-01-001 her ikisi de kayıtlı)
    private static final Map<String, Integer> caseMap = new ConcurrentHashMap<>();
    private static volatile boolean initialized = false;
    private static volatile boolean available   = true;

    /**
     * İlk @Before("@ui") çağrısında koşuyu başlatır.
     * Sonraki çağrılarda no-op.
     */
    public static synchronized void initRun() {
        if (initialized) return;
        initialized = true;

        try {
            // QA Hub erişilebilirlik kontrolü
            HttpResponse<String> ping = get("/api/projects");
            if (ping.statusCode() != 200) {
                log.warn("[QAHub] Servis erişilemez ({}), raporlama devre dışı.", ping.statusCode());
                available = false;
                return;
            }

            // Tüm test case'leri yükle → tcId/caseId haritası
            loadCaseMap();

            // Yeni TestRun oluştur
            String name = "UI Otomasyon - "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String body = JSON.writeValueAsString(Map.of(
                    "name",        name,
                    "description", "Maven Selenium UI otomasyon koşusu",
                    "projectId",   PROJECT_ID,
                    "suiteId",     SUITE_ID
            ));

            HttpResponse<String> resp = post("/api/runs", body);
            if (resp.statusCode() == 201) {
                runId = JSON.readTree(resp.body()).path("id").asInt();
                log.info("[QAHub] Test koşusu oluşturuldu → runId={} name='{}'", runId, name);
            } else {
                log.warn("[QAHub] Koşu oluşturulamadı: {} {}", resp.statusCode(), resp.body());
                available = false;
            }

        } catch (Exception e) {
            log.warn("[QAHub] Bağlantı hatası — raporlama devre dışı: {}", e.getMessage());
            available = false;
        }
    }

    /**
     * Senaryo sonucunu QA Hub'a gönderir.
     *
     * @param scenarioName Cucumber senaryo adı (ör. "TC-COMP-01-001 - Gecerli XML...")
     * @param status       Cucumber status stringi: PASSED / FAILED / SKIPPED / PENDING / UNDEFINED
     * @param errorMessage Başarısızlık mesajı (null olabilir)
     */
    public static void reportResult(String scenarioName, String status, String errorMessage) {
        if (!available || runId == null) return;

        try {
            String tcId = extractTcId(scenarioName);
            if (tcId == null) {
                log.debug("[QAHub] TC ID çıkarılamadı: '{}'", scenarioName);
                return;
            }

            Integer caseId = caseMap.get(tcId);
            if (caseId == null) {
                log.warn("[QAHub] Case bulunamadı — tcId='{}' (senaryo: '{}')", tcId, scenarioName);
                return;
            }

            String qaStatus = toQaStatus(status);
            String comment  = buildComment(status, errorMessage);

            String body = JSON.writeValueAsString(
                    comment != null
                    ? Map.of("runId", runId, "caseId", caseId, "status", qaStatus, "comment", comment)
                    : Map.of("runId", runId, "caseId", caseId, "status", qaStatus)
            );

            HttpResponse<String> resp = patch("/api/results", body);
            if (resp.statusCode() == 200) {
                log.info("[QAHub] Sonuç kaydedildi: {} → {} (caseId={})", tcId, qaStatus, caseId);
            } else {
                log.warn("[QAHub] Sonuç kaydedilemedi: {} {}", resp.statusCode(), resp.body());
            }

        } catch (Exception e) {
            log.warn("[QAHub] Sonuç gönderilemedi: {}", e.getMessage());
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private static void loadCaseMap() throws Exception {
        HttpResponse<String> resp = get("/api/cases?projectId=" + PROJECT_ID);
        JsonNode cases = JSON.readTree(resp.body());
        int count = 0;
        for (JsonNode c : cases) {
            int    id   = c.path("id").asInt();
            String tcId = c.path("tcId").asText(null);
            if (tcId == null || id == 0) continue;
            caseMap.put(tcId, id);
            // FL- ↔ TC- çift yönlü eşleme
            if (tcId.startsWith("FL-")) caseMap.put("TC-" + tcId.substring(3), id);
            else if (tcId.startsWith("TC-")) caseMap.put("FL-" + tcId.substring(3), id);
            count++;
        }
        log.info("[QAHub] {} test case yüklendi.", count);
    }

    /** "TC-COMP-01-001 - Açıklama" → "TC-COMP-01-001" */
    private static String extractTcId(String scenarioName) {
        if (scenarioName == null || scenarioName.isEmpty()) return null;
        String first = scenarioName.split("\\s")[0]; // ilk boşluğa kadar
        // Tire ile bitiyorsa temizle
        while (first.endsWith("-")) first = first.substring(0, first.length() - 1);
        return first.isEmpty() ? null : first;
    }

    private static String toQaStatus(String cucumberStatus) {
        if (cucumberStatus == null) return "untested";
        switch (cucumberStatus.toUpperCase()) {
            case "PASSED":    return "passed";
            case "FAILED":    return "failed";
            case "SKIPPED":   return "skipped";
            case "PENDING":   return "skipped";
            case "UNDEFINED": return "skipped";
            default:          return "untested";
        }
    }

    private static String buildComment(String status, String errorMessage) {
        if (!"FAILED".equalsIgnoreCase(status) || errorMessage == null) return null;
        String clean = errorMessage.replace("\r", "").replace("\n", " ").replace("\"", "'");
        return clean.length() > 500 ? clean.substring(0, 500) + "..." : clean;
    }

    // ─── HTTP ─────────────────────────────────────────────────────────────────

    private static HttpResponse<String> get(String path) throws Exception {
        return HTTP.send(
            HttpRequest.newBuilder().uri(URI.create(BASE_URL + path)).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }

    private static HttpResponse<String> post(String path, String body) throws Exception {
        return HTTP.send(
            HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }

    private static HttpResponse<String> patch(String path, String body) throws Exception {
        return HTTP.send(
            HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
    }
}
