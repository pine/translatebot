package moe.pine.translatebot.microsoft.translator;

import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class TranslateRequestParameters {
    static final String API_VERSION = "3.0";

    String from;
    String to;

    public Map<String, ?> toMap() {
        final HashMap<String, String> m = new HashMap<>();
        m.put("api-version", API_VERSION);

        if (StringUtils.isNotEmpty(from)) m.put("from", from);
        if (StringUtils.isNotEmpty(to)) m.put("to", to);

        return m;
    }
}
