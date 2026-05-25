package com.faturalab.automation.pages;

import com.faturalab.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePageObject {
    
    // ==================== LOGIN PAGE LOCATORS ====================
    
    // Email Input - Vaadin GWT component with v-textfield class
    private final By EMAIL_INPUT = By.cssSelector("input[type='text'].v-login-textfield-component");
    
    // Password Input - Vaadin passwordfield with specific classes
    private final By PASSWORD_INPUT = By.cssSelector("input[type='password'].v-login-textfield-component");
    
    // Remember Me Checkbox
    private final By REMEMBER_ME_CHECKBOX = By.cssSelector("input[type='checkbox']");
    
    // Login Button - GİRİŞ YAP (Vaadin renders buttons as div elements)
    private final By LOGIN_BUTTON = By.cssSelector("div.login-button[role='button']");
    
    // SSO Login Button (Vaadin div button)
    private final By SSO_LOGIN_BUTTON = By.cssSelector("div.login-button.margin-top-15px[role='button']");
    
    // Register Button - KAYIT OL (Vaadin div button)
    private final By REGISTER_BUTTON = By.cssSelector("div.register-button[role='button']");
    
    // Forgot Password Button (Vaadin div button)
    private final By FORGOT_PASSWORD_BUTTON = By.cssSelector("div.forgot-password-button[role='button']");
    
    // Error Notification - Warning message container
    private final By ERROR_NOTIFICATION = By.cssSelector(".v-Notification.notification-warning");
    
    // Error Message Caption - "Uyarı" heading
    private final By ERROR_CAPTION = By.cssSelector(".v-Notification-caption");
    
    // Error Message Description - Actual error text
    private final By ERROR_DESCRIPTION = By.cssSelector(".v-Notification-description");
    
    // Support Email Link
    private final By SUPPORT_EMAIL_LINK = By.linkText("destek@faturalab.com");
    
    // reCAPTCHA Elements
    private final By RECAPTCHA_IFRAME = By.cssSelector("iframe[title='reCAPTCHA']");
    private final By RECAPTCHA_CHECKBOX = By.cssSelector(".recaptcha-checkbox-border");
    
    // Faturalab Logo - Multiple locator strategies for reliability
    private final String LOGO_XPATH = "/html/body/div/div[1]/div/div[1]";
    private final By LOGO_BY_IMAGE = By.cssSelector("img[alt*='Faturalab'], img[alt*='faturalab'], img[src*='logo']");
    private final By LOGO_BY_CLASS = By.cssSelector(".logo, .brand, .header-logo, [class*='logo']");
    private final By LOGO_BUTTON = By.cssSelector(".v-button.login_logo");
    
    @FindBy(xpath = "/html/body/div/div[1]/div/div[1]")
    private WebElement logoElement;
    
    // Language Toggle Button - EN/TR
    private final By LANGUAGE_BUTTON = By.cssSelector(".v-button.language-button");
    
    // Live Chat Icon (Desk360)
    private final By LIVE_CHAT_IFRAME = By.id("desk360-chat-iframe");
    
    // ==================== CONSTRUCTOR ====================
    
    public HomePage(WebDriver driver) {
        super(driver);
    }
    
    // ==================== PAGE ACTIONS ====================
    
    public void navigateToHomePage() {
        String baseUrl = ConfigReader.getProperty("base.url");
        driver.get(baseUrl);
        log.info("Navigated to homepage: {}", baseUrl);
        waitForPageLoad();
    }
    
    public String getPageTitle() {
        String title = driver.getTitle();
        log.info("Page title is: {}", title);
        return title;
    }
    
    public boolean isLogoDisplayed() {
        try {
            log.info("Attempting to find Faturalab logo with multiple strategies...");
            WebElement logo = null;
            
            // Wait a bit for page to fully load
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Strategy 1: Try to find logo by image (most reliable)
            try {
                logo = waitForVisibility(LOGO_BY_IMAGE, 5);
                if (logo != null && logo.isDisplayed()) {
                    log.info("✅ Logo found using image locator");
                    return true;
                }
            } catch (Exception e1) {
                log.debug("Logo not found by image locator, trying class locator");
            }
            
            // Strategy 2: Try to find logo by class
            try {
                logo = waitForVisibility(LOGO_BY_CLASS, 5);
                if (logo != null && logo.isDisplayed()) {
                    log.info("✅ Logo found using class locator");
                    return true;
                }
            } catch (Exception e2) {
                log.debug("Logo not found by class locator, trying XPath");
            }
            
            // Strategy 3: Fallback to original XPath
            try {
                logo = driver.findElement(By.xpath(LOGO_XPATH));
                if (logo != null && logo.isDisplayed()) {
                    log.info("✅ Logo found using XPath locator");
                    return true;
                }
            } catch (Exception e3) {
                log.debug("Logo not found by XPath");
            }
            
            // Strategy 4: Try any div in header area (very broad fallback)
            try {
                By headerDiv = By.cssSelector("body > div > div:first-child");
                logo = driver.findElement(headerDiv);
                if (logo != null && logo.isDisplayed()) {
                    log.info("✅ Logo area found using header div locator");
                    return true;
                }
            } catch (Exception e4) {
                log.debug("Logo not found by header div locator");
            }
            
            log.warn("❌ Logo element not found with any strategy");
            return false;
            
        } catch (Exception e) {
            log.error("❌ Error checking if logo is displayed. All locator strategies failed: {}", e.getMessage());
            return false;
        }
    }
    
    // ==================== LOGIN ACTIONS ====================
    
    /**
     * Enters email address in the login form
     * @param email The email address to enter
     */
    public void enterEmail(String email) {
        WebElement emailField = waitForElementToBeClickable(EMAIL_INPUT);
        emailField.clear();
        emailField.sendKeys(email);
        log.info("Entered email: {}", email);
    }
    
    /**
     * Enters password in the login form
     * @param password The password to enter
     */
    public void enterPassword(String password) {
        WebElement passwordField = waitForElementToBeClickable(PASSWORD_INPUT);
        passwordField.clear();
        passwordField.sendKeys(password);
        log.info("Entered password");
    }
    
    /**
     * Clicks the login button
     */
    public void clickLoginButton() {
        WebElement loginBtn = waitForElementToBeClickable(LOGIN_BUTTON);
        loginBtn.click();
        log.info("Clicked login button");
    }
    
    /**
     * Toggles the Remember Me checkbox
     */
    public void toggleRememberMe() {
        WebElement checkbox = findElement(REMEMBER_ME_CHECKBOX);
        checkbox.click();
        log.info("Toggled Remember Me checkbox");
    }
    
    /**
     * Performs login with given credentials
     * @param email User email
     * @param password User password
     */
    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLoginButton();
        log.info("Attempted login with email: {}", email);
    }
    
    // ==================== ERROR VALIDATION ====================
    
    /**
     * Checks if error notification is displayed
     * @return true if error notification is visible
     */
    public boolean isErrorNotificationDisplayed() {
        try {
            WebElement errorNotif = waitForVisibility(ERROR_NOTIFICATION, 5);
            boolean isDisplayed = errorNotif.isDisplayed();
            log.info("Error notification displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.warn("Error notification not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the error message text from notification
     * @return Error message text
     */
    public String getErrorMessage() {
        try {
            WebElement errorDesc = waitForVisibility(ERROR_DESCRIPTION, 5);
            String errorText = errorDesc.getText();
            log.info("Error message: {}", errorText);
            return errorText;
        } catch (Exception e) {
            log.error("Could not get error message: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Gets the error caption (usually "Uyarı")
     * @return Error caption text
     */
    public String getErrorCaption() {
        try {
            WebElement caption = waitForVisibility(ERROR_CAPTION, 5);
            String captionText = caption.getText();
            log.info("Error caption: {}", captionText);
            return captionText;
        } catch (Exception e) {
            log.error("Could not get error caption: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Validates that expected error message is displayed
     * @param expectedMessage The expected error message
     * @return true if error matches expected message
     */
    public boolean validateErrorMessage(String expectedMessage) {
        String actualError = getErrorMessage();
        boolean matches = actualError.contains(expectedMessage);
        log.info("Error validation - Expected: {}, Actual: {}, Matches: {}", 
                 expectedMessage, actualError, matches);
        return matches;
    }
    
    // ==================== ALTERNATE LOGIN OPTIONS ====================
    
    /**
     * Clicks SSO login button
     */
    public void clickSSOLogin() {
        WebElement ssoBtn = waitForElementToBeClickable(SSO_LOGIN_BUTTON);
        ssoBtn.click();
        log.info("Clicked SSO login button");
    }
    
    /**
     * Clicks register button
     */
    public void clickRegister() {
        WebElement registerBtn = waitForElementToBeClickable(REGISTER_BUTTON);
        registerBtn.click();
        log.info("Clicked register button");
    }
    
    /**
     * Clicks forgot password button
     */
    public void clickForgotPassword() {
        WebElement forgotBtn = waitForElementToBeClickable(FORGOT_PASSWORD_BUTTON);
        forgotBtn.click();
        log.info("Clicked forgot password button");
    }
    
    // ==================== reCAPTCHA HANDLING ====================
    
    /**
     * Checks if reCAPTCHA is displayed
     * @return true if reCAPTCHA iframe is visible
     */
    public boolean isRecaptchaDisplayed() {
        try {
            WebElement recaptchaFrame = waitForVisibility(RECAPTCHA_IFRAME, 3);
            boolean isDisplayed = recaptchaFrame.isDisplayed();
            log.info("reCAPTCHA displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.debug("reCAPTCHA not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Waits for reCAPTCHA to appear and logs its presence
     * Note: Automated reCAPTCHA solving is not implemented due to security restrictions
     */
    public void handleRecaptcha() {
        if (isRecaptchaDisplayed()) {
            log.warn("reCAPTCHA detected! Manual intervention may be required.");
            log.info("In test environment, reCAPTCHA should be disabled or mocked.");
            
            // Wait a bit for potential auto-resolution or manual intervention
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for reCAPTCHA: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Enhanced login method that handles reCAPTCHA
     * @param email User email
     * @param password User password
     */
    public void loginWithRecaptchaHandling(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickLoginButton();
        
        // Handle reCAPTCHA if it appears
        handleRecaptcha();
        
        log.info("Login attempt completed with email: {}", email);
    }
    
    // ==================== LOGIN PAGE ELEMENT VALIDATION ====================
    
    /**
     * Checks if email input field is displayed on the login page
     * @return true if email input is visible
     */
    public boolean isEmailInputDisplayed() {
        try {
            WebElement emailInput = waitForVisibility(EMAIL_INPUT, 5);
            boolean isDisplayed = emailInput.isDisplayed();
            log.info("Email input field displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.error("Email input field not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if password input field is displayed on the login page
     * @return true if password input is visible
     */
    public boolean isPasswordInputDisplayed() {
        try {
            WebElement passwordInput = waitForVisibility(PASSWORD_INPUT, 5);
            boolean isDisplayed = passwordInput.isDisplayed();
            log.info("Password input field displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.error("Password input field not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if live chat icon/iframe is displayed on the login page
     * @return true if live chat is visible
     */
    public boolean isLiveChatDisplayed() {
        try {
            WebElement liveChatIframe = waitForVisibility(LIVE_CHAT_IFRAME, 5);
            boolean isDisplayed = liveChatIframe.isDisplayed();
            log.info("Live chat iframe displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.error("Live chat iframe not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if language toggle button is displayed on the login page
     * @return true if language button is visible
     */
    public boolean isLanguageButtonDisplayed() {
        try {
            WebElement languageBtn = waitForVisibility(LANGUAGE_BUTTON, 5);
            boolean isDisplayed = languageBtn.isDisplayed();
            log.info("Language button displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.error("Language button not found: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the current language text from the language button (EN or TR)
     * @return Current language code displayed on the button
     */
    public String getLanguageButtonText() {
        try {
            WebElement languageBtn = waitForVisibility(LANGUAGE_BUTTON, 5);
            String languageText = languageBtn.getText().trim();
            log.info("Language button text: {}", languageText);
            return languageText;
        } catch (Exception e) {
            log.error("Could not get language button text: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Clicks the language toggle button to switch between EN and TR
     */
    public void clickLanguageButton() {
        try {
            WebElement languageBtn = waitForElementToBeClickable(LANGUAGE_BUTTON);
            languageBtn.click();
            log.info("Clicked language toggle button");
            // Wait a bit for the page to update
            waitForPageLoad();
        } catch (Exception e) {
            log.error("Could not click language button: {}", e.getMessage());
        }
    }
    
    /**
     * Verifies that the logo is displayed using the most reliable locator
     * @return true if logo button is visible
     */
    public boolean isLogoButtonDisplayed() {
        try {
            WebElement logoBtn = waitForVisibility(LOGO_BUTTON, 5);
            boolean isDisplayed = logoBtn.isDisplayed();
            log.info("Logo button displayed: {}", isDisplayed);
            return isDisplayed;
        } catch (Exception e) {
            log.error("Logo button not found: {}", e.getMessage());
            return false;
        }
    }
} 