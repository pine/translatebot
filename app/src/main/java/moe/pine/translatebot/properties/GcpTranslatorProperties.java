package moe.pine.translatebot.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties("gcp-translator")
public class GcpTranslatorProperties {
    @NotBlank String credentials;
    @NotBlank String projectId;
    @NotBlank String location;
}
