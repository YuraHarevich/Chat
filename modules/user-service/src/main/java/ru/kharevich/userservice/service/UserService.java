package ru.kharevich.userservice.service;

import org.springframework.data.domain.Page;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    Page<UserResponse> getAll(int page_number, int size);

    UserResponse get(UUID id);

    UserResponse create(UserRequest dto);

    UserResponse update(UUID id, UserRequest dto);

    void delete(UUID id);

    UserResponse recoverTheAccount(AccountRecoverRequest request);

    void setExternalId(UUID externalId, UUID userId);

    void setExistsStatus(UUID userId);

    UserResponse getByUsername(String username);
}
