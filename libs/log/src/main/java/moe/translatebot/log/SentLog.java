package moe.translatebot.log;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SentLog {
    private String channel;
    private String sourceTs;
    private String destinationTs;
}
