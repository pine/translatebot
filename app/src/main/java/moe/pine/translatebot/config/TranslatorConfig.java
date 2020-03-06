package moe.pine.translatebot.config;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.GcpTranslatorProperties;
import moe.pine.translatebot.translator.GcpTranslator;
import moe.pine.translatebot.translator.Translator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties(GcpTranslatorProperties.class)
public class TranslatorConfig {
    @Bean
    public Translator gcpTranslator(
        GcpTranslatorProperties gcpTranslatorProperties,
        ResourceLoader resourceLoader
    ) throws IOException {
        String location = gcpTranslatorProperties.getCredentials();
        log.info("Loading GCP credentials file '{}'", location);

        return new GcpTranslator(
            resourceLoader.getResource(location).getInputStream(),
            gcpTranslatorProperties.getProjectId(),
            gcpTranslatorProperties.getLocation()
        );
    }
}
