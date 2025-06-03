package ru.kharevich.userservice.util.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserServiceResponseConstantMessages {

    public static final String USER_NOT_FOUND_MESSAGE = "User with id %s not found";

    public static final String USER_IS_STILL_MODIFYING = "User with id %s is still modifying";

    public static final String ENUM_STATUS_CONVERSION_EXCEPTION_MESSAGE = "User status conversion failed in class %s";

    public static final String ACCOUNT_STATUS_PARAM_IS_ABSENT = "Account status is absent";

}
