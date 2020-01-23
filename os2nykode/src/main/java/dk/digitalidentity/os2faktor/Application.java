package dk.digitalidentity.os2faktor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = { "classpath:saml-default.properties", "classpath:default.properties", "classpath:custom.properties" }, ignoreResourceNotFound = true)
@SpringBootApplication(scanBasePackages = "dk.digitalidentity")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}