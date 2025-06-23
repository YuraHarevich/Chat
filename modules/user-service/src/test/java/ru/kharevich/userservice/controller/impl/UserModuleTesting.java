package ru.kharevich.userservice.controller.impl;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.kharevich.userservice.dto.events.UserEventTransferEntity;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.exceptions.UserModifyingException;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.repository.UserRepository;
import ru.kharevich.userservice.service.KeycloakUserService;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;
import static ru.kharevich.userservice.model.AccountStatus.DELETED;
import static ru.kharevich.userservice.model.AccountStatus.EXISTS;
import static ru.kharevich.userservice.model.AccountStatus.MODIFYING;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "keycloak.enabled=false" // Отключаем реальный Keycloak для тестов
})
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserModuleTesting {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @Container
    static PostgreSQLContainer<?> psqlContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17.4")
    );
    @LocalServerPort
    private int port;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private KafkaTemplate<String, UserEventTransferEntity> kafkaTemplate;
    @Mock
    private KeycloakUserService keycloakUserService;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", psqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", psqlContainer::getUsername);
        registry.add("spring.datasource.password", psqlContainer::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        userRepository.deleteAll();
        // Настройка моков для Keycloak
        when(keycloakUserService.sighIn(any())).thenReturn(new AccessTokenResponse());
    }

    // Вспомогательный метод для создания тестового пользователя
    private User createTestUser() {
        return User.builder()
                .username("testuser")
                .firstname("Test")
                .lastname("User")
                .email("test@example.com")
                .accountStatus(EXISTS)
                .build();
    }

    // ### ТЕСТЫ ###

    @Test
    void createUser_SuccessfulCreation_ShouldReturnCreated() {
        // Подготовка
        UserRequest request = new UserRequest(
                "newuser", "New", "User", "new@example.com", "password123");

        // Мокируем успешное создание в Keycloak
        when(keycloakUserService.createUser(any())).thenReturn(UUID.randomUUID());

        // Выполнение + Проверка
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // Проверка, что пользователь сохранен в БД
        assertFalse(userRepository.findByUsername("newuser").isEmpty());
    }

    @Test
    void createUser_KeycloakFails_ShouldCompensate() throws Exception {
        // Подготовка
        UserRequest request = new UserRequest(
                "failuser", "Fail", "User", "fail@example.com", "password123");

        // Мокируем ошибку в Keycloak
        when(keycloakUserService.createUser(any()))
                .thenThrow(new UserModifyingException("Keycloak error"));

        // Выполнение
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // Ждем обработки компенсационной транзакции
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(userRepository.findByUsername("failuser").get().getAccountStatus().equals(DELETED));
        });
    }

    @Test
    void updateUser_SuccessfulUpdate_ShouldReturnAccepted() {
        // Подготовка
        User existingUser = userRepository.save(createTestUser());
        UserRequest request = new UserRequest(
                "updateduser", "Updated", "User", "updated@example.com", "newpassword");

        // Выполнение + Проверка
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/v1/users/" + existingUser.getId())
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        // Проверка обновления в БД
        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertEquals("updateduser", updatedUser.getUsername());
    }

    @Test
    void deleteUser_SuccessfulDeletion_ShouldReturnNoContent() {
        // Подготовка
        User existingUser = userRepository.save(createTestUser());

        // Выполнение + Проверка
        given()
                .when()
                .delete("/api/v1/users/" + existingUser.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Проверка статуса в БД
        User deletedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertEquals(DELETED, deletedUser.getAccountStatus());
    }

    @Test
    void deleteUser_KeycloakFails_ShouldCompensate() {
        // Подготовка
        User existingUser = userRepository.save(createTestUser());

        // Мокируем ошибку в Keycloak
        doThrow(new UserModifyingException("Keycloak error"))
                .when(keycloakUserService).deleteUser(anyString());

        // Выполнение
        given()
                .when()
                .delete("/api/v1/users/" + existingUser.getId())
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());

        // Ждем компенсации
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            User user = userRepository.findById(existingUser.getId()).orElseThrow();
            assertEquals(EXISTS, user.getAccountStatus()); // Проверяем восстановление
        });
    }

    @Test
    void recoverAccount_SuccessfulRecovery_ShouldReturnAccepted() {
        // Подготовка
        User existingUser = createTestUser();
        existingUser.setAccountStatus(DELETED);
        existingUser = userRepository.save(existingUser);

        AccountRecoverRequest request = new AccountRecoverRequest(
                existingUser.getId(), "newpassword");

        // Выполнение + Проверка
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/api/v1/users/recover")
                .then()
                .statusCode(HttpStatus.ACCEPTED.value());

        // Проверка статуса в БД
        User recoveredUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertEquals(MODIFYING, recoveredUser.getAccountStatus());
    }
}
