package moe.pine.translatebot.slack;

import lombok.Data;

/**
 * <a href="https://api.slack.com/events/user_change">user_change event | Slack</a>
 */
@Data
public class UserChangeEvent implements Event {
    public static final String TYPE = "user_change";

    @Override
    public String getType() {
        return TYPE;
    }
}
