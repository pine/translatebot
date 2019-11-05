package moe.pine.translatebot.services.text_variable;

import lombok.RequiredArgsConstructor;
import moe.pine.translatebot.services.UserInformationManager;
import moe.pine.translatebot.slack.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class UserVariableProcessor implements VariableProcessor {
    private static final Pattern USER_PATTERN = Pattern.compile("<@(?<userId>[A-Z0-9]+)>");

    private final UserInformationManager userInformationManager;

    @Override
    public String execute(final String text) {
        final StringBuilder sb = new StringBuilder();

        int pos = 0;
        while (pos < text.length()) {
            final Matcher matcher = USER_PATTERN.matcher(text);
            if (!matcher.find(pos)) {
                sb.append(text.substring(pos));
                break;
            }

            final String userId = matcher.group("userId");
            final Optional<User> userOpt = userInformationManager.findByUserId(userId);
            if (userOpt.isEmpty()) {
                sb.append(text, pos, matcher.end());
                pos = matcher.end();
                continue;
            }

            final User user = userOpt.get();
            final String displayName = StringUtils.firstNonEmpty(user.getDisplayName(), user.getRealName());
            if (StringUtils.isEmpty(displayName)) {
                sb.append(text, pos, matcher.end());
                pos = matcher.end();
                continue;
            }

            sb.append(text, pos, matcher.start());
            sb.append('@');
            sb.append(displayName);

            pos = matcher.end();
        }

        return sb.toString();
    }
}
