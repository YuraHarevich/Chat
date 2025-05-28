package ru.kharevich.chatservice.utils.validation;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

public interface ChatServiceValidationService {

    void validateIfThrowsUsersNotFound(Set<UUID> participants);

    void validateIfThrowsChatNotFound(ObjectId chatId);

}
