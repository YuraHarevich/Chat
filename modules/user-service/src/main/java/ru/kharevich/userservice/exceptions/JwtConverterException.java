package ru.kharevich.userservice.exceptions;

public class JwtConverterException extends RuntimeException {
    public JwtConverterException(String message) {
        super(message);
    }
}
