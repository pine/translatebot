package moe.pine.translatebot.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
public class SentLogRepository {
    private static final Duration BLOCK_TIMEOUT = Duration.ofSeconds(30L);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final Duration retentionPeriod;
    private final SentLogKeyBuilder sentLogKeyBuilder;

    public SentLogRepository(
        ReactiveStringRedisTemplate redisTemplate,
        Duration retentionPeriod
    ) {
        this(redisTemplate, retentionPeriod, new SentLogKeyBuilder());
    }

    SentLogRepository(
        ReactiveStringRedisTemplate redisTemplate,
        Duration retentionPeriod,
        SentLogKeyBuilder sentLogKeyBuilder
    ) {
        this.redisTemplate = redisTemplate;
        this.retentionPeriod = retentionPeriod;
        this.sentLogKeyBuilder = sentLogKeyBuilder;
    }

    public void add(SentLog sentLog) {
        SentLogId sentLogId = new SentLogId(sentLog.getChannel(), sentLog.getSourceTs());
        String key = sentLogKeyBuilder.buildKey(sentLogId);
        String value;
        try {
            value = OBJECT_MAPPER.writeValueAsString(sentLog);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        redisTemplate.opsForValue().set(key, value, retentionPeriod).block(BLOCK_TIMEOUT);
    }

    public Optional<SentLog> get(SentLogId sentLogId) {
        String key = sentLogKeyBuilder.buildKey(sentLogId);
        String value = redisTemplate.opsForValue().get(key).block(BLOCK_TIMEOUT);
        if (StringUtils.isEmpty(value)) {
            return Optional.empty();
        }

        SentLog sentLog;
        try {
            sentLog = OBJECT_MAPPER.readValue(value, SentLog.class);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
        return Optional.of(sentLog);
    }
}
