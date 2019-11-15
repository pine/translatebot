package moe.pine.translatebot.services;

import com.google.common.collect.Streams;
import com.spotify.futures.CompletableFutures;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.gcp.translator.GcpTranslator;
import moe.pine.translatebot.microsoft.translator.MicrosoftTranslator;
import moe.pine.translatebot.services.text_variable.CompositeVariableProcessor;
import moe.pine.translatebot.services.translation.TranslatedText;
import moe.pine.translatebot.services.translation.TranslatorId;
import moe.pine.translatebot.translator.Translator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class TextTranslationUtils {
    private static final String POSTING_TEXT_FORMAT = ":jp: %s";

    private final TextSplitter textSplitter;
    private final CompositeVariableProcessor compositeVariableProcessor;
    private final List<Pair<TranslatorId, Translator>> translators;

    public TextTranslationUtils(
        final TextSplitter textSplitter,
        final CompositeVariableProcessor compositeVariableProcessor,
        final GcpTranslator gcpTranslator,
        final MicrosoftTranslator microsoftTranslator
    ) {
        this.textSplitter = textSplitter;
        this.compositeVariableProcessor = compositeVariableProcessor;
        this.translators = List.of(
            Pair.of(TranslatorId.GCP_TRANSLATOR, gcpTranslator),
            Pair.of(TranslatorId.MICROSOFT_TRANSLATOR, microsoftTranslator)
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    public List<TranslatedText> translate(final String text) throws InterruptedException {
        final Optional<TextSplitter.Result> splitTextsOpt = textSplitter.split(text);
        if (splitTextsOpt.isEmpty()) {
            return List.of();
        }

        final TextSplitter.Result splitTexts = splitTextsOpt.get();
        final String replacedText = compositeVariableProcessor.execute(splitTexts.getText());

        final List<CompletableFuture<Optional<String>>> translatedTextOptsFutures =
            translators
                .stream()
                .map(pair -> pair.getValue().translate(replacedText))
                .collect(Collectors.toUnmodifiableList());

        final List<Optional<String>> translatedTextOpts;
        try {
            translatedTextOpts = CompletableFutures.allAsList(translatedTextOptsFutures).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return Streams.zip(
            translators.stream().map(Pair::getKey),
            translatedTextOpts.stream(),
            Pair::of
        )
            .flatMap(pair -> {
                final TranslatorId translatorId = pair.getKey();
                final Optional<String> translatedTextOpt = pair.getValue();

                if (translatedTextOpt.isEmpty()) {
                    return Stream.empty();
                }

                final String translatedText = translatedTextOpt.get();
                log.info("Translated from \"{}\" to \"{}\"", splitTexts.getText(), translatedText);

                final String joinedText =
                    String.format(
                        POSTING_TEXT_FORMAT,
                        splitTexts.getPreText() + translatedText + splitTexts.getPostText());
                return Stream.of(new TranslatedText(translatorId, joinedText));
            })
            .collect(Collectors.toUnmodifiableList());
    }
}
