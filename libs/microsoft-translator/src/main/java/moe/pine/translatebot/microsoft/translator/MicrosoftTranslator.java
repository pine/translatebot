package moe.pine.translatebot.microsoft.translator;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.translator.Translator;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class MicrosoftTranslator implements Translator {
    static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

    private final String subscriptionKey;
    private final String endpoint;

    public MicrosoftTranslator(
        final String subscriptionKey,
        final String endpoint
    ) {
        this.subscriptionKey = Objects.requireNonNull(subscriptionKey);
        this.endpoint = Objects.requireNonNull(endpoint);
    }

    @Override
    public Optional<String> translate(String text) {
        if (StringUtils.isEmpty(text)) {
            return Optional.empty();
        }


        throw new RuntimeException();
    }
}
