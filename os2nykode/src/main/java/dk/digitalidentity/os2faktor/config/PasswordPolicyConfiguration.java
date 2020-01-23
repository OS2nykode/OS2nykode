package dk.digitalidentity.os2faktor.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "password")
public class PasswordPolicyConfiguration {
	private PasswordPolicy defaultPolicy;
	private List<PasswordPolicy> policies;
}
