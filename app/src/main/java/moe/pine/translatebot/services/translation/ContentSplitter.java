package moe.pine.translatebot.services.translation;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ContentSplitter {
    private static final String SKIP_PATTERN = "(?:\\s|(?:<![a-z]+>)|(?:<@[A-Z0-9]+>)|(?::[\\w-+]+:))*";
    private static final Pattern PRE_TEXT_PATTERN =
        Pattern.compile("\\A(?<preText>" + SKIP_PATTERN + ")(?<behindText>.*)\\z", Pattern.DOTALL);
    private static final Pattern POST_TEXT_PATTERN =
        Pattern.compile("\\A(?<centerText>.*?)(?<postText>" + SKIP_PATTERN + ")\\z", Pattern.DOTALL);

    public Optional<ContentComponents> split(String content) {
        Matcher preTextMatcher = PRE_TEXT_PATTERN.matcher(content);
        if (!preTextMatcher.matches()) {
            return Optional.empty();
        }

        String preText = preTextMatcher.group("preText");
        String behindText = preTextMatcher.group("behindText");

        Matcher postTextMatcher = POST_TEXT_PATTERN.matcher(behindText);
        if (!postTextMatcher.matches()) {
            return Optional.empty();
        }

        String centerText = postTextMatcher.group("centerText");
        String postText = postTextMatcher.group("postText");
        return Optional.of(new ContentComponents(preText, centerText, postText));
    }
}
