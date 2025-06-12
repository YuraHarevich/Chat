package ru.kharevich.chatservice.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import ru.kharevich.chatservice.controller.api.MessagingWebSocketControllerApi;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.dto.request.MessageRequestWebSocket;
import ru.kharevich.chatservice.service.ChatService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class MessagingWebSocketController implements MessagingWebSocketControllerApi {

    private final ChatService chatService;

    @Controller
    public class ChatController {

        @MessageMapping("/chat.send.{sharedId}")
        @SendTo("/topic/chat.{sharedId}")
        public MessageRequest sendMessage(@DestinationVariable UUID sharedId, @Payload MessageRequest message) {
            chatService.sendMessage(message);
            return message;
        }
    }


}
