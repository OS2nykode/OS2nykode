package dk.digitalidentity.os2faktor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dk.digitalidentity.os2faktor.config.PasswordPolicy;
import dk.digitalidentity.os2faktor.config.PasswordPolicyConfiguration;
import dk.digitalidentity.os2faktor.security.SecurityUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PasswordPolicyService {

	@Autowired
	private PasswordPolicyConfiguration passwordPolicyConfiguration;
	
	@Autowired
	private LdapService ldapService;

	public PasswordPolicy getCombinedPasswordPolicy() {
		String sAMAccountName = SecurityUtil.getSAMAccountName();
		if (sAMAccountName != null) {
			try {
				List<String> groups = ldapService.getGroups(sAMAccountName);
	
				if (groups != null && groups.size() > 0) {
					PasswordPolicy combinedPolicy = new PasswordPolicy();
					combinedPolicy.setMaxLength(255);
					boolean foundPolicy = false;

					if (passwordPolicyConfiguration.getPolicies() != null) {
						for (PasswordPolicy policy : passwordPolicyConfiguration.getPolicies()) {
							if (groups.contains(policy.getGroupName().toLowerCase())) {
								foundPolicy = true;
								
								combinedPolicy.setDigitsRequired(combinedPolicy.isDigitsRequired() || policy.isDigitsRequired());
								combinedPolicy.setLettersRequired(combinedPolicy.isLettersRequired() || policy.isLettersRequired());
								combinedPolicy.setSpecialCharactersRequired(combinedPolicy.isSpecialCharactersRequired() || policy.isSpecialCharactersRequired());
								combinedPolicy.setUpperAndLowerCaseRequired(combinedPolicy.isUpperAndLowerCaseRequired() || policy.isUpperAndLowerCaseRequired());
	
								if (policy.getMinLength() > combinedPolicy.getMinLength()) {
									combinedPolicy.setMinLength(policy.getMinLength());
								}
								
								if (policy.getMaxLength() < combinedPolicy.getMaxLength()) {
									combinedPolicy.setMaxLength(policy.getMaxLength());
								}
							}
						}
					}
					
					if (foundPolicy) {
						return combinedPolicy;
					}
				}
			}
			catch (Exception ex) {
				log.error("Failed to lookup groups for " + sAMAccountName, ex);
			}
		}
		
		return passwordPolicyConfiguration.getDefaultPolicy();
	}
}
