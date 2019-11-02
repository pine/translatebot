package moe.pine.translatebot.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.slack.DeleteMessageRequest;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.SlackClient;
import moe.translatebot.log.SentLog;
import moe.translatebot.log.SentLogId;
import moe.translatebot.log.SentLogRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageDeletedEventHandler {
    private final SlackClient slackClient;
    private final SentLogRepository sentLogRepository;

    public void execute(final MessageEvent messageEvent) {
        if (messageEvent.getPreviousMessage() == null) {
            return;
        }

        final SentLogId sentLogId =
            new SentLogId(
                messageEvent.getChannel(),
                messageEvent.getPreviousMessage().getTs());
        final Optional<SentLog> sentLogOpt = sentLogRepository.get(sentLogId);
        if (sentLogOpt.isEmpty()) {
            return;
        }

        final SentLog sentLog = sentLogOpt.get();
        final DeleteMessageRequest deleteMessageRequest =
            DeleteMessageRequest.builder()
                .channel(sentLog.getChannel())
                .ts(sentLog.getDestinationTs())
                .build();
        slackClient.deleteMessage(deleteMessageRequest);
    }
}
