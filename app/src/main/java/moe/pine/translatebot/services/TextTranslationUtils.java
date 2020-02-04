package moe.pine.translatebot.services;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.gcp.translator.GcpTranslator;
import moe.pine.translatebot.microsoft.translator.MicrosoftTranslator;
import moe.pine.translatebot.services.text_variable.CompositeVariableProcessor;
import moe.pine.translatebot.services.translation.LabeledTranslator;
import moe.pine.translatebot.services.translation.TranslatedText;
import moe.pine.translatebot.services.translation.TranslatorId;
import moe.pine.translatebot.translator.Lang;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class TextTranslationUtils {
    private static final String POSTING_TEXT_FORMAT = ":jp: %s";

    private final TextSplitter textSplitter;
    private final CompositeVariableProcessor compositeVariableProcessor;
    private final List<LabeledTranslator> translators;

    public TextTranslationUtils(
        final TextSplitter textSplitter,
        final CompositeVariableProcessor compositeVariableProcessor,
        final GcpTranslator gcpTranslator,
        final MicrosoftTranslator microsoftTranslator
    ) {
        this.textSplitter = textSplitter;
        this.compositeVariableProcessor = compositeVariableProcessor;
        this.translators = List.of(
            new LabeledTranslator(TranslatorId.GCP_TRANSLATOR, gcpTranslator),
        //    new LabeledTranslator(TranslatorId.MICROSOFT_TRANSLATOR, microsoftTranslator)
        );
    }

    public List<TranslatedText> translate(final String text) throws InterruptedException {
        final Optional<TextSplitter.Result> splitTextsOpt = textSplitter.split(text);
        if (splitTextsOpt.isEmpty()) {
            return List.of();
        }

        final TextSplitter.Result splitTexts = splitTextsOpt.get();
        final String replacedText = compositeVariableProcessor.execute(splitTexts.getText());

        final var translatedTextOptsMonos =
            translators
                .stream()
                .map(labeledTranslator ->
                    labeledTranslator.getTranslator()
                        .translate(Lang.EN, Lang.JA, replacedText)
                        .flatMap(translatedTextOpt -> {
                            if (translatedTextOpt.isEmpty()) {
                                return Mono.empty();
                            }

                            final TranslatorId translatorId = labeledTranslator.getTranslatorId();
                            final String translatedText = translatedTextOpt.get();
                            return Mono.just(new TranslatedText(translatorId, translatedText));
                        })
                )
                .collect(Collectors.toUnmodifiableList());


        final List<TranslatedText> translatedTexts =
            Flux.merge(translatedTextOptsMonos).collectList().block();
        if (translatedTexts == null) {
            return List.of();
        }

        return translatedTexts
            .stream()
            .flatMap(translatedText -> {
                log.info("Translated from \"{}\" to \"{}\"", splitTexts.getText(), translatedText.getText());

                final String joinedText =
                    String.format(
                        POSTING_TEXT_FORMAT,
                        splitTexts.getPreText() + translatedText.getText() + splitTexts.getPostText());
                return Stream.of(new TranslatedText(translatedText.getTranslatorId(), joinedText));
            })
            .collect(Collectors.toUnmodifiableList());
    }
}
