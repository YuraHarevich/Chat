package ru.kharevich.chatservice.service.impl;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import ru.kharevich.chatservice.dto.other.MessageTransferEntity;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;
import ru.kharevich.chatservice.kafka.producer.MessageEntityMessageProducer;
import ru.kharevich.chatservice.model.Chat;
import ru.kharevich.chatservice.model.Message;
import ru.kharevich.chatservice.model.MessageStatus;
import ru.kharevich.chatservice.repository.ChatRepository;
import ru.kharevich.chatservice.repository.MessageRepository;
import ru.kharevich.chatservice.utils.fabrics.EntityFactory;
import ru.kharevich.chatservice.utils.mapper.ChatMapper;
import ru.kharevich.chatservice.utils.mapper.MessageMapper;
import ru.kharevich.chatservice.utils.mapper.PageMapper;
import ru.kharevich.chatservice.utils.validation.ChatServiceValidationService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ChatServiceUnitTest {

    @Mock private ChatRepository chatRepository;
    @Mock private ChatMapper chatMapper;
    @Mock private PageMapper pageMapper;
    @Mock private ChatServiceValidationService validationService;
    @Mock private MessageRepository messageRepository;
    @Mock private MessageMapper messageMapper;
    @Mock private MessageEntityMessageProducer messageProducer;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void getAllChats_ValidPagination_ReturnsPageableResponse() {
        Page<Chat> chatPage = new PageImpl<>(List.of(new Chat()));
        Page<ChatResponse> chatResponsePage = new PageImpl<>(List.of(mock(ChatResponse.class)));

        when(chatRepository.findAll(any(PageRequest.class))).thenReturn(chatPage);
        when(chatMapper.toResponse(any(Chat.class))).thenReturn(chatResponsePage.getContent().get(0));
        when(pageMapper.toResponse(chatResponsePage)).thenReturn(new PageableResponse<>(1, 1, 0, 1, chatResponsePage.getContent()));

        PageableResponse<ChatResponse> result = chatService.getAllChats(1, 0);

        assertEquals(1, result.totalElements());
    }

    @Test
    void getChat_ExistingId_ReturnsChatResponse() {
        Chat chat = new Chat();
        chat.setId(new ObjectId());
        when(chatRepository.findById(any())).thenReturn(Optional.of(chat));
        when(chatMapper.toResponse(chat)).thenReturn(mock(ChatResponse.class));

        ChatResponse result = chatService.getChat(chat.getId());

        assertNotNull(result);
    }

    @Test
    void createChat_ValidRequest_SavesChatsForEachParticipant() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        Set<UUID> participants = Set.of(user1, user2);

        ChatRequest request = EntityFactory.createChatRequest(participants);
        Chat chat = EntityFactory.createChat(null, null, participants);
        ChatResponse response = mock(ChatResponse.class);

        when(chatMapper.toEntity(request)).thenReturn(chat);
        when(chatRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMapper.toResponse(any())).thenReturn(response);

        ChatResponse result = chatService.createChat(request);

        assertNotNull(result);
        verify(chatRepository, times(participants.size())).save(any());
    }

    @Test
    void getMessagesByUniqueChatId_ExistingChatId_ReturnsMessages() {
        ObjectId chatId = new ObjectId();
        Message message = EntityFactory.createMessage(chatId, UUID.randomUUID());

        Page<Message> messagePage = new PageImpl<>(List.of(message));
        Page<MessageResponse> responsePage = new PageImpl<>(List.of(mock(MessageResponse.class)));

        when(messageRepository.findByChatIdOrderBySentTimeDesc(eq(chatId), any(PageRequest.class))).thenReturn(messagePage);
        when(messageMapper.toResponse(any())).thenReturn(responsePage.getContent().get(0));
        when(pageMapper.toResponse(responsePage)).thenReturn(new PageableResponse<>(1, 1, 0, 1, responsePage.getContent()));

        PageableResponse<MessageResponse> result = chatService.getMessagesByUniqueChatId(10, 0, chatId);

        assertEquals(1, result.totalElements());
    }

    @Test
    void getMessagesBySharedChatIdAndOwnerId_ExistingChat_ReturnsMessages() {
        UUID sharedId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        ObjectId chatId = new ObjectId();

        Chat chat = EntityFactory.createChat(sharedId, ownerId, Set.of(ownerId));
        chat.setId(chatId);
        when(chatRepository.findBySharedIdAndOwner(sharedId, ownerId)).thenReturn(Optional.of(chat));

        Page<Message> messagePage = new PageImpl<>(List.of(EntityFactory.createMessage(chatId, ownerId)));
        Page<MessageResponse> responsePage = new PageImpl<>(List.of(mock(MessageResponse.class)));

        when(messageRepository.findByChatIdOrderBySentTimeDesc(eq(chatId), any(PageRequest.class))).thenReturn(messagePage);
        when(messageMapper.toResponse(any())).thenReturn(responsePage.getContent().get(0));
        when(pageMapper.toResponse(responsePage)).thenReturn(new PageableResponse<>(1, 1, 0, 1, responsePage.getContent()));

        PageableResponse<MessageResponse> result = chatService.getMessagesBySharedChatIdAndOwnerId(10, 0, sharedId, ownerId);

        assertEquals(1, result.totalElements());
    }

    @Test
    void sendMessage_ValidMessage_SavesAndProducesMessage() {
        UUID sender = UUID.randomUUID();
        UUID sharedId = UUID.randomUUID();
        ObjectId chatId = new ObjectId();

        MessageRequest request = EntityFactory.createMessageRequest("Hello", sender, sharedId);
        Chat chat = EntityFactory.createChat(sharedId, sender, Set.of(sender));
        chat.setId(chatId);

        Message message = EntityFactory.createMessage(chatId, sender);
        message.setStatus(MessageStatus.SENT);

        when(chatRepository.findBySharedIdAndOwner(sharedId, sender)).thenReturn(Optional.of(chat));
        when(messageMapper.toEntity(request, chatId)).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(mock(MessageResponse.class));

        MessageResponse result = chatService.sendMessage(request);

        assertNotNull(result);
        verify(messageRepository).save(message);
        verify(messageProducer).sendOrderRequest(any(MessageTransferEntity.class));
    }
}
