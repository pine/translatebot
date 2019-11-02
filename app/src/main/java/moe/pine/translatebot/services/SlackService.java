package moe.pine.translatebot.services;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.SlackProperties;
import moe.pine.translatebot.slack.Event;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.MessageEvent.Subtypes;
import moe.pine.translatebot.slack.PostMessageRequest;
import moe.pine.translatebot.slack.PostMessageResponse;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.slack.UpdateMessageRequest;
import moe.pine.translatebot.translation.Translator;
import moe.translatebot.log.SentLog;
import moe.translatebot.log.SentLogId;
import moe.translatebot.log.SentLogRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
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
    private final SentLogRepository sentLogRepository;
    private final Instant startupTime;

    public SlackService(
        final SlackProperties slackProperties,
        final SlackClient slackClient,
        final Translator translator,
        final SentLogRepository sentLogRepository,
        final Clock clock
    ) {
        this.slackProperties = slackProperties;
        this.slackClient = slackClient;
        this.translator = translator;
        this.sentLogRepository = sentLogRepository;

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
        } else if (Subtypes.MESSAGE_CHANGED.equals(messageEvent.getSubtype())) {
            onMessageChangedEvent(messageEvent);
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
                final PostMessageRequest postMessageRequest =
                    PostMessageRequest.builder()
                        .username(slackProperties.getUsername())
                        .threadTs(messageEvent.getThreadTs())
                        .channel(messageEvent.getChannel())
                        .text(postingText)
                        .iconUrl(slackProperties.getIconUrl())
                        .replyBroadcast(false)
                        .build();
                final PostMessageResponse postMessageResponse =
                    slackClient.postMessage(postMessageRequest);

                final SentLog sentLog = SentLog.builder()
                    .channel(messageEvent.getChannel())
                    .sourceTs(messageEvent.getTs())
                    .destinationTs(postMessageResponse.getTs())
                    .build();
                sentLogRepository.add(sentLog);
            });
    }

    private void onMessageChangedEvent(final MessageEvent messageEvent) {
        final SentLogId sentLogId =
            new SentLogId(
                messageEvent.getChannel(),
                messageEvent.getMessage().getTs());
        final Optional<SentLog> sentLogOpt = sentLogRepository.get(sentLogId);
        if (sentLogOpt.isEmpty()) {
            return;
        }

        final String text = messageEvent.getMessage().getText();
        final Optional<String> translatedTextOpt = translator.translate(text);
        if (translatedTextOpt.isEmpty()) {
            return;
        }

        final String translatedText = translatedTextOpt.get();
        final String postingText = String.format(POSTING_TEXT_FORMAT, translatedText);
        log.info("Translated from \"{}\" to \"{}\"", text, translatedText);

        final SentLog sentLog = sentLogOpt.get();
        final UpdateMessageRequest updateMessageRequest =
            UpdateMessageRequest.builder()
                .channel(sentLog.getChannel())
                .text(postingText)
                .ts(sentLog.getDestinationTs())
                .build();
        slackClient.updateMessage(updateMessageRequest);
    }
}
