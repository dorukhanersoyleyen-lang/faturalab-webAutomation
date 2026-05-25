package com.faturalab.automation.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class ReportOpener {
    
    private static final String[] REPORT_PATHS = {
        "target/cucumber-reports/advanced-reports/cucumber-html-reports/overview-features.html", // Advanced Report (PRIORITY 1)
        "target/cucumber-reports/index.html",        // Basic Cucumber HTML Report (PRIORITY 2)
        "target/surefire-reports/index.html",       // TestNG Report (PRIORITY 3)
        "target/cucumber-reports/cucumber.json"     // JSON Report (for info)
    };
    
    public static void main(String[] args) {
        System.out.println("🌐 Opening test reports in browser...");
        
        try {
            boolean reportOpened = false;
            
            // Try to open the first available report
            for (String reportPath : REPORT_PATHS) {
                File reportFile = new File(reportPath);
                if (reportFile.exists()) {
                    System.out.println("📊 Found report: " + reportPath);
                    
                    if (openInBrowser(reportFile)) {
                        System.out.println("✅ Report opened successfully: " + reportPath);
                        reportOpened = true;
                        break;
                    }
                }
            }
            
            if (!reportOpened) {
                System.out.println("⚠️ No test reports found or unable to open browser");
                System.out.println("📋 Please manually check these locations:");
                for (String path : REPORT_PATHS) {
                    System.out.println("   - " + path);
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error opening reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean openInBrowser(File htmlFile) {
        try {
            // Check if Desktop is supported
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                
                // Check if browse action is supported
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(htmlFile.toURI());
                    return true;
                }
            }
            
            // Fallback: Try OS-specific commands
            String os = System.getProperty("os.name").toLowerCase();
            String filePath = htmlFile.getAbsolutePath();
            
            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + filePath);
                return true;
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec("open " + filePath);
                return true;
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux
                Runtime.getRuntime().exec("xdg-open " + filePath);
                return true;
            }
            
        } catch (IOException e) {
            System.err.println("Failed to open report: " + e.getMessage());
        }
        
        return false;
    }
} 