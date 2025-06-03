package ru.kharevich.userservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface UserService {

    Page<UserResponse> getAll(int page_number, int size);

    UserResponse get(UUID id);

    UserResponse create(UserRequest dto);

    UserResponse update(UUID id, UserRequest dto);

    void delete(UUID id);

    UserResponse recoverTheAccount(UUID id);

}
