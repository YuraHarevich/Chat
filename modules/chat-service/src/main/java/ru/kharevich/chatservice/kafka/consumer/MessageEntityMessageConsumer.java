package ru.kharevich.chatservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.dto.other.MessageTransferEntity;
import ru.kharevich.chatservice.dto.request.MessageRequest;
import ru.kharevich.chatservice.service.SentMessageProcessService;

import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageEntityMessageConsumer {

    private final SentMessageProcessService sentMessageProcessService;

    @KafkaListener(topics = "message-topic",groupId = "message-group")
    public void consumeSupplyRequests(MessageTransferEntity msg) {
        log.info("MessageEntityMessageConsumer.Consuming message");
        sentMessageProcessService.processSentMessage(msg);
    }

}