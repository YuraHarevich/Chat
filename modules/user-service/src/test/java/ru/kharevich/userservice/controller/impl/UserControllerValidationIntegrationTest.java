package ru.kharevich.userservice.controller.impl;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.SignInRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerValidationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.4"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", psqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", psqlContainer::getUsername);
        registry.add("spring.datasource.password", psqlContainer::getPassword);
    }

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    // UserRequest validation tests
    @Test
    void createUser_WithNullUsername_ReturnBadRequest() {
        UserRequest invalidRequest = new UserRequest(
                null, // invalid username
                "John",
                "Doe",
                "john@example.com",
                "password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("username cannot be null"));
    }

    @Test
    void createUser_WithNullFirstname_ReturnBadRequest() {
        UserRequest invalidRequest = new UserRequest(
                "johndoe",
                null, // invalid firstname
                "Doe",
                "john@example.com",
                "password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("firstname cannot be null"));
    }

    @Test
    void createUser_WithNullLastname_ReturnBadRequest() {
        UserRequest invalidRequest = new UserRequest(
                "johndoe",
                "John",
                null, // invalid lastname
                "john@example.com",
                "password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("lastname cannot be null"));
    }

    @Test
    void createUser_WithInvalidEmail_ReturnBadRequest() {
        UserRequest invalidRequest = new UserRequest(
                "johndoe",
                "John",
                "Doe",
                "not-an-email", // invalid email
                "password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("email is not valid"));
    }

    @Test
    void createUser_WithNullPassword_ReturnBadRequest() {
        UserRequest invalidRequest = new UserRequest(
                "johndoe",
                "John",
                "Doe",
                "john@example.com",
                null // invalid password
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("password cannot be null"));
    }

    @Test
    void updateUser_WithInvalidEmail_ReturnBadRequest() {
        UUID userId = UUID.randomUUID();
        UserRequest invalidRequest = new UserRequest(
                "johndoe",
                "John",
                "Doe",
                "not-an-email", // invalid email
                "password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .patch("/api/v1/users/" + userId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("email is not valid"));
    }

    // SignInRequest validation tests
    @Test
    void signIn_WithNullUsername_ReturnBadRequest() {
        SignInRequest invalidRequest = new SignInRequest(
                null, // invalid username
                "password123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .get("/api/v1/users/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("username cannot be null"));
    }

    @Test
    void signIn_WithNullPassword_ReturnBadRequest() {
        SignInRequest invalidRequest = new SignInRequest(
                "johndoe",
                null // invalid password
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .get("/api/v1/users/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("password cannot be null"));
    }

    // AccountRecoverRequest validation tests
    @Test
    void recoverAccount_WithNullId_ReturnBadRequest() {
        AccountRecoverRequest invalidRequest = new AccountRecoverRequest(
                null, // invalid id
                "newpassword123"
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .patch("/api/v1/users/recover")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("id cannot be null"));
    }

    @Test
    void recoverAccount_WithNullPassword_ReturnBadRequest() {
        AccountRecoverRequest invalidRequest = new AccountRecoverRequest(
                UUID.randomUUID(),
                null // invalid password
        );

        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .patch("/api/v1/users/recover")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("password cannot be null"));
    }

    // Pagination validation tests
    @Test
    void getAllUsers_WithNegativePageNumber_ReturnBadRequest() {
        given()
                .queryParam("page_number", -1)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("page number must be greater than 0"));
    }

    @Test
    void getAllUsers_WithZeroSize_ReturnBadRequest() {
        given()
                .queryParam("page_number", 0)
                .queryParam("size", 0)
                .when()
                .get("/api/v1/users")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("size must be greater than 1"));
    }
}
