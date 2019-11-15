package moe.pine.translatebot.services.translation;

import lombok.Value;
import moe.pine.translatebot.translator.Translator;

@Value
public class LabeledTranslator {
    TranslatorId translatorId;
    Translator translator;
}
