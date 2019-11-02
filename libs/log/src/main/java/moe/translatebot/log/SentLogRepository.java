package moe.translatebot.log;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@RequiredArgsConstructor
public class SentLogRepository {
    private final ReactiveStringRedisTemplate redisTemplate;

    public void add(final SentLog sentLog) {
    }

    public SentLog get(final String channel, final String sourceTs) {
        throw new RuntimeException();
    }
}
