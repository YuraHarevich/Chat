package ru.kharevich.chatservice.controller.impl;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.repository.ChatRepository;

import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.CHAT_WITH_ID_NOT_FOUND;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatControllerValidationIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    private final UUID testParticipant1 = UUID.randomUUID();
    private final UUID testParticipant2 = UUID.randomUUID();
    private final UUID testOwner = UUID.randomUUID();
    private final UUID testSharedId = UUID.randomUUID();

    @LocalServerPort
    private int port;

    @Autowired
    private ChatRepository chatRepository;

    private String createdChatId;

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    // Тесты для создания чата
    @Test
    void createChat_ValidRequest_ReturnsCreated() {
        ChatRequest request = new ChatRequest(
                Set.of(testParticipant1, testParticipant2)
        );

        createdChatId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/chats")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("participants", hasSize(2))
                .body("sharedId", notNullValue())
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    @Test
    void createChat_EmptyParticipants_ReturnsBadRequest() {
        ChatRequest request = new ChatRequest(Set.of());

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/chats")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("There must be at least 2 participants"));
    }

    @Test
    void createChat_NullParticipants_ReturnsBadRequest() {
        String invalidJson = "{\"participants\": null}";

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/api/v1/chats")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("Participants cannot be null"));
    }

    // Тесты для отправки сообщений
    @Test
    void sendMessage_ValidRequest_ReturnsOk() {
        // Сначала создаем чат
        ChatRequest chatRequest = new ChatRequest(
                Set.of(testParticipant1, testParticipant2)
        );
        String chatId = given()
                .contentType(ContentType.JSON)
                .body(chatRequest)
                .when()
                .post("/api/v1/chats")
                .then()
                .extract()
                .path("id");

        UUID sharedId = UUID.fromString(
                given()
                        .when()
                        .get("/api/v1/chats/" + chatId)
                        .then()
                        .extract()
                        .path("sharedId"));

        // Затем отправляем сообщение
        MessageRequest messageRequest = new MessageRequest(
                "Hello",
                testParticipant1,
                sharedId
        );

        given()
                .contentType(ContentType.JSON)
                .body(messageRequest)
                .when()
                .post("/api/v1/chats/send-message")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("content", equalTo("Hello"))
                .body("sender", equalTo(testParticipant1.toString()));
    }

    @Test
    void sendMessage_EmptyContent_ReturnsBadRequest() {
        MessageRequest messageRequest = new MessageRequest(
                "",
                testParticipant1,
                testSharedId
        );

        given()
                .contentType(ContentType.JSON)
                .body(messageRequest)
                .when()
                .post("/api/v1/chats/send-message")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("Message content must not be blank"));
    }

    @Test
    void sendMessage_NullSender_ReturnsBadRequest() {
        String invalidJson = """
                {
                  "content": "Hello",
                  "sender": null,
                  "sharedId": "%s",
                  "owner": "%s"
                }
                """.formatted(testSharedId, testOwner);

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/api/v1/chats/send-message")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("Sender ID must not be null"));
    }

    // Тесты для получения чатов
    @Test
    void getChatByUniqueId_ExistingChat_ReturnsChat() {
        // Создаем чат
        ChatRequest request = new ChatRequest(Set.of(testParticipant1, testParticipant2));
        String chatId = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/chats")
                .then()
                .extract()
                .path("id");

        // Получаем чат
        given()
                .when()
                .get("/api/v1/chats/" + chatId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo(chatId))
                .body("participants", hasSize(2));
    }

    @Test
    void getChatByUniqueId_NonExistingChat_ReturnsNotFound() {
        String id = new ObjectId().toHexString();
        given()
                .when()
                .get("/api/v1/chats/" + id)
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", equalTo(CHAT_WITH_ID_NOT_FOUND.formatted(id)));
    }

    // Тесты валидации параметров запроса
    @Test
    void getAllChats_InvalidPageNumber_ReturnsBadRequest() {
        given()
                .queryParam("page_number", -1)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/chats")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("page number must be greater than 0"));
    }

    @Test
    void getMessages_InvalidSize_ReturnsBadRequest() {
        given()
                .queryParam("page_number", 0)
                .queryParam("size", 0)
                .when()
                .get("/api/v1/chats/507f1f77bcf86cd799439011/messages")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("size must be greater than 1"));
    }
}