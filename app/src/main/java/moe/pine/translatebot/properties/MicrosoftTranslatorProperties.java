package moe.pine.translatebot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@Data
@ConfigurationProperties("microsoft-translator")
public class MicrosoftTranslatorProperties {
    private @NotBlank String subscriptionKey;
    private @NotBlank String endpoint;
}
