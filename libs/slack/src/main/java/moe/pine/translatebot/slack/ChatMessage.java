package moe.pine.translatebot.slack;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChatMessage {
    private String channel;
    private String text;
}
