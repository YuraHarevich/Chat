package ru.kharevich.chatservice.kafka.consumer;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.tracing.Tracer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
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

    private Tracer tracer;

    private ObservationRegistry observationRegistry;

    private final KafkaTemplate<String, MessageTransferEntity> kafkaTemplate;

    @KafkaListener(topics = "message-topic",groupId = "message-group")
    public void consumeSupplyRequests(MessageTransferEntity msg) {
        Observation.createNotStarted("kafka.consume", observationRegistry)
                .observe(() -> {
                    log.info("MessageEntityMessageConsumer.Consuming message");
                    sentMessageProcessService.processSentMessage(msg);
                });
    }

}