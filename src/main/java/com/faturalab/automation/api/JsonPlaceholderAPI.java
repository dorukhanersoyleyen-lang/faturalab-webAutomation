package com.faturalab.automation.api;

import com.faturalab.automation.models.Post;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonPlaceholderAPI {
    
    private static final Logger log = LogManager.getLogger(JsonPlaceholderAPI.class);
    private Response response;
    
    public JsonPlaceholderAPI() {
        // Initialize REST-assured
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
    }
    
    public void sendGetRequest(String endpoint) {
        log.info("Sending GET request to: {}", endpoint);
        
        RequestSpecification requestSpec = RestAssured.given();
        requestSpec.header("Content-Type", "application/json");
        
        response = requestSpec.get(endpoint);
        log.info("Received response with status code: {}", response.getStatusCode());
    }
    
    public Response getResponse() {
        return response;
    }
    
    public int getStatusCode() {
        return response.getStatusCode();
    }
    
    public String getContentType() {
        return response.getContentType();
    }
    
    public boolean isResponseArray() {
        try {
            response.then().assertThat().extract().as(Object[].class);
            return true;
        } catch (Exception e) {
            log.error("Response is not an array: {}", e.getMessage());
            return false;
        }
    }
    
    public int getArraySize() {
        if (isResponseArray()) {
            Object[] responseArray = response.then().extract().as(Object[].class);
            return responseArray.length;
        }
        return 0;
    }
    
    public boolean validatePostStructure() {
        try {
            if (isResponseArray() && getArraySize() > 0) {
                // Convert the first item to a Post object
                Post[] posts = response.as(Post[].class);
                
                if (posts.length > 0) {
                    Post firstPost = posts[0];
                    
                    // Check that all required fields are present
                    boolean hasId = firstPost.getId() > 0;
                    boolean hasUserId = firstPost.getUserId() > 0;
                    boolean hasTitle = firstPost.getTitle() != null && !firstPost.getTitle().isEmpty();
                    boolean hasBody = firstPost.getBody() != null && !firstPost.getBody().isEmpty();
                    
                    log.info("Post structure validation - ID: {}, UserID: {}, Title: {}, Body: {}", 
                            hasId, hasUserId, hasTitle, hasBody);
                    
                    return hasId && hasUserId && hasTitle && hasBody;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error validating post structure: {}", e.getMessage());
            return false;
        }
    }
    
    public Post[] getPosts() {
        try {
            return response.as(Post[].class);
        } catch (Exception e) {
            log.error("Error extracting posts: {}", e.getMessage());
            return new Post[0];
        }
    }
} 