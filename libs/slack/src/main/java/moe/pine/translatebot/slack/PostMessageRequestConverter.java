package moe.pine.translatebot.slack;

import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;

import java.util.List;
import java.util.stream.Collectors;

class PostMessageRequestConverter {
    ChatPostMessageRequest convert(
        final PostMessageRequest postMessageRequest
    ) {
        final List<Field> fields =
            postMessageRequest.getTextFields()
                .stream()
                .map(v -> Field.builder()
                    .title(v.getTitle())
                    .value(v.getValue())
                    .build())
                .collect(Collectors.toUnmodifiableList());

        final Attachment attachment =
            Attachment.builder()
                .fields(fields)
                .build();

        return ChatPostMessageRequest.builder()
            .username(postMessageRequest.getUsername())
            .threadTs(postMessageRequest.getThreadTs())
            .channel(postMessageRequest.getChannel())
            .attachments(List.of(attachment))
            .iconUrl(postMessageRequest.getIconUrl())
            .replyBroadcast(postMessageRequest.isReplyBroadcast())
            .build();
    }
}
