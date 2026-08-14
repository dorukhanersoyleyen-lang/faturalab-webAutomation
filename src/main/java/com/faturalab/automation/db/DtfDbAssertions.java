package com.faturalab.automation.db;

import com.faturalab.automation.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * DTF (Dikey Ticaret Finansmanı) senaryoları için DB çapraz doğrulama yardımcısı.
 *
 * Ağırlıklı olarak SALT-OKUNUR (SELECT) — tek istisna {@link #setAraTedarikciRequiredDtf(boolean)}:
 * bkz. o metodun Javadoc'u — TZF-DTF supplier paylaşımı regresyonunu önlemek için kasıtlı, dar
 * kapsamlı bir UPDATE. Bağlantı bilgileri {@code loadtest.db.*} (dev.properties) ile PAYLAŞILIR
 * ({@link DtsDbAssertions} ile AYNI kalıp).
 */
public final class DtfDbAssertions {

    private static final Logger log = LogManager.getLogger(DtfDbAssertions.class);

    private DtfDbAssertions() {
    }

    public static class DtfTenderRow {
        public final long id;
        public final String referenceNo;
        public final String tenderType;
        public final String status;
        public final Long buyerId;
        public final Long auctionId;
        public final Integer totalSupplierCount;
        public final BigDecimal dtfRate;

        public DtfTenderRow(long id, String referenceNo, String tenderType, String status,
                             Long buyerId, Long auctionId, Integer totalSupplierCount, BigDecimal dtfRate) {
            this.id = id;
            this.referenceNo = referenceNo;
            this.tenderType = tenderType;
            this.status = status;
            this.buyerId = buyerId;
            this.auctionId = auctionId;
            this.totalSupplierCount = totalSupplierCount;
            this.dtfRate = dtfRate;
        }

        @Override
        public String toString() {
            return "DtfTenderRow{id=" + id + ", referenceNo='" + referenceNo + '\'' + ", tenderType='" + tenderType
                    + "', status='" + status + "', buyerId=" + buyerId + ", auctionId=" + auctionId
                    + ", totalSupplierCount=" + totalSupplierCount + ", dtfRate=" + dtfRate + '}';
        }
    }

    /**
     * Verilen buyerId'ye ait en YENİ (id DESC) tender kaydını salt-okunur okur — testin
     * kendi az önce başlattığı DTF Tender'ı budur (aynı buyer altında paralel koşum yok).
     *
     * SQL: {@code SELECT ... FROM tender WHERE buyerid = ? AND tendertype = 'DTF' ORDER BY id DESC LIMIT 1}
     */
    public static Optional<DtfTenderRow> findLatestDtfTenderByBuyerId(long buyerId) {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");

        String sql = "SELECT id, referenceno, tendertype, status, buyerid, auctionid, totalsuppliercount, dtfrate "
                + "FROM tender WHERE buyerid = ? AND tendertype = 'DTF' ORDER BY id DESC LIMIT 1";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setReadOnly(true);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, buyerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        log.warn("[DTF-DB] tender'da buyerid={} için DTF kaydı bulunamadı.", buyerId);
                        return Optional.empty();
                    }
                    DtfTenderRow row = new DtfTenderRow(
                            rs.getLong("id"),
                            rs.getString("referenceno"),
                            rs.getString("tendertype"),
                            rs.getString("status"),
                            rs.getObject("buyerid") != null ? rs.getLong("buyerid") : null,
                            rs.getObject("auctionid") != null ? rs.getLong("auctionid") : null,
                            rs.getObject("totalsuppliercount") != null ? rs.getInt("totalsuppliercount") : null,
                            rs.getBigDecimal("dtfrate"));
                    log.info("[DTF-DB] buyerid={} için en yeni DTF tender kaydı: {}", buyerId, row);
                    return Optional.of(row);
                }
            }
        } catch (Exception e) {
            log.error("[DTF-DB] tender sorgusu başarısız oldu (buyerid={}): {}", buyerId, e.getMessage(), e);
            throw new RuntimeException("tender salt-okunur sorgusu başarısız: " + e.getMessage(), e);
        }
    }

    /** Verilen auction.id'nin productType alanının 'DEEP_TIER_FINANCING' olup olmadığını okur. */
    public static Optional<String> findAuctionProductType(long auctionId) {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");

        String sql = "SELECT producttype FROM auction WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setReadOnly(true);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    String productType = rs.getString("producttype");
                    log.info("[DTF-DB] auction.id={} productType={}", auctionId, productType);
                    return Optional.ofNullable(productType);
                }
            }
        } catch (Exception e) {
            log.error("[DTF-DB] auction sorgusu başarısız oldu (id={}): {}", auctionId, e.getMessage(), e);
            throw new RuntimeException("auction salt-okunur sorgusu başarısız: " + e.getMessage(), e);
        }
    }

    /**
     * ⚠️⚠️ KÖK NEDEN — TZF↔DTF supplier PAYLAŞIMI REGRESYONU (2026-08-14, canlı Jenkins günlük
     * koşumunda kanıtlandı, build #78): DTF-001'in kurulum scripti ({@code setup_dtf_automation_chain.py})
     * {@code supplier.id=1091} (company.id=998 → buyer.id=145, TZF'nin de kullandığı AYNI bağlantı)
     * üzerinde {@code requireddtf=true} bırakmıştı — kaynak kod kanıtı
     * ({@code CompanyAddEditAuctionDialog.java:1013-1021}): {@code supplier.isRequiredDtf()} true VE
     * tarih aralığındaysa normal "Teklif Al" (startButton) KALICI OLARAK disabled kalıyor — DTF'ten
     * habersiz TZF senaryosu "Modal içindeki 'Teklif Al' tıklanamadı" ile patlıyordu (dtfenddate=2027
     * olduğu için sonraki HER günlük koşumda tekrarlayacaktı).
     * <p>
     * Fix: {@code requireddtf} artık KALICI DEĞİL, DTF-001 senaryosunun SÜRESİ boyunca geçici olarak
     * açılıp kapatılıyor — bu metod {@code @Before("@dtf-001")}'de {@code true}, {@code @After("@dtf-001")}'de
     * {@code false} ile çağrılır (bkz. {@code DtfIslemUATStepDefs}). UI paketi {@code parallel=false}
     * ile koştuğu için (Vaadin SPA kısıtı, bkz. web-automation.md) TZF ile DTF-001 asla eş zamanlı
     * çalışmaz — bu toggle güvenlidir.
     * <p>
     * ⚠️ Bilinçli olarak {@code id=1091 AND buyerid=145 AND companyid=998} ÜÇLÜ WHERE koşuluyla
     * DARALTILMIŞTIR — başka hiçbir supplier satırına yanlışlıkla dokunmayı imkansız kılar.
     */
    public static void setAraTedarikciRequiredDtf(boolean enabled) {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");

        String sql = "UPDATE supplier SET requireddtf = ? WHERE id = 1091 AND buyerid = 145 AND companyid = 998";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setBoolean(1, enabled);
                int rows = ps.executeUpdate();
                log.info("[DTF-DB] supplier.id=1091 requireddtf={} olarak ayarlandı ({} satır).", enabled, rows);
                if (rows != 1) {
                    log.warn("[DTF-DB] BEKLENEN 1 satır güncellenmeliydi, {} güncellendi — supplier.id=1091 "
                            + "değişmiş/silinmiş olabilir.", rows);
                }
            }
        } catch (Exception e) {
            log.error("[DTF-DB] setAraTedarikciRequiredDtf({}) başarısız oldu: {}", enabled, e.getMessage(), e);
            throw new RuntimeException("supplier.requireddtf güncellemesi başarısız: " + e.getMessage(), e);
        }
    }
}
