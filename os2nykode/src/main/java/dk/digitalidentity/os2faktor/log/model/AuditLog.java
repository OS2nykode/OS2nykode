package dk.digitalidentity.os2faktor.log.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private Date timestamp;

	@Column(nullable = false, name = "user_name")
	private String username;
	
	@Column(nullable = false, name = "user_cpr")
	private String userSsn;

	@Column(nullable = false)
	private String changedAccount;
}
