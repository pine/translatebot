package moe.pine.translatebot.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.rtm.RTMClient;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public class SlackClient {
    private String token;
    private final RTMClient rtmClient;
    private final List<Consumer<Event>> eventListeners = new ArrayList<>();

    public SlackClient(final String token) throws Exception {
        this.token = token;

        rtmClient = new Slack().rtm(token);
        rtmClient.addMessageHandler(this::onEvent);
        rtmClient.connect();
    }

    private void onEvent(final String content) {
        // log.debug("onEvent: {}", content);

        final Optional<Event> eventOpt = Events.parse(content);
        eventOpt.ifPresent(event ->
            eventListeners.forEach(listener -> listener.accept(event)));
    }

    public void addEventListener(final Consumer<Event> listener) {
        eventListeners.add(listener);
    }

    public void postMessage(final ChatMessage chatMessage) {
        final ChatPostMessageRequest messageRequest =
            ChatPostMessageRequest.builder()
                .username("translatebot")
                .channel(chatMessage.getChannel())
                .text(chatMessage.getText())
                .iconUrl("https://i.imgur.com/IpOE5eC.png")
                .build();

        try {
            Slack.getInstance().methods(token).chatPostMessage(messageRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
