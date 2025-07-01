package ru.kharevich.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
import ru.kharevich.userservice.service.UserService;
import ru.kharevich.userservice.util.JwtUtils;
import ru.kharevich.userservice.util.mapper.UserEventMapper;
import ru.kharevich.userservice.util.mapper.UserMapper;
import ru.kharevich.userservice.util.validation.UserValidationService;

import java.util.List;
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
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final UserValidationService userValidationService;

    private final UserEventProducer userEventProducer;

    private final UserEventMapper userEventMapper;

    private final JwtUtils jwtUtils;

    @Cacheable(value = "users", key = "'page_' + #page_number + '_size_' + #size")
    @Override
    public Page<UserResponse> getAll(int page_number, int size) {
        Page<User> users = userRepository.findByAccountStatus(EXISTS, PageRequest.of(page_number, size));
        return users.map(userMapper::toResponse);
    }

    @Cacheable(value = "users", keyGenerator = "userCacheKeyGenerator")
    @Override
    public UserResponse getUser() {
        String userName = jwtUtils.getPreferredUsername();
        Optional<User> user = userRepository.findByUsername(userName);
        if (user.isEmpty()) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(userName));
        }
        return userMapper.toResponse(user.get());
    }

    @CachePut(value = "users", keyGenerator = "userCacheKeyGenerator")
    @Override
    public UserResponse create(UserRequest dto) {
        userValidationService.throwsRepeatedUserDataExceptionForCreation(dto);
        User user = userMapper.toEntity(dto);
        userRepository.saveAndFlush(user);

        UserEventTransferEntity transferEntity = userEventMapper.toEventEntity(user, CREATE_EVENT, dto.password());
        userEventProducer.publishEventRequest(transferEntity);

        return userMapper.toResponse(user);
    }

    @CachePut(value = "users", keyGenerator = "userCacheKeyGenerator")
    @Override
    public UserResponse update(UserRequest request) {
        UUID id = UUID.fromString(jwtUtils.getUserId());
        userValidationService.throwsRepeatedUserDataExceptionForUpdate(request, id);
        User user = userValidationService.throwsUserNotFoundException(id);
        userMapper.updateUserByRequest(request, user);

        user.setAccountStatus(MODIFYING);
        User resultUser = userRepository.save(user);

        UserEventTransferEntity transferEntity = userEventMapper.toEventEntity(resultUser, UPDATE_EVENT, request.password());
        userEventProducer.publishEventRequest(transferEntity);

        return userMapper.toResponse(resultUser);
    }

    @CacheEvict(value = "users", key = "#id")
    @Override
    public void delete(UUID id) {
        User user = userValidationService.throwsUserNotFoundException(id);
        user.setAccountStatus(DELETED);
        userRepository.save(user);

        UserEventTransferEntity transferEntity = userEventMapper.toEventEntity(user, DELETE_EVENT, null);
        userEventProducer.publishEventRequest(transferEntity);
    }

    @CacheEvict(value = "users", key = "#id")
    @Override
    public void absoluteDelete(UUID id) {
        User user = userValidationService.throwsUserNotFoundException(id);
        userRepository.delete(user);
    }

    @CachePut(value = "users", key = "#request.id()")
    @Override
    public UserResponse recoverTheAccount(AccountRecoverRequest request) {
        User user = userValidationService.throwsUserNotFoundExceptionForDeletedUsers(request.id());
        user.setAccountStatus(MODIFYING);

        return userMapper.toResponse(userRepository.save(user));
    }

    @CachePut(value = "users", key = "#request.id()")
    @Override
    public UserResponse recoverTheAccountAndPostEvent(AccountRecoverRequest request) {
        User user = userValidationService.throwsUserNotFoundExceptionForDeletedUsers(request.id());
        user.setAccountStatus(MODIFYING);

        UserEventTransferEntity transferEntity = userEventMapper.toEventEntity(user, CREATE_EVENT, request.password());
        userEventProducer.publishEventRequest(transferEntity);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void setExternalId(UUID externalId, UUID userId) {
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

    @Cacheable(value = "users", key = "#username")
    @Override
    public Page<UserResponse> getUserByUsernameStartingWith(String username, int page_number, int size) {
        Page<User> users = userRepository.findByUsernameStartingWithIgnoreCase(username, PageRequest.of(page_number, size));
        return users.map(userMapper::toResponse);
    }

    @Cacheable(value = "users", key = "#id")
    @Override
    public UserResponse getUserById(UUID id) {
        User user = userValidationService.throwsUserNotFoundException(id);
        return userMapper.toResponse(user);
    }

    @Cacheable(value = "users", key = "#username")
    @Override
    public UserResponse getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UserNotFoundException(USER_NOT_FOUND_MESSAGE.formatted(username));
        }
        return userMapper.toResponse(user.get());
    }

}
