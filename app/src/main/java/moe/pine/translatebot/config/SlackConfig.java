package moe.pine.translatebot.config;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.rtm.RTMClient;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.SlackProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties(SlackProperties.class)
public class SlackConfig {
    @Bean
    public Slack slack() {
        return new Slack();
    }

    @Bean(destroyMethod = "disconnect")
    public RTMClient rtmClient(
            final Slack slack,
            final SlackProperties slackProperties
    ) throws IOException {
        return slack.rtm(slackProperties.getToken());
    }
}
