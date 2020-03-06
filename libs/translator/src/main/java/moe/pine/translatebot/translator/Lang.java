package moe.pine.translatebot.translator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Lang {
    EN("en", ":us:"),
    JA("ja", ":jp:"),
    ;

    private final String code;
    private final String flag;

    public Lang getDestinationLang() {
        switch (this) {
            case EN:
                return JA;
            case JA:
                return EN;
            default:
                throw new IllegalArgumentException("not supported");
        }
    }
}
