package com.faturalab.automation.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * "Sözleşmeler (... adet)" onay diyaloğu (kaynak: {@code CompanyLicenceAgreementDialog},
 * {@code origin/vaadin-24} READ-ONLY incelendi).
 *
 * ⚠️ KÖK NEDEN (2026-08-13, canlı koşumla kanıtlandı): Ara Tedarikçi (company 998) yeni kurulan
 * DTF zincirinde kendi buyer.id=151'ine bağlandığı için ({@code company.isbuyer=true} +
 * taxnumber eşleşmesi), {@code CompanyContractModel.getUnsignedActiveCompanyContractsForAuction}
 * artık company 998 için GENEL "DTF Sözleşmesi" (ContractType.DTF_AGREEMENT) kaydının
 * imzalanmamış olduğunu tespit ediyor ({@code CompanyContract} tablosunda hiç kayıt yok = imzasız
 * sayılıyor). Bu YÜZDEN "TEKLİF AL" tıklanınca {@code CompanyAddEditAuctionDialog} ile AYNI ANDA
 * bu sözleşme diyaloğu da açılıyor ve kabul edilene kadar "DTF Başlat"/"Teklif Al" DISABLED kalıyor
 * — bu bir otomasyon hatası değil, platformun gerçek bir hukuki-onay kapısı (feature). Kod kanıtı:
 * {@code CompanyLicenceAgreementDialog.acceptButton} click listener'ı {@code selectedCompanyContracts}
 * boşsa reddeder (en az bir sözleşme checkbox'ı işaretli olmalı), doluysa
 * {@code auctionDialog.setDtfButtonEnabled(true)} çağırıp gerçek imza (CompanyContract.signed=true)
 * kaydını yazar.
 */
public class CompanyLicenceAgreementDialogPage extends BasePageObject {

    public CompanyLicenceAgreementDialogPage(WebDriver driver) {
        super(driver);
    }

    /** "Sözleşmeler (" başlığı VEYA "Okudum ve Kabul Ediyorum" butonu görünüyorsa dialog açık demektir. */
    public boolean isOpened() {
        try {
            Boolean opened = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = fold(o.textContent);" +
                    "  if (t.indexOf('sozlesmeler (') >= 0 || t.indexOf('okudum ve kabul ediyorum') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTF] CompanyLicenceAgreementDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTF] isOpened (CompanyLicenceAgreementDialog): {}", e.getMessage());
            return false;
        }
    }

    /**
     * Diyalogdaki TÜM sözleşme checkbox'larını işaretler, sonra "Okudum ve Kabul Ediyorum"
     * butonuna tıklar. Checkbox işaretlenmeden buton tıklanırsa uygulama "Lütfen sözleşme seçimi
     * yapınız." uyarısıyla reddeder (kaynak kod kanıtı) — bu yüzden sıra ÖNEMLİ.
     */
    public boolean acceptAllContracts() {
        try {
            Boolean result = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var t = fold(o.textContent);" +
                    "  if (t.indexOf('sozlesmeler (') < 0 && t.indexOf('okudum ve kabul ediyorum') < 0) continue;" +
                    "  var cbs = o.querySelectorAll('vaadin-checkbox');" +
                    "  for (var cb of cbs) { if (!cb.checked) { cb.click(); } }" +
                    "  return true;" +
                    "}" +
                    "return false;");
            log.info("[DTF] Sözleşme checkbox'ları işaretlendi mi: {}", result);
            if (!Boolean.TRUE.equals(result)) {
                return false;
            }
            Thread.sleep(500);
            Boolean clicked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    if (fold(b.textContent) === 'okudum ve kabul ediyorum') { b.click(); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTF] 'Okudum ve Kabul Ediyorum' butonuna tıklandı mı: {}", clicked);
            if (Boolean.TRUE.equals(clicked)) {
                Thread.sleep(1500);
            }
            return Boolean.TRUE.equals(clicked);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTF] acceptAllContracts: {}", e.getMessage());
            return false;
        }
    }
}
