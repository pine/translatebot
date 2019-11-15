package moe.pine.translatebot.microsoft.translator;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class TranslateRequestBody {
    @JsonProperty("Text")
    String text;
}
