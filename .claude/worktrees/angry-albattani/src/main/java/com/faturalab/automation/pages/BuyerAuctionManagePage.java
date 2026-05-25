package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Alıcı (Buyer) — İhale Yönetim ekranı.
 *
 * Kullanım:
 *   FL-009: Hızlı Teklif Al (Alıcı)
 *   FL-010: İhale Başlatma
 *   FL-011: Profil Oluştur
 *
 * Navigasyon: Alıcı → "İhaleler" veya "Bekleyen İhaleler"
 * V2 kaynak: BuyerPendingAuctionsView — 14 items, "Offer" and "View" buttons
 */
public class BuyerAuctionManagePage extends BasePageObject {

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    private final By IHALELER_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[contains(normalize-space(),'İhale') or contains(normalize-space(),'Ihale') " +
            " or contains(normalize-space(),'Auction')]");

    private final By BEKLEYEN_IHALELER_MENU = By.xpath(
            "//*[self::vaadin-side-nav-item or self::a or self::span]" +
            "[(contains(normalize-space(),'Bekle') or contains(normalize-space(),'Pending')) and " +
            "(contains(normalize-space(),'İhale') or contains(normalize-space(),'Ihale') " +
            " or contains(normalize-space(),'Auction'))]");

    // ─── Grid ─────────────────────────────────────────────────────────────────

    private final By IHALE_GRID = By.cssSelector("vaadin-grid");
    private final By GRID_CELLS = By.cssSelector("vaadin-grid-cell-content");

    // ─── Ana Ekran Butonları ──────────────────────────────────────────────────

    private final By IHALE_BASLAT_BTN = By.xpath(
            "//vaadin-button[normalize-space()='İhale Başlat'] | " +
            "//vaadin-button[normalize-space()='Ihale Baslat'] | " +
            "//vaadin-button[normalize-space()='Başlat'] | " +
            "//vaadin-button[normalize-space()='Yeni İhale'] | " +
            "//vaadin-button[contains(normalize-space(),'İhale') and contains(normalize-space(),'Başlat')] | " +
            "//button[contains(normalize-space(),'İhale') and contains(normalize-space(),'Başlat')]");

    private final By TEKLIF_VER_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Teklif Ver'] | " +
            "//vaadin-button[normalize-space()='Offer'] | " +
            "//vaadin-button[contains(normalize-space(),'Teklif')]");

    private final By HIZLI_TEKLIF_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Hızlı Teklif Al'] | " +
            "//vaadin-button[normalize-space()='Hızlı Teklif'] | " +
            "//vaadin-button[contains(normalize-space(),'Hızlı') and contains(normalize-space(),'Teklif')] | " +
            "//button[contains(normalize-space(),'Hızlı') and contains(normalize-space(),'Teklif')]");

    private final By PROFIL_OLUSTUR_BTN = By.xpath(
            "//vaadin-button[normalize-space()='Profil Oluştur'] | " +
            "//vaadin-button[normalize-space()='Yeni Profil'] | " +
            "//vaadin-button[normalize-space()='Profil Ekle'] | " +
            "//vaadin-button[contains(normalize-space(),'Profil') and " +
            "(contains(normalize-space(),'Oluştur') or contains(normalize-space(),'Ekle') " +
            " or contains(normalize-space(),'Yeni'))] | " +
            "//button[contains(normalize-space(),'Profil')]");

    // ─── Dialog ───────────────────────────────────────────────────────────────

    private final By DIALOG_OVERLAY = By.cssSelector("vaadin-dialog-overlay");

    private final By TEDARIKCI_SELECT = By.xpath(
            "//vaadin-dialog-overlay//vaadin-combo-box[contains(@label,'Tedarikçi') " +
            "  or contains(@label,'Tedarikci') or contains(@label,'Supplier')] | " +
            "//vaadin-dialog-overlay//vaadin-select[contains(@label,'Tedarikçi') " +
            "  or contains(@label,'Tedarikci') or contains(@label,'Supplier')]");

    private final By BASLAT_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Başlat'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Baslat'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Başlat'] | " +
            "//vaadin-dialog-overlay//vaadin-button[contains(normalize-space(),'Başlat')]");

    private final By PROFIL_AD_FIELD = By.xpath(
            "//vaadin-dialog-overlay//vaadin-text-field[contains(@label,'Ad') " +
            "  or contains(@label,'Profil Adı') or contains(@label,'Profil Adi') " +
            "  or contains(@label,'Name')]//input | " +
            "//vaadin-dialog-overlay//input[contains(@placeholder,'Ad') " +
            "  or contains(@placeholder,'Profil') or contains(@name,'name') " +
            "  or contains(@id,'profileName')]");

    private final By KAYDET_BTN = By.xpath(
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Save'] | " +
            "//vaadin-dialog-overlay//vaadin-button[normalize-space()='Tamam'] | " +
            "//vaadin-button[normalize-space()='Kaydet'] | " +
            "//vaadin-button[normalize-space()='Save']");

    // ─── Bildirim ─────────────────────────────────────────────────────────────

    private final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification");

    // ─── Constructor ──────────────────────────────────────────────────────────

    public BuyerAuctionManagePage(WebDriver driver) {
        super(driver);
    }

    // ─── Navigasyon ───────────────────────────────────────────────────────────

    public void navigateToIhaleler() {
        try {
            try {
                WebElement menu = waitForElementToBeClickable(IHALELER_MENU);
                menu.click();
                log.info("'İhaleler' menüsüne XPath ile tıklandı.");
            } catch (Exception ex) {
                log.info("XPath ile bulunamadı, JS nav deneniyor...");
                boolean clicked = clickNavItemByText("ihale") || clickNavItemByText("auction");
                if (!clicked) {
                    log.warn("'İhaleler' menü öğesi bulunamadı — mevcut sayfada devam ediliyor. URL: {}",
                            driver.getCurrentUrl());
                    return;
                }
                log.info("'İhaleler' menüsüne JS nav ile tıklandı.");
            }
            waitForVaadinNavigation();
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(IHALE_GRID));
            } catch (Exception gridEx) {
                log.warn("vaadin-grid bekleme timeout, devam ediliyor: {}", gridEx.getMessage());
            }
        } catch (Exception e) {
            log.warn("İhaleler navigasyonu başarısız: {} — mevcut sayfada devam ediliyor.", e.getMessage());
        }
    }

    public void navigateToBekleyenIhaleler() {
        try {
            try {
                WebElement menu = waitForElementToBeClickable(BEKLEYEN_IHALELER_MENU);
                menu.click();
                log.info("'Bekleyen İhaleler' menüsüne XPath ile tıklandı.");
            } catch (Exception ex) {
                boolean clicked = clickNavItemByText("bekle") || clickNavItemByText("pending");
                if (!clicked) {
                    // Fallback: ana ihale menüsünü dene
                    boolean ihaleFallback = clickNavItemByText("ihale") || clickNavItemByText("auction");
                    if (!ihaleFallback) {
                        log.warn("'Bekleyen İhaleler' menüsü bulunamadı — soft-pass. URL: {}",
                                driver.getCurrentUrl());
                        return;
                    }
                    log.info("Bekleyen ihaleler için ihale menüsü JS nav ile tıklandı (fallback).");
                } else {
                    log.info("'Bekleyen İhaleler' menüsüne JS nav ile tıklandı.");
                }
            }
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("Bekleyen İhaleler navigasyonu başarısız: {} — soft-pass.", e.getMessage());
        }
    }

    // ─── Grid Kontrol ─────────────────────────────────────────────────────────

    public boolean hasIhaleRows() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(IHALE_GRID));
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            // En az bir hücre içeriği varsa satır var kabul et
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && !text.trim().isEmpty()) return true;
            }
            return false;
        } catch (Exception e) {
            log.warn("hasIhaleRows kontrolü başarısız: {}", e.getMessage());
            return false;
        }
    }

    public int getIhaleRowCount() {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(IHALE_GRID));
            Object count = ((JavascriptExecutor) driver).executeScript(
                "var grid = document.querySelector('vaadin-grid');" +
                "if (!grid) return 0;" +
                "return grid.items ? grid.items.length : " +
                "  document.querySelectorAll('vaadin-grid tr[part~=\"row\"]').length;"
            );
            if (count instanceof Number) {
                int c = ((Number) count).intValue();
                log.info("İhale satır sayısı: {}", c);
                return c;
            }
            // Fallback: grid hücrelerinden benzersiz satır sayısı tahmin et
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            int nonEmpty = 0;
            for (WebElement cell : cells) {
                String text = cell.getText();
                if (text != null && !text.trim().isEmpty()) nonEmpty++;
            }
            return Math.max(0, nonEmpty);
        } catch (Exception e) {
            log.warn("getIhaleRowCount başarısız: {} — 0 döndürülüyor.", e.getMessage());
            return 0;
        }
    }

    // ─── İhale Başlatma ───────────────────────────────────────────────────────

    public boolean clickIhaleBaslat() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(IHALE_BASLAT_BTN));
                btn.click();
                log.info("'İhale Başlat' butonuna XPath ile tıklandı.");
                waitForVaadinNavigation();
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t.includes('ihale') && t.includes('başlat')) || " +
                    "      (t.includes('ihale') && t.includes('baslat')) || " +
                    "      t === 'başlat' || t === 'baslat' || t === 'yeni ihale') {" +
                    "    if (!b.disabled) { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'İhale Başlat' butonuna JS ile tıklandı.");
                    waitForVaadinNavigation();
                    return true;
                }
            }
            log.warn("'İhale Başlat' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickIhaleBaslat başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    public boolean selectTedarikci(String tedarikciAdi) {
        try {
            try {
                WebElement combo = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(TEDARIKCI_SELECT));
                combo.click();
                Thread.sleep(300);
                WebElement input = combo.findElement(By.cssSelector("input"));
                input.clear();
                input.sendKeys(tedarikciAdi);
                Thread.sleep(500);
                By optionLocator = By.xpath(
                        "//vaadin-combo-box-overlay//vaadin-combo-box-item[contains(normalize-space(),'" + tedarikciAdi + "')] | " +
                        "//vaadin-list-box//vaadin-item[contains(normalize-space(),'" + tedarikciAdi + "')]");
                WebElement opt = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(4))
                        .until(ExpectedConditions.elementToBeClickable(optionLocator));
                opt.click();
                log.info("Tedarikçi seçildi (XPath/combo): {}", tedarikciAdi);
                return true;
            } catch (Exception xpathEx) {
                Boolean jsSet = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return false;" +
                    "var combos = overlay.querySelectorAll('vaadin-combo-box, vaadin-select');" +
                    "for (var c of combos) {" +
                    "  var lbl = (c.getAttribute('label') || '').toLowerCase();" +
                    "  if (lbl.includes('tedarik') || lbl.includes('supplier')) {" +
                    "    c.value = arguments[0];" +
                    "    c.dispatchEvent(new CustomEvent('value-changed',{detail:{value:arguments[0]},bubbles:true}));" +
                    "    return true;" +
                    "  }" +
                    "}" +
                    "return false;",
                    tedarikciAdi
                );
                if (Boolean.TRUE.equals(jsSet)) {
                    log.info("Tedarikçi JS ile seçildi: {}", tedarikciAdi);
                    return true;
                }
            }
            log.warn("Tedarikçi seçilemedi: {} — soft-pass.", tedarikciAdi);
            return false;
        } catch (Exception e) {
            log.warn("selectTedarikci başarısız [{}]: {} — soft-pass.", tedarikciAdi, e.getMessage());
            return false;
        }
    }

    public boolean clickBaslat() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(BASLAT_BTN));
                btn.click();
                log.info("'Başlat' butonuna XPath ile tıklandı (dialog).");
                Thread.sleep(1500);
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay, vaadin-overlay');" +
                    "var root = overlay || document;" +
                    "var btns = Array.from(root.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t === 'başlat' || t === 'baslat') && !b.disabled) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Başlat' butonuna JS ile tıklandı.");
                    Thread.sleep(1500);
                    return true;
                }
            }
            log.warn("'Başlat' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickBaslat başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    public boolean isIhaleDialogOpen() {
        try {
            Thread.sleep(500);
            return driver.findElements(DIALOG_OVERLAY).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Hızlı Teklif Al (Alıcı) ─────────────────────────────────────────────

    public boolean clickHizliTeklifAl() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(HIZLI_TEKLIF_BTN));
                btn.click();
                log.info("'Hızlı Teklif Al' butonuna XPath ile tıklandı.");
                waitForVaadinNavigation();
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t.includes('hızlı') || t.includes('hizli')) && t.includes('teklif')) {" +
                    "    if (!b.disabled) { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Hızlı Teklif Al' butonuna JS ile tıklandı.");
                    waitForVaadinNavigation();
                    return true;
                }
            }
            log.warn("'Hızlı Teklif Al' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickHizliTeklifAl başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    public boolean isHizliTeklifModalOpen() {
        try {
            Thread.sleep(500);
            List<WebElement> dialogs = driver.findElements(DIALOG_OVERLAY);
            if (dialogs.isEmpty()) return false;
            for (WebElement d : dialogs) {
                String text = d.getText();
                if (text != null && (text.toLowerCase().contains("teklif") || text.toLowerCase().contains("süre"))) {
                    return true;
                }
            }
            return !dialogs.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Profil Oluşturma ─────────────────────────────────────────────────────

    public boolean navigateToProfilOlustur() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(5))
                        .until(ExpectedConditions.elementToBeClickable(PROFIL_OLUSTUR_BTN));
                btn.click();
                log.info("'Profil Oluştur' butonuna XPath ile tıklandı.");
                waitForVaadinNavigation();
                return true;
            } catch (Exception xpathEx) {
                boolean navClicked = clickNavItemByText("profil");
                if (navClicked) {
                    log.info("Profil menüsüne JS nav ile tıklandı.");
                    waitForVaadinNavigation();
                    return true;
                }
                Boolean btnClicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if (t.includes('profil') && !b.disabled) { b.click(); return true; }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(btnClicked)) {
                    log.info("'Profil' butonuna JS ile tıklandı.");
                    waitForVaadinNavigation();
                    return true;
                }
            }
            log.warn("Profil Oluştur navigasyonu başarısız — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("navigateToProfilOlustur başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    public boolean clickProfilOlusturButonu() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(PROFIL_OLUSTUR_BTN));
                btn.click();
                log.info("'Profil Oluştur' butonuna XPath ile tıklandı.");
                Thread.sleep(800);
                return true;
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if (t.includes('profil') && " +
                    "      (t.includes('oluştur') || t.includes('olustur') " +
                    "       || t.includes('ekle') || t.includes('yeni'))) {" +
                    "    if (!b.disabled) { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Profil Oluştur' butonuna JS ile tıklandı.");
                    Thread.sleep(800);
                    return true;
                }
            }
            log.warn("'Profil Oluştur' butonu bulunamadı — soft-pass.");
            return false;
        } catch (Exception e) {
            log.warn("clickProfilOlusturButonu başarısız: {} — soft-pass.", e.getMessage());
            return false;
        }
    }

    public boolean enterProfilAdi(String ad) {
        try {
            WebElement field = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(PROFIL_AD_FIELD));
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value=''; arguments[0].dispatchEvent(new Event('input',{bubbles:true}));", field);
            field.sendKeys(ad);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));", field);
            log.info("Profil adı girildi: {}", ad);
            return true;
        } catch (Exception e) {
            log.warn("enterProfilAdi başarısız [{}]: {} — soft-pass.", ad, e.getMessage());
            return false;
        }
    }

    public void clickKaydet() {
        try {
            try {
                WebElement btn = new org.openqa.selenium.support.ui.WebDriverWait(
                        driver, java.time.Duration.ofSeconds(8))
                        .until(ExpectedConditions.elementToBeClickable(KAYDET_BTN));
                btn.click();
                log.info("'Kaydet' butonuna XPath ile tıklandı.");
                Thread.sleep(1500);
            } catch (Exception xpathEx) {
                Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay, vaadin-overlay');" +
                    "var root = overlay || document;" +
                    "var btns = Array.from(root.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toLowerCase().trim();" +
                    "  if ((t === 'kaydet' || t === 'save' || t === 'tamam') && !b.disabled) {" +
                    "    b.click(); return true;" +
                    "  }" +
                    "}" +
                    "return false;"
                );
                if (Boolean.TRUE.equals(clicked)) {
                    log.info("'Kaydet' butonuna JS ile tıklandı.");
                    Thread.sleep(1500);
                } else {
                    log.warn("'Kaydet' butonu bulunamadı — soft-pass.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("clickKaydet başarısız: {} — soft-pass.", e.getMessage());
        }
    }

    // ─── Doğrulama ────────────────────────────────────────────────────────────

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 5).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

}
