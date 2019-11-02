package moe.pine.translatebot.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatDeleteRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.retryutils.RetryTemplateFactory;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public class SlackClient {
    class StateManagerImpl implements StateManager {
        @Override
        public boolean isClosed() {
            return closed.get();
        }
    }

    private final RetryTemplate retryTemplate;
    private final RetryTemplate unlimitedRetryTemplate;
    private final SlackRtmClient slackRtmClient;
    private final MethodsClient methodsClient;

    private final PostMessageRequestConverter postMessageRequestConverter =
        new PostMessageRequestConverter();
    private final PostMessageResponseConverter postMessageResponseConverter =
        new PostMessageResponseConverter();
    private final UpdateMessageRequestConverter updateMessageRequestConverter =
        new UpdateMessageRequestConverter();

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
        this.retryTemplate = Objects.requireNonNull(retryTemplate);
        this.unlimitedRetryTemplate = Objects.requireNonNull(unlimitedRetryTemplate);
        this.slackRtmClient = new SlackRtmClient(
            token,
            stateManager,
            retryTemplate,
            unlimitedRetryTemplate);

        methodsClient = Slack.getInstance().methods(token);
    }

    public void addEventListener(final Consumer<Event> listener) {
        slackRtmClient.addEventListener(listener);
    }

    public void removeEventListener(final Consumer<Event> listener) {
        slackRtmClient.removeEventListener(listener);
    }

    public PostMessageResponse postMessage(final PostMessageRequest postMessageRequest) {
        final ChatPostMessageRequest chatPostMessageRequest =
            postMessageRequestConverter.convert(postMessageRequest);

        final ChatPostMessageResponse chatPostMessageResponse =
            retryTemplate.execute(ctx -> {
                stateManager.throwIfAlreadyClosed();
                try {
                    return methodsClient.chatPostMessage(chatPostMessageRequest);
                } catch (IOException | SlackApiException e) {
                    throw new SlackClientException(e);
                }
            });

        return postMessageResponseConverter.convert(chatPostMessageResponse);
    }

    public void updateMessage(final UpdateMessageRequest updateMessageRequest) {
        final ChatUpdateRequest chatUpdateRequest =
            updateMessageRequestConverter.convert(updateMessageRequest);

        retryTemplate.execute(ctx -> {
            stateManager.throwIfAlreadyClosed();
            try {
                return methodsClient.chatUpdate(chatUpdateRequest);
            } catch (IOException | SlackApiException e) {
                throw new SlackClientException(e);
            }
        });
    }

    public void deleteMessage(final DeleteMessageRequest deleteMessageRequest) {
        final ChatDeleteRequest chatDeleteRequest =
            ChatDeleteRequest.builder()
                .channel(deleteMessageRequest.getChannel())
                .ts(deleteMessageRequest.getTs())
                .build();

        retryTemplate.execute(ctx -> {
            stateManager.throwIfAlreadyClosed();
            try {
                return methodsClient.chatDelete(chatDeleteRequest);
            } catch (IOException | SlackApiException e) {
                throw new SlackClientException(e);
            }
        });
    }

    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            log.info("Closing the socket.");
            slackRtmClient.close();
        }
    }
}
