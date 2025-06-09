package ru.kharevich.userservice.util.validation.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.exceptions.RepeatedUserDataException;
import ru.kharevich.userservice.exceptions.UserNotFoundException;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.repository.UserRepository;
import ru.kharevich.userservice.util.validation.UserValidationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.kharevich.userservice.model.AccountStatus.DELETED;
import static ru.kharevich.userservice.model.AccountStatus.MODIFYING;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_IS_STILL_MODIFYING;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_NOT_FOUND_MESSAGE;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_REPEATED_DATA_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationServiceImpl implements UserValidationService {

    private final UserRepository userRepository;
    @Override
    public User throwsUserNotFoundException(UUID id) {
        Optional<User> userOptional = userRepository.findByIdAndAccountStatusNot(id, DELETED);
        if(userOptional.isPresent()) {
            if(userOptional.get().getAccountStatus() == MODIFYING) {
                log.info("UserValidationServiceImpl.User {} is already modifying", id);
                throw new UserNotFoundException(USER_IS_STILL_MODIFYING.formatted(id));
            }
            return userOptional.get();
        }
        throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(id));
    }

    @Override
    public User throwsUserNotFoundExceptionForDeletedUsers(UUID uuid) {
        return userRepository.findByIdAndAccountStatus(uuid, DELETED).orElseThrow(() -> {
            log.info("UserValidationServiceImpl.User {} is deleted", uuid);
            return new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(uuid));
        });
    }

    @Override
    public void throwsRepeatedUserDataExceptionForCreation(UserRequest request) {
        if(!userRepository.findByUsernameOrEmail(request.username(), request.email()).isEmpty()){
            log.info("UserValidationServiceImpl.Error while creation, users with such username {} or email {} already exists", request.username(), request.email());
            throw new RepeatedUserDataException(USER_REPEATED_DATA_MESSAGE);
        }
    }

    @Override
    public void throwsRepeatedUserDataExceptionForUpdate(UserRequest request, UUID id) {
        List<User> users = userRepository.findByUsernameOrEmail(request.username(), request.email());
        if(users.size() > 1){
            log.info("UserValidationServiceImpl.Error while update, users with such username {} and email {} already exists", request.username(), request.email());
            throw new RepeatedUserDataException(USER_REPEATED_DATA_MESSAGE);
        }
        if(users.size() == 1){
            if(users.getFirst().getId().equals(id)){
                log.info("UserValidationServiceImpl.Error while update, users with such username {} or email {} already exists", request.username(), request.email());
                throw new RepeatedUserDataException(USER_REPEATED_DATA_MESSAGE);
            }
        }
    }

}
