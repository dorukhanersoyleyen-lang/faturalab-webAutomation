package com.faturalab.automation.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Locale;

/**
 * Tedarikçi "FATURA YÜKLE / TALEP ET" dialogu — farklı dosya tipleriyle yükleme.
 *
 * Canlı DOM'da doğrulanmış yapı (2026-07-08):
 *  - Sidebar butonu: "FATURA YÜKLE/ TALEP ET"
 *  - Dialog başlığı: "FATURA YÜKLE / TALEP ET", sekmeler: Fatura Yükle / Fatura Sil / Fatura Yükleme Talebi
 *  - Fatura Türü radyoları (vaadin-radio-button):
 *      "E-Fatura/E-Arşiv/E-Müstahsil" -> xls, xlsx, xml, zip
 *      "Kağıt Fatura"                 -> jpg, jpeg, png, gif, bmp, pdf, doc, docx (İMZASIZ = PAPER)
 *  - vaadin-upload (auto-upload); dosya seçilince otomatik yüklenir.
 *
 * Dialog içi upload/notification işlemleri {@link CompanyInvoicePage}'e delege edilir.
 */
public class SupplierInvoiceUploadPage extends BasePageObject {

    private final CompanyInvoicePage dialogOps;

    public SupplierInvoiceUploadPage(WebDriver driver) {
        super(driver);
        this.dialogOps = new CompanyInvoicePage(driver);
    }

    /** Tedarikçi ekranında "FATURA YÜKLE/ TALEP ET" dialogunu açar. */
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
                log.info("Tedarikçi 'FATURA YÜKLE/ TALEP ET' butonuna tıklandı.");
                waitForVaadinNavigation();
            } else {
                log.warn("Tedarikçi fatura yükleme butonu bulunamadı.");
                return false;
            }
        } catch (Exception e) {
            log.warn("openUploadDialog: {}", e.getMessage());
            return false;
        }
        return dialogOps.isUploadDialogOpen();
    }

    /** "Kağıt Fatura" fatura türü radyosunu seçer (jpg/png/pdf/... görsel = PAPER). */
    public void selectKagitFatura() {
        selectFaturaTuru("kağıt");
    }

    /** "E-Fatura/E-Arşiv/E-Müstahsil" fatura türü radyosunu seçer (xls/xlsx/xml/zip). */
    public void selectEFatura() {
        selectFaturaTuru("e-fatura");
    }

    private void selectFaturaTuru(String needleLower) {
        try {
            Boolean ok = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var needle = arguments[0];" +
                    "var overlay = document.querySelector('vaadin-dialog-overlay');" +
                    "var root = overlay || document;" +
                    "var radios = root.querySelectorAll('vaadin-radio-button');" +
                    "for (var r of radios) {" +
                    "  var t = (r.textContent || '').toLowerCase();" +
                    "  if (t.includes(needle)) {" +
                    "    if (!(r.checked || r.hasAttribute('checked'))) r.click();" +
                    "    return true;" +
                    "  }" +
                    "}" +
                    "return false;", needleLower);
            log.info("Fatura türü '{}' seçimi: {}", needleLower, ok);
            waitForVaadinNavigation();
        } catch (Exception e) {
            log.warn("selectFaturaTuru '{}': {}", needleLower, e.getMessage());
        }
    }

    /** Görsel/PDF/belge dosyasını vaadin-upload'a gönderir (auto-upload tetiklenir). */
    public void uploadFile(String absoluteFilePath) {
        dialogOps.uploadFile(absoluteFilePath);
    }

    /**
     * Yükleme başarısını bekler. Kağıt fatura görseli auto-upload olduğundan
     * başarı toast'ı kısa süre görünür — hızlı poll edilir.
     */
    public boolean waitForUploadSuccess(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            String toast = readVisibleNotificationText();
            if (toast != null) {
                String lower = toast.toLowerCase(Locale.ROOT);
                if (lower.contains("hata") || lower.contains("başarısız") || lower.contains("basarisiz")
                        || lower.contains("only ") || lower.contains("uploaded")) {
                    // "...can be uploaded" red mesajı da içerir; başarı kelimesi yoksa hata say
                    if (!(lower.contains("başarı") || lower.contains("basari")
                            || lower.contains("successfully") || lower.contains("yüklendi") || lower.contains("yuklendi"))) {
                        log.warn("Yükleme hata/red bildirimi: {}", toast);
                        return false;
                    }
                }
                if (lower.contains("başarı") || lower.contains("basari") || lower.contains("successfully")
                        || lower.contains("yüklendi") || lower.contains("yuklendi")) {
                    log.info("Yükleme başarı bildirimi: {}", toast);
                    return true;
                }
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // Toast kaçırıldıysa: hata yok + dialog kapandıysa başarı say
        boolean dialogClosed = !dialogOps.isUploadDialogOpen();
        boolean noError = !dialogOps.isErrorNotificationVisible();
        log.info("Başarı toast'ı yakalanamadı — dialog kapalı: {}, hata yok: {}", dialogClosed, noError);
        return dialogClosed && noError;
    }

    /**
     * Geçersiz dosya tipi reddini bekler. Kağıt fatura türünde desteklenmeyen uzantı
     * "Only jpg, jpeg, png, gif, bmp, pdf, doc and docx files can be uploaded" (veya TR karşılığı) verir.
     *
     * @return red bildirimi metni; görülmezse null
     */
    public String waitForRejectionMessage(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            String toast = readVisibleNotificationText();
            if (toast != null) {
                String lower = toast.toLowerCase(Locale.ROOT);
                boolean isRejection = lower.contains("only ") || lower.contains("can be uploaded")
                        || lower.contains("uzant") || lower.contains("yüklenebilir")
                        || lower.contains("yuklenebilir") || lower.contains("desteklen");
                if (isRejection) {
                    log.info("Dosya tipi reddi bildirimi: {}", toast);
                    return toast;
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
