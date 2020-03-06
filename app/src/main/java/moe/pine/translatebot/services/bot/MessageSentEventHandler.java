package moe.pine.translatebot.services.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.log.SentLog;
import moe.pine.translatebot.log.SentLogRepository;
import moe.pine.translatebot.properties.SlackProperties;
import moe.pine.translatebot.services.translation.TranslationService;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.PostMessageRequest;
import moe.pine.translatebot.slack.PostMessageResponse;
import moe.pine.translatebot.slack.SlackClient;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
class MessageSentEventHandler {
    private static final Pattern EMOTICON_ONLY_TEXT_PATTERN =
        Pattern.compile("^(?:\\s*:[\\w-+]+:)+\\s*$");

    private final SlackProperties slackProperties;
    private final SlackClient slackClient;
    private final SentLogRepository sentLogRepository;
    private final TranslationService translationService;

    void execute(MessageEvent messageEvent) throws InterruptedException {
        Matcher matcher = EMOTICON_ONLY_TEXT_PATTERN.matcher(messageEvent.getText());
        if (matcher.matches()) {
            return;
        }

        String text = messageEvent.getText();
        Optional<String> translatedTextOpt = translationService.translate(text);
        if (translatedTextOpt.isEmpty()) {
            return;
        }

        boolean replyBroadcast =
            MessageEvent.Subtypes.THREAD_BROADCAST.equals(messageEvent.getSubtype());
        PostMessageRequest postMessageRequest =
            PostMessageRequest.builder()
                .username(slackProperties.getUsername())
                .threadTs(messageEvent.getThreadTs())
                .channel(messageEvent.getChannel())
                .text(translatedTextOpt.get())
                .iconUrl(slackProperties.getIconUrl())
                .replyBroadcast(replyBroadcast)
                .build();
        PostMessageResponse postMessageResponse = slackClient.postMessage(postMessageRequest);

        SentLog sentLog = SentLog.builder()
            .channel(messageEvent.getChannel())
            .sourceTs(messageEvent.getTs())
            .destinationTs(postMessageResponse.getTs())
            .build();
        sentLogRepository.add(sentLog);
    }
}
