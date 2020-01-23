package dk.digitalidentity.os2faktor.util;

public class Utilities {

	public static String maskSsn(String ssn) {
		if (ssn != null && (ssn.length() == 10 || ssn.length() == 11)) {
			return ssn.substring(0, 6) + "-xxxx";
		}

		return ssn;
	}
}
