package dk.digitalidentity.os2faktor.service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RESTServiceSAMAccountNameResponse {
	private String name;
	private List<String> accounts;
}
