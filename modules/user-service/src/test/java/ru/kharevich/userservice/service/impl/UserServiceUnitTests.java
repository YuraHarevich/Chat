package ru.kharevich.userservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.kharevich.userservice.dto.events.UserEventTransferEntity;
import ru.kharevich.userservice.dto.request.AccountRecoverRequest;
import ru.kharevich.userservice.dto.request.UserRequest;
import ru.kharevich.userservice.dto.response.UserResponse;
import ru.kharevich.userservice.exceptions.UserNotFoundException;
import ru.kharevich.userservice.kafka.producer.UserEventProducer;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.repository.UserRepository;
import ru.kharevich.userservice.util.mapper.UserEventMapper;
import ru.kharevich.userservice.util.mapper.UserMapper;
import ru.kharevich.userservice.util.validation.UserValidationService;
import ru.kharevich.userservice.utils.UserTestFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.kharevich.userservice.model.AccountStatus.DELETED;
import static ru.kharevich.userservice.model.AccountStatus.EXISTS;
import static ru.kharevich.userservice.model.AccountStatus.MODIFYING;
import static ru.kharevich.userservice.model.UserModifyEventType.CREATE_EVENT;
import static ru.kharevich.userservice.model.UserModifyEventType.UPDATE_EVENT;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private UserEventProducer userEventProducer;

    @Mock
    private UserEventMapper userEventMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserRequest testUserRequest;

    @BeforeEach
    void setUp() {
        testUser = UserTestFactory.createValidUser();
        testUserResponse = new UserResponse(
                testUser.getId(),
                testUser.getExternalId(),
                testUser.getUsername(),
                testUser.getFirstname(),
                testUser.getLastname(),
                testUser.getBirthDate(),
                testUser.getEmail()
        );
        testUserRequest = UserTestFactory.createValidUserRequest();
    }

    // getAll tests
    @Test
    void getAll_WithExistingUsers_ReturnPageOfUserResponses() {
        // Arrange
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findByAccountStatus(eq(EXISTS), any(PageRequest.class))).thenReturn(userPage);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        Page<UserResponse> result = userService.getAll(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testUserResponse, result.getContent().get(0));
    }

    @Test
    void getAll_WithNoUsers_ReturnEmptyPage() {
        // Arrange
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findByAccountStatus(eq(EXISTS), any(PageRequest.class))).thenReturn(emptyPage);

        // Act
        Page<UserResponse> result = userService.getAll(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // get tests
    @Test
    void get_WithExistingUserId_ReturnUserResponse() {
        // Arrange
        when(userValidationService.throwsUserNotFoundException(testUser.getId())).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        UserResponse result = userService.get(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testUserResponse, result);
    }

    @Test
    void get_WithNonExistingUserId_ThrowUserNotFoundException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(userValidationService.throwsUserNotFoundException(nonExistingId)).thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.get(nonExistingId));
    }

    // create tests
    @Test
    void create_WithValidUserRequest_ReturnCreatedUserResponse() {
        // Arrange
        doNothing().when(userValidationService).throwsRepeatedUserDataExceptionForCreation(testUserRequest);
        when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
        when(userRepository.saveAndFlush(testUser)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        UserResponse result = userService.create(testUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUserResponse, result);
        verify(userRepository).saveAndFlush(testUser);
    }

    // update tests
    @Test
    void update_WithValidData_ReturnUpdatedUserResponse() {
        // Arrange
        User updatedUser = User.builder()
                .id(testUser.getId())
                .username("updateduser")
                .firstname("Updated")
                .lastname("User")
                .email("updated@example.com")
                .accountStatus(MODIFYING)
                .build();

        UserResponse updatedResponse = new UserResponse(
                updatedUser.getId(),
                updatedUser.getExternalId(),
                updatedUser.getUsername(),
                updatedUser.getFirstname(),
                updatedUser.getLastname(),
                updatedUser.getBirthDate(),
                updatedUser.getEmail()
        );

        when(userValidationService.throwsUserNotFoundException(testUser.getId())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(updatedResponse);

        // Act
        UserResponse result = userService.update(testUser.getId(), testUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(updatedResponse, result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_WithNonExistingUserId_ThrowUserNotFoundException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(userValidationService.throwsUserNotFoundException(nonExistingId))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.update(nonExistingId, testUserRequest));
    }

    // recoverTheAccount tests
    @Test
    void recoverTheAccount_WithDeletedUser_ReturnUserResponseWithModifyingStatus() {
        // Arrange
        User deletedUser = UserTestFactory.createUserWithStatus(DELETED);
        AccountRecoverRequest recoverRequest = new AccountRecoverRequest(deletedUser.getId(), "newpassword");
        UserResponse expectedResponse = new UserResponse(
                deletedUser.getId(),
                deletedUser.getExternalId(),
                deletedUser.getUsername(),
                deletedUser.getFirstname(),
                deletedUser.getLastname(),
                deletedUser.getBirthDate(),
                deletedUser.getEmail()
        );

        when(userValidationService.throwsUserNotFoundExceptionForDeletedUsers(recoverRequest.id())).thenReturn(deletedUser);
        when(userRepository.save(deletedUser)).thenReturn(deletedUser);
        when(userMapper.toResponse(deletedUser)).thenReturn(expectedResponse);
        when(userEventMapper.toEventEntity(eq(deletedUser), eq(CREATE_EVENT), eq(recoverRequest.password())))
                .thenReturn(mock(UserEventTransferEntity.class));

        // Act
        UserResponse result = userService.recoverTheAccount(recoverRequest);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
        assertEquals(MODIFYING, deletedUser.getAccountStatus());
        verify(userEventProducer).publishEventRequest(any(UserEventTransferEntity.class));
    }

    @Test
    void recoverTheAccount_WithNonDeletedUser_ThrowUserNotFoundException() {
        // Arrange
        AccountRecoverRequest recoverRequest = new AccountRecoverRequest(testUser.getId(), "newpassword");
        when(userValidationService.throwsUserNotFoundExceptionForDeletedUsers(recoverRequest.id()))
                .thenThrow(new UserNotFoundException("Deleted user not found"));

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.recoverTheAccount(recoverRequest));
    }

    // setExternalId tests
    @Test
    void setExternalId_WithExistingUserId_SuccessfullySetExternalId() {
        // Arrange
        UUID externalId = UUID.randomUUID();
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        userService.setExternalId(externalId, testUser.getId());

        // Assert
        assertEquals(externalId, testUser.getExternalId());
        verify(userRepository).save(testUser);
    }

    @Test
    void setExternalId_WithNonExistingUserId_ThrowUserNotFoundException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.setExternalId(UUID.randomUUID(), nonExistingId));
        verify(userRepository, never()).save(any());
    }

    // setExistsStatus tests
    @Test
    void setExistsStatus_WithExistingUserId_SuccessfullySetStatus() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        // Act
        userService.setExistsStatus(testUser.getId());

        // Assert
        assertEquals(EXISTS, testUser.getAccountStatus());
        verify(userRepository).save(testUser);
    }

    @Test
    void setExistsStatus_WithNonExistingUserId_ThrowUserNotFoundException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.setExistsStatus(nonExistingId));
        verify(userRepository, never()).save(any());
    }

    // createUserPostEvent tests
    @Test
    void createUserPostEvent_WithValidUserRequest_PublishEventAndReturnUserResponse() {
        // Arrange
        doNothing().when(userValidationService).throwsRepeatedUserDataExceptionForCreation(testUserRequest);
        when(userMapper.toEntity(testUserRequest)).thenReturn(testUser);
        when(userRepository.saveAndFlush(testUser)).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
        when(userEventMapper.toEventEntity(eq(testUser), eq(CREATE_EVENT), eq(testUserRequest.password())))
                .thenReturn(mock(UserEventTransferEntity.class));

        // Act
        UserResponse result = userService.createUserPostEvent(testUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUserResponse, result);
        verify(userEventProducer).publishEventRequest(any(UserEventTransferEntity.class));
    }

    // updateUserPostEvent tests
    @Test
    void updateUserPostEvent_WithValidData_PublishEventAndReturnUpdatedUserResponse() {
        // Arrange
        User updatedUser = User.builder()
                .id(testUser.getId())
                .username("updateduser")
                .firstname("Updated")
                .lastname("User")
                .email("updated@example.com")
                .accountStatus(MODIFYING)
                .build();

        UserResponse updatedResponse = new UserResponse(
                updatedUser.getId(),
                updatedUser.getExternalId(),
                updatedUser.getUsername(),
                updatedUser.getFirstname(),
                updatedUser.getLastname(),
                updatedUser.getBirthDate(),
                updatedUser.getEmail()
        );

        when(userValidationService.throwsUserNotFoundException(testUser.getId())).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(updatedResponse);
        when(userEventMapper.toEventEntity(eq(updatedUser), eq(UPDATE_EVENT), eq(testUserRequest.password())))
                .thenReturn(mock(UserEventTransferEntity.class));

        // Act
        UserResponse result = userService.updateUserPostEvent(testUser.getId(), testUserRequest);

        // Assert
        assertNotNull(result);
        assertEquals(updatedResponse, result);
        verify(userEventProducer).publishEventRequest(any(UserEventTransferEntity.class));
    }
}