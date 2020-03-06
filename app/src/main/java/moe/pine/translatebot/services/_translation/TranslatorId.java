package moe.pine.translatebot.services._translation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TranslatorId {
    GCP_TRANSLATOR("Google翻訳"),
    MICROSOFT_TRANSLATOR("Bing翻訳"),
    ;

    private final String title;
}
