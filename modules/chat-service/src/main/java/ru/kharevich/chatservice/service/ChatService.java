package ru.kharevich.chatservice.service;

import org.bson.types.ObjectId;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;

import java.util.UUID;

public interface ChatService {
    PageableResponse<ChatResponse> getAllChats(int size, int pageNumber);

    ChatResponse getChat(ObjectId id);

    ChatResponse createChat(ChatRequest chat);

    MessageResponse sendMessage(MessageRequest messageRequest);

    PageableResponse<MessageResponse> getMessagesByUniqueChatId(int size, int pageNumber, ObjectId chatId);

    PageableResponse<MessageResponse> getMessagesBySharedChatIdAndOwnerId(int size, int pageNumber, UUID sharedChatId, UUID ownerId);
}
