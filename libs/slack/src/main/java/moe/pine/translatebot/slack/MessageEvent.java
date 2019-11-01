package moe.pine.translatebot.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @see <a href="https://github.com/nlopes/slack/blob/master/messages.go">slack/messages.go at master · nlopes/slack</a>
 */
@Data
public class MessageEvent implements Event {
    public static final String TYPE = "message";

    private String type;
    private String channel;
    private String user;
    private String text;
    private String ts;
    @JsonProperty("thread_ts")
    private String threadTs;
    private String subtype;
    private Boolean hidden;
    private MessageEvent message;
}
