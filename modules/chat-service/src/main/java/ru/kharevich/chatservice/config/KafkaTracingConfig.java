package ru.kharevich.chatservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.kharevich.chatservice.dto.other.MessageTransferEntity;

@Configuration
public class KafkaTracingConfig {

    @Bean
    public KafkaTemplate<String, MessageTransferEntity> messageKafkaTemplate(
            ProducerFactory<String, MessageTransferEntity> producerFactory) {
        KafkaTemplate<String, MessageTransferEntity> template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true); // Включаем автоматическую передачу traceId
        return template;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageTransferEntity>
    messageKafkaListenerContainerFactory(
            ConsumerFactory<String, MessageTransferEntity> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, MessageTransferEntity> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setObservationEnabled(true); // Включаем обработку traceId
        return factory;
    }
}
