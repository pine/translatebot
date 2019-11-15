package moe.pine.translatebot.translator;

import java.util.Optional;
import java.util.concurrent.Future;

public interface Translator {
    Future<Optional<String>> translate(String text);
}
