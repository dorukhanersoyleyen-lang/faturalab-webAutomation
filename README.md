# Faturalab Web and API Automation Project

This project is a web and API test automation framework prepared for Faturalab. Tests written with the BDD approach have been implemented using Cucumber, Selenium WebDriver, TestNG, and REST-Assured.

## Project Features

* Developed with Java 11
* Using Cucumber 7.15.0 BDD framework
* Web automation with Selenium WebDriver 4.16.1
* Test management with TestNG 7.9.0
* API tests with REST-Assured 5.4.0
* Page Object Model (POM) design pattern used
* Logging with Log4j
* Allure and Cucumber reporting integrated

## Tests

### Web Tests

* Faturalab Academy page access tests
* Homepage elements verification
* Instructor list validation

### API Tests

* JsonPlaceholder API endpoint validation
* HTTP status code verification
* Response format validation
* Data structure verification

## How to Run

```bash
# To run web tests
mvn clean test -Dtest=TestRunner

# To run API tests
mvn clean test -Dtest=APITestRunner
```

## Project Video

The following video demonstrates the test automation in action:

[![Faturalab Web Automation Demo](https://i.vimeocdn.com/video/1074132020_640.jpg)](https://vimeo.com/1074132020/35cb3f0e92?ts=0&share=copy "Faturalab Web Automation Demo")

## Project Structure

```
src
├── main
│   └── java
│       └── com.faturalab.automation
│           ├── api          # API Operations
│           ├── config       # Configuration Management
│           ├── driver       # WebDriver Factory
│           ├── models       # Data Models
│           ├── pages        # Page Objects
│           └── utils        # Helper Classes
│
├── test
│   └── java
│       └── com.faturalab.automation
│           ├── runners        # Cucumber Test Runners
│           ├── stepdefinitions # Cucumber Step Definitions
│
└── resources
    ├── features              # Gherkin Feature Files
    └── config                # Properties Files
```

## Technology Stack

| Technology | Version |
|-----------|----------|
| Java | 11 |
| Selenium | 4.16.1 |
| Cucumber | 7.15.0 |
| TestNG | 7.9.0 |
| REST-Assured | 5.4.0 |
| WebDriverManager | 5.6.3 |
| Log4j2 | 2.22.1 |
| Maven | 3.x |

## Reporting

After running tests, reports are generated in the following locations:

- **Cucumber HTML Reports**: `target/cucumber-reports/`
- **Allure Reports**: `target/allure-results/`

To view Allure reports:
```bash
mvn allure:serve
``` 