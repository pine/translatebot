package moe.pine.translatebot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.gcp.translator.GcpTranslator;
import moe.pine.translatebot.microsoft.translator.MicrosoftTranslator;
import moe.pine.translatebot.services.text_variable.CompositeVariableProcessor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextTranslationUtils {
    private static final String POSTING_TEXT_FORMAT = ":jp: %s";

    private final GcpTranslator gcpTranslator;
    private final MicrosoftTranslator microsoftTranslator;
    private final TextSplitter textSplitter;
    private final CompositeVariableProcessor compositeVariableProcessor;

    public Optional<String> translate(final String text) throws InterruptedException {
        final Optional<TextSplitter.Result> splitTextsOpt = textSplitter.split(text);
        if (splitTextsOpt.isEmpty()) {
            return Optional.empty();
        }

        final TextSplitter.Result splitTexts = splitTextsOpt.get();
        final String replacedText = compositeVariableProcessor.execute(splitTexts.getText());
        final Optional<String> translatedTextOpt;
        try {
            translatedTextOpt = gcpTranslator.translate(replacedText).get();
            //translatedTextOpt = microsoftTranslator.translate(replacedText).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return Optional.empty();
        }

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
