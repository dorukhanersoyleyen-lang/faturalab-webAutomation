package com.faturalab.automation;

import com.faturalab.automation.api.FaturalabAPI;
import com.faturalab.automation.config.EnvironmentManager;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BienAuthTest {
    
    private static final Logger log = LogManager.getLogger(BienAuthTest.class);
    
    public static void main(String[] args) {
        System.out.println("🚀 BIEN Environment Authentication Test başlatılıyor...");
        System.out.println("==================================================");
        
        try {
            // BIEN Environment ile API instance oluştur
            EnvironmentManager.EnvironmentConfig environmentConfig = EnvironmentManager.loadEnvironment("dev.faturalab.bank.bien");
            FaturalabAPI faturalabAPI = new FaturalabAPI(environmentConfig);
            
            System.out.println("✅ Environment yüklendi: " + faturalabAPI.getEnvironment().getAlias());
            System.out.println("🌐 Host: " + faturalabAPI.getEnvironment().getHost());
            System.out.println("🔑 API Key: " + faturalabAPI.getEnvironment().getApiKey());
            System.out.println("👤 Alias: " + faturalabAPI.getEnvironment().getAlias());
            System.out.println("📞 Tax Number: " + faturalabAPI.getEnvironment().getTaxNumber());
            System.out.println("📧 Email: " + faturalabAPI.getEnvironment().getUserEmail());
            System.out.println("");
            
            // Authentication testi
            System.out.println("🔐 Authentication test ediliyor...");
            Response response = faturalabAPI.authenticate();
            
            System.out.println("📊 Sonuçlar:");
            System.out.println("   Status Code: " + response.getStatusCode());
            System.out.println("   Response: " + response.getBody().asString());
            System.out.println("");
            
            // Sonuç değerlendirmesi
            boolean isSuccessful = faturalabAPI.isResponseSuccessful();
            if (isSuccessful) {
                System.out.println("🎉 ✅ BAŞARILI! Authentication çalışıyor!");
                System.out.println("🆔 Session ID: " + faturalabAPI.getSessionId());
                System.out.println("");
                System.out.println("🚀 BIEN environment'ı ile testleri çalıştırabilirsiniz!");
            } else {
                System.out.println("❌ BAŞARISIZ! Authentication hatası:");
                System.out.println("   " + response.getBody().asString());
                System.out.println("");
                System.out.println("💡 Öneriler:");
                System.out.println("   - BIEN environment credential'larını kontrol edin");
                System.out.println("   - API endpoint'in erişilebilir olduğunu doğrulayın");
                System.out.println("   - VPN bağlantınızı kontrol edin");
            }
            
        } catch (Exception e) {
            System.out.println("💥 HATA oluştu:");
            System.out.println("   " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("==================================================");
        System.out.println("🏁 Test tamamlandı!");
    }
} 