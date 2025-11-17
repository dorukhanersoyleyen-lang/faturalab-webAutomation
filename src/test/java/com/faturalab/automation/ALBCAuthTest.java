package com.faturalab.automation;

import com.faturalab.automation.api.FaturalabAPI;
import com.faturalab.automation.config.EnvironmentManager;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ALBCAuthTest {
    
    private static final Logger log = LogManager.getLogger(ALBCAuthTest.class);
    
    public static void main(String[] args) {
        System.out.println("🚀 ALBC Environment Authentication Test başlatılıyor...");
        System.out.println("==================================================");
        
        try {
            // ALBC Environment ile API instance oluştur
            EnvironmentManager.EnvironmentConfig environmentConfig = EnvironmentManager.loadEnvironment("dev.faturalab.buyer.albc");
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
                System.out.println("🎉 ✅ BAŞARILI! ALBC Authentication çalışıyor!");
                System.out.println("🆔 Session ID: " + faturalabAPI.getSessionId());
                System.out.println("");
                System.out.println("🚀 ALBC environment'ı ile testleri çalıştırabilirsiniz!");
                System.out.println("   mvn clean test -Dtest=ALBCTestRunner");
                System.out.println("   mvn clean test -DsuiteXmlFile=albc-tests.xml");
                System.out.println("");
                System.out.println("📋 Test Kategorileri:");
                System.out.println("   @smoke       - Temel fatura upload/delete testi");
                System.out.println("   @negative    - Boş parametreler testi");
                System.out.println("   @validation  - Geçersiz değerler testi");
                System.out.println("   @invoiceTypes - E-Arşiv fatura testi");
            } else {
                System.out.println("❌ BAŞARISIZ! Authentication hatası:");
                System.out.println("   " + response.getBody().asString());
                System.out.println("");
                System.out.println("💡 Öneriler:");
                System.out.println("   - ALBC environment credential'larını kontrol edin");
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