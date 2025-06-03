package ru.kharevich.userservice.controller.ex;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kharevich.userservice.dto.other.ErrorMessage;
import ru.kharevich.userservice.exceptions.UserNotFoundException;

import java.time.LocalDateTime;

import static ru.kharevich.userservice.util.constants.UserServiceResponseConstantMessages.USER_NOT_FOUND_MESSAGE;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            UserNotFoundException.class
    })
    public ResponseEntity<ErrorMessage> handle(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorMessage.builder()
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            ConstraintDeclarationException.class
    })
    public ResponseEntity<ErrorMessage> handleValidationExceptions(Exception ex) {
        String errorMessage = ex instanceof MethodArgumentNotValidException
                ? ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().getFirst().getDefaultMessage()
                : ex.getMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorMessage.builder()
                        .message(errorMessage)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

}
