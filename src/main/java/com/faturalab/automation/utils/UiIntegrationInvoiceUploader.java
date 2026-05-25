package com.faturalab.automation.utils;

import com.faturalab.automation.api.FaturalabAPI;
import com.faturalab.automation.config.ConfigReader;
import com.faturalab.automation.config.EnvironmentManager;
import com.faturalab.automation.models.invoice.UploadInvoiceRequest;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Alıcı (buyer) entegrasyon API’si ile E-fatura yüklemek için köprü — yalnızca alıcı tarafı
 * senaryolarında kullanılmalıdır; tedarikçi fatura yükleme UI ile yapılır.
 * <p>Yapılandırma {@code dev2.properties} (veya {@code -D}) üzerinden okunur.</p>
 *
 * <p>Gerekli özellikler (en az {@code integration.api.key}, {@code integration.api.alias},
 * {@code integration.api.password} dolu olmalı):</p>
 * <ul>
 *   <li>{@code integration.api.host} — yoksa buyer v0 varsayılanı</li>
 *   <li>{@code integration.api.user.email} — yoksa {@code buyer.email}</li>
 *   <li>Authenticate {@code taxNumber}: {@code integration.api.auth.taxno} veya {@code test.matched.buyer.taxno}</li>
 *   <li>Fatura gövdesi {@code supplierTaxNo}: {@code integration.supplier.taxno} veya {@code test.matched.company.taxno}</li>
 * </ul>
 */
public final class UiIntegrationInvoiceUploader {

    private static final Logger log = LogManager.getLogger(UiIntegrationInvoiceUploader.class);

    private UiIntegrationInvoiceUploader() {
    }

    public static boolean isApiUploadConfigured() {
        String key = ConfigReader.getProperty("integration.api.key", "");
        String alias = ConfigReader.getProperty("integration.api.alias", "");
        String password = ConfigReader.getProperty("integration.api.password", "");
        return key != null && !key.isBlank()
                && alias != null && !alias.isBlank()
                && password != null && !password.isBlank();
    }

    /**
     * Alıcı entegrasyon API’si: authenticate (alıcı VKN) + E-Fatura upload (faturadaki tedarikçi VKN).
     *
     * @return API başarılıysa true; yapılandırma eksik veya hata varsa false
     */
    public static boolean tryUploadSampleEInvoiceAsBuyer() {
        if (!isApiUploadConfigured()) {
            log.info("integration.api.* eksik — alıcı API fatura yükleme atlanıyor.");
            return false;
        }
        String buyerTax = buyerAuthTaxNo();
        if (buyerTax == null || buyerTax.isBlank()) {
            log.warn("Alıcı VKN yok — integration.api.auth.taxno veya test.matched.buyer.taxno doldurun.");
            return false;
        }
        String supplierTax = supplierTaxNoOnInvoice();
        if (supplierTax == null || supplierTax.isBlank()) {
            log.warn("Fatura tedarikçi VKN yok — integration.supplier.taxno veya test.matched.company.taxno doldurun.");
            return false;
        }
        try {
            EnvironmentManager.EnvironmentConfig env = buildEnvForBuyerApi(buyerTax);
            FaturalabAPI api = new FaturalabAPI(env);

            Response auth = api.authenticate();
            if (auth.getStatusCode() != 200 || !api.isResponseSuccessful()) {
                log.warn("API authenticate başarısız: status={} body={}",
                        auth.getStatusCode(), auth.getBody().asString());
                return false;
            }

            String userEmail = env.getUserEmail();
            int amount = parseAmount();

            String today = InvoiceTestDataGenerator.getCurrentDate();
            String due = InvoiceTestDataGenerator.getFutureWorkingDate(60);
            String invoiceNo = "UI-BUYER-API-" + System.currentTimeMillis();

            UploadInvoiceRequest request = UploadInvoiceRequest.builder()
                    .userEmail(userEmail)
                    .supplierTaxNo(supplierTax)
                    .invoiceAmount(amount)
                    .remainingAmount(amount)
                    .currencyType("TL")
                    .invoiceDate(today)
                    .dueDate(due)
                    .additionalDueDate(due)
                    .invoiceNo(invoiceNo)
                    .invoiceType("E_FATURA")
                    .hashCode(InvoiceTestDataGenerator.randomApiInvoiceHash())
                    .taxExclusiveAmount(0)
                    .build();

            Response upload = api.uploadInvoice(request);
            boolean ok = upload.getStatusCode() == 200 && api.isResponseSuccessful();
            if (ok) {
                log.info("Alıcı API ile fatura yüklendi: {}", invoiceNo);
            } else {
                log.warn("Alıcı API fatura yüklenemedi: status={} body={}",
                        upload.getStatusCode(), upload.getBody().asString());
            }
            return ok;
        } catch (Exception e) {
            log.warn("Alıcı API fatura yükleme hatası: {}", e.getMessage());
            return false;
        }
    }

    private static EnvironmentManager.EnvironmentConfig buildEnvForBuyerApi(String buyerTaxNo) {
        EnvironmentManager.EnvironmentConfig c = new EnvironmentManager.EnvironmentConfig();
        c.setHost(defaultIntegrationHost());
        c.setApiKey(ConfigReader.getProperty("integration.api.key", "").trim());
        c.setAlias(ConfigReader.getProperty("integration.api.alias", "").trim());
        c.setPassword(ConfigReader.getProperty("integration.api.password", "").trim());
        c.setTaxNumber(buyerTaxNo.trim());
        String email = ConfigReader.getProperty("integration.api.user.email", "").trim();
        if (email.isEmpty()) {
            email = ConfigReader.getProperty("buyer.email", "").trim();
        }
        c.setUserEmail(email);
        return c;
    }

    private static String buyerAuthTaxNo() {
        String t = ConfigReader.getProperty("integration.api.auth.taxno", "").trim();
        if (!t.isEmpty()) {
            return t;
        }
        return ConfigReader.getProperty("test.matched.buyer.taxno", "").trim();
    }

    private static String supplierTaxNoOnInvoice() {
        String t = ConfigReader.getProperty("integration.supplier.taxno", "").trim();
        if (!t.isEmpty()) {
            return t;
        }
        return ConfigReader.getProperty("test.matched.company.taxno", "").trim();
    }

    private static String defaultIntegrationHost() {
        String configured = ConfigReader.getProperty("integration.api.host", "").trim();
        if (!configured.isEmpty()) {
            return configured.replaceAll("/+$", "");
        }
        return "https://dev.faturalab.com/app/api/integration/buyer/v0";
    }

    private static int parseAmount() {
        try {
            return Integer.parseInt(ConfigReader.getProperty("integration.api.invoice.amount", "5000").trim());
        } catch (NumberFormatException e) {
            return 5000;
        }
    }
}
