package moe.pine.translatebot.microsoft.translator;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.translator.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;

@Slf4j
public class MicrosoftTranslator implements Translator {
    static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
    static final String TRANSLATE_PATH = "/translate";

    private final String subscriptionKey;
    private final WebClient webClient;

    public MicrosoftTranslator(
        final String subscriptionKey,
        final String baseUrl,
        final WebClient.Builder webClientBuilder
    ) {
        this.subscriptionKey = Objects.requireNonNull(subscriptionKey);
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public Future<Optional<String>> translate(String text) {
        if (StringUtils.isEmpty(text)) {
            return new AsyncResult<>(Optional.empty());
        }

        final var requestParameters = TranslateRequestParameters.builder()
            .from("en")
            .to("ja")
            .build();

        throw new RuntimeException();
    }
}
