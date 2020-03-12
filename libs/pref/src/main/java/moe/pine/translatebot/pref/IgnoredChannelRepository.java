package moe.pine.translatebot.pref;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.util.StringUtils;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
public class IgnoredChannelRepository {
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30L);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final IgnoredChannelKeyBuilder ignoredChannelKeyBuilder;

    public IgnoredChannelRepository(
        ReactiveStringRedisTemplate redisTemplate
    ) {
        this(redisTemplate, new IgnoredChannelKeyBuilder());
    }

    IgnoredChannelRepository(
        ReactiveStringRedisTemplate redisTemplate,
        IgnoredChannelKeyBuilder ignoredChannelKeyBuilder
    ) {
        this.redisTemplate = redisTemplate;
        this.ignoredChannelKeyBuilder = ignoredChannelKeyBuilder;
    }

    public void set(String channel, boolean ignored) {
        String key = ignoredChannelKeyBuilder.buildKey(channel);
        String value;
        try {
            value = OBJECT_MAPPER.writeValueAsString(ignored);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        redisTemplate.opsForValue().set(key, value).block(BLOCK_TIMEOUT);
    }

    public Optional<Boolean> get(String channel) {
        String key = ignoredChannelKeyBuilder.buildKey(channel);
        String value = redisTemplate.opsForValue().get(key).block(BLOCK_TIMEOUT);
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }

        Boolean ignored;
        try {
            ignored = OBJECT_MAPPER.readValue(value, Boolean.class);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
        return Optional.ofNullable(ignored);
    }
}
