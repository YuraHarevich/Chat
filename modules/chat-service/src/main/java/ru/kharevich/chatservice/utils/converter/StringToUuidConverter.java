package ru.kharevich.chatservice.utils.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.UUID;

@ReadingConverter
public class StringToUuidConverter implements Converter<String, UUID> {
    @Override
    public UUID convert(String source) {
        return source != null ? UUID.fromString(source) : null;
    }
}
