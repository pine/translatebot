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
        final HelloEvent helloEvent;
        try {
            helloEvent = OBJECT_MAPPER.readValue(content, HelloEvent.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (helloEvent == null || StringUtils.isEmpty(helloEvent.getType())) {
            return Optional.empty();
        }

        switch (helloEvent.getType()) {
            case HelloEvent.TYPE:
                return Optional.of(helloEvent);
            case MessageEvent.TYPE:
                final MessageEvent messageEvent;
                try {
                    messageEvent = OBJECT_MAPPER.readValue(content, MessageEvent.class);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                return Optional.of(messageEvent);
            default:
                return Optional.empty();
        }
    }
}
