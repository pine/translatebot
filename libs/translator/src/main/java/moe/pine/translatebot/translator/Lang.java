package moe.pine.translatebot.translator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Lang {
    EN("en"),
    JA("ja"),
    ;

    private final String code;
}
