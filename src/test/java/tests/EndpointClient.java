package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class EndpointClient {
    Response send(String token, String action) {
        return sendWithApiKey(TestConfig.API_KEY, token, action);
    }

    Response sendWithApiKey(String apiKey, String token, String action) {
        RequestSpecification request = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json");

        if (apiKey != null) {
            request.header("X-Api-Key", apiKey);
        }
        if (token != null) {
            request.formParam("token", token);
        }
        if (action != null) {
            request.formParam("action", action);
        }

        return request.post(TestConfig.ENDPOINT_URL);
    }
}
