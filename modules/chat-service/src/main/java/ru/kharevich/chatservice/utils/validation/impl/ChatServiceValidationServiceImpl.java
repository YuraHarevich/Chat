package ru.kharevich.chatservice.utils.validation.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.exception.ChatNotFoundException;
import ru.kharevich.chatservice.feign.UserFeignClient;
import ru.kharevich.chatservice.repository.ChatRepository;
import ru.kharevich.chatservice.utils.validation.ChatServiceValidationService;

import java.util.Set;
import java.util.UUID;

import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.CHAT_WITH_ID_NOT_FOUND;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatServiceValidationServiceImpl implements ChatServiceValidationService {

    private final ChatRepository chatRepository;

    private final UserFeignClient userFeignClient;

    @Override
    public void validateIfThrowsUsersNotFound(Set<UUID> participants) {
        for(UUID participant : participants) {
            userFeignClient.getUserIfExists(participant);
        }
    }

    @Override
    public void validateIfThrowsChatNotFoundByChatId(ObjectId chatId) {
        chatRepository.findById(chatId).orElseThrow(() -> {
            log.error("Chat with id {} not found", chatId);
            return new ChatNotFoundException(CHAT_WITH_ID_NOT_FOUND.formatted(chatId));
        });
    }

    @Override
    public void validateIfThrowsChatNotFoundBySharedChatId(UUID sharedChatId) {
        if(chatRepository.findBySharedId(sharedChatId).isEmpty()) {
            log.error("Chat with shared id {} not found", sharedChatId);
            throw new ChatNotFoundException(CHAT_WITH_ID_NOT_FOUND.formatted(sharedChatId));
        }
    }

}
