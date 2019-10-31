package moe.pine.translatebot.config;

import moe.pine.translatebot.translation.Translator;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TranslationConfig {
    public Translator translator() {
        final var translator = new Translator();

        return translator;
    }
}
