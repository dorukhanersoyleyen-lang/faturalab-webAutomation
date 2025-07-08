# language: en
@api @jsonplaceholder
Feature: JsonPlaceholder API Tests
  
  Scenario: Verify Posts endpoint returns correct status code and data structure
    Given user sends a GET request to "https://jsonplaceholder.typicode.com/posts"
    Then the response status code should be 200
    And the response should be in JSON format
    And the response should be a non-empty array
    And each post should have the correct structure with id, userId, title, and body fields 