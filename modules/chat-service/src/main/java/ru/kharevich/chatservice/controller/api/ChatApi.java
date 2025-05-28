package ru.kharevich.chatservice.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;

public interface ChatApi {

    PageableResponse<ChatResponse> getAllChats(@RequestParam(defaultValue = "0") @Min(value = 0, message = "page number must be greater than 0") int page_number,
                                               @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be greater than 1") int size);

    PageableResponse<MessageResponse> getMessagesByChatId(@RequestParam(defaultValue = "0")
                                                          @Min(value = 0, message = "page number must be greater than 0") int page_number,
                                                          @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be greater than 1") int size,
                                                          @PathVariable @Valid ObjectId chatId);

    ChatResponse getChat(@PathVariable @Valid ObjectId id);

    ChatResponse createChat(@RequestBody @Valid ChatRequest chat);

    MessageResponse sendMessage(@PathVariable @Valid ObjectId chatId, @RequestBody @Valid MessageRequest messageRequest);

}
