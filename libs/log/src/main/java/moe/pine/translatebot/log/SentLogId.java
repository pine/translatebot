package moe.pine.translatebot.log;

import lombok.Value;

@Value
public class SentLogId {
    String channel;
    String sourceTs;
}
