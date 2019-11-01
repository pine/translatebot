package moe.pine.translatebot.slack;

import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;

class ChatPostMessageResponseConverter {
    OutgoingMessageResult toResult(
            final ChatPostMessageResponse response
    ) {
        return OutgoingMessageResult.builder()
                .channel(response.getChannel())
                .ts(response.getTs())
                .build();
    }
}
