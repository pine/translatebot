package moe.pine.translatebot.microsoft.translator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.translator.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MicrosoftTranslator implements Translator {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
    private static final String TRANSLATE_PATH = "translate";

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
    public CompletableFuture<Optional<String>> translate(String text) {
        if (StringUtils.isEmpty(text)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final TranslateRequestParameters requestParameters =
            TranslateRequestParameters.builder()
                .from("en")
                .to("ja")
                .build();
        final TranslateRequestBody[] requestBody =
            new TranslateRequestBody[]{new TranslateRequestBody(text)};

        final byte[] bodyValue;
        try {
            bodyValue = OBJECT_MAPPER.writeValueAsBytes(requestBody);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        return webClient.post()
            .uri(uriBuilder -> uriBuilder
                .path(TRANSLATE_PATH)
                .queryParams(requestParameters.toMap())
                .build())
            .contentType(MediaType.APPLICATION_JSON)
            .contentLength(bodyValue.length)
            .header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
            .bodyValue(bodyValue)
            .retrieve()
            .bodyToMono(TranslateResponseBody[].class)
            .<Optional<String>>map(body -> {
                if (body.length == 0) {
                    return Optional.empty();
                }

                final List<TranslateResponseBody.TranslationResult> translations = body[0].getTranslations();
                if (CollectionUtils.isEmpty(translations)) {
                    return Optional.empty();
                }

                final String translatedText = translations.get(0).getText();
                if (StringUtils.isEmpty(translatedText)) {
                    return Optional.empty();
                }

                return Optional.of(translatedText);
            })
            .toFuture();
    }
}
