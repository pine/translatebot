package moe.pine.translatebot.services;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TextSplitter {
    private static final Pattern PRE_TEXT_PATTERN =
        Pattern.compile("^(?<preText>(?:\\s|(?:<![a-z]+>)|(?:<@[A-Z0-9]+>)|(?::[\\w-+]+:))*)(?<behindText>.*)$");
    private static final Pattern POST_TEXT_PATTERN =
        Pattern.compile("^(?<centerText>.*?)(?<postText>(?:\\s|(?:<![a-z]+>)|(?:<@[A-Z0-9]+>)|(?::[\\w-+]+:))*)$");

    @Value
    @Builder
    public static class Result {
        private String preText;
        private String text;
        private String postText;
    }

    public Optional<Result> split(final String text) {
        final Matcher preTextMatcher = PRE_TEXT_PATTERN.matcher(text);
        if (!preTextMatcher.matches()) {
            return Optional.empty();
        }

        final String preText = preTextMatcher.group("preText");
        final String behindText = preTextMatcher.group("behindText");

        final Matcher postTextMatcher = POST_TEXT_PATTERN.matcher(behindText);
        if (!postTextMatcher.matches()) {
            return Optional.empty();
        }

        final String centerText = postTextMatcher.group("centerText");
        final String postText = postTextMatcher.group("postText");
        return Optional.of(
            Result.builder()
                .preText(preText)
                .text(centerText)
                .postText(postText)
                .build());
    }
}
