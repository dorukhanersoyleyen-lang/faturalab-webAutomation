package com.faturalab.automation.utils;

import com.faturalab.automation.config.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test verisi tarih üretiminde tek tatil kaynağı — uygulamanın kullandığı dev DB {@code holiday}
 * tablosunu okur (salt-okunur, koşum başına bir kez, cache'li).
 *
 * Neden: tatil listesi 3 sınıfta ayrı ayrı hardcode'luydu ve DB'den sapıyordu. 2026-09-28 Jenkins
 * build 135: DB'de 28.10.2026 (Cumhuriyet Bayramı Arifesi) tatil, statik listede yok → DTS vadesi
 * 28 Ekim'e düştü → {@code INVALID_DUE_DATE_HOLIDAY}.
 *
 * Kapsam: {@code usertype} ADMIN + BUYER satırları (fatura upload kontrolü {@code isHoliday(date, buyerId, null)}
 * bunları görür). FACTORING satırları bilinçli dışarıda — upload onları görmez (bkz. OP#5784 lever'ı).
 * {@code fixed=true} satırlar her yıl aynı ay-gün olarak uygulanır.
 *
 * DB'ye erişilemezse statik yedek listeye düşer (sabit resmi tatiller + 28 Ekim arifesi).
 */
public final class HolidayCalendar {

    private static final Logger log = LogManager.getLogger(HolidayCalendar.class);

    /** DB erişilemezse kullanılan yedek — sabit tarihli TR resmi tatilleri + 28 Ekim arifesi. */
    private static final Set<MonthDay> FALLBACK_FIXED = new HashSet<>(Arrays.asList(
            MonthDay.of(1, 1), MonthDay.of(4, 23), MonthDay.of(5, 1), MonthDay.of(5, 19),
            MonthDay.of(7, 15), MonthDay.of(8, 30), MonthDay.of(10, 28), MonthDay.of(10, 29)));

    /** "HOLIDAY" token'ı için: her yıl, her alıcıda (ADMIN, fixed) tatil olan gün. */
    private static final MonthDay REFERENCE_HOLIDAY = MonthDay.of(10, 29);

    private static volatile Holidays cache;

    private HolidayCalendar() {
    }

    /** Hafta sonu ya da tatil değilse true. */
    public static boolean isBusinessDay(LocalDate d) {
        return d.getDayOfWeek() != DayOfWeek.SATURDAY
                && d.getDayOfWeek() != DayOfWeek.SUNDAY
                && !isHoliday(d);
    }

    public static boolean isHoliday(LocalDate d) {
        return holidays().contains(d);
    }

    /** Verilen tarihten itibaren (kendisi dahil) ilk iş günü. */
    public static LocalDate nextBusinessDay(LocalDate from) {
        LocalDate d = from;
        while (!isBusinessDay(d)) {
            d = d.plusDays(1);
        }
        return d;
    }

    /** Verilen tarihten geriye doğru (kendisi dahil) ilk iş günü. */
    public static LocalDate previousBusinessDay(LocalDate from) {
        LocalDate d = from;
        while (!isBusinessDay(d)) {
            d = d.minusDays(1);
        }
        return d;
    }

    /**
     * {@code after}'dan KESİNLİKLE sonra gelen ilk 29 Ekim. Ek vade gibi "vadeden sonra olmalı"
     * kısıtı olan alanlarda referans tarih olarak vade verilir.
     */
    public static LocalDate nextReferenceHolidayAfter(LocalDate after) {
        LocalDate candidate = REFERENCE_HOLIDAY.atYear(after.getYear());
        if (!candidate.isAfter(after)) {
            candidate = REFERENCE_HOLIDAY.atYear(after.getYear() + 1);
        }
        return candidate;
    }

    private static Holidays holidays() {
        Holidays h = cache;
        if (h == null) {
            synchronized (HolidayCalendar.class) {
                h = cache;
                if (h == null) {
                    h = load();
                    cache = h;
                }
            }
        }
        return h;
    }

    private static Holidays load() {
        String jdbcUrl = ConfigReader.getProperty("loadtest.db.url");
        String dbUser = ConfigReader.getProperty("loadtest.db.user");
        String dbPassword = ConfigReader.getProperty("loadtest.db.password");
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            log.warn("[HOLIDAY] loadtest.db.url tanımlı değil — statik yedek tatil listesi kullanılıyor.");
            return Holidays.fallback();
        }

        String sql = "SELECT startdate, enddate, fixed FROM holiday WHERE usertype IN ('ADMIN', 'BUYER')";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            connection.setReadOnly(true);
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                Set<MonthDay> fixed = new HashSet<>();
                List<LocalDate[]> ranges = new ArrayList<>();
                while (rs.next()) {
                    LocalDate start = rs.getDate("startdate").toLocalDate();
                    LocalDate end = rs.getDate("enddate").toLocalDate();
                    if (rs.getBoolean("fixed")) {
                        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                            fixed.add(MonthDay.from(d));
                        }
                    } else {
                        ranges.add(new LocalDate[]{start, end});
                    }
                }
                log.info("[HOLIDAY] DB'den {} sabit gün + {} tarih aralığı yüklendi.", fixed.size(), ranges.size());
                return new Holidays(fixed, ranges);
            }
        } catch (Exception e) {
            log.warn("[HOLIDAY] holiday tablosu okunamadı ({}) — statik yedek tatil listesi kullanılıyor.",
                    e.getMessage());
            return Holidays.fallback();
        }
    }

    private static final class Holidays {
        private final Set<MonthDay> fixed;
        private final List<LocalDate[]> ranges;

        Holidays(Set<MonthDay> fixed, List<LocalDate[]> ranges) {
            this.fixed = Collections.unmodifiableSet(fixed);
            this.ranges = Collections.unmodifiableList(ranges);
        }

        static Holidays fallback() {
            return new Holidays(FALLBACK_FIXED, Collections.emptyList());
        }

        boolean contains(LocalDate d) {
            if (fixed.contains(MonthDay.from(d))) {
                return true;
            }
            for (LocalDate[] r : ranges) {
                if (!d.isBefore(r[0]) && !d.isAfter(r[1])) {
                    return true;
                }
            }
            return false;
        }
    }
}
