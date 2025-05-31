package ru.kharevich.chatservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.restassured.RestAssured;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.model.Message;
import ru.kharevich.chatservice.model.MessageStatus;
import ru.kharevich.chatservice.repository.ChatRepository;
import ru.kharevich.chatservice.repository.MessageRepository;
import ru.kharevich.chatservice.service.ChatService;
import ru.kharevich.chatservice.utils.mapper.ChatMapper;
import ru.kharevich.chatservice.utils.mapper.MessageMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class ChatServiceModuleTests {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatService chatService;
    @Autowired
    private ChatMapper chatMapper;
    @Autowired
    private MessageMapper messageMapper;

    @LocalServerPort
    private int port;

    private UUID participant1;
    private UUID participant2;
    private UUID sharedChatId;
    private ObjectId chatIdParticipant1;
    private ObjectId chatIdParticipant2;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        participant1 = UUID.randomUUID();
        participant2 = UUID.randomUUID();

        ChatRequest chatRequest = new ChatRequest(Set.of(participant1, participant2));
        ChatResponse chatResponse = chatService.createChat(chatRequest);
        sharedChatId = chatResponse.sharedId();

        chatIdParticipant1 = chatRepository.findBySharedIdAndOwner(sharedChatId, participant1)
                .orElseThrow().getId();
        chatIdParticipant2 = chatRepository.findBySharedIdAndOwner(sharedChatId, participant2)
                .orElseThrow().getId();
    }

    @Test
    @DisplayName("sendMessage_ValidRequest_ShouldSaveMessageAndSendToKafka")
    void sendMessage_ValidRequest_ShouldSaveMessageAndSendToKafka() {
        String content = "Test message";
        MessageRequest request = new MessageRequest(content, participant1, sharedChatId);

        // 1. Отправляем сообщение
        MessageResponse response = chatService.sendMessage(request);

        // 2. Проверяем сохранение у отправителя
        assertNotNull(response.id());
        assertEquals(content, response.content());
        assertEquals(participant1, response.sender());
        assertEquals(chatIdParticipant1, response.chatId());

        Message senderMessage = messageRepository.findById(new ObjectId(response.id())).orElseThrow();
        assertEquals(content, senderMessage.getContent());
        assertEquals(participant1, senderMessage.getSender());
        assertEquals(chatIdParticipant1, senderMessage.getChatId());
        assertEquals(MessageStatus.SENT, senderMessage.getStatus());
        assertNotNull(senderMessage.getSentTime());

        // 3. Ждём обработки сообщения Kafka и сохранения у получателя
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            // Проверяем, что статус у отправителя изменился
            Message updatedSenderMessage = messageRepository.findById(new ObjectId(response.id())).orElseThrow();

            // Проверяем наличие сообщения у получателя
            Page<Message> receiverMessagesPage = messageRepository.findByChatIdOrderBySentTimeDesc(
                    chatIdParticipant2,
                    PageRequest.of(0, 10)
            );
            assertFalse(receiverMessagesPage.getContent().isEmpty(), "Сообщение не найдено у получателя");

            Message receiverMessage = receiverMessagesPage.getContent().get(0);
            assertEquals(content, receiverMessage.getContent());
            assertEquals(participant1, receiverMessage.getSender());
            assertEquals(chatIdParticipant2, receiverMessage.getChatId());
            assertEquals(MessageStatus.RECEIVED, receiverMessage.getStatus());
            assertNotNull(receiverMessage.getSentTime());
        });
    }
}
