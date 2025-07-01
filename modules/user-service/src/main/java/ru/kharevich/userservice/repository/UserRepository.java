package ru.kharevich.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import ru.kharevich.userservice.model.AccountStatus;
import ru.kharevich.userservice.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepositoryImplementation<User, UUID> {
    Optional<User> findByIdAndAccountStatus(UUID id, AccountStatus accountStatus);

    Optional<User> findByUsername(String username);

    Page<User> findByUsernameStartingWithIgnoreCase(String username , Pageable pageable);

    List<User> findAllByAccountStatusAndUpdatedAtBefore(AccountStatus status, LocalDateTime updatedAt);

    Optional<User> findByIdAndAccountStatusNot(UUID id, AccountStatus accountStatus);

    Page<User> findByAccountStatus(AccountStatus accountStatus, Pageable pageable);

    List<User> findByUsernameOrEmail(String username, String email);
}