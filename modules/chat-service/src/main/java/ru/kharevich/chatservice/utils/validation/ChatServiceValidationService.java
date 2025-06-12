package ru.kharevich.chatservice.utils.validation;

import org.bson.types.ObjectId;

import java.util.Set;
import java.util.UUID;

public interface ChatServiceValidationService {

    void validateIfThrowsUsersNotFound(Set<UUID> participants);

    void validateIfThrowsUserNotFoundByUsername(String username);

    void validateIfThrowsChatNotFoundByChatId(ObjectId chatId);

    void validateIfThrowsChatNotFoundBySharedChatId(UUID sharedChatId);

}
