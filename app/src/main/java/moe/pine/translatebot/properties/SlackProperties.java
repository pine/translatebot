package moe.pine.translatebot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@ConfigurationProperties("slack")
public class SlackProperties {
    private @NotNull Set<String> channels;
    private @NotBlank String iconUrl;
    private @NotBlank String token;
    private @NotBlank String username;
}
