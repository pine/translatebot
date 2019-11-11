package moe.pine.translatebot.config;

import lombok.extern.slf4j.Slf4j;
import moe.pine.translatebot.properties.GcpProperties;
import moe.pine.translatebot.gcp.GcpTranslator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Slf4j
@Configuration
@EnableConfigurationProperties(GcpProperties.class)
public class GcpConfig {
    @Bean
    public GcpTranslator gcpTranslator(
        final GcpProperties gcpProperties,
        final ResourceLoader resourceLoader
    ) throws IOException {
        final String location = gcpProperties.getCredentials();
        log.info("Loading GCP credentials file '{}'", location);

        final Resource resource = resourceLoader.getResource(location);
        return new GcpTranslator(
            resource.getInputStream(),
            gcpProperties.getProjectId(),
            gcpProperties.getLocation()
        );
    }
}
