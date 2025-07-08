# language: en
@academy
Feature: Faturalab Academy Functionality
  
  Scenario: User should be able to view instructors on the Academy page
    Given user navigates to the Faturalab Academy page
    When user clicks on the instructors page
    Then the instructor list should not be empty
    And the instructor count should be 8 