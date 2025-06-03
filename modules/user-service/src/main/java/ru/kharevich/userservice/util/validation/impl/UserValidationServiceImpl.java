package ru.kharevich.userservice.util.validation.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kharevich.userservice.exceptions.UserNotFoundException;
import ru.kharevich.userservice.model.AccountStatus;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.repository.UserRepository;
import ru.kharevich.userservice.util.validation.UserValidationService;

import java.util.Optional;
import java.util.UUID;

import static ru.kharevich.userservice.model.AccountStatus.DELETED;
import static ru.kharevich.userservice.model.AccountStatus.MODIFYING;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_IS_STILL_MODIFYING;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_NOT_FOUND_MESSAGE;

@Service
@RequiredArgsConstructor
public class UserValidationServiceImpl implements UserValidationService {

    private final UserRepository userRepository;
    @Override
    public User throwsUserNotFoundException(UUID id) {
        Optional<User> userOptional = userRepository.findByIdAndAccountStatusNot(id, DELETED);
        if(userOptional.isPresent()) {
            if(userOptional.get().getAccountStatus() == MODIFYING) {
                throw new UserNotFoundException(USER_IS_STILL_MODIFYING.formatted(id));
            }
            return userOptional.get();
        }
        throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(id));
    }

    @Override
    public User throwsUserNotFoundExceptionForDeletedUsers(UUID uuid) {
        return userRepository.findByIdAndAccountStatus(uuid, DELETED).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(uuid)));
    }

}
