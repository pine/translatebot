package moe.pine.translatebot.translator;

import reactor.core.publisher.Mono;

import java.util.Optional;

public interface Detector {
    Mono<Optional<Lang>> detect(String content);
}
