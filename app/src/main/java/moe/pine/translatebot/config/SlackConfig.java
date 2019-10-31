package moe.pine.translatebot.config;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.rtm.RTMClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SlackConfig {
    @Bean
    public Slack slack() {
        return new Slack();
    }

    @Bean(destroyMethod = "disconnect")
    public RTMClient rtmClient(
            final Slack slack
    ) throws Exception {
        final var rtm = slack.rtm("");

        rtm.addMessageHandler(message -> {
            log.info(message);
        });
        rtm.addErrorHandler(error -> {
            error.printStackTrace();
        });
        rtm.addCloseHandler(closeReason -> {
            log.info("{}", closeReason);
        });
        rtm.connect();

        return rtm;
    }
}
