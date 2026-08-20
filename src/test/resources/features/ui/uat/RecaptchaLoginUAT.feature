# RECAPTCHA-LOGIN-001: reCAPTCHA'li Admin Login Doğrulaması (OP#5909, OP#5255)
#
# appsettings.CAPTCHA_ENABLED normalde CLOSED tutulur (diğer tüm günlük UAT senaryoları
# hızlı login için buna güvenir). Bu senaryo SADECE kendi süresi boyunca CAPTCHA_ENABLED'i
# OPEN yapar (@Before), reCAPTCHA v3 token bekleme + gerekirse ikinci tıklama mantığıyla
# (RoleSessionManager.performLogin) admin login'in gerçekten çalıştığını doğrular, sonra
# (PASS/FAIL fark etmeden) CAPTCHA_ENABLED'i tekrar CLOSED'a döndürür (@After).
Feature: reCAPTCHA'li Login UAT
  reCAPTCHA aktifken admin login akışı, v3 token bekleme ve gerekirse V2 fallback
  teşhisiyle birlikte sorunsuz tamamlanmalı.

  @ui @uat @recaptcha-login
  Scenario: RECAPTCHA-LOGIN-001 - reCAPTCHA aktifken admin login başarıyla tamamlanır
    When admin reCAPTCHA aktifken login olur
    Then admin dashboard'a başarıyla ulaşır
