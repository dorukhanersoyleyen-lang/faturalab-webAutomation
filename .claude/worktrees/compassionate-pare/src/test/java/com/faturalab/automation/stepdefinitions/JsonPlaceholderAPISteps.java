package com.faturalab.automation.stepdefinitions;

import com.faturalab.automation.api.JsonPlaceholderAPI;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

public class JsonPlaceholderAPISteps {
    
    private static final Logger log = LogManager.getLogger(JsonPlaceholderAPISteps.class);
    private JsonPlaceholderAPI jsonPlaceholderAPI;
    
    public JsonPlaceholderAPISteps() {
        this.jsonPlaceholderAPI = new JsonPlaceholderAPI();
    }
    
    @Given("user sends a GET request to {string}")
    public void user_sends_a_get_request_to(String endpoint) {
        log.info("Sending GET request to endpoint: {}", endpoint);
        jsonPlaceholderAPI.sendGetRequest(endpoint);
    }
    
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer expectedStatusCode) {
        int actualStatusCode = jsonPlaceholderAPI.getStatusCode();
        log.info("Checking status code: expected {}, actual {}", expectedStatusCode, actualStatusCode);
        Assert.assertEquals(actualStatusCode, expectedStatusCode.intValue(), 
                "Status code mismatch. Expected: " + expectedStatusCode + ", Actual: " + actualStatusCode);
    }
    
    @And("the response should be in JSON format")
    public void the_response_should_be_in_json_format() {
        String contentType = jsonPlaceholderAPI.getContentType();
        log.info("Response content type: {}", contentType);
        Assert.assertTrue(contentType.contains("application/json"), 
                "Content type is not JSON. Actual: " + contentType);
    }
    
    @And("the response should be a non-empty array")
    public void the_response_should_be_a_non_empty_array() {
        log.info("Checking if response is a non-empty array");
        Assert.assertTrue(jsonPlaceholderAPI.isResponseArray(), 
                "Response is not an array");
        
        int arraySize = jsonPlaceholderAPI.getArraySize();
        log.info("Array size: {}", arraySize);
        Assert.assertTrue(arraySize > 0, 
                "Array is empty. Size: " + arraySize);
    }
    
    @And("each post should have the correct structure with id, userId, title, and body fields")
    public void each_post_should_have_the_correct_structure() {
        log.info("Validating post structure");
        boolean isValid = jsonPlaceholderAPI.validatePostStructure();
        Assert.assertTrue(isValid, 
                "Posts do not have the expected structure with id, userId, title, and body fields");
    }
} 