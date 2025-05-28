package ru.kharevich.chatservice.service;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;
import ru.kharevich.chatservice.exception.ChatNotFoundException;

import java.util.Optional;
import java.util.Set;

public interface ChatService {
    public PageableResponse<ChatResponse> getAllChats(int size, int pageNumber);

    public ChatResponse getChat(ObjectId id);

    public ChatResponse createChat(ChatRequest chat);

    public PageableResponse<MessageResponse> getMessagesByChatId(int size, int pageNumber, ObjectId chatId);

    public MessageResponse sendMessage(ObjectId chatId, MessageRequest messageRequest);
}
