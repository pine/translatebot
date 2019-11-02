package moe.pine.translatebot.config;

import moe.translatebot.log.SentLogRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
public class LogConfig {
    @Bean
    public SentLogRepository sentLogRepository(
        final ReactiveStringRedisTemplate redisTemplate
    ) {
        return new SentLogRepository(redisTemplate);
    }
}
