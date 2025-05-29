//package ru.kharevich.chatservice.service.impl;
//
//import org.bson.types.ObjectId;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import ru.kharevich.chatservice.dto.request.ChatRequest;
//import ru.kharevich.chatservice.dto.request.MessageRequest;
//import ru.kharevich.chatservice.dto.response.ChatResponse;
//import ru.kharevich.chatservice.dto.response.MessageResponse;
//import ru.kharevich.chatservice.dto.response.PageableResponse;
//import ru.kharevich.chatservice.kafka.producer.MessageEntityMessageProducer;
//import ru.kharevich.chatservice.model.Chat;
//import ru.kharevich.chatservice.model.Message;
//import ru.kharevich.chatservice.repository.ChatRepository;
//import ru.kharevich.chatservice.repository.MessageRepository;
//import ru.kharevich.chatservice.utils.mapper.ChatMapper;
//import ru.kharevich.chatservice.utils.mapper.MessageMapper;
//import ru.kharevich.chatservice.utils.mapper.PageMapper;
//import ru.kharevich.chatservice.utils.validation.ChatServiceValidationService;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class ChatServiceTest {
//
//    private final ObjectId existingChatId = new ObjectId();
//    private final ObjectId nonExistingChatId = new ObjectId();
//    private final UUID testOwnerId = UUID.randomUUID();
//
//    @Mock
//    private ChatRepository chatRepository;
//    @Mock
//    private MessageEntityMessageProducer messageEntityMessageProducer;
//    @Mock
//    private ChatMapper chatMapper;
//    @Mock
//    private PageMapper pageMapper;
//    @Mock
//    private MessageRepository messageRepository;
//    @Mock
//    private MessageMapper messageMapper;
//    @Mock
//    private ChatServiceValidationService chatServiceValidationService;
//    @InjectMocks
//    private ChatServiceImpl chatService;
//
//    @Test
//    void getAllChats_WithValidPagination_ReturnsPageOfChats() {
//        // Arrange
//        int page = 0;
//        int size = 10;
//        Chat chat = TestDataFactory.createChat(existingChatId);
//        Page<Chat> mockPage = new PageImpl<>(List.of(chat));
//
//        when(chatRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
//        when(chatMapper.toResponse(chat)).thenReturn(
//                new ChatResponse(existingChatId.toHexString(), chat.getSharedId(), chat.getParticipants())
//        );
//        when(pageMapper.toResponse(any())).thenReturn(new PageableResponse<>(1, 0, 1, 1, List.of()));
//
//        // Act
//        PageableResponse<ChatResponse> result = chatService.getAllChats(size, page);
//
//        // Assert
//        assertNotNull(result);
//        verify(chatRepository).findAll(PageRequest.of(page, size));
//    }
//
//    @Test
//    void getChat_WithExistingId_ReturnsChatResponse() {
//        // Arrange
//        Chat chat = TestDataFactory.createChat(existingChatId);
//        when(chatRepository.findById(existingChatId)).thenReturn(Optional.of(chat));
//        when(chatMapper.toResponse(chat)).thenReturn(
//                new ChatResponse(existingChatId.toHexString(), chat.getSharedId(), chat.getParticipants())
//        );
//
//        // Act
//        ChatResponse result = chatService.getChat(existingChatId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(existingChatId.toHexString(), result.id());
//    }
//
//    @Test
//    void createChat_WithValidRequest_ReturnsCreatedChat() {
//        // Arrange
//        ChatRequest request = TestDataFactory.createValidChatRequest();
//        Chat chat = TestDataFactory.createChat(existingChatId);
//        when(chatMapper.toEntity(request)).thenReturn(chat);
//        when(chatRepository.save(chat)).thenReturn(chat);
//        when(chatMapper.toResponse(chat)).thenReturn(
//                new ChatResponse(existingChatId.toHexString(), UUID.randomUUID(), request.participants())
//        );
//
//        // Act
//        ChatResponse result = chatService.createChat(request);
//
//        // Assert
//        assertNotNull(result);
//    }
//
//    @Test
//    void getMessagesByChatId_WithExistingChat_ReturnsMessages() {
//        // Arrange
//        int page = 0;
//        int size = 10;
//        Message message = TestDataFactory.createMessage(existingChatId);
//        Page<Message> mockPage = new PageImpl<>(List.of(message));
//
//        when(messageRepository.findByChatIdOrderBySentTimeDesc(eq(existingChatId), any(Pageable.class)))
//                .thenReturn(mockPage);
//        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse(
//                message.getId().toHexString(),
//                message.getContent(),
//                message.getSender(),
//                message.getChatId()
//        ));
//        when(pageMapper.toResponse(any())).thenReturn(new PageableResponse<>(1, 0, 1, 1, List.of()));
//
//        // Act
//        PageableResponse<MessageResponse> result = chatService.getMessagesByUniqueChatId(size, page, existingChatId);
//
//        // Assert
//        assertNotNull(result);
//        verify(messageRepository).findByChatIdOrderBySentTimeDesc(existingChatId, PageRequest.of(page, size));
//    }
//
//    @Test
//    void sendMessage_WithValidRequest_ReturnsMessageResponse() {
//        // Arrange
//        MessageRequest request = TestDataFactory.createValidMessageRequest(existingChatId);
//        Message message = TestDataFactory.createMessage(existingChatId);
//
//        when(messageMapper.toEntity(request, existingChatId)).thenReturn(message);
//        when(messageRepository.save(message)).thenReturn(message);
//        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse(
//                message.getId().toHexString(),
//                message.getContent(),
//                message.getSender(),
//                message.getChatId()
//        ));
//
//        // Act
//        MessageResponse result = chatService.sendMessage(request);
//
//        // Assert
//        assertNotNull(result);
//    }
//}