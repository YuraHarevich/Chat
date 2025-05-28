package ru.kharevich.chatservice.controller.impl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kharevich.chatservice.controller.api.ChatApi;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;
import ru.kharevich.chatservice.service.ChatService;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Validated
public class ChatController implements ChatApi {

    private final ChatService chatService;

    @GetMapping
    public PageableResponse<ChatResponse> getAllChats(@RequestParam(defaultValue = "0") @Min(value = 0, message = "page number must be greater than 0") int page_number,
                                                      @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be greater than 1") int size) {
        size = size > 50 ? 50 : size;
        PageableResponse<ChatResponse> chats = chatService.getAllChats(size, page_number);
        return chats;
    }

    @GetMapping("{chatId}/messages")
    public PageableResponse<MessageResponse> getMessagesByChatId(@RequestParam(defaultValue = "0")
                                                                 @Min(value = 0, message = "page number must be greater than 0") int page_number,
                                                                 @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be greater than 1") int size,
                                                                 @PathVariable @Valid ObjectId chatId) {
        size = size > 50 ? 50 : size;
        PageableResponse<MessageResponse> chats = chatService.getMessagesByChatId(size, page_number, chatId);
        return chats;
    }

    @GetMapping("/{id}")
    public ChatResponse getChat(@PathVariable @Valid ObjectId id) {
        return chatService.getChat(id);
    }

    @PostMapping
    public ChatResponse createChat(@RequestBody @Valid ChatRequest chat) {
        return chatService.createChat(chat);
    }

    @PostMapping("{chatId}/send")
    public MessageResponse sendMessage(@PathVariable @Valid ObjectId chatId, @RequestBody @Valid MessageRequest messageRequest) {
        return chatService.sendMessage(chatId, messageRequest);
    }

}
