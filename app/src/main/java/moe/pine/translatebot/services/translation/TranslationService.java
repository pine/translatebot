package moe.pine.translatebot.services.translation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.services.text_variable.CompositeVariableProcessor;
import moe.pine.translatebot.translator.Lang;
import moe.pine.translatebot.translator.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {
    private final ContentSplitter contentSplitter;
    private final CompositeVariableProcessor compositeVariableProcessor;
    private final Translator translator;

    public Optional<String> translate(String content) throws InterruptedException {
        if (StringUtils.isEmpty(content)) {
            return Optional.empty();
        }

        Optional<ContentComponents> contentComponentsOpt = contentSplitter.split(content);
        if (contentComponentsOpt.isEmpty()) {
            return Optional.empty();
        }

        ContentComponents contentComponents = contentComponentsOpt.get();
        Lang sourceLang = translator.detect(contentComponents.getText())
            .blockOptional()
            .orElseThrow(() -> new RuntimeException("Unable to detect language"));
        log.info("Language detected : {}", sourceLang);

        String processedText = compositeVariableProcessor.execute(contentComponents.getText());

        Lang destLang = sourceLang.getDestinationLang();
        Optional<String> translatedTextOpt =
            translator.translate(sourceLang, destLang, processedText)
                .blockOptional()
                .flatMap(Function.identity());
        if (translatedTextOpt.isEmpty()) {
            return Optional.empty();
        }

        String translatedText = translatedTextOpt.get();
        if (StringUtils.isEmpty(translatedText)) {
            return Optional.empty();
        }

        log.info("Translated from \"{}\" to \"{}\"", processedText, translatedText);
        return Optional.of(destLang.getFlag() + " " + translatedText);
    }
}
