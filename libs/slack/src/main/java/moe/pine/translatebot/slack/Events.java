package moe.pine.translatebot.slack;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

class Events {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static Optional<Event> parse(String content) {
        final HelloEvent helloEvent = readJson(content, HelloEvent.class);
        if (StringUtils.isEmpty(helloEvent.getType())) {
            return Optional.empty();
        }

        switch (helloEvent.getType()) {
            case HelloEvent.TYPE:
                return Optional.of(helloEvent);
            case MessageEvent.TYPE:
                return Optional.of(readJson(content, MessageEvent.class));
            case UserChangeEvent.TYPE:
                return Optional.of(readJson(content, UserChangeEvent.class));
            default:
                return Optional.empty();
        }
    }

    private static <T> T readJson(final String content, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(content, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
