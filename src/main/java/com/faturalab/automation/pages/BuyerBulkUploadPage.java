package com.faturalab.automation.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Alıcı — Excel taslağıyla toplu fatura yükleme (TZF işlemi akışı).
 *
 * Alıcı ana ekranındaki "FATURA YÜKLE" butonu ve dialog, tedarikçi tarafındaki
 * Vaadin bileşenleriyle aynı olduğundan dialog içi işlemler
 * {@link CompanyInvoicePage}'e delege edilir; bu sınıf alıcıya özgü
 * navigasyon ve başarı/hata toast doğrulamasını ekler.
 */
public class BuyerBulkUploadPage extends BasePageObject {

    private final CompanyInvoicePage dialogOps;

    public BuyerBulkUploadPage(WebDriver driver) {
        super(driver);
        this.dialogOps = new CompanyInvoicePage(driver);
    }

    /**
     * Alıcı ekranında "FATURA YÜKLE & SİL" dialogunu açar.
     * Canlı DOM (2026-07-02): sidebar'da yeşil "FATURA YÜKLE & SİL" butonu,
     * dialog başlığı "Fatura Yükle & Sil", sekmeler: Fatura Yükle / Fatura Listesi Yükle / Fatura Sil.
     * Excel liste taslağı varsayılan "Fatura Yükle" sekmesine yüklenir (xls/xlsx/xml/zip kabul eder).
     */
    public boolean openUploadDialog() {
        tryOpenNavigationDrawer();
        try {
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var btns = Array.from(document.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  var t = (b.textContent || '').toUpperCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t.includes('FATURA YÜKLE') || t.includes('FATURA YUKLE')) { b.click(); return true; }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(clicked)) {
                log.info("'FATURA YÜKLE & SİL' butonuna tıklandı.");
                waitForVaadinNavigation();
            } else {
                // Genel fallback KULLANILMAZ: 'ekle' eşleşmesi yanlış ekranda
                // "Yeni Admin Ekle" gibi butonlara basabiliyor.
                log.warn("'FATURA YÜKLE & SİL' butonu sayfada yok — alıcı ekranında değiliz olabilir.");
                return false;
            }
        } catch (Exception e) {
            log.warn("openUploadDialog: {}", e.getMessage());
            return false;
        }
        return dialogOps.isUploadDialogOpen();
    }

    /** Dialog'da Excel sekmesi varsa seçer (tek sekmeli dialog'da sessizce geçer). */
    public void selectExcelTabIfPresent() {
        try {
            Boolean ok = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return false;" +
                    "var els = overlay.querySelectorAll('vaadin-tab, [role=\"tab\"], vaadin-radio-button, vaadin-button, button');" +
                    "for (var el of els) {" +
                    "  var t = (el.textContent || '').toLowerCase().replace(/\\s+/g,' ');" +
                    "  if (t.includes('excel')) { el.click(); return true; }" +
                    "}" +
                    "return false;");
            if (Boolean.TRUE.equals(ok)) {
                log.info("Alıcı yükleme dialogunda Excel sekmesi seçildi.");
                waitForVaadinNavigation();
            }
        } catch (Exception e) {
            log.debug("Excel sekmesi seçimi: {}", e.getMessage());
        }
    }

    /** Üretilen .xls dosyasını dialog'daki upload bileşenine gönderir. */
    public void uploadExcel(String absoluteFilePath) {
        dialogOps.uploadFile(absoluteFilePath);
    }

    /**
     * Yüklemeyi tetikler. DİKKAT: dropzone içindeki "Yükle" butonu native dosya
     * penceresi açar (Selenium'u kilitler) — o yüzden yalnızca vaadin-upload DIŞINDAKİ
     * aksiyon butonlarına basılır. vaadin-upload dosya eklenince otomatik yükler;
     * ayrı buton yoksa hiçbir şeye tıklamadan sonuç beklenir.
     */
    public void clickYukle() {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "if (!overlay) return 'dialog_yok';" +
                    "var btns = Array.from(overlay.querySelectorAll('vaadin-button, button'));" +
                    "for (var b of btns) {" +
                    "  if (b.closest('vaadin-upload')) continue;" + // dropzone butonu: native picker açar
                    "  if (b.disabled) continue;" +
                    "  var t = (b.textContent || '').toLowerCase().replace(/\\s+/g,' ').trim();" +
                    "  if (t === 'yükle' || t === 'yukle' || t.includes('belgelerini yükle') || t === 'kaydet') {" +
                    "    b.click(); return 'tiklandi: ' + t;" +
                    "  }" +
                    "}" +
                    "return 'aksiyon_butonu_yok_otomatik_yukleme';");
            log.info("Yükle aksiyonu: {}", result);
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("clickYukle: {}", e.getMessage());
        }
    }

    /**
     * Yükleme sonucunu doğrular. Başarı toast'ı kısa süre göründüğünden
     * Yükle'ye basıldıktan hemen sonra çağrılmalı ve hızlı poll yapılmalıdır.
     *
     * @return başarı bildirimi görüldüyse veya hata görülmeden dialog kapandıysa true
     */
    public boolean waitForUploadSuccess(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        boolean sawSuccessToast = false;
        while (System.currentTimeMillis() < deadline) {
            String toast = readVisibleNotificationText();
            if (toast != null) {
                String lower = toast.toLowerCase();
                if (lower.contains("hata") || lower.contains("başarısız") || lower.contains("basarisiz")) {
                    log.warn("Yükleme hata bildirimi: {}", toast);
                    return false;
                }
                if (lower.contains("başarı") || lower.contains("basari") || lower.contains("yüklendi")
                        || lower.contains("yuklendi")) {
                    log.info("Yükleme başarı bildirimi: {}", toast);
                    sawSuccessToast = true;
                    break;
                }
            }
            if (dialogOps.isErrorNotificationVisible()) {
                log.warn("Yükleme hata durumu algılandı (inline/upload error).");
                return false;
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (sawSuccessToast) {
            return true;
        }
        // Toast kaçırıldıysa: dialog hatasız kapandı mı?
        boolean dialogClosed = !dialogOps.isUploadDialogOpen();
        boolean noError = !dialogOps.isErrorNotificationVisible();
        log.info("Başarı toast'ı yakalanamadı — dialog kapalı: {}, hata yok: {}", dialogClosed, noError);
        return dialogClosed && noError;
    }

    /**
     * Geçersiz dosya tipi reddini bekler ve red bildiriminin metnini döner.
     * Kaynak koda göre alıcı dialog'u desteklenmeyen uzantıda
     * "Only files with xls, xlsx, csv, xml, zip extensions can be uploaded." (veya TR karşılığı) verir.
     *
     * @return red bildirimi metni; süre içinde red görülmezse null
     */
    public String waitForRejectionMessage(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            String toast = readVisibleNotificationText();
            if (toast != null) {
                String lower = toast.toLowerCase(java.util.Locale.ROOT);
                boolean isRejection = lower.contains("extension") || lower.contains("uzant")
                        || lower.contains("only files") || lower.contains("yüklenebilir")
                        || lower.contains("yuklenebilir") || lower.contains("desteklen");
                if (isRejection) {
                    log.info("Dosya tipi reddi bildirimi: {}", toast);
                    return toast;
                }
            }
            if (dialogOps.isErrorNotificationVisible()) {
                String n = dialogOps.getNotificationText();
                if (n != null && !n.trim().isEmpty()) {
                    log.info("Red (inline) bildirimi: {}", n);
                    return n;
                }
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
    }

    /** O an görünür vaadin-notification-card / toast metnini döner, yoksa null. */
    private String readVisibleNotificationText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "var cards = document.querySelectorAll(" +
                    "  'vaadin-notification-card, vaadin-notification-container > *, .v-Notification');" +
                    "for (var c of cards) {" +
                    "  var r = c.getBoundingClientRect();" +
                    "  if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = (c.textContent || '').trim();" +
                    "  if (t.length > 0) return t;" +
                    "}" +
                    "return null;");
            return text != null ? text.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
