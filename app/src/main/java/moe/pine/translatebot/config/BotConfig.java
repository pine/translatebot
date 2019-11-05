package moe.pine.translatebot.config;

import moe.pine.translatebot.services.text_variable.CommandVariableProcessor;
import moe.pine.translatebot.services.text_variable.CompositeVariableProcessor;
import moe.pine.translatebot.services.text_variable.UserVariableProcessor;
import moe.pine.translatebot.services.text_variable.VariableProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BotConfig {
    @Bean
    public CompositeVariableProcessor compositeVariableProcessor(
            final UserVariableProcessor userVariableProcessor,
            final CommandVariableProcessor commandVariableProcessor
    ) {
        final List<VariableProcessor> variableProcessors =
                List.of(userVariableProcessor, commandVariableProcessor);
        return new CompositeVariableProcessor(variableProcessors);
    }
}
