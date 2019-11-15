package moe.pine.translatebot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.log.SentLog;
import moe.pine.translatebot.log.SentLogRepository;
import moe.pine.translatebot.properties.SlackProperties;
import moe.pine.translatebot.services.translation.TranslatedText;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.PostMessageRequest;
import moe.pine.translatebot.slack.PostMessageResponse;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.slack.TextField;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSentEventHandler {
    private static final Pattern EMOTICON_ONLY_TEXT_PATTERN =
        Pattern.compile("^(?:\\s*:[\\w-+]+:)+\\s*$");

    private final SlackProperties slackProperties;
    private final SlackClient slackClient;
    private final SentLogRepository sentLogRepository;
    private final TextTranslationUtils textTranslationUtils;

    public void execute(final MessageEvent messageEvent) throws InterruptedException {
        final Matcher matcher = EMOTICON_ONLY_TEXT_PATTERN.matcher(messageEvent.getText());
        if (matcher.matches()) {
            return;
        }

        final String text = messageEvent.getText();
        final List<TranslatedText> translatedTexts = textTranslationUtils.translate(text);
        if (translatedTexts.isEmpty()) {
            return;
        }

        final List<TextField> textFields =
            translatedTexts.stream()
                .map(translatedText ->
                    new TextField(
                        translatedText.getTranslatorId().getTitle(),
                        translatedText.getTranslatedText()))
                .collect(Collectors.toUnmodifiableList());

        final boolean replyBroadcast =
            MessageEvent.Subtypes.THREAD_BROADCAST.equals(messageEvent.getSubtype());
        final PostMessageRequest postMessageRequest =
            PostMessageRequest.builder()
                .username(slackProperties.getUsername())
                .threadTs(messageEvent.getThreadTs())
                .channel(messageEvent.getChannel())
                .textFields(textFields)
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
    }
}
