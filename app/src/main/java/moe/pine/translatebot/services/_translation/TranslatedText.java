package moe.pine.translatebot.services._translation;

import lombok.Value;

@Value
public class TranslatedText {
    TranslatorId translatorId;
    String text;
}
