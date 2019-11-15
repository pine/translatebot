package moe.pine.translatebot.microsoft.translator;

import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Value
@Builder
public class TranslateRequestParameters {
    static final String API_VERSION = "3.0";

    String from;
    String to;

    public MultiValueMap<String, String> toMap() {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("api-version", API_VERSION);

        if (StringUtils.isNotEmpty(from)) parameters.add("from", from);
        if (StringUtils.isNotEmpty(to)) parameters.add("to", to);

        return parameters;
    }
}
