package moe.pine.translatebot.slack;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PostMessageRequest {
    private String username;
    private String threadTs;
    private String channel;
    private List<TextField> textFields;
    private String iconUrl;
    private boolean replyBroadcast;
}
