package moe.pine.translatebot.slack;

import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;

class ChatPostMessageRequestConverter {
    ChatPostMessageRequest fromMessage(
            final OutgoingMessage message
    ) {
        return ChatPostMessageRequest.builder()
                .username(message.getUsername())
                .threadTs(message.getThreadTs())
                .channel(message.getChannel())
                .text(message.getText())
                .iconUrl(message.getIconUrl())
                .replyBroadcast(message.isReplyBroadcast())
                .build();
    }
}
