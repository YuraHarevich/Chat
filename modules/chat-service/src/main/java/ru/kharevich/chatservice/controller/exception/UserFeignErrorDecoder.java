package ru.kharevich.chatservice.controller.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.kharevich.chatservice.exception.UserNotFoundException;
import ru.kharevich.chatservice.exception.UserServiceInternalError;

import java.nio.charset.StandardCharsets;

import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.USER_NOT_FOUND;
import static ru.kharevich.chatservice.utils.constants.ChatServiceResponseConstantMessages.USER_SERVICE_UNAVAILABLE;

@Slf4j
public class UserFeignErrorDecoder implements ErrorDecoder {
    @Override
    @SneakyThrows
    public Exception decode(String methodKey, Response response) {
        log.debug("UserFeignErrorDecoder.response code: {}", response.status());
        if (response.status() == 404) {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            JsonNode jsonNode = new ObjectMapper().readTree(body);
            String id = jsonNode.has("id") ? jsonNode.get("id").asText() : "unknown";

            log.debug("UserFeignErrorDecoder.decode:" + USER_NOT_FOUND.formatted(id));
            return new UserNotFoundException(USER_NOT_FOUND.formatted(id));
        }
        log.debug("UserFeignErrorDecoder.decode:" + USER_SERVICE_UNAVAILABLE);
        return new UserServiceInternalError(USER_SERVICE_UNAVAILABLE);
    }
}
