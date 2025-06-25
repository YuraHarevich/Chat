package ru.kharevich.chatservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.kharevich.chatservice.redispubsub.RedisPubsubReceiver;
import ru.kharevich.chatservice.utils.constants.RedisProperties;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final SimpMessagingTemplate template;
    private final ObjectMapper mapper;
    private final RedisConnectionFactory connectionFactory;
    private final RedisProperties redisProperties;

    @Bean
    public RedisPubsubReceiver redisPubsubReceiver() {
        return new RedisPubsubReceiver(template, mapper);
    }

    @Bean
    public MessageListenerAdapter redisPubsubListenerAdapter() {
        return new MessageListenerAdapter(redisPubsubReceiver(), "receiveMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisPubsubContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(redisPubsubListenerAdapter(), new PatternTopic(redisProperties.getChanel()));
        return container;
    }

}
