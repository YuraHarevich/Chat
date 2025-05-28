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
import org.springframework.data.domain.Pageable;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;
import ru.kharevich.chatservice.exception.ChatNotFoundException;
import ru.kharevich.chatservice.model.Chat;
import ru.kharevich.chatservice.model.Message;
import ru.kharevich.chatservice.repository.ChatRepository;
import ru.kharevich.chatservice.repository.MessageRepository;
import ru.kharevich.chatservice.utils.fabrics.TestDataFactory;
import ru.kharevich.chatservice.utils.mapper.ChatMapper;
import ru.kharevich.chatservice.utils.mapper.MessageMapper;
import ru.kharevich.chatservice.utils.mapper.PageMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatMapper chatMapper;

    @Mock
    private PageMapper pageMapper;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    private final ObjectId existingChatId = new ObjectId();
    private final ObjectId nonExistingChatId = new ObjectId();

    // Тесты для getAllChats
    @Test
    void getAllChats_WithValidPagination_ReturnsPageOfChats() {
        // Arrange
        int page = 0;
        int size = 10;
        Page<Chat> mockPage = new PageImpl<>(List.of(TestDataFactory.createChat(existingChatId)));
        when(chatRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(chatMapper.toResponse(any())).thenReturn(new ChatResponse(existingChatId.toHexString(), Set.of()));
        when(pageMapper.toResponse(any())).thenReturn(new PageableResponse<>(1, 0, 1, 1, List.of()));

        // Act
        PageableResponse<ChatResponse> result = chatService.getAllChats(size, page);

        // Assert
        assertNotNull(result);
        verify(chatRepository).findAll(PageRequest.of(page, size));
    }

    // Тесты для getChat
    @Test
    void getChat_WithExistingId_ReturnsChatResponse() {
        // Arrange
        Chat chat = TestDataFactory.createChat(existingChatId);
        when(chatRepository.findById(existingChatId)).thenReturn(Optional.of(chat));
        when(chatMapper.toResponse(chat)).thenReturn(new ChatResponse(existingChatId.toHexString(), chat.getParticipants()));

        // Act
        ChatResponse result = chatService.getChat(existingChatId);

        // Assert
        assertNotNull(result);
        assertEquals(existingChatId.toHexString(), result.id());
    }

    @Test
    void getChat_WithNonExistingId_ThrowsNotFoundException() {
        // Arrange
        when(chatRepository.findById(nonExistingChatId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ChatNotFoundException.class, () -> chatService.getChat(nonExistingChatId));
    }

    // Тесты для createChat
    @Test
    void createChat_WithValidRequest_ReturnsCreatedChat() {
        // Arrange
        ChatRequest request = TestDataFactory.createValidChatRequest();
        Chat chat = TestDataFactory.createChat(existingChatId);
        when(chatMapper.toEntity(request)).thenReturn(chat);
        when(chatRepository.save(chat)).thenReturn(chat);
        when(chatMapper.toResponse(chat)).thenReturn(new ChatResponse(existingChatId.toHexString(), request.participants()));

        // Act
        ChatResponse result = chatService.createChat(request);

        // Assert
        assertNotNull(result);
        assertEquals(request.participants(), result.participants());
    }
    // Тесты для getMessagesByChatId
    @Test
    void getMessagesByChatId_WithExistingChat_ReturnsMessages() {
        // Arrange
        int page = 0;
        int size = 10;
        Message message = TestDataFactory.createMessage(existingChatId);
        Page<Message> mockPage = new PageImpl<>(List.of(message));

        when(messageRepository.findByChatIdOrderBySentTimeDesc(eq(existingChatId), any(Pageable.class)))
                .thenReturn(mockPage);
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse(
                message.getId().toHexString(),
                message.getContent(),
                message.getSender(),
                message.getReceiver(),
                message.getChatId()
        ));
        when(pageMapper.toResponse(any())).thenReturn(new PageableResponse<>(1, 0, 1, 1, List.of()));

        // Act
        PageableResponse<MessageResponse> result = chatService.getMessagesByChatId(size, page, existingChatId);

        // Assert
        assertNotNull(result);
        verify(messageRepository).findByChatIdOrderBySentTimeDesc(existingChatId, PageRequest.of(page, size));
    }

    @Test
    void getMessagesByChatId_WithNonExistingChat_ReturnsEmptyPage() {
        // Arrange
        int page = 0;
        int size = 10;
        when(messageRepository.findByChatIdOrderBySentTimeDesc(eq(nonExistingChatId), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(pageMapper.toResponse(any())).thenReturn(new PageableResponse<>(0, 0, 0, 0, List.of()));

        // Act
        PageableResponse<MessageResponse> result = chatService.getMessagesByChatId(size, page, nonExistingChatId);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.totalElements());
    }

    // Тесты для sendMessage
    @Test
    void sendMessage_WithValidRequest_ReturnsMessageResponse() {
        // Arrange
        MessageRequest request = TestDataFactory.createValidMessageRequest(existingChatId);
        Message message = TestDataFactory.createMessage(existingChatId);

        when(messageMapper.toEntity(request, existingChatId)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse(
                message.getId().toHexString(),
                message.getContent(),
                message.getSender(),
                message.getReceiver(),
                message.getChatId()
        ));

        // Act
        MessageResponse result = chatService.sendMessage(existingChatId, request);

        // Assert
        assertNotNull(result);
    }

}