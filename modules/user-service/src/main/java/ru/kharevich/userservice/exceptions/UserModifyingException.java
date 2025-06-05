package ru.kharevich.userservice.exceptions;

public class UserModifyingException extends RuntimeException {
    public UserModifyingException(String message) {
        super(message);
    }
}
