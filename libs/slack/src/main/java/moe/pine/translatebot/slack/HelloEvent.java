package moe.pine.translatebot.slack;

import lombok.Data;

@Data
public class HelloEvent implements Event {
    public static final String TYPE = "hello";

    private String type;
}
