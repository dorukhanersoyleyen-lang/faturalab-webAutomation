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
 * DTS (Doğrudan Tahsilat Sistemi) senaryoları için SALT-OKUNUR DB çapraz doğrulama yardımcısı.
 *
 * ⚠️ Sadece SELECT çalıştırır — hiçbir INSERT/UPDATE/DELETE yok. Bağlantı bilgileri
 * {@code loadtest.db.*} (dev.properties) ile PAYLAŞILIR ({@link com.faturalab.automation.loadtest.PgIdleMonitor}
 * ile aynı `faturalab_dev` @ 192.168.97.33) — burada YENİDEN TANIMLANMAZ, {@link ConfigReader} üzerinden okunur.
 */
public final class DtsDbAssertions {

    private static final Logger log = LogManager.getLogger(DtsDbAssertions.class);

    private DtsDbAssertions() {
    }

    /** {@code dbsdocs} tablosundan bir satırın salt-okunur anlık görüntüsü. */
    public static class DbsDocRow {
        public final long id;
        public final Long dtsId;
        public final String docNo;
        public final BigDecimal dbsAmount;
        public final Long factoringId;
        public final String status;

        public DbsDocRow(long id, Long dtsId, String docNo, BigDecimal dbsAmount, Long factoringId, String status) {
            this.id = id;
            this.dtsId = dtsId;
            this.docNo = docNo;
            this.dbsAmount = dbsAmount;
            this.factoringId = factoringId;
            this.status = status;
        }

        @Override
        public String toString() {
            return "DbsDocRow{id=" + id + ", dtsId=" + dtsId + ", docNo='" + docNo + '\''
                    + ", dbsAmount=" + dbsAmount + ", factoringId=" + factoringId
                    + ", status='" + status + "'}";
        }
    }

    /**
     * {@code dbsdocs} tablosunda verilen fatura numarasına (docno) ait kaydı arar.
     *
     * SQL: {@code SELECT id, dtsid, docno, dbsamount, factoringid, status FROM dbsdocs WHERE docno = ?}
     * — sadece SELECT, parametre PreparedStatement ile geçilir (SQL injection'dan kaçınmak için).
     *
     * @param docNo aranacak fatura numarası (CompanyBatchApi.generateUniqueInvoiceNo() çıktısı)
     * @return kayıt bulunduysa dolu, bulunamadıysa boş Optional
     */
    public static Optional<DbsDocRow> findDbsDocByDocNo(String docNo) {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");

        String sql = "SELECT id, dtsid, docno, dbsamount, factoringid, status "
                + "FROM dbsdocs WHERE docno = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setReadOnly(true);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, docNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        log.warn("[DTS-DB] dbsdocs'ta docno={} için kayıt bulunamadı.", docNo);
                        return Optional.empty();
                    }
                    DbsDocRow row = new DbsDocRow(
                            rs.getLong("id"),
                            rs.getObject("dtsid") != null ? rs.getLong("dtsid") : null,
                            rs.getString("docno"),
                            rs.getBigDecimal("dbsamount"),
                            rs.getObject("factoringid") != null ? rs.getLong("factoringid") : null,
                            rs.getString("status"));
                    log.info("[DTS-DB] dbsdocs'ta docno={} için kayıt bulundu: {}", docNo, row);
                    return Optional.of(row);
                }
            }
        } catch (Exception e) {
            log.error("[DTS-DB] dbsdocs sorgusu başarısız oldu (docno={}): {}", docNo, e.getMessage(), e);
            throw new RuntimeException("dbsdocs salt-okunur sorgusu başarısız: " + e.getMessage(), e);
        }
    }

    /**
     * {@code dealerlimit} tablosunda verilen id'ye ait güncel {@code dbslimitamount} değerini okur.
     *
     * ⚠️ @dts-limit-crud senaryosunda grid-metin taraması YERİNE kullanılıyor: DBS Limit Tutarı
     * gridde `InvoiceHelper.getAmountLabelForTable` ile (Teminat Limiti/DBS Maliyet ile aynı
     * belirsiz nokta/virgül-ondalık + olası binlik-ayraç formatlama) gösteriliyor — büyük sayılarda
     * (bu senaryoda mevcut değer 200000.00) binlik ayracın gerçek formatı (nokta mı virgül mü)
     * belirsiz olduğu için metin araması kırılgan olurdu. DB'den doğrudan BigDecimal okumak
     * formatlamadan tamamen bağımsız, kesin bir doğrulama sağlıyor (aynı gerekçe: DTS-001'in
     * {@link #findDbsDocByDocNo} DB doğrulaması).
     *
     * @param id dealerlimit.id (ör. 88 — "Otomasyon Bayi" ↔ Akbank)
     * @return kayıt bulunduysa dolu, bulunamadıysa boş Optional
     */
    public static Optional<BigDecimal> findDealerLimitDbsAmount(long id) {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");

        String sql = "SELECT dbslimitamount FROM dealerlimit WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setReadOnly(true);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        log.warn("[DTS-DB] dealerlimit'te id={} için kayıt bulunamadı.", id);
                        return Optional.empty();
                    }
                    BigDecimal amount = rs.getBigDecimal("dbslimitamount");
                    log.info("[DTS-DB] dealerlimit id={} güncel dbslimitamount={}", id, amount);
                    return Optional.of(amount);
                }
            }
        } catch (Exception e) {
            log.error("[DTS-DB] dealerlimit sorgusu başarısız oldu (id={}): {}", id, e.getMessage(), e);
            throw new RuntimeException("dealerlimit salt-okunur sorgusu başarısız: " + e.getMessage(), e);
        }
    }
}
