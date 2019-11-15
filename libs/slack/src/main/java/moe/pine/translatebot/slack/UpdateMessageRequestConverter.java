package moe.pine.translatebot.slack;

import com.github.seratch.jslack.api.methods.request.chat.ChatUpdateRequest;
import com.github.seratch.jslack.api.model.Attachment;

import java.util.List;

public class UpdateMessageRequestConverter {
    ChatUpdateRequest convert(final UpdateMessageRequest updateMessageRequest) {
        final Attachment attachment =
            Attachment.builder()
                .text(updateMessageRequest.getText())
                .build();

        return ChatUpdateRequest.builder()
            .channel(updateMessageRequest.getChannel())
            .ts(updateMessageRequest.getTs())
            .attachments(List.of(attachment))
            .build();
    }
}
