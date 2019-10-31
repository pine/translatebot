package moe.pine.translatebot.services;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.SlackProperties;
import moe.pine.translatebot.slack.Event;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.translation.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SlackService {
    private static final Pattern EMOTICON_ONLY_TEXT_PATTERN =
        Pattern.compile("^(?:\\s*:[\\w-+]+:)+\\s*$");

    private final SlackProperties slackProperties;
    private final SlackClient slackClient;
    private final Translator translator;
    private final Instant startupTime;

    public SlackService(
        final SlackProperties slackProperties,
        final SlackClient slackClient,
        final Translator translator,
        final Clock clock
    ) {
        this.slackProperties = slackProperties;
        this.slackClient = slackClient;
        this.translator = translator;

        startupTime = clock.instant();
        slackClient.addEventListener(this::onEvent);
    }

    private void onEvent(final Event event) {
        log.info("onEvent: {}", event);

        if (event instanceof MessageEvent) {
            onMessageEvent((MessageEvent) event);
        }
    }

    private void onMessageEvent(final MessageEvent messageEvent) {
        final double ts = Double.parseDouble(messageEvent.getTs());
        if (ts < startupTime.getEpochSecond()) {
            return;
        }

        final Set<String> channels = slackProperties.getChannels();
        if (!channels.contains(messageEvent.getChannel())) {
            return;
        }

        if (StringUtils.isNotEmpty(messageEvent.getSubtype())) {
            return;
        }

        final Matcher matcher = EMOTICON_ONLY_TEXT_PATTERN.matcher(messageEvent.getText());
        if (matcher.matches()) {
            return;
        }

        final String text = messageEvent.getText();
        final boolean isJapanese = translator.isJapanese(text);
        if (isJapanese) {
            log.info("\"{}\" was already guessed to be in Japanese.", text);
            return;
        }

        translator.translateToJapanese(text)
            .ifPresent(translatedText -> {
                log.info("Translated from \"{}\" to \"{}\"", text, translatedText);

                final String postingText = ":flag-jp: " + translatedText;
                slackClient.postMessage(messageEvent.getChannel(), postingText);
            });
    }
}
