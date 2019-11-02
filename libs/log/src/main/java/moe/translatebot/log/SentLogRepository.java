package moe.translatebot.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Objects;

@Slf4j
public class SentLogRepository {
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30L);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final Duration retentionPeriod;
    private final SentLogKeyBuilder sentLogKeyBuilder;

    public SentLogRepository(
        final ReactiveStringRedisTemplate redisTemplate,
        final Duration retentionPeriod
    ) {
        this(redisTemplate, retentionPeriod, new SentLogKeyBuilder());
    }

    SentLogRepository(
        final ReactiveStringRedisTemplate redisTemplate,
        final Duration retentionPeriod,
        final SentLogKeyBuilder sentLogKeyBuilder
    ) {
        this.redisTemplate = redisTemplate;
        this.retentionPeriod = retentionPeriod;
        this.sentLogKeyBuilder = sentLogKeyBuilder;
    }

    public void add(final SentLog sentLog) {
        final SentLogId sentLogId = new SentLogId(sentLog.getChannel(), sentLog.getSourceTs());
        final String key = sentLogKeyBuilder.buildKey(sentLogId);
        final String value;
        try {
            value = OBJECT_MAPPER.writeValueAsString(sentLog);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        redisTemplate.opsForValue().set(key, value, retentionPeriod).block(BLOCK_TIMEOUT);
    }

    public SentLog get(final SentLogId sentLogId) {
        final String key = sentLogKeyBuilder.buildKey(sentLogId);
        final String value = redisTemplate.opsForValue().get(key).block(BLOCK_TIMEOUT);
        Objects.requireNonNull(value);

        try {
            return OBJECT_MAPPER.readValue(value, SentLog.class);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
