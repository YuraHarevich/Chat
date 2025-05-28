package ru.kharevich.chatservice.controller.impl;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;
import ru.kharevich.chatservice.controller.api.ChatApi;
import ru.kharevich.chatservice.dto.request.ChatRequest;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.response.MessageResponse;
import ru.kharevich.chatservice.dto.response.ChatResponse;
import ru.kharevich.chatservice.dto.response.PageableResponse;
import ru.kharevich.chatservice.service.ChatService;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController implements ChatApi {

    private final ChatService chatService;

    @GetMapping
    public PageableResponse<ChatResponse> getAllChats(@RequestParam(defaultValue = "0") @Min(0) int page_number,
                                                      @RequestParam(defaultValue = "10") int size) {
        PageableResponse<ChatResponse> chats = chatService.getAllChats(size, page_number);
        return chats;
    }

    @GetMapping("{chatId}/messages")
    public PageableResponse<MessageResponse> getMessagesByChatId(@RequestParam(defaultValue = "0") @Min(0) int page_number,
                                                                 @RequestParam(defaultValue = "10") int size,
                                                                 @PathVariable ObjectId chatId) {
        PageableResponse<MessageResponse> chats = chatService.getMessagesByChatId(size, page_number, chatId);
        return chats;
    }

    @GetMapping("/{id}")
    public ChatResponse getChat(@PathVariable ObjectId id) {
        return chatService.getChat(id);
    }

    @PostMapping
    public ChatResponse createChat(@RequestBody ChatRequest chat) {
        return chatService.createChat(chat);
    }

    @PostMapping("{chatId}/send")
    public MessageResponse sendMessage(@PathVariable ObjectId chatId, @RequestBody MessageRequest messageRequest){
        return chatService.sendMessage(chatId, messageRequest);
    }

}
