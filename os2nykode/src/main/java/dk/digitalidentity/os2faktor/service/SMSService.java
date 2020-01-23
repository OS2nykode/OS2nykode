package dk.digitalidentity.os2faktor.service;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import dk.digitalidentity.os2faktor.service.dto.SendMessageDTO;
import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class SMSService {
	private RestTemplate restTemplate;
	
	@Value(value = "${sms.gateway.url:}")
	private String smsGatewayUrl;
	
	@Value("${sms.message:Dit kodeord er blevet skiftet}")
	private String changePasswordMessage;

	public SMSService() {
		restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {

        	@Override
        	public boolean hasError(ClientHttpResponse response) throws IOException {
        		return false;
        	}
        });
	}

	@Async
	public void sendMessage(String phoneNumber) {
		if (StringUtils.isEmpty(smsGatewayUrl)) {
			log.debug("SMS Gateway not enabled - not sending SMS to user");
			return;
		}

		if (!smsGatewayUrl.endsWith("/")) {
			smsGatewayUrl += "/";
		}

		HttpEntity<SendMessageDTO> request = new HttpEntity<SendMessageDTO>(new SendMessageDTO(changePasswordMessage, Collections.singletonList(phoneNumber)));
		try {
			ResponseEntity<String> response = restTemplate.postForEntity(smsGatewayUrl + "api/gateway", request, String.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				log.error("Failed to send SMS through gateway: " + response.getStatusCodeValue() + " / " + response.getBody());
				return;
			}
		}
		catch (RestClientException ex) {
			log.error("Failed to send SMS through gateway", ex);
		}
	}
}
