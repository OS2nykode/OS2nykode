package dk.digitalidentity.os2faktor.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
	private String name;
	private String cpr;
	private String sAMAccountName;
}
