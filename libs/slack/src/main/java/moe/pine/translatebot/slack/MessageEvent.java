package moe.pine.translatebot.slack;

import lombok.Data;

@Data
public class MessageEvent implements Event {
    public static final String TYPE = "message";

    private String type;
    private String subtype;
    private String channel;
    private String user;
    private String text;
    private String ts;
}
