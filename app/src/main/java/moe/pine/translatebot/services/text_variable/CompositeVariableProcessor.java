package moe.pine.translatebot.services.text_variable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CompositeVariableProcessor implements VariableProcessor {
    private final List<VariableProcessor> variableProcessors;

    @Override
    public String execute(String text) {
        if (StringUtils.isEmpty(text)) {
            return StringUtils.EMPTY;
        }

        for (var variableProcessor : variableProcessors) {
            text = variableProcessor.execute(text);
        }
        return text;
    }
}
