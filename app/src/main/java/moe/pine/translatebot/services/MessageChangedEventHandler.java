package moe.pine.translatebot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.MessageEvent.Subtypes;

import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.slack.UpdateMessageRequest;
import moe.pine.translatebot.translation.Translator;
import moe.translatebot.log.SentLog;
import moe.translatebot.log.SentLogId;
import moe.translatebot.log.SentLogRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageChangedEventHandler {
    private static final String POSTING_TEXT_FORMAT = ":jp: %s";

    private final SlackClient slackClient;
    private final Translator translator;
    private final SentLogRepository sentLogRepository;

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
