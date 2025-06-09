package ru.kharevich.chatservice.exception;

public class UserServiceInternalError extends RuntimeException {
    public UserServiceInternalError(String message) {
        super(message);
    }
}
