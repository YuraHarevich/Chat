package ru.kharevich.chatservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;
import ru.kharevich.chatservice.model.Message;
import ru.kharevich.chatservice.model.MessageStatus;
import ru.kharevich.chatservice.repository.ChatRepository;
import ru.kharevich.chatservice.model.Chat;
import ru.kharevich.chatservice.repository.MessageRepository;
import ru.kharevich.chatservice.service.ChatService;
import ru.kharevich.chatservice.utils.mapper.ChatMapper;
import ru.kharevich.chatservice.utils.mapper.MessageMapper;
import ru.kharevich.chatservice.utils.mapper.PageMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    private final ChatMapper chatMapper;

    private final PageMapper pageMapper;

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public PageableResponse<ChatResponse> getAllChats(int size, int pageNumber) {
        Page<Chat> chatPage = chatRepository.findAll(PageRequest.of(pageNumber, size));
        Page<ChatResponse> convertedToResponseChatPage = chatPage.map(chatMapper::toResponse);
        return pageMapper.toResponse(convertedToResponseChatPage);
    }

    public ChatResponse getChat(ObjectId id) {
        Optional<Chat> chatOptional = chatRepository.findById(id);
        Chat chat = chatOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
        return chatMapper.toResponse(chat);
    }

    public ChatResponse createChat(ChatRequest chat) {
        return chatMapper.toResponse(chatRepository.save(chatMapper.toEntity(chat)));
    }

    public PageableResponse<MessageResponse> getMessagesByChatId(int size, int pageNumber, ObjectId chatId) {
        Page<Message> messagePage = messageRepository.findByChatIdOrderBySentTimeDesc(chatId, PageRequest.of(pageNumber,size));
        Page<MessageResponse> convertedToResponseMessagePage = messagePage.map(messageMapper::toResponse);
        return pageMapper.toResponse(convertedToResponseMessagePage);

    }

    public MessageResponse sendMessage(ObjectId chatId, MessageRequest messageRequest) {
        Message msg = messageMapper.toEntity(messageRequest, chatId);
        msg.setStatus(MessageStatus.SENT);
        messageRepository.save(msg);
        return messageMapper.toResponse(msg);
    }

}
