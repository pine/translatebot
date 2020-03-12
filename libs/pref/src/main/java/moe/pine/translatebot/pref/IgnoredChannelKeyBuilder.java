package moe.pine.translatebot.pref;

import java.util.Objects;

class IgnoredChannelKeyBuilder {
    private static final String KEY_FORMAT = "ignored_channel:%s";

    String buildKey(String channel) {
        Objects.requireNonNull(channel);
        return String.format(KEY_FORMAT, channel);
    }
}
