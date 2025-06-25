package ru.kharevich.chatservice.redispubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.kharevich.chatservice.model.Message;

@RequiredArgsConstructor
@Slf4j
public class RedisPubsubReceiver {

    private final SimpMessagingTemplate template;
    private final ObjectMapper mapper;

    @SneakyThrows
    public void receiveMessage(String message) {
        Message chatMessage = mapper.readValue(message, Message.class);
        log.info("RedisPubsubReceiver.receiveMessage:Converted to: {}", chatMessage);
        template.convertAndSend("/topic/public", chatMessage);
    }

}