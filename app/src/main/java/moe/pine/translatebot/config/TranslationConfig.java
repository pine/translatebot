package moe.pine.translatebot.config;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.TranslationProperties;
import moe.pine.translatebot.translation.Translator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties(TranslationProperties.class)
public class TranslationConfig {
    @Bean
    public Translator translator(
        final TranslationProperties translationProperties,
        final ResourceLoader resourceLoader
    ) throws IOException {
        final String location = translationProperties.getCredentials();
        log.info("Loading GCP credentials file '{}'", location);

        final Resource resource = resourceLoader.getResource(location);
        return new Translator(
            resource.getInputStream(),
            translationProperties.getProjectId(),
            translationProperties.getLocation()
        );
    }
}
