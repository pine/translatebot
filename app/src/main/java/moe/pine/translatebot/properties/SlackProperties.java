package moe.pine.translatebot.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@ConfigurationProperties("slack")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackProperties {
    @NotNull Set<String> channels;
    @NotBlank String iconUrl;
    @NotBlank String token;
    @NotBlank String username;
}
