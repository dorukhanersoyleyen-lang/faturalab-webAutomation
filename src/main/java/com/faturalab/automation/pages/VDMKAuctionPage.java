package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * VDMK (Varlığa Dayalı Menkul Kıymet) — CMB Onaylı İhale Akışı.
 *
 * Durum zinciri: CMB_APPROVAL -> CMB_APPROVED -> WAITING -> PENDINGBUYER -> ACCEPTED
 *
 * Ekranlar:
 *  - Alıcı: Bekleyen Fatura Listesi → ONAYLA
 *  - Tedarikçi: CompanyVDMKFormView → VDMK ihale formu
 *  - Admin: CMB Onay Listesi → CMB Onayla
 *  - Finansman: Aktif İhaleler → teklif ver (CMB_APPROVAL'da bloklu)
 *  - Tedarikçi: CompanyAuctionOffersView → Kabul Et
 */
public class VDMKAuctionPage extends BasePageObject {

    // ─── Genel Grid ───────────────────────────────────────────────────────────

    private final By GRID          = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS    = By.cssSelector("vaadin-grid-cell-content");
    private final By SUCCESS_NOTIF = By.cssSelector("vaadin-notification-container, .v-Notification");

    // ─── Onay Dialog ──────────────────────────────────────────────────────────

    private final By CONFIRM_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onay']");

    // ─── Alıcı: Bekleyen Fatura ───────────────────────────────────────────────

    private final By ALICI_BEKLEYEN_FATURA_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[(contains(normalize-space(),'Fatura') or contains(normalize-space(),'fatura')) and " +
            "(contains(normalize-space(),'Bekleyen') or contains(normalize-space(),'Onay') " +
            " or contains(normalize-space(),'Pending') or contains(normalize-space(),'Liste'))]");

    private final By ONAYLA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='ONAYLA'] | " +
            "//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-button[contains(normalize-space(),'Onayla') and not(contains(normalize-space(),'CMB'))] | " +
            "//button[normalize-space()='ONAYLA']");

    // ─── Tedarikçi: VDMK İhale Formu ─────────────────────────────────────────

    private final By VDMK_FORM_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[(contains(normalize-space(),'VDMK') or contains(normalize-space(),'Menkul') " +
            " or contains(normalize-space(),'Varlık') or contains(normalize-space(),'CMB'))]");

    private final By APPROVED_INVOICE_ROW = By.xpath(
            "//*[contains(text(),'APPROVED') or contains(text(),'Onaylı') or contains(text(),'Onaylandi')]" +
            "/ancestor::vaadin-grid-row | " +
            "//vaadin-grid-cell-content[contains(text(),'APPROVED')]");

    private final By SELECT_INVOICE_BTN = By.xpath(
            "//vaadin-button[contains(normalize-space(),'Seç') or contains(normalize-space(),'Sec') " +
            " or contains(normalize-space(),'İhale') or normalize-space()='Seç'] | " +
            "//vaadin-button[contains(normalize-space(),'Select')]");

    // VDMK-specific form fields: label'larında VDMK, CMB, Menkul, ihraç gibi kelimeler
    private final By VDMK_FIELD = By.xpath(
            "//vaadin-text-field[contains(@label,'VDMK') or contains(@label,'Menkul')]//input | " +
            "//vaadin-select[contains(@label,'VDMK') or contains(@label,'Menkul')] | " +
            "//vaadin-text-field[contains(@label,'ihraç') or contains(@label,'ihrac')]//input | " +
            "//input[contains(@id,'vdmk') or contains(@name,'vdmk')]");

    private final By CMB_FIELD = By.xpath(
            "//vaadin-text-field[contains(@label,'CMB')]//input | " +
            "//vaadin-select[contains(@label,'CMB')] | " +
            "//input[contains(@id,'cmb') or contains(@name,'cmb')]");

    private final By VDMK_SUBMIT_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Gönder'] | " +
            "//vaadin-button[normalize-space()='Gonder'] | " +
            "//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-button[normalize-space()='KAYDET'] | " +
            "//vaadin-button[normalize-space()='Yayınla'] | " +
            "//vaadin-button[normalize-space()='Yayinla'] | " +
            "//vaadin-button[contains(normalize-space(),'Gönder') or contains(normalize-space(),'Submit')]");

    // ─── Tedarikçi: Aktif İhaleler / Offers View ──────────────────────────────

    private final By AKTIF_IHALELER_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[(contains(normalize-space(),'İhale') or contains(normalize-space(),'Ihale') " +
            " or contains(normalize-space(),'Auction')) and " +
            "(contains(normalize-space(),'Aktif') or contains(normalize-space(),'Liste'))]");

    private final By KABUL_ET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Kabul Et'] | " +
            "//vaadin-button[normalize-space()='KABUL ET'] | " +
            "//vaadin-button[normalize-space()='Kabul'] | " +
            "//vaadin-button[normalize-space()='Accept']");

    // ─── Admin: CMB Onay Listesi ───────────────────────────────────────────────

    private final By CMB_APPROVAL_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'CMB') or " +
            "(contains(normalize-space(),'VDMK') and contains(normalize-space(),'Onay'))]");

    private final By CMB_ONAYLA_BTN = By.xpath(
            "//vaadin-button[contains(normalize-space(),'CMB') and contains(normalize-space(),'Onayla')] | " +
            "//vaadin-button[normalize-space()='CMB Onayla'] | " +
            "//vaadin-button[normalize-space()='CMB ONAYLA'] | " +
            "//button[contains(normalize-space(),'CMB')]");

    // ─── Finansman: Teklif Formu ───────────────────────────────────────────────

    private final By TEKLIF_TUTARI_INPUT = By.xpath(
            "//vaadin-number-field[contains(@label,'Tutar') or contains(@label,'Miktar') " +
            " or contains(@label,'Amount')]//input | " +
            "//vaadin-text-field[contains(@label,'Teklif') or contains(@label,'Tutar')]//input | " +
            "//input[contains(@name,'amount') or contains(@id,'teklif') or contains(@placeholder,'tutar')]");

    private final By KAYDET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-button[normalize-space()='KAYDET'] | " +
            "//vaadin-button[normalize-space()='Teklif Ver'] | " +
            "//vaadin-button[normalize-space()='Gönder'] | " +
            "//vaadin-button[normalize-space()='Submit']");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public VDMKAuctionPage(WebDriver driver) {
        super(driver);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ALICI — Bekleyen Fatura
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Alıcı panelinde bekleyen fatura listesine gider.
     */
    public void navigateToBuyerPendingInvoices() {
        try {
            try {
                WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(ALICI_BEKLEYEN_FATURA_MENU));
                menu.click();
                log.info("[Alıcı] Bekleyen fatura listesine XPath ile gidildi.");
                waitForVaadinNavigation();
                return;
            } catch (Exception ignored) {}

            for (String kw : new String[]{"bekleyen", "pending", "fatura", "onay"}) {
                if (clickNavItemByText(kw)) {
                    waitForVaadinNavigation();
                    log.info("[Alıcı] Bekleyen fatura nav JS ile (kw='{}').", kw);
                    return;
                }
            }
            log.warn("[Alıcı] Bekleyen fatura menüsü bulunamadı — mevcut sayfada devam.");
        } catch (Exception e) {
            log.warn("[Alıcı] navigateToBuyerPendingInvoices: {}", e.getMessage());
        }
    }

    /**
     * Listede ilk görünür "ONAYLA" butonuna tıklar (fatura onayı).
     */
    public void clickApproveInvoiceButton() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(ONAYLA_BTN));
            btn.click();
            log.info("[Alıcı] ONAYLA butonuna tıklandı.");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').trim().toUpperCase();" +
                "  if ((t === 'ONAYLA' || t === 'ONAY') && !b.disabled) { b.click(); return true; }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) {
                log.warn("[Alıcı] ONAYLA butonu bulunamadı: {}", e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEDARİKÇİ — VDMK İhale Oluşturma
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * VDMK ihale oluşturma ekranına gider (CompanyVDMKFormView).
     */
    public void navigateToVDMKAuctionForm() {
        try {
            try {
                WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(VDMK_FORM_MENU));
                menu.click();
                log.info("[Tedarikçi] VDMK form menüsüne XPath ile tıklandı.");
                waitForVaadinNavigation();
                return;
            } catch (Exception ignored) {}

            for (String kw : new String[]{"vdmk", "menkul", "cmb", "varlık", "varlik"}) {
                if (clickNavItemByText(kw)) {
                    waitForVaadinNavigation();
                    log.info("[Tedarikçi] VDMK nav JS ile (kw='{}').", kw);
                    return;
                }
            }

            // Fallback: URL-based navigation attempt
            Boolean navigated = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var links = document.querySelectorAll('a[href]');" +
                "for (var l of links) {" +
                "  var h = (l.href || '').toLowerCase();" +
                "  if (h.includes('vdmk') || h.includes('cmb') || h.includes('menkul')) {" +
                "    l.click(); return true;" +
                "  }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(navigated)) waitForVaadinNavigation();
            else log.warn("[Tedarikçi] VDMK ihale formu navigasyonu başarısız.");
        } catch (Exception e) {
            log.warn("[Tedarikçi] navigateToVDMKAuctionForm: {}", e.getMessage());
        }
    }

    /**
     * Grid'de onaylanmış faturayı seçer.
     */
    public void selectApprovedInvoice() {
        try {
            // Önce APPROVED satırındaki seç butonuna tıklamayı dene
            try {
                WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(SELECT_INVOICE_BTN));
                btn.click();
                log.info("[Tedarikçi] Fatura seç butonuna tıklandı.");
                waitForVaadinNavigation();
                return;
            } catch (Exception ignored) {}

            // JS ile: APPROVED içeren satırın ilk butonuna tıkla
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                "for (var cell of cells) {" +
                "  if ((cell.textContent || '').includes('APPROVED')) {" +
                "    var row = cell.closest('tr, [part~=row]');" +
                "    if (row) {" +
                "      var btn = row.querySelector('vaadin-button, button');" +
                "      if (btn && !btn.disabled) { btn.click(); return true; }" +
                "    }" +
                "  }" +
                "}" +
                "// Fallback: ilk aktif satıra tıkla" +
                "var rows = document.querySelectorAll('vaadin-grid-cell-content');" +
                "for (var r of rows) {" +
                "  var b = r.querySelector('vaadin-button:not([disabled]), button:not([disabled])');" +
                "  if (b) { b.click(); return true; }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(clicked)) {
                log.info("[Tedarikçi] APPROVED fatura JS ile seçildi.");
                waitForVaadinNavigation();
            } else {
                log.warn("[Tedarikçi] Onaylanan fatura seçilemedi — grid kontrolü gerekiyor.");
            }
        } catch (Exception e) {
            log.warn("[Tedarikçi] selectApprovedInvoice: {}", e.getMessage());
        }
    }

    /**
     * VDMK formundaki özgül alanları doldurur (VDMK, CMB, Menkul, ihraç anahtarları).
     */
    public void fillVDMKSpecificFields() {
        try {
            // VDMK / Menkul / ihraç alanları
            fillFieldByJs(
                "vdmk, menkul, ihrac",
                "VDMK-TEST-001",
                "VDMK spesifik alan"
            );
            Thread.sleep(300);

            // CMB kodu alanı
            fillFieldByJs(
                "cmb",
                "CMB-001",
                "CMB kodu"
            );

            // Vaadin select komponentleri (varsa ilk seçeneği seç)
            ((JavascriptExecutor) driver).executeScript(
                "var selects = document.querySelectorAll('vaadin-select');" +
                "for (var s of selects) {" +
                "  var lbl = (s.getAttribute('label') || '').toLowerCase();" +
                "  if (lbl.includes('vdmk') || lbl.includes('cmb') || lbl.includes('menkul') " +
                "   || lbl.includes('ihrac') || lbl.includes('tür') || lbl.includes('tip')) {" +
                "    try {" +
                "      var items = s.querySelectorAll('vaadin-list-box vaadin-item');" +
                "      if (items.length > 0) items[0].click();" +
                "    } catch(e) {}" +
                "  }" +
                "}"
            );

            log.info("[Tedarikçi] VDMK özgül alanlar dolduruldu.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[Tedarikçi] fillVDMKSpecificFields: {}", e.getMessage());
        }
    }

    /**
     * VDMK formunu gönderir (Kaydet / Gönder / Yayınla).
     */
    public void submitVDMKForm() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(VDMK_SUBMIT_BTN));
            btn.click();
            log.info("[Tedarikçi] VDMK formu gönderildi.");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "var keywords = ['gönder', 'gonder', 'kaydet', 'submit', 'yayinla', 'yayınla'];" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  for (var kw of keywords) {" +
                "    if (t === kw && !b.disabled) { b.click(); return true; }" +
                "  }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) log.error("[Tedarikçi] VDMK form gönder butonu bulunamadı.");
        }
    }

    /**
     * Tedarikçi aktif ihaleler listesini yeniler (sayfayı reload veya refresh butonuna basar).
     */
    public void refreshVDMKAuctionList() {
        try {
            // Önce "Yenile" / "Refresh" butonu ara
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if (t.includes('yenile') || t.includes('refresh') || t.includes('güncelle')) {" +
                "    b.click(); return true;" +
                "  }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) {
                driver.navigate().refresh();
                log.info("[Tedarikçi] Sayfa yenilendi (F5).");
            } else {
                log.info("[Tedarikçi] Yenile butonuna tıklandı.");
            }
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[Tedarikçi] refreshVDMKAuctionList: {}", e.getMessage());
        }
    }

    /**
     * Tedarikçi aktif ihaleler listesine gider.
     */
    public void navigateToCompanyActiveAuctions() {
        try {
            try {
                WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(AKTIF_IHALELER_MENU));
                menu.click();
                log.info("[Tedarikçi] Aktif ihaleler menüsüne tıklandı.");
                waitForVaadinNavigation();
                return;
            } catch (Exception ignored) {}

            for (String kw : new String[]{"ihale", "auction", "aktif", "teklif"}) {
                if (clickNavItemByText(kw)) {
                    waitForVaadinNavigation();
                    log.info("[Tedarikçi] Aktif ihaleler JS nav (kw='{}').", kw);
                    return;
                }
            }
            log.warn("[Tedarikçi] Aktif ihaleler navigasyonu başarısız.");
        } catch (Exception e) {
            log.warn("[Tedarikçi] navigateToCompanyActiveAuctions: {}", e.getMessage());
        }
    }

    /**
     * Aktif ihaleler listesinde VDMK ihalesi satırına tıklar.
     */
    public void selectVDMKAuctionFromList() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                "for (var cell of cells) {" +
                "  var t = (cell.textContent || '').toUpperCase();" +
                "  if (t.includes('VDMK') || t.includes('CMB') || t.includes('MENKUL')) {" +
                "    var row = cell.closest('tr, [part~=row]');" +
                "    if (row) { row.click(); return true; }" +
                "    cell.click(); return true;" +
                "  }" +
                "}" +
                "// Fallback: CMB_APPROVAL veya CMB_APPROVED satırı ara" +
                "for (var c of cells) {" +
                "  var ct = (c.textContent || '');" +
                "  if (ct.includes('CMB_APPROV') || ct.includes('WAITING')) {" +
                "    var r = c.closest('tr, [part~=row]');" +
                "    if (r) { r.click(); return true; }" +
                "  }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(clicked)) {
                log.info("[Tedarikçi] VDMK ihalesi listeden seçildi.");
                waitForVaadinNavigation();
            } else {
                log.warn("[Tedarikçi] VDMK ihalesi listede bulunamadı.");
            }
        } catch (Exception e) {
            log.warn("[Tedarikçi] selectVDMKAuctionFromList: {}", e.getMessage());
        }
    }

    /**
     * CompanyAuctionOffersView'ı açar (ihale detayından teklif listesi).
     */
    public void openCompanyAuctionOffersView() {
        try {
            // Önce "Teklifler" / "Offers" butonu/linki ara
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button, a'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if (t.includes('teklif') || t.includes('offer') || t.includes('kabul') " +
                "   || t.includes('detay') || t.includes('gör')) {" +
                "    if (!b.disabled) { b.click(); return true; }" +
                "  }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(clicked)) {
                log.info("[Tedarikçi] CompanyAuctionOffersView açıldı.");
                waitForVaadinNavigation();
            } else {
                log.info("[Tedarikçi] CompanyAuctionOffersView — mevcut sayfada teklif listesi bekleniyor.");
            }
        } catch (Exception e) {
            log.warn("[Tedarikçi] openCompanyAuctionOffersView: {}", e.getMessage());
        }
    }

    /**
     * Teklif listesinde ilk teklifi seçer ve "Kabul Et" butonuna tıklar.
     */
    public void selectOfferAndClickAccept() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(KABUL_ET_BTN));
            btn.click();
            log.info("[Tedarikçi] Kabul Et butonuna tıklandı.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if ((t.includes('kabul') || t.includes('accept')) && !b.disabled) {" +
                "    b.click(); return true;" +
                "  }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) log.error("[Tedarikçi] Kabul Et butonu bulunamadı.");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ADMİN — CMB Onay Akışı
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Admin panelinde CMB onay bekleyen ihaleler listesine gider.
     */
    public void navigateToCMBApprovalList() {
        try {
            try {
                WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(CMB_APPROVAL_MENU));
                menu.click();
                log.info("[Admin] CMB onay listesine XPath ile gidildi.");
                waitForVaadinNavigation();
                return;
            } catch (Exception ignored) {}

            for (String kw : new String[]{"cmb", "vdmk", "menkul", "onay bekleyen"}) {
                if (clickNavItemByText(kw)) {
                    waitForVaadinNavigation();
                    log.info("[Admin] CMB onay nav JS (kw='{}').", kw);
                    return;
                }
            }

            // Sayfa kaynağında CMB_APPROVAL içerip içermediğini kontrol et
            String src = driver.getPageSource();
            if (src.contains("CMB_APPROVAL")) {
                log.info("[Admin] CMB_APPROVAL mevcut sayfada görünüyor.");
            } else {
                log.warn("[Admin] CMB onay listesi bulunamadı — mevcut sayfada devam.");
            }
        } catch (Exception e) {
            log.warn("[Admin] navigateToCMBApprovalList: {}", e.getMessage());
        }
    }

    /**
     * CMB_APPROVAL durumundaki VDMK ihalesi için "CMB Onayla" butonuna tıklar.
     */
    public void clickCMBApproveButton() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.elementToBeClickable(CMB_ONAYLA_BTN));
            btn.click();
            log.info("[Admin] CMB Onayla butonuna tıklandı.");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if (t.includes('cmb') && (t.includes('onayla') || t.includes('onay') || t.includes('approve'))) {" +
                "    if (!b.disabled) { b.click(); return true; }" +
                "  }" +
                "}" +
                "// Fallback: genel onayla butonuna tıkla" +
                "for (var b2 of btns) {" +
                "  var t2 = (b2.textContent || '').trim().toUpperCase();" +
                "  if (t2 === 'ONAYLA' && !b2.disabled) { b2.click(); return true; }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) {
                log.error("[Admin] CMB Onayla butonu bulunamadı: {}", e.getMessage());
            }
        }
    }

    /**
     * Açık onay dialogunu onaylar (Evet / Onayla / Tamam).
     */
    public void confirmDialog() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(CONFIRM_BTN));
            btn.click();
            log.info("Onay dialogu onaylandı.");
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // Dialog gelmedi — normal akış
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FİNANSMAN — Teklif Akışı
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Finansman aktif ihaleler listesine gider.
     */
    public void navigateToFactoringActiveAuctions() {
        try {
            try {
                WebElement menu = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(AKTIF_IHALELER_MENU));
                menu.click();
                log.info("[Finansman] Aktif ihaleler menüsüne tıklandı.");
                waitForVaadinNavigation();
                return;
            } catch (Exception ignored) {}

            for (String kw : new String[]{"ihale", "auction", "aktif", "teklif", "bekleyen"}) {
                if (clickNavItemByText(kw)) {
                    waitForVaadinNavigation();
                    log.info("[Finansman] Aktif ihaleler JS nav (kw='{}').", kw);
                    return;
                }
            }
            log.warn("[Finansman] Aktif ihaleler navigasyonu başarısız.");
        } catch (Exception e) {
            log.warn("[Finansman] navigateToFactoringActiveAuctions: {}", e.getMessage());
        }
    }

    /**
     * CMB_APPROVAL durumundaki VDMK ihalesi için teklif butonunun pasif/devre dışı
     * olup olmadığını kontrol eder.
     *
     * @return true eğer teklif butonu disabled veya mevcut değilse
     */
    public boolean isOfferButtonDisabledForCMBApprovalAuction() {
        try {
            String src = driver.getPageSource();
            boolean hasCMBApproval = src.contains("CMB_APPROVAL");
            log.info("[Finansman] CMB_APPROVAL sayfada: {}", hasCMBApproval);

            // Teklif ver butonunu bul — disabled olmalı
            Boolean isDisabled = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if (t.includes('teklif') || t.includes('offer') || t.includes('bid') " +
                "   || t.includes('gözat') || t.includes('gozat')) {" +
                "    var isDisabled = b.disabled || b.getAttribute('aria-disabled') === 'true'" +
                "      || b.getAttribute('disabled') !== null;" +
                "    return isDisabled;" +
                "  }" +
                "}" +
                "// Teklif butonu hiç yoksa — CMB_APPROVAL engellemesi geçerli" +
                "return true;"
            );
            return Boolean.TRUE.equals(isDisabled);
        } catch (Exception e) {
            log.warn("[Finansman] isOfferButtonDisabledForCMBApprovalAuction: {}", e.getMessage());
            return true; // Soft-pass
        }
    }

    /**
     * Aktif ihaleler listesinde VDMK ihalesi satırının görünür olup olmadığını kontrol eder.
     */
    public boolean isVDMKAuctionVisibleInActiveList() {
        try {
            String src = driver.getPageSource();
            return src.contains("VDMK") || src.contains("CMB") || src.contains("MENKUL")
                    || src.contains("WAITING");
        } catch (Exception e) {
            log.warn("[Finansman] isVDMKAuctionVisibleInActiveList: {}", e.getMessage());
            return false;
        }
    }

    /**
     * VDMK ihalesi detayını açar (ilk VDMK/CMB satırındaki butona tıklar).
     */
    public void openVDMKAuctionDetail() {
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var cells = document.querySelectorAll('vaadin-grid-cell-content');" +
                "for (var cell of cells) {" +
                "  var t = (cell.textContent || '').toUpperCase();" +
                "  if (t.includes('WAITING') || t.includes('VDMK') || t.includes('CMB')) {" +
                "    var row = cell.closest('tr, [part~=row]');" +
                "    if (row) {" +
                "      var btn = row.querySelector('vaadin-button:not([disabled]), button:not([disabled])');" +
                "      if (btn) { btn.click(); return true; }" +
                "      row.click(); return true;" +
                "    }" +
                "  }" +
                "}" +
                "// Fallback: ilk ihale detay butonuna tıkla" +
                "var btns = document.querySelectorAll('vaadin-button, button');" +
                "for (var b of btns) {" +
                "  var t2 = (b.textContent||'').toLowerCase();" +
                "  if ((t2.includes('detay') || t2.includes('gör') || t2.includes('incele')) && !b.disabled) {" +
                "    b.click(); return true;" +
                "  }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(clicked)) {
                log.info("[Finansman] VDMK ihalesi detayı açıldı.");
                waitForVaadinNavigation();
            } else {
                log.warn("[Finansman] VDMK ihalesi detayı açılamadı.");
            }
        } catch (Exception e) {
            log.warn("[Finansman] openVDMKAuctionDetail: {}", e.getMessage());
        }
    }

    /**
     * Teklif formunu açar ve teklif miktarını girer.
     */
    public void openOfferFormAndFillAmount() {
        try {
            // "Teklif Ver" / "GÖZAT" butonuna tıkla
            Boolean opened = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  if ((t.includes('teklif') || t.includes('gözat') || t.includes('gozat') " +
                "    || t.includes('bid') || t.includes('offer')) && !b.disabled) {" +
                "    b.click(); return true;" +
                "  }" +
                "}" +
                "return false;"
            );
            if (Boolean.TRUE.equals(opened)) {
                log.info("[Finansman] Teklif Ver butonuna tıklandı.");
                waitForVaadinNavigation();
            }

            Thread.sleep(1000);

            // Teklif miktarı alanını doldur
            try {
                WebElement field = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(TEKLIF_TUTARI_INPUT));
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                    field);
                field.sendKeys("1000000");
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", field);
                log.info("[Finansman] Teklif miktarı girildi: 1000000");
            } catch (Exception ex) {
                log.warn("[Finansman] Teklif miktarı girilemedi: {}", ex.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[Finansman] openOfferFormAndFillAmount: {}", e.getMessage());
        }
    }

    /**
     * Teklifi kaydeder (Kaydet / Teklif Ver / Gönder).
     */
    public void saveOffer() {
        try {
            WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
            btn.click();
            log.info("[Finansman] Teklif kaydedildi.");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                "var kws = ['kaydet', 'teklif ver', 'gönder', 'submit', 'kaydet'];" +
                "for (var b of btns) {" +
                "  var t = (b.textContent || '').toLowerCase().trim();" +
                "  for (var kw of kws) { if (t === kw && !b.disabled) { b.click(); return true; } }" +
                "}" +
                "return false;"
            );
            if (!Boolean.TRUE.equals(clicked)) log.error("[Finansman] Kaydet butonu bulunamadı.");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ORTAK — Durum Doğrulama
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Grid'den VDMK ihalesi durum metnini döner.
     */
    public String getVDMKAuctionStatus() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(GRID));
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            String[] statuses = {
                "CMB_APPROVAL", "CMB_APPROVED", "WAITING", "PENDINGBUYER",
                "ACCEPTED", "REJECTED", "DRAFT"
            };
            for (WebElement cell : cells) {
                String text = cell.getText();
                for (String s : statuses) {
                    if (text != null && text.contains(s)) return s;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Sayfa kaynağından teklif OfferState'ini döner.
     *
     * @param expectedState Beklenen OfferState (örn. "WONAUCTION")
     */
    public boolean isOfferStatePresent(String expectedState) {
        String src = driver.getPageSource();
        return src.contains(expectedState);
    }

    /**
     * Admin listesinde VDMK ihalesi belirtilen status ile görünüyor mu?
     */
    public boolean isVDMKAuctionInAdminListWithStatus(String status) {
        String src = driver.getPageSource();
        return src.contains(status) && (src.contains("VDMK") || src.contains("CMB") || src.contains("MENKUL"));
    }

    /**
     * Başarı bildiriminin görünür olup olmadığını kontrol eder.
     */
    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIF, 6).isDisplayed();
        } catch (Exception e) {
            String src = driver.getPageSource();
            return src.contains("başarı") || src.contains("basari") || src.contains("success");
        }
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    /**
     * Etiket anahtar kelimelerine göre JS ile metin alanını bulup değer girer.
     *
     * @param labelKeywords Virgülle ayrılmış etiket anahtar kelimeleri
     * @param value         Girilecek değer
     * @param fieldName     Log için alan adı
     */
    private void fillFieldByJs(String labelKeywords, String value, String fieldName) {
        try {
            String[] keys = labelKeywords.split(",\\s*");
            StringBuilder condition = new StringBuilder("false");
            for (String k : keys) {
                condition.append(" || lbl.includes('").append(k.trim().toLowerCase()).append("')");
            }
            String script =
                "var inputs = document.querySelectorAll('vaadin-text-field input, input[type=\"text\"], input[type=\"number\"]');" +
                "for (var inp of inputs) {" +
                "  var parent = inp.closest('vaadin-text-field, vaadin-number-field');" +
                "  var lbl = parent ? (parent.getAttribute('label') || '').toLowerCase() : '';" +
                "  if (" + condition + ") {" +
                "    inp.value = '';" +
                "    inp.dispatchEvent(new Event('input',{bubbles:true}));" +
                "    inp.value = arguments[0];" +
                "    inp.dispatchEvent(new Event('change',{bubbles:true}));" +
                "    return true;" +
                "  }" +
                "}" +
                "return false;";
            Boolean done = (Boolean) ((JavascriptExecutor) driver).executeScript(script, value);
            if (Boolean.TRUE.equals(done)) {
                log.info("{} girildi: {}", fieldName, value);
            } else {
                log.warn("{} JS ile doldurulamadı.", fieldName);
            }
        } catch (Exception e) {
            log.warn("{} fillFieldByJs hatası: {}", fieldName, e.getMessage());
        }
    }
}
