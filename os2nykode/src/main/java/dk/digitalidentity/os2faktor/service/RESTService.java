package dk.digitalidentity.os2faktor.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.os2faktor.service.dto.RESTServiceSAMAccountNameResponse;
import dk.digitalidentity.os2faktor.service.dto.RESTServiceSSNResponse;
import dk.digitalidentity.os2faktor.service.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RESTService {

	// https://domain.com/path?userId={sAMAccountName}
	@Value("${rest.ssnLookupUrl:}")
	private String ssnLookupUrl;
	
	// https://domain.com/otherpath?cpr={SSN}
	@Value("${rest.sAMAccountNameLookupUrl:}")
	private String sAMAccountNameLookupUrl;
	
	@Autowired
	private RestTemplate restTemplate;

	public String getSsn(String sAMAccountName) {
		try {
			if (!StringUtils.isEmpty(ssnLookupUrl)) {
                String url = ssnLookupUrl.replace("{sAMAccountName}", sAMAccountName);

                ResponseEntity<RESTServiceSSNResponse> response = restTemplate.getForEntity(url, RESTServiceSSNResponse.class); 
                if (response.getStatusCodeValue() >= 200 || response.getStatusCodeValue() <= 299) {
                	String cpr = response.getBody().getResult();

                	cpr = cpr.replace("-", "");
                    cpr = cpr.replace(" ", "");
                    
                    return cpr;
                }
                else {
                	log.error("Failed to connect to REST service (" + response.getStatusCodeValue() + ")");
                }
			}
			else {
				log.warn("No REST service URL configured");
			}
		}
		catch (Exception ex) {
			log.error("Failed to query REST Service for SSN", ex);
		}

		return null;
	}
	
	public List<UserDTO> getSAMAccountNames(String ssn) {
		List<UserDTO> result = new ArrayList<>();
		
		try {
			if (!StringUtils.isEmpty(sAMAccountNameLookupUrl)) {
                String url = ssnLookupUrl.replace("{SSN}", ssn);

                ResponseEntity<RESTServiceSAMAccountNameResponse> response = restTemplate.getForEntity(url, RESTServiceSAMAccountNameResponse.class); 
                if (response.getStatusCodeValue() >= 200 || response.getStatusCodeValue() <= 299) {
                	RESTServiceSAMAccountNameResponse body = response.getBody();

                	for (String sAMAccountName : body.getAccounts()) {
						UserDTO dto = new UserDTO();
						dto.setName(body.getName());
						dto.setSAMAccountName(sAMAccountName);

						result.add(dto);
                	}
                }
                else {
                	log.error("Failed to connect to REST service (" + response.getStatusCodeValue() + ")");
                }

				return result;
			}
			else {
				log.warn("No REST service URL configured");
			}
		}
		catch (Exception ex) {
			log.error("Failed to query REST Service for sAMAccountNames", ex);
		}
		
		return result;
	}
}
