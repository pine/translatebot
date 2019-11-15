package moe.pine.translatebot.slack;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.retry.support.RetryTemplateFactory;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SlackClient {
    class StateManagerImpl implements StateManager {
        @Override
        public boolean isClosed() {
            return closed.get();
        }
    }

    private final SlackRtmClient slackRtmClient;
    private final SlackWebClient slackWebClient;

    private final AtomicBoolean closed = new AtomicBoolean();
    private final StateManager stateManager = new StateManagerImpl();

    public SlackClient(final String token) {
        this(token,
                RetryTemplateFactory.create(
                        5, 500, 2.0, SlackClientException.class),
                RetryTemplateFactory.createUnlimited(
                        500, 60 * 1000, 2.0, SlackClientException.class));
    }

    SlackClient(
            final String token,
            final RetryTemplate retryTemplate,
            final RetryTemplate unlimitedRetryTemplate
    ) {
        Objects.requireNonNull(retryTemplate);
        Objects.requireNonNull(unlimitedRetryTemplate);

        this.slackRtmClient = new SlackRtmClient(
                token,
                stateManager,
                retryTemplate,
                unlimitedRetryTemplate);
        this.slackWebClient = new SlackWebClient(
                token,
                stateManager,
                retryTemplate);
    }

    public void addEventListener(final EventListener listener) {

        slackRtmClient.addEventListener(listener);
    }

    public void removeEventListener(final EventListener listener) {
        slackRtmClient.removeEventListener(listener);
    }

    public PostMessageResponse postMessage(final PostMessageRequest postMessageRequest) {
        return slackWebClient.postMessage(postMessageRequest);
    }

    public void updateMessage(final UpdateMessageRequest updateMessageRequest) {
        slackWebClient.updateMessage(updateMessageRequest);
    }

    public void deleteMessage(final DeleteMessageRequest deleteMessageRequest) {
        slackWebClient.deleteMessage(deleteMessageRequest);
    }

    public List<User> getUsers() {
        return slackWebClient.getUsers();
    }

    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            log.info("Closing the socket.");
            slackRtmClient.close();
        }
    }
}
