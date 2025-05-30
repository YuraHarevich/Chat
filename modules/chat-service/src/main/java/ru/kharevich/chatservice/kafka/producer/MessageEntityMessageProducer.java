package ru.kharevich.chatservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.dto.other.MessageTransferEntity;
import ru.kharevich.chatservice.dto.request.MessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageEntityMessageProducer {

    private final KafkaTemplate<String, MessageTransferEntity> kafkaTemplate;

    @Value("${spring.kafka.topic.message}")
    private String topic;

    public void sendOrderRequest(MessageTransferEntity msg){
        log.info("MessageEntityMessageProducer.Sending message from sender");
        Message<MessageTransferEntity> message = MessageBuilder
                .withPayload(msg)
                .setHeader(KafkaHeaders.TOPIC,topic)
                .build();
        kafkaTemplate.send(message);
    }

}