package moe.pine.translatebot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Optional<String> translate(final String text) {
        final Optional<String> translatedTextOpt = translator.translate(text);
        if (translatedTextOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(translatedTextOpt.get());

        /*
        final Optional<TextSplitter.Result> processedTextsOpt = textSplitter.split(text);
        if (processedTextsOpt.isEmpty()) {
            return Optional.empty();
        }

        final TextSplitter.Result processedTexts = processedTextsOpt.get();
        final Optional<String> translatedTextOpt = translator.translate(processedTexts.getText());
        if (translatedTextOpt.isEmpty()) {
            return Optional.empty();
        }

        final String translatedText = translatedTextOpt.get();
        log.info("Translated from \"{}\" to \"{}\"", processedTexts.getText(), translatedText);

        final String joinedText = String.format(
            POSTING_TEXT_FORMAT,
            processedTexts.getPreText()
                + translatedText
                + processedTexts.getPostText());

        return Optional.of(joinedText);

         */
    }
}