package moe.pine.translatebot.config;

import moe.pine.translatebot.microsoft.translator.MicrosoftTranslator;
import moe.pine.translatebot.properties.MicrosoftTranslatorProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(MicrosoftTranslatorProperties.class)
public class MicrosoftTranslatorConfig {
    @Bean
    public MicrosoftTranslator microsoftTranslator(
        final MicrosoftTranslatorProperties microsoftTranslatorProperties,
        final WebClient.Builder webClientBuilder
    ) {
        return new MicrosoftTranslator(
            microsoftTranslatorProperties.getSubscriptionKey(),
            microsoftTranslatorProperties.getEndpoint(),
            webClientBuilder
        );
    }
}
