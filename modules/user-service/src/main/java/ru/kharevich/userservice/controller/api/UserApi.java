package ru.kharevich.userservice.controller.api;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.RefreshTokenRequest;
import ru.kharevich.userservice.dto.request.SignInRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;

import java.util.UUID;

public interface UserApi {

    public PagedModel<UserResponse> getAll(@RequestParam(defaultValue = "0") @Min(value = 0, message = "page number must be greater than 0") int page_number,
                                           @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be greater than 1") int size);

    public UserResponse get();

    @GetMapping("/username/{username}")
    @ResponseStatus(HttpStatus.OK)
    UserResponse getByUsername(@PathVariable String username);

    PagedModel<UserResponse> getByUsernameStartingWith(@PathVariable String username);

    UserResponse getById(@PathVariable UUID id);

    public UserResponse create(@RequestBody @Valid UserRequest dto);

    public UserResponse update(@RequestBody @Valid UserRequest dto);

    public void delete(@PathVariable @Valid UUID id);

    public UserResponse recoverAccount(@RequestBody @Valid AccountRecoverRequest request);

    AccessTokenResponse sighIn(@RequestBody @Valid SignInRequest request, HttpServletResponse serverResponse);

    AccessTokenResponse refreshToken(@RequestBody @Valid RefreshTokenRequest request);
}
