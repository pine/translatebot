package moe.pine.translatebot.slack;

import lombok.Builder;
import lombok.Value;

/**
 * @see <a href="https://api.slack.com/methods/chat.update">chat.update method | Slack</a>
 */
@Value
@Builder
public class UpdateMessageRequest {
    private String channel;
    private String text;
    private String ts;
}
