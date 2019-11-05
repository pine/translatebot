package moe.pine.translatebot.services.text_variable;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class CommandVariableProcessor implements VariableProcessor {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("(?:<!(?<commandName>[a-z]+)>)");

    @Override
    public String execute(final String text) {
        final StringBuilder sb = new StringBuilder();

        int pos = 0;
        while (pos < text.length()) {
            final Matcher matcher = COMMAND_PATTERN.matcher(text);
            if (!matcher.find(pos)) {
                sb.append(text.substring(pos));
                break;
            }

            final String commandName = matcher.group("commandName");
            sb.append(text, pos, matcher.start());
            sb.append('@');
            sb.append(commandName);

            pos = matcher.end();
        }

        return sb.toString();
    }
}
