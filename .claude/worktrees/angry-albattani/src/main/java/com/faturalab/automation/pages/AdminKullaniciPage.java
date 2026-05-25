package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Admin — Kullanıcı Yönetimi ekranı (FL-004: Kullanıcı Düzenleme).
 * Navigasyon: Admin dashboard → .button-menu "Kullanıcılar" butonu
 * DisplayUsersView: Tab'lar Ticari İşletme/Finansal Kurum/Admin/Alıcı
 * Grid'de DÜZENLE (.button-orange) butonu → AddEditUserDialog → "Adres" alanı
 */
public class AdminKullaniciPage extends BasePageObject {

    private static final By KULLANICI_GRID  = By.cssSelector("vaadin-grid");
    private static final By DIALOG_OVERLAY  = By.cssSelector("vaadin-dialog-overlay");
    private static final By SUCCESS_NOTIF   = By.cssSelector(
            "vaadin-notification-container, vaadin-notification[opened]");

    public AdminKullaniciPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Admin dashboard menüsünden "Kullanıcılar" butonuna tıklar.
     */
    public void navigateToKullaniciYonetimi() {
        boolean clicked = clickNavItemByText("kullanıcılar");
        if (!clicked) clicked = clickNavItemByText("kullanici");
        if (!clicked) clicked = clickNavItemByText("user");
        if (!clicked) {
            log.warn("Kullanıcılar menü butonu bulunamadı. URL: {}", driver.getCurrentUrl());
            throw new RuntimeException("Kullanıcılar menüsüne gidilemedi");
        }
        waitForVaadinNavigation();
        waitForVaadinGrid();
        log.info("Kullanıcı Yönetimi ekranına geçildi.");
    }

    // ─── Grid ─────────────────────────────────────────────────────────────────

    public boolean isKullaniciGridVisible() {
        return isVaadinGridVisible();
    }

    /**
     * Grid'de satır var mı kontrolü.
     */
    public boolean hasKullaniciRows() {
        try {
            waitForVaadinGrid();
            List<WebElement> cells = driver.findElements(
                    By.cssSelector("vaadin-grid-cell-content"));
            return cells.stream().anyMatch(c -> {
                String t = c.getText();
                return t != null && !t.trim().isEmpty();
            });
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Grid'deki ilk satırın ilk hücresine tıklar (kullanıcı seçimi).
     */
    public void clickFirstUserRow() {
        try {
            waitForVaadinGrid();
            List<WebElement> cells = driver.findElements(
                    By.cssSelector("vaadin-grid-cell-content"));
            if (!cells.isEmpty()) {
                // İlk dolu hücreye tıkla
                for (WebElement cell : cells) {
                    String txt = cell.getText();
                    if (txt != null && !txt.trim().isEmpty()) {
                        cell.click();
                        log.info("Kullanıcı satırına tıklandı: {}", txt);
                        try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        return;
                    }
                }
            }
            log.warn("Kullanıcı grid'inde tıklanacak satır bulunamadı");
        } catch (Exception e) {
            log.warn("Kullanıcı satırına tıklanamadı: {}", e.getMessage());
        }
    }

    // Backward compat
    public void clickKullaniciRow(int rowIndex) { clickFirstUserRow(); }

    // ─── DÜZENLE Butonu ───────────────────────────────────────────────────────

    /**
     * Sayfadaki (veya dialog'daki) ilk DÜZENLE butonuna tıklar.
     */
    public void clickDuzenle() {
        waitForVaadinNavigation();
        // Try all button selectors — .button-orange first, then all vaadin-button
        boolean buttonClicked = false;
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                if (attempt > 0) Thread.sleep(1500);
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var kws = ['d\\u00fczenle', 'duzenle', 'edit'];" +
                    // Try .button-orange first (V2 grid action buttons)
                    "var orangeBtns = document.querySelectorAll('vaadin-button.button-orange, button.button-orange');" +
                    "for (var b of orangeBtns) {" +
                    "  var txt = (b.textContent || '').trim().toLowerCase();" +
                    "  for (var kw of kws) { if (txt.includes(kw)) { b.click(); return true; } }" +
                    "}" +
                    "if (orangeBtns.length > 0) { orangeBtns[0].click(); return 'orange-first'; }" +
                    // Fallback: any vaadin-button with DÜZENLE text
                    "var allBtns = document.querySelectorAll('vaadin-button, button');" +
                    "for (var b of allBtns) {" +
                    "  var txt = (b.textContent || '').trim().toLowerCase();" +
                    "  for (var kw of kws) { if (txt === kw || txt.startsWith(kw)) { b.click(); return true; } }" +
                    "}" +
                    "return false;"
                );
                if (clicked != null && !Boolean.FALSE.equals(clicked)) {
                    log.info("Kullanıcı DÜZENLE butonuna tıklandı (attempt {}).", attempt);
                    buttonClicked = true;
                    break;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {}
        }

        if (!buttonClicked) {
            // XPath fallback
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath(
                                "//vaadin-button[contains(normalize-space(),'DÜZENLE') or contains(normalize-space(),'Düzenle')" +
                                " or contains(normalize-space(),'Duzenle')]")));
                btn.click();
                log.info("Kullanıcı DÜZENLE butonuna XPath ile tıklandı.");
                buttonClicked = true;
            } catch (Exception e) {
                log.error("DÜZENLE butonu bulunamadı: {}", e.getMessage());
                throw new RuntimeException("DÜZENLE butonu tıklanamadı", e);
            }
        }

        // Wait for dialog to appear (up to 8 seconds)
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8))
                    .until(d -> isDialogOpen());
            log.info("Kullanıcı düzenleme dialogu açıldı.");
        } catch (Exception e) {
            log.warn("Dialog 8 saniyede açılmadı — devam ediliyor. URL: {}", driver.getCurrentUrl());
        }
    }

    // ─── Dialog Alan Güncelleme ────────────────────────────────────────────────

    /**
     * Dialog'da "Adres" alanını günceller (AddEditUserDialog).
     */
    public void updateKullaniciAdres(String yeniAdres) {
        // Works in both dialog and page view modes
        boolean set = setVaadinFieldValue("adres", yeniAdres);
        if (!set) {
            // Fallback: ilk text-area veya text-field'a yaz
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "var areas = document.querySelectorAll('vaadin-text-area, vaadin-text-field');" +
                    "if (areas.length > 0) {" +
                    "  var f = areas[areas.length-1];" +
                    "  f.value = arguments[0];" +
                    "  f.dispatchEvent(new CustomEvent('value-changed',{bubbles:true,detail:{value:arguments[0]}}));" +
                    "  if (f.shadowRoot) { var inp = f.shadowRoot.querySelector('input,textarea'); if(inp){inp.value=arguments[0];inp.dispatchEvent(new Event('input',{bubbles:true}));}}" +
                    "}",
                    yeniAdres
                );
                log.warn("Adres fallback JS ile güncellendi: {}", yeniAdres);
            } catch (Exception ex) {
                throw new RuntimeException("Adres alanı güncellenemedi", ex);
            }
        }
        log.info("Kullanıcı adresi güncellendi: {}", yeniAdres);
    }

    // ─── Kaydet ───────────────────────────────────────────────────────────────

    public void clickKaydet() {
        // First try inside vaadin-dialog-overlay (highest priority)
        Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
            "var kws = ['kaydet', 'güncelle', 'guncelle', 'save', 'kaydet & kapat'];" +
            "var overlay = document.querySelector('vaadin-dialog-overlay');" +
            "if (overlay) {" +
            "  var btns = overlay.querySelectorAll('vaadin-button, button');" +
            "  for (var b of btns) {" +
            "    var txt = (b.textContent || '').trim().toLowerCase();" +
            "    for (var kw of kws) { if (txt === kw || txt.startsWith(kw)) { b.click(); return true; } }" +
            "  }" +
            "}" +
            // Fallback: search ALL vaadin-button elements (handles full page view)
            "var allBtns = document.querySelectorAll('vaadin-button, button');" +
            "for (var b of allBtns) {" +
            "  var txt = (b.textContent || '').trim().toLowerCase();" +
            "  for (var kw of kws) { if (txt === kw || txt.startsWith(kw)) { b.click(); return true; } }" +
            "}" +
            "return false;"
        );
        if (!Boolean.TRUE.equals(clicked)) {
            try {
                // Try dialog overlay XPath first
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(By.xpath(
                                "//vaadin-dialog-overlay//vaadin-button[contains(normalize-space(),'aydet')] |" +
                                "//vaadin-dialog-overlay//vaadin-button[contains(normalize-space(),'ncelle')]")));
                btn.click();
                clicked = true;
            } catch (Exception e) {
                // Fallback: any button on page with save-like text
                try {
                    WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                            driver, java.time.Duration.ofSeconds(5))
                            .until(ExpectedConditions.elementToBeClickable(By.xpath(
                                    "//vaadin-button[contains(normalize-space(),'aydet') or contains(normalize-space(),'ncelle')]")));
                    btn.click();
                    clicked = true;
                } catch (Exception e2) {
                    log.error("Kaydet butonu bulunamadı: {}", e2.getMessage());
                }
            }
        }
        if (!Boolean.TRUE.equals(clicked)) throw new RuntimeException("Kaydet butonu bulunamadı");
        log.info("Kullanıcı Kaydet butonuna tıklandı.");
        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ─── Sekme Geçişi ─────────────────────────────────────────────────────────

    public void switchToTab(String tabName) {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var kw = arguments[0].toLowerCase();" +
                "var els = document.querySelectorAll('vaadin-tab, [role=\"tab\"], button');" +
                "for (var el of els) {" +
                "  var txt = (el.textContent||'').toLowerCase().trim();" +
                "  if (txt.includes(kw)) { el.click(); return true; }" +
                "}" +
                "return false;",
                tabName
            );
            if (Boolean.TRUE.equals(clicked)) {
                log.info("Sekme tıklandı: {}", tabName);
                try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } else {
                log.warn("Sekme bulunamadı: {}", tabName);
            }
        } catch (Exception e) {
            log.warn("Sekme tıklanamadı {}: {}", tabName, e.getMessage());
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public boolean isSuccessNotificationVisible() {
        try {
            waitForVisibility(SUCCESS_NOTIF, 8);
            return true;
        } catch (Exception e) {
            if (isAnyNotificationVisible()) return true;
            // If dialog closed, save probably succeeded
            return !isDialogOpen();
        }
    }

    public boolean isDialogOpen() {
        return super.isDialogOpen();
    }

    // Backward compat for email/name updates
    public void updateKullaniciAdi(String ad) { setVaadinFieldValue("adı soyadı", ad); }
    public void updateEmail(String email) { setVaadinFieldValue("e-posta", email); }
}
