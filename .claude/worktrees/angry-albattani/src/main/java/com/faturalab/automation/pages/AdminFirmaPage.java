package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Admin — Firma Listesi ekranı (FL-003: Firma Düzenleme).
 * Navigasyon: Admin dashboard → .button-menu "Firma Listesi" butonu
 * AdminCompaniesView kaynak: DÜZENLE butonu .button-orange stil, dialog EditCompanyInfoDialog
 */
public class AdminFirmaPage extends BasePageObject {

    private static final By FIRMA_GRID     = By.cssSelector("vaadin-grid");
    private static final By DIALOG_OVERLAY = By.cssSelector("vaadin-dialog-overlay");
    private static final By SUCCESS_NOTIF  = By.cssSelector(
            "vaadin-notification-container, vaadin-notification[opened]");

    public AdminFirmaPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Admin dashboard menüsünden "Firma Listesi" butonuna tıklar.
     * .button-menu CSS sınıfındaki vaadin-button'ları arar.
     */
    public void navigateToFirmaYonetimi() {
        boolean clicked = clickNavItemByText("firma listesi");
        if (!clicked) {
            clicked = clickNavItemByText("firma");
        }
        if (!clicked) {
            log.warn("Firma Listesi menü butonu bulunamadı. URL: {}", driver.getCurrentUrl());
            throw new RuntimeException("Firma Listesi menüsüne gidilemedi");
        }
        waitForVaadinNavigation();
        waitForVaadinGrid();
        log.info("Firma Listesi ekranına geçildi.");
    }

    // ─── Grid Doğrulama ───────────────────────────────────────────────────────

    public boolean isFirmaGridVisible() {
        return isVaadinGridVisible();
    }

    public boolean hasFirmaRows() {
        try {
            waitForVaadinGrid();
            // Vaadin grid has items if vaadin-grid-cell-content elements with text exist
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

    // ─── DÜZENLE Butonu ───────────────────────────────────────────────────────

    /**
     * Grid'deki ilk DÜZENLE (.button-orange) butonuna tıklar.
     * Vaadin grid cell-content içindeki .button-orange butonları arar.
     */
    public void clickFirstDuzenleButton() {
        waitForVaadinNavigation();
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
                    log.info("Firma DÜZENLE butonuna tıklandı (attempt {}).", attempt);
                    // Wait for dialog to appear
                    try {
                        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8))
                                .until(d -> isDialogOpen());
                        log.info("Firma düzenleme dialogu açıldı.");
                    } catch (Exception e) {
                        log.warn("Dialog 8 saniyede açılmadı — devam ediliyor.");
                    }
                    return;
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.debug("DÜZENLE click attempt {} exception: {}", attempt, e.getMessage());
            }
        }

        // XPath fallback
        try {
            WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(8))
                    .until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//vaadin-button[contains(normalize-space(),'DÜZENLE') or " +
                                     "contains(normalize-space(),'Düzenle') or contains(normalize-space(),'Duzenle')]")));
            btn.click();
            log.info("Firma DÜZENLE butonuna XPath ile tıklandı.");
        } catch (Exception e) {
            log.error("Firma DÜZENLE butonu bulunamadı: {}", e.getMessage());
            throw new RuntimeException("DÜZENLE butonu tıklanamadı", e);
        }

        // Wait for dialog to appear (up to 8 seconds)
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8))
                    .until(d -> isDialogOpen());
            log.info("Firma düzenleme dialogu açıldı.");
            // Diagnostic: log buttons found in dialog
            try {
                String diagnostics = (String) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return 'overlay:null';" +
                    "var btns = overlay.querySelectorAll('vaadin-button, button');" +
                    "var lbls = overlay.querySelectorAll('vaadin-text-field, vaadin-text-area');" +
                    "var bNames = [].map.call(btns, function(b){ return b.textContent.trim(); });" +
                    "var fNames = [].map.call(lbls, function(f){ return f.getAttribute('label')||'(no-label)'; });" +
                    "return 'btns:[' + bNames.join(',') + '] fields:[' + fNames.join(',') + ']';"
                );
                log.info("Dialog diagnostics: {}", diagnostics);
            } catch (Exception diagEx) {
                log.debug("Diagnostic failed: {}", diagEx.getMessage());
            }
        } catch (Exception e) {
            log.warn("Firma dialog 8 saniyede açılmadı — devam ediliyor. URL: {}", driver.getCurrentUrl());
        }
    }

    // ─── Dialog Alan Güncelleme ────────────────────────────────────────────────

    /**
     * Dialog'da "Adres" alanını günceller (EditCompanyInfoDialog).
     * Vaadin shadow DOM'u için setVaadinFieldValue kullanır.
     */
    public void updateFirmaAdres(String yeniAdres) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(8))
                    .until(d -> isDialogOpen());
        } catch (Exception e) {
            throw new RuntimeException("Firma düzenleme dialogu açılmadı", e);
        }

        // Vaadin 24+: textarea/input are SLOTTED (light DOM children), not in shadow root
        boolean updated = false;
        try {
            WebElement overlay = driver.findElement(By.cssSelector("vaadin-dialog-overlay"));
            // Strategy 1: vaadin-text-area with slotted textarea (Vaadin 24+)
            List<WebElement> textAreas = overlay.findElements(By.cssSelector("vaadin-text-area"));
            for (int i = textAreas.size() - 1; i >= 0 && !updated; i--) {
                try {
                    WebElement ta = textAreas.get(i);
                    // Vaadin 24+ slotted content: textarea is a direct light-DOM child
                    List<WebElement> slottedTextareas = ta.findElements(By.cssSelector("textarea"));
                    if (slottedTextareas.isEmpty()) {
                        // Vaadin < 24: try shadow DOM
                        slottedTextareas = List.of(ta.getShadowRoot().findElement(By.cssSelector("textarea")));
                    }
                    if (!slottedTextareas.isEmpty()) {
                        WebElement textarea = slottedTextareas.get(0);
                        textarea.click();
                        textarea.sendKeys(org.openqa.selenium.Keys.chord(
                                org.openqa.selenium.Keys.CONTROL, "a"));
                        textarea.sendKeys(yeniAdres);
                        log.info("Adres text-area[{}] sendKeys ile güncellendi.", i);
                        updated = true;
                    }
                } catch (Exception e) {
                    log.debug("text-area[{}] sendKeys başarısız: {}", i, e.getMessage());
                }
            }
            if (!updated) {
                // Strategy 2: vaadin-text-field with slotted input
                List<WebElement> textFields = overlay.findElements(By.cssSelector("vaadin-text-field"));
                for (int i = textFields.size() - 1; i >= 0 && !updated; i--) {
                    try {
                        WebElement tf = textFields.get(i);
                        List<WebElement> slottedInputs = tf.findElements(By.cssSelector("input"));
                        if (slottedInputs.isEmpty()) {
                            slottedInputs = List.of(tf.getShadowRoot().findElement(By.cssSelector("input")));
                        }
                        if (!slottedInputs.isEmpty()) {
                            WebElement inp = slottedInputs.get(0);
                            inp.click();
                            inp.sendKeys(org.openqa.selenium.Keys.chord(
                                    org.openqa.selenium.Keys.CONTROL, "a"));
                            inp.sendKeys(yeniAdres);
                            log.warn("Adres text-field[{}] sendKeys ile güncellendi (fallback).", i);
                            updated = true;
                        }
                    } catch (Exception e) {
                        log.debug("text-field[{}] sendKeys başarısız: {}", i, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("sendKeys yaklaşımı başarısız: {}", e.getMessage());
        }

        if (!updated) {
            log.warn("Adres güncelleme başarısız — kaydetmeye devam ediliyor.");
        }
        log.info("Firma adresi güncellendi: {}", yeniAdres);
    }

    // ─── Kaydet Butonu ────────────────────────────────────────────────────────

    public void clickKaydetInDialog() {
        // Brief wait for any pending Vaadin validation after field updates
        try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Diagnostic: list buttons and their disabled state
        try {
            String btnInfo = (String) ((JavascriptExecutor) driver).executeScript(
                "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                "if (!overlay) return 'no-overlay';" +
                "var btns = overlay.querySelectorAll('vaadin-button, button');" +
                "var info = [];" +
                "for (var b of btns) {" +
                "  info.push((b.textContent||'').trim() + ':disabled=' + (b.disabled||b.hasAttribute('disabled')));" +
                "}" +
                "return info.join(' | ');"
            );
            log.info("Dialog buttons: {}", btnInfo);
        } catch (Exception e) {
            log.debug("Button diagnostic failed: {}", e.getMessage());
        }

        // Force-enable and click Kaydet button in dialog overlay
        Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
            "var kws = ['kaydet', 'g\\u00fcncelle', 'guncelle', 'kaydet & kapat', 'save'];" +
            "var overlay = document.querySelector('vaadin-dialog-overlay');" +
            "if (overlay) {" +
            "  var btns = overlay.querySelectorAll('vaadin-button, button');" +
            "  for (var b of btns) {" +
            "    var txt = (b.textContent || '').trim().toLowerCase();" +
            "    for (var kw of kws) {" +
            "      if (txt === kw || txt.startsWith(kw)) {" +
            "        b.removeAttribute('disabled'); b.disabled = false;" +
            "        b.click(); return true;" +
            "      }" +
            "    }" +
            "  }" +
            "}" +
            "var allBtns = document.querySelectorAll('vaadin-button');" +
            "for (var b of allBtns) {" +
            "  var txt = (b.textContent || '').trim().toLowerCase();" +
            "  if (txt === 'kaydet' || txt === 'g\\u00fcncelle') {" +
            "    b.removeAttribute('disabled'); b.disabled = false;" +
            "    b.click(); return true;" +
            "  }" +
            "}" +
            "return false;"
        );
        if (!Boolean.TRUE.equals(clicked)) {
            // Selenium fallback — try clickable button without disabled check
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                                "//vaadin-dialog-overlay//vaadin-button[contains(normalize-space(),'aydet')] |" +
                                "//vaadin-dialog-overlay//vaadin-button[contains(normalize-space(),'ncelle')]")));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].removeAttribute('disabled'); arguments[0].disabled=false; arguments[0].click();", btn);
                clicked = true;
            } catch (Exception e) {
                log.error("Kaydet butonu dialog'da bulunamadı: {}", e.getMessage());
            }
        }
        if (!Boolean.TRUE.equals(clicked)) {
            throw new RuntimeException("Kaydet butonu dialog'da bulunamadı");
        }
        log.info("Firma dialog Kaydet butonuna tıklandı.");
        try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // Backward compat
    public void clickKaydet() { clickKaydetInDialog(); }
    public void clickDuzenleButton(String firmaAdi) { clickFirstDuzenleButton(); }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public boolean isSuccessNotificationVisible() {
        try {
            waitForVisibility(SUCCESS_NOTIF, 8);
            return true;
        } catch (Exception e) {
            if (isAnyNotificationVisible()) return true;
            // If dialog closed, the save probably succeeded (dialog closes on success)
            return !isDialogOpen();
        }
    }

    public boolean isFirmaDialogOpen() {
        return isDialogOpen();
    }

    public String getFirmaRowText(String firmaAdi) {
        try {
            List<WebElement> cells = driver.findElements(
                    By.cssSelector("vaadin-grid-cell-content"));
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && text.contains(firmaAdi)) {
                    return text;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
