package ru.kharevich.userservice.util.validation.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.exceptions.RepeatedUserDataException;
import ru.kharevich.userservice.exceptions.UserNotFoundException;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.repository.UserRepository;
import ru.kharevich.userservice.utils.UserValidationTestFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.kharevich.userservice.model.AccountStatus.DELETED;
import static ru.kharevich.userservice.model.AccountStatus.EXISTS;
import static ru.kharevich.userservice.model.AccountStatus.MODIFYING;

@ExtendWith(MockitoExtension.class)
public class UserValidationUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidationServiceImpl validationService;

    private UUID userId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
    }

    @Test
    void throwsUserNotFoundException_UserNotExists_ThrowsUserNotFoundException() {
        when(userRepository.findByIdAndAccountStatusNot(userId, DELETED)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> validationService.throwsUserNotFoundException(userId));
    }

    @Test
    void throwsUserNotFoundException_UserIsModifying_ThrowsUserNotFoundException() {
        User user = UserValidationTestFactory.userWithStatus(userId, MODIFYING);
        when(userRepository.findByIdAndAccountStatusNot(userId, DELETED)).thenReturn(Optional.of(user));

        assertThrows(UserNotFoundException.class, () -> validationService.throwsUserNotFoundException(userId));
    }

    @Test
    void throwsUserNotFoundException_UserIsActive_ReturnsUser() {
        User user = UserValidationTestFactory.userWithStatus(userId, EXISTS);
        when(userRepository.findByIdAndAccountStatusNot(userId, DELETED)).thenReturn(Optional.of(user));

        User result = validationService.throwsUserNotFoundException(userId);

        assertEquals(user, result);
    }

    // === TESTS for throwsUserNotFoundExceptionForDeletedUsers ===

    @Test
    void throwsUserNotFoundExceptionForDeletedUsers_UserExists_ReturnsUser() {
        User user = UserValidationTestFactory.userWithStatus(userId, DELETED);
        when(userRepository.findByIdAndAccountStatus(userId, DELETED)).thenReturn(Optional.of(user));

        User result = validationService.throwsUserNotFoundExceptionForDeletedUsers(userId);

        assertEquals(user, result);
    }

    @Test
    void throwsUserNotFoundExceptionForDeletedUsers_UserNotExists_ThrowsUserNotFoundException() {
        when(userRepository.findByIdAndAccountStatus(userId, DELETED)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> validationService.throwsUserNotFoundExceptionForDeletedUsers(userId));
    }

    // === TESTS for throwsRepeatedUserDataExceptionForCreation ===

    @Test
    void throwsRepeatedUserDataExceptionForCreation_DuplicateUsernameOrEmail_ThrowsRepeatedUserDataException() {
        UserRequest request = UserValidationTestFactory.validUserRequest();
        when(userRepository.findByUsernameOrEmail(request.username(), request.email())).thenReturn(List.of(new User()));

        assertThrows(RepeatedUserDataException.class, () -> validationService.throwsRepeatedUserDataExceptionForCreation(request));
    }

    @Test
    void throwsRepeatedUserDataExceptionForCreation_UniqueUsernameAndEmail_DoesNotThrowException() {
        UserRequest request = UserValidationTestFactory.validUserRequest();
        when(userRepository.findByUsernameOrEmail(request.username(), request.email())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validationService.throwsRepeatedUserDataExceptionForCreation(request));
    }

    // === TESTS for throwsRepeatedUserDataExceptionForUpdate ===

    @Test
    void throwsRepeatedUserDataExceptionForUpdate_TwoConflictingUsers_ThrowsRepeatedUserDataException() {
        UserRequest request = UserValidationTestFactory.validUserRequest();
        when(userRepository.findByUsernameOrEmail(request.username(), request.email())).thenReturn(List.of(new User(), new User()));

        assertThrows(RepeatedUserDataException.class, () -> validationService.throwsRepeatedUserDataExceptionForUpdate(request, userId));
    }

    @Test
    void throwsRepeatedUserDataExceptionForUpdate_OneUserWithSameId_ThrowsRepeatedUserDataException() {
        User existingUser = UserValidationTestFactory.userWithId(userId);
        UserRequest request = UserValidationTestFactory.validUserRequest();

        when(userRepository.findByUsernameOrEmail(request.username(), request.email())).thenReturn(List.of(existingUser));

        assertThrows(RepeatedUserDataException.class, () -> validationService.throwsRepeatedUserDataExceptionForUpdate(request, userId));
    }

    @Test
    void throwsRepeatedUserDataExceptionForUpdate_OneUserWithDifferentId_DoesNotThrow() {
        User existingUser = UserValidationTestFactory.userWithId(UUID.randomUUID());
        UserRequest request = UserValidationTestFactory.validUserRequest();

        when(userRepository.findByUsernameOrEmail(request.username(), request.email())).thenReturn(List.of(existingUser));

        assertDoesNotThrow(() -> validationService.throwsRepeatedUserDataExceptionForUpdate(request, userId));
    }

    @Test
    void throwsRepeatedUserDataExceptionForUpdate_NoConflicts_DoesNotThrow() {
        UserRequest request = UserValidationTestFactory.validUserRequest();

        when(userRepository.findByUsernameOrEmail(request.username(), request.email())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> validationService.throwsRepeatedUserDataExceptionForUpdate(request, userId));
    }
}
