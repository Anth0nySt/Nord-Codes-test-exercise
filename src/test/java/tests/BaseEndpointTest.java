package tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BaseEndpointTest {
    static WireMockServer externalService;

    @BeforeAll
    static void startExternalServiceMock() {
        externalService = new WireMockServer(options().port(TestConfig.MOCK_PORT));
        externalService.start();
    }

    @BeforeEach
    void resetExternalServiceMock() {
        externalService.resetAll();
    }

    @AfterAll
    static void stopExternalServiceMock() {
        externalService.stop();
    }

    protected void assertOk(Response response) {
        addResponseToAllure(response);

        assertAll(
                () -> assertEquals(200, response.statusCode(), response.asString()),
                () -> assertEquals("OK", response.jsonPath().getString("result"), response.asString())
        );
    }

    protected void assertError(Response response) {
        addResponseToAllure(response);

        assertAll(
                () -> assertNotEquals(200, response.statusCode(), response.asString()),
                () -> assertEquals("ERROR", response.jsonPath().getString("result"), response.asString())
        );
    }

    protected void reportStep(String stepName) {
        Allure.step(stepName);
    }

    private void addResponseToAllure(Response response) {
        Allure.step("HTTP status: " + response.statusCode());
        Allure.step("Response body: " + response.asString());
    }
}
