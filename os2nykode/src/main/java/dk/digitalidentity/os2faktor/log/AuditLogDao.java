package dk.digitalidentity.os2faktor.log;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import dk.digitalidentity.os2faktor.log.model.AuditLog;

public interface AuditLogDao extends CrudRepository<AuditLog, Long> {
	void deleteByTimestampBefore(Date before);
	List<AuditLog> findByChangedAccountInAndTimestampAfter(List<String> sAMAccountNames, Date after);
}
