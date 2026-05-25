package com.faturalab.automation.pages;

import com.faturalab.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Alıcı — İhale listesi, hızlı teklif, profil (UAT FL-009–011).
 */
public class BuyerAuctionManagePage extends BasePageObject {

    private static final By GRID = By.cssSelector("vaadin-grid");
    private static final By GRID_CELLS = By.cssSelector("vaadin-grid-cell-content");
    private static final By SUCCESS_NOTIFICATION = By.cssSelector(
            "vaadin-notification-container, .v-Notification.notification-success");

    private final BuyerAuctionPage buyerAuctionPage;

    public BuyerAuctionManagePage(WebDriver driver) {
        super(driver);
        this.buyerAuctionPage = new BuyerAuctionPage(driver);
    }

    public void navigateToIhaleler() {
        buyerAuctionPage.navigateToPendingAuctions();
    }

    /**
     * Alıcı girişi sonrası ana URL (Vaadin dashboard). Hızlı Teklif Al bazen ihale listesinde değil ana sayfa altında.
     */
    public void navigateToBuyerHome() {
        try {
            String base = ConfigReader.getProperty("base.url");
            if (base != null && !base.isBlank()) {
                driver.get(base.replaceAll("/+$", "") + "/");
            }
            waitForVaadinNavigation();
            Thread.sleep(600);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Alıcı ana sayfa: {}", e.getMessage());
        }
    }

    /** Ana sayfa / dashboard’da sağ alttaki aksiyon için sayfayı aşağı kaydırır. */
    public void scrollBuyerDashboardToBottom() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "window.scrollTo(0, document.body.scrollHeight);" +
                    "var main = document.querySelector('main, [main], #main');" +
                    "if (main) main.scrollTop = main.scrollHeight;");
            Thread.sleep(450);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Scroll: {}", e.getMessage());
        }
    }

    /**
     * Ana sayfadaki (grid dışındaki) “Hızlı Teklif Al” — tüm gövde + shadow ağacında tıklanır.
     * Önkoşul: yeterli fatura (ürün kuralına göre, örn. 10) sistemde olmalıdır.
     */
    public boolean clickHizliTeklifAlOnBuyerHome() {
        try {
            Boolean js = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function matches(txt) {" +
                    "  if (!txt) return false;" +
                    "  var t = txt.toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t.includes('hızlı teklif') || t.includes('hizli teklif')) return true;" +
                    "  if (t.includes('teklif al') && (t.includes('hızlı') || t.includes('hizli'))) return true;" +
                    "  if (t.includes('teklif al')) return true;" +
                    "  return false;" +
                    "}" +
                    "function walk(node, depth) {" +
                    "  if (!node || depth > 18) return false;" +
                    "  if (node.shadowRoot && walk(node.shadowRoot, depth + 1)) return true;" +
                    "  var btns = node.querySelectorAll ? node.querySelectorAll('vaadin-button, button') : [];" +
                    "  for (var i = btns.length - 1; i >= 0; i--) {" +
                    "    if (matches(btns[i].textContent) && !btns[i].disabled) { btns[i].click(); return true; }" +
                    "  }" +
                    "  var ch = node.children;" +
                    "  if (ch) for (var j = ch.length - 1; j >= 0; j--) if (walk(ch[j], depth + 1)) return true;" +
                    "  return false;" +
                    "}" +
                    "return walk(document.body, 0);");
            if (Boolean.TRUE.equals(js)) {
                Thread.sleep(900);
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Hızlı Teklif Al (ana sayfa): {}", e.getMessage());
        }
        return false;
    }

    public boolean hasIhaleRows() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(GRID));
            List<WebElement> cells = driver.findElements(GRID_CELLS);
            return !cells.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickHizliTeklifAl() {
        clickJsButton("hızlı teklif", "hizli teklif", "teklif al");
    }

    /** Step definition’dan çağrılan genel buton tıklama (light DOM; gölge ağaç için {@link #clickHizliTeklifAlOnBuyerHome}). */
    public void clickJsButtonPublic(String label) {
        if (label == null || label.isBlank()) {
            return;
        }
        clickJsButton(label.trim().toLowerCase());
    }

    public void clickBaslat() {
        clickJsButton("başlat", "baslat", "tamam", "onayla");
    }

    public void clickIhaleBaslat() {
        clickJsButton("ihale başlat", "ihale baslat", "yeni ihale");
    }

    public void selectTedarikci(String ignored) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "var items = document.querySelectorAll('vaadin-item, vaadin-combo-box-item, [role=\"option\"]');" +
                    "for (var it of items) { if ((it.textContent || '').trim().length > 2) { it.click(); return; } }");
        } catch (Exception e) {
            log.warn("Tedarikçi seçimi: {}", e.getMessage());
        }
    }

    public void navigateToProfilOlustur() {
        clickNavItemByText("profil");
        clickNavItemByText("Profil");
    }

    public void clickProfilOlusturButonu() {
        clickJsButton("profil oluştur", "profil olustur", "yeni profil");
    }

    public void enterProfilAdi(String ad) {
        try {
            WebElement input = driver.findElement(By.cssSelector(
                    "vaadin-dialog-overlay vaadin-text-field input, vaadin-text-field input"));
            input.clear();
            input.sendKeys(ad);
        } catch (Exception e) {
            log.warn("Profil adı: {}", e.getMessage());
        }
    }

    public boolean isSuccessNotificationVisible() {
        try {
            return waitForVisibility(SUCCESS_NOTIFICATION, 6).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    private void clickJsButton(String... keywords) {
        for (String kw : keywords) {
            try {
                Boolean ok = (Boolean) ((JavascriptExecutor) driver).executeScript(
                        "var kw = arguments[0].toLowerCase();" +
                        "var btns = document.querySelectorAll('vaadin-button, button');" +
                        "for (var b of btns) {" +
                        "  var t = (b.textContent || '').toLowerCase();" +
                        "  if (t.includes(kw)) { b.click(); return true; }" +
                        "}" +
                        "return false;",
                        kw);
                if (Boolean.TRUE.equals(ok)) {
                    return;
                }
            } catch (Exception e) {
                log.warn("Buton tıklama: {}", e.getMessage());
            }
        }
    }
}
