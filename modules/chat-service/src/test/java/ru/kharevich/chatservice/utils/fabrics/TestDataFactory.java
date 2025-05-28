package ru.kharevich.chatservice.utils.fabrics;

import org.bson.types.ObjectId;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.model.Chat;
import ru.kharevich.chatservice.model.Message;
import ru.kharevich.chatservice.model.MessageStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class TestDataFactory {
    public static ChatRequest createValidChatRequest() {
        return new ChatRequest(Set.of(UUID.randomUUID(), UUID.randomUUID()));
    }

    public static ChatRequest createInvalidChatRequest() {
        return new ChatRequest(null); // Некорректный запрос
    }

    public static MessageRequest createValidMessageRequest(ObjectId chatId) {
        return new MessageRequest(
                "Test message",
                UUID.randomUUID(),
                UUID.randomUUID(),
                chatId
        );
    }

    public static MessageRequest createInvalidMessageRequest() {
        return new MessageRequest(
                "", // Пустое сообщение
                null, // Отсутствует отправитель
                UUID.randomUUID(),
                new ObjectId()
        );
    }

    public static Chat createChat(ObjectId id) {
        Chat chat = new Chat();
        chat.setId(id);
        chat.setParticipants(Set.of(UUID.randomUUID()));
        chat.setCreationTime(LocalDateTime.now());
        return chat;
    }

    public static Message createMessage(ObjectId chatId) {
        Message message = new Message();
        message.setId(new ObjectId());
        message.setContent("Test");
        message.setSender(UUID.randomUUID());
        message.setReceiver(UUID.randomUUID());
        message.setChatId(chatId);
        message.setStatus(MessageStatus.SENT);
        message.setSentTime(LocalDateTime.now());
        return message;
    }
}
