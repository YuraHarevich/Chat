package ru.kharevich.userservice.service;

import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    Page<UserResponse> getAll(int page_number, int size);

    UserResponse getUser();

    UserResponse getUserByUsername(String username);

    UserResponse create(UserRequest dto);

    UserResponse update(UserRequest dto);

    void delete(UUID id);

    void absoluteDelete(UUID id);

    UserResponse recoverTheAccount(AccountRecoverRequest request);

    @CachePut(value = "users", key = "#request.id()")
    UserResponse recoverTheAccountAndPostEvent(AccountRecoverRequest request);

    void setExternalId(UUID externalId, UUID userId);

    void setExistsStatus(UUID userId);

    Page<UserResponse> getUserByUsernameStartingWith(String username, int page_number, int size);

    UserResponse getUserById(UUID id);
}
