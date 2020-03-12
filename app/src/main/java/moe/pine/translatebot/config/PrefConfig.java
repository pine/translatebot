package moe.pine.translatebot.config;

import moe.pine.translatebot.pref.IgnoredChannelRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
public class PrefConfig {
    @Bean
    public IgnoredChannelRepository ignoredChannelRepository(
        ReactiveStringRedisTemplate redisTemplate
    ) {
        return new IgnoredChannelRepository(redisTemplate);
    }
}
