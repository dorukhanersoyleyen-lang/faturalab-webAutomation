# language: en
@LoginPageElements @UI
Feature: Login Page UI Elements Validation
  As a user, I want to verify that all important UI elements
  on the login page are visible

  Background:
    Given user navigates to "https://dev.faturalab.com/app"

  @SmokeTest @Critical
  Scenario: Verify email input field exists on login page
    Then email input field should be visible

  @SmokeTest @Critical
  Scenario: Verify password input field exists on login page
    Then password input field should be visible

  @SmokeTest
  Scenario: Verify logo exists on login page
    Then FaturaLab logo should be visible

  @SmokeTest
  Scenario: Verify live chat icon exists on login page
    Then live chat icon should be visible

  @SmokeTest
  Scenario: Verify language toggle button exists on login page
    Then language toggle button should be visible

  @Functional
  Scenario: Verify language button shows EN initially
    Then language button should display "EN" text

  @Functional @LanguageSwitch
  Scenario: Verify page switches to Turkish when EN button is clicked
    When user clicks the language toggle button
    Then language button should display "TR" text
    And page should be in Turkish language

  @Functional @LanguageSwitch
  Scenario: Verify page switches to English when TR button is clicked
    When user clicks the language toggle button
    When user clicks the language toggle button
    Then language button should display "EN" text
    And page should be in English language

  @SmokeTest @Critical
  Scenario: Verify all critical elements exist together on login page
    Then email input field should be visible
    And password input field should be visible
    And FaturaLab logo should be visible
    And live chat icon should be visible
    And language toggle button should be visible

