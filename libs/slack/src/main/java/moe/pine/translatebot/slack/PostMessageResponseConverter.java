package moe.pine.translatebot.slack;

import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;

class PostMessageResponseConverter {
    PostMessageResponse convert(
            final ChatPostMessageResponse response
    ) {
        return PostMessageResponse.builder()
                .channel(response.getChannel())
                .ts(response.getTs())
                .build();
    }
}
