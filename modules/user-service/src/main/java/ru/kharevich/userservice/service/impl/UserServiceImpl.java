package ru.kharevich.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.kharevich.userservice.dto.events.UserEventTransferEntity;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;
import ru.kharevich.userservice.exceptions.UserNotFoundException;
import ru.kharevich.userservice.kafka.producer.UserEventProducer;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.repository.UserRepository;
import ru.kharevich.userservice.service.UserEventService;
import ru.kharevich.userservice.service.UserService;
import ru.kharevich.userservice.util.mapper.UserEventMapper;
import ru.kharevich.userservice.util.mapper.UserMapper;
import ru.kharevich.userservice.util.validation.UserValidationService;

import java.util.Optional;
import java.util.UUID;

import static ru.kharevich.userservice.model.AccountStatus.DELETED;
import static ru.kharevich.userservice.model.AccountStatus.EXISTS;
import static ru.kharevich.userservice.model.AccountStatus.MODIFYING;
import static ru.kharevich.userservice.model.UserModifyEventType.CREATE_EVENT;
import static ru.kharevich.userservice.model.UserModifyEventType.DELETE_EVENT;
import static ru.kharevich.userservice.model.UserModifyEventType.UPDATE_EVENT;
import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_NOT_FOUND_MESSAGE;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserEventService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final UserValidationService userValidationService;

    private final UserEventProducer userEventProducer;

    private final UserEventMapper userEventMapper;

    @Override
    public Page<UserResponse> getAll(int page_number, int size) {
        Page<User> users = userRepository.findByAccountStatus(EXISTS, PageRequest.of(page_number, size));
        return users.map(userMapper::toResponse);
    }

    @Override
    public UserResponse get(UUID id) {
        User user = userValidationService.throwsUserNotFoundException(id);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse create(UserRequest dto) {
        userValidationService.throwsRepeatedUserDataExceptionForCreation(dto);
        User user = userMapper.toEntity(dto);
        userRepository.saveAndFlush(user);

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse update(UUID id, UserRequest request) {
        userValidationService.throwsRepeatedUserDataExceptionForUpdate(request, id);
        User user = userValidationService.throwsUserNotFoundException(id);
        userMapper.updateUserByRequest(request, user);

        user.setAccountStatus(MODIFYING);
        User resultUser = userRepository.save(user);

        return userMapper.toResponse(resultUser);
    }

    @Override
    public void delete(UUID id) {
        User user = userValidationService.throwsUserNotFoundException(id);
        user.setAccountStatus(DELETED);
        userRepository.save(user);
    }

    @Override
    public UserResponse recoverTheAccount(AccountRecoverRequest request) {
        User user = userValidationService.throwsUserNotFoundExceptionForDeletedUsers(request.id());
        user.setAccountStatus(MODIFYING);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse recoverTheAccountAndPostEvent(AccountRecoverRequest request) {
        User user = userValidationService.throwsUserNotFoundExceptionForDeletedUsers(request.id());
        user.setAccountStatus(MODIFYING);

        UserEventTransferEntity transferEntity = userEventMapper.toEventEntity(user, CREATE_EVENT, request.password());
        userEventProducer.publishEventRequest(transferEntity);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void setExternalId(UUID externalId,  UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(userId)));
        user.setExternalId(externalId);
        userRepository.save(user);
    }

    @Override
    public void setExistsStatus(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(userId)));
        user.setAccountStatus(EXISTS);
        userRepository.save(user);
    }

    @Override
    public UserResponse getByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(username));
        }
        return userMapper.toResponse(user.get());
    }

    @Override
    public UserResponse createUserPostEvent(UserRequest dto) {
        userValidationService.throwsRepeatedUserDataExceptionForCreation(dto);
        User user = userMapper.toEntity(dto);

        userRepository.saveAndFlush(user);

        UserEventTransferEntity transferEntity = userEventMapper.toEventEntity(user, CREATE_EVENT, dto.password());
        userEventProducer.publishEventRequest(transferEntity);

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUserPostEvent(UUID id, UserRequest request) {
        userValidationService.throwsRepeatedUserDataExceptionForUpdate(request, id);
        User user = userValidationService.throwsUserNotFoundException(id);
        userMapper.updateUserByRequest(request, user);

        user.setAccountStatus(MODIFYING);
        User resultUser = userRepository.save(user);

        UserEventTransferEntity transferEntity = userEventMapper.toEventEntity(resultUser, UPDATE_EVENT, request.password());
        userEventProducer.publishEventRequest(transferEntity);

        return userMapper.toResponse(resultUser);
    }

    @Override
    public void deleteUserPostEvent(UUID id) {
        User user = userValidationService.throwsUserNotFoundException(id);
        user.setAccountStatus(DELETED);
        userRepository.save(user);

        UserEventTransferEntity transferEntity = userEventMapper.toEventEntity(user, DELETE_EVENT, null);
        userEventProducer.publishEventRequest(transferEntity);
    }
}
