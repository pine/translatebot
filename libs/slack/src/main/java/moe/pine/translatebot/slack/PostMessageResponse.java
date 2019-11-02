package moe.pine.translatebot.slack;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PostMessageResponse {
    private String channel;
    private String ts;
}
