package moe.pine.translatebot.slack;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OutgoingMessage {
    private String username;
    private String channel;
    private String text;
}
