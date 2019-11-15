package moe.pine.translatebot.services;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.SlackProperties;
import moe.pine.translatebot.slack.Event;
import moe.pine.translatebot.slack.EventListener;
import moe.pine.translatebot.slack.MessageEvent;
import moe.pine.translatebot.slack.MessageEvent.Subtypes;
import moe.pine.translatebot.slack.SlackClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
@Service
public class BotService {
    private final SlackProperties slackProperties;
    private final SlackClient slackClient;
    private final MessageChangedEventHandler messageChangedEventHandler;
    private final MessageDeletedEventHandler messageDeletedEventHandler;
    private final MessageSentEventHandler messageSentEventHandler;
    private final Instant startupTime;

    private final EventListener eventConsumer = this::onEvent;

    public BotService(
            final SlackProperties slackProperties,
            final SlackClient slackClient,
            final MessageChangedEventHandler messageChangedEventHandler,
            final MessageDeletedEventHandler messageDeletedEventHandler,
            final MessageSentEventHandler messageSentEventHandler,
            final Clock clock
    ) {
        this.slackProperties = slackProperties;
        this.slackClient = slackClient;
        this.messageChangedEventHandler = messageChangedEventHandler;
        this.messageDeletedEventHandler = messageDeletedEventHandler;
        this.messageSentEventHandler = messageSentEventHandler;

        startupTime = clock.instant();
        slackClient.addEventListener(eventConsumer);
    }

    @PreDestroy
    public void destroy() {
        slackClient.removeEventListener(eventConsumer);
    }

    private void onEvent(final Event event) throws InterruptedException {
        if (event instanceof MessageEvent) {
            onMessageEvent((MessageEvent) event);
        }
    }

    private void onMessageEvent(final MessageEvent messageEvent) throws InterruptedException {
        final double ts = Double.parseDouble(messageEvent.getTs());
        if (ts < startupTime.getEpochSecond()) {
            return;
        }
        if (StringUtils.isNotEmpty(messageEvent.getBotId())) {
            return;
        }
        final Set<String> channels = slackProperties.getChannels();
        if (!channels.contains(messageEvent.getChannel())) {
            return;
        }

        if (StringUtils.isEmpty(messageEvent.getSubtype()) ||
                Subtypes.THREAD_BROADCAST.equals(messageEvent.getSubtype())) {
            messageSentEventHandler.execute(messageEvent);
        } else if (Subtypes.MESSAGE_CHANGED.equals(messageEvent.getSubtype())) {
            messageChangedEventHandler.execute(messageEvent);
        } else if (Subtypes.MESSAGE_DELETED.equals(messageEvent.getSubtype())) {
            messageDeletedEventHandler.execute(messageEvent);
        }
    }
}
