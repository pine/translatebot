package moe.pine.translatebot.services;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.slack.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class UserInformationManager {
    private final SlackClient slackClient;
    private final AtomicReference<List<User>> users;

    public UserInformationManager(
        final SlackClient slackClient
    ) {
        this.slackClient = slackClient;

        users = new AtomicReference<>(slackClient.getUsers());
        log.info("Users information received : {}", users);
    }

    public Optional<User> findByUserId(final String userId) {
        Objects.requireNonNull(userId);

        final List<User> snapshotUsers = users.get();
        return snapshotUsers.stream()
            .filter(user -> Objects.equals(user.getId(), userId))
            .findFirst();
    }

    public void refresh() {
        users.set(slackClient.getUsers());
        log.info("Users information updated : {}", users);
    }
}
