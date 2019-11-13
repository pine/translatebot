package moe.pine.translatebot.microsoft.translator;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.translator.Translator;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class MicrosoftTranslator implements Translator {
    public MicrosoftTranslator(
        final String subscriptionKey
    ) {
        Objects.requireNonNull(subscriptionKey);

    }

    @Override
    public Optional<String> translate(String text) {
        throw new RuntimeException();
    }
}
