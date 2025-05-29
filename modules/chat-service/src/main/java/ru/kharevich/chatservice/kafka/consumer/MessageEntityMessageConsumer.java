package ru.kharevich.chatservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.dto.request.MessageRequest;

import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageEntityMessageConsumer {

    @Value("${spring.kafka.topic.message}")
    private String orderTopic;

    private final KafkaTemplate<String, MessageRequest> kafkaTemplate;

    @KafkaListener(topics = "message-topic",groupId = "message-group")
    public void consumeSupplyRequests(MessageRequest queueProceedRequest) {
        log.info("MessageEntityMessageConsumer.Consuming message");
    }

}