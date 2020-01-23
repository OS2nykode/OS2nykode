package dk.digitalidentity.os2faktor.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordPolicy {
	private String groupName;
	private int minLength;
	private int maxLength;
	private boolean upperAndLowerCaseRequired;
	private boolean specialCharactersRequired;
	private boolean lettersRequired;
	private boolean digitsRequired;
	
	public boolean showRequireDigits() {
		if (digitsRequired) {
			// if no casing requirements are present, and letters are required, we show a combined message instead
			if (upperAndLowerCaseRequired || !lettersRequired) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean showRequireLettersAndDigits() {
		// if casing requirement is set, then we cannot create a combined message
		if (!upperAndLowerCaseRequired && digitsRequired && lettersRequired) {
			return true;
		}
		
		return false;
	}
}
