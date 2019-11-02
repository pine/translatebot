package moe.translatebot.log;

import lombok.Builder;
import lombok.Value;

@Value
public class SentLogId {
    private String channel;
    private String sourceTs;
}
