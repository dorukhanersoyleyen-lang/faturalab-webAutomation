package com.faturalab.automation.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentManager {
    
    private static final Logger log = LogManager.getLogger(EnvironmentManager.class);
    private static final String CONFIG_PATH = "src/test/resources/config/";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, EnvironmentConfig> environments = new HashMap<>();
    
    public static class EnvironmentConfig {
        private String host;
        private String apiKey;
        private String alias;
        private String password;
        private String taxNumber;
        private String userEmail;
        private String sessionId;
        
        // Getters and Setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        
        public String getAlias() { return alias; }
        public void setAlias(String alias) { this.alias = alias; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getTaxNumber() { return taxNumber; }
        public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }
        
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        @Override
        public String toString() {
            return "EnvironmentConfig{" +
                    "host='" + host + '\'' +
                    ", alias='" + alias + '\'' +
                    ", taxNumber='" + taxNumber + '\'' +
                    ", userEmail='" + userEmail + '\'' +
                    '}';
        }
    }
    
    public static EnvironmentConfig loadEnvironment(String environmentName) {
        if (environments.containsKey(environmentName)) {
            return environments.get(environmentName);
        }
        
        try {
            String fileName = environmentName + ".postman_environment.json";
            File configFile = new File(CONFIG_PATH + fileName);
            
            log.info("Loading environment from: {}", configFile.getAbsolutePath());
            
            if (!configFile.exists()) {
                throw new RuntimeException("Environment file not found: " + fileName);
            }
            
            JsonNode rootNode = objectMapper.readTree(configFile);
            JsonNode valuesNode = rootNode.get("values");
            
            EnvironmentConfig config = new EnvironmentConfig();
            
            if (valuesNode != null && valuesNode.isArray()) {
                for (JsonNode valueNode : valuesNode) {
                    String key = valueNode.get("key").asText();
                    String value = valueNode.get("value").asText();
                    
                    switch (key) {
                        case "host":
                            config.setHost(value);
                            break;
                        case "apiKey":
                            config.setApiKey(value);
                            break;
                        case "alias":
                            config.setAlias(value);
                            break;
                        case "password":
                            config.setPassword(value);
                            break;
                        case "taxNumber":
                            config.setTaxNumber(value);
                            break;
                        case "userEmail":
                            config.setUserEmail(value);
                            break;
                        case "sessionId":
                            config.setSessionId(value);
                            break;
                    }
                }
            }
            
            environments.put(environmentName, config);
            log.info("Environment loaded successfully: {}", config);
            return config;
            
        } catch (IOException e) {
            log.error("Failed to load environment: {}", environmentName, e);
            throw new RuntimeException("Failed to load environment: " + environmentName, e);
        }
    }
    
    public static String[] getBuyerEnvironments() {
        return new String[]{
            "dev.faturalab.buyer.albc",
            "dev.faturalab.buyer.migros",
            "dev.faturalab.buyer.hepsiburada", 
            "dev.faturalab.buyer.carrefoursa",
            "dev.faturalab.buyer.a101",
            "dev.faturalab.buyer.bizimtoptan"
        };
    }
    
    public static String[] getBankEnvironments() {
        return new String[]{
            "dev.faturalab.bank.isbank.fl",
            "dev.faturalab.bank.garanti.fl",
            "dev.faturalab.bank.akbank.fl"
        };
    }
} 