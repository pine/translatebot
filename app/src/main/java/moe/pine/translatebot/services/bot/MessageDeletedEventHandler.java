package moe.pine.translatebot.services.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.slack.DeleteMessageRequest;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.SlackClient;
import moe.pine.translatebot.log.SentLog;
import moe.pine.translatebot.log.SentLogId;
import moe.pine.translatebot.log.SentLogRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
class MessageDeletedEventHandler {
    private final SlackClient slackClient;
    private final SentLogRepository sentLogRepository;

    void execute(MessageEvent messageEvent) {
        SentLogId sentLogId =
            new SentLogId(
                messageEvent.getChannel(),
                messageEvent.getPreviousMessage().getTs());
        Optional<SentLog> sentLogOpt = sentLogRepository.get(sentLogId);
        if (sentLogOpt.isEmpty()) {
            return;
        }

        SentLog sentLog = sentLogOpt.get();
        DeleteMessageRequest deleteMessageRequest =
            DeleteMessageRequest.builder()
                .channel(sentLog.getChannel())
                .ts(sentLog.getDestinationTs())
                .build();
        slackClient.deleteMessage(deleteMessageRequest);
    }
}
