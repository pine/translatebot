package moe.pine.translatebot.services;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.slack.User;
import org.springframework.stereotype.Component;

import java.util.List;
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
}
