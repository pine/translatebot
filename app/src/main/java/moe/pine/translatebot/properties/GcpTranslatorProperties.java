package moe.pine.translatebot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@Data
@ConfigurationProperties("gcp")
public class GcpTranslatorProperties {
    private @NotBlank String credentials;
    private @NotBlank String projectId;
    private @NotBlank String location;
}
