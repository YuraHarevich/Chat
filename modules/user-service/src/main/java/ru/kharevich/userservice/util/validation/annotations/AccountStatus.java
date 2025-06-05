package ru.kharevich.userservice.util.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.ACCOUNT_STATUS_PARAM_IS_ABSENT;

@Documented
@Constraint(validatedBy = AccountStatusValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountStatus {

    String message() default ACCOUNT_STATUS_PARAM_IS_ABSENT;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}