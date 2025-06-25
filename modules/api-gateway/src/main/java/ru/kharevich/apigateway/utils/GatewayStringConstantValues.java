package ru.kharevich.apigateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GatewayStringConstantValues {

    public static final String HTTP_REQUEST_MESSAGE = "HTTP Request | Method: %s | Path: %s | Headers: %s | Body: %s.";

    public static final String HTTP_RESPONSE_MESSAGE = "HTTP Response | Path: %s | Status: %s | Duration: %sms  | Response: %s.";

    public static final String RELATED_PATH_FOR_LOGS = "/api/v1";

}
