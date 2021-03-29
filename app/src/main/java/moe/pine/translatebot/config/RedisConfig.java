package moe.pine.translatebot.config;

import lombok.extern.slf4j.Slf4j;
import moe.pine.heroku.addons.HerokuRedis;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Slf4j
@Configuration
public class RedisConfig {
    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        HerokuRedis redis = HerokuRedis.get();
        log.info("HerokuRedis: {}", redis);

        if (redis != null) {
            configuration.setHostName(redis.getHost());
            configuration.setPort(redis.getPort());
            configuration.setPassword(redis.getPassword());
        }

        return configuration;
    }

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(
            RedisStandaloneConfiguration redisStandaloneConfiguration
    ) {
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }
}
