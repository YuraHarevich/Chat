package ru.kharevich.chatservice.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.service.ChatService;
import ru.kharevich.chatservice.utils.constants.RedisProperties;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MessagingWebSocketController {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper mapper;

    private final ChatService chatService;

    private final RedisProperties redisProperties;

    @MessageMapping("/chat.send.{sharedId}")
    @SendTo("/topic/chat.{sharedId}")
    @SneakyThrows
    public MessageRequest sendMessage(@DestinationVariable UUID sharedId, @Payload MessageRequest message) {
        String data = mapper.writeValueAsString(message);
        redisTemplate.convertAndSend(redisProperties.getChanel(), data);
        chatService.sendMessage(message);
        return message;
    }

}
