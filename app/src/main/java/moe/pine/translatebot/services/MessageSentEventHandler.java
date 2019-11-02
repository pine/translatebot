package moe.pine.translatebot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.SlackProperties;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.PostMessageRequest;
import moe.pine.translatebot.slack.PostMessageResponse;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.translation.Translator;
import moe.translatebot.log.SentLog;
import moe.translatebot.log.SentLogRepository;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSentEventHandler {
    private static final String POSTING_TEXT_FORMAT = ":jp: %s";
    private static final Pattern EMOTICON_ONLY_TEXT_PATTERN =
        Pattern.compile("^(?:\\s*:[\\w-+]+:)+\\s*$");

    private final SlackProperties slackProperties;
    private final SlackClient slackClient;
    private final Translator translator;
    private final SentLogRepository sentLogRepository;

    public void execute(final MessageEvent messageEvent) {
        final Matcher matcher = EMOTICON_ONLY_TEXT_PATTERN.matcher(messageEvent.getText());
        if (matcher.matches()) {
            return;
        }

        final String text = messageEvent.getText();
        translator.translate(text)
            .ifPresent(translatedText -> {
                log.info("Translated from \"{}\" to \"{}\"", text, translatedText);

                final String postingText = String.format(POSTING_TEXT_FORMAT, translatedText);
                final boolean replyBroadcast =
                    MessageEvent.Subtypes.THREAD_BROADCAST.equals(messageEvent.getSubtype());
                final PostMessageRequest postMessageRequest =
                    PostMessageRequest.builder()
                        .username(slackProperties.getUsername())
                        .threadTs(messageEvent.getThreadTs())
                        .channel(messageEvent.getChannel())
                        .text(postingText)
                        .iconUrl(slackProperties.getIconUrl())
                        .replyBroadcast(replyBroadcast)
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
}
