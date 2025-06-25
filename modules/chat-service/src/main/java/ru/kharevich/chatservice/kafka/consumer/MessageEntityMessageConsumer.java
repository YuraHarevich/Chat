package ru.kharevich.chatservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.dto.other.MessageTransferEntity;
import ru.kharevich.chatservice.service.SentMessageProcessService;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageEntityMessageConsumer {

    private final SentMessageProcessService sentMessageProcessService;

    @KafkaListener(topics = "message-topic", groupId = "message-group")
    public void consumeSupplyRequests(MessageTransferEntity msg) {
        log.info("MessageEntityMessageConsumer.Consuming message");
        sentMessageProcessService.processSentMessage(msg);
    }

}