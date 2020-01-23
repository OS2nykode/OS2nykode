package dk.digitalidentity.os2faktor.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.os2faktor.service.dto.UserDTO;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class ChildrenCprService {
	
	@Value("${omfamilie.url:}")
	private String cprUrl;

	public List<UserDTO> getChildren(String cpr) {
		if (StringUtils.isEmpty(cpr) || StringUtils.isEmpty(cprUrl)) {
			return new ArrayList<UserDTO>();
		}

		RestTemplate restTemplate = new RestTemplate();
		String resourceUrl = cprUrl + "?cpr=" + cpr;

		try {
			ResponseEntity<List<UserDTO>> response = restTemplate.exchange(resourceUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDTO>>() {
				
			});
	
			return response.getBody();
		}
		catch (Exception ex) {
			log.error("Cannot access familie lookup service", ex);
		}
		
		return new ArrayList<UserDTO>();
	}
}
