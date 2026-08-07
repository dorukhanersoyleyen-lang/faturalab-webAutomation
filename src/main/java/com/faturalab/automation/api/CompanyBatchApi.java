package com.faturalab.automation.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.faturalab.automation.config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * DTS (Doğrudan Tahsilat Sistemi) — Ticari İşletme (company) entegrasyon REST API'si.
 *
 * Base path: {@code /app/api/integration/company/v0} — {@link FaturalabAPI}'nin kullandığı
 * {@code /app/api/integration/buyer/v0} ile AYNI kalıp (form-urlencoded, tek JSON parametre,
 * {@code FLINTEGRATIONHEADERPARAMS} header'ı ile apiKey/sessionId taşınır).
 *
 * Akış (Python probu ile 2026-08-06'da dev.faturalab.com'da canlı doğrulandı):
 *   authenticate → batch/start → batch/upload → batch/end
 * Etki: {@code dts.status} STARTED → (upload) → DRAFT.
 *
 * Kimlik bilgileri {@code dev.properties} > {@code dts.api.*} anahtarlarından okunur
 * (Test Otomasyon Sadece Tedarikçi, company.id=998 — izole test firması).
 */
public class CompanyBatchApi {

    private static final Logger log = LogManager.getLogger(CompanyBatchApi.class);
    private static final String BASE_PATH = "/api/integration/company/v0";
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Resmî TR tatilleri (sabit tarihli) — {@code TzfInvoiceExcelGenerator} ile aynı set. */
    private static final Set<MonthDay> TR_HOLIDAYS = new HashSet<>(Arrays.asList(
            MonthDay.of(1, 1), MonthDay.of(4, 23), MonthDay.of(5, 1), MonthDay.of(5, 19),
            MonthDay.of(7, 15), MonthDay.of(8, 30), MonthDay.of(10, 29)));

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String host;
    private final String alias;
    private final String password;
    private final String taxNumber;
    private final String apiKey;
    private final String userEmail;
    private final String dealerTaxNo;

    private String sessionId;
    private Response lastResponse;

    public CompanyBatchApi() {
        // base.url = https://dev.faturalab.com/app (dev.properties)
        this.host = ConfigReader.getProperty("base.url", "https://dev.faturalab.com/app");
        this.alias = ConfigReader.getProperty("dts.api.alias");
        this.password = ConfigReader.getProperty("dts.api.password");
        this.taxNumber = ConfigReader.getProperty("dts.api.taxnumber");
        this.apiKey = ConfigReader.getProperty("dts.api.apikey");
        this.userEmail = ConfigReader.getProperty("dts.api.useremail");
        this.dealerTaxNo = ConfigReader.getProperty("dts.api.dealer.taxno");

        RestAssured.useRelaxedHTTPSValidation();
        log.info("CompanyBatchApi initialized for host: {}", host);
    }

    // ─── Unique test verisi üreticileri (her koşum benzersiz — statik fixture YASAK) ───

    public static String generateUniqueDtsReferenceNo() {
        return "DTS_AUTOTEST_" + (System.currentTimeMillis() / 1000L);
    }

    public static String generateUniqueBatchNo() {
        return "BATCH_AUTOTEST_" + System.currentTimeMillis()
                + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(java.util.Locale.ROOT);
    }

    public static String generateUniqueInvoiceNo() {
        return "INV_AUTOTEST_" + System.currentTimeMillis()
                + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(java.util.Locale.ROOT);
    }

    // ─── Hafta sonu / resmi tatil ileri-kaydırma (batchDueDate/batchClosureDate İŞ GÜNÜ olmalı) ───

    private static boolean isBusinessDay(LocalDate d) {
        return d.getDayOfWeek() != DayOfWeek.SATURDAY
                && d.getDayOfWeek() != DayOfWeek.SUNDAY
                && !TR_HOLIDAYS.contains(MonthDay.from(d));
    }

    /** Verilen tarihten itibaren en yakın (kendisi dahil) iş gününü döner. */
    public static LocalDate nextBusinessDay(LocalDate from) {
        LocalDate d = from;
        while (!isBusinessDay(d)) {
            d = d.plusDays(1);
        }
        return d;
    }

    /** Bugünden {@code days} gün sonrasının en yakın iş gününü döner (vade/kapanış tarihi). */
    public static LocalDate getFutureBusinessDate(int days) {
        return nextBusinessDay(LocalDate.now().plusDays(days));
    }

    // ─── HTTP header ───

    private String buildHeaderParams() {
        Map<String, String> headerParams = new HashMap<>();
        headerParams.put("apiKey", apiKey);
        if (sessionId != null && !sessionId.isEmpty()) {
            headerParams.put("sessionId", sessionId);
        }
        try {
            return objectMapper.writeValueAsString(headerParams);
        } catch (Exception e) {
            throw new RuntimeException("FLINTEGRATIONHEADERPARAMS üretilemedi", e);
        }
    }

    // ─── 1. authenticate ───

    public Response authenticate() {
        log.info("=== DTS COMPANY BATCH API — AUTHENTICATE ===");
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("alias", alias);
            body.put("password", password);
            body.put("taxNumber", taxNumber);
            String requestParam = objectMapper.writeValueAsString(body);

            String headerValue = buildHeaderParams();
            log.info("Endpoint: POST {}{}/authenticate/", host, BASE_PATH);
            log.info("FLINTEGRATIONHEADERPARAMS: {}", headerValue);
            log.info("authenticateParam: {}", requestParam);

            lastResponse = RestAssured.given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("authenticateParam", requestParam)
                    .when()
                    .post(host + BASE_PATH + "/authenticate/");

            log.info("Authenticate response [{}]: {}", lastResponse.getStatusCode(), lastResponse.getBody().asString());

            if (lastResponse.getStatusCode() == 200) {
                JsonNode result = objectMapper.readTree(lastResponse.getBody().asString()).path("result");
                String sid = result.path("sessionId").asText(null);
                if (sid != null && !sid.isEmpty()) {
                    this.sessionId = sid;
                    log.info("✅ sessionId alındı: {}", sessionId);
                }
            }
            return lastResponse;
        } catch (Exception e) {
            log.error("DTS authenticate başarısız", e);
            throw new RuntimeException("DTS authenticate başarısız", e);
        }
    }

    // ─── 2. batch/start ───

    public Response startBatch(String dtsReferenceNo, int batchCount, int invoiceCount, int paymentDocCount) {
        log.info("=== DTS COMPANY BATCH API — BATCH/START ({}) ===", dtsReferenceNo);
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("dtsReferenceNo", dtsReferenceNo);
            body.put("batchCount", batchCount);
            body.put("invoiceCount", invoiceCount);
            body.put("paymentDocCount", paymentDocCount);
            body.put("userEmail", userEmail);
            String requestParam = objectMapper.writeValueAsString(body);

            String headerValue = buildHeaderParams();
            log.info("Endpoint: POST {}{}/batch/start", host, BASE_PATH);
            log.info("startBatchParam: {}", requestParam);

            lastResponse = RestAssured.given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("startBatchParam", requestParam)
                    .when()
                    .post(host + BASE_PATH + "/batch/start");

            log.info("Batch start response [{}]: {}", lastResponse.getStatusCode(), lastResponse.getBody().asString());
            return lastResponse;
        } catch (Exception e) {
            log.error("DTS batch/start başarısız", e);
            throw new RuntimeException("DTS batch/start başarısız", e);
        }
    }

    // ─── 3. batch/upload ───

    /** Fatura satırı — {@code invoices[]} elemanı. */
    public static class BatchInvoiceItem {
        public final String invoiceNo;
        public final String invoiceType; // E_FATURA/E_ARSIV/PAPER/PROFORMA/ESLEME_E_FATURA/ESLEME_E_ARSIV/ESLEME_MATBU/E_MUSTAHSIL
        public final LocalDate invoiceDate;
        public final LocalDate dueDate;
        public final double payableAmount;
        public final double remainingAmount;
        public final double invoiceAmount;
        public final double taxExclusiveAmount;
        public final String currency; // TL/EUR/USD/AED/GBP
        public final String customerId; // dealerTaxNo

        public BatchInvoiceItem(String invoiceNo, String invoiceType, LocalDate invoiceDate, LocalDate dueDate,
                                 double payableAmount, double remainingAmount, double invoiceAmount,
                                 double taxExclusiveAmount, String currency, String customerId) {
            this.invoiceNo = invoiceNo;
            this.invoiceType = invoiceType;
            this.invoiceDate = invoiceDate;
            this.dueDate = dueDate;
            this.payableAmount = payableAmount;
            this.remainingAmount = remainingAmount;
            this.invoiceAmount = invoiceAmount;
            this.taxExclusiveAmount = taxExclusiveAmount;
            this.currency = currency;
            this.customerId = customerId;
        }
    }

    public Response uploadBatch(String dtsReferenceNo, String batchNo, double totalAmount,
                                 LocalDate batchDueDate, LocalDate batchClosureDate,
                                 List<BatchInvoiceItem> invoices) {
        log.info("=== DTS COMPANY BATCH API — BATCH/UPLOAD ({} / {}) ===", dtsReferenceNo, batchNo);
        try {
            // ⚠️ batchDueDate/batchClosureDate iş günü olmalı — hafta sonuna denk gelirse
            // INVALID_DUE_DATE_HOLIDAY döner. Çağıran taraf getFutureBusinessDate ile üretmeli;
            // burada da savunmacı olarak ileri-kaydırılır.
            LocalDate dueDate = nextBusinessDay(batchDueDate);
            LocalDate closureDate = nextBusinessDay(batchClosureDate);

            ObjectNode body = objectMapper.createObjectNode();
            body.put("dtsReferenceNo", dtsReferenceNo);
            body.put("userEmail", userEmail);
            body.put("totalAmount", totalAmount);
            body.put("referenceNo", batchNo);
            body.put("dealerTaxNo", dealerTaxNo);
            body.put("batchDueDate", dueDate.format(ISO_DATE));
            body.put("batchClosureDate", closureDate.format(ISO_DATE));
            body.put("batchNo", batchNo);
            body.put("type", "B");

            ArrayNode invoiceArray = body.putArray("invoices");
            for (BatchInvoiceItem inv : invoices) {
                ObjectNode invNode = invoiceArray.addObject();
                invNode.put("invoiceNo", inv.invoiceNo);
                invNode.put("invoiceType", inv.invoiceType);
                invNode.put("invoiceDate", inv.invoiceDate.format(ISO_DATE));
                invNode.put("dueDate", inv.dueDate.format(ISO_DATE));
                invNode.put("payableAmount", inv.payableAmount);
                invNode.put("remainingAmount", inv.remainingAmount);
                invNode.put("invoiceAmount", inv.invoiceAmount);
                invNode.put("taxExclusiveAmount", inv.taxExclusiveAmount);
                invNode.put("currency", inv.currency);
                invNode.put("customerId", inv.customerId);
            }
            body.putArray("paymentDocs");

            String requestParam = objectMapper.writeValueAsString(body);
            String headerValue = buildHeaderParams();
            log.info("Endpoint: POST {}{}/batch/upload", host, BASE_PATH);
            log.info("uploadBatchParam: {}", requestParam);

            lastResponse = RestAssured.given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("uploadBatchParam", requestParam)
                    .when()
                    .post(host + BASE_PATH + "/batch/upload");

            log.info("Batch upload response [{}]: {}", lastResponse.getStatusCode(), lastResponse.getBody().asString());
            return lastResponse;
        } catch (Exception e) {
            log.error("DTS batch/upload başarısız", e);
            throw new RuntimeException("DTS batch/upload başarısız", e);
        }
    }

    // ─── 4. batch/end ───

    public Response endBatch(String dtsReferenceNo, int batchCount, int invoiceCount, int paymentDocCount) {
        log.info("=== DTS COMPANY BATCH API — BATCH/END ({}) ===", dtsReferenceNo);
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("dtsReferenceNo", dtsReferenceNo);
            body.put("batchCount", batchCount);
            body.put("invoiceCount", invoiceCount);
            body.put("paymentDocCount", paymentDocCount);
            body.put("userEmail", userEmail);
            String requestParam = objectMapper.writeValueAsString(body);

            String headerValue = buildHeaderParams();
            log.info("Endpoint: POST {}{}/batch/end", host, BASE_PATH);
            log.info("batchEndParam: {}", requestParam);

            lastResponse = RestAssured.given()
                    .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                    .accept("application/json")
                    .header("FLINTEGRATIONHEADERPARAMS", headerValue)
                    .formParam("batchEndParam", requestParam)
                    .when()
                    .post(host + BASE_PATH + "/batch/end");

            log.info("Batch end response [{}]: {}", lastResponse.getStatusCode(), lastResponse.getBody().asString());
            return lastResponse;
        } catch (Exception e) {
            log.error("DTS batch/end başarısız", e);
            throw new RuntimeException("DTS batch/end başarısız", e);
        }
    }

    // ─── Yardımcı okuma metodları ───

    public boolean isSuccess(Response response) {
        if (response == null) {
            return false;
        }
        try {
            JsonNode node = objectMapper.readTree(response.getBody().asString());
            return node.path("success").asBoolean(false);
        } catch (Exception e) {
            log.error("Response success ayrıştırılamadı", e);
            return false;
        }
    }

    public String getMessage(Response response) {
        if (response == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(response.getBody().asString());
            return node.path("result").path("message").asText(null);
        } catch (Exception e) {
            log.error("Response mesajı ayrıştırılamadı", e);
            return null;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public String getDealerTaxNo() {
        return dealerTaxNo;
    }
}
