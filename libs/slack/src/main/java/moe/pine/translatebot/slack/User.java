package moe.pine.translatebot.slack;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
    private String id;
    private String displayName;
}
