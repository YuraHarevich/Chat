package ru.kharevich.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;
import ru.kharevich.userservice.model.AccountStatus;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.repository.UserRepository;
import ru.kharevich.userservice.service.KeycloakUserService;
import ru.kharevich.userservice.service.UserService;
import ru.kharevich.userservice.util.mapper.UserMapper;
import ru.kharevich.userservice.util.validation.UserValidationService;

import java.util.Optional;
import java.util.UUID;

import static ru.kharevich.userservice.model.AccountStatus.DELETED;
import static ru.kharevich.userservice.model.AccountStatus.EXISTS;
import static ru.kharevich.userservice.model.AccountStatus.MODIFYING;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final UserValidationService userValidationService;

    private final KeycloakUserService keycloakUserService;

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
        User user = userMapper.toEntity(dto);
        User resultUser = userRepository.save(user);

        keycloakUserService.createUser(dto);

        return userMapper.toResponse(resultUser);
    }

    @Override
    public UserResponse update(UUID id, UserRequest request) {
        User user = userValidationService.throwsUserNotFoundException(id);
        userMapper.updateUserByRequest(request, user);
        User resultUser = userRepository.save(user);
        return userMapper.toResponse(resultUser);
    }

    @Override
    public void delete(UUID id) {
        User user = userValidationService.throwsUserNotFoundException(id);
        user.setAccountStatus(DELETED);
        userRepository.save(user);
        userMapper.toResponse(user);
    }

    public UserResponse recoverTheAccount(UUID id) {
        User user = userValidationService.throwsUserNotFoundExceptionForDeletedUsers(id);
        user.setAccountStatus(MODIFYING);
        //todo: запрос в кафка для создания user
        return userMapper.toResponse(userRepository.save(user));
    }

}
