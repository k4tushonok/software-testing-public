package org.itmo.testing.lab2.integration;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab2.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;
    private int port = 7000;

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест регистрации пользователя")
    void testUserRegistration() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(2)
    @DisplayName("Тест регистрации существующего пользователя")
    void testRegisterUserDuplicate() {
        given()
                .queryParam("userId", "user2")
                .queryParam("userName", "Bob")
                .when()
                .post("/register")
                .then()
                .statusCode(200);

        given()
                .queryParam("userId", "user2")
                .queryParam("userName", "Bob")
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(containsString("User already exists"));
    }

    @Test
    @Order(3)
    @DisplayName("Тест регистрации пользователя с пустыми параметрам")
    void testRegisterUserMissingParameters() {
        given()
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(4)
    @DisplayName("Тест записи сессии")
    void testRecordSession() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(5)
    @DisplayName("Тест записи сессии с пустыми параметрами")
    void testRecordSessionMissingParameters() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "2023-03-01T10:00:00")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(6)
    @DisplayName("Тест записи сессии для несуществующего пользователя")
    void testRecordSessionNonexistentUser() {
        given()
                .queryParam("userId", "user0")
                .queryParam("loginTime", "2023-03-01T10:00:00")
                .queryParam("logoutTime", "2023-03-01T12:00:00")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("User not found"));
    }

    @Test
    @Order(7)
    @DisplayName("Тест записи дублирующей сессии")
    void testRecordSessionDuplicate() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "2023-01-01T10:00:00")
                .queryParam("logoutTime", "2023-01-01T12:00:00")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(containsString("Session recorded"));

        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "2023-01-01T10:00:00")
                .queryParam("logoutTime", "2023-01-01T12:00:00")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Session already recorded with these dates"));
    }

    @Test
    @Order(8)
    @DisplayName("Тест записи сессии с невалидными датами")
    void testRecordSessionInvalidDate() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "2023-0")
                .queryParam("logoutTime", "2023-03-01T12:00:00")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data:"));
    }

    @Test
    @Order(9)
    @DisplayName("Тест записи сессии с будующими датами")
    void testRecordSessionFutureDate() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "2027-03-01T10:00:00")
                .queryParam("logoutTime", "2027-03-01T12:00:00")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Login time must be before the future"));
    }

    @Test
    @Order(10)
    @DisplayName("Тест записи сессии где выход раньше входа")
    void testRecordSessionLogoutBeforeLogin() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "2025-03-01T14:00:00")
                .queryParam("logoutTime", "2025-03-01T12:00:00")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Login time must before logout time"));
    }

    @Test
    @Order(11)
    @DisplayName("Тест получения общего времени активности")
    void testGetTotalActivity() {
        given()
                .queryParam("userId", "user1")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(200)
                .body(containsString("Total activity:"))
                .body(containsString("minutes"));
    }

    @Test
    @Order(12)
    @DisplayName("Тест получения общего времени активности для несуществующего пользователя")
    void testGetTotalActivityNonexistentUser() {
        given()
                .queryParam("userId", "nonexistentUser")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing userId"));
    }

    @Test
    @Order(13)
    @DisplayName("Тест получения общего времени активности с пустыми параметрами")
    void testGetTotalActivityMissingParameters() {
        given()
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing userId"));
    }

    @Test
    @Order(14)
    @DisplayName("Тест получения общего времени активности без сессии")
    void testGetTotalActivityMissingSessions() {
        given()
                .queryParam("userId", "user2")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(containsString("No sessions found for user"));
    }

    @Test
    @Order(15)
    @DisplayName("Тест получения общего времени активности")
    public void testInactiveUsers() {
        given()
                .queryParam("days", "30")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .contentType("application/json");
    }

    @Test
    @Order(16)
    @DisplayName("Тест получения общего времени активности без параметров")
    public void testInactiveUsersMissingParameters() {
        given()
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(containsString("Missing days parameter"));
    }

    @Test
    @Order(17)
    @DisplayName("Тест получения общего времени активности с отрицательным количеством дней")
    public void testInactiveUsersNegativeDays() {
        given()
                .queryParam("days", "-30")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(containsString("Invalid number format for days"));
    }

    @Test
    @Order(18)
    @DisplayName("Тест получения общего времени активности с невалидным форматом дней")
    public void testInactiveUsersInvalidDays() {
        given()
                .queryParam("days", "a")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(containsString("Invalid number format for days"));
    }

    @Test
    @Order(19)
    @DisplayName("Тест проверки активности в течении заданного месяца")
    public void testMonthlyActivity() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "2023-01")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body("2023-01-01", notNullValue());
    }

    @Test
    @Order(20)
    @DisplayName("Тест проверки активности в течении заданного месяца с пустыми параметрами")
    public void testMonthlyActivityMissingParameters() {
        given()
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(equalTo("Missing parameters"));
    }

    @Test
    @Order(20)
    @DisplayName("Тест проверки активности в течении заданного месяца с невалидным месяцем")
    public void testMonthlyActivityInvalidMonth() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "2023-13")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data:"));
    }

    @Test
    @Order(21)
    @DisplayName("Тест проверки активности в течении заданного месяца с несуществующим пользователем")
    public void testMonthlyActivityNonexistentUser() {
        given()
                .queryParam("userId", "user0")
                .queryParam("month", "2023-03")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data:"))
                .body(containsString("No sessions found for user"));
    }

    @Test
    @Order(22)
    @DisplayName("Тест проверки активности в течении заданного месяца с пустыми сессиями")
    public void testMonthlyActivityNullSessions() {
        given()
                .queryParam("userId", "user1")
                .queryParam("month", "2023-04")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }
}
