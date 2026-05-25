package com.faturalab.automation.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    
    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final Properties properties = new Properties();
    private static final String DEFAULT_CONFIG_PATH = "src/test/resources/config/";
    private static String environment = System.getProperty("env", "dev");
    
    static {
        try {
            loadConfig();
        } catch (IOException e) {
            log.error("Failed to load config: {}", e.getMessage());
        }
    }
    
    private ConfigReader() {
        // Private constructor to prevent instantiation
    }
    
    private static void loadConfig() throws IOException {
        String configFile = DEFAULT_CONFIG_PATH + environment + ".properties";
        log.info("Loading configuration from: {}", configFile);
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
            log.info("Configuration loaded successfully");
        } catch (IOException e) {
            log.error("Unable to load properties from {}: {}", configFile, e.getMessage());
            throw e;
        }
    }
    
    public static String getProperty(String key) {
        // First check system properties (e.g. command line arguments -Dkey=value)
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        
        // Then check loaded properties file
        value = properties.getProperty(key);
        if (value == null) {
            log.warn("Property '{}' not found in configuration", key);
        }
        return value;
    }
    
    public static String getProperty(String key, String defaultValue) {
        // First check system properties
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        }

        // Then check loaded properties file
        value = properties.getProperty(key);
        if (value == null) {
            log.warn("Property '{}' not found in configuration, using default: {}", key, defaultValue);
            return defaultValue;
        }
        return value;
    }
    
    public static void setEnvironment(String env) {
        log.info("Changing environment from {} to {}", environment, env);
        environment = env;
        try {
            loadConfig();
        } catch (IOException e) {
            log.error("Failed to load config for environment {}: {}", env, e.getMessage());
        }
    }
} 