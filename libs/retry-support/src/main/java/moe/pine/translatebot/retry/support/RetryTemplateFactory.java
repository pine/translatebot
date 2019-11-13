package moe.pine.translatebot.retry.support;

import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
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

    @SafeVarargs
    public static RetryTemplate createUnlimited(
        final long initialInterval,
        final long maxInterval,
        final double multiplier,
        final Class<? extends Throwable>... exceptions
    ) {
        final AlwaysRetryPolicy alwaysRetryPolicy = new AlwaysRetryPolicy();
        final Map<Class<? extends Throwable>, RetryPolicy> policyMap =
            Stream.of(exceptions)
                .map(exception -> Map.entry(exception, alwaysRetryPolicy))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        final ExceptionClassifierRetryPolicy retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setPolicyMap(policyMap);

        final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(initialInterval);
        backOffPolicy.setMaxInterval(maxInterval);
        backOffPolicy.setMultiplier(multiplier);

        final RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
