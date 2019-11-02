package moe.translatebot.log;

import java.util.Objects;

class SentLogKeyBuilder {
    private static final String KEY_FORMAT = "sent_log:%s:%s";

    String buildKey(final SentLogId sentLogId) {
        Objects.requireNonNull(sentLogId, "`sentLogId` is required.");
        Objects.requireNonNull(sentLogId.getChannel(), "`channel` is required.");
        Objects.requireNonNull(sentLogId.getSourceTs(), "`sourceTs` is required.");

        return String.format(KEY_FORMAT, sentLogId.getChannel(), sentLogId.getSourceTs());
    }
}
