package ru.kharevich.chatservice.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;

import java.util.UUID;

public interface ChatApi {

    PageableResponse<ChatResponse> getAllChats(@Min(value = 0, message = "page number must be greater than 0") int page_number,
                                               @Min(value = 1, message = "size must be greater than 1") int size);

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

}
