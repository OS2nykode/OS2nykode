package dk.digitalidentity.os2faktor.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import dk.digitalidentity.os2faktor.config.Constants;
import dk.digitalidentity.os2faktor.service.LdapService;
import dk.digitalidentity.os2faktor.service.RESTService;
import dk.digitalidentity.os2faktor.service.SQLService;
import dk.digitalidentity.saml.extension.SamlLoginPostProcessor;
import dk.digitalidentity.saml.model.TokenUser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoginPostProcesser implements SamlLoginPostProcessor {
	
	@Autowired
	private LdapService ldapService;

	@Autowired
	private SQLService sqlService;
	
	@Autowired
	private RESTService restService;

	@Value("${ssn.lookup.method:AD}")
	private String ssnLookupMethod;
	
	@Override
	public void process(TokenUser tokenUser) {
		HttpServletRequest request = getRequest();
		if (request != null) {			
			String sAMAccountName = tokenUser.getUsername();
			String ssn = null;
			
			try {
				if ("AD".equals(ssnLookupMethod)) {
					ssn = ldapService.getSsn(sAMAccountName);
				}
				else if ("SQL".equals(ssnLookupMethod)) {
					ssn = sqlService.getSsn(sAMAccountName);
				}
				else if ("REST".equals(ssnLookupMethod)) {
					ssn = restService.getSsn(sAMAccountName);
				}
			}
			catch (Exception ex) {
				log.error("Failed to lookup user in Active Directory", ex);
			}
			
			if (ssn != null) {
				request.getSession().setAttribute(Constants.SESSION_SSN, ssn);
				request.getSession().setAttribute(Constants.SESSION_CURRENTLY_LOGGEDIN_USER, sAMAccountName);
			}
			else {
				throw new UsernameNotFoundException("Brugeren " + sAMAccountName + " har ikke noget personnummer tilknyttet");
			}
		}
	}
	
	private static HttpServletRequest getRequest() {
		try {
			return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		}
		catch (IllegalStateException ex) {
			return null;
		}
	}
}
