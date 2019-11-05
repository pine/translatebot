package moe.pine.translatebot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.services.text_variable.CompositeVariableProcessor;
import moe.pine.translatebot.translation.Translator;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextTranslationUtils {
    private static final String POSTING_TEXT_FORMAT = ":jp: %s";

    private final Translator translator;
    private final TextSplitter textSplitter;
    private final CompositeVariableProcessor compositeVariableProcessor;

    public Optional<String> translate(final String text) {
        final Optional<TextSplitter.Result> splitTextsOpt = textSplitter.split(text);
        if (splitTextsOpt.isEmpty()) {
            return Optional.empty();
        }

        final TextSplitter.Result splitTexts = splitTextsOpt.get();
        final String replacedText = compositeVariableProcessor.execute(splitTexts.getText());
        final Optional<String> translatedTextOpt = translator.translate(replacedText);
        if (translatedTextOpt.isEmpty()) {
            return Optional.empty();
        }

        final String translatedText = translatedTextOpt.get();
        log.info("Translated from \"{}\" to \"{}\"", splitTexts.getText(), translatedText);

        final String joinedText =
            String.format(
                POSTING_TEXT_FORMAT,
                splitTexts.getPreText() + translatedText + splitTexts.getPostText());

        return Optional.of(joinedText);
    }
}
