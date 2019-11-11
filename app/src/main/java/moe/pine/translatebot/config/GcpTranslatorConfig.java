package moe.pine.translatebot.config;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.GcpTranslatorProperties;
import moe.pine.translatebot.gcp.translator.GcpTranslator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties(GcpTranslatorProperties.class)
public class GcpTranslatorConfig {
    @Bean
    public GcpTranslator gcpTranslator(
        final GcpTranslatorProperties gcpTranslatorProperties,
        final ResourceLoader resourceLoader
    ) throws IOException {
        final String location = gcpTranslatorProperties.getCredentials();
        log.info("Loading GCP credentials file '{}'", location);

        final Resource resource = resourceLoader.getResource(location);
        return new GcpTranslator(
            resource.getInputStream(),
            gcpTranslatorProperties.getProjectId(),
            gcpTranslatorProperties.getLocation()
        );
    }
}
