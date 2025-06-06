package ru.kharevich.chatservice.utils.validation.impl;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import ru.kharevich.chatservice.exception.ChatNotFoundException;
import ru.kharevich.chatservice.repository.ChatRepository;
import ru.kharevich.chatservice.utils.validation.ChatServiceValidationService;

import java.util.Set;
import java.util.UUID;

import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.CHAT_WITH_ID_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ChatServiceValidationServiceImpl implements ChatServiceValidationService {

    private final ChatRepository chatRepository;

    @Override
    public void validateIfThrowsUsersNotFound(Set<UUID> participants) {
        //todo отправить запрос в user service
    }

    @Override
    public void validateIfThrowsChatNotFoundByChatId(ObjectId chatId) {
        chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException(CHAT_WITH_ID_NOT_FOUND.formatted(chatId)));
    }

    @Override
    public void validateIfThrowsChatNotFoundBySharedChatId(UUID sharedChatId) {
        if(chatRepository.findBySharedId(sharedChatId).isEmpty())
            throw new ChatNotFoundException(CHAT_WITH_ID_NOT_FOUND.formatted(sharedChatId));
    }

}
