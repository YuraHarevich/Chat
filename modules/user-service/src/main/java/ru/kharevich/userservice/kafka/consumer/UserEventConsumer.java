package ru.kharevich.userservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.kharevich.userservice.dto.events.UserEventTransferEntity;
import ru.kharevich.userservice.service.SagaEventHandlerService;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final SagaEventHandlerService eventHandlerService;

    @KafkaListener(topics = "${spring.kafka.topic.user}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSupplyRequests(UserEventTransferEntity msg) {
        log.info("UserEventConsumer.consumeSupplyRequests: Consuming message");
        eventHandlerService.handleEvent(msg);
    }

}