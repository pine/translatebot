package moe.pine.translatebot.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.rtm.RTMClient;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.retryutils.RetryTemplateFactory;
import org.springframework.retry.support.RetryTemplate;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public class SlackClient {
    private final RetryTemplate retryTemplate;
    private final RetryTemplate unlimitedRetryTemplate;
    private final RTMClient rtmClient;
    private final MethodsClient methodsClient;

    private final ChatPostMessageRequestConverter chatPostMessageRequestConverter =
        new ChatPostMessageRequestConverter();
    private final ChatPostMessageResponseConverter chatPostMessageResponseConverter =
        new ChatPostMessageResponseConverter();
    private final List<Consumer<Event>> eventListeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean closed = new AtomicBoolean();

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
        this.retryTemplate = Objects.requireNonNull(retryTemplate);
        this.unlimitedRetryTemplate = Objects.requireNonNull(unlimitedRetryTemplate);

        methodsClient = Slack.getInstance().methods(token);
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

    private void onEvent(final String content) {
        throwIfAlreadyClosed();

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
            throwIfAlreadyClosed();
            try {
                rtmClient.reconnect();
                return null;
            } catch (IOException | SlackApiException | URISyntaxException | DeploymentException e) {
                log.warn("Connecting failed. Number of retries is {}", ctx.getRetryCount(), e);
                throw new SlackClientException(e);
            }
        });
    }

    public void addEventListener(final Consumer<Event> listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(final Consumer<Event> listener) {
        eventListeners.remove(listener);
    }

    public OutgoingMessageResult postMessage(final OutgoingMessage outgoingMessage) {
        final ChatPostMessageRequest request =
            chatPostMessageRequestConverter.fromMessage(outgoingMessage);

        final ChatPostMessageResponse response =
            retryTemplate.execute(ctx -> {
                throwIfAlreadyClosed();
                try {
                    return methodsClient.chatPostMessage(request);
                } catch (IOException | SlackApiException e) {
                    throw new SlackClientException(e);
                }
            });

        return chatPostMessageResponseConverter.toResult(response);
    }

    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            log.info("Closing the socket.");
            rtmClient.close();
        }
    }

    private void throwIfAlreadyClosed() {
        if (closed.get()) {
            throw new IllegalStateException("The server has already been shutdown.");
        }
    }
}
