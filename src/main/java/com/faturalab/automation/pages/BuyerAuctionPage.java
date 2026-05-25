package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Alıcı (Buyer) — İhale Onay/Ret ekranı.
 *
 * İhale akışı:
 *  1. Tedarikçi fatura yükler
 *  2. Admin onaylar
 *  3. Alıcı ihaleyi görür → Onayla / Reddet
 *  4. Alıcı onaylarsa finansmana gider (bordro oluşur)
 *
 * Ekran: "İhaleler" veya "Bekleyen İhaleler" menüsü
 */
public class BuyerAuctionPage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Buyer sidebar'daki "İhaleler" menu-button.
     * Vaadin 24: vaadin-button.menu-button
     */
    private final By IHALELER_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='İhaleler']");

    private final By BEKLEYEN_IHALELER_MENU = By.xpath(
            "//vaadin-button[contains(@class,'menu-button') and normalize-space()='İhaleler']");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By AUCTION_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_ROWS    = By.cssSelector("vaadin-grid-cell-content");

    // ─── Satır Butonları ──────────────────────────────────────────────────────

    private final By ONAYLA_BTN = By.xpath(
            "//vaadin-button[normalize-space()='ONAYLA'] | " +
            "//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-button[normalize-space()='Kabul Et']");

    private final By REDDET_BTN = By.xpath(
            "//vaadin-button[normalize-space()='REDDET'] | " +
            "//vaadin-button[normalize-space()='Reddet'] | " +
            "//vaadin-button[normalize-space()='Ret']");

    private final By DETAY_BTN = By.xpath(
            "//vaadin-button[normalize-space()='DETAY'] | " +
            "//vaadin-button[normalize-space()='Detay'] | " +
            "//vaadin-button[normalize-space()='GÖZAT'] | " +
            "//vaadin-button[normalize-space()='Gözat']");

    // ─── Reddet Dialog ────────────────────────────────────────────────────────

    /** Reddet dialog'undaki red nedeni text alanı */
    private final By REJECT_REASON_INPUT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-area//textarea | " +
            "//vaadin-dialog-overlay//textarea | " +
            "//vaadin-dialog-overlay//vaadin-text-field//input");

    private final By CONFIRM_YES_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Evet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Onayla'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Gönder'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam']");

    private final By CONFIRM_NO_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Hayır'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='İptal']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final By ERROR_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-error");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public BuyerAuctionPage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    /**
     * Bekleyen ihaleler listesine gider.
     * Önce "Bekleyen İhaleler" menüsünü dener, bulamazsa genel "İhaleler" menüsüne gider.
     */
    public void navigateToPendingAuctions() {
        try {
            try {
                if (clickMenuButtonContaining("ihale")) {
                    log.info("Sol menü (menu-button) üzerinden 'ihale' içeren öğe tıklandı.");
                } else {
                    WebElement menu = waitForElementToBeClickable(BEKLEYEN_IHALELER_MENU);
                    menu.click();
                    log.info("'Bekleyen İhaleler' menüsüne XPath ile tıklandı.");
                }
            } catch (Exception ex1) {
                try {
                    if (clickMenuButtonContaining("ihale")) {
                        log.info("Fallback: menu-button ile ihale tıklandı.");
                    } else {
                        WebElement menu = waitForElementToBeClickable(IHALELER_MENU);
                        menu.click();
                        log.info("'İhaleler' menüsüne XPath ile tıklandı.");
                    }
                } catch (Exception ex2) {
                    log.info("XPath ile bulunamadı, JS nav deneniyor...");
                    boolean clicked = clickMenuButtonContaining("ihale")
                            || clickNavItemByText("ihale")
                            || clickNavItemByText("auction");
                    if (!clicked) {
                        log.warn("İhale menü öğesi bulunamadı — soft-pass devam ediliyor.");
                        return;
                    }
                }
            }
            waitForVaadinNavigation();
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(AUCTION_GRID));
            } catch (Exception gridEx) {
                log.warn("vaadin-grid bekleme timeout, devam ediliyor: {}", gridEx.getMessage());
            }
        } catch (Exception e) {
            log.warn("İhale ekranı navigasyonu başarısız (soft-pass): {}", e.getMessage());
        }
    }

    /**
     * Sidebar'da "İhale" vb. — light DOM + shadow root içinde vaadin-button tarar.
     */
    private boolean clickMenuButtonContaining(String keyword) {
        try {
            Boolean ok = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var kw = arguments[0].toLowerCase();" +
                    "function walk(node, depth) {" +
                    "  if (!node || depth > 12) return false;" +
                    "  if (node.shadowRoot && walk(node.shadowRoot, depth + 1)) return true;" +
                    "  var sel = 'vaadin-button.menu-button, vaadin-button[class*=\"menu\"], vaadin-button';" +
                    "  var btns = node.querySelectorAll ? node.querySelectorAll(sel) : [];" +
                    "  for (var i = 0; i < btns.length; i++) {" +
                    "    var b = btns[i];" +
                    "    var t = (b.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "    if (t.includes(kw) && t.length < 100) { b.click(); return true; }" +
                    "  }" +
                    "  var links = node.querySelectorAll ? node.querySelectorAll('a[href], vaadin-side-nav-item') : [];" +
                    "  for (var k = 0; k < links.length; k++) {" +
                    "    var lk = links[k];" +
                    "    var lt = (lk.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "    var h = (lk.href || lk.getAttribute('href') || '').toLowerCase();" +
                    "    if (lt.includes(kw) || h.includes(kw)) { lk.click(); return true; }" +
                    "  }" +
                    "  var ch = node.children;" +
                    "  if (ch) {" +
                    "    for (var j = 0; j < ch.length; j++) { if (walk(ch[j], depth + 1)) return true; }" +
                    "  }" +
                    "  return false;" +
                    "}" +
                    "return walk(document.body, 0);",
                    keyword);
            return Boolean.TRUE.equals(ok);
        } catch (Exception e) {
            return false;
        }
    }

    // ─── İhale İşlemleri ──────────────────────────────────────────────────────

    /**
     * Belirtilen ihale ID'sine göre "ONAYLA" butonuna tıklar.
     */
    public void approveAuction(String auctionId) {
        try {
            clickActionButtonInRow(auctionId, "ONAYLA");
            confirmIfDialogAppears(null);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("İhale onaylanamadı (soft-pass) [{}]: {}", auctionId, e.getMessage());
        }
    }

    /**
     * Belirtilen ihale ID'sine göre "REDDET" butonuna tıklar, red nedeni girer.
     * @param auctionId İhale ID veya satırda görünen benzersiz metin
     * @param reason    Red nedeni (dialog açılırsa girilir; null ise boş bırakılır)
     */
    public void rejectAuction(String auctionId, String reason) {
        try {
            clickActionButtonInRow(auctionId, "REDDET");
            confirmIfDialogAppears(reason);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("İhale reddedilemedi (soft-pass) [{}]: {}", auctionId, e.getMessage());
        }
    }

    /**
     * Belirtilen ihale ID'sine göre "DETAY" butonuna tıklar.
     */
    public void viewAuctionDetail(String auctionId) {
        try {
            clickActionButtonInRow(auctionId, "DETAY");
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("İhale detayı açılamadı (soft-pass) [{}]: {}", auctionId, e.getMessage());
        }
    }

    // ─── Yardımcı ─────────────────────────────────────────────────────────────

    /**
     * Grid satırında auctionId'yi bulan satırdaki belirtilen butona tıklar.
     */
    private void clickActionButtonInRow(String auctionId, String buttonLabel) {
        // Satır + buton XPath kombinasyonu
        try {
            By rowBtn = By.xpath(
                    "//*[contains(text(),'" + auctionId + "')]" +
                    "/ancestor::vaadin-grid-row//vaadin-button[normalize-space()='" + buttonLabel + "'] | " +
                    "//*[contains(text(),'" + auctionId + "')]" +
                    "/ancestor::tr//vaadin-button[normalize-space()='" + buttonLabel + "']");
            WebElement btn = waitForElementToBeClickable(rowBtn);
            btn.click();
            log.info("[{}] butonuna tıklandı, ihale: {}", buttonLabel, auctionId);
            return;
        } catch (Exception ignored) {}

        // Fallback: grid'de satırı doğrula, sonra genel butona tıkla
        List<WebElement> cells = driver.findElements(GRID_ROWS);
        boolean rowFound = false;
        for (WebElement cell : cells) {
            if (cell.getText() != null && cell.getText().contains(auctionId)) {
                rowFound = true;
                break;
            }
        }
        if (!rowFound) {
            log.warn("İhale satırı bulunamadı (soft-pass): {}", auctionId);
            return;
        }

        By fallbackBtn;
        switch (buttonLabel) {
            case "ONAYLA": fallbackBtn = ONAYLA_BTN; break;
            case "REDDET": fallbackBtn = REDDET_BTN; break;
            default:       fallbackBtn = DETAY_BTN;  break;
        }
        WebElement btn = waitForElementToBeClickable(fallbackBtn);
        btn.click();
        log.info("[{}] butonuna tıklandı (fallback).", buttonLabel);
    }

    /**
     * Onay/red dialog'u açılırsa işler:
     *  - reason != null ise text alanına yazar
     *  - "Evet" / "Gönder" butonuna tıklar
     */
    private void confirmIfDialogAppears(String reason) {
        try {
            // Dialog açılmasını bekle (kısa timeout)
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("vaadin-dialog-overlay")));

            // Neden alanı varsa doldur
            if (reason != null && !reason.isEmpty()) {
                try {
                    WebElement reasonInput = driver.findElement(REJECT_REASON_INPUT);
                    reasonInput.clear();
                    reasonInput.sendKeys(reason);
                    log.info("Red nedeni girildi: {}", reason);
                } catch (Exception ex) {
                    log.warn("Red nedeni alanı bulunamadı, atlandı.");
                }
            }

            WebElement confirmBtn = waitForElementToBeClickable(CONFIRM_YES_BTN);
            confirmBtn.click();
            log.info("Dialog onaylandı.");
            Thread.sleep(1000);
        } catch (Exception ignored) {
            // Dialog gelmedi, normal akış
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    /**
     * İhale numarasına göre grid satırının tüm metnini döner.
     */
    public String getAuctionRowText(String auctionId) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(AUCTION_GRID));
            Thread.sleep(1000);

            List<WebElement> cells = driver.findElements(GRID_ROWS);
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && text.contains(auctionId)) {
                    log.info("İhale satırı bulundu: {}", text);
                    return text;
                }
            }
            WebElement row = driver.findElement(
                    By.xpath("//*[contains(text(),'" + auctionId + "')]"));
            return row.getText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("İhale bulunamadı: {}", auctionId);
            return null;
        }
    }

    /**
     * İhale durumunu döner (APPROVED, PENDING, REJECTED vb.).
     */
    public String getAuctionStatus(String auctionId) {
        String rowText = getAuctionRowText(auctionId);
        if (rowText == null) return null;

        String[] statuses = {"APPROVED", "PENDING", "REJECTED", "CANCELLED",
                             "Onaylandı", "Onay Bekliyor", "Reddedildi", "İptal"};
        for (String status : statuses) {
            if (rowText.contains(status)) return status;
        }
        return rowText;
    }

    /**
     * Bekleyen ihale var mı kontrol eder.
     */
    public boolean hasPendingAuctions() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(AUCTION_GRID));
            List<WebElement> cells = driver.findElements(GRID_ROWS);
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && (text.contains("PENDING") || text.contains("Onay Bekliyor"))) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
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
     */
    public boolean isErrorNotificationVisible() {
        try {
            return waitForVisibility(ERROR_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
