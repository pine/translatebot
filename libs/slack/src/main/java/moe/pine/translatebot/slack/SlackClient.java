package moe.pine.translatebot.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.rtm.RTMClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class SlackClient {
    private final RTMClient rtmClient;
    private final MethodsClient methodsClient;

    private final ChatPostMessageRequestConverter chatPostMessageRequestConverter = new ChatPostMessageRequestConverter();
    private final List<Consumer<Event>> eventListeners = new ArrayList<>();

    private static final RetryTemplate RETRY_TEMPLATE =
            new RetryTemplate() {{
                final Map<Class<? extends Throwable>, Boolean> retryableExceptions =
                        Map.of(SlackClientException.class, true);
                final RetryPolicy retryPolicy = new SimpleRetryPolicy(5, retryableExceptions);
                setRetryPolicy(retryPolicy);

                final ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
                backOffPolicy.setInitialInterval(500);
                backOffPolicy.setMultiplier(2.0);
                setBackOffPolicy(backOffPolicy);
            }};

    public SlackClient(final String token) throws Exception {
        rtmClient = new Slack().rtm(token);
        rtmClient.addMessageHandler(this::onEvent);
        rtmClient.addErrorHandler(this::onError);
        rtmClient.addCloseHandler(this::onClose);
        rtmClient.connect();

        methodsClient = Slack.getInstance().methods(token);
    }

    private void onEvent(final String content) {
        // log.debug("onEvent: {}", content);

        final Optional<Event> eventOpt = Events.parse(content);
        eventOpt.ifPresent(event ->
                eventListeners.forEach(listener -> listener.accept(event)));
    }

    private void onError(final Throwable error) {
        error.printStackTrace();
        try {
            rtmClient.reconnect();
        } catch (IOException | SlackApiException | URISyntaxException | DeploymentException e) {
            e.printStackTrace();
        }
    }

    private void onClose(final CloseReason closeReason) {
        log.error("{}", closeReason);
        try {
            rtmClient.reconnect();
        } catch (IOException | SlackApiException | URISyntaxException | DeploymentException e) {
            e.printStackTrace();
        }
    }

    public void addEventListener(final Consumer<Event> listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(final Consumer<Event> listener) {
        eventListeners.remove(listener);
    }

    public OutgoingMessageResult postMessage(final OutgoingMessage outgoingMessage) {
        final ChatPostMessageRequest messageRequest =
                chatPostMessageRequestConverter.from(outgoingMessage);

        final ChatPostMessageResponse chatPostMessageResponse =
                RETRY_TEMPLATE.execute(ctx -> {
                    try {
                        return methodsClient.chatPostMessage(messageRequest);
                    } catch (IOException | SlackApiException e) {
                        throw new SlackClientException(e);
                    }
                });

        return OutgoingMessageResult.builder()
                .channel(chatPostMessageResponse.getChannel())
                .ts(chatPostMessageResponse.getTs())
                .build();
    }
}
