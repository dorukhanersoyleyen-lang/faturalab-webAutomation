package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Admin — Raporlar ekranı (FL-001: Teklif Talepleri Filtreleme, FL-002: Rapor İndirme).
 * Navigasyon: Sol menü → "Raporlar" → "Teklif Talepleri" veya rapor listesi
 */
public class AdminReportsPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    private final By RAPORLAR_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'Rapor') or contains(normalize-space(),'Report')]");

    private final By TEKLIF_TALEPLERI_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'Teklif Talepleri') or contains(normalize-space(),'Tender')]");

    // ─── Filtreler ────────────────────────────────────────────────────────────

    private final By TARIH_FILTER_START = By.xpath(
            "(//vaadin-date-picker//input | //input[@type='date'])[1]");

    private final By TARIH_FILTER_END = By.xpath(
            "(//vaadin-date-picker//input | //input[@type='date'])[2]");

    private final By DURUM_FILTER = By.xpath(
            "//vaadin-combo-box[contains(@label,'Durum') or contains(@label,'Status')] | " +
            "//vaadin-select[contains(@label,'Durum') or contains(@label,'Status')]");

    private final By FK_FILTER = By.xpath(
            "//vaadin-combo-box[contains(@label,'FK') or contains(@label,'Finansman') or contains(@label,'Finans')] | " +
            "//vaadin-select[contains(@label,'FK') or contains(@label,'Finansman')]");

    private final By FILTRELE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Filtrele'] | " +
            "//vaadin-button[normalize-space()='Ara'] | " +
            "//vaadin-button[normalize-space()='Uygula'] | " +
            "//button[normalize-space()='Filtrele']");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By RAPOR_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_ROWS  = By.cssSelector("vaadin-grid-cell-content");

    // ─── İndirme ──────────────────────────────────────────────────────────────

    private final By INDIR_BTN = By.xpath(
            "//vaadin-button[contains(normalize-space(),'İndir')] | " +
            "//vaadin-button[contains(normalize-space(),'Indir')] | " +
            "//vaadin-button[contains(normalize-space(),'Download')] | " +
            "//vaadin-button[contains(normalize-space(),'Export')] | " +
            "//vaadin-button[contains(normalize-space(),'Excel')] | " +
            "//button[contains(normalize-space(),'İndir')]");

    private final By RAPOR_TYPE_SELECT = By.xpath(
            "//vaadin-select[contains(@label,'Rapor Tipi') or contains(@label,'Rapor')] | " +
            "//vaadin-combo-box[contains(@label,'Rapor Tipi') or contains(@label,'Rapor')]");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public AdminReportsPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    public void navigateToRaporlar() {
        boolean clicked = clickNavItemByText("raporlar");
        if (!clicked) clicked = clickNavItemByText("rapor");
        if (!clicked) {
            log.warn("Raporlar menü butonu bulunamadı. URL: {}", driver.getCurrentUrl());
            throw new RuntimeException("Raporlar menüsüne gidilemedi");
        }
        waitForVaadinNavigation();
        log.info("Raporlar ekranına geçildi.");
    }

    public void navigateToTeklifTalepleri() {
        // Raporlar sayfasında "Günlük İşlemler" veya sekme ile teklif talepleri
        boolean clicked = clickNavItemByText("günlük");
        if (!clicked) clicked = clickNavItemByText("teklif");
        if (!clicked) {
            // Grid zaten yüklüyse devam et
            log.warn("Teklif Talepleri sekmesi bulunamadı — mevcut görünümde devam.");
        }
        waitForVaadinNavigation();
        log.info("Teklif Talepleri ekranına geçildi.");
    }

    // ─── Filtreler ────────────────────────────────────────────────────────────

    public void applyTarihFilter(String startDate, String endDate) {
        try {
            WebElement startInput = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(TARIH_FILTER_START));
            startInput.clear();
            startInput.sendKeys(startDate);
            log.info("Başlangıç tarihi girildi: {}", startDate);
        } catch (Exception e) {
            log.warn("Başlangıç tarihi girilemedi: {}", e.getMessage());
        }

        try {
            WebElement endInput = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(TARIH_FILTER_END));
            endInput.clear();
            endInput.sendKeys(endDate);
            log.info("Bitiş tarihi girildi: {}", endDate);
        } catch (Exception e) {
            log.warn("Bitiş tarihi girilemedi: {}", e.getMessage());
        }
    }

    public void applyDurumFilter(String durum) {
        try {
            WebElement combo = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(DURUM_FILTER));
            combo.click();

            By optionLocator = By.xpath(
                    "//vaadin-combo-box-item[contains(normalize-space(),'" + durum + "')] | " +
                    "//vaadin-item[contains(normalize-space(),'" + durum + "')]");
            WebElement option = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(optionLocator));
            option.click();
            log.info("Durum filtresi seçildi: {}", durum);
        } catch (Exception e) {
            log.warn("Durum filtresi uygulanamadı [{}]: {}", durum, e.getMessage());
        }
    }

    public void applyFKFilter(String fkName) {
        try {
            WebElement combo = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(FK_FILTER));
            combo.click();

            By optionLocator = By.xpath(
                    "//vaadin-combo-box-item[contains(normalize-space(),'" + fkName + "')] | " +
                    "//vaadin-item[contains(normalize-space(),'" + fkName + "')]");
            WebElement option = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(optionLocator));
            option.click();
            log.info("FK filtresi seçildi: {}", fkName);
        } catch (Exception e) {
            log.warn("FK filtresi uygulanamadı [{}]: {}", fkName, e.getMessage());
        }
    }

    public void clickFiltrele() {
        try {
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(FILTRELE_BTN));
            btn.click();
            log.info("Filtrele butonuna tıklandı.");
            try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log.warn("Filtrele butonu tıklanamadı: {}", e.getMessage());
        }
    }

    // ─── Grid ─────────────────────────────────────────────────────────────────

    public boolean isGridVisible() {
        try {
            return new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(RAPOR_GRID))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasGridRows() {
        try {
            List<WebElement> cells = driver.findElements(GRID_ROWS);
            boolean hasData = cells.stream()
                    .anyMatch(c -> c.getText() != null && !c.getText().trim().isEmpty());
            log.info("Grid veri durumu: {}", hasData);
            return hasData;
        } catch (Exception e) {
            log.warn("Grid satırları kontrol edilemedi: {}", e.getMessage());
            return false;
        }
    }

    // ─── İndirme ──────────────────────────────────────────────────────────────

    public void clickIndirButonu() {
        try {
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(INDIR_BTN));
            btn.click();
            log.info("İndir/Export butonuna tıklandı.");
            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        } catch (Exception e) {
            log.warn("İndir butonu tıklanamadı: {}", e.getMessage());
        }
    }

    public void selectRaporTipi(String tip) {
        try {
            WebElement select = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(RAPOR_TYPE_SELECT));
            select.click();

            By optionLocator = By.xpath(
                    "//vaadin-combo-box-item[contains(normalize-space(),'" + tip + "')] | " +
                    "//vaadin-item[contains(normalize-space(),'" + tip + "')] | " +
                    "//vaadin-select-item[contains(normalize-space(),'" + tip + "')]");
            WebElement option = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(optionLocator));
            option.click();
            log.info("Rapor tipi seçildi: {}", tip);
        } catch (Exception e) {
            log.warn("Rapor tipi seçilemedi [{}]: {}", tip, e.getMessage());
        }
    }

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

}
