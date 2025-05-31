package ru.kharevich.chatservice.kafka.producer;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
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
import ru.kharevich.chatservice.dto.request.MessageRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageEntityMessageProducer {

    private final KafkaTemplate<String, MessageTransferEntity> kafkaTemplate;
    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;

    @Value("${spring.kafka.topic.message}")
    private String topic;

    public void sendOrderRequest(MessageTransferEntity msg) {
        Observation.createNotStarted("kafka.produce", observationRegistry)
                .contextualName("kafka.send.message")
                .lowCardinalityKeyValue("kafka.topic", topic)
                .observe(() -> {
                    Span span = tracer.nextSpan()
                            .name("kafka.produce.message")
                            .tag("kafka.topic", topic);

                    try (Tracer.SpanInScope scope = tracer.withSpan(span)) {
                        span.start();

                        Message<MessageTransferEntity> message = MessageBuilder
                                .withPayload(msg)
                                .setHeader(KafkaHeaders.TOPIC, topic)
                                .setHeader("traceparent", span.context().traceId())
                                .setHeader("spanId", span.context().spanId())
                                .build();

                        log.info("Sending message with traceId: {}", span.context().traceId());

                        kafkaTemplate.send(message);
                    } finally {
                        span.end();
                    }
                });
    }
}