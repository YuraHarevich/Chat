package ru.kharevich.chatservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import ru.kharevich.chatservice.utils.converter.StringToUuidConverter;
import ru.kharevich.chatservice.utils.converter.UuidToStringConverter;

import java.util.Arrays;

@Configuration
@ConditionalOnProperty(
        name = "spring.profiles.active",
        havingValue = "!prod",
        matchIfMissing = true
)
public class MongoConfig {

    @Bean
    public MongoCustomConversions uuidToStringConverter() {
        return new MongoCustomConversions(Arrays.asList(
                new UuidToStringConverter(),
                new StringToUuidConverter()
        ));
    }
}