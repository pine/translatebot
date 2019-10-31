package moe.pine.translatebot.translation;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.v3beta1.DetectLanguageRequest;
import com.google.cloud.translate.v3beta1.DetectLanguageResponse;
import com.google.cloud.translate.v3beta1.DetectedLanguage;
import com.google.cloud.translate.v3beta1.LocationName;
import com.google.cloud.translate.v3beta1.TranslateTextRequest;
import com.google.cloud.translate.v3beta1.TranslateTextResponse;
import com.google.cloud.translate.v3beta1.Translation;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import com.google.cloud.translate.v3beta1.TranslationServiceSettings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
public class Translator {
    private final TranslationServiceClient translationServiceClient;
    private final LocationName locationName;

    public Translator(
        final InputStream credentialsStream,
        final String projectId,
        final String location
    ) {
        try {
            final GoogleCredentials credentials =
                GoogleCredentials.fromStream(credentialsStream);
            final CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isJapanese(final String content) {
        final DetectLanguageRequest detectLanguageRequest =
            DetectLanguageRequest.newBuilder()
                .setParent(locationName.toString())
                .setMimeType("text/plain")
                .setContent(content)
                .build();

        final DetectLanguageResponse detectLanguageResponse =
            translationServiceClient.detectLanguage(detectLanguageRequest);
        final List<DetectedLanguage> detectedLanguages = detectLanguageResponse.getLanguagesList();

        return detectedLanguages.stream()
            .anyMatch(v -> v.getLanguageCode().equals(Locale.JAPANESE.getLanguage()));
    }

    public Optional<String> translateToJapanese(final String content) {
        if (StringUtils.isEmpty(content)) {
            return Optional.empty();
        }

        final TranslateTextRequest translateTextRequest =
            TranslateTextRequest.newBuilder()
                .setParent(locationName.toString())
                .setMimeType("text/plain")
                .setTargetLanguageCode(Locale.JAPANESE.getLanguage())
                .addContents(content)
                .build();

        final TranslateTextResponse translateTextResponse =
            translationServiceClient.translateText(translateTextRequest);

        final List<Translation> translations = translateTextResponse.getTranslationsList();
        if (translations.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(translations.get(0).getTranslatedText());
    }
}
