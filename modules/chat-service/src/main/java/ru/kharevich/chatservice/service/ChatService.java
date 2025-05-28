package ru.kharevich.chatservice.service;


import jakarta.validation.constraints.Min;
import org.bson.types.ObjectId;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;

public interface ChatService {

    PageableResponse<ChatResponse> getAllChats(int size, int page);

    ChatResponse getChat(ObjectId id);

    ChatResponse createChat(ChatRequest chat);

    PageableResponse<MessageResponse> getMessagesByChatId(int size, @Min(0) int pageNumber, ObjectId chatId);

    MessageResponse sendMessage(ObjectId chatId, MessageRequest messageRequest);
}
