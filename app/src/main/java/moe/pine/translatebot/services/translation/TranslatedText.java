package moe.pine.translatebot.services.translation;

import lombok.Value;

@Value
public class TranslatedText {
    TranslatorId translatorId;
    String text;
}
