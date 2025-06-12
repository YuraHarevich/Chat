package ru.kharevich.chatservice.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.request.MessageRequestWebSocket;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.FrontChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;

import java.util.UUID;

public interface ChatApi {

    PageableResponse<ChatResponse> getAllChats(@Min(value = 0, message = "page number must be greater than 0") int page_number,
                                               @Min(value = 1, message = "size must be greater than 1") int size);

    public PageableResponse<FrontChatResponse> getAllChatsByUsername(@RequestParam(defaultValue = "0") @Min(value = 0, message = "page number must be greater than 0") int page_number,
                                                                     @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be greater than 1") int size,
                                                                     String username);

    PageableResponse<MessageResponse> getMessagesBySharedChatIdAndOwnerId(@Min(value = 0, message = "page number must be greater than 0") int page_number,
                                                                          @Min(value = 1, message = "size must be greater than 1") int size,
                                                                          @Valid UUID sharedChatId,
                                                                          @Valid UUID ownerId);

    public PageableResponse<MessageResponse> getMessagesByUniqueChatId(@Min(value = 0, message = "page number must be greater than 0") int page_number,
                                                                       @Min(value = 1, message = "size must be greater than 1") int size,
                                                                       @Valid ObjectId chatId);

    ChatResponse getChatByUniqueId(@Valid ObjectId id);

    ChatResponse createChat(@Valid ChatRequest chat);

    MessageResponse sendMessage(@Valid MessageRequest messageRequest);

    @PostMapping("/send-message")
    @ResponseStatus(HttpStatus.ACCEPTED)
    MessageResponse sendMessage(@RequestBody @Valid MessageRequestWebSocket messageRequest);
}
