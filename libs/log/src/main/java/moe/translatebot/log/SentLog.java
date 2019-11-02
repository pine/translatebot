package moe.translatebot.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentLog {
    private String channel;
    private String sourceTs;
    private String destinationTs;
}
