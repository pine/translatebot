package moe.pine.translatebot.properties;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Value
@NonFinal
@Validated
@ConstructorBinding
@ConfigurationProperties("slack")
public class SlackProperties {
    @NotBlank String iconUrl;
    @NotBlank String token;
    @NotBlank String username;
}
