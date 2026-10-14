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
@Feature("Выполнение действия")
@DisplayName("Пользователь выполняет действие")
public class ActionTest extends BaseEndpointTest {
    private final EndpointClient endpointClient = new EndpointClient();

    @Test
    @Story("Авторизованный пользователь")
    @DisplayName("Действие доступно авторизованному пользователю")
    @Description("Сначала входим в систему, затем выполняем действие с тем же token и ожидаем успешный результат.")
    void authorizedUserCanDoAction() {
        String token = TestConfig.newValidToken();

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));
        externalService.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(okJson("{}")));

        reportStep("Пользователь выполняет вход в систему");
        Response loginResponse = endpointClient.send(token, "LOGIN");
        reportStep("Пользователь авторизован");
        assertOk(loginResponse);

        externalService.resetRequests();

        reportStep("Пользователь выполняет действие");
        Response actionResponse = endpointClient.send(token, "ACTION");
        reportStep("Действие выполнено успешно");
        assertOk(actionResponse);

        reportStep("Вызов внешнего сервиса выполнения действия (/doAction) отправлен");
        externalService.verify(postRequestedFor(urlEqualTo("/doAction"))
                .withRequestBody(containing("token=" + token)));
    }

    @Test
    @Story("Неавторизованный пользователь")
    @DisplayName("Без входа в систему действие недоступно")
    @Description("Пытаемся выполнить действие без предварительного входа в систему и ожидаем отказ.")
    void userWithoutLoginCannotDoAction() {
        String token = TestConfig.newValidToken();

        externalService.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(okJson("{}")));

        reportStep("Пользователь пытается выполнить действие без входа в систему");
        Response actionResponse = endpointClient.send(token, "ACTION");
        reportStep("Пользователь получает отказ");
        assertError(actionResponse);

        reportStep("Действие не отправлено во внешний сервис");
        externalService.verify(0, postRequestedFor(urlEqualTo("/doAction")));
    }

    @Test
    @Story("Чужой token")
    @DisplayName("Вход в систему с одним token не авторизует действие с другим token")
    @Description("Входим в систему с одним token, а действие выполняем с другим token. Ожидаем отказ.")
    void loginOfOneTokenDoesNotAuthorizeAnotherToken() {
        String loggedInToken = TestConfig.newValidToken();
        String anotherToken = TestConfig.newValidToken();

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));
        externalService.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(okJson("{}")));

        reportStep("Первый пользователь выполняет вход в систему");
        Response loginResponse = endpointClient.send(loggedInToken, "LOGIN");
        reportStep("Первый пользователь авторизован");
        assertOk(loginResponse);

        externalService.resetRequests();

        reportStep("Другой пользователь пытается выполнить действие без своего входа в систему");
        Response actionResponse = endpointClient.send(anotherToken, "ACTION");
        reportStep("Другой пользователь получает отказ");
        assertError(actionResponse);

        reportStep("Действие другого пользователя не отправлено во внешний сервис");
        externalService.verify(0, postRequestedFor(urlEqualTo("/doAction")));
    }

    @Test
    @Story("Ошибка внешнего сервиса")
    @DisplayName("Ошибка при выполнении действия отображается пользователю")
    @Description("Внешнее выполнение действия отвечает ошибкой. Пользователь должен получить отказ.")
    void externalActionErrorReturnsError() {
        String token = TestConfig.newValidToken();

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));
        externalService.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(serverError()));

        reportStep("Пользователь выполняет вход в систему");
        Response loginResponse = endpointClient.send(token, "LOGIN");
        reportStep("Пользователь авторизован");
        assertOk(loginResponse);

        externalService.resetRequests();

        reportStep("Пользователь выполняет действие");
        Response actionResponse = endpointClient.send(token, "ACTION");
        reportStep("Пользователь получает ошибку выполнения действия");
        assertError(actionResponse);

        reportStep("Вызов внешнего сервиса выполнения действия (/doAction) был отправлен");
        externalService.verify(postRequestedFor(urlEqualTo("/doAction"))
                .withRequestBody(containing("token=" + token)));
    }
}
