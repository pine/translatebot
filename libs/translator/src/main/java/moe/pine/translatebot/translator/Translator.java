package moe.pine.translatebot.translator;

import reactor.core.publisher.Mono;

import java.util.Optional;

public interface Translator {
    Mono<Optional<String>> translate(
        Lang from,
        Lang to,
        String content
    );
}
