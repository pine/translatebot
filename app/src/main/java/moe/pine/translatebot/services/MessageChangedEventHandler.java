package moe.pine.translatebot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.slack.UpdateMessageRequest;
import moe.pine.translatebot.log.SentLog;
import moe.pine.translatebot.log.SentLogId;
import moe.pine.translatebot.log.SentLogRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageChangedEventHandler {
    private final SlackClient slackClient;
    private final SentLogRepository sentLogRepository;
    private final TextTranslationUtils textTranslationUtils;

    public void execute(final MessageEvent messageEvent) {
        if (messageEvent.getMessage().getEdited() == null) {
            return;
        }

        final SentLogId sentLogId =
            new SentLogId(
                messageEvent.getChannel(),
                messageEvent.getMessage().getTs());
        final Optional<SentLog> sentLogOpt = sentLogRepository.get(sentLogId);
        if (sentLogOpt.isEmpty()) {
            return;
        }

        final String text = messageEvent.getMessage().getText();
        final Optional<String> postingTextOpt = textTranslationUtils.translate(text);
        if (postingTextOpt.isEmpty()) {
            return;
        }

        final String postingText = postingTextOpt.get();
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
