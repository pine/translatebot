package moe.pine.translatebot.microsoft.translator;

import lombok.Value;

import java.util.List;

@Value
public class TranslateResponseBody {
    List<TranslationResult> translations;

    @Value
    public static class TranslationResult {
        String text;
    }
}
