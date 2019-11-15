package moe.pine.translatebot.slack;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * @see <a href="https://api.slack.com/methods/chat.update">chat.update method | Slack</a>
 */
@Value
@Builder
public class UpdateMessageRequest {
    private String channel;
    private List<TextField> textFields;
    private String ts;
}
