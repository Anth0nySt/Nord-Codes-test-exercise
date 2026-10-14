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
@Feature("Выход из системы")
@DisplayName("Пользователь завершает сессию")
public class LogoutTest extends BaseEndpointTest {
    private final EndpointClient endpointClient = new EndpointClient();

    @Test
    @Story("Выход из системы")
    @DisplayName("После выхода пользователь не может выполнять действия")
    @Description("Входим в систему, выходим из нее и проверяем, что с тем же token действие больше недоступно.")
    void logoutRemovesTokenFromSession() {
        String token = TestConfig.newValidToken();

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));
        externalService.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(okJson("{}")));

        reportStep("Пользователь выполняет вход в систему");
        Response loginResponse = endpointClient.send(token, "LOGIN");
        reportStep("Пользователь авторизован");
        assertOk(loginResponse);

        reportStep("Пользователь выполняет выход из системы");
        Response logoutResponse = endpointClient.send(token, "LOGOUT");
        reportStep("Сессия пользователя завершена");
        assertOk(logoutResponse);

        externalService.resetRequests();

        reportStep("Пользователь пытается выполнить действие после выхода из системы");
        Response actionAfterLogoutResponse = endpointClient.send(token, "ACTION");
        reportStep("Пользователь получает отказ");
        assertError(actionAfterLogoutResponse);

        reportStep("Действие после выхода из системы не отправлено во внешний сервис");
        externalService.verify(0, postRequestedFor(urlEqualTo("/doAction")));
    }
}
