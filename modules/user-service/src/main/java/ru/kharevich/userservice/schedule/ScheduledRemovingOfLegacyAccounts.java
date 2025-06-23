package ru.kharevich.userservice.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.kharevich.userservice.model.AccountStatus;
import ru.kharevich.userservice.model.User;
import ru.kharevich.userservice.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledRemovingOfLegacyAccounts {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private final UserRepository userRepository;
    @Value("${app.config.time_of_expiration_of_accounts_in_days}")
    private long timeOfExpirationOfAccountsInDays;

    @Scheduled(fixedRateString = "${app.config.period_of_user_cleaning_in_millis}")
    public void clearingTheQueueOfOutdated() {
        LocalDateTime expirationThreshold = LocalDateTime.now().minusDays(timeOfExpirationOfAccountsInDays);

        List<User> usersToDelete = userRepository.findAllByAccountStatusAndUpdatedAtBefore(AccountStatus.DELETED, expirationThreshold);

        if (!usersToDelete.isEmpty()) {
            userRepository.deleteAll(usersToDelete);
            log.info("Deleted {} outdated users marked as DELETED", usersToDelete.size());
        } else {
            log.info("No outdated DELETED users found for cleanup at {}", dateFormat.format(new Date()));
        }
    }

}