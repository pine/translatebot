package moe.pine.translatebot.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.rtm.RTMClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
class SlackRtmClient {
    private final StateManager stateManager;
    private final RetryTemplate unlimitedRetryTemplate;
    private final RTMClient rtmClient;

    private final List<Consumer<Event>> eventListeners = new CopyOnWriteArrayList<>();

    SlackRtmClient(
        final String token,
        final StateManager stateManager,
        final RetryTemplate retryTemplate,
        final RetryTemplate unlimitedRetryTemplate
    ) {
        this.stateManager = stateManager;
        this.unlimitedRetryTemplate = unlimitedRetryTemplate;

        rtmClient = retryTemplate.execute(ctx -> {
            try {
                return new Slack().rtm(token);
            } catch (IOException e) {
                throw new SlackClientException(e);
            }
        });
        rtmClient.addMessageHandler(this::onEvent);
        rtmClient.addErrorHandler(this::onError);
        rtmClient.addCloseHandler(this::onClose);

        retryTemplate.execute(ctx -> {
            try {
                rtmClient.connect();
                return null;
            } catch (IOException | DeploymentException e) {
                throw new SlackClientException(e);
            }
        });
    }

    void addEventListener(final Consumer<Event> listener) {
        eventListeners.add(listener);
    }

    void removeEventListener(final Consumer<Event> listener) {
        eventListeners.remove(listener);
    }

    void close() throws IOException {
        rtmClient.close();
    }

    private void onEvent(final String content) {
        log.info("New event received: {}", content);
        stateManager.throwIfAlreadyClosed();

        final Optional<Event> eventOpt = Events.parse(content);
        eventOpt.ifPresent(event ->
            eventListeners.forEach(listener -> listener.accept(event)));
    }

    private void onError(final Throwable error) {
        log.error("An error has occurred.", error);
    }

    private void onClose(final CloseReason closeReason) {
        log.warn("The socket has been closed. The reason is {}. Trying reconnect.", closeReason);

        unlimitedRetryTemplate.execute(ctx -> {
            stateManager.throwIfAlreadyClosed();
            try {
                rtmClient.reconnect();
                return null;
            } catch (IOException | SlackApiException | URISyntaxException | DeploymentException e) {
                log.warn("Connecting failed. Number of retries is {}", ctx.getRetryCount(), e);
                throw new SlackClientException(e);
            }
        });
    }
}
