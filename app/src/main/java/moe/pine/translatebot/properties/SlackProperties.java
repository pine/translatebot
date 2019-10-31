package moe.pine.translatebot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@Data
@ConfigurationProperties("slack")
public class SlackProperties {
    private @NotBlank String token;
}
