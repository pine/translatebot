package moe.pine.translatebot.config;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.SlackProperties;
import moe.pine.translatebot.slack.SlackClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(SlackProperties.class)
public class SlackConfig {
    @Bean
    public SlackClient slackClient(
        final SlackProperties slackProperties
    ) throws Exception {
        return new SlackClient(slackProperties.getToken());
    }
}
