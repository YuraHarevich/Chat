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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.CHAT_WITH_ID_NOT_FOUND;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ChatControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private ChatRepository chatRepository;

    private final UUID testParticipant1 = UUID.randomUUID();
    private final UUID testParticipant2 = UUID.randomUUID();

    private String createdChatId;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void getAllChats_InvalidPageNumber_ReturnsBadRequest() {
        given()
                .queryParam("page_number", -1)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/chats")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getAllChats_InvalidSize_ReturnsBadRequest() {
        given()
                .queryParam("page_number", 0)
                .queryParam("size", 0)
                .when()
                .get("/api/v1/chats")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("size must be greater than 1"));
    }

    @Test
    void getMessagesByChatId_InvalidChatIdFormat_ReturnsBadRequest() {
        given()
                .queryParam("page_number", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/chats/invalid_id/messages")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void getMessagesByChatId_NegativePageNumber_ReturnsBadRequest() {
        given()
                .queryParam("page_number", -1)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/chats/507f1f77bcf86cd799439011/messages")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createChat_ValidRequest_ReturnsCreated() {
        ChatRequest request = new ChatRequest(Set.of(testParticipant1, testParticipant2));

        createdChatId =
                given()
                        .contentType(ContentType.JSON)
                        .body(request)
                        .when()
                        .post("/api/v1/chats")
                        .then()
                        .statusCode(HttpStatus.OK.value())
                        .body("participants", hasSize(2))
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
                .body("message", not(emptyString()));
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
                .body("message", not(emptyString()));
    }

    @Test
    void sendMessage_ValidRequest_ReturnsOk() {
        ChatRequest chatRequest = new ChatRequest(Set.of(testParticipant1, testParticipant2));
        String chatId =
                given()
                        .contentType(ContentType.JSON)
                        .body(chatRequest)
                        .when()
                        .post("/api/v1/chats")
                        .then()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .path("id");

        MessageRequest messageRequest = new MessageRequest("Hello", testParticipant1, testParticipant2, new ObjectId(chatId));

        given()
                .contentType(ContentType.JSON)
                .body(messageRequest)
                .when()
                .post("/api/v1/chats/" + chatId + "/send")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", notNullValue())
                .body("content", equalTo("Hello"));
    }

    @Test
    void sendMessage_EmptyContent_ReturnsBadRequest() {
        MessageRequest messageRequest = new MessageRequest("", testParticipant1, testParticipant2, new ObjectId("507f1f77bcf86cd799439011"));

        given()
                .contentType(ContentType.JSON)
                .body(messageRequest)
                .when()
                .post("/api/v1/chats/507f1f77bcf86cd799439011/send")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", not(emptyString()));
    }

    @Test
    void sendMessage_NullSender_ReturnsBadRequest() {
        String json = """
            {
              "content": "Hello",
              "sender": null,
              "receiver": "%s",
              "chatId": "%s"
            }
            """.formatted(testParticipant2, "507f1f77bcf86cd799439011");

        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/api/v1/chats/507f1f77bcf86cd799439011/send")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", not(emptyString()));
    }

    @Test
    void getMessagesByChatId_NonExistingChat_ReturnsNotFound() {
        given()
                .queryParam("page_number", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/chats/"+new ObjectId().toHexString()+"/messages")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .body("message", equalTo(CHAT_WITH_ID_NOT_FOUND));
    }

    @Test
    void sendMessage_InvalidJson_ReturnsBadRequest() {
        String invalidJson = "{invalid}";

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/api/v1/chats/507f1f77bcf86cd799439011/send")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void createChat_InvalidJson_ReturnsBadRequest() {
        String invalidJson = "{participants: [invalid]}";

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/api/v1/chats")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
