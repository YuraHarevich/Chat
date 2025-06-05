package ru.kharevich.userservice.util.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserServiceResponseConstantMessages {

    public static final String USER_NOT_FOUND_MESSAGE = "User with id %s not found";

    public static final String USER_IS_STILL_MODIFYING = "User with id %s is still modifying";

    public static final String ENUM_STATUS_CONVERSION_EXCEPTION_MESSAGE = "User status conversion failed in class %s";

    public static final String ACCOUNT_STATUS_PARAM_IS_ABSENT = "Account status is absent";

    public static final String USER_REPEATED_DATA_MESSAGE = "User with such email/username already exists";

    public static final String USER_CREATION_EXCEPTION_MESSAGE = "User creation failed";

    public static final String USER_UPDATE_EXCEPTION_MESSAGE = "User update failed";

    public static final String USER_DELETE_EXCEPTION_MESSAGE = "User delete failed";

    public static final String USER_CREATION_EXCEPTION_WHILE_REQUEST_MESSAGE = "User creation failed int request phase";

    public static final String JWT_CONVERT_EXCEPTION_MESSAGE = "JWT conversion failed";

    public static final String WRONG_CREDENTIALS_MESSAGE = "wrong credentials";

}
