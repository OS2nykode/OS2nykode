package dk.digitalidentity.os2faktor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Value("${rest.username:}")
	private String username;

	@Value("${rest.password:}")
	private String password;
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
			builder = builder.basicAuthentication(username, password);
		}

		return builder.build();
	}
}
