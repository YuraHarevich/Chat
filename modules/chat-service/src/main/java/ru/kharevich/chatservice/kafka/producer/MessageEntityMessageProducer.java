package ru.kharevich.chatservice.kafka.producer;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.dto.other.MessageTransferEntity;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageEntityMessageProducer {

    private final KafkaTemplate<String, MessageTransferEntity> kafkaTemplate;
    private final Tracer tracer;
    @Value("${spring.kafka.topic.message}")
    private String topic;

    public void sendOrderRequest(MessageTransferEntity msg) {
        Message<MessageTransferEntity> message = null;

        if (getCurrentTraceparent().isPresent()) {
            message = MessageBuilder
                    .withPayload(msg)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .removeHeader("traceparent")
                    .setHeader("traceparent", getCurrentTraceparent())
                    .build();
        } else {
            message = MessageBuilder
                    .withPayload(msg)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .build();
        }

        kafkaTemplate.send(message);

    }

    private Optional<String> getCurrentTraceparent() {
        TraceContext context = tracer.currentTraceContext().context();
        if (context != null) {
            return Optional.of(String.format("00-%s-%s-00", context.traceId(), context.spanId()));
        } else {
            return Optional.empty();
        }
    }

}