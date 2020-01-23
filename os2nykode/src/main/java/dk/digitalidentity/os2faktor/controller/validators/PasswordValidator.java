package dk.digitalidentity.os2faktor.controller.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import dk.digitalidentity.os2faktor.config.PasswordPolicy;
import dk.digitalidentity.os2faktor.controller.model.NewPasswordForm;
import dk.digitalidentity.os2faktor.service.PasswordPolicyService;

@Component
public class PasswordValidator implements Validator {

	@Autowired
	private PasswordPolicyService passwordPolicyService;

	@Override
	public boolean supports(Class<?> aClass) {
		return (NewPasswordForm.class.isAssignableFrom(aClass));
	}

	@Override
	public void validate(Object o, Errors errors) {
		NewPasswordForm newPasswordForm = (NewPasswordForm) o;

		if (StringUtils.isEmpty(newPasswordForm.getNewPassword())) {
			errors.rejectValue("newPassword", "html.errors.password.empty");
		}
		else if (!newPasswordForm.getNewPassword().equals(newPasswordForm.getConfirmPassword())) {
			errors.rejectValue("confirmPassword", "html.errors.confirmPassword.match");
		}
		else if (!goodPassword(newPasswordForm.getNewPassword())) {
			errors.rejectValue("newPassword", "html.errors.password.bad");
		}
	}

	private boolean goodPassword(String newPassword) {
		PasswordPolicy policy = passwordPolicyService.getCombinedPasswordPolicy();

		boolean hasLetter = false;
		boolean hasDigit = false;
		boolean hasUpperCase = false;
		boolean hasLowerCase = false;
		boolean hasSpecialCharacter = false;

		if (newPassword.length() < policy.getMinLength()) {
			return false;
		}
		
		if (newPassword.length() > policy.getMaxLength()) {
			return false;
		}
		
		for (char c : newPassword.toCharArray()) {
			if (Character.isDigit(c)) {
				hasDigit = true;
			}
			else if (Character.isLetter(c)) {
				if (Character.isUpperCase(c)) {
					hasUpperCase = true;
				}
				else {
					hasLowerCase = true;
				}

				hasLetter = true;
			}
			else {
				hasSpecialCharacter = true;
			}
		}
		
		if (policy.isDigitsRequired() && !hasDigit) {
			return false;
		}
		else if (policy.isLettersRequired() && !hasLetter) {
			return false;
		}
		else if (policy.isSpecialCharactersRequired() && !hasSpecialCharacter) {
			return false;
		}
		else if (policy.isUpperAndLowerCaseRequired() && (!hasUpperCase || !hasLowerCase)) {
			return false;
		}

		return true;
	}
}
