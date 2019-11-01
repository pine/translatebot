package moe.pine.translatebot.services;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.SlackProperties;
import moe.pine.translatebot.slack.Event;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.MessageEvent.Subtypes;
import moe.pine.translatebot.slack.OutgoingMessage;
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
    private static final String POSTING_TEXT_FORMAT = ":jp: %s";
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

        if (StringUtils.isEmpty(messageEvent.getSubtype()) ||
            Subtypes.THREAD_BROADCAST.equals(messageEvent.getSubtype())) {
            onMessageSentEvent(messageEvent);
        }
    }

    private void onMessageSentEvent(final MessageEvent messageEvent) {
        final Matcher matcher = EMOTICON_ONLY_TEXT_PATTERN.matcher(messageEvent.getText());
        if (matcher.matches()) {
            return;
        }

        final String text = messageEvent.getText();
        translator.translate(text)
            .ifPresent(translatedText -> {
                log.info("Translated from \"{}\" to \"{}\"", text, translatedText);

                final String postingText = String.format(POSTING_TEXT_FORMAT, translatedText);
                final OutgoingMessage outgoingMessage =
                    OutgoingMessage.builder()
                        .username(slackProperties.getUsername())
                        .threadTs(messageEvent.getThreadTs())
                        .channel(messageEvent.getChannel())
                        .text(postingText)
                        .iconUrl(slackProperties.getIconUrl())
                        .replyBroadcast(false)
                        .build();

                slackClient.postMessage(outgoingMessage);
            });
    }
}
