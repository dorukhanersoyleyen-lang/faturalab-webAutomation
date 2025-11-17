# language: en
@web @homepage @academy
Feature: Faturalab Homepage Functionality
  
  Scenario: User should be able to successfully access the homepage
    Given user navigates to the homepage
    Then the Faturalab logo should be displayed
  
  @login @negative
  Scenario: User should see error message when login with invalid credentials
    Given user navigates to the homepage
    When user enters email "invalidmail@mail.com" in the login form
    And user enters password "invalidpassword" in the login form
    And user clicks the login button
    And user handles reCAPTCHA if present
    Then user should see an error notification
    And the error message should contain "E-Posta adresi/şifre geçersiz"
  
  @login @negative @recaptcha
  Scenario: User should handle reCAPTCHA during login process
    Given user navigates to the homepage
    When user attempts login with invalid credentials and reCAPTCHA handling
    Then reCAPTCHA should be detected and handled appropriately 