package moe.pine.translatebot.translator;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.v3beta1.LocationName;
import com.google.cloud.translate.v3beta1.TranslateTextRequest;
import com.google.cloud.translate.v3beta1.TranslateTextResponse;
import com.google.cloud.translate.v3beta1.Translation;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import com.google.cloud.translate.v3beta1.TranslationServiceSettings;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.retry_support.RetryTemplateFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.support.RetryTemplate;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
public class GcpTranslator implements Translator {
    private final TranslationServiceClient translationServiceClient;
    private final LocationName locationName;
    private final RetryTemplate retryTemplate;

    public GcpTranslator(
        final InputStream credentialsStream,
        final String projectId,
        final String location
    ) throws IOException {
        this(credentialsStream,
            projectId,
            location,
            RetryTemplateFactory.create(5, 500, 2.0, ApiException.class));
    }

    GcpTranslator(
        final InputStream credentialsStream,
        final String projectId,
        final String location,
        final RetryTemplate retryTemplate
    ) throws IOException {
        final GoogleCredentials credentials =
            GoogleCredentials.fromStream(credentialsStream);
        final CredentialsProvider credentialsProvider =
            FixedCredentialsProvider.create(credentials);
        final TranslationServiceSettings translationServiceSettings =
            TranslationServiceSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .build();

        translationServiceClient =
            TranslationServiceClient.create(translationServiceSettings);
        locationName =
            LocationName.newBuilder()
                .setProject(projectId)
                .setLocation(location)
                .build();

        this.retryTemplate = retryTemplate;
    }

    @Override
    public Mono<Optional<String>> translate(
        final Lang from,
        final Lang to,
        final String content
    ) {
        return Mono.create(sink -> sink.success(translateImpl(from, to, content)));
    }

    Optional<String> translateImpl(
        final Lang from,
        final Lang to,
        final String content
    ) {
        if (StringUtils.isEmpty(content)) {
            return Optional.empty();
        }

        final TranslateTextRequest translateTextRequest =
            TranslateTextRequest.newBuilder()
                .setParent(locationName.toString())
                .setMimeType("text/plain")
                .setSourceLanguageCode(from.getCode())
                .setTargetLanguageCode(to.getCode())
                .addContents(content)
                .build();

        final TranslateTextResponse translateTextResponse =
            retryTemplate.execute(ctx ->
                translationServiceClient.translateText(translateTextRequest));

        final List<Translation> translations = translateTextResponse.getTranslationsList();
        if (translations.isEmpty()) {
            return Optional.empty();
        }

        final String translatedText = translations.get(0).getTranslatedText();
        if (StringUtils.isEmpty(translatedText)) {
            return Optional.empty();
        }

        return Optional.of(translatedText);
    }

    @Override
    public Mono<Optional<Lang>> detect(String content) {
        throw new RuntimeException("not implemented");
    }
}
