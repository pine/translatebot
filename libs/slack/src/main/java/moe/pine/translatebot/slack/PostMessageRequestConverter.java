package moe.pine.translatebot.slack;

import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.model.Attachment;

import java.util.List;

class PostMessageRequestConverter {
    ChatPostMessageRequest convert(
        final PostMessageRequest message
    ) {
        final Attachment attachment =
            Attachment.builder()
                .text(message.getText())
                .build();

        return ChatPostMessageRequest.builder()
            .username(message.getUsername())
            .threadTs(message.getThreadTs())
            .channel(message.getChannel())
            .attachments(List.of(attachment))
            .iconUrl(message.getIconUrl())
            .replyBroadcast(message.isReplyBroadcast())
            .build();
    }
}
