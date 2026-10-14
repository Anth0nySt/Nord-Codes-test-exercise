package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Epic("Действия пользователя")
@Feature("Вход в систему")
@DisplayName("Пользователь входит в систему")
public class LoginTest extends BaseEndpointTest {
    private final EndpointClient endpointClient = new EndpointClient();

    @Test
    @Story("Успешный вход")
    @DisplayName("Вход в систему проходит успешно")
    @Description("Входим в систему с корректным token и ожидаем успешную авторизацию.")
    void successfulLoginReturnsOk() {
        String token = TestConfig.newValidToken();

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));

        reportStep("Пользователь выполняет вход в систему");
        Response response = endpointClient.send(token, "LOGIN");

        reportStep("Пользователь авторизован");
        assertOk(response);

        reportStep("Вызов внешнего сервиса авторизации (/auth) отправлен");
        externalService.verify(postRequestedFor(urlEqualTo("/auth"))
                .withRequestBody(containing("token=" + token)));
    }

    @Test
    @Story("Отказ при входе")
    @DisplayName("Вход в систему завершается отказом при ошибке внешней авторизации")
    @Description("Внешняя авторизация отвечает ошибкой, поэтому вход в систему должен завершиться отказом.")
    void failedLoginReturnsError() {
        String token = TestConfig.newValidToken();

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(serverError()));

        reportStep("Пользователь выполняет вход в систему");
        Response response = endpointClient.send(token, "LOGIN");

        reportStep("Пользователь получает отказ в авторизации");
        assertError(response);

        reportStep("Вызов внешнего сервиса авторизации (/auth) был отправлен");
        externalService.verify(postRequestedFor(urlEqualTo("/auth"))
                .withRequestBody(containing("token=" + token)));
    }
}
