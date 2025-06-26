package ru.kharevich.chatservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kharevich.chatservice.dto.other.MessageTransferEntity;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.FrontChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;
import ru.kharevich.chatservice.exception.ChatNotFoundException;
import ru.kharevich.chatservice.external.reponse.UserResponse;
import ru.kharevich.chatservice.feign.UserFeignClient;
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
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;

    private final ChatMapper chatMapper;

    private final PageMapper pageMapper;

    private final ChatServiceValidationService chatServiceValidationService;

    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    private final MessageEntityMessageProducer messageEntityMessageProducer;

    private final UserFeignClient userFeignClient;

    @Cacheable(value = "chats", key = "{#size, #pageNumber}")
    public PageableResponse<ChatResponse> getAllChats(int size, int pageNumber) {
        Page<Chat> chatPage = chatRepository.findAll(PageRequest.of(pageNumber, size));
        Page<ChatResponse> convertedToResponseChatPage = chatPage.map(chatMapper::toResponse);
        return pageMapper.toResponse(convertedToResponseChatPage);
    }

    @Cacheable(value = "chat", key = "#id")
    public ChatResponse getChat(ObjectId id) {
        Optional<Chat> chatOptional = chatRepository.findById(id);
        Chat chat = chatOptional.orElseThrow(() ->
                new ChatNotFoundException(CHAT_WITH_ID_NOT_FOUND.formatted(id)));
        return chatMapper.toResponse(chat);
    }

    @Transactional
    @CacheEvict(value = {"chats", "userChats"}, allEntries = true)
    public ChatResponse createChat(ChatRequest chatRequest) {
        chatServiceValidationService.validateIfThrowsUsersNotFound(chatRequest.participants());
        Chat chat = chatMapper.toEntity(chatRequest);

        UUID chatSharedId = UUID.randomUUID();
        chat.setSharedId(chatSharedId);
        Chat individualChat = null;
        for (UUID participant : chatRequest.participants()) {
            log.info("creating chat for participant: {}", participant);
            //saving chat instance for each participant
            individualChat = chat.clone();
            individualChat.setOwner(participant);
            chatRepository.save(individualChat);
        }

        return chatMapper.toResponse(individualChat);
    }

    @Cacheable(value = "chatMessages", key = "{#chatId, #pageNumber, #size}")
    public PageableResponse<MessageResponse> getMessagesByUniqueChatId(int size, int pageNumber, ObjectId chatId) {
        chatServiceValidationService.validateIfThrowsChatNotFoundByChatId(chatId);

        Page<Message> messagePage = messageRepository.findByChatIdOrderBySentTimeDesc(chatId, PageRequest.of(pageNumber, size));
        Page<MessageResponse> convertedToResponseMessagePage = messagePage.map(messageMapper::toResponse);
        return pageMapper.toResponse(convertedToResponseMessagePage);
    }

    @Cacheable(value = "userChats", key = "{#username, #pageNumber, #size}")
    public PageableResponse<FrontChatResponse> getAllChatsByUsername(String username, int size, int pageNumber) {
        UserResponse userResponse = userFeignClient.getUserByUsernameIfExists(username);
        UUID userId = userResponse.id();

        Page<Chat> chats = chatRepository.findByOwner(userId, PageRequest.of(pageNumber, size));
        Page<FrontChatResponse> convertedToResponseChatPage = chats.map((chat -> {
            Set<UUID> participants = chat.getParticipants();
            participants.remove(userId);
            String chatUserName = null;
            if (participants.iterator().hasNext()) {
                chatUserName = userFeignClient.getUserIfExists(participants.iterator().next()).username();
            }
            return chatMapper.toFrontChatResponse(chat, chatUserName);
        }));
        return pageMapper.toResponse(convertedToResponseChatPage);
    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "chat", key = "#result.id"),
                    @CacheEvict(value = "chatMessages", key = "{#result.sharedId, #result.sender}")
            }
    )
    public MessageResponse sendMessage(MessageRequest messageRequest) {
        log.info("ChatServiceImpl.sendMessage: sending message: {}", messageRequest);
        chatServiceValidationService.validateIfThrowsChatNotFoundBySharedChatId(messageRequest.sharedId());
        chatServiceValidationService.validateIfThrowsUsersNotFound(Set.of(messageRequest.sender()));

        ObjectId chatId = chatRepository.findBySharedIdAndOwner(messageRequest.sharedId(), messageRequest.sender()).get().getId();

        Message msg = messageMapper.toEntity(messageRequest, chatId);
        msg.setStatus(SENT);
        messageRepository.save(msg);
        messageEntityMessageProducer.sendOrderRequest(
                new MessageTransferEntity(msg.getChatId().toHexString(), msg.getId().toHexString()));
        return messageMapper.toResponse(msg);
    }

}
