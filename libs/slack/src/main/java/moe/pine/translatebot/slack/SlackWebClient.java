package moe.pine.translatebot.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.request.chat.ChatDeleteRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersListRequest;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.users.UsersListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
class SlackWebClient {
    private final StateManager stateManager;
    private final MethodsClient methodsClient;
    private final RetryTemplate retryTemplate;

    private final PostMessageRequestConverter postMessageRequestConverter =
        new PostMessageRequestConverter();
    private final PostMessageResponseConverter postMessageResponseConverter =
        new PostMessageResponseConverter();
    private final UpdateMessageRequestConverter updateMessageRequestConverter =
        new UpdateMessageRequestConverter();

    SlackWebClient(
        final String token,
        final StateManager stateManager,
        final RetryTemplate retryTemplate
    ) {
        this.stateManager = stateManager;
        this.retryTemplate = retryTemplate;
        methodsClient = Slack.getInstance().methods(token);
    }

    PostMessageResponse postMessage(final PostMessageRequest postMessageRequest) {
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

    void updateMessage(final UpdateMessageRequest updateMessageRequest) {
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

    void deleteMessage(final DeleteMessageRequest deleteMessageRequest) {
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

    List<User> getUsers() {
        final UsersListRequest usersListRequest =
            UsersListRequest.builder()
                .includeLocale(false)
                .build();

        final UsersListResponse usersListResponse =
            retryTemplate.execute(ctx -> {
                stateManager.throwIfAlreadyClosed();
                try {
                    return methodsClient.usersList(usersListRequest);
                } catch (IOException | SlackApiException e) {
                    throw new SlackClientException(e);
                }
            });

        return usersListResponse.getMembers()
            .stream()
            .map(v -> User.builder()
                .id(v.getId())
                .realName(v.getProfile().getRealName())
                .displayName(v.getProfile().getDisplayName())
                .build())
            .collect(Collectors.toUnmodifiableList());
    }
}
