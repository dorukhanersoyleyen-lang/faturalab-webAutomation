package com.faturalab.automation.pages;

import com.faturalab.automation.utils.VaadinGridFilterHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Admin — Raporlar → "TAHSİLAT İŞLEMLERİ" sekmesi (kaynak: {@code DisplayReportsView.26},
 * grid: {@code DisplayDtsView}) — tüm firmaların DTS kayıtlarını listeler.
 *
 * Satır aksiyon butonu {@code CompanyTendersView.7}="Gözat" ile {@code AdminDtsDialog}
 * ("Doğrudan Tahsilat Bilgileri") açılır.
 */
public class AdminDtsReportPage extends BasePageObject {

    private static final By GRID = By.cssSelector("vaadin-grid");
    private final AdminReportsPage adminReportsPage;

    public AdminDtsReportPage(WebDriver driver) {
        super(driver);
        this.adminReportsPage = new AdminReportsPage(driver);
    }

    /** Admin sidebar "Raporlar" → "TAHSİLAT İŞLEMLERİ" tab'ına gider. */
    public boolean navigateToTahsilatIslemleri() {
        adminReportsPage.navigateToRaporlar();
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var btns = document.querySelectorAll('vaadin-button, button');" +
                    "for (var b of btns) {" +
                    "  var t = fold(b.textContent);" +
                    "  if (t === 'tahsilat islemleri' || t.indexOf('tahsilat islemleri') >= 0) { b.click(); return true; }" +
                    "}" +
                    "return false;");
            log.info("[DTS] 'TAHSİLAT İŞLEMLERİ' tab tıklandı mı: {}", clicked);
            if (Boolean.TRUE.equals(clicked)) {
                waitForVaadinNavigation();
                try {
                    new WebDriverWait(driver, Duration.ofSeconds(20))
                            .until(ExpectedConditions.visibilityOfElementLocated(GRID));
                    Thread.sleep(1500);
                } catch (Exception ignored) {
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("[DTS] navigateToTahsilatIslemleri: {}", e.getMessage());
            return false;
        }
    }

    /** "DTS No" kolon filtresiyle grid'i verilen referans numarasına indirir. */
    public boolean findDtsByReferenceNo(String referenceNo) {
        boolean filtered = VaadinGridFilterHelper.applyOnlyValuesWithRetry(
                driver, "DTS No", java.util.Collections.singletonList(referenceNo), 3);
        log.info("[DTS] AdminDtsReportPage - '{}' kolon filtresi uygulandi mi: {}", referenceNo, filtered);
        return filtered;
    }

    /**
     * Filtre tam 1 kayda indirildiğinde gridde görünür TEK aksiyon butonuna ("Gözat")
     * gerçek Selenium click() ile basar (virtual scroll cache hücrelerine karşı korumalı).
     */
    public boolean clickOnlySingleRowActionButton() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "document.querySelectorAll('[data-admindts-single-row-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-admindts-single-row-target');});" +
                    "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                    "var found = null;" +
                    "for (var c of cells) {" +
                    "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  if (c.querySelector('.filter-header-cell')) continue;" +
                    "  var btn = c.querySelector('vaadin-button, button');" +
                    "  if (btn && !btn.disabled) { found = btn; break; }" +
                    "}" +
                    "if (!found) return false;" +
                    "found.setAttribute('data-admindts-single-row-target', '1');" +
                    "return true;");
            log.info("[DTS] AdminDtsReportPage clickOnlySingleRowActionButton - buton isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement btn = driver.findElement(By.cssSelector("[data-admindts-single-row-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            btn.click();
            log.info("[DTS] AdminDtsReportPage: 'Gözat' butonuna gerçek Selenium click uygulandı.");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS] AdminDtsReportPage clickOnlySingleRowActionButton: {}", e.getMessage());
            return false;
        }
    }

    /** Gridde (fold'lu) verilen statü anahtar kelimesinin görünür bir hücrede belirmesini poll eder. */
    public boolean waitForStatusFold(String statusFoldKeyword, int timeoutSeconds) {
        String escaped = statusFoldKeyword.replace("'", "\\'");
        return VaadinGridFilterHelper.waitForJs(driver, timeoutSeconds,
                "function fold(s){return (s||'')" +
                ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                "var target = fold('" + escaped + "');" +
                "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                "for (var c of cells) {" +
                "  var r = c.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                "  if (fold(c.textContent||'').indexOf(target) >= 0) return true;" +
                "}" +
                "return false;");
    }
}
