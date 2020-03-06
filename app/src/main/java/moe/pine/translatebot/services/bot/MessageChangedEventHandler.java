package moe.pine.translatebot.services.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.log.SentLog;
import moe.pine.translatebot.log.SentLogId;
import moe.pine.translatebot.log.SentLogRepository;
import moe.pine.translatebot.services.TextTranslationUtils;
import moe.pine.translatebot.services._translation.TranslatedText;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.slack.TextField;
import moe.pine.translatebot.slack.UpdateMessageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class MessageChangedEventHandler {
    private final SlackClient slackClient;
    private final SentLogRepository sentLogRepository;
    private final TextTranslationUtils textTranslationUtils;

    void execute(MessageEvent messageEvent) throws InterruptedException {
        if (messageEvent.getMessage().getEdited() == null) {
            return;
        }

        SentLogId sentLogId =
            new SentLogId(
                messageEvent.getChannel(),
                messageEvent.getMessage().getTs());
        Optional<SentLog> sentLogOpt = sentLogRepository.get(sentLogId);
        if (sentLogOpt.isEmpty()) {
            return;
        }

        String text = messageEvent.getMessage().getText();
        List<TranslatedText> translatedTexts = textTranslationUtils.translate(text);
        List<TextField> textFields =
            translatedTexts.stream()
                .map(translatedText ->
                    new TextField(
                        translatedText.getTranslatorId().getTitle(),
                        translatedText.getText()))
                .collect(Collectors.toUnmodifiableList());

        SentLog sentLog = sentLogOpt.get();
        UpdateMessageRequest updateMessageRequest =
            UpdateMessageRequest.builder()
                .channel(sentLog.getChannel())
                .textFields(textFields)
                .ts(sentLog.getDestinationTs())
                .build();
        slackClient.updateMessage(updateMessageRequest);
    }
}
