package moe.pine.translatebot.translator;

import java.util.Optional;

public interface Translator {
    Optional<String> translate(String text);
}
