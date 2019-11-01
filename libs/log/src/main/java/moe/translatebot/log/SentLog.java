package moe.translatebot.log;

import lombok.Data;

@Data
public class SentLog {
    private String channel;
    private String sourceTs;
    private String destinationTs;
}
