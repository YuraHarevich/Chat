package ru.kharevich.userservice.exceptions;

public class RepeatedUserDataException extends RuntimeException {
    public RepeatedUserDataException(String message) {
        super(message);
    }
}
