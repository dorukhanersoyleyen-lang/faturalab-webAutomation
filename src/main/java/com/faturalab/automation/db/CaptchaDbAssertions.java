package com.faturalab.automation.db;

import com.faturalab.automation.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * reCAPTCHA login senaryosu (@recaptcha-login, OP#5909) için {@code appsettings.CAPTCHA_ENABLED}
 * bayrağını geçici olarak açıp kapatan DB yardımcısı.
 *
 * ⚠️ Aynı desen: {@link DtfDbAssertions#setAraTedarikciRequiredDtf(boolean)} — paylaşılan/global bir
 * ayarı KALICI değiştirmek yerine sadece senaryonun süresi boyunca aç, sonra (PASS/FAIL fark etmeden)
 * geri kapat. Gerekçe: {@code CAPTCHA_ENABLED} tüm dev.faturalab.com'u etkileyen global bir DB
 * ayarıdır — açık kalırsa diğer tüm günlük UAT senaryolarının (TZF/DTS/DTF) her login'i reCAPTCHA
 * token beklemesiyle yavaşlar; kapalı varsayılan bu senaryonun dışında korunmalıdır.
 *
 * Bağlantı bilgileri {@code loadtest.db.*} (dev.properties) ile PAYLAŞILIR (DtfDbAssertions/DtsDbAssertions
 * ile AYNI kalıp).
 */
public final class CaptchaDbAssertions {

    private static final Logger log = LogManager.getLogger(CaptchaDbAssertions.class);
    private static final String KEY = "CAPTCHA_ENABLED";

    private CaptchaDbAssertions() {
    }

    /** appsettings.CAPTCHA_ENABLED'in şu anki değerini salt-okunur okur ("OPEN"/"CLOSED"/başka). */
    public static Optional<String> readCaptchaEnabled() {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");

        String sql = "SELECT value FROM appsettings WHERE key = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setReadOnly(true);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, KEY);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        log.warn("[CAPTCHA-DB] appsettings.{} bulunamadı.", KEY);
                        return Optional.empty();
                    }
                    return Optional.ofNullable(rs.getString("value"));
                }
            }
        } catch (Exception e) {
            log.error("[CAPTCHA-DB] {} okunamadı: {}", KEY, e.getMessage(), e);
            throw new RuntimeException("appsettings." + KEY + " salt-okunur sorgusu başarısız: " + e.getMessage(), e);
        }
    }

    /**
     * {@code appsettings.CAPTCHA_ENABLED}'i {@code OPEN}/{@code CLOSED} olarak ayarlar.
     * ⚠️ Bilinçli olarak TEK satır ({@code WHERE key = 'CAPTCHA_ENABLED'}) hedefler — başka hiçbir
     * appsettings kaydına dokunmaz.
     *
     * @param enabled true → OPEN (reCAPTCHA aktif), false → CLOSED (varsayılan/normal durum)
     */
    public static void setCaptchaEnabled(boolean enabled) {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");

        String value = enabled ? "OPEN" : "CLOSED";
        String sql = "UPDATE appsettings SET value = ?, changedon = now() WHERE key = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, value);
                ps.setString(2, KEY);
                int rows = ps.executeUpdate();
                log.info("[CAPTCHA-DB] appsettings.{} = {} olarak ayarlandı ({} satır).", KEY, value, rows);
                if (rows != 1) {
                    log.warn("[CAPTCHA-DB] BEKLENEN 1 satır güncellenmeliydi, {} güncellendi — appsettings.{} "
                            + "satırı değişmiş/silinmiş olabilir.", rows, KEY);
                }
            }
        } catch (Exception e) {
            log.error("[CAPTCHA-DB] setCaptchaEnabled({}) başarısız oldu: {}", enabled, e.getMessage(), e);
            throw new RuntimeException("appsettings." + KEY + " güncellemesi başarısız: " + e.getMessage(), e);
        }
    }
}
