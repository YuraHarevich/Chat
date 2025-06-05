package ru.kharevich.userservice.util.validation.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class AccountStatusValidator implements ConstraintValidator<AccountStatus, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return Arrays.stream(ru.kharevich.userservice.model.AccountStatus.values())
                .anyMatch(obj -> obj.name().equals(value));
    }

}