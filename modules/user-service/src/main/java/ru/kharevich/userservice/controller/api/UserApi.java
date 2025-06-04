package ru.kharevich.userservice.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;

import java.util.UUID;

public interface UserApi {

    public PagedModel<UserResponse> getAll(@RequestParam(defaultValue = "0") @Min(value = 0, message = "page number must be greater than 0") int page_number,
                                           @RequestParam(defaultValue = "10") @Min(value = 1, message = "size must be greater than 1") int size);

    public UserResponse get(@PathVariable @Valid UUID id);

    public UserResponse create(@RequestBody @Valid UserRequest dto);

    public UserResponse update(@PathVariable @Valid UUID id, @RequestBody @Valid UserRequest dto);

    public void delete(@PathVariable @Valid UUID id);

    public UserResponse recoverAccount(@RequestBody @Valid AccountRecoverRequest request);

}
