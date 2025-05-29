package ru.kharevich.chatservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;
import ru.kharevich.chatservice.exception.ChatNotFoundException;
import ru.kharevich.chatservice.kafka.producer.MessageEntityMessageProducer;
import ru.kharevich.chatservice.model.Chat;
import ru.kharevich.chatservice.model.Message;
import ru.kharevich.chatservice.repository.ChatRepository;
import ru.kharevich.chatservice.repository.MessageRepository;
import ru.kharevich.chatservice.service.ChatService;
import ru.kharevich.chatservice.utils.mapper.ChatMapper;
import ru.kharevich.chatservice.utils.mapper.MessageMapper;
import ru.kharevich.chatservice.utils.mapper.PageMapper;
import ru.kharevich.chatservice.utils.validation.ChatServiceValidationService;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static ru.kharevich.chatservice.model.MessageStatus.SENT;
import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.CHAT_WITH_ID_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    private final ChatMapper chatMapper;

    private final PageMapper pageMapper;

    private final ChatServiceValidationService chatServiceValidationService;

    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    private final MessageEntityMessageProducer messageEntityMessageProducer;

    public PageableResponse<ChatResponse> getAllChats(int size, int pageNumber) {
        Page<Chat> chatPage = chatRepository.findAll(PageRequest.of(pageNumber, size));
        Page<ChatResponse> convertedToResponseChatPage = chatPage.map(chatMapper::toResponse);
        return pageMapper.toResponse(convertedToResponseChatPage);
    }

    public ChatResponse getChat(ObjectId id) {
        Optional<Chat> chatOptional = chatRepository.findById(id);
        Chat chat = chatOptional.orElseThrow(() ->
                new ChatNotFoundException(CHAT_WITH_ID_NOT_FOUND.formatted(id)));
        return chatMapper.toResponse(chat);
    }

    public ChatResponse createChat(ChatRequest chatRequest) {
        chatServiceValidationService.validateIfThrowsUsersNotFound(chatRequest.participants());
        Chat chat = chatMapper.toEntity(chatRequest);

        UUID chatSharedId = UUID.randomUUID();
        chat.setSharedId(chatSharedId);

        for(UUID participant: chatRequest.participants()){
            //saving chat instance for each participant
            chat.setOwner(participant);
            chatRepository.save(chat);
        }

        return chatMapper.toResponse(chat);
    }

    public PageableResponse<MessageResponse> getMessagesByUniqueChatId(int size, int pageNumber, ObjectId chatId) {
        chatServiceValidationService.validateIfThrowsChatNotFoundByChatId(chatId);

        Page<Message> messagePage = messageRepository.findByChatIdOrderBySentTimeDesc(chatId, PageRequest.of(pageNumber, size));
        Page<MessageResponse> convertedToResponseMessagePage = messagePage.map(messageMapper::toResponse);
        return pageMapper.toResponse(convertedToResponseMessagePage);
    }

    public PageableResponse<MessageResponse> getMessagesBySharedChatIdAndOwnerId(int size, int pageNumber, ObjectId chatId, UUID ownerId) {
        chatServiceValidationService.validateIfThrowsChatNotFoundByChatId(chatId);

        Page<Message> messagePage = messageRepository.findByChatIdOrderBySentTimeDesc(chatId, PageRequest.of(pageNumber, size));
        Page<MessageResponse> convertedToResponseMessagePage = messagePage.map(messageMapper::toResponse);
        return pageMapper.toResponse(convertedToResponseMessagePage);
    }

    public MessageResponse sendMessage(MessageRequest messageRequest) {
        chatServiceValidationService.validateIfThrowsChatNotFoundBySharedChatId(messageRequest.sharedId());
        chatServiceValidationService.validateIfThrowsUsersNotFound(Set.of(messageRequest.sender()));

        ObjectId chatId = chatRepository.findBySharedIdAndOwner(messageRequest.sharedId(),messageRequest.owner()).getId();

        Message msg = messageMapper.toEntity(messageRequest, chatId);
        msg.setStatus(SENT);
        messageRepository.save(msg);
        messageEntityMessageProducer.sendOrderRequest(messageRequest);
        return messageMapper.toResponse(msg);
    }

}
