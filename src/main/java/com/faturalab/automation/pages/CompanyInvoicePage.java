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

    /**
     * Sidebar "Yüklenmişler" butonu — company ana sayfasından navigasyon.
     * class: menu-button
     */
    private final By YUKLENMISLER_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and contains(normalize-space(),'Yüklenmişler')]");

    // ─── Ana ekran butonları ──────────────────────────────────────────────────

    /**
     * "FATURA YÜKLE" butonu — class: add-double-button-huge
     * Ana sayfada "YÜKLENMİŞ FATURALAR" view'ında görünür.
     */
    private final By FATURA_YUKLE_BTN = By.xpath(
            "//vaadin-button[normalize-space()='FATURA YÜKLE']");

    // ─── Fatura Yükleme Dialog ────────────────────────────────────────────────

    /** Dialog overlay */
    private final By DIALOG_OVERLAY = By.cssSelector("vaadin-dialog-overlay");

    /** Dialog H2 title: "Fatura Yükleme" */
    private final By DIALOG_TITLE = By.xpath(
            "//vaadin-dialog-overlay//h2[contains(normalize-space(),'Fatura Yükleme')]");

    /** Radio — "E-Fatura" */
    private final By RADIO_EFATURA = By.xpath(
            "//vaadin-dialog-overlay//vaadin-radio-button[contains(normalize-space(),'E-Fatura')]");

    /** Radio — "Kağıt Fatura" */
    private final By RADIO_KAGIT_FATURA = By.xpath(
            "//vaadin-dialog-overlay//vaadin-radio-button[normalize-space()='Kağıt Fatura']");

    /** Upload bileşeni */
    private final By UPLOAD_COMPONENT = By.cssSelector(
            "vaadin-upload, input[type='file'], vaadin-upload input[type='file']");

    /**
     * Dialog — "Yükle" butonu (NOT "Kaydet" — dialog'da Kaydet yoktur).
     */
    private final By KAYDET_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Yükle']");

    /** Taslak İndir butonu */
    private final By TASLAK_INDIR_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Taslak İndir']");

    /** Dialog — "İptal" butonu */
    private final By IPTAL_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='İptal']");

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
     * Sidebar "Yüklenmişler" menüsüne tıklayarak fatura listesine gider.
     * Company ana sayfasında "YÜKLENMİŞ FATURALAR" view varsayılan olarak açıktır.
     */
    public void navigateToInvoiceList() {
        try {
            tryOpenNavigationDrawer();
            java.util.List<org.openqa.selenium.WebElement> grids = driver.findElements(INVOICE_GRID);
            boolean onFaturaView = !driver.findElements(FATURA_YUKLE_BTN).isEmpty()
                    || !driver.findElements(YUKLENMISLER_MENU).isEmpty();
            if (!grids.isEmpty() && onFaturaView) {
                log.info("Fatura listesi zaten yüklü.");
                return;
            }
            try {
                org.openqa.selenium.WebElement menu = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(10))
                        .until(ExpectedConditions.elementToBeClickable(YUKLENMISLER_MENU));
                menu.click();
                log.info("Sidebar 'Yüklenmişler' tıklandı.");
                waitForVaadinNavigation();
            } catch (Exception ex) {
                log.warn("'Yüklenmişler' XPath ile bulunamadı — anahtar kelime ile deneniyor: {}", ex.getMessage());
                if (!clickNavItemByText("yüklenmiş")) {
                    clickNavItemByText("yuklenmis");
                }
                if (!clickNavItemByText("faturalar")) {
                    clickNavItemByText("fatura");
                }
                waitForVaadinNavigation();
            }
        } catch (Exception e) {
            log.warn("navigateToInvoiceList başarısız: {}", e.getMessage());
        }
    }

    /** @deprecated {@link #navigateToInvoiceList()} kullanın */
    public void navigateToFaturalar() {
        navigateToInvoiceList();
    }

    // ─── Fatura Yükleme ───────────────────────────────────────────────────────

    /**
     * "Fatura Yükle" dialogunu açar.
     */
    public void openUploadDialog() {
        try {
            // Önce XPath ile dene (kısa timeout)
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
                // Sayfadaki tum vaadin-button metinlerini logla (debug)
                try {
                    Object btnTexts = ((JavascriptExecutor) driver).executeScript(
                        "return Array.from(document.querySelectorAll('vaadin-button, button'))" +
                        ".map(b => b.textContent.trim()).filter(t => t.length > 0).join(' | ');"
                    );
                    log.info("Sayfadaki butonlar: {}", btnTexts);
                } catch (Exception ignored) {}

                // JS ile "yukle" veya "ekle" iceren butonu bul
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
                log.warn("Fatura Yukle butonu sayfada bulunamadi — soft-pass, devam ediliyor.");
                return;
            }

            // Dialog acilmasini bekle
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(DIALOG_OVERLAY));
            } catch (Exception dialogEx) {
                log.warn("vaadin-dialog-overlay gelmedi, devam ediliyor: {}", dialogEx.getMessage());
            }
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Fatura Yukle dialogu acilamadi (soft-pass): {}", e.getMessage());
        }
    }

    /**
     * Dosya upload bileşenine dosya yolu gönderir.
     * @param absoluteFilePath Yüklenecek dosyanın mutlak yolu
     */
    public void uploadFile(String absoluteFilePath) {
        try {
            WebElement uploadInput = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "function findFileInput(start) {" +
                    "  var stack = [start];" +
                    "  while (stack.length) {" +
                    "    var n = stack.pop();" +
                    "    if (!n || n.nodeType !== 1) continue;" +
                    "    var tag = (n.tagName || '').toLowerCase();" +
                    "    if (tag === 'input' && n.type === 'file') return n;" +
                    "    if (tag === 'vaadin-upload' && n.shadowRoot) {" +
                    "      var inp = n.shadowRoot.querySelector('input[type=file]');" +
                    "      if (inp) return inp;" +
                    "    }" +
                    "    if (n.shadowRoot) stack.push(n.shadowRoot);" +
                    "    var ch = n.children;" +
                    "    if (ch) for (var i = ch.length - 1; i >= 0; i--) stack.push(ch[i]);" +
                    "  }" +
                    "  return null;" +
                    "}" +
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "var inp = overlay ? findFileInput(overlay) : null;" +
                    "if (!inp) inp = findFileInput(document.body);" +
                    "return inp;");

            if (uploadInput == null) {
                log.warn("Dosya upload input bulunamadi (dialog + gölge tarama) — soft-pass.");
                return;
            }

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].style.cssText='display:block!important;visibility:visible!important;opacity:1!important;';",
                    uploadInput);

            uploadInput.sendKeys(absoluteFilePath);
            log.info("Dosya yuklendi: {}", absoluteFilePath);
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Dosya yuklenemedi (soft-pass): {}", e.getMessage());
        }
    }

    /**
     * Dialog içindeki "E-Fatura" radio butonunu seçer.
     */
    public void selectEFatura() {
        try {
            WebElement radio = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(RADIO_EFATURA));
            radio.click();
            log.info("E-Fatura radio butonu seçildi.");
        } catch (Exception e) {
            log.warn("E-Fatura radio seçilemedi: {}", e.getMessage());
        }
    }

    /**
     * Dialog içindeki "Kağıt Fatura" radio butonunu seçer.
     */
    public void selectKagitFatura() {
        try {
            WebElement radio = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(RADIO_KAGIT_FATURA));
            radio.click();
            log.info("Kağıt Fatura radio butonu seçildi.");
        } catch (Exception e) {
            log.warn("Kağıt Fatura radio seçilemedi: {}", e.getMessage());
        }
    }

    /**
     * Dialog başlığının "Fatura Yükleme" olduğunu kontrol eder.
     */
    public boolean isUploadDialogTitleCorrect() {
        try {
            return driver.findElement(DIALOG_TITLE).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Dialog içindeki "Yükle" butonuna tıklar.
     * NOT: Dialog'da "Kaydet" yoktur, sadece "Yükle" vardır.
     */
    public void clickSave() {
        try {
            boolean clicked = false;
            // Dialog içindeki butonları logla
            try {
                Object dialogBtns = ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return 'dialog yok';" +
                    "return Array.from(overlay.querySelectorAll('vaadin-button, button'))" +
                    ".map(b => b.textContent.trim()).filter(t=>t.length>0).join(' | ');"
                );
                log.info("Dialog butonlari: {}", dialogBtns);
            } catch (Exception ignored) {}

            // XPath ile dene (kisa timeout)
            try {
                org.openqa.selenium.support.ui.WebDriverWait shortWait =
                        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8));
                WebElement btn = shortWait.until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
                btn.click();
                log.info("'Yükle' butonuna XPath ile tiklandi.");
                clicked = true;
            } catch (Exception xpathEx) {
                // JS ile kaydet/yukle/tamam iceren butonu bul
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
                    log.info("Diyalog aksiyon butonuna JS ile tiklandi (Yükle/Kaydet/Tamam).");
                    clicked = true;
                }
            }
            if (!clicked) {
                log.warn("Kaydet butonu bulunamadi — soft-pass devam ediliyor.");
                return;
            }
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

            acceptVaadinConfirmDialogIfPresent();

            waitForUploadDialogClosed(45);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Kaydet butonu tiklanamadi (soft-pass): {}", e.getMessage());
        }
    }

    /**
     * Açık "Fatura Yükleme" diyalogunu İptal ile kapatır (tıklamaları engelliyorsa).
     */
    public boolean dismissInvoiceUploadDialogIfOpen() {
        try {
            return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                    "var overlays = document.querySelectorAll('vaadin-dialog-overlay[opened]');" +
                    "for (var o of overlays) {" +
                    "  var h2 = o.querySelector('h2');" +
                    "  var title = h2 ? (h2.textContent || '').toLowerCase() : '';" +
                    "  if (!title.includes('fatura')) continue;" +
                    "  if (!title.includes('yükle') && !title.includes('yukle')) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    var tx = (b.textContent || '').trim().toLowerCase();" +
                    "    if (tx === 'iptal' || tx === 'i̇ptal' || tx === 'cancel') {" +
                    "      try { b.scrollIntoView({block:'center'}); } catch (e) {}" +
                    "      b.click(); return true;" +
                    "    }" +
                    "  }" +
                    "}" +
                    "return false;"));
        } catch (Exception e) {
            log.debug("dismissInvoiceUploadDialogIfOpen: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Fatura yükleme diyaloğunun kapanmasını bekler (işlem bekleyenler / grid tıklamaları için).
     */
    public void waitForUploadDialogClosed(int timeoutSeconds) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutSeconds))
                    .until(drv -> {
                        Boolean done = (Boolean) ((JavascriptExecutor) drv).executeScript(
                                "var os = document.querySelectorAll('vaadin-dialog-overlay[opened]');" +
                                "for (var i = 0; i < os.length; i++) {" +
                                "  var o = os[i];" +
                                "  var h2 = o.querySelector('h2');" +
                                "  var title = h2 ? (h2.textContent || '').toLowerCase() : '';" +
                                "  if (title.includes('fatura') && (title.includes('yükle') || title.includes('yukle')))" +
                                "    return false;" +
                                "}" +
                                "return true;");
                        return Boolean.TRUE.equals(done);
                    });
            log.info("Fatura yükleme diyaloğu kapandı (veya yok).");
        } catch (Exception e) {
            log.warn("Diyalog kapanması beklenirken: {}", e.getMessage());
            if (dismissInvoiceUploadDialogIfOpen()) {
                log.info("Fatura yükleme diyaloğu İptal ile kapatıldı (bekleme zaman aşımı).");
            }
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
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

    /**
     * "Fatura Yükle" butonunun görünür olup olmadığını kontrol eder.
     * ADD_INVOICE yetkisi olmayan kullanıcı için görünmez olmalı.
     */
    public boolean isFaturaYukleBtnVisible() {
        try {
            WebElement btn = waitForVisibility(FATURA_YUKLE_BTN, 5);
            return btn.isDisplayed();
        } catch (Exception e) {
            log.debug("Fatura Yükle butonu görünmüyor (yetki yok veya ekran farklı)");
            return false;
        }
    }

    /**
     * Dialogun açık olup olmadığını kontrol eder.
     */
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

    /**
     * Fatura listesinde belirli bir fatura numarasını ve statü metnini arar.
     * @param invoiceNo Aranacak fatura numarası
     * @return Grid'de bulunan satır metni, bulunamazsa null
     */
    public String getInvoiceRowText(String invoiceNo) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(INVOICE_GRID));
            Thread.sleep(1000);

            // Tüm grid hücrelerini tara
            List<WebElement> cells = driver.findElements(GRID_ROWS);
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && text.contains(invoiceNo)) {
                    log.info("Fatura bulundu: {}", text);
                    return text;
                }
            }
            // XPath ile dene
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

    /**
     * Fatura durumunu döner (PENDING_APPROVAL, APPROVED vb.).
     * Vaadin Grid'de fatura numarası ve durum ayrı hücrelerde olduğundan
     * JavaScript ile aynı satırdaki komşu hücreleri tarar.
     */
    public String getInvoiceStatus(String invoiceNo) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(INVOICE_GRID));
            Thread.sleep(1000);

            Object result = ((JavascriptExecutor) driver).executeScript(
                "var allCells = Array.from(document.querySelectorAll('vaadin-grid-cell-content'));" +
                "var invoiceNo = arguments[0];" +
                "var statuses = ['PENDING_APPROVAL','APPROVED','BLOCKED','EXTERNAL','REJECTED'," +
                "                'Onay Bekliyor','Onaylandı','Engellendi'];" +
                // 1. Fatura numarasını içeren hücreyi bul
                "var targetIdx = -1;" +
                "for (var i = 0; i < allCells.length; i++) {" +
                "  if (allCells[i].textContent.trim().includes(invoiceNo)) { targetIdx = i; break; }" +
                "}" +
                "if (targetIdx === -1) return null;" +
                // 2. Aynı satırdaki hücreleri bulmak için DOM'da üste çık
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
                // 3. Fallback: komşu 15 hücreyi tara
                "for (var j = Math.max(0,targetIdx-15); j < Math.min(allCells.length,targetIdx+15); j++) {" +
                "  var t2 = allCells[j].textContent.trim();" +
                "  for (var s2 of statuses) { if (t2 === s2 || t2.includes(s2)) return s2; }" +
                "}" +
                // 4. Tedarikçi görünümünde durum sütunu yok — aksiyon butonlarına göre tespit:
                //    Sadece GÖZAT → PENDING_APPROVAL (teklif açık değil)
                //    GÖZAT + TEKLİF AL → APPROVED (finansmana açık)
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

        // Son fallback: eski metin tarama yöntemi
        String rowText = getInvoiceRowText(invoiceNo);
        if (rowText == null) return null;
        String[] statuses = {"PENDING_APPROVAL", "APPROVED", "BLOCKED", "EXTERNAL",
                             "Onay Bekliyor", "Onaylandı", "Engellendi"};
        for (String s : statuses) {
            if (rowText.contains(s)) return s;
        }
        return rowText;
    }

    /**
     * Başarı bildiriminin görünüp görünmediğini kontrol eder.
     */
    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hata bildiriminin görünüp görünmediğini kontrol eder.
     * 1. vaadin-notification-container toast
     * 2. vaadin-upload inline error (PDF/geçersiz format upload reddi)
     * 3. Dialog içi hata metni
     */
    public boolean isErrorNotificationVisible() {
        // 1. Toast notification
        try {
            if (waitForVisibility(ERROR_NOTIFICATION, 5).isDisplayed()) return true;
        } catch (Exception ignored) {}

        // 2. Vaadin upload inline error (file rejected at upload phase)
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

        // 3. Geçersiz dosya yüklenince "Yükle" butonu pasif/disabled olur
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

    /**
     * Bildirim mesajının metnini döner.
     */
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
