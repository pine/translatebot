package moe.pine.translatebot.slack;

import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;

class ChatPostMessageRequestConverter {
    ChatPostMessageRequest from(final OutgoingMessage outgoingMessage) {
        return ChatPostMessageRequest.builder()
                .username(outgoingMessage.getUsername())
                .threadTs(outgoingMessage.getThreadTs())
                .channel(outgoingMessage.getChannel())
                .text(outgoingMessage.getText())
                .iconUrl(outgoingMessage.getIconUrl())
                .replyBroadcast(outgoingMessage.isReplyBroadcast())
                .build();
    }
}
