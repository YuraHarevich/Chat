package ru.kharevich.userservice.controller.impl;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.kharevich.userservice.controller.api.UserApi;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.RefreshTokenRequest;
import ru.kharevich.userservice.dto.request.SignInRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;
import ru.kharevich.userservice.service.KeycloakUserService;
import ru.kharevich.userservice.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController implements UserApi {

    private final UserService userService;

    private final KeycloakUserService keycloakUserService;

    @GetMapping("all")
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<UserResponse> getAll(@RequestParam(defaultValue = "0") @Min(value = 0, message = "page number must be greater than 0") int page_number,
                                           @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be greater than 1") int size) {
        Page<UserResponse> userResponses = userService.getAll(page_number, size);
        return new PagedModel<>(userResponses);
    }

    @GetMapping("me")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse get() {
        return userService.getUser();
    }

    @GetMapping("/username/{username}")
    @ResponseStatus(HttpStatus.OK)
    @Override
    public UserResponse getByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/username/starts-with/{username}")
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<UserResponse> getByUsernameStartingWith(@PathVariable String username) {
        Page<UserResponse> userResponses = userService.getUserByUsernameStartingWith(username, 0, 5);
        return new PagedModel<>(userResponses);
    }

    @GetMapping("/id/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Override
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PostMapping("sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody @Valid UserRequest dto) {
        return userService.create(dto);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserResponse update(@RequestBody @Valid UserRequest dto) {
        return userService.update(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Valid UUID id) {
        userService.delete(id);
    }

    @PatchMapping("/recover")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UserResponse recoverAccount(@RequestBody @Valid AccountRecoverRequest request) {
        return userService.recoverTheAccountAndPostEvent(request);
    }

    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.OK)
    public AccessTokenResponse sighIn(@RequestBody @Valid SignInRequest request,
                                      HttpServletResponse serverResponse) {
        AccessTokenResponse response = keycloakUserService.sighIn(request);
        return response;
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public AccessTokenResponse refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return keycloakUserService.refreshToken(request);
    }

}
