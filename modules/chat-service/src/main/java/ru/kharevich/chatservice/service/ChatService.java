package ru.kharevich.chatservice.service;

import jakarta.validation.constraints.Min;
import org.bson.types.ObjectId;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.FrontChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;

import java.util.UUID;

public interface ChatService {

    PageableResponse<ChatResponse> getAllChats(int size, int pageNumber);

    ChatResponse getChat(ObjectId id);

    ChatResponse createChat(ChatRequest chat);

    MessageResponse sendMessage(MessageRequest messageRequest);

    PageableResponse<MessageResponse> getMessagesByUniqueChatId(int size, int pageNumber, ObjectId chatId);

    PageableResponse<FrontChatResponse> getAllChatsByUsername(String username, @Min(value = 1, message = "size must be greater than 1") int size, @Min(value = 0, message = "page number must be greater than 0") int pageNumber);

}
