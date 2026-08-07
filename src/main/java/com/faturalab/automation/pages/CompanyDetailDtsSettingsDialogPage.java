package com.faturalab.automation.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Tedarikçi (Company) — DTS "Devam Et" (Ayar dialogu) sonrası açılan hesaplama diyaloğu
 * (kaynak: {@code CompanyDetailDtsSettingsDialog}, başlık "Yeni Doğrudan Tahsilat Bilgileri" —
 * {@code CompanyDetailDtsSettingsDialog.0}). "Başlat" ({@code .15}) butonu DTS hesaplamasını
 * (algoritma) tetikler: statü hemen CALCULATING (Hesaplanıyor) yapılır, asıl hesaplama arka
 * planda ayrı thread'de ({@code DtsAllocationModel.dtsCalculation()}) çalışır.
 *
 * FAZ 3 kapsamı: dialog açılışını doğrulama + "Başlat" + onay dialogu ("Onay" başlıklı,
 * "Hesaplama işlemi başlatılacak, devam etmek istiyor musunuz?" — Evet/Hayır).
 */
public class CompanyDetailDtsSettingsDialogPage extends BasePageObject {

    public CompanyDetailDtsSettingsDialogPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Diyaloğun açık olup olmadığını görünür metinle doğrular: "Yeni Doğrudan Tahsilat
     * Bilgileri" başlığı VE "Bordro Listesi" accordion'u aynı anda görünür olmalı.
     */
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
                    "  if (t.indexOf('dogrudan tahsilat bilgileri') >= 0 && t.indexOf('bordro listesi') >= 0) return true;" +
                    "}" +
                    "return false;");
            log.info("[DTS] CompanyDetailDtsSettingsDialog acik mi: {}", opened);
            return Boolean.TRUE.equals(opened);
        } catch (Exception e) {
            log.warn("[DTS] isOpened: {}", e.getMessage());
            return false;
        }
    }

    /** Diyalog içeriğinin ilk 800 karakterini debug için loglar (locator kanıtı). */
    public String dumpVisibleText() {
        try {
            Object text = ((JavascriptExecutor) driver).executeScript(
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  return (o.textContent || '').replace(/\\s+/g,' ').trim().substring(0, 800);" +
                    "}" +
                    "return '';");
            String s = String.valueOf(text);
            log.info("[DTS] CompanyDetailDtsSettingsDialog metni: {}", s);
            return s;
        } catch (Exception e) {
            log.warn("[DTS] dumpVisibleText: {}", e.getMessage());
            return "";
        }
    }

    /**
     * "Başlat" butonuna basar ve açılan "Onay" ConfirmDialog'unu
     * ({@code CompanyDetailDtsSettingsDialog.16}="Hesaplama işlemi başlatılacak, devam etmek
     * istiyor musunuz?", Evet/Hayır) onaylar. Geri alınamaz — DTS statüsü hemen CALCULATING'e
     * geçer ve algoritma arka planda başlar.
     */
    public boolean clickStart() {
        try {
            Boolean marked = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "function fold(s){return (s||'')" +
                    ".replace(/[İıI]/g,'i').replace(/[şŞ]/g,'s').replace(/[ğĞ]/g,'g')" +
                    ".replace(/[üÜ]/g,'u').replace(/[öÖ]/g,'o').replace(/[çÇ]/g,'c')" +
                    ".toLowerCase().replace(/\\s+/g,' ').trim();}" +
                    "document.querySelectorAll('[data-dts-start-target]').forEach(function(e){" +
                    "  e.removeAttribute('data-dts-start-target');});" +
                    "var overlays = Array.from(document.querySelectorAll('vaadin-dialog-overlay'));" +
                    "for (var o of overlays) {" +
                    "  var r = o.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue;" +
                    "  var full = fold(o.textContent);" +
                    "  if (full.indexOf('dogrudan tahsilat bilgileri') < 0 || full.indexOf('bordro listesi') < 0) continue;" +
                    "  var btns = o.querySelectorAll('vaadin-button, button');" +
                    "  for (var b of btns) {" +
                    "    if (b.disabled) continue;" +
                    "    var t = fold(b.textContent);" +
                    "    if (t === 'baslat') { b.setAttribute('data-dts-start-target', '1'); return true; }" +
                    "  }" +
                    "}" +
                    "return false;");
            log.info("[DTS] 'Başlat' butonu isaretlendi mi: {}", marked);
            if (!Boolean.TRUE.equals(marked)) {
                return false;
            }
            WebElement btn = driver.findElement(By.cssSelector("[data-dts-start-target='1']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            Thread.sleep(300);
            btn.click();
            Thread.sleep(800);
            acceptVaadinConfirmDialogIfPresent();
            Thread.sleep(1500);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("[DTS] clickStart: {}", e.getMessage());
            return false;
        }
    }
}
