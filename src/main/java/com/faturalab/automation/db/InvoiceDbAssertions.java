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
 * Fatura ({@code invoice}) tablosu için SALT-OKUNUR DB çapraz doğrulama yardımcısı.
 *
 * "Teklif Talebi İptal Akışı" UAT senaryosu (eski adıyla WP#5649 canlı reprosu) için: teklif
 * talebi iptal edildikten sonra {@code invoice.remainingamount}'ın {@code invoice.payableamount}'a
 * (rezervasyon öncesi orijinal değere) geri döndüğünü teyit eder — sadece "hata yok" değil,
 * pozitif/gerçek DB kanıtı (bkz. {@code fix-verification.md} "pozitif doğrulama" ilkesi).
 *
 * ⚠️ Sadece SELECT çalıştırır. Bağlantı bilgileri {@code loadtest.db.*} (dev.properties) ile
 * PAYLAŞILIR ({@link DtfDbAssertions} / {@link DtsDbAssertions} ile AYNI kalıp).
 */
public final class InvoiceDbAssertions {

    private static final Logger log = LogManager.getLogger(InvoiceDbAssertions.class);

    private InvoiceDbAssertions() {
    }

    /** {@code invoice} tablosundaki bir kaydın salt-okunur anlık görüntüsü. */
    public static class InvoiceAmountRow {
        public final long id;
        public final String invoiceNo;
        public final BigDecimal payableAmount;
        public final BigDecimal remainingAmount;
        /**
         * ⚠️ Bilinen açık defect #5905 ("İptal/Red Edilince Faturanın auctionCount Alanı
         * Düşmüyor") — bu alan senaryoda SADECE loglanır/raporlanır, KESİN assert edilmez.
         * #5905 kapandığında burada {@code Assert.assertEquals} eklenebilir.
         */
        public final Integer auctionCount;

        public InvoiceAmountRow(long id, String invoiceNo, BigDecimal payableAmount,
                                 BigDecimal remainingAmount, Integer auctionCount) {
            this.id = id;
            this.invoiceNo = invoiceNo;
            this.payableAmount = payableAmount;
            this.remainingAmount = remainingAmount;
            this.auctionCount = auctionCount;
        }

        @Override
        public String toString() {
            return "InvoiceAmountRow{id=" + id + ", invoiceNo='" + invoiceNo + '\''
                    + ", payableAmount=" + payableAmount + ", remainingAmount=" + remainingAmount
                    + ", auctionCount=" + auctionCount + '}';
        }
    }

    /**
     * Verilen fatura numarasına ait {@code payableamount}/{@code remainingamount}/{@code auctioncount}
     * değerlerini okur. {@code invoiceno} runtime'da benzersiz üretildiği için
     * ({@link com.faturalab.automation.utils.TzfInvoiceExcelGenerator}) tek başına yeterli anahtardır.
     */
    public static Optional<InvoiceAmountRow> findAmountsByInvoiceNo(String invoiceNo) {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");

        String sql = "SELECT id, invoiceno, payableamount, remainingamount, auctioncount "
                + "FROM invoice WHERE invoiceno = ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setReadOnly(true);
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, invoiceNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        log.warn("[INVOICE-DB] invoiceno={} için kayıt bulunamadı.", invoiceNo);
                        return Optional.empty();
                    }
                    InvoiceAmountRow row = new InvoiceAmountRow(
                            rs.getLong("id"),
                            rs.getString("invoiceno"),
                            rs.getBigDecimal("payableamount"),
                            rs.getBigDecimal("remainingamount"),
                            rs.getObject("auctioncount") != null ? rs.getInt("auctioncount") : null);
                    log.info("[INVOICE-DB] invoiceno={} için kayıt: {}", invoiceNo, row);
                    return Optional.of(row);
                }
            }
        } catch (Exception e) {
            log.error("[INVOICE-DB] invoice sorgusu başarısız oldu (invoiceno={}): {}", invoiceNo, e.getMessage(), e);
            throw new RuntimeException("invoice salt-okunur sorgusu başarısız: " + e.getMessage(), e);
        }
    }
}
