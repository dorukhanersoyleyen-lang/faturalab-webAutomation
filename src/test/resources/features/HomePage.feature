# language: en
@web @homepage @academy
Feature: Faturalab Homepage Functionality
  
  Scenario: User should be able to successfully access the homepage
    Given user navigates to the homepage
    Then the page title should contain "Faturalab"
    And the Faturalab logo should be displayed 