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

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageEntityMessageConsumer {

    private final SentMessageProcessService sentMessageProcessService;
    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;

    @KafkaListener(topics = "message-topic", groupId = "message-group")
    public void consumeSupplyRequests(
            @Payload MessageTransferEntity msg,
            @Header(name = "traceparent", required = false) String traceparent,
            @Header(name = "spanId", required = false) String spanId
    ) {
        Observation.createNotStarted("kafka.consume", observationRegistry)
                .contextualName("kafka.consume.message")
                .lowCardinalityKeyValue("kafka.topic", "message-topic")
                .lowCardinalityKeyValue("kafka.group", "message-group")
                .observe(() -> {
                    Span span = tracer.nextSpan()
                            .name("kafka.consume.process")
                            .tag("kafka.topic", "message-topic")
                            .tag("kafka.groupId", "message-group");

                    // Используем try-with-resources для SpanInScope
                    try (Tracer.SpanInScope scope = tracer.withSpan(span)) {
                        span.start();
                        log.info("Processing message with traceId: {}", span.context().traceId());
                        log.debug("Parent spanId: {}", spanId);

                        sentMessageProcessService.processSentMessage(msg);

                        log.debug("Message processed successfully");
                    } catch (Exception e) {
                        span.error(e);
                        log.error("Error processing message", e);
                        throw e;
                    } finally {
                        span.end();
                    }
                });
    }
}