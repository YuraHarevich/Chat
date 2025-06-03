package ru.kharevich.userservice.util.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.kharevich.userservice.exceptions.EnumStatusConversionException;
import ru.kharevich.userservice.model.AccountStatus;

import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.ENUM_STATUS_CONVERSION_EXCEPTION_MESSAGE;

@Converter
public class MyAccountStatusConverter implements AttributeConverter<AccountStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AccountStatus object) {
        if (object == null) {
            throw new EnumStatusConversionException(ENUM_STATUS_CONVERSION_EXCEPTION_MESSAGE.formatted(AccountStatus.class.getName()));
        }
        return object.getStatus();
    }

    @Override
    public AccountStatus convertToEntityAttribute(Integer code) {
        return AccountStatus.fromStatus(code)
                .orElseThrow(() -> new EnumStatusConversionException(ENUM_STATUS_CONVERSION_EXCEPTION_MESSAGE.formatted(AccountStatus.class.getName())));
    }

}