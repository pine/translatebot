package moe.pine.translatebot.config;

import moe.pine.translatebot.properties.LogProperties;
import moe.pine.translatebot.log.SentLogRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
@EnableConfigurationProperties(LogProperties.class)
public class LogConfig {
    @Bean
    public SentLogRepository sentLogRepository(
        final ReactiveStringRedisTemplate redisTemplate,
        final LogProperties logProperties
    ) {
        return new SentLogRepository(
            redisTemplate,
            logProperties.getRetentionPeriod()
        );
    }
}
