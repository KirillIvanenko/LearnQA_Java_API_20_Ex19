package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lib.BaseCaseTest;
import lib.Assertions;

import lib.ApiCoreRequests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Epic("Authorization cases")
@Feature("Authorization")
public class UserAuthTest extends BaseCaseTest{
    String cookie;
    String header;
    Integer userIdOnAuth;
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void loginUser () {
        Map<String, String> authorizationDate = new HashMap<>();
        authorizationDate.put("email", "vinkotov@example.com");
        authorizationDate.put("password", "1234");

        Response responseAuthorization = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authorizationDate);

        this.cookie = this.getCookie(responseAuthorization, "auth_sid");
        this.header = this.getHeader(responseAuthorization, "x-csrf-token");
        this.userIdOnAuth = this.getIntFromJson(responseAuthorization, "user_id");
    }

    @Test
    @Description("This is a test successfully authorization of the user by email and password")
    @DisplayName("Positive test for authorization")
    public void authorizationTest() {

        Response responseCheckAuth = apiCoreRequests
                .makeGetRequest(
                        "https://playground.learnqa.ru/api/user/auth",
                        this.header,
                        this.cookie
                );
        Assertions.asserJsonByName(responseCheckAuth, "user_id", this.userIdOnAuth);

    }
    @Description("This are test for authorization without of sending auth cookie and token")
    @DisplayName("Negative test for authorization")
    @ParameterizedTest
    @ValueSource(strings = {"cookie", "headers"})
    public void testNegativeAuthUser(String condition) {

        if (condition.equals("cookie")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithCookie(
                    "https://playground.learnqa.ru/api/user/auth",
                    this.cookie
            );
            Assertions.asserJsonByName(responseForCheck, "user_id", 0);
        } else if (condition.equals("headers")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithToken(
                    "https://playground.learnqa.ru/api/user/auth",
                    this.header
            );
            Assertions.asserJsonByName(responseForCheck, "user_id", 0);
        } else {
            throw new IllegalArgumentException("Condition value is not known: " + condition);
        }
    }
}
