package dk.digitalidentity.os2faktor.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.OperationNotSupportedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dk.digitalidentity.os2faktor.config.Constants;
import dk.digitalidentity.os2faktor.controller.model.NewPasswordForm;
import dk.digitalidentity.os2faktor.controller.validators.PasswordValidator;
import dk.digitalidentity.os2faktor.log.AuditLogService;
import dk.digitalidentity.os2faktor.security.SecurityUtil;
import dk.digitalidentity.os2faktor.service.ChildrenCprService;
import dk.digitalidentity.os2faktor.service.LdapService;
import dk.digitalidentity.os2faktor.service.PasswordPolicyService;
import dk.digitalidentity.os2faktor.service.RESTService;
import dk.digitalidentity.os2faktor.service.SQLService;
import dk.digitalidentity.os2faktor.service.dto.UserDTO;
import dk.digitalidentity.os2faktor.service.model.UsernameAndPassword;
import dk.digitalidentity.os2faktor.util.Utilities;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class PasswordResetController {

	@Autowired
	private LdapService ldapService;
	
	@Autowired
	private SQLService sqlService;

	@Autowired
	private RESTService restService;

	@Autowired
	private PasswordValidator passwordValidator;
	
	@Autowired
	private PasswordPolicyService passwordPolicyService;

	@Autowired
	private ChildrenCprService childrenCprService;

	@Autowired
	private AuditLogService auditLogService;
	
	@Value("${ldap.base}")
	private String ldapBase;

	@Value("${ldap.groups.cannotChangePwd:}")
	private String groupCannotChangePwd;

	@Value("${ldap.groups.cannotBeChangedPwdOn:}")
	private String groupCannotBeChangedPwdOn;
	
	@Value("${ldap.groups.canChangeOthersPwd:}")
	private String groupPasswordAdmins;
	
	@Value("${ldap.groups.masterPwdAdmins:}")
	private String groupPasswordMasterAdmins;
	
	@Value("${ldap.groups.pwdCirclesOU:}")
	private String passwordGroupsOU;

	@Value("${ssn.lookup.method:AD}")
	private String ssnLookupMethod;

	@InitBinder("newPasswordForm")
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(passwordValidator);
	}
	
	@PostConstruct
	public void passwordGroupOUDNFix() {
		if (!StringUtils.isEmpty(passwordGroupsOU)) {
			if (!passwordGroupsOU.toLowerCase().endsWith(ldapBase.toLowerCase())) {
				passwordGroupsOU = passwordGroupsOU + "," + ldapBase;
			}
		}
	}
	
	@GetMapping("/password/reset")
	public String resetGet(Model model, HttpServletRequest request) throws Exception {
		if (isLoggedInAndProcessed(request)) {
			return "redirect:/password/pickuser";
		}

		// if we cannot extract an SSN, go back to the front page
		String ssn = SecurityUtil.getSsn();
		if (StringUtils.isEmpty(ssn)) {
			log.warn("No SSN found on session!");
			return "redirect:/failed?cause=NoSSN";
		}

		// fetch ALL sAMAccountNames with that SSN associated with it
		List<UserDTO> sAMAccountNames = new ArrayList<UserDTO>();
		if ("AD".equals(ssnLookupMethod)) {
			sAMAccountNames = ldapService.getSAMAccountNames(ssn);
		}
		else if ("SQL".equals(ssnLookupMethod)) {
			sAMAccountNames = sqlService.getSAMAccountNames(ssn);
		}
		else if ("REST".equals(ssnLookupMethod)) {
			sAMAccountNames = restService.getSAMAccountNames(ssn);
		}

		// check if any of the users accounts are prohibited from changing password
		try {
			for (UserDTO userDTO : sAMAccountNames) {
				String sAMAccountName = userDTO.getSAMAccountName();

				if (!isAllowedToChangePassword(sAMAccountName)) {
					return "password/nopasswordchange";
				}
			}
		}
		catch (Exception ex) {
			log.error("Failed to validate if user can change password:" + Utilities.maskSsn(ssn), ex);

			return "password/error";
		}
		
		// set the currently logged in user field in the session if needed (depending on how they logged in,
		// it might not be set, and we need it for logging purposes - for those without an AD account, it gets
		// set to their SSN, masked though)
		boolean loggedInWithAD = true;
		if (request.getSession().getAttribute(Constants.SESSION_CURRENTLY_LOGGEDIN_USER) == null) {
			loggedInWithAD = false;

			if (sAMAccountNames.size() > 0) {
				request.getSession().setAttribute(Constants.SESSION_CURRENTLY_LOGGEDIN_USER, sAMAccountNames.get(0).getSAMAccountName());
			}
			else {
				request.getSession().setAttribute(Constants.SESSION_CURRENTLY_LOGGEDIN_USER, Utilities.maskSsn(ssn));
			}
		}

		// find all children for this user (they might not be in AD though)
		List<UserDTO> others = childrenCprService.getChildren(ssn);
		List<UserDTO> othersDTOs = new ArrayList<UserDTO>();

		// see if the children are in AD
		for (UserDTO user : others) {
			List<UserDTO> childrenAccounts = ldapService.getSAMAccountNames(user.getCpr());

			for (UserDTO account : childrenAccounts) {
				othersDTOs.add(account);
			}
		}

		// if the user logged in using AD, see if they can change password on any other user
		if (loggedInWithAD && !StringUtils.isEmpty(passwordGroupsOU) && (!StringUtils.isEmpty(groupPasswordAdmins) || !StringUtils.isEmpty(groupPasswordMasterAdmins))) {
			// find the groups that this user is a member of
			List<String> groups = ldapService.getGroups((String) request.getSession().getAttribute(Constants.SESSION_CURRENTLY_LOGGEDIN_USER));

			boolean canChangePasswordMasterAdmin = groups.stream().anyMatch(g -> g.equalsIgnoreCase(groupPasswordMasterAdmins));
			boolean canChangePasswordAdmin = canChangePasswordMasterAdmin || groups.stream().anyMatch(g -> g.equalsIgnoreCase(groupPasswordAdmins));
			
			// who is the user allowed to change password on?
			if (canChangePasswordAdmin) {
	
				// filter the users list of group memberships, so we only get the "password-circles" that the user is a member of
				if (!StringUtils.isEmpty(groupCannotBeChangedPwdOn)) {
					groups.removeIf(c -> c.toLowerCase().equals(groupCannotBeChangedPwdOn.toLowerCase()));
				}
				if (!StringUtils.isEmpty(groupCannotChangePwd)) {
					groups.removeIf(c -> c.toLowerCase().equals(groupCannotChangePwd.toLowerCase()));
				}
				if (!StringUtils.isEmpty(groupPasswordMasterAdmins)) {
					groups.removeIf(c -> c.toLowerCase().equals(groupPasswordMasterAdmins.toLowerCase()));
				}
				if (!StringUtils.isEmpty(groupPasswordMasterAdmins)) {
					groups.removeIf(g -> !g.toLowerCase().contains(passwordGroupsOU.toLowerCase()));
				}
				
				groups.removeIf(g -> !g.toLowerCase().contains(passwordGroupsOU.toLowerCase()));

				if (groups.size() > 0) {
					List<String> pwdResetProhibited = new ArrayList<>();
	
					// if the user is not a master-admin, do not allow changing password on admins
					if (!canChangePasswordMasterAdmin) {
						pwdResetProhibited.addAll(ldapService.getMembers(groupPasswordAdmins.toLowerCase())
								.stream()
								.map(u -> u.getSAMAccountName().toLowerCase())
								.collect(Collectors.toList()));
					}
					
					// no-one can change password on master-admins
					if (!StringUtils.isEmpty(groupPasswordMasterAdmins)) {
						pwdResetProhibited.addAll(ldapService.getMembers(groupPasswordMasterAdmins.toLowerCase())
								.stream()
								.map(u -> u.getSAMAccountName().toLowerCase())
								.collect(Collectors.toList()));
					}
	
					// finally a list of user accounts no-one can change password on
					if (!StringUtils.isEmpty(groupCannotBeChangedPwdOn)) {
						pwdResetProhibited.addAll(ldapService.getMembers(groupCannotBeChangedPwdOn.toLowerCase())
								.stream()
								.map(u -> u.getSAMAccountName().toLowerCase())
								.collect(Collectors.toList()));
					}
	
					// find all members of the password-circles that the user is a member of
					for (String groupDN : groups) {
						List<UserDTO> temp = ldapService.getMembers(groupDN);
	
						for (UserDTO member : temp) {
							if (!pwdResetProhibited.contains(member.getSAMAccountName().toLowerCase())) {
								boolean found = false;
								for (UserDTO existing : othersDTOs) {
									if (existing.getSAMAccountName().equalsIgnoreCase(member.getSAMAccountName())) {
										found = true;
										break;
									}
								}
								
								if (!found) {
									othersDTOs.add(member);
								}
							}
						}
					}
				}
			}
		}
		
		request.getSession().setAttribute(Constants.SESSION_SAMACCOUNTNAMES, sAMAccountNames);
		request.getSession().setAttribute(Constants.SESSION_OTHERSAMACCOUNTNAMES, othersDTOs);

		model.addAttribute("users", sAMAccountNames);
		model.addAttribute("others", othersDTOs);

		return "redirect:/password/pickuser";
	}
	
	@SuppressWarnings("unchecked")
	@GetMapping("/password/pickuser")
	public String pickUser(Model model, HttpServletRequest request) throws Exception {
		List<UserDTO> sAMAccountNames = new ArrayList<UserDTO>();
		List<UserDTO> othersAccountNames = new ArrayList<UserDTO>();

		Object oAccountNames = request.getSession().getAttribute(Constants.SESSION_SAMACCOUNTNAMES);
		if (oAccountNames != null && oAccountNames instanceof List<?> && ((List<?>)oAccountNames).size() > 0) {
			sAMAccountNames.addAll((List<UserDTO>)oAccountNames);
		}

		Object oOtherAccountNames = request.getSession().getAttribute(Constants.SESSION_OTHERSAMACCOUNTNAMES);
		if (oOtherAccountNames != null && oOtherAccountNames instanceof List<?> && ((List<?>) oOtherAccountNames).size() > 0) {
			othersAccountNames.addAll((List<UserDTO>) oOtherAccountNames);
		}

		List<String> allAccounts = Stream.concat(
				sAMAccountNames.stream().map(UserDTO::getSAMAccountName),
				othersAccountNames.stream().map(UserDTO::getSAMAccountName))
				.collect(Collectors.toList());
		
		if (allAccounts.size() == 0) {
			log.warn("User did not have any accounts to change password on");
			return "redirect:/failed?cause=NoAccount";
		}

		model.addAttribute("users", sAMAccountNames);
		model.addAttribute("others", othersAccountNames);
		model.addAttribute("logs", auditLogService.findByChangedAccountIn(allAccounts));

		return "password/pickuser";
	}

	public boolean isAllowedToChangePassword(String sAMAccountName) throws Exception {
		if (StringUtils.isEmpty(groupCannotChangePwd)) {
			return true;
		}

		List<String> groups = ldapService.getGroups(sAMAccountName);

		return !groups.contains(groupCannotChangePwd.toLowerCase());
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/password/reset/{sAMAccountName}")
	public String resetGetWithSAMAccountName(Model model, @PathVariable("sAMAccountName") String sAMAccountName, @RequestParam(name = "badPassword", defaultValue = "false") boolean badPassword, HttpServletRequest request) throws Exception {
		List<UserDTO> sAMAccountNames = new ArrayList<>();
		List<UserDTO> childrensAccountNames = new ArrayList<>();

		Object o = request.getSession().getAttribute(Constants.SESSION_SAMACCOUNTNAMES);
		if (o != null && (o instanceof List<?>)) {
			sAMAccountNames = (List<UserDTO>) request.getSession().getAttribute(Constants.SESSION_SAMACCOUNTNAMES);
		}
		
		Object c = request.getSession().getAttribute(Constants.SESSION_OTHERSAMACCOUNTNAMES);
		if (c != null && (c instanceof List<?>)) {
			childrensAccountNames = (List<UserDTO>) request.getSession().getAttribute(Constants.SESSION_OTHERSAMACCOUNTNAMES);
		}
		
		List<String> sAMAccountNamesAsStrings = sAMAccountNames.stream().map(s -> s.getSAMAccountName()).collect(Collectors.toList());
		
		if (!sAMAccountNamesAsStrings.contains(sAMAccountName) && !childrensAccountNames.stream().anyMatch(a -> a.getSAMAccountName().equals(sAMAccountName))) {
			log.error("User picked a sAMAccountName that his/her ssn is not associated with...");

			return "password/error";
		}
		
		request.getSession().setAttribute(Constants.SESSION_SAMACCOUNTNAME, sAMAccountName);
		model.addAttribute("newPasswordForm", new NewPasswordForm());
		model.addAttribute("policy", passwordPolicyService.getCombinedPasswordPolicy());

		if (badPassword) {
			model.addAttribute("badPassword", true);
		}

		return "password/newPassword";
	}

	@PostMapping(path = "/password/reset", consumes= "application/x-www-form-urlencoded;charset=UTF-8")
	public String resetPassword(Model model, @Valid @ModelAttribute("newPasswordForm") NewPasswordForm newPasswordForm , BindingResult bindingResult, HttpServletRequest request) throws Exception {
		if (bindingResult.hasErrors()) {
			model.addAttribute("newPasswordForm", newPasswordForm);
			model.addAttribute("policy", passwordPolicyService.getCombinedPasswordPolicy());

			return "password/newPassword";
		}

		String sAMAccountName = (String) request.getSession().getAttribute(Constants.SESSION_SAMACCOUNTNAME);
		if (sAMAccountName == null) {
			return "redirect:/password/reset/login";
		}

		try {
			UsernameAndPassword result = ldapService.resetPassword(sAMAccountName, newPasswordForm.getNewPassword());
			auditLogService.log(sAMAccountName);

			log.info("Password changed for '" + result.getUsername() + "'");
			SecurityUtil.clearSelectedSAMAccountName();

			return "password/success";
		}
		catch (Exception ex) {
			if (ex instanceof OperationNotSupportedException && ex.getMessage().contains("52D")) {
				log.warn("New password for " + sAMAccountName + " was rejected by Active Directory: " + ex.getMessage());

				return "redirect:/password/reset/" + sAMAccountName + "?badPassword=true";
			}

			log.error("Failed to change password for user with samAccountName=" + sAMAccountName, ex);
		}

		model.addAttribute("message", "Failed to change password.");

		return "password/error";
	}
	
	private boolean isLoggedInAndProcessed(HttpServletRequest request) {
		Object sAMAccountNames = request.getSession().getAttribute(Constants.SESSION_SAMACCOUNTNAMES);
		Object othersAccountNames = request.getSession().getAttribute(Constants.SESSION_OTHERSAMACCOUNTNAMES);
		
		if (sAMAccountNames != null && sAMAccountNames instanceof List<?> && ((List<?>)sAMAccountNames).size() > 0) {
			return true;
		}
		else if (othersAccountNames != null && othersAccountNames instanceof List<?> && ((List<?>) othersAccountNames).size() > 0) {
			return true;
		}
		
		return false;
	}
}
