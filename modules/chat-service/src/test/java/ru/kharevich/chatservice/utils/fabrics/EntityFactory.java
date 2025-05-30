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

public class EntityFactory {

    public static Chat createChat(UUID sharedId, UUID ownerId, Set<UUID> participants) {
        Chat chat = new Chat();
        chat.setId(new ObjectId());
        chat.setSharedId(sharedId);
        chat.setOwner(ownerId);
        chat.setParticipants(participants);
        chat.setCreationTime(LocalDateTime.now());
        return chat;
    }

    public static Message createMessage(ObjectId chatId, UUID sender) {
        Message message = new Message();
        message.setId(new ObjectId());
        message.setChatId(chatId);
        message.setSender(sender);
        message.setStatus(MessageStatus.SENT);
        message.setSentTime(LocalDateTime.now());
        message.setContent("Hello");
        return message;
    }

    public static ChatRequest createChatRequest(Set<UUID> participants) {
        return new ChatRequest(participants);
    }

    public static MessageRequest createMessageRequest(String content, UUID sender, UUID sharedId) {
        return new MessageRequest(content, sender, sharedId);
    }
}
