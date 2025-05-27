package ru.kharevich.chatservice.controller.api;

import jakarta.validation.constraints.Min;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;

public interface ChatApi {

    public PageableResponse<ChatResponse> getAllChats(@RequestParam(defaultValue = "0") @Min(0) int page_number,
                                                      @RequestParam(defaultValue = "10") int size);

    public PageableResponse<MessageResponse> getMessagesByChatId(@RequestParam(defaultValue = "0") @Min(0) int page_number,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @PathVariable ObjectId chatId);

    public ChatResponse getChat(@PathVariable ObjectId id);

    public ChatResponse createChat(@RequestBody ChatRequest chat);

    public MessageResponse sendMessage(@PathVariable ObjectId chatId, @RequestBody MessageRequest messageRequest);

}
