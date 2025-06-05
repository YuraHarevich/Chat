package ru.kharevich.userservice.util.validation;

import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.model.User;

import java.util.UUID;

public interface UserValidationService {

    User throwsUserNotFoundException(UUID uuid);

    User throwsUserNotFoundExceptionForDeletedUsers(UUID uuid);

    void throwsRepeatedUserDataExceptionForCreation(UserRequest request);

    void throwsRepeatedUserDataExceptionForUpdate(UserRequest request, UUID id);

}
