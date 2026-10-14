package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Действия пользователя")
@Feature("Проверка входных данных")
@DisplayName("Проверка входных данных")
public class ValidationTest extends BaseEndpointTest {
    private final EndpointClient endpointClient = new EndpointClient();

    @Test
    @Story("Token при входе в систему")
    @DisplayName("Вход в систему с token из полного алфавита A-Z0-9 должен проходить по ТЗ")
    @Description("Совершаем вход в систему с token, в котором есть буквы G-Z. По ТЗ такой token должен приниматься.")
    void tokenWithLettersGToZShouldBeAcceptedBySpecification() {
        String token = "ABCDEFGHIJKLMNOPQRSTUV0123456789";

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));

        reportStep("Пользователь выполняет вход в систему с token, валидным по ТЗ");
        Response response = endpointClient.send(token, "LOGIN");
        reportStep("По ТЗ пользователь должен быть авторизован");

        assertAll(
                () -> assertEquals(200, response.statusCode(), response.asString()),
                () -> assertEquals("OK", response.jsonPath().getString("result"), response.asString())
        );

        reportStep("Вызов внешнего сервиса авторизации (/auth) должен быть отправлен");
        externalService.verify(postRequestedFor(urlEqualTo("/auth"))
                .withRequestBody(containing("token=" + token)));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(value = {
            "Вход в систему с некорректным token короче 32 символов отклоняется|ABCDEF0123456789ABCDEF012345678",
            "Вход в систему с некорректным token длиннее 32 символов отклоняется|ABCDEF0123456789ABCDEF01234567890",
            "Вход в систему с некорректным token со строчными буквами отклоняется|abcdef0123456789abcdef0123456789",
            "Вход в систему с некорректным token с неподдерживаемым символом отклоняется|ABCDEF0123456789ABCDEF012345678!"
    }, delimiter = '|')
    @Story("Token при входе в систему")
    @Description("Отправляем вход в систему с token неверного формата и ожидаем отказ.")
    void invalidTokenReturnsError(String checkName, String token) {
        Allure.getLifecycle().updateTestCase(testResult -> testResult.setName(checkName));

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));

        reportStep("Пользователь выполняет вход в систему: " + checkName);
        Response response = endpointClient.send(token, "LOGIN");

        reportStep("Пользователь получает отказ из-за невалидного token");
        assertError(response);

        reportStep("Вход в систему с невалидным token не отправлен во внешний сервис");
        externalService.verify(0, postRequestedFor(urlMatching(".*")));
    }

    @Test
    @Story("Значение действия")
    @DisplayName("Передача action=UNKNOWN отклоняется")
    @Description("Передаем action=UNKNOWN и ожидаем отказ без выполнения действия.")
    void unknownActionReturnsError() {
        String token = TestConfig.newValidToken();

        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));
        externalService.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(okJson("{}")));

        reportStep("Пользователь передает неподдерживаемое значение действия");
        Response response = endpointClient.send(token, "UNKNOWN");

        reportStep("Пользователь получает отказ из-за неподдерживаемого значения действия");
        assertError(response);

        reportStep("Действие с неподдерживаемым значением не отправлено во внешний сервис");
        externalService.verify(0, postRequestedFor(urlMatching(".*")));
    }

    @Test
    @Story("Доступ к сервису")
    @DisplayName("Вход в систему без API-ключа отклоняется")
    @Description("Пытаемся войти в систему без API-ключа и ожидаем отказ.")
    void missingApiKeyReturnsError() {
        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));

        reportStep("Клиент выполняет вход в систему без API-ключа");
        Response response = endpointClient.sendWithApiKey(null, TestConfig.newValidToken(), "LOGIN");

        reportStep("Клиент получает отказ в доступе");
        assertError(response);

        reportStep("Вход в систему без API-ключа не отправлен во внешний сервис");
        externalService.verify(0, postRequestedFor(urlMatching(".*")));
    }

    @Test
    @Story("Доступ к сервису")
    @DisplayName("Вход в систему с неверным API-ключом отклоняется")
    @Description("Пытаемся войти в систему с неверным API-ключом и ожидаем отказ.")
    void wrongApiKeyReturnsError() {
        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));

        reportStep("Клиент выполняет вход в систему с неверным API-ключом");
        Response response = endpointClient.sendWithApiKey("wrong-key", TestConfig.newValidToken(), "LOGIN");

        reportStep("Клиент получает отказ в доступе");
        assertError(response);

        reportStep("Вход в систему с неверным API-ключом не отправлен во внешний сервис");
        externalService.verify(0, postRequestedFor(urlMatching(".*")));
    }

    @Test
    @Story("Обязательные параметры")
    @DisplayName("Вход в систему без token отклоняется")
    @Description("Пытаемся войти в систему без token и ожидаем отказ.")
    void missingTokenReturnsError() {
        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));

        reportStep("Клиент выполняет вход в систему без token");
        Response response = endpointClient.sendWithApiKey(TestConfig.API_KEY, null, "LOGIN");

        reportStep("Клиент получает отказ из-за отсутствующего token");
        assertError(response);

        reportStep("Вход в систему без token не отправлен во внешний сервис");
        externalService.verify(0, postRequestedFor(urlMatching(".*")));
    }

    @Test
    @Story("Обязательные параметры")
    @DisplayName("Передача данных без параметра action отклоняется")
    @Description("Отправляем данные без параметра action и ожидаем отказ.")
    void missingActionReturnsError() {
        externalService.stubFor(post(urlEqualTo("/auth"))
                .willReturn(okJson("{}")));
        externalService.stubFor(post(urlEqualTo("/doAction"))
                .willReturn(okJson("{}")));

        reportStep("Клиент отправляет данные без значения действия");
        Response response = endpointClient.sendWithApiKey(TestConfig.API_KEY, TestConfig.newValidToken(), null);

        reportStep("Клиент получает отказ из-за отсутствующего действия");
        assertError(response);

        reportStep("Данные без значения действия не отправлены во внешний сервис");
        externalService.verify(0, postRequestedFor(urlMatching(".*")));
    }
}
