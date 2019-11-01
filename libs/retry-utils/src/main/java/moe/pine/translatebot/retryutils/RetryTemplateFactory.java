package moe.pine.translatebot.retryutils;

import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RetryTemplateFactory {
    @SafeVarargs
    public static RetryTemplate create(
        final int maxAttempts,
        final long initialInterval,
        final double multiplier,
        final Class<? extends Throwable>... exceptions
    ) {
        final Map<Class<? extends Throwable>, Boolean> retryableExceptions =
            Stream.of(exceptions)
                .map(exception -> Map.entry(exception, true))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        final RetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, retryableExceptions);

        final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMultiplier(multiplier);

        final RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
