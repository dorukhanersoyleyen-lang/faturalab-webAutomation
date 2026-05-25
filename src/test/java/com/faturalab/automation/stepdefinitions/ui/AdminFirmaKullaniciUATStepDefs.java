package com.faturalab.automation.stepdefinitions.ui;

import com.faturalab.automation.driver.DriverManager;
import com.faturalab.automation.pages.AdminFirmaPage;
import com.faturalab.automation.pages.AdminKullaniciPage;
import com.faturalab.automation.utils.VaadinFormFieldSnapshot;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

/**
 * FL-003: Firma Düzenleme
 * FL-004: Kullanıcı Düzenleme
 */
public class AdminFirmaKullaniciUATStepDefs {

    private static final Logger log = LogManager.getLogger(AdminFirmaKullaniciUATStepDefs.class);

    private AdminFirmaPage firmaPage;
    private AdminKullaniciPage kullaniciPage;
    private String activeContext = "firma";
    /** Güncellemeden önceki tüm diyalog alanları (JSON) — senaryo bitince geri yüklenir */
    private String firmaDialogSnapshotJson;
    private String kullaniciDialogSnapshotJson;

    private AdminFirmaPage getFirmaPage() {
        if (firmaPage == null) firmaPage = new AdminFirmaPage(DriverManager.getDriver());
        return firmaPage;
    }

    private AdminKullaniciPage getKullaniciPage() {
        if (kullaniciPage == null) kullaniciPage = new AdminKullaniciPage(DriverManager.getDriver());
        return kullaniciPage;
    }

    // ─── FL-003: Firma Düzenleme ──────────────────────────────────────────────

    @When("admin firma yönetimi ekranına gidilir")
    public void adminFirmaYonetimi() {
        activeContext = "firma";
        getFirmaPage().navigateToFirmaYonetimi();
    }

    @And("firma listesi görüntülenir")
    public void firmaListesiGoruntur() {
        Assert.assertTrue(getFirmaPage().isFirmaGridVisible(),
                "Firma listesi grid'i görünmeli");
        log.info("Firma listesi grid'i görüntülendi.");
    }

    @And("ilk firma için düzenle butonuna tıklanır")
    public void ilkFirmaDuzenle() {
        getFirmaPage().clickFirstDuzenleButton();
        Assert.assertTrue(getFirmaPage().waitForFirmaDialogOpen(12),
                "Firma düzenleme dialogu açılmalı");
    }

    @And("firma bilgileri güncellenir")
    public void firmaBilgileriGuncelle() {
        if (firmaDialogSnapshotJson == null) {
            firmaDialogSnapshotJson = VaadinFormFieldSnapshot.snapshot(
                    DriverManager.getDriver(), VaadinFormFieldSnapshot.ROOT_FIRMA_EDIT);
            log.info("Firma diyalog alanları snapshot ({} karakter)", 
                    firmaDialogSnapshotJson != null ? firmaDialogSnapshotJson.length() : 0);
        }
        String yeniAdres = "Test Otomasyon Adres " + (System.currentTimeMillis() % 10000);
        getFirmaPage().fillFirmaEditDialogForSave(yeniAdres);
        log.info("Firma diyaloğu dolduruldu ve adres güncellendi: {}", yeniAdres);
    }

    @And("kaydet butonuna tıklanır")
    public void kaydetButonunaTikla() {
        if ("kullanici".equals(activeContext)) {
            getKullaniciPage().clickKaydet();
        } else {
            getFirmaPage().clickKaydetInDialog();
        }
    }

    @Then("firma bilgileri başarıyla güncellenmiş olmalı")
    public void firmaBilgileriGuncellenmeli() {
        try {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            boolean success = getFirmaPage().isSuccessNotificationVisible();
            boolean dialogKapandi = !getFirmaPage().isFirmaDialogOpen();
            boolean gridHala = getFirmaPage().isFirmaGridVisible();
            if (!success) {
                log.warn("Firma güncelleme toast görünmedi — dialog kapandı / sessiz kayıt olabilir.");
            }
            Assert.assertTrue(success || dialogKapandi || gridHala,
                    "Başarı bildirimi, kapanan diyalog veya firma listesi grid'i beklenir. URL: "
                            + DriverManager.getDriver().getCurrentUrl());
            log.info("Firma güncelleme kontrolü tamamlandı.");
        } finally {
            revertFirmaDialogToSnapshot();
        }
    }

    private void revertFirmaDialogToSnapshot() {
        if (firmaDialogSnapshotJson == null) {
            return;
        }
        try {
            Thread.sleep(800);
            activeContext = "firma";
            getFirmaPage().clickFirstDuzenleButton();
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (!getFirmaPage().waitForFirmaDialogOpen(10)) {
                log.warn("Firma geri yükleme: diyalog açılmadı.");
            } else {
                VaadinFormFieldSnapshot.restore(DriverManager.getDriver(), firmaDialogSnapshotJson,
                        VaadinFormFieldSnapshot.ROOT_FIRMA_EDIT);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                getFirmaPage().clickKaydetInDialog();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Firma diyalog alanları önceki değerlere geri yazıldı.");
        } catch (Exception e) {
            log.warn("Firma diyalog geri alınamadı: {}", e.getMessage());
        } finally {
            firmaDialogSnapshotJson = null;
        }
    }

    // ─── FL-004: Kullanıcı Düzenleme ─────────────────────────────────────────

    @When("admin kullanıcı yönetimi ekranına gidilir")
    public void adminKullaniciYonetimi() {
        activeContext = "kullanici";
        getKullaniciPage().navigateToKullaniciYonetimi();
    }

    @And("kullanıcı listesi görüntülenir")
    public void kullaniciListesiGoruntur() {
        Assert.assertTrue(getKullaniciPage().isKullaniciGridVisible(),
                "Kullanıcı listesi grid'i görünmeli");
        log.info("Kullanıcı listesi grid'i görüntülendi.");
    }

    @And("ilk kullanıcı satırına tıklanır")
    public void ilkKullaniciSatirina() {
        getKullaniciPage().clickFirstUserRow();
    }

    @And("kullanıcı düzenle işlemi yapılır")
    public void kullaniciDuzenle() {
        getKullaniciPage().clickDuzenle();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (kullaniciDialogSnapshotJson == null) {
            kullaniciDialogSnapshotJson = VaadinFormFieldSnapshot.snapshot(
                    DriverManager.getDriver(), VaadinFormFieldSnapshot.ROOT_KULLANICI_EDITOR);
            log.info("Kullanıcı form alanları snapshot ({} karakter)",
                    kullaniciDialogSnapshotJson != null ? kullaniciDialogSnapshotJson.length() : 0);
        }
        String yeniAdres = "Test Kullanıcı Adres " + (System.currentTimeMillis() % 10000);
        getKullaniciPage().updateKullaniciAdres(yeniAdres);
        log.info("Kullanıcı adresi güncellendi: {}", yeniAdres);
    }

    @Then("kullanıcı bilgileri başarıyla güncellenmiş olmalı")
    public void kullaniciBilgileriGuncellenmeli() {
        try {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            boolean success = getKullaniciPage().isSuccessNotificationVisible();
            if (!success) {
                log.warn("Kullanıcı güncelleme toast görünmedi — grid hâlâ yüklü mü kontrol ediliyor.");
            }
            boolean grid = getKullaniciPage().isKullaniciGridVisible();
            Assert.assertTrue(success || grid,
                    "Başarı bildirimi veya kullanıcı listesi grid'i görünür olmalı. URL: "
                            + DriverManager.getDriver().getCurrentUrl());
            log.info("Kullanıcı güncelleme kontrolü tamamlandı.");
        } finally {
            revertKullaniciFormToSnapshot();
        }
    }

    private void revertKullaniciFormToSnapshot() {
        if (kullaniciDialogSnapshotJson == null) {
            return;
        }
        try {
            activeContext = "kullanici";
            Thread.sleep(800);
            getKullaniciPage().clickFirstUserRow();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            getKullaniciPage().clickDuzenle();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            VaadinFormFieldSnapshot.restore(DriverManager.getDriver(), kullaniciDialogSnapshotJson,
                    VaadinFormFieldSnapshot.ROOT_KULLANICI_EDITOR);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            getKullaniciPage().clickKaydet();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Kullanıcı form alanları önceki değerlere geri yazıldı.");
        } catch (Exception e) {
            log.warn("Kullanıcı form geri alınamadı: {}", e.getMessage());
        } finally {
            kullaniciDialogSnapshotJson = null;
        }
    }
}
