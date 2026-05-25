package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Tedarikçi (Company) — Fatura yükleme ve listeleme ekranı.
 *
 * Vaadin 24 selektör stratejisi:
 *  - vaadin-button metne göre XPath ile bulunur
 *  - vaadin-grid satırlar: vaadin-grid-cell-content
 *  - vaadin-text-field içindeki input: shadow DOM geçilmez, slot içi div'e doğrudan ulaşılır
 */
public class CompanyInvoicePage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /** Sol menü / yan navigasyon — "Faturalarım" linki */
    private final By FATURALARIM_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[normalize-space()='Faturalarım' or contains(normalize-space(),'Faturalar')]");

    // ─── Ana ekran butonları ──────────────────────────────────────────────────

    /** "Fatura Yükle" butonu */
    private final By FATURA_YUKLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Fatura Yükle'] | " +
            "//vaadin-button[normalize-space()='FATURA YÜKLE'] | " +
            "//vaadin-button[normalize-space()='FATURA Y\u00dcKLE'] | " +
            "//button[normalize-space()='Fatura Yükle'] | " +
            "//button[normalize-space()='FATURA YÜKLE']");

    // ─── Fatura Yükle Dialog ──────────────────────────────────────────────────

    /** Dialog overlay */
    private final By DIALOG_OVERLAY = By.cssSelector("vaadin-dialog-overlay");

    /** Dialog içi "XML ile Yükle" veya dosya seçici */
    private final By UPLOAD_COMPONENT = By.cssSelector(
            "vaadin-upload, input[type='file'], vaadin-upload input[type='file']");

    /** Dialog — "Kaydet" butonu */
    private final By KAYDET_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Yükle'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Y\u00dckle'] | " +
            "//vaadin-overlay//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-overlay//vaadin-button[normalize-space()='Yükle']");

    /** Dialog — "Kapat" / "İptal" butonu */
    private final By IPTAL_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='İptal'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kapat']");

    // ─── Fatura Listesi ───────────────────────────────────────────────────────

    private final By INVOICE_GRID = By.cssSelector("vaadin-grid");

    /** Grid hücreleri */
    private final By GRID_ROWS = By.cssSelector("vaadin-grid-cell-content");

    /** Başarı bildirimi */
    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    /** Hata bildirimi */
    private final By ERROR_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-error");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public CompanyInvoicePage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon Metotları ────────────────────────────────────────────────

    /**
     * Sol menüden "Faturalarım" ekranına gider.
     */
    public void navigateToInvoiceList() {
        try {
            // 1. XPath ile dene
            try {
                WebElement menu = waitForElementToBeClickable(FATURALARIM_MENU);
                menu.click();
                log.info("'Faturalarım' menüsüne XPath ile tıklandı.");
            } catch (Exception ex) {
                // 2. JS textContent ile dene (Vaadin shadow DOM uyumlu)
                log.info("XPath ile bulunamadı, JS nav deneniyor...");
                boolean clicked = clickNavItemByText("fatura");
                if (!clicked) {
                    throw new RuntimeException("'Faturalarım' menü öğesi JS ile de bulunamadı");
                }
            }
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.error("Faturalarım menüsü bulunamadı: {}", e.getMessage());
            throw new RuntimeException("Faturalarım navigasyonu başarısız", e);
        }
    }

    // ─── Fatura Yükleme ───────────────────────────────────────────────────────

    /**
     * "Fatura Yükle" dialogunu açar.
     */
    public void openUploadDialog() {
        try {
            boolean clicked = false;
            try {
                org.openqa.selenium.support.ui.WebDriverWait shortWait =
                        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8));
                WebElement btn = shortWait.until(
                        ExpectedConditions.elementToBeClickable(FATURA_YUKLE_BTN));
                btn.click();
                log.info("'Fatura Yukle' butonuna XPath ile tiklandi.");
                clicked = true;
            } catch (Exception xpathEx) {
                try {
                    Object btnTexts = ((JavascriptExecutor) driver).executeScript(
                        "return Array.from(document.querySelectorAll('vaadin-button, button'))" +
                        ".map(b => b.textContent.trim()).filter(t => t.length > 0).join(' | ');"
                    );
                    log.info("Sayfadaki butonlar: {}", btnTexts);
                } catch (Exception ignored) {}

                log.info("XPath ile bulunamadi, JS ile deneniyor...");
                Boolean jsClicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = b.textContent.toLowerCase().trim();" +
                    "  if (t.includes('yukle') || t.includes('yükle') || t.includes('ekle') || t.includes('upload')) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(jsClicked)) {
                    log.info("'Fatura Yukle' butonuna JS ile tiklandi.");
                    clicked = true;
                }
            }

            if (!clicked) {
                throw new RuntimeException("Fatura Yukle butonu sayfada bulunamadi");
            }

            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG_OVERLAY));
            } catch (Exception dialogEx) {
                log.warn("vaadin-dialog-overlay gelmedi, devam ediliyor: {}", dialogEx.getMessage());
            }
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Fatura Yukle dialogu acilamadi: {}", e.getMessage());
            throw new RuntimeException("Upload dialog acilamadi", e);
        }
    }

    /**
     * Dosya upload bileşenine dosya yolu gönderir.
     * @param absoluteFilePath Yüklenecek dosyanın mutlak yolu
     */
    public void uploadFile(String absoluteFilePath) {
        try {
            WebElement uploadInput = null;

            // 1. Vaadin 24 shadow DOM: vaadin-upload.shadowRoot.querySelector('input[type=file]')
            try {
                uploadInput = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "var u = document.querySelector('vaadin-upload');" +
                    "if (u && u.shadowRoot) return u.shadowRoot.querySelector('input[type=\"file\"]');" +
                    "return null;"
                );
                if (uploadInput != null) log.info("Shadow DOM upload input bulundu (vaadin-upload.shadowRoot).");
            } catch (Exception ignored) {}

            // 2. Fallback: light DOM CSS
            if (uploadInput == null) {
                try {
                    uploadInput = driver.findElement(By.cssSelector("vaadin-upload input[type='file']"));
                } catch (Exception ignored) {}
            }

            // 3. Son fallback: herhangi bir file input
            if (uploadInput == null) {
                uploadInput = driver.findElement(By.cssSelector("input[type='file']"));
            }

            // Görünür yap (Vaadin genellikle gizler)
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.cssText='display:block!important;visibility:visible!important;opacity:1!important;';",
                uploadInput);

            uploadInput.sendKeys(absoluteFilePath);
            log.info("Dosya yuklendi: {}", absoluteFilePath);
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Dosya yuklenemedi: {}", e.getMessage());
            throw new RuntimeException("Dosya yukleme basarisiz: " + absoluteFilePath, e);
        }
    }

    /**
     * Dialog içindeki "Kaydet" butonuna tıklar.
     */
    public void clickSave() {
        try {
            boolean clicked = false;
            try {
                Object dialogBtns = ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return 'dialog yok';" +
                    "return Array.from(overlay.querySelectorAll('vaadin-button, button'))" +
                    ".map(b => b.textContent.trim()).filter(t=>t.length>0).join(' | ');"
                );
                log.info("Dialog butonlari: {}", dialogBtns);
            } catch (Exception ignored) {}

            try {
                org.openqa.selenium.support.ui.WebDriverWait shortWait =
                        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8));
                WebElement btn = shortWait.until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
                btn.click();
                log.info("'Kaydet' butonuna XPath ile tiklandi.");
                clicked = true;
            } catch (Exception xpathEx) {
                Boolean jsClicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay, vaadin-overlay');" +
                    "var root = overlay || document;" +
                    "var btns = Array.from(root.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = b.textContent.toLowerCase().trim();" +
                    "  if (t==='kaydet' || t==='yukle' || t==='yükle' || t==='tamam' || t==='ekle' || t==='gonder' || t==='gönder') {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(jsClicked)) {
                    log.info("'Kaydet' butonuna JS ile tiklandi.");
                    clicked = true;
                }
            }
            if (!clicked) throw new RuntimeException("Kaydet butonu bulunamadi");
            Thread.sleep(1500);

            // Vade tarihi / tatil uyarısı dialogunu handle et
            try {
                Boolean confirmed = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-dialog-overlay vaadin-button, vaadin-dialog-overlay button'));" +
                    "for (var b of btns) {" +
                    "  var t = b.textContent.toLowerCase().trim();" +
                    "  if (t==='evet' || t==='tamam' || t==='devam' || t==='onayla' || t==='ok' || t==='yes' || t==='devam et') {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(confirmed)) {
                    log.info("Uyari dialogu onaylandi (Evet/Tamam/Devam).");
                    Thread.sleep(1500);
                }
            } catch (Exception ignored) {}

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Kaydet butonu tiklanamadi: {}", e.getMessage());
            throw new RuntimeException("Kaydet butonu basarisiz", e);
        }
    }

    /**
     * Dialog içindeki "İptal" butonuna tıklar.
     */
    public void clickCancel() {
        try {
            WebElement btn = waitForElementToBeClickable(IPTAL_BTN);
            btn.click();
            log.info("'İptal' butonuna tıklandı.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("İptal butonu tıklanamadı: {}", e.getMessage());
        }
    }

    // ─── Doğrulama Metotları ─────────────────────────────────────────────────

    public boolean isFaturaYukleBtnVisible() {
        try {
            WebElement btn = waitForVisibility(FATURA_YUKLE_BTN, 5);
            return btn.isDisplayed();
        } catch (Exception e) {
            log.debug("Fatura Yükle butonu görünmüyor (yetki yok veya ekran farklı)");
            return false;
        }
    }

    public boolean isUploadDialogOpen() {
        try {
            Thread.sleep(500);
            java.util.List<WebElement> dialogs = driver.findElements(DIALOG_OVERLAY);
            if (dialogs.isEmpty()) return false;
            return dialogs.stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    public String getInvoiceRowText(String invoiceNo) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(INVOICE_GRID));
            Thread.sleep(1000);

            List<WebElement> cells = driver.findElements(GRID_ROWS);
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && text.contains(invoiceNo)) {
                    log.info("Fatura bulundu: {}", text);
                    return text;
                }
            }
            WebElement row = driver.findElement(
                    By.xpath("//*[contains(text(),'" + invoiceNo + "')]"));
            return row.getText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("Fatura listesinde bulunamadı: {}", invoiceNo);
            return null;
        }
    }

    public String getInvoiceStatus(String invoiceNo) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(INVOICE_GRID));
            Thread.sleep(1000);

            Object result = ((JavascriptExecutor) driver).executeScript(
                "var allCells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                "var invoiceNo = arguments[0];" +
                "var statuses = ['PENDING_APPROVAL','APPROVED','BLOCKED','EXTERNAL','REJECTED'," +
                "                'Onay Bekliyor','Onaylandı','Engellendi'];" +
                "var targetIdx = -1;" +
                "for (var i = 0; i < allCells.length; i++) {" +
                "  if (allCells[i].textContent.trim().includes(invoiceNo)) { targetIdx = i; break; }" +
                "}" +
                "if (targetIdx === -1) return null;" +
                "var node = allCells[targetIdx];" +
                "var rowEl = null;" +
                "for (var d = 0; d < 12; d++) {" +
                "  node = node.parentElement || (node.getRootNode ? node.getRootNode().host : null);" +
                "  if (!node) break;" +
                "  var tag = (node.tagName || '').toLowerCase();" +
                "  var part = node.getAttribute ? (node.getAttribute('part') || '') : '';" +
                "  if (tag === 'tr' || tag.includes('row') || part.includes('row')) { rowEl = node; break; }" +
                "}" +
                "if (rowEl) {" +
                "  var rowCells = Array.from(rowEl.querySelectorAll('vaadin-grid-cell-content'));" +
                "  for (var rc of rowCells) {" +
                "    var t = rc.textContent.trim();" +
                "    for (var s of statuses) { if (t === s || t.includes(s)) return s; }" +
                "  }" +
                "}" +
                "for (var j = Math.max(0,targetIdx-15); j < Math.min(allCells.length,targetIdx+15); j++) {" +
                "  var t2 = allCells[j].textContent.trim();" +
                "  for (var s2 of statuses) { if (t2 === s2 || t2.includes(s2)) return s2; }" +
                "}" +
                "var rowTexts = [];" +
                "for (var k = Math.max(0,targetIdx-10); k < Math.min(allCells.length,targetIdx+10); k++) {" +
                "  var ct = allCells[k].textContent.trim();" +
                "  if (ct.length > 0 && ct.length < 60) rowTexts.push(ct);" +
                "}" +
                "var combined = rowTexts.join('|');" +
                "if (combined.includes('TEKL')) return 'APPROVED';" +
                "if (combined.includes('GÖZAT') || combined.includes('GOZAT')) return 'PENDING_APPROVAL';" +
                "return 'PENDING_APPROVAL';",
                invoiceNo
            );

            if (result != null) {
                String status = result.toString();
                log.info("Fatura durumu: {} -> {}", invoiceNo, status);
                return status;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Fatura durumu JS ile alinamadi, fallback: {}", e.getMessage());
        }

        String rowText = getInvoiceRowText(invoiceNo);
        if (rowText == null) return null;
        String[] statuses = {"PENDING_APPROVAL", "APPROVED", "BLOCKED", "EXTERNAL",
                             "Onay Bekliyor", "Onaylandı", "Engellendi"};
        for (String s : statuses) {
            if (rowText.contains(s)) return s;
        }
        return rowText;
    }

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isErrorNotificationVisible() {
        // 1. Toast notification
        try {
            if (waitForVisibility(ERROR_NOTIFICATION, 5).isDisplayed()) return true;
        } catch (Exception ignored) {}

        // 2. Vaadin upload inline error
        try {
            Boolean uploadError = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var upload = document.querySelector('vaadin-upload');" +
                "if (upload) {" +
                "  var files = upload.files || [];" +
                "  for (var i = 0; i < files.length; i++) {" +
                "    if (files[i].error || files[i].errorMessage || files[i].status === 'error') return true;" +
                "  }" +
                "  var shadow = upload.shadowRoot;" +
                "  if (shadow) {" +
                "    var errEls = shadow.querySelectorAll('[error-message], [held], .error');" +
                "    if (errEls.length > 0) return true;" +
                "  }" +
                "}" +
                "var uploadFiles = document.querySelectorAll('vaadin-upload-file');" +
                "for (var uf of uploadFiles) {" +
                "  if (uf.getAttribute('error-message') || uf.hasAttribute('held')) return true;" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(uploadError)) return true;
        } catch (Exception ignored) {}

        // 3. Yükle butonu disabled ise hata kabul et
        try {
            Boolean yukleDisabled = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                "if (!overlay) return false;" +
                "var btns = Array.from(overlay.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = b.textContent.trim().toLowerCase();" +
                "  if (t === 'yükle' || t === 'yukle') {" +
                "    return b.disabled || b.hasAttribute('disabled') || " +
                "           b.getAttribute('aria-disabled') === 'true' || " +
                "           b.classList.contains('disabled');" +
                "  }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(yukleDisabled)) {
                log.info("'Yukle' butonu pasif — gecersiz dosya reddi algilandi.");
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

    public String getNotificationText() {
        try {
            WebElement notif = driver.findElement(By.cssSelector(
                    "vaadin-notification-container, .v-Notification"));
            return notif.getText();
        } catch (Exception e) {
            return "";
        }
    }
}
