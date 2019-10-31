package moe.pine.translatebot.services;

import com.github.seratch.jslack.api.rtm.RTMClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BotService {
    private final RTMClient rtmClient;

    public BotService(
            final RTMClient rtmClient
    ) throws Exception {
        rtmClient.addCloseHandler(closeReason -> {
            log.info("{}", closeReason);
        });
        rtmClient.addErrorHandler(error -> {
            error.printStackTrace();
        });
        rtmClient.addMessageHandler(message -> {
            log.info(message);
        });

        rtmClient.connect();

        this.rtmClient = rtmClient;
    }
}
