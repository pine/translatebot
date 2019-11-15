package moe.pine.translatebot.translator;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public interface Translator {
    CompletableFuture<Optional<String>> translate(String text);
}
