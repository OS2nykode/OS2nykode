package dk.digitalidentity.os2faktor.service;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dk.digitalidentity.os2faktor.service.dto.UserDTO;
import dk.digitalidentity.os2faktor.service.model.UsernameAndPassword;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LdapService {

	@Autowired
	private LdapTemplate ldapTemplate;

	@Autowired
	private SMSService smsService;

	@Value("${ldap.field.ssn}")
	private String ssnField;
	
	@Value("${ldap.field.mobile:}")
	private String mobileField;
	
	@Value("${ldap.groups.cannotChangePwd}")
	private String passwordChangePreventGroup;
	
	@Value("${ldap.groups.pwdCirclesOU}")
	private String passwordChangeBaseOU;

	public LdapService(@Value("${ldap.cert.trustall:false}") boolean trustAllCert) {
		if (trustAllCert) {
			allowUntrustedCert();
		}
	}
	
	public String getSsn(String sAMAccountName) {
		List<DirContextOperations> result = ldapTemplate.search(query()
			.where("sAMAccountName").is(sAMAccountName), new AbstractContextMapper<DirContextOperations>() {

				@Override
				protected DirContextOperations doMapFromContext(DirContextOperations ctx) {
					return ctx;
				}
			}
		);

		if (result != null && result.size() > 0) {
			DirContextOperations personContext = result.get(0);
			String ssnValue = (String) personContext.getObjectAttribute(ssnField);
			
			if (ssnValue != null && ssnValue.length() > 0) {
				return ssnValue;
			}
		}
		
		return null;
	}
	
	public List<UserDTO> getSAMAccountNames(String ssn) throws Exception {
		if (ssn == null || ssn.length() != 10) {
			log.warn("Cannot lookup sAMAccountNames with ssn = '" + ssn + "' - it needs to be 10 characters in length");
			return new ArrayList<>();
		}

		// search with the given SSN (which does not have a dash)
		List<DirContextOperations> result1 = ldapTemplate.search(query()
			.where("objectclass").is("person")
			.and("userAccountControl:1.2.840.113556.1.4.803:").not().is("2")
			.and(ssnField).is(ssn), new AbstractContextMapper<DirContextOperations>() {

				@Override
				protected DirContextOperations doMapFromContext(DirContextOperations ctx) {
					return ctx;
				}
			}
		);

		// now search for all with a dash added (because...)
		String ssnWithSlash = ssn.substring(0, 6) + "-" + ssn.substring(6);		
		List<DirContextOperations> result2 = ldapTemplate.search(query()
			.where("objectclass").is("person")
			.and("userAccountControl:1.2.840.113556.1.4.803:").not().is("2")
			.and(ssnField).is(ssnWithSlash), new AbstractContextMapper<DirContextOperations>() {

				@Override
				protected DirContextOperations doMapFromContext(DirContextOperations ctx) {
					return ctx;
				}
			}
		);
		
		List<DirContextOperations> result = new ArrayList<>();
		if (result1 != null) {
			result.addAll(result1);
		}
		if (result2 != null) {
			result.addAll(result2);
		}

		List<UserDTO> sAMAccountNames = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			DirContextOperations personContext = result.get(i);
			String sAMAccountName = (String) personContext.getObjectAttribute("sAMAccountName");
			String name = (String) personContext.getObjectAttribute("displayName");

			UserDTO userDTO = new UserDTO();
			userDTO.setName(name);
			userDTO.setSAMAccountName(sAMAccountName);
			
			sAMAccountNames.add(userDTO);
		}
		
		return sAMAccountNames;
	}

	public List<String> getGroups(String sAMAccountName) throws Exception {
		List<String> result = new ArrayList<>();

		// recursive version, somewhat slow
		List<String> distinguishedNames = ldapTemplate.search(query()
				.where("sAMAccountName").is(sAMAccountName),
		        (AttributesMapper<String>) attrs -> attrs.get("distinguishedName").get().toString());

		if (distinguishedNames != null && distinguishedNames.size() > 0) {
			result = ldapTemplate.search(query()
					.searchScope(SearchScope.SUBTREE)
					.where("member:1.2.840.113556.1.4.1941:").is(distinguishedNames.get(0)),
			        (AttributesMapper<String>) attrs -> attrs.get("distinguishedName").get().toString().toLowerCase());
		}
		
		return result;
	}
	
	public List<UserDTO> getMembers(String groupName) throws Exception {
		List<UserDTO> result = new ArrayList<>();

		List<DirContextOperations> ctxs = ldapTemplate.search(query()
				.searchScope(SearchScope.SUBTREE)
				.where("objectclass").is("person")
				.and("memberOf:1.2.840.113556.1.4.1941:").is(groupName),
				new AbstractContextMapper<DirContextOperations>() {

					@Override
					protected DirContextOperations doMapFromContext(DirContextOperations ctx) {
						return ctx;
					}
				}
		);
		
		if (ctxs != null && ctxs.size() > 0) {
			for (DirContextOperations ctx : ctxs) {
				String sAMAccountName = (String) ctx.getObjectAttribute("sAMAccountName");
				String name = (String) ctx.getObjectAttribute("displayName");

				UserDTO userDto = new UserDTO();
				userDto.setName(name);
				userDto.setSAMAccountName(sAMAccountName);
				result.add(userDto);
			}
		}

		return result;
	}

	public boolean isAllowedToChangePassword(String sAMAccountName) throws Exception {
		List<String> groups = getGroups(sAMAccountName);

		return !groups.contains(passwordChangePreventGroup.toLowerCase());
	}

	public UsernameAndPassword resetPassword(String sAMAccountName, String newPassword) throws Exception {
		DirContextOperations ctx = getDirContextOperations(sAMAccountName);
		String upn = (String) ctx.getObjectAttribute("userPrincipalName");
		String mobile = (String) (StringUtils.isEmpty(mobileField) ? null : ctx.getObjectAttribute(mobileField));
		Name dn = ctx.getDn();

		final String password = "\"" + newPassword + "\"";
	    ModificationItem changePwd = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodepwd", password.getBytes("UTF-16LE")));

	    List<ModificationItem> mods = new ArrayList<>();
	    mods.add(changePwd);

    	ldapTemplate.modifyAttributes(dn, mods.toArray(new ModificationItem[0]));

    	if (!StringUtils.isEmpty(mobile) && mobile.length() >= 8) {
    		log.info("Sending SMS to " + sAMAccountName + " / " + mobile);
    		smsService.sendMessage(mobile);
    	}
    	
    	UsernameAndPassword usernameAndPassword = new UsernameAndPassword();
    	usernameAndPassword.setPassword(password.replace("\"", ""));
    	usernameAndPassword.setUsername(upn);
    	
		return usernameAndPassword;
	}

	private DirContextOperations getDirContextOperations(String sAMAccountName) throws Exception {
		List<DirContextOperations> result = ldapTemplate.search(query()
			.where("objectclass").is("person")
			.and("sAMAccountName").is(sAMAccountName), new AbstractContextMapper<DirContextOperations>() {

				@Override
				protected DirContextOperations doMapFromContext(DirContextOperations ctx) {
					return ctx;
				}
			}
		);

		// should NEVER happen
		if (result == null || result.size() == 0 || result.size() > 1) {
			throw new Exception("Found " + ((result != null) ? result.size() : -1) + " users with sAMAccountName " + sAMAccountName);
		}

		return result.get(0);
	}

	private static void allowUntrustedCert() {
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					;
				}
				
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					;
				}
			}
		};
		
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			SSLContext.setDefault(sc);
		}
		catch (Exception ex) {
			log.error("Failed to flag all certificates as trusted!", ex);
		}
	}
}
