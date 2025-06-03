package ru.kharevich.userservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum AccountStatus {

    EXISTS(100),

    MODIFYING(200),

    DELETED(300);

    private final int status;

    public static Optional<AccountStatus> fromStatus(int status) {
        return Arrays.stream(AccountStatus.values())
                .filter(entity -> entity.status == status)
                .findAny();
    }

}
